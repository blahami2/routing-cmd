/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.dijkstra.DijkstraRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.data.number.LengthDistanceFactory;
import cz.certicon.routing.data.ConfigIoFactory;
import cz.certicon.routing.model.Config;
import cz.certicon.routing.data.basic.FileSource;
import java.io.File;
import java.io.IOException;
import cz.certicon.routing.data.ConfigReader;
import cz.certicon.routing.data.ConfigWriter;
import cz.certicon.routing.data.MapDataSource;
import cz.certicon.routing.data.Restriction;
import cz.certicon.routing.data.ResultIoFactory;
import cz.certicon.routing.data.ResultWriter;
import cz.certicon.routing.data.basic.FileDestination;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.coordinates.CoordinateSupplyFactory;
import cz.certicon.routing.data.coordinates.CoordinateWriter;
import cz.certicon.routing.data.coordinates.xml.XmlCoordinateSupplyFactory;
import cz.certicon.routing.data.graph.GraphIoFactory;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.data.graph.GraphWriter;
import cz.certicon.routing.data.graph.xml.XmlGraphIoFactory;
import cz.certicon.routing.data.osm.OsmPbfDataSource;
import cz.certicon.routing.data.xml.XmlConfigIoFactory;
import cz.certicon.routing.data.xml.XmlResultIoFactory;
import cz.certicon.routing.model.basic.ConfigImpl;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.model.entity.neighbourlist.NeighbourListGraphEntityFactory;
import cz.certicon.routing.presentation.PathPresenter;
import cz.certicon.routing.presentation.jxmapviewer.JxMapViewerFrame;
import cz.certicon.routing.utils.GraphUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Route {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main( String[] args ) throws IOException {
        String configFilePath = System.getProperty( "user.dir" ) + File.separator + "config.xml";
        if ( args.length > 0 ) {
            configFilePath = args[0];
        }
        new Route().run( configFilePath );
    }

    private final ConfigIoFactory configIoFactory = new XmlConfigIoFactory();
    private final GraphIoFactory graphIoFactory = new XmlGraphIoFactory();
    private final CoordinateSupplyFactory coordFactory = new XmlCoordinateSupplyFactory();
    private final GraphEntityFactory entityFactory = new NeighbourListGraphEntityFactory();
    private final DistanceFactory distanceFactory = new LengthDistanceFactory();
    private File pbfFile;
    private File inputDir;
    private File outputDir;
    private File graphFile;
    private File coordFile;
    private Config config;

    public void run( String configFilePath ) throws IOException {
        File configFile = new File( configFilePath );
        if ( !configFile.exists() ) {
            System.out.println( "File does not exist: '" + configFile.getAbsolutePath() + "'" );
            System.out.print( "Create template at the given location? (yes/no): " );
            Scanner sc = new Scanner( System.in );
            while ( true ) {
                String next = sc.next();
                if ( next.equalsIgnoreCase( "y" ) || next.equalsIgnoreCase( "yes" ) ) {
                    Config cfg = new ConfigImpl(
                            "insert/path/here",
                            new Coordinate( 1.2345, 1.2345 ),
                            new Coordinate( 1.2345, 1.2345 ) );
                    ConfigWriter configWriter = configIoFactory.createConfigWriter( new FileDestination( configFile ) );
                    configWriter.open();
                    configWriter.write( cfg );
                    configWriter.close();
                    System.out.println( "Template has been created at: '" + configFile.getAbsolutePath() + "'. Exiting." );
                    return;
                } else if ( next.equalsIgnoreCase( "n" ) || next.equalsIgnoreCase( "no" ) ) {
                    System.out.println( "Exiting." );
                    return;
                } else {
                    System.out.print( "Incorrect input: '" + next + "'. Type 'yes' or 'no': " );
                }
            }
        }
        ConfigReader configReader = configIoFactory.createConfigReader( new FileSource( configFile ) );
        configReader.open();
        config = configReader.read( null );
        configReader.close();
        pbfFile = new File( config.getPbfPath() );
        if ( !config.getPbfPath().endsWith( ".pbf" ) ) {
            System.out.println( "PBF file must have suffix '.pbf'. Exiting." );
            return;
        }
        if ( !pbfFile.exists() ) {
            System.out.println( "PBF file does not exist: '" + config.getPbfPath() + "'. Exiting." );
            return;
        }
        inputDir = new File( pbfFile.getParent() + File.separator + pbfFile.getName().substring( 0, pbfFile.getName().length() - 4 ) );
        if ( inputDir.exists() && !inputDir.isDirectory() ) {
            System.out.println( "File exists, but is not a directory: '" + inputDir.getAbsolutePath() + "'. Exiting." );
            return;
        }
        graphFile = new File( inputDir.getAbsolutePath() + File.separator + pbfFile.getName().substring( 0, pbfFile.getName().length() - 4 ) + "_graph.xml" );
        coordFile = new File( inputDir.getAbsolutePath() + File.separator + pbfFile.getName().substring( 0, pbfFile.getName().length() - 4 ) + "_coord.xml" );

        if ( !graphFile.exists() || !coordFile.exists() ) {
            if ( !inputDir.exists() ) {
                inputDir.mkdir();
            }
            System.out.println( "Required files do not exist. Generating..." );
            GraphWriter graphWriter = graphIoFactory.createGraphWriter( new FileDestination( graphFile ) );
            CoordinateWriter coordWriter = coordFactory.createWriter( new FileDestination( coordFile ) );
            MapDataSource dataSource = new OsmPbfDataSource( new FileSource( pbfFile ) );
            Restriction restriction = Restriction.getDefault();
            restriction.addAllowedPair( "highway", "motorway" );
            restriction.addAllowedPair( "highway", "trunk" );
            restriction.addAllowedPair( "highway", "primary" );
            restriction.addAllowedPair( "highway", "secondary" );
            restriction.addAllowedPair( "highway", "tertiary" );
            restriction.addAllowedPair( "highway", "unclassified" );
            restriction.addAllowedPair( "highway", "residential" );
            restriction.addAllowedPair( "highway", "service" );
            restriction.addAllowedPair( "highway", "motorway-link" );
            restriction.addAllowedPair( "highway", "trunk-link" );
            restriction.addAllowedPair( "highway", "primary-link" );
            restriction.addAllowedPair( "highway", "secondary-link" );
            restriction.addAllowedPair( "highway", "tertiary-link" );
            restriction.addForbiddenPair( "motor_vehicle", "no" );
            dataSource.setRestrictions( restriction );
            dataSource.loadGraph( entityFactory, distanceFactory, ( Graph graph ) -> {
//            File coordFile = new File( "D:\\Routing\\Data\\coords.xml" );
                try {
                    graphWriter.open();
                    graphWriter.write( graph );
                    graphWriter.close();
                    coordWriter.open();
                    for ( Edge edge : graph.getEdges() ) {
                        coordWriter.write( new Pair<>( edge, edge.getCoordinates() ) );
                        edge.setCoordinates( null );
                    }
                    coordWriter.close();
                    System.out.print( "Done generating. " );
                    onFilesAvailable();
                } catch ( IOException ex ) {
                    System.err.println( ex );
                }
            } );
        } else {
            onFilesAvailable();
        }
    }

    public void onFilesAvailable() throws IOException {
        System.out.println( "Loading graph..." );
        GraphReader graphReader = graphIoFactory.createGraphReader( new FileSource( graphFile ) );
        CoordinateReader coordReader = coordFactory.createReader( new FileSource( coordFile ) );
        graphReader.open();
        Graph graph = graphReader.read( new Pair<>( entityFactory, distanceFactory ) );
        graphReader.close();
        RoutingAlgorithm routingAlgorithm = new DijkstraRoutingAlgorithm( graph, entityFactory, distanceFactory );
        Node source = entityFactory.createNode( Node.Id.generateId(), config.getSource().getLatitude(), config.getSource().getLongitude() );
        Node destination = entityFactory.createNode( Node.Id.generateId(), config.getDestination().getLatitude(), config.getDestination().getLongitude() );
        System.out.println( "Done loading. Routing..." );
        Path route = routingAlgorithm.route( source, destination );
        if ( route == null ) {
            System.out.println( "Path between the two nodes has not been found. Exiting." );
            return;
        }
        System.out.println( "Done routing. Exporting results..." );
        outputDir = new File( inputDir.getAbsolutePath() + "_output" );
        if ( !outputDir.exists() ) {
            outputDir.mkdir();
        }
        ResultIoFactory resultIoFactory = new XmlResultIoFactory();
        File resultFile = new File( outputDir.getAbsolutePath() + File.separator + "result.xml" );
        ResultWriter resultWriter = resultIoFactory.createResultWriter( new FileDestination( resultFile ) );
        coordReader.open();
        Map<Edge, List<Coordinate>> coordinates = coordReader.read( new HashSet<>( route.getEdges() ) );
        coordReader.close();
        GraphUtils.fillWithCoordinates( route.getEdges(), coordinates );
        resultWriter.open();
        resultWriter.write( route );
        resultWriter.close();
        System.out.println( "Done exporting. Displaying map..." );
        PathPresenter map = new JxMapViewerFrame();
        map.setDisplayEdgeText( false );
        map.setDisplayNodeText( false );
        map.displayPath( route );
    }

}
