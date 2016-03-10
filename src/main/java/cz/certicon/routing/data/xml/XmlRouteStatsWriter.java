/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.RouteStatsWriter;
import cz.certicon.routing.data.basic.xml.AbstractXmlWriter;
import cz.certicon.routing.model.RouteStats;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import static cz.certicon.routing.data.xml.RouteStatsTag.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlRouteStatsWriter extends AbstractXmlWriter<RouteStats> implements RouteStatsWriter {

    XmlRouteStatsWriter( DataDestination destination ) {
        super( destination );
    }

    @Override
    protected void openedWrite( RouteStats routeStats ) throws IOException {
        try {
            getWriter().writeStartElement( LENGTH.name().toLowerCase() );
            getWriter().writeCharacters( Long.toString( routeStats.getLength() ) );
            getWriter().writeEndElement();
            getWriter().writeStartElement( TIME.name().toLowerCase() );
            getWriter().writeCharacters( Long.toString( routeStats.getTime() ) );
            getWriter().writeEndElement();
            getWriter().writeStartElement( PRICE.name().toLowerCase() );
            getWriter().writeCharacters( Double.toString( routeStats.getPrice() ) );
            getWriter().writeEndElement();
            getWriter().flush();
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
        close();
    }

}
