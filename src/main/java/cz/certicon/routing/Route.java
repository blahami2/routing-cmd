/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.astar.StraightLineAStarRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.dijkstra.DijkstraRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.data.number.LengthDistanceFactory;
import cz.certicon.routing.model.Config;
import cz.certicon.routing.data.basic.FileSource;
import java.io.File;
import java.io.IOException;
import cz.certicon.routing.data.ConfigReader;
import cz.certicon.routing.data.ConfigWriter;
import cz.certicon.routing.data.ExecutionStatsWriter;
import cz.certicon.routing.data.ResultWriter;
import cz.certicon.routing.data.RouteStatsWriter;
import cz.certicon.routing.data.basic.FileDestination;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.coordinates.xml.XmlCoordinateReader;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.data.graph.xml.XmlGraphReader;
import cz.certicon.routing.data.xml.XmlConfigReader;
import cz.certicon.routing.data.xml.XmlConfigWriter;
import cz.certicon.routing.data.xml.XmlExecutionStatsWriter;
import cz.certicon.routing.data.xml.XmlResultWriter;
import cz.certicon.routing.data.xml.XmlRouteStatsReader;
import cz.certicon.routing.data.xml.XmlRouteStatsWriter;
import cz.certicon.routing.model.PathPresenterEnum;
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
import cz.certicon.routing.model.entity.neighbourlist.DirectedNeighbourListGraphEntityFactory;
import cz.certicon.routing.model.entity.neighbourlist.NeighbourListGraphEntityFactory;
import cz.certicon.routing.presentation.PathPresenter;
import cz.certicon.routing.presentation.graphstream.GraphStreamPathPresenter;
import cz.certicon.routing.presentation.jxmapviewer.JxMapViewerFrame;
import cz.certicon.routing.utils.GraphUtils;
import cz.certicon.routing.utils.RouteStatsComparator;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
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

    private final GraphEntityFactory entityFactory = new DirectedNeighbourListGraphEntityFactory();
    private final DistanceFactory distanceFactory = new LengthDistanceFactory();
//    private File pbfFile;
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
                    ConfigImpl cfg = new ConfigImpl(
                            "datafile_basename",
                            "insert/datafiles/path/here",
                            "insert/reference_route_stats/path/here",
                            new Coordinate( 1.2345, 1.2345 ),
                            new Coordinate( 1.2345, 1.2345 ) );
                    cfg.setPathPresenterEnum( PathPresenterEnum.JXMAPVIEWER );
                    ConfigWriter configWriter = new XmlConfigWriter( new FileDestination( configFile ) );
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
        ConfigReader configReader = new XmlConfigReader( new FileSource( configFile ) );
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
                    RouteStatsWriter routeStatsWriter = new XmlRouteStatsWriter( new FileDestination( refRouteStatsFile ) );
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
        if ( config.getInputDataFolderPath() == null ) {
            System.out.println( "Data folder path not provided. Please, provide data folder path in tag 'data' (or let the application create a new template for you). Exiting." );
            return;
        }
        inputDir = new File( config.getInputDataFolderPath() );
        if ( inputDir.exists() && !inputDir.isDirectory() ) {
            System.out.println( "File exists, but is not a directory: '" + inputDir.getAbsolutePath() + "'. Exiting." );
            return;
        }
        if ( config.getPathPresenter() == null ) {
            System.out.println( "Warning: no path presenter specified. Using default. (solution: use 'path_presenter' tag in order to specify a path presenter:" );
            for ( PathPresenterEnum value : PathPresenterEnum.values() ) {
                System.out.println( value.name() );
            }
            System.out.println( ")" );
        }
        graphFile = new File( inputDir.getAbsolutePath() + File.separator + config.getFileName() + "_graph.xml" ).getAbsoluteFile();
        coordFile = new File( inputDir.getAbsolutePath() + File.separator + config.getFileName() + "_coord.xml" ).getAbsoluteFile();

        if ( !graphFile.exists() || !coordFile.exists() ) {
            if ( !inputDir.exists() ) {
                inputDir.mkdir();
            }
            System.out.println( "Required files do not exist. Please, provide files at:" );
            System.out.println( graphFile.getAbsolutePath() );
            System.out.println( coordFile.getAbsolutePath() );
            System.out.println( "Exiting." );
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
//        RoutingAlgorithm routingAlgorithm = new DijkstraRoutingAlgorithm( graph, entityFactory, distanceFactory );
        RoutingAlgorithm routingAlgorithm = new StraightLineAStarRoutingAlgorithm( graph, entityFactory, distanceFactory );
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
        ResultWriter resultWriter = new XmlResultWriter( new FileDestination( resultFile ) );
        Map<Edge, List<Coordinate>> coordinates = coordReader.read( new HashSet<>( route.getEdges() ) );
        coordReader.close();
        GraphUtils.fillWithCoordinates( route.getEdges(), coordinates );
        resultWriter.write( route );
        File routeStatsFile = new File( outputDir.getAbsolutePath() + File.separator + "route_statistics.xml" );
        RouteStatsWriter routeStatsWriter = new XmlRouteStatsWriter( new FileDestination( routeStatsFile ) );
        RouteStats actualRouteStats = new RouteStatsImpl( (long) route.getLength(), (long) route.getTime(), 0 );
        routeStatsWriter.write( actualRouteStats );
        File executionStatsFile = new File( outputDir.getAbsolutePath() + File.separator + "execution_statistics.xml" );
        ExecutionStatsWriter executionStatsWriter = new XmlExecutionStatsWriter( new FileDestination( executionStatsFile ) );
        double accuracy = RouteStatsComparator.calculateAccuracy( new XmlRouteStatsReader( new FileSource( refRouteStatsFile ) ).read( null ), actualRouteStats );
        executionStatsWriter.write( new ExecutionStatsImpl( timeMeasurement.getTimeElapsed(), 0, accuracy ) );

        PathPresenter map;
        if ( config.getPathPresenter() != null ) {
            switch ( config.getPathPresenter() ) {
                case GRAPHSTREAM:
                    map = new GraphStreamPathPresenter( entityFactory );
                    break;
                case JXMAPVIEWER:
                    map = new JxMapViewerFrame();
                    break;
                case NONE:
                    map = null;
                    break;
                default:
                    map = new JxMapViewerFrame();
                    break;
            }
        } else {
            map = new JxMapViewerFrame();
        }
        if ( map != null ) {
            System.out.println( "Done exporting. Displaying map..." );
            System.out.flush();
            map.setDisplayEdgeText( false );
            map.setDisplayNodeText( false );
            map.displayPath( route );
        } else {
            System.out.println( "Done exporting. Path presenter set to none. Finishing..." );
            System.out.flush();
        }
    }

}
