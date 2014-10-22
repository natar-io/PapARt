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

/**
 *
 * @author jeremy
 */
public class TouchPointTracker implements Comparable<TouchPointTracker> {

    TouchPoint oldTp;
    TouchPoint newTp;
    float distance;

    public TouchPointTracker(TouchPoint oldTp, TouchPoint newTp) {
        this.oldTp = oldTp;
        this.newTp = newTp;
        
        distance = oldTp.distanceTo(newTp);
    }

    public TouchPoint getOld() {
        return oldTp;
    }

    public TouchPoint getNew() {
        return newTp;
    }

    public boolean update(int currentTime) {
        return oldTp.updateWith(newTp, currentTime);
    }

    public int compareTo(TouchPointTracker tpt) {
        return Float.compare(distance, tpt.distance);
    }

    @Override
    public String toString() {
        return "TouchPointTracker : \n " + getOld() + "\n" + getNew() + " \nDistance " + distance + " \n";
    }
}
