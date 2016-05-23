/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.view;

import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.Result;
import cz.certicon.routing.model.utility.ProgressListener;
import cz.certicon.routing.view.listeners.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface MainUserInterface extends ProgressListener {

    public void setOnExecutionListener( OnExecutionListener onExecutionListener );

    public void setOnExceptionThrownListener( OnExceptionThrownListener onExceptionThrownListener );

    public void statusUpdate( StatusEvent statusEvent );

    public void displayResult( Input input, Result result );

    public void run( String... args );

    public void report( Exception ex );
}
