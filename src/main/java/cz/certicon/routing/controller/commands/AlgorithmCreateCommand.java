/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.controller.commands;

import cz.certicon.routing.application.algorithm.AlgorithmType;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.astar.StraightLineAStarRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.dijkstra.DijkstraRoutingAlgorithm;
import cz.certicon.routing.controller.Command;
import cz.certicon.routing.model.basic.Quaternion;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;

/**
 *
 * @deprecated 
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class AlgorithmCreateCommand implements Command<Quaternion<AlgorithmType, Graph, GraphEntityFactory, DistanceFactory>, RoutingAlgorithm> {

    @Override
    public RoutingAlgorithm execute( Quaternion<AlgorithmType, Graph, GraphEntityFactory, DistanceFactory> in ) {
        if ( 1 == 1 ) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
        switch ( in.a ) {
            case DIJKSTRA:
                return new DijkstraRoutingAlgorithm( in.b, in.c, in.d );
            case ASTAR:
                return new StraightLineAStarRoutingAlgorithm( in.b, in.c, in.d );
            default:
                throw new IllegalArgumentException( "Unsupported algoritghm type: " + in.a.name() );
        }
    }

}
