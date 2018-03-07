/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.multitouch.tracking.TrackedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import static javafx.scene.input.KeyCode.T;
import processing.core.PVector;

/**
 *
 * @author realitytech
 */
public class StickerCluster extends ArrayList<TrackedElement> {

    public static StickerCluster EMPTY_CLUSTER = new StickerCluster();

    public TrackedElement first;
    public PVector center;

    static float dist = 40; 
    
    public static ArrayList<StickerCluster> createCluster(ArrayList<TrackedElement> elements) {
        ArrayList<StickerCluster> clusterList = new ArrayList<>();

        StickerCluster sc = findClusterAndRemoveIt(elements);
        if (sc == EMPTY_CLUSTER) {
            return clusterList;
        }

        while (sc != EMPTY_CLUSTER) {
            clusterList.add(sc);
            sc = findClusterAndRemoveIt(elements);
        }
        return clusterList;
    }

    public static StickerCluster findClusterAndRemoveIt(ArrayList<TrackedElement> elements) {

        // take a random one. 
        if (elements.isEmpty()) {
            return EMPTY_CLUSTER;
        }

        // take an element.
        TrackedElement first = elements.get(0);

        Comparator<TrackedElement> distanceComparator = new Comparator<TrackedElement>() {
            public int compare(TrackedElement t1, TrackedElement t2) {
                return Float.compare(t1.distanceTo(first), t2.distanceTo(first));
            }
        };

        StickerCluster sc = findClusterAround(elements, first.getPosition());
        StickerCluster sc2 = findClusterAround(elements, sc.center);

        PVector currentCenter = sc.center;
        PVector newCenter = sc2.center;
        float d = currentCenter.dist(newCenter);
        System.out.println("d1: " + d);
        int k = 0;
        // center is moving , 10 steps only
        while (k < 10 && d > 1){

            // comput the cluster
            StickerCluster newCluster = findClusterAround(elements, sc.center);
            
            // update the values
            sc = sc2;
            sc2 = newCluster;
            
            // compute the distance
            currentCenter = sc.center;
            newCenter = sc2.center;
            d = currentCenter.dist(newCenter);

            k++;
        }
        System.out.println("k :" + k );
        // sc2 is the last one
        elements.removeAll(sc2);
        return sc2;

//        StickerCluster sc = new StickerCluster();
//        sc.first = first;
//
//        PVector mean = new PVector();
//
//        // find its neighbours by distance.
//        Collections.sort(elements, distanceComparator);
//        for (TrackedElement te : elements) {
//            if (te == first) {
//                continue;
//            }
//
//            if (te.distanceTo(first) > 50) {
//                break;
//            }
//            mean.add(te.getPosition());
//            sc.add(te);
//        }
//
//        mean.mult(1f / sc.size());
        // We have a cluster, we find the center point, and restart the search !
//        elements.remove(first);
//        elements.removeAll(sc);
//        return sc;
    }

    private static StickerCluster findClusterAround(ArrayList<TrackedElement> elements, PVector point) {

        StickerCluster sc = new StickerCluster();
        sc.center = point.get();
        PVector mean = new PVector();

        Comparator<TrackedElement> distanceComparator = new Comparator<TrackedElement>() {
            public int compare(TrackedElement t1, TrackedElement t2) {
                return Float.compare(
                        t1.getPosition().dist(sc.center),
                        t2.getPosition().dist(sc.center));
            }
        };

        // find its neighbours by distance.
        Collections.sort(elements, distanceComparator);
        for (TrackedElement te : elements) {
//            if (te == first) {
//                continue;
//            }
            if (te.getPosition().dist(sc.center) > dist) {
                break;
            }
            mean.add(te.getPosition());
            sc.add(te);
        }

        mean.mult(1f / sc.size());
        sc.center.set(mean);
        return sc;
    }

//    class TrackedElementComparator<TrackedElement> implements Comparable<TrackedElementComparator<TrackedElement>> {
//        TrackedElement t1;
//        float distance;
//        int commonElements;
//
//        public TrackedElementComparator(TrackedElement t1) {
//            this.t1 = t1;
//        }
//
//        public int compareTo(TrackedElementComparator<TrackedElement> t) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//    }
}
