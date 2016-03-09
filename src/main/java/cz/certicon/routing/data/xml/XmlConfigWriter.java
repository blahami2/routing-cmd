/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.ConfigWriter;
import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.model.Config;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlConfigWriter implements ConfigWriter {
    
    private final DataDestination dataDestination;

    XmlConfigWriter( DataDestination dataDestination ) {
        this.dataDestination = dataDestination;
    }

    @Override
    public ConfigWriter open() throws IOException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ConfigWriter write( Config config ) throws IOException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ConfigWriter close() throws IOException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
}
