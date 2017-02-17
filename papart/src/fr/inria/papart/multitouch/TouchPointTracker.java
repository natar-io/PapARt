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
    public static void trackPoints(ArrayList<TrackedDepthPoint> currentList,
            ArrayList<TrackedDepthPoint> newPoints, int currentTime) {

        deleteOldPoints(currentList, currentTime);
        updatePoints(currentList, newPoints);
        addNewPoints(currentList, newPoints);
        setNonUpdatedPointsSpeed(currentList);
    }

    public static void updatePoints(ArrayList<TrackedDepthPoint> currentList, ArrayList<TrackedDepthPoint> newPoints) {

        // many previous points, try to find correspondances.
        ArrayList<TouchPointComparison> tpt = new ArrayList<TouchPointComparison>();
        for (TrackedDepthPoint newPoint : newPoints) {
            for (TrackedDepthPoint oldPoint : currentList) {
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

    public static void addNewPoints(ArrayList<TrackedDepthPoint> currentList, ArrayList<TrackedDepthPoint> newPoints) {

        // Add the new ones ?
        for (TrackedDepthPoint tp : newPoints) {
            if (!tp.isToDelete()) {
                currentList.add(tp);
            }
        }
    }

    public static void setNonUpdatedPointsSpeed(ArrayList<TrackedDepthPoint> currentList) {

        // Add the new ones ?
        for (TrackedDepthPoint tp : currentList) {
            if (tp.isUpdated()) {
                continue;
            }
            tp.updateAlone();
        }
    }

    public static void deleteOldPoints(ArrayList<TrackedDepthPoint> currentList, int currentTime) {
        // Clear the old ones 
        for (Iterator<TrackedDepthPoint> it = currentList.iterator();
                it.hasNext();) {
            TrackedDepthPoint tp = it.next();
            tp.setUpdated(false);

            if (tp.isObselete(currentTime)) {
                tp.delete(currentTime);
                it.remove();
            }

        }
    }
}
