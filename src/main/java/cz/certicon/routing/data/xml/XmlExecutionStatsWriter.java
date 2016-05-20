/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.ExecutionStatsWriter;
import cz.certicon.routing.data.basic.xml.AbstractXmlWriter;
import cz.certicon.routing.model.ExecutionStats;
import java.io.IOException;
import static cz.certicon.routing.data.xml.ExecutionStatsTag.*;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlExecutionStatsWriter extends AbstractXmlWriter<ExecutionStats> implements ExecutionStatsWriter {

    public XmlExecutionStatsWriter( DataDestination destination ) {
        super( destination );
    }

    @Override
    protected void checkedWrite( ExecutionStats executionStats ) throws IOException {
        throw new UnsupportedOperationException( "Not implemented yet" );
//        try {
//            getWriter().writeStartElement( MEMORY.name().toLowerCase() );
//            getWriter().writeCharacters( Long.toString( executionStats.getMemory()) );
//            getWriter().writeEndElement();
//            getWriter().writeStartElement( TIME.name().toLowerCase() );
//            getWriter().writeCharacters( Long.toString( executionStats.getTime() ) );
//            getWriter().writeEndElement();
//            getWriter().writeStartElement( ACCURACY.name().toLowerCase() );
//            getWriter().writeCharacters( Double.toString( executionStats.getAccuracy()) );
//            getWriter().writeEndElement();
//            getWriter().flush();
//        } catch ( XMLStreamException ex ) {
//            throw new IOException( ex );
//        }
//        close();
    }

}
