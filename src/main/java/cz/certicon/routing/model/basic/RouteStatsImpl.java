/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import cz.certicon.routing.model.RouteStats;

public class RouteStatsImpl implements RouteStats {

    private final long length;
    private final long time;
    private final double price;

    public RouteStatsImpl( long length, long time, double price ) {
        this.length = length;
        this.time = time;
        this.price = price;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public double getPrice() {
        return price;
    }

}
