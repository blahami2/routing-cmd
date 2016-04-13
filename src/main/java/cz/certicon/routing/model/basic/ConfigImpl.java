/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import cz.certicon.routing.model.Config;
import cz.certicon.routing.model.PathPresenterEnum;
import cz.certicon.routing.model.entity.Coordinates;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ConfigImpl implements Config {

    private final String fileName;
    private final String inputDataFolderPath;
    private final String referenceRouteStatsPath;
    private final Coordinates source;
    private final Coordinates destination;
    private PathPresenterEnum pathPresenterEnum;

    public ConfigImpl( String fileName, String inputDataFolderPath, String referenceRouteStatsPath, Coordinates source, Coordinates destination ) {
        this.fileName = fileName;
        this.inputDataFolderPath = inputDataFolderPath;
        this.referenceRouteStatsPath = referenceRouteStatsPath;
        this.source = source;
        this.destination = destination;
    }

    public void setPathPresenterEnum( PathPresenterEnum pathPresenterEnum ) {
        this.pathPresenterEnum = pathPresenterEnum;
    }

    @Override
    public Coordinates getSource() {
        return source;
    }

    @Override
    public Coordinates getDestination() {
        return destination;
    }

    @Override
    public String getReferenceRouteStatsPath() {
        return referenceRouteStatsPath;
    }

    @Override
    public String getInputDataFolderPath() {
        return inputDataFolderPath;
    }

    @Override
    public PathPresenterEnum getPathPresenter() {
        return pathPresenterEnum;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

}
