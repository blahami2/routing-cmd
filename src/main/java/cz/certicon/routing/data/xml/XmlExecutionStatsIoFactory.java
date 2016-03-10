/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.ExecutionStatsIoFactory;
import cz.certicon.routing.data.ExecutionStatsReader;
import cz.certicon.routing.data.ExecutionStatsWriter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlExecutionStatsIoFactory implements ExecutionStatsIoFactory {

    @Override
    public ExecutionStatsReader createReader( DataSource dataSource ) {
        return new XmlExecutionStatsReader( dataSource );
    }

    @Override
    public ExecutionStatsWriter createWriter( DataDestination dataDestination ) {
        return new XmlExecutionStatsWriter( dataDestination );
    }

}
