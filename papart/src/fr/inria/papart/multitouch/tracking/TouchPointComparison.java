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
package fr.inria.papart.multitouch.tracking;

import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.multitouch.tracking.TrackedElement;

/**
 *
 * @author jeremy Laviole - laviole@rea.lity.tech
 */
public class TouchPointComparison<T extends TrackedElement> implements Comparable<TouchPointComparison<T>> {

    T oldTp;
    T newTp;
    float distance;
    int commonElements;

    public TouchPointComparison(T oldTp, T newTp) {
        this.oldTp = oldTp;
        this.newTp = newTp;

        if (oldTp instanceof TrackedDepthPoint) {
            distance = ((TrackedDepthPoint) oldTp).distanceTo((TrackedDepthPoint) newTp);
            // Common elements method not used. -> problematic results of jittering.
//            commonElements = TrackedDepthPoint.numberOfCommonElements((TrackedDepthPoint) oldTp,
//                    (TrackedDepthPoint) newTp);
        } else {
            distance = oldTp.distanceTo(newTp);
        }
    }

    public T getOld() {
        return oldTp;
    }

    public T getNew() {
        return newTp;
    }

    public boolean update() {
        return oldTp.updateWith(newTp);
    }

    public int compareTo(TouchPointComparison tpt) {
//            System.out.println("commons" + commonElements + " " + tpt.commonElements);
//        int haveCommons = Integer.compare(commonElements, tpt.commonElements);
//        if(haveCommons == 0 ){  // same number in common, or none.
//            // use the distance
               return Float.compare(distance, tpt.distance);
//        } 
    
//        return haveCommons;
    }

    @Override
    public String toString() {
        return "TouchPointTracker : \n " + getOld() + "\n" + getNew() + " \nDistance " + distance + " \n";
    }
}
