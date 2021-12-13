package fr.inria.papart.multitouch.detection;

import fr.inria.papart.multitouch.tracking.TrackedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import processing.core.PVector;

/**
 *
 * @author realitytech
 */
public class StickerCluster extends ArrayList<TrackedElement> {

    public static StickerCluster EMPTY_CLUSTER = new StickerCluster();

    public TrackedElement first;
    public PVector center;

    public PVector eigenValue = new PVector();
    public PVector eigenVector = new PVector();
//    public PMatrix2D mat = new PMatrix2D();

    public static ArrayList<StickerCluster> createZoneCluster(ArrayList<TrackedElement> elements, float size) {
        ArrayList<StickerCluster> clusterList = new ArrayList<>();

        StickerCluster sc = findZoneClusterAndRemoveIt(elements, size);
        if (sc == EMPTY_CLUSTER) {
            return clusterList;
        }

        while (sc != EMPTY_CLUSTER) {
            clusterList.add(sc);
            sc = findZoneClusterAndRemoveIt(elements, size);
        }
        return clusterList;
    }

    public static StickerCluster findZoneClusterAndRemoveIt(ArrayList<TrackedElement> elements, float size) {
        // take a random one. 
        if (elements.isEmpty()) {
            return EMPTY_CLUSTER;
        }

        // take an element.
        TrackedElement first = elements.get(0);

        StickerCluster sc = findZoneClusterAround(elements, first.getPosition(), size);
        StickerCluster sc2 = findZoneClusterAround(elements, sc.center, size);

        PVector currentCenter = sc.center;
        PVector newCenter = sc2.center;
        float d = currentCenter.dist(newCenter);
        int k = 0;

        // center is moving , 10 steps only
        while (k < 10 && d > 1) {

            // comput the cluster
            StickerCluster newCluster = findZoneClusterAround(elements, sc.center, size);

            // update the values
            sc = sc2;
            sc2 = newCluster;

            // compute the distance
            currentCenter = sc.center;
            newCenter = sc2.center;
            d = currentCenter.dist(newCenter);

            k++;
        }
        // sc2 is the last one
        elements.removeAll(sc2);
        return sc2;

    }

    private static StickerCluster findZoneClusterAround(ArrayList<TrackedElement> elements, PVector point, float size) {

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
            if (te.getPosition().dist(sc.center) > size) {
                break;
            }
            mean.add(te.getPosition());
            sc.add(te);
        }

        mean.mult(1f / sc.size());
        sc.center.set(mean);
        return sc;
    }

}
