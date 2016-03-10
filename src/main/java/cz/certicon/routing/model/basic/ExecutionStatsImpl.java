/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import cz.certicon.routing.model.ExecutionStats;

public class ExecutionStatsImpl implements ExecutionStats {

    private final long time;
    private final long memory;
    private final double accuracy;

    public ExecutionStatsImpl( long time, long memory, double accuracy ) {
        this.time = time;
        this.memory = memory;
        this.accuracy = accuracy;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public long getMemory() {
        return memory;
    }

    @Override
    public double getAccuracy() {
        return accuracy;
    }

}
