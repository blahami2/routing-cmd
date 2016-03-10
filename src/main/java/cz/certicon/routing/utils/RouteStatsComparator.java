/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import cz.certicon.routing.model.RouteStats;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class RouteStatsComparator {

    public static double calculateAccuracy( RouteStats expected, RouteStats actual ) {
        double time = (double) expected.getTime() / (double) actual.getTime();
        double length = (double) expected.getLength() / (double) actual.getLength();
        double price = ( actual.getPrice() == 0 ) ? 0 : expected.getPrice() / actual.getPrice();
        return time * length * ( ( price == 0 ) ? 1 : price );
    }
}
