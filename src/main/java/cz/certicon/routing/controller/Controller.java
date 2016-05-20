/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.controller;

import cz.certicon.routing.controller.commands.ExecuteCommand;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.Result;
import cz.certicon.routing.view.MainUserInterface;
import cz.certicon.routing.view.cli.CliMainUserInterface;
import cz.certicon.routing.view.listeners.OnExecutionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Controller {

    private MainUserInterface userInterface;

    public void run( String... args ) {
        userInterface = new CliMainUserInterface();
        userInterface.setOnExecutionListener( new OnExecutionListener() {
            @Override
            public void execute( Input input ) {
                Result result = new ExecuteCommand().execute( input );
                userInterface.displayResult( result );
            }
        } );
        userInterface.run( args );
    }

}
