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

/**
 *
 * @author jeremy
 */
public class TouchPointComparison implements Comparable<TouchPointComparison> {

        TouchPoint oldTp;
        TouchPoint newTp;
        float distance;

        public TouchPointComparison(TouchPoint oldTp, TouchPoint newTp) {
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

        public boolean update() {
            return oldTp.updateWith(newTp);
        }

        public int compareTo(TouchPointComparison tpt) {
            return Float.compare(distance, tpt.distance);
        }

        @Override
        public String toString() {
            return "TouchPointTracker : \n " + getOld() + "\n" + getNew() + " \nDistance " + distance + " \n";
        }
    }