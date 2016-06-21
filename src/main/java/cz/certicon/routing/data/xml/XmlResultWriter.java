/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.ResultWriter;
import cz.certicon.routing.data.basic.xml.AbstractXmlWriter;
import static cz.certicon.routing.data.xml.XmlCommonTags.*;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.PathStats;
import cz.certicon.routing.model.ReferenceOutput;
import cz.certicon.routing.model.Result;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Path;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlResultWriter implements ResultWriter {

    @Override
    public void write( DataDestination destination, Input input, Result result ) throws IOException {
        AbstractXmlWriter<Void> writer = new AbstractXmlWriter<Void>( destination ) {
            @Override
            protected void checkedWrite( Void out ) throws IOException {
                /*
                <root>
                <version>1</version>
                <algorithm>DIJKSTRA</algorithm>
                <priority>TIME</priority>
                <data>
                <output id="1">
                <time result="155" unit="s" accuracy="98"/>
                <length result="15515" unit="m" accuracy="99"/>
                </output>
                <output id="2">
                ...
                </data>
                </root>
                 */
                Map<Integer, ReferenceOutput.AccuracyPair> accuracyMap = input.getReferenceOutput().calculateAccuracy( result.getPaths() );
                try {
                    writeStringValue( VERSION, input.getVersion() );
                    writeStringValue( ALGORITHM, input.getAlgorithmType().name() );
                    writeStringValue( PRIORITY, input.getDistanceType().name() );
                    getWriter().writeStartElement( DATA );
                    for ( PathStats path : result.getPaths() ) {
                        ReferenceOutput.AccuracyPair acc = accuracyMap.get( path.getId() );
                        getWriter().writeStartElement( OUTPUT_ENTRY );
                        getWriter().writeAttribute( ID, path.getId() + "" );
                        getWriter().writeEmptyElement( TIME );
                        getWriter().writeAttribute( RESULT, path.getTime().toString() );
                        getWriter().writeAttribute( UNIT, path.getTime().getUnit() );
                        getWriter().writeAttribute( ACCURACY, acc.time + "" );
                        getWriter().writeEmptyElement( LENGTH );
                        getWriter().writeAttribute( RESULT, path.getLength().toString() );
                        getWriter().writeAttribute( UNIT, path.getLength().getUnit() );
                        getWriter().writeAttribute( ACCURACY, acc.length + "" );
                        getWriter().writeEndElement();
                    }
                    getWriter().writeEndElement();
                } catch ( XMLStreamException ex ) {
                    throw new IOException( ex );
                }
            }

            private void writeStringValue( String tag, String value ) throws XMLStreamException {
                getWriter().writeStartElement( tag );
                getWriter().writeCharacters( value );
                getWriter().writeEndElement();
            }
        };
        writer.write( null );
        writer.close();
    }

}
