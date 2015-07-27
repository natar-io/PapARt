/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.multitouch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class TouchPointTracker {

    /**
     *
     * @param currentList
     * @param newPoints
     * @param currentTime
     * @return the deleted points.
     */
    public static void trackPoints(ArrayList<TouchPoint> currentList,
            ArrayList<TouchPoint> newPoints, int currentTime) {

        deleteOldPoints(currentList, currentTime);
        updatePoints(currentList, newPoints);
        addNewPoints(currentList, newPoints);
    }

    public static void updatePoints(ArrayList<TouchPoint> currentList, ArrayList<TouchPoint> newPoints) {

        // many previous points, try to find correspondances.
        ArrayList<TouchPointComparison> tpt = new ArrayList<TouchPointComparison>();
        for (TouchPoint newPoint : newPoints) {
            for (TouchPoint oldPoint : currentList) {
                tpt.add(new TouchPointComparison(oldPoint, newPoint));
            }
        }

        // update the old touch points with the new informations. 
        // to keep the informations coherent.
        Collections.sort(tpt);

        for (TouchPointComparison tpc : tpt) {
            if (tpc.distance < tpc.newTp.getDetection().getTrackingMaxDistance()) {
                // new points are marked for deletion after update.
                tpc.update();
            }
        }
    }

    public static void addNewPoints(ArrayList<TouchPoint> currentList, ArrayList<TouchPoint> newPoints) {

        // Add the new ones ?
        for (TouchPoint tp : newPoints) {
            if (!tp.isToDelete()) {
                currentList.add(tp);
            }
        }
    }

    public static void deleteOldPoints(ArrayList<TouchPoint> currentList, int currentTime) {
        // Clear the old ones 
        for (Iterator<TouchPoint> it = currentList.iterator();
                it.hasNext();) {
            TouchPoint tp = it.next();
            tp.setUpdated(false);

            if (tp.isObselete(currentTime)) {
                tp.delete(currentTime);
                it.remove();
            }

        }
    }
}
