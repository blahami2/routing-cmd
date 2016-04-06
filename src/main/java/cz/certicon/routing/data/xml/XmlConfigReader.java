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
import cz.certicon.routing.model.PathPresenterEnum;
import cz.certicon.routing.presentation.PathPresenter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlConfigReader extends AbstractXmlReader<Void, Config> implements ConfigReader {

    public XmlConfigReader( DataSource dataSource ) {
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

        private String filename;
        private String dataPath;
        private String routeStatsPath;
        private String pathPresenter;
        private double aLatitude;
        private double aLongitude;
        private double bLatitude;
        private double bLongitude;
        private boolean isParsingString = false;
        private StringBuilder stringBuilder;

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
            } else if ( qName.equalsIgnoreCase( DATA.name() )
                    || qName.equalsIgnoreCase( ROUTE_STATS.name() )
                    || qName.equalsIgnoreCase( PATH_PRESENTER.name() )
                    || qName.equalsIgnoreCase( FILENAME.name() ) ) {
                isParsingString = true;
                stringBuilder = new StringBuilder();
            }
        }

        @Override
        public void characters( char[] chars, int i, int i1 ) throws SAXException {
            if ( isParsingString ) {
                stringBuilder.append( new String( chars, i, i1 ) );
            }
        }

        @Override
        public void endElement( String uri, String localName, String qName ) throws SAXException {
            if ( qName.equalsIgnoreCase( DATA.name() ) ) {
                isParsingString = false;
                dataPath = stringBuilder.toString();
            } else if ( qName.equalsIgnoreCase( ROUTE_STATS.name() ) ) {
                isParsingString = false;
                routeStatsPath = stringBuilder.toString();
            } else if ( qName.equalsIgnoreCase( PATH_PRESENTER.name() ) ) {
                isParsingString = false;
                pathPresenter = stringBuilder.toString();
            } else if ( qName.equalsIgnoreCase( FILENAME.name() ) ) {
                isParsingString = false;
                filename = stringBuilder.toString();
            }
        }

        public Config getConfig() {
            ConfigImpl cfg = new ConfigImpl( filename, dataPath, routeStatsPath, new Coordinate( aLatitude, aLongitude ), new Coordinate( bLatitude, bLongitude ) );
            PathPresenterEnum pathPresenterEnum;
            try {
                pathPresenterEnum = PathPresenterEnum.valueOf( pathPresenter );
                cfg.setPathPresenterEnum( pathPresenterEnum );
            } catch ( IllegalArgumentException | NullPointerException ex ) {
                // not present
            }
            return cfg;
        }
    }
}
