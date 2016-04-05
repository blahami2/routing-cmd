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
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.ExecutionStatsWriter;
import cz.certicon.routing.data.MapDataSource;
import cz.certicon.routing.data.Restriction;
import cz.certicon.routing.data.ResultIoFactory;
import cz.certicon.routing.data.ResultWriter;
import cz.certicon.routing.data.RouteStatsIoFactory;
import cz.certicon.routing.data.RouteStatsWriter;
import cz.certicon.routing.data.basic.FileDestination;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.coordinates.CoordinateWriter;
import cz.certicon.routing.data.coordinates.xml.XmlCoordinateReader;
import cz.certicon.routing.data.coordinates.xml.XmlCoordinateWriter;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.data.graph.GraphWriter;
import cz.certicon.routing.data.graph.xml.XmlGraphReader;
import cz.certicon.routing.data.graph.xml.XmlGraphWriter;
import cz.certicon.routing.data.osm.OsmPbfDataSource;
import cz.certicon.routing.data.xml.XmlConfigIoFactory;
import cz.certicon.routing.data.xml.XmlExecutionStatsIoFactory;
import cz.certicon.routing.data.xml.XmlResultIoFactory;
import cz.certicon.routing.data.xml.XmlRouteStatsIoFactory;
import cz.certicon.routing.model.RouteStats;
import cz.certicon.routing.model.basic.ConfigImpl;
import cz.certicon.routing.model.basic.ExecutionStatsImpl;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.RouteStatsImpl;
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
import cz.certicon.routing.utils.RouteStatsComparator;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.io.InputStream;
import java.util.HashMap;
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
    private final GraphEntityFactory entityFactory = new NeighbourListGraphEntityFactory();
    private final DistanceFactory distanceFactory = new LengthDistanceFactory();
    private final RouteStatsIoFactory routeStatsIoFactory = new XmlRouteStatsIoFactory();
    private File pbfFile;
    private File inputDir;
    private File outputDir;
    private File graphFile;
    private File coordFile;
    private File configFile;
    private File refRouteStatsFile;
    private Config config;

    public void run( String configFilePath ) throws IOException {
        configFile = new File( configFilePath ).getAbsoluteFile();
        if ( !configFile.getName().endsWith( ".xml" ) ) {
            System.out.println( "File does not end with .xml: '" + configFile.getAbsolutePath() + "'" );
            System.out.println( "Exiting." );
            return;
        }
        if ( !configFile.exists() ) {
            System.out.println( "File does not exist: '" + configFile.getAbsolutePath() + "'" );
            System.out.print( "Create template at the given location? (yes/no): " );
            Scanner sc = new Scanner( System.in );
            while ( true ) {
                String next = sc.next();
                if ( next.equalsIgnoreCase( "y" ) || next.equalsIgnoreCase( "yes" ) ) {
                    Config cfg = new ConfigImpl(
                            "insert/pbf/path/here",
                            "insert/reference_route_stats/path/here",
                            new Coordinate( 1.2345, 1.2345 ),
                            new Coordinate( 1.2345, 1.2345 ) );
                    ConfigWriter configWriter = configIoFactory.createWriter( new FileDestination( configFile ) );
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
        ConfigReader configReader = configIoFactory.createReader( new FileSource( configFile ) );
        config = configReader.read( null );
        if ( config.getReferenceRouteStatsPath() == null ) {
            System.out.println( "Update your config file with reference route statistics (or let the application create a new tempalte for you). Exiting." );
            return;
        }
        refRouteStatsFile = new File( config.getReferenceRouteStatsPath() );
        if ( !refRouteStatsFile.getAbsolutePath().endsWith( ".xml" ) ) {
            System.out.println( "Reference file with route statistics must have suffix '.xml'. Exiting." );
            return;
        }
        if ( !refRouteStatsFile.exists() ) {
            System.out.println( "Reference file with route statistics does not exist: '" + refRouteStatsFile.getAbsolutePath() + "'" );
            System.out.print( "Create template at the given location? (yes/no): " );
            Scanner sc = new Scanner( System.in );
            while ( true ) {
                String next = sc.next();
                if ( next.equalsIgnoreCase( "y" ) || next.equalsIgnoreCase( "yes" ) ) {
                    RouteStats routeStats = new RouteStatsImpl( 1, 1, 0 );
                    RouteStatsWriter routeStatsWriter = routeStatsIoFactory.createWriter( new FileDestination( refRouteStatsFile ) );
                    routeStatsWriter.write( routeStats );
                    System.out.println( "Template has been created at: '" + refRouteStatsFile.getAbsolutePath() + "'. Exiting." );
                    return;
                } else if ( next.equalsIgnoreCase( "n" ) || next.equalsIgnoreCase( "no" ) ) {
                    System.out.println( "Exiting." );
                    return;
                } else {
                    System.out.print( "Incorrect input: '" + next + "'. Type 'yes' or 'no': " );
                }
            }
        } else {
            refRouteStatsFile = refRouteStatsFile.getAbsoluteFile();
        }
        pbfFile = new File( config.getPbfPath() ).getAbsoluteFile();
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
        graphFile = new File( inputDir.getAbsolutePath() + File.separator + pbfFile.getName().substring( 0, pbfFile.getName().length() - 4 ) + "_graph.xml" ).getAbsoluteFile();
        coordFile = new File( inputDir.getAbsolutePath() + File.separator + pbfFile.getName().substring( 0, pbfFile.getName().length() - 4 ) + "_coord.xml" ).getAbsoluteFile();

        if ( !graphFile.exists() || !coordFile.exists() ) {
            if ( !inputDir.exists() ) {
                inputDir.mkdir();
            }
            System.out.println( "Required files do not exist. Generating..." );
            GraphWriter graphWriter = new XmlGraphWriter( new FileDestination( graphFile ) );
            CoordinateWriter coordWriter = new XmlCoordinateWriter( new FileDestination( coordFile ) );
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
                    Map<Edge, List<Coordinate>> cm = new HashMap<>();
                    for ( Edge edge : graph.getEdges() ) {
                        cm.put( edge, edge.getCoordinates() );
                        edge.setCoordinates( null );
                    }
                    coordWriter.write( cm );
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
        GraphReader graphReader = new XmlGraphReader( new FileSource( graphFile ) );
        CoordinateReader coordReader = new XmlCoordinateReader( new FileSource( coordFile ) );
        graphReader.open();
        Graph graph = graphReader.read( new Pair<>( entityFactory, distanceFactory ) );
        graphReader.close();
        RoutingAlgorithm routingAlgorithm = new DijkstraRoutingAlgorithm( graph, entityFactory, distanceFactory );
        Node source = entityFactory.createNode( Node.Id.generateId(), config.getSource().getLatitude(), config.getSource().getLongitude() );
        Node destination = entityFactory.createNode( Node.Id.generateId(), config.getDestination().getLatitude(), config.getDestination().getLongitude() );
        System.out.println( "Done loading. Routing..." );
        TimeMeasurement timeMeasurement = new TimeMeasurement();
        timeMeasurement.start();
        Path route = routingAlgorithm.route( source, destination );
        timeMeasurement.stop();
        if ( route == null ) {
            System.out.println( "Path between the two nodes has not been found. Exiting." );
            return;
        }
        outputDir = new File( configFile.getParent() + File.separator + configFile.getName().substring( 0, configFile.getName().length() - 4 ) + "_output" );
        if ( !outputDir.exists() ) {
            outputDir.mkdir();
        }
        System.out.println( "Done routing. Exporting results to directory: '" + outputDir.getAbsolutePath() + "'" );
        File resultFile = new File( outputDir.getAbsolutePath() + File.separator + "result.xml" );
        ResultIoFactory resultIoFactory = new XmlResultIoFactory();
        ResultWriter resultWriter = resultIoFactory.createWriter( new FileDestination( resultFile ) );
        Map<Edge, List<Coordinate>> coordinates = coordReader.read( new HashSet<>( route.getEdges() ) );
        coordReader.close();
        GraphUtils.fillWithCoordinates( route.getEdges(), coordinates );
        resultWriter.write( route );
        File routeStatsFile = new File( outputDir.getAbsolutePath() + File.separator + "route_statistics.xml" );
        RouteStatsWriter routeStatsWriter = routeStatsIoFactory.createWriter( new FileDestination( routeStatsFile ) );
        RouteStats actualRouteStats = new RouteStatsImpl( (long) route.getLength(), (long) route.getTime(), 0 );
        routeStatsWriter.write( actualRouteStats );
        File executionStatsFile = new File( outputDir.getAbsolutePath() + File.separator + "execution_statistics.xml" );
        ExecutionStatsWriter executionStatsWriter = new XmlExecutionStatsIoFactory().createWriter( new FileDestination( executionStatsFile ) );
        double accuracy = RouteStatsComparator.calculateAccuracy( routeStatsIoFactory.createReader( new FileSource( refRouteStatsFile ) ).read( null ), actualRouteStats );
        executionStatsWriter.write( new ExecutionStatsImpl( timeMeasurement.getTimeElapsed(), 0, accuracy ) );
        System.out.println( "Done exporting. Displaying map..." );
        System.out.flush();
        PathPresenter map = new JxMapViewerFrame();
        map.setDisplayEdgeText( false );
        map.setDisplayNodeText( false );
        map.displayPath( route );
    }

}
