/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.view;

import cz.certicon.routing.model.Result;
import cz.certicon.routing.view.listeners.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface MainUserInterface {

    public void setOnExecutionListener( OnExecutionListener onExecutionListener );

    public void displayResult( Result result );

    public void run( String... args );
}
