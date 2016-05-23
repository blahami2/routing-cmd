/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.ExecutionStatsWriter;
import cz.certicon.routing.data.basic.xml.AbstractXmlWriter;
import static cz.certicon.routing.data.xml.XmlCommonTags.*;
import cz.certicon.routing.model.ExecutionStats;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.PathStats;
import cz.certicon.routing.model.Result;
import cz.certicon.routing.model.basic.Time;
import cz.certicon.routing.model.basic.TimeUnits;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlExecutionStatsWriter implements ExecutionStatsWriter {

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
      <node_searching time="12" unit="ms"/>
      <routing time="101" unit="ms"/>
      <route_building time="1" unit="ms"/>
      <path_loading time="8" unit="ms"/>
    </output>
    <output id="2">
    ...
  </data>
</root>
                 */
                // calculate average values
                Time nodeSearch = new Time( TIME_UNITS, 0 );
                Time route = new Time( TIME_UNITS, 0 );
                Time routeBuild = new Time( TIME_UNITS, 0 );
                Time pathLoad = new Time( TIME_UNITS, 0 );
                for ( ExecutionStats execution : result.getExecutions() ) {
                    nodeSearch = nodeSearch.add( execution.getNodeSearchTime() );
                    route = route.add( execution.getRouteTime() );
                    routeBuild = routeBuild.add( execution.getRouteBuildingTime() );
                    pathLoad = pathLoad.add( execution.getPathLoadTime() );
                }
                // write
                try {
                    writeStringValue( VERSION, input.getVersion() );
                    writeStringValue( ALGORITHM, input.getAlgorithmType().name() );
                    writeStringValue( PRIORITY, input.getDistanceType().name() );
                    getWriter().writeStartElement( AVERAGE );
                    writeTime( TIME_NODE_SEARCH, nodeSearch.divide( result.getExecutions().size() ) );
                    writeTime( TIME_ROUTING, route.divide( result.getExecutions().size() ) );
                    writeTime( TIME_ROUTE_BUILDING, routeBuild.divide( result.getExecutions().size() ) );
                    writeTime( TIME_PATH_LOADING, pathLoad.divide( result.getExecutions().size() ) );
                    getWriter().writeStartElement( DATA );
                    for ( ExecutionStats execution : result.getExecutions() ) {
                        getWriter().writeStartElement( OUTPUT_ENTRY );
                        getWriter().writeAttribute( ID, execution.getId() + "" );
                        writeTime( TIME_NODE_SEARCH, execution.getNodeSearchTime() );
                        writeTime( TIME_ROUTING, execution.getRouteTime() );
                        writeTime( TIME_ROUTE_BUILDING, execution.getRouteBuildingTime() );
                        writeTime( TIME_PATH_LOADING, execution.getPathLoadTime() );
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

            private void writeTime( String tag, Time time ) throws XMLStreamException {
                getWriter().writeEmptyElement( tag );
                getWriter().writeAttribute( TIME, time.toString() );
                getWriter().writeAttribute( UNIT, time.getUnit() );
            }
        };
        writer.write( null );
        writer.close();
    }

}
