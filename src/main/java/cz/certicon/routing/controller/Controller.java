/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.controller;

import cz.certicon.routing.GlobalOptions;
import cz.certicon.routing.application.algorithm.AlgorithmType;
import cz.certicon.routing.memsensitive.algorithm.Route;
import cz.certicon.routing.memsensitive.algorithm.RouteBuilder;
import cz.certicon.routing.memsensitive.algorithm.RouteNotFoundException;
import cz.certicon.routing.memsensitive.algorithm.RoutingAlgorithm;
import cz.certicon.routing.memsensitive.algorithm.algorithms.AstarRoutingAlgorithm;
import cz.certicon.routing.memsensitive.algorithm.algorithms.ContractionHierarchiesRoutingAlgorithm;
import cz.certicon.routing.memsensitive.algorithm.algorithms.ContractionHierarchiesUbRoutingAlgorithm;
import cz.certicon.routing.memsensitive.algorithm.algorithms.DijkstraRoutingAlgorithm;
import cz.certicon.routing.memsensitive.algorithm.common.SimpleRouteBuilder;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.ContractionHierarchiesPreprocessor;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.calculators.SpatialHeuristicEdgeDifferenceCalculator;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.strategies.NeighboursOnlyRecalculationStrategy;
import cz.certicon.routing.memsensitive.data.ch.ContractionHierarchiesDataRW;
import cz.certicon.routing.memsensitive.data.ch.NotPreprocessedException;
import cz.certicon.routing.memsensitive.data.ch.sqlite.SqliteContractionHierarchiesRW;
import cz.certicon.routing.memsensitive.data.graph.GraphReader;
import cz.certicon.routing.memsensitive.data.graph.sqlite.SqliteGraphReader;
import cz.certicon.routing.memsensitive.data.nodesearch.EvaluableOnlyException;
import cz.certicon.routing.memsensitive.data.nodesearch.NodeSearcher;
import cz.certicon.routing.memsensitive.data.nodesearch.sqlite.SqliteNodeSearcher;
import cz.certicon.routing.memsensitive.data.path.PathReader;
import cz.certicon.routing.memsensitive.data.path.sqlite.SqlitePathReader;
import cz.certicon.routing.memsensitive.data.turntables.TurnTablesReader;
import cz.certicon.routing.memsensitive.data.turntables.sqlite.SqliteTurnTablesReader;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.NodeSet;
import cz.certicon.routing.memsensitive.model.entity.Path;
import cz.certicon.routing.memsensitive.model.entity.PathBuilder;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.memsensitive.model.entity.ch.SimpleChDataBuilder;
import cz.certicon.routing.memsensitive.model.entity.ch.SimpleChDataFactory;
import cz.certicon.routing.memsensitive.model.entity.common.SimpleCoordinateSetBuilderFactory;
import cz.certicon.routing.memsensitive.model.entity.common.SimpleGraphBuilderFactory;
import cz.certicon.routing.memsensitive.model.entity.common.SimpleNodeSetBuilderFactory;
import cz.certicon.routing.memsensitive.model.entity.common.SimplePathBuilder;
import cz.certicon.routing.memsensitive.model.entity.common.SimpleTurnTablesBuilder;
import cz.certicon.routing.memsensitive.presentation.jxmapviewer.JxMapViewerFrame;
import cz.certicon.routing.model.ExecutionStats;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.PathStats;
import cz.certicon.routing.model.Result;
import cz.certicon.routing.model.basic.Length;
import cz.certicon.routing.model.basic.LengthUnits;
import cz.certicon.routing.model.basic.Time;
import cz.certicon.routing.model.basic.TimeUnits;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.CoordinateSetBuilderFactory;
import cz.certicon.routing.model.entity.NodeSetBuilderFactory;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import cz.certicon.routing.view.MainUserInterface;
import cz.certicon.routing.view.StatusEvent;
import cz.certicon.routing.view.cli.CliMainUserInterface;
import java.io.IOException;
import java.nio.ByteBuffer;
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
                userInterface.statusUpdate( StatusEvent.START_DISPLAYING_RESULT );
                userInterface.displayResult( input, result );
                userInterface.statusUpdate( StatusEvent.COMPLETED_DISPLAYING_RESULT );
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
        try {
            Thread.sleep( 3000);
        } catch ( InterruptedException ex ) {
            Logger.getLogger( Controller.class.getName() ).log( Level.SEVERE, null, ex );
        }
        GlobalOptions.DEBUG_DISPLAY = true;
        GlobalOptions.DEBUG_DISPLAY_PAUSE = 250;
        GlobalOptions.DEBUG_DISPLAY_PROPERTIES = input.getProperties();
        userInterface.statusUpdate( StatusEvent.START_LOADING_GRAPH );
        Graph graph = loadGraph( input );
        userInterface.statusUpdate( StatusEvent.COMPLETED_LOADING_GRAPH );
        userInterface.statusUpdate( StatusEvent.START_LOADING_PREPROCESSED_DATA );
        PreprocessedData preprocessedData = null;
        if ( input.getAlgorithmType().equals( AlgorithmType.CONTRACTION_HIERARCHIES )
                || input.getAlgorithmType().equals( AlgorithmType.CONTRACTION_HIERARCHIES_OPTIMIZED )
                || input.getAlgorithmType().equals( AlgorithmType.CONTRACTION_HIERARCHIES_OPTIMIZED_UB ) ) {
            preprocessedData = loadPreprocessedData( input, graph );
        }
        userInterface.statusUpdate( StatusEvent.COMPLETED_LOADING_PREPROCESSED_DATA );
        userInterface.statusUpdate( StatusEvent.START_PREPARING_ALGORITHM );
        RoutingAlgorithm<Graph> routingAlgorithm = createAlgorithm( input, graph, preprocessedData );
        userInterface.statusUpdate( StatusEvent.COMPLETED_PREPARING_ALGORITHM );
        NodeSearcher nodeSearcher = createNodeSearcher( input );
        PathReader<Graph> pathReader = createPathReader( input );
        PathBuilder<Path, Graph> pathBuilder = new SimplePathBuilder();
        List<ExecutionStats> executions = new ArrayList<>();
        List<PathStats> paths = new ArrayList<>();
        GlobalOptions.MEASURE_TIME = true;
        GlobalOptions.MEASURE_STATS = true;
        NodeSetBuilderFactory<NodeSet<Graph>> nsbFactory = new SimpleNodeSetBuilderFactory( graph, input.getDistanceType() );
        RouteBuilder<Route, Graph> routeBuilder = new SimpleRouteBuilder();
        CoordinateSetBuilderFactory coordinateSetBuilderFactory = new SimpleCoordinateSetBuilderFactory();
        userInterface.setNumOfUpdates( 100 );
        userInterface.init( input.getNumberOfRuns() * input.getData().size(), 1.0 );
        userInterface.statusUpdate( StatusEvent.START_COMPUTING );
        for ( int run = 0; run < input.getNumberOfRuns(); run++ ) {

            for ( int i = 0; i < input.getData().size(); i++ ) {

                Trinity<Integer, Coordinate, Coordinate> pair = input.getData().get( i );
                Coordinate from = pair.b;
                Coordinate to = pair.c;
                TimeLogger.log( TimeLogger.Event.NODE_SEARCHING, TimeLogger.Command.START );
                Path path = null;
                try {
                    NodeSet nodeSet = nodeSearcher.findClosestNodes( nsbFactory, from, to );
                    TimeLogger.log( TimeLogger.Event.NODE_SEARCHING, TimeLogger.Command.STOP );
                    Route route = routingAlgorithm.route( routeBuilder,
                            nodeSet.getMap( graph, NodeSet.NodeCategory.SOURCE ),
                            nodeSet.getMap( graph, NodeSet.NodeCategory.TARGET ) );
                    // print route
//                System.out.println( "ROUTE" );
//                Iterator<Pair<Long, Boolean>> it = route.getEdgeIterator();
//                while(it.hasNext()){
//                    System.out.println( it.next() );
//                }
                    // end print
                    TimeLogger.log( TimeLogger.Event.PATH_LOADING, TimeLogger.Command.START );
                    pathBuilder.clear();
                    path = pathReader.readPath( pathBuilder, graph, route, from, to );
                    // print path
//                System.out.println( "PATH" );
//                System.out.println( "length = " + path.getLength() );
//                System.out.println( "time = " + path.getTime() );
//                System.out.println( path.getCoordinates() );
                    // end print
                    TimeLogger.log( TimeLogger.Event.PATH_LOADING, TimeLogger.Command.STOP );
                } catch ( EvaluableOnlyException ex ) {
                    path = pathReader.readPath( pathBuilder, graph, ex.getEdgeId(), from, to );
                } catch ( RouteNotFoundException ex ) {
                    Logger.getLogger( Controller.class.getName() ).log( Level.SEVERE, null, ex );
//                    throw new RuntimeException( ex );
                    if ( paths.size() <= i ) {
                        paths.add( new PathStats( pair.a, new Time( TimeUnits.SECONDS, 1 ), new Length( LengthUnits.METERS, 1 ) ) );
                    }
                    if ( executions.size() <= i ) {
                        executions.add( new ExecutionStats(
                                pair.a,
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.NODE_SEARCHING ).getTime(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTING ).getTime(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTE_BUILDING ).getTime(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.PATH_LOADING ).getTime(),
                                StatsLogger.getValue( StatsLogger.Statistic.NODES_EXAMINED ),
                                StatsLogger.getValue( StatsLogger.Statistic.EDGES_EXAMINED ) ) );
                    }
                    continue;
                }
                if ( paths.size() <= i ) {
                    Time time = new Time( TimeUnits.SECONDS, path.getTime().getTime() );
                    Length length = new Length( LengthUnits.METERS, path.getLength().getLength() );
                    paths.add( new PathStats( pair.a, time, length ) );
                }
                if ( executions.size() <= i ) {
                    executions.add( new ExecutionStats(
                            pair.a,
                            TimeLogger.getTimeMeasurement( TimeLogger.Event.NODE_SEARCHING ).getTime(),
                            TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTING ).getTime(),
                            TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTE_BUILDING ).getTime(),
                            TimeLogger.getTimeMeasurement( TimeLogger.Event.PATH_LOADING ).getTime(),
                            StatsLogger.getValue( StatsLogger.Statistic.NODES_EXAMINED ),
                            StatsLogger.getValue( StatsLogger.Statistic.EDGES_EXAMINED ) ) );
                } else {
                    ExecutionStats last = executions.get( i );
                    executions.set( i, new ExecutionStats(
                            pair.a,
                            TimeLogger.getTimeMeasurement( TimeLogger.Event.NODE_SEARCHING ).getTime().add( last.getNodeSearchTime() ),
                            TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTING ).getTime().add( last.getRouteTime() ),
                            TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTE_BUILDING ).getTime().add( last.getRouteBuildingTime() ),
                            TimeLogger.getTimeMeasurement( TimeLogger.Event.PATH_LOADING ).getTime().add( last.getPathLoadTime() ),
                            StatsLogger.getValue( StatsLogger.Statistic.NODES_EXAMINED ) + last.getExaminedNodes(),
                            StatsLogger.getValue( StatsLogger.Statistic.EDGES_EXAMINED ) + last.getExaminedEdges() ) );
                }
                new JxMapViewerFrame().displayPath( path );
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
                    last.getPathLoadTime().divide( input.getNumberOfRuns() ),
                    last.getExaminedNodes() / input.getNumberOfRuns(),
                    last.getExaminedEdges() / input.getNumberOfRuns() ) );
        }

        GlobalOptions.MEASURE_TIME = false;
        userInterface.statusUpdate( StatusEvent.COMPLETED_COMPUTING );
        return new Result( paths, executions );
    }

    private Graph loadGraph( Input input ) throws IOException {
        GraphReader graphReader;
        TurnTablesReader ttReader;
        switch ( input.getInputType() ) {
            case SQLITE:
                graphReader = new SqliteGraphReader( input.getProperties() );
                ttReader = new SqliteTurnTablesReader( input.getProperties() );
                break;
            default:
                throw new IllegalArgumentException( "Unsupported input type: " + input.getInputType().name() );
        }
        Graph graph = graphReader.readGraph( new SimpleGraphBuilderFactory( input.getDistanceType() ), input.getDistanceType() );
        SimpleTurnTablesBuilder.TurnTablesContainer read = ttReader.read( graph, new SimpleTurnTablesBuilder() );
        graph.setTurnRestrictions( read.getTr() );
        return graph;
    }

    private PreprocessedData loadPreprocessedData( Input input, Graph graph ) throws IOException {
        ContractionHierarchiesDataRW dataRw;
        ContractionHierarchiesPreprocessor preprocessor = new ContractionHierarchiesPreprocessor( new NeighboursOnlyRecalculationStrategy() );
        preprocessor.setEdgeDifferenceCalculator( new SpatialHeuristicEdgeDifferenceCalculator( graph.getNodeCount() ) );
        switch ( input.getInputType() ) {

            case SQLITE:
                dataRw = new SqliteContractionHierarchiesRW( input.getProperties() );
                break;
            default:
                throw new IllegalArgumentException( "Unsupported input type: " + input.getInputType().name() );
        }
        return dataRw.read( new SimpleChDataFactory( graph, input.getDistanceType() ), graph, preprocessor );
    }

    private NodeSearcher createNodeSearcher( Input input ) throws IOException {
        switch ( input.getInputType() ) {
            case SQLITE:
                return new SqliteNodeSearcher( input.getProperties() );
            default:
                throw new IllegalArgumentException( "Unsupported input type: " + input.getInputType().name() );
        }
    }

    private PathReader<Graph> createPathReader( Input input ) {
        switch ( input.getInputType() ) {
            case SQLITE:
                return new SqlitePathReader( input.getProperties() );
            default:
                throw new IllegalArgumentException( "Unsupported input type: " + input.getInputType().name() );
        }
    }

    private RoutingAlgorithm createAlgorithm( Input input, Graph graph, PreprocessedData preprocessedData ) {

        switch ( input.getAlgorithmType() ) {
            case DIJKSTRA:
                return new DijkstraRoutingAlgorithm( graph );
            case ASTAR:
                return new AstarRoutingAlgorithm( graph, input.getDistanceType() );
            case CONTRACTION_HIERARCHIES:
                return new ContractionHierarchiesRoutingAlgorithm( graph, preprocessedData );
//            case CONTRACTION_HIERARCHIES_OPTIMIZED:
//                return new OptimizedContractionHierarchiesRoutingAlgorithm( graph, graphEntityFactory, distanceFactory, preprocessedData.b, preprocessedData.a );
            case CONTRACTION_HIERARCHIES_UB:
                return new ContractionHierarchiesUbRoutingAlgorithm( graph, preprocessedData );
            default:
                throw new IllegalArgumentException( "Unsupported algoritghm type: " + input.getAlgorithmType().name() );
        }
    }
}
