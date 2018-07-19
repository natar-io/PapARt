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

import fr.inria.papart.multitouch.tracking.Trackable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Tracking methods, quite abstract for many uses.
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class TouchPointTracker {

    /**
     * Update the current list with the new points. delete the old points,
     * update the existing with the new ones, adds the new points to the current
     * list, and updates the speed of all non-updated points.
     *
     * @param <T>
     * @param currentList
     * @param newPoints
     * @param currentTime
     */
    public static <T extends Trackable> void trackPoints(ArrayList<T> currentList,
            ArrayList<T> newPoints, int currentTime) {

//        System.out.println("In TrackPoints: " + currentList.size() + " new: " + newPoints);
        deleteOldPoints(currentList, currentTime);
//        System.out.println("deleted old points: " + currentList.size() + ".");
        updatePoints(currentList, newPoints);

//        System.out.println("updated old points: " + currentList.size() + ".");
        addNewPoints(currentList, newPoints, currentTime);

//        System.out.println("Added new points: " + currentList.size() + ".");
        setNonUpdatedPointsSpeed(currentList);

//        System.out.println("Updated other points: " + currentList.size() + ".");
    }

    public static <T extends Trackable> void filterPositions(ArrayList<T> currentList) {
        // Add the new ones ?
        for (Trackable tp : currentList) {
            tp.filter();
        }
    }

    public static <T extends Trackable> void filterPositions(ArrayList<T> currentList, int time) {
        // Add the new ones ?
        for (Trackable tp : currentList) {
            tp.filter(time);
        }
    }

    public static <T extends Trackable> void updatePoints(ArrayList<T> currentList, ArrayList<T> newPoints) {

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
            if (tpc.distance < tpc.newTp.getTrackingMaxDistance()) {
//            if (tpc.distance < tpc.newTp.getDetection().getTrackingMaxDistance()) {
                // new points are marked for deletion after update.
                tpc.update();
            }
        }
    }

    public static <T extends Trackable> void addNewPoints(ArrayList<T> currentList, ArrayList<T> newPoints, int currentTime) {

        // Add the new ones ?
        for (T tp : newPoints) {
            if (!tp.isToDelete()) {
                currentList.add(tp);
                tp.setCreationTime(currentTime);
            }
        }
    }

    public static <T extends Trackable> T convertInstanceOfObject(Object o, Class<T> clazz) {
        try {
            return clazz.cast(o);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public static <T extends Trackable> void setNonUpdatedPointsSpeed(ArrayList<T> currentList) {

        // Add the new ones ?
        for (Trackable tp : currentList) {
            if (!tp.isUpdated()) {
                tp.updateAlone();
            }

        }
    }

    public static <T extends Trackable> void deleteOldPoints(ArrayList<T> currentList, int currentTime) {
        // Clear the old ones 
        for (Iterator<T> it = currentList.iterator();
                it.hasNext();) {
            Trackable tp = it.next();
            tp.setUpdated(false);

            if (tp.isObselete(currentTime)) {
                tp.delete(currentTime);
                it.remove();
            }

        }
    }
}
