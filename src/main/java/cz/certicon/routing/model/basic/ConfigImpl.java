/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import cz.certicon.routing.model.Config;
import cz.certicon.routing.model.entity.Coordinate;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ConfigImpl implements Config {

    private final String pbfPath;
    private final Coordinate source;
    private final Coordinate destination;

    public ConfigImpl( String pbfPath, Coordinate source, Coordinate destination ) {
        this.pbfPath = pbfPath;
        this.source = source;
        this.destination = destination;
    }

    @Override
    public String getPbfPath() {
        return pbfPath;
    }

    @Override
    public Coordinate getSource() {
        return source;
    }

    @Override
    public Coordinate getDestination() {
        return destination;
    }

}
