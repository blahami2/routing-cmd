/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.RouteStatsIoFactory;
import cz.certicon.routing.data.RouteStatsReader;
import cz.certicon.routing.data.RouteStatsWriter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlRouteStatsIoFactory implements RouteStatsIoFactory {

    @Override
    public RouteStatsReader createReader( DataSource dataSource ) {
        return new XmlRouteStatsReader( dataSource );
    }

    @Override
    public RouteStatsWriter createWriter( DataDestination dataDestination ) {
        return new XmlRouteStatsWriter( dataDestination );
    }

}
