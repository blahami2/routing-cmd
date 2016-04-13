/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.ResultWriter;
import cz.certicon.routing.data.basic.xml.AbstractXmlWriter;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import static cz.certicon.routing.data.xml.ResultTag.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlResultWriter extends AbstractXmlWriter<Path> implements ResultWriter {

    public XmlResultWriter( DataDestination destination ) {
        super( destination );
    }

    private void writeCoordinate( Coordinates coordinate ) throws XMLStreamException {
        getWriter().writeStartElement( COORDINATE.name().toLowerCase() );
        getWriter().writeAttribute( LATITUDE.name().toLowerCase(), Double.toString( coordinate.getLatitude() ) );
        getWriter().writeAttribute( LONGITUDE.name().toLowerCase(), Double.toString( coordinate.getLongitude() ) );
        getWriter().writeEndElement();
    }

    @Override
    protected void checkedWrite( Path path ) throws IOException {
        try {
            List<Node> sortedNodes = path.getNodes();
            Collections.sort( sortedNodes, ( Node o1, Node o2 ) -> o1.getId().compareTo( o2.getId() ) );
            getWriter().writeStartElement( NODES.name().toLowerCase() );
            for ( Node node : sortedNodes ) {
                getWriter().writeStartElement( NODE.name().toLowerCase() );
                getWriter().writeAttribute( ID.name().toLowerCase(), Node.Id.toString( node.getId() ) );
                getWriter().writeAttribute( LATITUDE.name().toLowerCase(), Double.toString( node.getCoordinates().getLatitude() ) );
                getWriter().writeAttribute( LONGITUDE.name().toLowerCase(), Double.toString( node.getCoordinates().getLongitude() ) );
                getWriter().writeEndElement();
            }
            getWriter().writeEndElement();
            List<Edge> sortedEdges = path.getEdges();
            Collections.sort( sortedEdges, ( Edge o1, Edge o2 ) -> o1.getId().compareTo( o2.getId() ) );
            getWriter().writeStartElement( EDGES.name().toLowerCase() );
            for ( Edge edge : sortedEdges ) {
                getWriter().writeStartElement( EDGE.name().toLowerCase() );
                getWriter().writeAttribute( ID.name().toLowerCase(), Edge.Id.toString( edge.getId() ) );
                getWriter().writeAttribute( SOURCE.name().toLowerCase(), Node.Id.toString( edge.getSourceNode().getId() ) );
                getWriter().writeAttribute( TARGET.name().toLowerCase(), Node.Id.toString( edge.getTargetNode().getId() ) );
                getWriter().writeAttribute( SPEED_FORWARD.name().toLowerCase(), Double.toString( edge.getSpeed(  ) ) );
                getWriter().writeAttribute( LENGTH.name().toLowerCase(), Double.toString( edge.getAttributes().getLength() ) );
                getWriter().writeAttribute( PAID.name().toLowerCase(), Boolean.toString( edge.getAttributes().isPaid() ) );
                getWriter().writeEndElement();
            }
            getWriter().writeEndElement();
            getWriter().writeStartElement( COORDINATES.name().toLowerCase() );
            Node currentNode = path.getSourceNode();
            for ( Edge edge : path.getEdges() ) {
                getWriter().writeStartElement( EDGE.name().toLowerCase() );
                getWriter().writeAttribute( ID.name().toLowerCase(), Edge.Id.toString( edge.getId() ) );
                List<Coordinates> coordinates = edge.getCoordinates();
                if ( currentNode.equals( edge.getSourceNode() ) ) {
                    for ( int i = 0; i < coordinates.size(); i++ ) {
                        writeCoordinate( coordinates.get( i ) );
                    }
                } else {
                    for ( int i = coordinates.size() - 1; i >= 0; i-- ) {
                        writeCoordinate( coordinates.get( i ) );
                    }
                }
                currentNode = edge.getOtherNode( currentNode );
                getWriter().writeEndElement();
            }
            getWriter().writeEndElement();
            getWriter().flush();
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
        close();
    }

}
