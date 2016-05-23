/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.view.cli;

import cz.certicon.routing.data.ExecutionStatsWriter;
import cz.certicon.routing.data.ResultWriter;
import cz.certicon.routing.data.basic.FileDestination;
import cz.certicon.routing.data.basic.FileSource;
import cz.certicon.routing.data.xml.XmlExecutionStatsWriter;
import cz.certicon.routing.data.xml.XmlInputReader;
import cz.certicon.routing.data.xml.XmlResultWriter;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.Result;
import cz.certicon.routing.model.utility.progress.SimpleProgressListener;
import cz.certicon.routing.view.MainUserInterface;
import cz.certicon.routing.view.StatusEvent;
import cz.certicon.routing.view.listeners.OnExceptionThrownListener;
import cz.certicon.routing.view.listeners.OnExecutionListener;
import java.io.File;
import java.io.IOException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

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
            System.out.println( input.toString() );
            onExecutionListener.execute( input );
        } catch ( IOException ex ) {
            onExceptionThrownListener.onException( ex );
        }
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

        public File getInputFile() {
            return inputFile;
        }

        public File getOutputFile() {
            return outputFile;
        }

        public File getExecOutputFile() {
            return execOutputFile;
        }
    }
}
