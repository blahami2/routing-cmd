/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model;

import cz.certicon.routing.model.entity.Coordinates;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Config {
    
    public String getFileName();
    
    public String getInputDataFolderPath();
    
    public PathPresenterEnum getPathPresenter();

    public String getReferenceRouteStatsPath();
    
    public Coordinates getSource();

    public Coordinates getDestination();
}
