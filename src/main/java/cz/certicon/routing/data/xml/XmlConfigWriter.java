/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.ConfigWriter;
import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.basic.xml.AbstractXmlWriter;
import cz.certicon.routing.model.Config;
import java.io.IOException;
import static cz.certicon.routing.data.xml.ConfigTag.*;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlConfigWriter extends AbstractXmlWriter<Config> implements ConfigWriter {

    public XmlConfigWriter( DataDestination dataDestination ) {
        super( dataDestination );
    }

    @Override
    protected void checkedWrite( Config config ) throws IOException {
        try {
            getWriter().writeStartElement( FILENAME.name().toLowerCase() );
            getWriter().writeCharacters( config.getFileName());
            getWriter().writeEndElement();
            getWriter().writeStartElement( DATA.name().toLowerCase() );
            getWriter().writeCharacters( config.getInputDataFolderPath() );
            getWriter().writeEndElement();
            getWriter().writeStartElement( ROUTE_STATS.name().toLowerCase() );
            getWriter().writeCharacters( config.getReferenceRouteStatsPath() );
            getWriter().writeEndElement();
            getWriter().writeStartElement( PATH_PRESENTER.name().toLowerCase() );
            getWriter().writeCharacters( config.getPathPresenter().name() );
            getWriter().writeEndElement();
            getWriter().writeStartElement( FROM.name().toLowerCase() );
            getWriter().writeAttribute( LATITUDE.name().toLowerCase(), Double.toString( config.getSource().getLatitude() ) );
            getWriter().writeAttribute( LONGITUDE.name().toLowerCase(), Double.toString( config.getSource().getLongitude() ) );
            getWriter().writeEndElement();
            getWriter().writeStartElement( TO.name().toLowerCase() );
            getWriter().writeAttribute( LATITUDE.name().toLowerCase(), Double.toString( config.getDestination().getLatitude() ) );
            getWriter().writeAttribute( LONGITUDE.name().toLowerCase(), Double.toString( config.getDestination().getLongitude() ) );
            getWriter().writeEndElement();
            getWriter().flush();
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
        close();
    }

}
