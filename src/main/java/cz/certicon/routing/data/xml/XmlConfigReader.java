/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.model.Config;
import java.io.IOException;
import cz.certicon.routing.data.ConfigReader;
import cz.certicon.routing.data.DataSource;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlConfigReader implements ConfigReader {
    
    private final DataSource dataSource;

    XmlConfigReader( DataSource dataSource ) {
        this.dataSource = dataSource;
    }

    @Override
    public ConfigReader open() throws IOException {
        return this;
    }

    @Override
    public Config read() throws IOException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ConfigReader close() throws IOException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}
