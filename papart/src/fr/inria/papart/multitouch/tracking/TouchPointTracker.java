/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
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
package fr.inria.papart.multitouch.tracking;

import fr.inria.papart.multitouch.tracking.TrackedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class TouchPointTracker {

    /**
     * Update the current list with the new points. 
     * delete the old points, update the existing with the new ones, adds the 
     * new points to the current list, and updates the speed of all non-updated
     * points. 
     * @param <T>
     * @param currentList
     * @param newPoints
     * @param currentTime
     */
    public static <T extends TrackedElement> void trackPoints(ArrayList<T> currentList,
            ArrayList<T> newPoints, int currentTime) {

        deleteOldPoints(currentList, currentTime);
        updatePoints(currentList, newPoints);
        addNewPoints(currentList, newPoints);
        setNonUpdatedPointsSpeed(currentList);
    }
    public static <T extends TrackedElement> void filterPositions(ArrayList<T> currentList){
               // Add the new ones ?
        for (TrackedElement tp : currentList) {
                tp.filter();
        }
    }
    public static <T extends TrackedElement> void filterPositions(ArrayList<T> currentList, int time){
               // Add the new ones ?
        for (TrackedElement tp : currentList) {
                tp.filter(time);
        }
    }

    public static <T extends TrackedElement> void updatePoints(ArrayList<T> currentList, ArrayList<T> newPoints) {

        // many previous points, try to find correspondances.
        ArrayList<TouchPointComparison> tpt = new ArrayList<>();
        for (T newPoint : newPoints) {
            for (T oldPoint : currentList) {
                tpt.add(new TouchPointComparison<T>(oldPoint, newPoint));
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

    public static <T extends TrackedElement> void addNewPoints(ArrayList<T> currentList, ArrayList<T> newPoints) {

        // Add the new ones ?
        for (T tp : newPoints) {
            if (!tp.isToDelete()) {
                currentList.add(tp);
            }
        }
    }

    public static <T extends TrackedElement> T convertInstanceOfObject(Object o, Class<T> clazz) {
        try {
            return clazz.cast(o);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public static <T extends TrackedElement> void setNonUpdatedPointsSpeed(ArrayList<T> currentList) {

        // Add the new ones ?
        for (TrackedElement tp : currentList) {
            if (!tp.isUpdated()) {
                tp.updateAlone();
            }
            
        }
    }

    public static <T extends TrackedElement> void deleteOldPoints(ArrayList<T> currentList, int currentTime) {
        // Clear the old ones 
        for (Iterator<T> it = currentList.iterator();
                it.hasNext();) {
            TrackedElement tp = it.next();
            tp.setUpdated(false);

            if (tp.isObselete(currentTime)) {
                tp.delete(currentTime);
                it.remove();
            }

        }
    }
}
