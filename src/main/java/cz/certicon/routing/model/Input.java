/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model;

import cz.certicon.routing.data.ch.DistanceType;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinates;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Input {

    private final InputType inputType;
    private final Properties properties;
    private final AlgorithmType algorithmType;
    private final DistanceType distanceType;
    private final List<Pair<Coordinates, Coordinates>> data;

    public Input( InputType inputType, Properties properties, AlgorithmType algorithmType, DistanceType distanceType, List<Pair<Coordinates, Coordinates>> data ) {
        this.inputType = inputType;
        this.properties = properties;
        this.algorithmType = algorithmType;
        this.distanceType = distanceType;
        this.data = data;
    }

    public InputType getInputType() {
        return inputType;
    }

    public Properties getProperties() {
        return properties;
    }

    public AlgorithmType getAlgorithmType() {
        return algorithmType;
    }

    public DistanceType getDistanceType() {
        return distanceType;
    }

    public List<Pair<Coordinates, Coordinates>> getData() {
        return data;
    }

}
