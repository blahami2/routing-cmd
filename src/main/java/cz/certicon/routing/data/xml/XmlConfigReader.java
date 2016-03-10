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
import cz.certicon.routing.data.basic.xml.AbstractXmlReader;
import cz.certicon.routing.model.basic.ConfigImpl;
import cz.certicon.routing.model.entity.Coordinate;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import static cz.certicon.routing.data.xml.ConfigTag.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlConfigReader extends AbstractXmlReader<Void, Config> implements ConfigReader {

    XmlConfigReader( DataSource dataSource ) {
        super( dataSource );
    }

    @Override
    protected Config openedRead( Void in ) throws IOException {
        Config result;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            Handler handler = new Handler();
            saxParser.parse( getDataSource().getInputStream(), handler );
            result = handler.getConfig();
        } catch ( ParserConfigurationException | SAXException ex ) {
            throw new IOException( ex );
        }
        close();
        return result;
    }

    private static class Handler extends DefaultHandler {

        private String pbfPath;
        private double aLatitude;
        private double aLongitude;
        private double bLatitude;
        private double bLongitude;
        private boolean isParsingPath = false;
        private StringBuilder pathBuilder;

        public Handler() {
        }

        @Override
        public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
            if ( qName.equalsIgnoreCase( FROM.name() ) ) {
                aLatitude = Double.parseDouble( attributes.getValue( LATITUDE.name().toLowerCase() ) );
                aLongitude = Double.parseDouble( attributes.getValue( LONGITUDE.name().toLowerCase() ) );
            } else if ( qName.equalsIgnoreCase( TO.name() ) ) {
                bLatitude = Double.parseDouble( attributes.getValue( LATITUDE.name().toLowerCase() ) );
                bLongitude = Double.parseDouble( attributes.getValue( LONGITUDE.name().toLowerCase() ) );
            } else if ( qName.equalsIgnoreCase( PBF.name() ) ) {
                isParsingPath = true;
                pathBuilder = new StringBuilder();
            }
        }

        @Override
        public void characters( char[] chars, int i, int i1 ) throws SAXException {
            if ( isParsingPath ) {
                pathBuilder.append( new String( chars, i, i1 ) );
            }
        }

        @Override
        public void endElement( String uri, String localName, String qName ) throws SAXException {
            if ( qName.equalsIgnoreCase( PBF.name() ) ) {
                isParsingPath = false;
                pbfPath = pathBuilder.toString();
            }
        }

        public Config getConfig() {
            return new ConfigImpl( pbfPath, new Coordinate( aLatitude, aLongitude ), new Coordinate( bLatitude, bLongitude ) );
        }
    }
}
