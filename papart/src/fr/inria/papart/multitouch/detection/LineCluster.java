/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import Jama.Matrix;
import com.mkobos.pca_transform.PCA;
import fr.inria.papart.multitouch.tracking.Trackable;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.atan2;
import processing.core.PVector;

/**
 *
 * @author realitytech
 */
class LinePair {

    public TrackedElement t1, t2;

    public boolean equals(Object o) {
        LinePair p = (LinePair) o;

        return (t1 == p.t1 && t2 == p.t2)
                || (t1 == p.t2 && t2 == p.t1);
    }
}

public class LineCluster extends ArrayList<TrackedElement> implements Trackable {

    public LineCluster() {
        super();
    }

    public LineCluster(
            TrackedElement t1,
            TrackedElement t2,
            TrackedElement t3) {
        assert (t1 != t2);
        assert (t1 != t3);
        assert (t2 != t3);
        this.add(t1);
        this.add(t2);
        this.add(t3);
    }

    public double angle = Float.MIN_VALUE;
    public boolean highAngle = false;

    private LineCluster(LineCluster lineCluster) {
        super(lineCluster);
    }

    public PVector position() {
        PVector mean = new PVector();
        for (TrackedElement t : this) {
            mean.add(t.getPosition());
        }
        mean.mult(1f / this.size());
        return mean;
    }
    
    public String getStringCode(boolean numeric) {
        ArrayList<TrackedElement> copy = new ArrayList<>(this);
        PVector border = this.getBorders()[0].getPosition();
        Collections.sort(copy, new Comparator<TrackedElement>() {
            @Override
            public int compare(TrackedElement t1, TrackedElement t2) {
                return Float.compare(t2.getPosition().dist(border), t1.getPosition().dist(border));
            }
        });
        
        StringBuilder code = new StringBuilder();
        for (TrackedElement te : copy) {
            if (numeric) {
                code.append(Integer.toString(te.attachedValue));
            }
            else {
                switch (te.attachedValue) {
                    case 0:
                        code.append("R");
                        break;
                    case 1:
                        code.append("B");
                        break;
                    case 2:
                        code.append("G");
                        break;
                    case 3:
                        code.append("Y");
                        break;
                    case 4:
                        code.append("P");
                        break;
                    default:
                        break;
                }
            }
        }
        return code.toString();
    }
    
    public String getFlippedStringCode(boolean numeric) {
        StringBuilder sb = new StringBuilder(this.getStringCode(numeric));
        return sb.reverse().toString();
    }

    public TrackedElement[] getBorders() {
        TrackedElement[] borders = new TrackedElement[2];
        ArrayList<TrackedElement> copy = new ArrayList<>(this);
        PVector center = this.position();

        Collections.sort(copy, new Comparator<TrackedElement>() {
            @Override
            public int compare(TrackedElement t1, TrackedElement t2) {
                return Float.compare(t2.getPosition().dist(center), t1.getPosition().dist(center));
            }
        });

        borders[0] = copy.get(0);
        borders[1] = copy.get(1);

        return borders;
    }

    public boolean tryAddColinear(TrackedElement third) {

        if (this.contains(third)) {
            return false;
        }
        boolean aligned = true;

        for (int i = 0; i < this.size() - 1; i++) {
            TrackedElement te = this.get(i);

            for (int j = i + 1; j < this.size(); j++) {
                TrackedElement te2 = this.get(j);

                aligned = aligned && pointsColinear(te.getPosition(),
                        te2.getPosition(),
                        third.getPosition());
            }
        }

        return aligned;
    }

