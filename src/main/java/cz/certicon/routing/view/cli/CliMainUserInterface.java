/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.view.cli;

import cz.blahami2.utils.table.model.ITable;
import cz.blahami2.utils.table.model.ITableBuilder;
import cz.blahami2.utils.table.model.basic.SimpleTableBuilder;
import cz.blahami2.utils.table.view.ITableViewer;
import cz.blahami2.utils.table.view.cli.CliTableViewer;
import cz.certicon.routing.data.ExecutionStatsWriter;
import cz.certicon.routing.data.ResultWriter;
import cz.certicon.routing.data.basic.FileDestination;
import cz.certicon.routing.data.basic.FileSource;
import cz.certicon.routing.data.xml.XmlExecutionStatsWriter;
import cz.certicon.routing.data.xml.XmlInputReader;
import cz.certicon.routing.data.xml.XmlResultWriter;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.Result;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.utility.progress.SimpleProgressListener;
import cz.certicon.routing.view.MainUserInterface;
import cz.certicon.routing.view.StatusEvent;
import cz.certicon.routing.view.listeners.OnExceptionThrownListener;
import cz.certicon.routing.view.listeners.OnExecutionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class CliMainUserInterface extends SimpleProgressListener implements MainUserInterface {

    private ParserBean bean;

    private OnExecutionListener onExecutionListener = null;
    private OnExceptionThrownListener onExceptionThrownListener = null;

    @Override
    public void setOnExecutionListener( OnExecutionListener onExecutionListener ) {
        this.onExecutionListener = onExecutionListener;
    }

    @Override
    public void setOnExceptionThrownListener( OnExceptionThrownListener onExceptionThrownListener ) {
        this.onExceptionThrownListener = onExceptionThrownListener;
    }

    @Override
    public void displayResult( Input input, Result result ) {
        ResultWriter resultWriter = new XmlResultWriter();
        ExecutionStatsWriter executionStatsWriter = new XmlExecutionStatsWriter();
        try {
            resultWriter.write( new FileDestination( bean.outputFile ), input, result );
            executionStatsWriter.write( new FileDestination( bean.execOutputFile ), input, result );
        } catch ( IOException ex ) {
            onExceptionThrownListener.onException( ex );
        }

        ITableBuilder<Double> builder = new SimpleTableBuilder<>();
//        builder.addColumns( result.getPaths(), ( value ) -> {
//            return (double) value.getLength().getLength();
//        }, ( value ) -> {
//            return (double) value.getTime().getTime();
//        } );
        builder.addColumns( result.getExecutions(), ( value ) -> {
            return (double) value.getNodeSearchTime().getTime();
        }, ( value ) -> {
            return (double) value.getRouteTime().getTime();
        }, ( value ) -> {
            return (double) value.getRouteBuildingTime().getTime();
        }, ( value ) -> {
            return (double) value.getPathLoadTime().getTime();
        } );
        ITable<Double> table = builder.build();
        ITableViewer tableViewer = new CliTableViewer();
        tableViewer.displayTable( table, ( value ) -> Double.toString( value ) );

        XYSeries[] series = new XYSeries[table.getColumnCount()];
        for ( int i = 0; i < table.getColumnCount(); i++ ) {
            series[i] = new XYSeries( i + "" );
            for ( int j = 0; j < table.getColumn( 0 ).size(); j++ ) {
                double d = table.getColumn( 0 ).get( j );
                series[i].add( j, d );
            }
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        for ( XYSeries ser : series ) {
            dataset.addSeries( ser );
        }
        JFreeChart chart = ChartFactory.createXYStepChart( "Result", "id", "ns", dataset, PlotOrientation.VERTICAL, true, true, false );
        NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );

        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument( null, "svg", null );
        SVGGraphics2D svgGenerator = new SVGGraphics2D( document );
        svgGenerator.getGeneratorContext().setPrecision( 6 );
        chart.draw( svgGenerator, new Rectangle2D.Double( 0, 0, 600, 800 ), null );
        boolean useCSS = true;
        Writer out;
        try {
            out = new OutputStreamWriter( new FileOutputStream( bean.getChartSearching() ), "UTF-8" );
            svgGenerator.stream( out, useCSS );
        } catch ( UnsupportedEncodingException | FileNotFoundException | SVGGraphics2DIOException ex ) {
            onExceptionThrownListener.onException( ex );
        }
    }

    @Override
    public void run( String... args ) {
        bean = new ParserBean();
        CmdLineParser parser = new CmdLineParser( bean );
        try {
            parser.parseArgument( args );
        } catch ( CmdLineException ex ) {
            System.err.println( ex.getMessage() );
            parser.printUsage( System.err );
            return;
        }
        try {
            Input input = new XmlInputReader().read( new FileSource( bean.getInputFile() ) );
            onExecutionListener.execute( input );
        } catch ( IOException ex ) {
            onExceptionThrownListener.onException( ex );
        }
    }

    private static String coordToString( Coordinate coord ) {
        return String.format( "%.7f,%.7f", coord.getLatitude(), coord.getLongitude() );
    }

    @Override
    public void report( Exception ex ) {
        System.err.println( ex.getMessage() );
        ex.printStackTrace();
    }

    @Override
    public void onProgressUpdate( double d ) {
        System.out.println( String.format( "%.0f%%", d * 100 ) );
    }

    @Override
    public void statusUpdate( StatusEvent statusEvent ) {
        System.out.println( statusEvent.name() );
    }

    private static class ParserBean {

        @Option( name = "-i", usage = "input file" )
        private File inputFile;
        @Option( name = "-o", usage = "output file" )
        private File outputFile;
        @Option( name = "-e-o", usage = "execution output file" )
        private File execOutputFile;
        @Option( name = "-c-s", usage = "chart node-searching output file" )
        private File chartSearching;
        @Option( name = "-c-r", usage = "chart routing output file" )
        private File chartRouting;
        @Option( name = "-c-b", usage = "chart route-building output file" )
        private File chartRouteBuilding;
        @Option( name = "-c-c", usage = "chart coordinate-loading output file" )
        private File chartCoordinates;

        public File getInputFile() {
            return inputFile;
        }

        public File getOutputFile() {
            return outputFile;
        }

        public File getExecOutputFile() {
            return execOutputFile;
        }

        public File getChartSearching() {
            return chartSearching;
        }

        public File getChartRouting() {
            return chartRouting;
        }

        public File getChartRouteBuilding() {
            return chartRouteBuilding;
        }

        public File getChartCoordinates() {
            return chartCoordinates;
        }
    }
}
