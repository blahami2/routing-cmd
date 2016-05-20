/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.InputReader;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.InputType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlInputReader implements InputReader {

    private boolean isOpen = false;

    @Override
    public void open() throws IOException {
        isOpen = true;
    }

    @Override
    public Input read( DataSource in ) throws IOException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse( in.getInputStream() );
            doc.getDocumentElement().normalize();
            XPath xPath = XPathFactory.newInstance().newXPath();

            String version = (String) xPath.compile( "/root/version" ).evaluate( doc, XPathConstants.STRING );
            InputType inputType = InputType.valueOf( (String) xPath.compile( "/root/input_type" ).evaluate( doc, XPathConstants.STRING ) );

        } catch ( ParserConfigurationException | SAXException | XPathExpressionException | IllegalArgumentException ex ) {
            throw new IOException( ex );
        }

        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws IOException {
        isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

}
