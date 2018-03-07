/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import Jama.Matrix;
import com.mkobos.pca_transform.PCA;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import static javafx.scene.input.KeyCode.T;
import processing.core.PMatrix2D;
import processing.core.PVector;
import toxi.geom.Vec3D;

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
    public PMatrix2D mat = new PMatrix2D();

    public void analyse() {

        int k = 0;
        int total = this.size();
        double[][] dataPoints = new double[total][2];
        for (TrackedElement te : this) {
            dataPoints[k][0] = te.getPosition().x;
            dataPoints[k][1] = te.getPosition().y;
            k++;
        }
        //  column corresponding to dimension: x, y, z */
        Matrix trainingData = new Matrix(dataPoints);
        PCA fingerPCA = new PCA(trainingData);
        Matrix vectors = fingerPCA.getEigenvectorsMatrix();

        try {
            if (fingerPCA.getEigenvectorsMatrix().getColumnDimension() != 2
                    || fingerPCA.getEigenvectorsMatrix().getRowDimension() != 2) {
                System.out.println("EigenMatrix too short.");
// Propably not finger
            } else {

                // Size in X?
                double e0 = fingerPCA.getEigenvalue(0);

                // Size in Y
                double e1 = fingerPCA.getEigenvalue(1);

                eigenValue.set((float) e0, (float) e1);
                eigenVector.set((float) vectors.get(0, 0), (float) vectors.get(0, 1));

                PMatrix2D newmat = new PMatrix2D((float) vectors.get(0, 0), (float) vectors.get(0, 1), 0,
                        (float) vectors.get(1, 0), (float) vectors.get(1, 1), 0);
                this.mat.set(newmat);

//                System.out.println("Eigen values: " + e0 + " " + e1);
//                System.out.println("Eigen vectors: "
//                        + vectors.get(0, 0) + " "
//                        + vectors.get(0, 1) + " "
//                        + vectors.get(1, 0) + " "
//                        + vectors.get(1, 1) + " ");

                // rotate stuff
                
//                for (TrackedElement te : this) {
//                    PVector out = new PVector();
//                    PVector center = this.center.get();
//                    
//                    PVector v2 = te.getPosition().sub(center);
//                    mat.mult(v2, out);
//                    
//                    v2 = out.add(center);
//                    te.getPosition().set(v2);
//                }
            }

//                fingerPCA.transform(vectors, PCA.TransformationType.ROTATION)
            // e0 -> length  of finger
            // e1 -> width of finger                // e0 -> length  of finger
            // e1 -> width of finger
//                Matrix eigenvectorsMatrix = fingerPCA.getEigenvectorsMatrix().copy();
//                eigenvectorsMatrix.inverse();
//
//                Matrix furtherPt = new Matrix(new double[][]{
//                    {e0 / 8f, 0, 0}});
//
//                Matrix mult = furtherPt.times(eigenvectorsMatrix);
//
////                System.out.println("Mult: " + mult.get(0, 0)
////                        + " " + mult.get(0, 1)
////                        + " " + mult.get(0, 2));
//                Vec3D shift = new Vec3D(
//                        (float) mult.get(0, 0),
//                        (float) mult.get(0, 1), 0);
//                        (float) mult.get(0, 2));
//                TrackedDepthPoint tp = createTouchPoint(connectedComponent, shift);
//                Matrix testData = new Matrix(new double[][]{
//                    {
//                        tp.getPositionDepthCam().x,
//                        tp.getPositionDepthCam().y,
//                        tp.getPositionDepthCam().z}});
//                Matrix transformedData
//                        = fingerPCA.transform(testData, PCA.TransformationType.ROTATION);
//                float trX = (float) transformedData.get(0, 0);
//                float trY = (float) transformedData.get(0, 1);
//                float trZ = (float) transformedData.get(0, 2);
//                System.out.println(trX + " " + trY + " " + trZ);
//                newPoints.add(tp);
//
//                if (e0 < 20f && e0 > 200f
//                        && e1 < 30) {
//                    continue;
//                }
        } catch (Exception e) {
            System.out.println("Error ");
            e.printStackTrace();
        }

    }

    public static ArrayList<StickerCluster> createCluster(ArrayList<TrackedElement> elements, float size) {
        ArrayList<StickerCluster> clusterList = new ArrayList<>();

        StickerCluster sc = findClusterAndRemoveIt(elements, size);
        if (sc == EMPTY_CLUSTER) {
            return clusterList;
        }

        while (sc != EMPTY_CLUSTER) {
            clusterList.add(sc);
            sc = findClusterAndRemoveIt(elements, size);
        }
        return clusterList;
    }

    public static StickerCluster findClusterAndRemoveIt(ArrayList<TrackedElement> elements, float size) {

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

        StickerCluster sc = findClusterAround(elements, first.getPosition(), size);
        StickerCluster sc2 = findClusterAround(elements, sc.center, size);

        PVector currentCenter = sc.center;
        PVector newCenter = sc2.center;
        float d = currentCenter.dist(newCenter);
//        System.out.println("d1: " + d);
        int k = 0;
        // center is moving , 10 steps only
        while (k < 10 && d > 1) {

            // comput the cluster
            StickerCluster newCluster = findClusterAround(elements, sc.center, size);

            // update the values
            sc = sc2;
            sc2 = newCluster;

            // compute the distance
            currentCenter = sc.center;
            newCenter = sc2.center;
            d = currentCenter.dist(newCenter);

            k++;
        }
//        System.out.println("k :" + k );
        // sc2 is the last one
        elements.removeAll(sc2);
        return sc2;

    }

    private static StickerCluster findClusterAround(ArrayList<TrackedElement> elements, PVector point, float size) {

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
