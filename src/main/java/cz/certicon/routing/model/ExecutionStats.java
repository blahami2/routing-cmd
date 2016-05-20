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
public class ExecutionStats {

    private final long nodeSearchTime;
    private final long routeTime;
    private final long routeBuildingTime;
    private final long pathLoadTime;

    public ExecutionStats( long nodeSearchTime, long routeTime, long routeBuildingTime, long pathLoadTime ) {
        this.nodeSearchTime = nodeSearchTime;
        this.routeTime = routeTime;
        this.routeBuildingTime = routeBuildingTime;
        this.pathLoadTime = pathLoadTime;
    }

    public long getNodeSearchTime() {
        return nodeSearchTime;
    }

    public long getRouteTime() {
        return routeTime;
    }

    public long getRouteBuildingTime() {
        return routeBuildingTime;
    }

    public long getPathLoadTime() {
        return pathLoadTime;
    }
}
