/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.controller;

import cz.certicon.routing.GlobalOptions;
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.astar.StraightLineAStarRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.ch.ContractionHierarchiesRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.ch.OptimizedContractionHierarchiesRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.ch.OptimizedContractionHierarchiesRoutingAlgorithmWithUB;
import cz.certicon.routing.application.algorithm.algorithms.dijkstra.DijkstraRoutingAlgorithm;
import cz.certicon.routing.controller.commands.AlgorithmCreateCommand;
import cz.certicon.routing.controller.commands.CoordinateReaderCreateCommand;
import cz.certicon.routing.controller.commands.ExecuteCommand;
import cz.certicon.routing.controller.commands.GraphLoadCommand;
import cz.certicon.routing.controller.commands.NodeSearcherCreateCommand;
import cz.certicon.routing.data.DistanceType;
import cz.certicon.routing.data.ch.ContractionHierarchiesDataRW;
import cz.certicon.routing.data.ch.sqlite.SqliteContractionHierarchiesDataRW;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.coordinates.sqlite.SqliteCoordinateRW;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.data.graph.sqlite.SqliteGraphRW;
import cz.certicon.routing.data.nodesearch.NodeSearcher;
import cz.certicon.routing.data.nodesearch.sqlite.SqliteNodeSearcher;
import cz.certicon.routing.model.ExecutionStats;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.PathStats;
import cz.certicon.routing.model.Result;
import cz.certicon.routing.model.basic.Length;
import cz.certicon.routing.model.basic.LengthUnits;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Time;
import cz.certicon.routing.model.basic.TimeUnits;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.model.entity.Shortcut;
import cz.certicon.routing.model.entity.neighbourlist.NeighborListGraphEntityFactory;
import cz.certicon.routing.utils.measuring.TimeLogger;
import cz.certicon.routing.view.MainUserInterface;
import cz.certicon.routing.view.cli.CliMainUserInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Controller {

    private MainUserInterface userInterface;

    public void run( String... args ) {
        userInterface = new CliMainUserInterface();
        userInterface.setOnExecutionListener( ( Input input ) -> {
            try {
                Result result = execute( input );
                userInterface.displayResult( input, result );
            } catch ( IOException ex ) {
                userInterface.report( ex );
            }
        } );
        userInterface.setOnExceptionThrownListener( ( Exception ex ) -> {
            userInterface.report( ex );
        } );
        userInterface.run( args );
    }

    private Result execute( Input input ) throws IOException {
        DistanceFactory distanceFactory = input.getDistanceType().getDistanceFactory();
        GraphEntityFactory graphEntityFactory = new NeighborListGraphEntityFactory();
        Graph graph = loadGraph( input, graphEntityFactory, distanceFactory );
        Trinity<Map<Node.Id, Integer>, List<Shortcut>, DistanceType> preprocessedData = loadPreprocessedData( input, graph, graphEntityFactory, input.getDistanceType() );
        RoutingAlgorithm routingAlgorithm = createAlgorithm( input, graph, graphEntityFactory, distanceFactory, preprocessedData );
        NodeSearcher nodeSearcher = createNodeSearcher( input );
        CoordinateReader coordinateReader = createCoordinateReader( input );
        List<ExecutionStats> executions = new ArrayList<>();
        List<PathStats> paths = new ArrayList<>();
        GlobalOptions.MEASURE_TIME = true;

        userInterface.setNumOfUpdates( 100 );
        userInterface.init( input.getNumberOfRuns() * input.getData().size(), 1.0 );
        for ( int run = 0; run < input.getNumberOfRuns(); run++ ) {

            for ( int i = 0; i < input.getData().size(); i++ ) {
                Trinity<Integer, Coordinate, Coordinate> pair = input.getData().get( i );
                try {
                    Coordinate from = pair.b;
                    Coordinate to = pair.c;
                    TimeLogger.log( TimeLogger.Event.NODE_SEARCHING, TimeLogger.Command.START );
                    Pair<Map<Node.Id, Distance>, Long> fromMap = nodeSearcher.findClosestNodes( from, distanceFactory, NodeSearcher.SearchFor.SOURCE );
                    Pair<Map<Node.Id, Distance>, Long> toMap = nodeSearcher.findClosestNodes( to, distanceFactory, NodeSearcher.SearchFor.TARGET );
                    TimeLogger.log( TimeLogger.Event.NODE_SEARCHING, TimeLogger.Command.STOP );

                    Path route = routingAlgorithm.route( fromMap.a, toMap.a );
                    TimeLogger.log( TimeLogger.Event.PATH_LOADING, TimeLogger.Command.START );
                    route.setSourceOrigin( from, fromMap.b );
                    route.setTargetOrigin( to, toMap.b );
                    route.loadCoordinates( coordinateReader );
                    TimeLogger.log( TimeLogger.Event.PATH_LOADING, TimeLogger.Command.STOP );

                    if ( paths.size() <= i ) {
                        Time time = new Time( TimeUnits.SECONDS, (long) route.getTime() );
                        Length length = new Length( LengthUnits.METERS, (long) route.getLength() );
                        paths.add( new PathStats( pair.a, time, length ) );
                    }
                    if ( executions.size() <= i ) {
                        executions.add( new ExecutionStats(
                                pair.a,
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.NODE_SEARCHING ).getTime(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTING ).getTime(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTE_BUILDING ).getTime(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.PATH_LOADING ).getTime() ) );
                    } else {
                        ExecutionStats last = executions.get( i );
                        executions.set( i, new ExecutionStats(
                                pair.a,
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.NODE_SEARCHING ).getTime().add( last.getNodeSearchTime() ),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTING ).getTime().add( last.getRouteTime() ),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTE_BUILDING ).getTime().add( last.getRouteBuildingTime() ),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.PATH_LOADING ).getTime().add( last.getPathLoadTime() ) ) );
                    }
                } catch ( IOException ex ) {
                    Logger.getLogger( ExecuteCommand.class.getName() ).log( Level.SEVERE, null, ex );
                }
                userInterface.nextStep();
            }
        }
        for ( int i = 0; i < input.getData().size(); i++ ) {
            ExecutionStats last = executions.get( i );
            executions.set( i, new ExecutionStats(
                    last.getId(),
                    last.getNodeSearchTime().divide( input.getNumberOfRuns() ),
                    last.getRouteTime().divide( input.getNumberOfRuns() ),
                    last.getRouteBuildingTime().divide( input.getNumberOfRuns() ),
                    last.getPathLoadTime().divide( input.getNumberOfRuns() ) ) );
        }

        GlobalOptions.MEASURE_TIME = false;
        return new Result( paths, executions );
    }

    private Graph loadGraph( Input input, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory ) throws IOException {
        GraphReader graphReader;
        switch ( input.getInputType() ) {
            case SQLITE:
                graphReader = new SqliteGraphRW( input.getProperties() );
                break;
            default:
                throw new IllegalArgumentException( "Unsupported input type: " + input.getInputType().name() );
        }
        return graphReader.read( new Pair<>( graphEntityFactory, distanceFactory ) );
    }

    private Trinity<Map<Node.Id, Integer>, List<Shortcut>, DistanceType> loadPreprocessedData( Input input, Graph graph, GraphEntityFactory graphEntityFactory, DistanceType distanceType ) throws IOException {
        ContractionHierarchiesDataRW dataRw;
        switch ( input.getInputType() ) {
            case SQLITE:
                dataRw = new SqliteContractionHierarchiesDataRW( input.getProperties() );
                break;
            default:
                throw new IllegalArgumentException( "Unsupported input type: " + input.getInputType().name() );
        }
        return dataRw.read( new Trinity<>( graph, graphEntityFactory, distanceType ) );
    }

    private NodeSearcher createNodeSearcher( Input input ) throws IOException {
        switch ( input.getInputType() ) {
            case SQLITE:
                return new SqliteNodeSearcher( input.getProperties() );
            default:
                throw new IllegalArgumentException( "Unsupported input type: " + input.getInputType().name() );
        }
    }

    private CoordinateReader createCoordinateReader( Input input ) {
        switch ( input.getInputType() ) {
            case SQLITE:
                return new SqliteCoordinateRW( input.getProperties() );
            default:
                throw new IllegalArgumentException( "Unsupported input type: " + input.getInputType().name() );
        }
    }

    private RoutingAlgorithm createAlgorithm( Input input, Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, Trinity<Map<Node.Id, Integer>, List<Shortcut>, DistanceType> preprocessedData ) {

        switch ( input.getAlgorithmType() ) {
            case DIJKSTRA:
                return new DijkstraRoutingAlgorithm( graph, graphEntityFactory, distanceFactory );
            case ASTAR:
                return new StraightLineAStarRoutingAlgorithm( graph, graphEntityFactory, distanceFactory );
            case CONTRACTION_HIERARCHIES:
                preprocessedData.b.stream().forEach( ( shortcut ) -> {
                    graph.addEdge( shortcut );
                } );
                return new ContractionHierarchiesRoutingAlgorithm( graph, graphEntityFactory, distanceFactory, preprocessedData.a );
            case CONTRACTION_HIERARCHIES_OPTIMIZED:
                return new OptimizedContractionHierarchiesRoutingAlgorithm( graph, graphEntityFactory, distanceFactory, preprocessedData.b, preprocessedData.a );
            case CONTRACTION_HIERARCHIES_OPTIMIZED_UB:
                return new OptimizedContractionHierarchiesRoutingAlgorithmWithUB( graph, graphEntityFactory, preprocessedData.b, preprocessedData.a );
            default:
                throw new IllegalArgumentException( "Unsupported algoritghm type: " + input.getAlgorithmType().name() );
        }
    }
}
