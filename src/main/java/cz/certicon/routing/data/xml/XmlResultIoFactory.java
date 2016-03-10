/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.ResultIoFactory;
import cz.certicon.routing.data.ResultReader;
import cz.certicon.routing.data.ResultWriter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlResultIoFactory implements ResultIoFactory {

    @Override
    public ResultWriter createWriter( DataDestination dataDestination ) {
        return new XmlResultWriter( dataDestination );
    }

    @Override
    public ResultReader createReader( DataSource dataSource ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}
