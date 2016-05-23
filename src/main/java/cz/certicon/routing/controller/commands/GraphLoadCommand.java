/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.controller.commands;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.controller.Command;
import cz.certicon.routing.model.InputType;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Quaternion;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import java.util.Properties;

/**
 *
 * @deprecated 
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GraphLoadCommand implements Command<Quaternion<InputType, Properties, GraphEntityFactory, DistanceFactory>, Graph> {

    @Override
    public Graph execute( Quaternion<InputType, Properties, GraphEntityFactory, DistanceFactory> in ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}
