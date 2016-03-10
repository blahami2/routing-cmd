/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.ConfigIoFactory;
import cz.certicon.routing.data.ConfigReader;
import cz.certicon.routing.data.ConfigWriter;
import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlConfigIoFactory implements ConfigIoFactory {

    @Override
    public ConfigReader createReader( DataSource dataSource ) {
        return new XmlConfigReader( dataSource );
    }

    @Override
    public ConfigWriter createWriter( DataDestination dataDestination ) {
        return new XmlConfigWriter( dataDestination );
    }

}
