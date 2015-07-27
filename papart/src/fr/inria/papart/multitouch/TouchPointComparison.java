/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
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