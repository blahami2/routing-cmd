/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.application.algorithm.AlgorithmType;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.InputReader;
import cz.certicon.routing.data.DistanceType;
import static cz.certicon.routing.data.xml.XmlCommonTags.*;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.InputType;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Coordinate;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlInputReader implements InputReader {

    @Override
    public Input read( DataSource in ) throws IOException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse( in.getInputStream() );
            doc.getDocumentElement().normalize();
            XPath xPath = XPathFactory.newInstance().newXPath();
            ValueReader valueReader = new ValueReader( doc, xPath );
            String root = "/" + ROOT + "/";
            String version = valueReader.getValue( root + VERSION, XPathConstants.STRING );
            InputType inputType = InputType.valueOf( valueReader.getValue( root + INPUT_TYPE, XPathConstants.STRING ) );
            String propertiesFilePath = valueReader.getValue( root + INPUT_PROPERTIES, XPathConstants.STRING );
            Properties properties = new Properties();
            properties.load( new FileInputStream( propertiesFilePath ) );

            int runs = valueReader.<Double>getValue( root + RUNS, XPathConstants.NUMBER ).intValue();
            AlgorithmType algorithmType = AlgorithmType.valueOf( valueReader.getValue( root + ALGORITHM, XPathConstants.STRING ) );
            DistanceType distanceType = DistanceType.valueOf( valueReader.getValue( root + PRIORITY, XPathConstants.STRING ) );
            NodeList ids = valueReader.getValue( "//" + DATA + "/" + INPUT_ENTRY + "/@" + ID, XPathConstants.NODESET );
            NodeList fromLatitudes = valueReader.getValue( "//" + FROM + "/@" + LATITUDE, XPathConstants.NODESET );
            NodeList fromLongitudes = valueReader.getValue( "//" + FROM + "/@" + LONGITUDE, XPathConstants.NODESET );
            NodeList toLatitudes = valueReader.getValue( "//" + TO + "/@" + LATITUDE, XPathConstants.NODESET );
            NodeList toLongitudes = valueReader.getValue( "//" + TO + "/@" + LONGITUDE, XPathConstants.NODESET );
            if ( fromLatitudes.getLength() != fromLongitudes.getLength() || toLatitudes.getLength() != toLongitudes.getLength() || fromLatitudes.getLength() != toLatitudes.getLength() ) {
                throw new IOException( "Invalid input: wrong counts (|" + FROM + "@" + LATITUDE + "|=" + fromLatitudes.getLength() + ", |" + FROM + "@" + LONGITUDE + "|=" + fromLongitudes.getLength() + ", |" + TO + "@" + LATITUDE + "|=" + toLatitudes.getLength() + ", |" + TO + "@" + LONGITUDE + "|=" + toLongitudes.getLength() );
            }
            List<Trinity<Integer, Coordinate, Coordinate>> data = new ArrayList<>();
            for ( int i = 0; i < fromLatitudes.getLength(); i++ ) {
                int id = Integer.parseInt( ids.item( i ).getNodeValue() );
                double fromLatitude = Double.parseDouble( fromLatitudes.item( i ).getNodeValue() );
                double fromLongitude = Double.parseDouble( fromLongitudes.item( i ).getNodeValue() );
                double toLatitude = Double.parseDouble( toLatitudes.item( i ).getNodeValue() );
                double toLongitude = Double.parseDouble( toLongitudes.item( i ).getNodeValue() );
                data.add( new Trinity<>(
                        id,
                        new Coordinate( fromLatitude, fromLongitude ),
                        new Coordinate( toLatitude, toLongitude )
                ) );
            }
            return new Input( runs, version, inputType, properties, algorithmType, distanceType, data );
        } catch ( ParserConfigurationException | SAXException | XPathExpressionException | IllegalArgumentException ex ) {
            throw new IOException( ex );
        }
    }

    private static class ValueReader {

        private final Document document;
        private final XPath xPath;

        public ValueReader( Document document, XPath xPath ) {
            this.document = document;
            this.xPath = xPath;
        }

        public <T> T getValue( String path, XPathConstants xpathConstant ) throws XPathExpressionException {
            return (T) xPath.compile( path ).evaluate( document, xpathConstant.getXPathConstant() );
        }
    }

}
