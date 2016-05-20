/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.controller.commands;

import cz.certicon.routing.GlobalOptions;
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.controller.Command;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.nodesearch.NodeSearcher;
import cz.certicon.routing.model.ExecutionStats;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.PathStats;
import cz.certicon.routing.model.Result;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Quaternion;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.model.entity.neighbourlist.NeighborListGraphEntityFactory;
import cz.certicon.routing.utils.measuring.TimeLogger;
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
public class ExecuteCommand implements Command<Input, Result> {

    private static final int RUNS = 100;

    @Override
    public Result execute( Input in ) {
        DistanceFactory distanceFactory = in.getDistanceType().getDistanceFactory();
        GraphEntityFactory graphEntityFactory = new NeighborListGraphEntityFactory();
        Graph graph = new GraphLoadCommand().execute( new Quaternion<>( in.getInputType(), in.getProperties(), graphEntityFactory, distanceFactory ) );
        RoutingAlgorithm routingAlgorithm = new AlgorithmCreateCommand().execute( new Trinity<>( in.getAlgorithmType(), in.getProperties(), graph ) );
        NodeSearcher nodeSearcher = new NodeSearcherCreateCommand().execute( new Pair<>( in.getInputType(), in.getProperties() ) );
        CoordinateReader coordinateReader = new CoordinateReaderCreateCommand().execute( new Pair<>( in.getInputType(), in.getProperties() ) );
        List<ExecutionStats> executions = new ArrayList<>();
        List<PathStats> paths = new ArrayList<>();
        GlobalOptions.MEASURE_TIME = true;

        for ( int run = 0; run < RUNS; run++ ) {

            for ( int i = 0; i < in.getData().size(); i++ ) {
                Pair<Coordinates, Coordinates> pair = in.getData().get( i );
                try {
                    Coordinates from = pair.a;
                    Coordinates to = pair.b;
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
                        paths.add( new PathStats( route.getTime(), route.getLength() ) );
                    }
                    if ( executions.size() <= i ) {
                        executions.add( new ExecutionStats(
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.NODE_SEARCHING ).getTimeElapsed(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTING ).getTimeElapsed(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTE_BUILDING ).getTimeElapsed(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.PATH_LOADING ).getTimeElapsed() ) );
                    } else {
                        ExecutionStats last = executions.get( i );
                        executions.set( i, new ExecutionStats(
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.NODE_SEARCHING ).getTimeElapsed() + last.getNodeSearchTime(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTING ).getTimeElapsed() + last.getRouteTime(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.ROUTE_BUILDING ).getTimeElapsed() + last.getRouteBuildingTime(),
                                TimeLogger.getTimeMeasurement( TimeLogger.Event.PATH_LOADING ).getTimeElapsed() + last.getPathLoadTime() ) );
                    }
                } catch ( IOException ex ) {
                    Logger.getLogger( ExecuteCommand.class.getName() ).log( Level.SEVERE, null, ex );
                }
            }
        }
        for ( int i = 0; i < in.getData().size(); i++ ) {
            ExecutionStats last = executions.get( i );
            executions.set( i, new ExecutionStats(
                    last.getNodeSearchTime() / RUNS,
                    last.getRouteTime() / RUNS,
                    last.getRouteBuildingTime() / RUNS,
                    last.getPathLoadTime() / RUNS ) );
        }

        GlobalOptions.MEASURE_TIME = false;
        return new Result( paths, executions );
    }

}
