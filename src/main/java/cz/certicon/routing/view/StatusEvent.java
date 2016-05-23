/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.view;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public enum StatusEvent {
    START_LOADING_GRAPH,
    COMPLETED_LOADING_GRAPH,
    START_LOADING_PREPROCESSED_DATA,
    COMPLETED_LOADING_PREPROCESSED_DATA,
    START_PREPARING_ALGORITHM,
    COMPLETED_PREPARING_ALGORITHM,
    START_COMPUTING,
    COMPLETED_COMPUTING,
    START_DISPLAYING_RESULT,
    COMPLETED_DISPLAYING_RESULT;

}
