/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model;

import cz.certicon.routing.application.algorithm.AlgorithmType;
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Coordinate;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Input {

    private final int numberOfRuns;
    private final String version;
    private final InputType inputType;
    private final Properties properties;
    private final AlgorithmType algorithmType;
    private final DistanceType distanceType;
    private final ReferenceOutput referenceOutput;
    private final List<Trinity<Integer, Coordinate, Coordinate>> data;

    public Input( int numberOfRuns, String version, InputType inputType, Properties properties, AlgorithmType algorithmType, DistanceType distanceType, ReferenceOutput referenceOutput, List<Trinity<Integer, Coordinate, Coordinate>> data ) {
        this.numberOfRuns = numberOfRuns;
        this.version = version;
        this.inputType = inputType;
        this.properties = properties;
        this.algorithmType = algorithmType;
        this.distanceType = distanceType;
        this.referenceOutput = referenceOutput;
        this.data = data;
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
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

    public List<Trinity<Integer, Coordinate, Coordinate>> getData() {
        return data;
    }

    public String getVersion() {
        return version;
    }

    public ReferenceOutput getReferenceOutput() {
        return referenceOutput;
    }

    @Override
    public String toString() {
        return "Input{" + "numberOfRuns=" + numberOfRuns + ", version=" + version + ", inputType=" + inputType + ", properties=" + properties + ", algorithmType=" + algorithmType + ", distanceType=" + distanceType + ", data=" + data + '}';
    }

}
