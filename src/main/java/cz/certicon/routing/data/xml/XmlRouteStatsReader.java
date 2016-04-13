/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.RouteStatsReader;
import cz.certicon.routing.data.basic.xml.AbstractXmlReader;
import cz.certicon.routing.model.RouteStats;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import static cz.certicon.routing.data.xml.RouteStatsTag.*;
import cz.certicon.routing.model.basic.RouteStatsImpl;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlRouteStatsReader extends AbstractXmlReader<Void, RouteStats> implements RouteStatsReader {

    public XmlRouteStatsReader( DataSource source ) {
        super( source );
    }

    @Override
    protected RouteStats checkedRead( Void in ) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            Handler handler = new Handler();
            saxParser.parse( getDataSource().getInputStream(), handler );
            close();
            return handler.getRouteStats();
        } catch ( ParserConfigurationException | SAXException ex ) {
            throw new IOException( ex );
        }
    }

    private static class Handler extends DefaultHandler {

        private long length;
        private long time;
        private double price;
        private boolean parsingLength = false;
        private boolean parsingTime = false;
        private boolean parsingPrice = false;
        private StringBuilder sb;

        public Handler() {
        }

        @Override
        public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
            if ( qName.equalsIgnoreCase( LENGTH.name() ) ) {
                parsingLength = true;
                sb = new StringBuilder();
            } else if ( qName.equalsIgnoreCase( TIME.name() ) ) {
                parsingTime = true;
                sb = new StringBuilder();
            } else if ( qName.equalsIgnoreCase( PRICE.name() ) ) {
                parsingPrice = true;
                sb = new StringBuilder();
            }
        }

        @Override
        public void characters( char[] chars, int i, int i1 ) throws SAXException {
            if ( parsingLength || parsingTime || parsingPrice ) {
                sb.append( new String( chars, i, i1 ) );
            }
        }

        @Override
        public void endElement( String uri, String localName, String qName ) throws SAXException {
            if ( qName.equalsIgnoreCase( LENGTH.name() ) ) {
                parsingLength = false;
                length = Long.parseLong( sb.toString() );
            } else if ( qName.equalsIgnoreCase( TIME.name() ) ) {
                parsingTime = false;
                time = Long.parseLong( sb.toString() );
            } else if ( qName.equalsIgnoreCase( PRICE.name() ) ) {
                parsingPrice = false;
                price = Double.parseDouble( sb.toString() );
            }
        }

        public RouteStats getRouteStats() {
            return new RouteStatsImpl( length, time, price );
        }
    }

}
