/*
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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

//    static public final int forgetTime = 1000;       // in ms -- TODO: For debug
    static public final int forgetTime = 250;       // in ms

    public static void trackPoints(ArrayList<TouchPoint> currentList,
            ArrayList<TouchPoint> newPoints, int currentTime,
            float trackDistance) {

        deleteOldPoints(currentList, currentTime);

        updatePoints(currentList, newPoints, trackDistance);
        addNewPoints(currentList, newPoints);

    }

    public static void updatePoints(ArrayList<TouchPoint> currentList, ArrayList<TouchPoint> newPoints, float trackDistance) {

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
            if (tpc.distance < trackDistance) {
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
            if (tp.isObselete(currentTime, forgetTime)) {
                tp.setToDelete();
                it.remove();
            }
        }
    }
}
