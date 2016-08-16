/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.multitouch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class TouchPointTracker {

    /**
     *
     * @param currentList
     * @param newPoints
     * @param currentTime
     */
    public static void trackPoints(ArrayList<TouchPoint> currentList,
            ArrayList<TouchPoint> newPoints, int currentTime) {

        deleteOldPoints(currentList, currentTime);
        updatePoints(currentList, newPoints);
        addNewPoints(currentList, newPoints);
        setNonUpdatedPointsSpeed(currentList);
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

    public static void setNonUpdatedPointsSpeed(ArrayList<TouchPoint> currentList) {

        // Add the new ones ?
        for (TouchPoint tp : currentList) {
            if (tp.isUpdated()) {
                continue;
            }
            tp.updateAlone();
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
