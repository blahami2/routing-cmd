/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.view.cli;

import cz.certicon.routing.model.Result;
import cz.certicon.routing.view.MainUserInterface;
import cz.certicon.routing.view.listeners.OnExecutionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class CliMainUserInterface implements MainUserInterface {

    private OnExecutionListener onExecutionListener = null;

    @Override
    public void setOnExecutionListener( OnExecutionListener onExecutionListener ) {
        this.onExecutionListener = onExecutionListener;
    }

    @Override
    public void displayResult( Result result ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run( String... args ) {
        ParserBean bean = new ParserBean();
        CmdLineParser parser = new CmdLineParser( bean );
        try {
            parser.parseArgument( args );
        } catch ( CmdLineException ex ) {
            System.err.println( ex.getMessage() );
            parser.printUsage( System.err );
            return;
        }
        // load all the files
        
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
