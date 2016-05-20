/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model;

import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Result {

    private final List<PathStats> paths;
    private final List<ExecutionStats> executions;

    public Result( List<PathStats> paths, List<ExecutionStats> executions ) {
        this.paths = paths;
        this.executions = executions;
    }

    public List<PathStats> getPaths() {
        return paths;
    }

    public List<ExecutionStats> getExecutions() {
        return executions;
    }
}
