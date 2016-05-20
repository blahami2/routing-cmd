/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.controller.commands;

import cz.certicon.routing.controller.Command;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.model.InputType;
import cz.certicon.routing.model.basic.Pair;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class CoordinateReaderCreateCommand implements Command<Pair<InputType,Properties>,CoordinateReader> {

    @Override
    public CoordinateReader execute( Pair<InputType, Properties> in ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
}
