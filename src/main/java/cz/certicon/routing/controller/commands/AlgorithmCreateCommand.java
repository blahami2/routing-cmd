/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.controller.commands;

import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.controller.Command;
import cz.certicon.routing.model.AlgorithmType;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Graph;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class AlgorithmCreateCommand implements Command<Trinity<AlgorithmType, Properties, Graph>, RoutingAlgorithm> {

    @Override
    public RoutingAlgorithm execute( Trinity<AlgorithmType, Properties, Graph> in ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}
