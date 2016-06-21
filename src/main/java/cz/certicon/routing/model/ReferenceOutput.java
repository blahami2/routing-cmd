/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model;

import cz.certicon.routing.application.algorithm.AlgorithmType;
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.model.basic.Length;
import cz.certicon.routing.model.basic.LengthUnits;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Time;
import cz.certicon.routing.model.basic.TimeUnits;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ReferenceOutput {

    private static final TimeUnits TIME_UNITS = TimeUnits.SECONDS;
    private static final LengthUnits LENGTH_UNITS = LengthUnits.METERS;

    private final DistanceType distanceType;
    private final Map<Integer, Pair<Time, Length>> data;

    public ReferenceOutput( DistanceType distanceType, Map<Integer, Pair<Time, Length>> data ) {
        this.distanceType = distanceType;
        this.data = data;
    }

    public DistanceType getDistanceType() {
        return distanceType;
    }

    public Map<Integer, Pair<Time, Length>> getData() {
        return data;
    }

    public Map<Integer, AccuracyPair> calculateAccuracy( List<PathStats> paths ) {
        Map<Integer, AccuracyPair> accuracyMap = new HashMap<>();
        for ( int i = 0; i < paths.size(); i++ ) {
            PathStats path = paths.get( i );
            Pair<Time, Length> ref = data.get( path.getId() );
            int timeAcc = (int) ( ( 100 * ref.a.getTime( TIME_UNITS ) ) / path.getTime().getTime( TIME_UNITS ) );
            int lengthAcc = (int) ( ( 100 * ref.b.getLength( LENGTH_UNITS ) ) / path.getLength().getLength( LENGTH_UNITS ) );
            accuracyMap.put( path.getId(), new AccuracyPair( timeAcc, lengthAcc ) );
        }
        return accuracyMap;
    }

    public static class AccuracyPair {

        public final int time;
        public final int length;

        public AccuracyPair( int time, int length ) {
            this.time = time;
            this.length = length;
        }

    }
}
