/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model;

import cz.certicon.routing.model.basic.Time;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ExecutionStats {

    private final int id;
    private final Time nodeSearchTime;
    private final Time routeTime;
    private final Time routeBuildingTime;
    private final Time pathLoadTime;

    public ExecutionStats( int id, Time nodeSearchTime, Time routeTime, Time routeBuildingTime, Time pathLoadTime ) {
        this.id = id;
        this.nodeSearchTime = nodeSearchTime;
        this.routeTime = routeTime;
        this.routeBuildingTime = routeBuildingTime;
        this.pathLoadTime = pathLoadTime;
    }

    public int getId() {
        return id;
    }

    public Time getNodeSearchTime() {
        return nodeSearchTime;
    }

    public Time getRouteTime() {
        return routeTime;
    }

    public Time getRouteBuildingTime() {
        return routeBuildingTime;
    }

    public Time getPathLoadTime() {
        return pathLoadTime;
    }
}