    private void computeAnglePCA() {
        double[][] dataPoints = new double[this.size()][2];
        for (int i = 0; i < this.size(); i++) {
            dataPoints[i][0] = this.get(i).getPosition().x;
            dataPoints[i][1] = this.get(i).getPosition().y;
        }
        //  column corresponding to dimension: x, y, z */
        Matrix trainingData = new Matrix(dataPoints);
        PCA pca = new PCA(trainingData);
        Matrix vectors = pca.getEigenvectorsMatrix();

        if (pca.getEigenvectorsMatrix().getColumnDimension() != 2
                || pca.getEigenvectorsMatrix().getRowDimension() != 2) {

//                System.out.println("EigenMatrix too short. ?");
//                for (int i = 0; i < this.size(); i++) {
//                    System.out.println("id: " + i + " " + this.get(i).getPosition());
//                }
            // Short eigen matrix -> Points aligned according to an existing axis. 
            float x = this.get(0).getPosition().x;
            float y = this.get(0).getPosition().y;
            int xCount = 1;
            int yCount = 1;

            for (int i = 1; i < this.size(); i++) {
                float x1 = this.get(i).getPosition().x;
                float y1 = this.get(i).getPosition().y;

                if (x == x1) {
                    xCount++;
                }
                if (y == y1) {
                    yCount++;
                }
            }
            // same X
            if (xCount == this.size()) {
//                angle = 0;
            }
            // same Y
            if (yCount == this.size()) {
//                angle = Math.PI;
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

    void computeAngleVectors() {

        PVector directions = new PVector();
        PVector p0 = this.get(0).getPosition().get();

        for (int i = 1; i < this.size(); i++) {
            PVector p1 = this.get(i).getPosition();

            PVector d = p0.get().sub(p1);
//            directions.add(abs(d.x), abs(d.y));
            directions.add(d.x, d.y);
        }
//         System.out.println("d: " + directions);
        angle = atan2(directions.y, directions.x);
    }

    public double e0, e1;

    public double angle() {
        if (angle != Float.MIN_VALUE) {
            return angle;
        } else {
//            computeAnglePCA();
            computeAngleVectors();
        }
        return angle;
    }

//    @Override
    public boolean equals(Object other) {
//        boolean c1 = ((LineCluster) other).containsAll(this);

        LineCluster secondInit = (LineCluster) other;
//        LineCluster second = new LineCluster((LineCluster) other);

        int common = 0;
        for (TrackedElement t : secondInit) {
            if (this.contains(t)) {
                common++;
            }
        }
//        return secondInit.size() == second.size() ;
//        boolean c2 = this.containsAll((LineCluster) other);
        return common > 1;
    }
//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 83 * hash + Objects.hashCode(this.this);
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
            TrackedElement first = it.next();

            // Remove it since the beginning. We do not want to find it anymore.
//            it.remove();
//        for (TrackedElement first : elements) {
//        TrackedElement first = elements.get(0);
            // Get the closets elements
            ArrayList<TrackedElement> allElements = new ArrayList<TrackedElement>(elements);

            allElements.remove(first);

//            tryAttach(currentCluster, first, elements, size, 1);
            ArrayList<TrackedElement> closeElements = elementsCloseTo(elements, first, size);

            // one element at least. 
            if (closeElements.isEmpty()) {
                continue;
            }

            // we have a pair, or a list of pairs.
            // Get the first
            for (TrackedElement second : closeElements) {

                // Deep search from the pair. 
                LineCluster currentCluster = new LineCluster();
                currentCluster.add(first);
                currentCluster.add(second);

                // Remove the elements, we might add them back if they are part of a "border"
                allElements.remove(first);
                allElements.remove(second);

                // Search in both directions
                tryAttach(currentCluster, second, allElements, size);
                tryAttach(currentCluster, first, allElements, size);

                if (currentCluster.size() > 2) {

                    if (!lines.contains(currentCluster)) {
                        lines.add(currentCluster);
//                        TrackedElement[] borders = currentCluster.getBorders();
//                        allElements
                        // TODO: Add back the borders
                    }
                }
//                System.out.println("Cluster size: " + currentCluster.size());
            }

        }

        return lines;
    }

    /**
     * Try to attach close point recursively.
     *
     * @param currentCluster
     * @param currentPoint
     * @param elements
     * @param dist distance to next element
     */
    public static void tryAttach(LineCluster currentCluster,
            TrackedElement currentPoint,
            ArrayList<TrackedElement> elements,
            float dist) {

        ArrayList<TrackedElement> closeElements = elementsCloseTo(elements, currentPoint, dist);

        // one element at least. 
        if (closeElements.isEmpty()) {
            return;
        }

        for (TrackedElement third : closeElements) {

            boolean colinear = currentCluster.tryAddColinear(third);
            if (colinear) {
                currentCluster.add(third);
                elements.remove(third);

                tryAttach(currentCluster, third, elements, dist);
            }
        }

    }

    public static ArrayList<TrackedElement> elementsCloseTo(ArrayList<TrackedElement> elements,
            TrackedElement first,
            float dist) {
        ArrayList<TrackedElement> closeElements = new ArrayList<TrackedElement>(elements);
        closeElements.remove(first);

        // Deep search from this.
        // search the really close ones (only one necessary).  1 cm max
        Predicate<TrackedElement> closeFilter = te -> te.distanceTo(first) > dist;
        closeElements.removeIf(closeFilter);
        return closeElements;
    }

    public static float epsilon = 20f;

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

    @Override
    public float distanceTo(Trackable newTp) {
        return newTp.getPosition().dist(this.getPosition());
    }

    @Override
    public void setPosition(PVector pos) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PVector getPosition() {
        return this.position();
    }

    /**
     * Not supported yet.
     * @return 
     */
    @Override
    public PVector getPreviousPosition() {
        return this.position();
    }

    /**
     * Not supported yet
     * @return 
     */
    @Override
    public PVector getSpeed() {
        return new PVector();
    }

    /**
     * Not supported yet.
     */
    @Override
    public void filter() {
    }

    /**
     * Not supported yet.
     */
    @Override
    public void filter(int updateTime) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Not supported yet.
     */
    @Override
    public void forceID(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Not supported yet.
     */
    @Override
    public int getID() {
        String stringCode = getStringCode(true);
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    int creationTime, updateTime, deletionTime;
    boolean updated = false, toDelete;
    int shortTime = 200;
    int forgetTime = 400;
    float maxDistance = 40f;
    
    /**
     * Not supported yet.
     */
    @Override
    public void setCreationTime(int timeStamp) {
        this.creationTime = timeStamp;
    }

    @Override
    public int getAge(int currentTime) {
        return currentTime - creationTime;
    }

    @Override
    public boolean isYoung(int currentTime) {
       return getAge(currentTime) > 200;
    }

    @Override
    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    @Override
    public boolean isUpdated() {
        return this.updated;
    }

    @Override
    public int lastUpdate() {
        return updateTime;
    }

    @Override
    public boolean updateWith(Trackable tp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAlone() {
    }

    @Override
    public boolean isObselete(int currentTime) {
        return (currentTime - updateTime) > forgetTime;
    }

    @Override
    public boolean isToRemove(int currentTime, int duration) {
          return (currentTime - deletionTime) > duration;
   }

    @Override
    public boolean isToDelete() {
        return toDelete;
    }

    @Override
    public void delete(int time) {
        toDelete = true;
        deletionTime = time;
    }

    @Override
    public float getTrackingMaxDistance() {
        return maxDistance;
    }

}
