/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import Jama.Matrix;
import com.mkobos.pca_transform.PCA;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import processing.core.PVector;

/**
 *
 * @author realitytech
 */
class LinePair{
    public TrackedElement t1, t2;
    
    public boolean equals(Object o){
        LinePair p = (LinePair ) o;
        
        return (t1 == p.t1 && t2 == p.t2) || 
                (t1 == p.t2 && t2 == p.t1);
    }
}

public class LineCluster extends StickerCluster {

    public ArrayList<TrackedElement> line = new ArrayList<>();

    public LineCluster(
            TrackedElement t1,
            TrackedElement t2,
            TrackedElement t3) {
        assert(t1 != t2);
        assert(t1 != t3);
        assert(t2 != t3);
        line.add(t1);
        line.add(t2);
        line.add(t3);
    }

    private double angle = Float.MIN_VALUE;

    public PVector position() {
        PVector mean = new PVector();
        for (TrackedElement t : line) {
            mean.add(t.getPosition());
        }
        mean.mult(1f / line.size());
        return mean;
    }

    public boolean highAngle = false;
    
    private void computeAngle() {
        double[][] dataPoints = new double[line.size()][2];
        for (int i = 0; i < line.size(); i++) {
            dataPoints[i][0] = line.get(i).getPosition().x;
            dataPoints[i][1] = line.get(i).getPosition().y;
        }
        //  column corresponding to dimension: x, y, z */
        Matrix trainingData = new Matrix(dataPoints);
        PCA pca = new PCA(trainingData);
        Matrix vectors = pca.getEigenvectorsMatrix();

        if (pca.getEigenvectorsMatrix().getColumnDimension() != 2
                || pca.getEigenvectorsMatrix().getRowDimension() != 2) {

//                System.out.println("EigenMatrix too short. ?");
//                for (int i = 0; i < line.size(); i++) {
//                    System.out.println("id: " + i + " " + line.get(i).getPosition());
//                }
            // Short eigen matrix -> Points aligned according to an existing axis. 
            float x = line.get(0).getPosition().x;
            float y = line.get(0).getPosition().y;
            int xCount = 1;
            int yCount = 1;

            for (int i = 1; i < line.size(); i++) {
                float x1 = line.get(i).getPosition().x;
                float y1 = line.get(i).getPosition().y;

                if (x == x1) {
                    xCount++;
                }
                if (y == y1) {
                    yCount++;
                }
            }
            // same X
            if (xCount == line.size()) {
                angle = 0;
            }
            // same Y
            if (yCount == line.size()) {
                angle = Math.PI;
            }

        } else {

            // Size in X?
            e0 = pca.getEigenvalue(0);
            // Size in Y // should be very low.
            e1 = pca.getEigenvalue(1);

            // check that e1 is very low.
//            if (e1 > 2) {
////                System.out.println("e1 high: " + e1);
//                highAngle = true;
//            }
//            System.out.println("e0: " + e0);
            angle = Math.atan2(vectors.get(0, 1), vectors.get(0, 0));
        }
    }

    public double e0, e1;

    public double angle() {
        if (angle != Float.MIN_VALUE) {
            return angle;
        }else{
            computeAngle();
        }
        return angle;
    }

    @Override
    public boolean equals(Object other) {
        return ((LineCluster) other).line.containsAll(line);
    }

//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 83 * hash + Objects.hashCode(this.line);
//        return hash;
//    }
    /**
     * TODO:
     *
     * @param elements
     * @param size
     * @return
     */
    public static ArrayList<LineCluster> createLineCluster(ArrayList<TrackedElement> elements, float size) {
        ArrayList<LineCluster> allLines = findLineCluster(elements, size);

        // Regroup the lines... 
        return allLines;
    }

    public static ArrayList<LineCluster> findLineCluster(ArrayList<TrackedElement> elements, float size) {
        // take a random one. 
        if (elements.isEmpty()) {
            return new ArrayList<>();
        }

        // for each elements... 
//         take an element.
        ArrayList<LineCluster> lines = new ArrayList<>();

        Iterator<TrackedElement> it = elements.iterator();

        // With iterators, we can delete elements on the fly. 
        while (it.hasNext()) {
            TrackedElement first = it.next();;

//        for (TrackedElement first : elements) {
//        TrackedElement first = elements.get(0);
            // Get the closets elements
            ArrayList<TrackedElement> elementsCopy = new ArrayList<TrackedElement>(elements);
            elementsCopy.remove(first);
            
            // Deep search from this.

            Predicate<TrackedElement> closeFilter = te -> te.distanceTo(first) > size;
            elementsCopy.removeIf(closeFilter);

            // find triplets only. 
            if (elementsCopy.size() < 2) {
                continue;
//                return new ArrayList<>();
            }

            float epsilon = 1f;

            // find the triplets. (naive version, too many calls).
            for (TrackedElement t1 : elementsCopy) {
                for (TrackedElement t2 : elementsCopy) {
                    if (t1 == t2) {
                        continue;
                    }
                    PVector a = first.getPosition();
                    PVector b = t1.getPosition();
                    PVector c = t2.getPosition();

                    if (pointsColinear(a, b, c)) {
                        LineCluster lineCluster = new LineCluster(first, t1, t2);

                        if (!lines.contains(lineCluster)) {
                            lines.add(lineCluster);
                            lineCluster.computeAngle();
                        }
                    }
                }

            }
            it.remove();
        }

        return lines;
    }

    public static float epsilon = 15f;

    /**
     * Check if 3 points are aligned.
     * https://stackoverflow.com/questions/3813681/checking-to-see-if-3-points-are-on-the-same-line
     * [ Ax * (By - Cy) + Bx * (Cy - Ay) + Cx * (Ay - By) ] / 2
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static boolean pointsColinear(PVector a, PVector b, PVector c) {
        float area = (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y)) / 2;
//        System.out.println("area: " + area);
        return Math.abs(area) < epsilon;
    }

}
