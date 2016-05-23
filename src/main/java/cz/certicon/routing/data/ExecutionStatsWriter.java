/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import cz.certicon.routing.model.ExecutionStats;
import cz.certicon.routing.model.Input;
import cz.certicon.routing.model.Result;
import cz.certicon.routing.model.basic.TimeUnits;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ExecutionStatsWriter {

    static final TimeUnits TIME_UNITS = TimeUnits.NANOSECONDS;

    public void write( DataDestination destination, Input input, Result result ) throws IOException;
}
