/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model;

import cz.certicon.routing.model.basic.Length;
import cz.certicon.routing.model.basic.Time;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class PathStats {

    private final int id;
    private final Time time;
    private final Length length;

    public PathStats( int id, Time time, Length length ) {
        this.id = id;
        this.time = time;
        this.length = length;
    }

    public Time getTime() {
        return time;
    }

    public Length getLength() {
        return length;
    }

    public int getId() {
        return id;
    }

}
