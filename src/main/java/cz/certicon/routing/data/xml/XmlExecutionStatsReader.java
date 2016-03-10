/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.ExecutionStatsReader;
import cz.certicon.routing.data.basic.xml.AbstractXmlReader;
import cz.certicon.routing.model.ExecutionStats;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import static cz.certicon.routing.data.xml.ExecutionStatsTag.*;
import cz.certicon.routing.model.basic.ExecutionStatsImpl;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlExecutionStatsReader extends AbstractXmlReader<Void, ExecutionStats> implements ExecutionStatsReader {

    XmlExecutionStatsReader( DataSource source ) {
        super( source );
    }

    @Override
    protected ExecutionStats openedRead( Void in ) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            Handler handler = new Handler();
            saxParser.parse( getDataSource().getInputStream(), handler );
            close();
            return handler.getExecutionStats();
        } catch ( ParserConfigurationException | SAXException ex ) {
            throw new IOException( ex );
        }
    }

    private static class Handler extends DefaultHandler {

        private long memory;
        private long time;
        private double accuracy;
        private boolean parsingMemory = false;
        private boolean parsingTime = false;
        private boolean parsingAccuracy = false;
        private StringBuilder sb;

        public Handler() {
        }

        @Override
        public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
            if ( qName.equalsIgnoreCase( MEMORY.name() ) ) {
                parsingMemory = true;
                sb = new StringBuilder();
            } else if ( qName.equalsIgnoreCase( TIME.name() ) ) {
                parsingTime = true;
                sb = new StringBuilder();
            } else if ( qName.equalsIgnoreCase( ACCURACY.name() ) ) {
                parsingAccuracy = true;
                sb = new StringBuilder();
            }
        }

        @Override
        public void characters( char[] chars, int i, int i1 ) throws SAXException {
            if ( parsingMemory || parsingTime || parsingAccuracy ) {
                sb.append( new String( chars, i, i1 ) );
            }
        }

        @Override
        public void endElement( String uri, String localName, String qName ) throws SAXException {
            if ( qName.equalsIgnoreCase( MEMORY.name() ) ) {
                parsingMemory = false;
                memory = Long.parseLong( sb.toString() );
            } else if ( qName.equalsIgnoreCase( TIME.name() ) ) {
                parsingTime = false;
                time = Long.parseLong( sb.toString() );
            } else if ( qName.equalsIgnoreCase( ACCURACY.name() ) ) {
                parsingAccuracy = false;
                accuracy = Double.parseDouble( sb.toString() );
            }
        }

        public ExecutionStats getExecutionStats() {
            return new ExecutionStatsImpl( time, memory, accuracy );
        }
    }
}
