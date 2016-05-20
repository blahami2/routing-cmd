/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class PathStats {

    private final double time;
    private final double length;

    public PathStats( double time, double length ) {
        this.time = time;
        this.length = length;
    }

    public double getTime() {
        return time;
    }

    public double getLength() {
        return length;
    }

}
