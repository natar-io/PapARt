/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import Jama.Matrix;
import com.mkobos.pca_transform.PCA;
import fr.inria.papart.multitouch.OneEuroFilter;
import fr.inria.papart.multitouch.tracking.Trackable;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.multitouch.tracking.TrackedPosition;
import static fr.inria.papart.multitouch.tracking.TrackedPosition.NO_ID;
import static fr.inria.papart.multitouch.tracking.TrackedPosition.NO_TIME;
import static fr.inria.papart.multitouch.tracking.TrackedPosition.filterBeta;
import static fr.inria.papart.multitouch.tracking.TrackedPosition.filterCut;
import static fr.inria.papart.multitouch.tracking.TrackedPosition.filterFreq;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.atan2;
import processing.core.PMatrix2D;
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

    public static final LineCluster INVALID_CLUSTER = new LineCluster();

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
        LineCluster copy = new LineCluster(this);
        PVector border = this.getBorders()[0].getPosition();
        Collections.sort(copy, new Comparator<TrackedElement>() {
            @Override
            public int compare(TrackedElement t1, TrackedElement t2) {
                return Float.compare(t2.getPosition().dist(border), t1.getPosition().dist(border));
            }
        });

        return getCodeFromSorted(copy, numeric);
    }

    private String getCodeFromSorted(LineCluster line, boolean numeric) {
        StringBuilder code = new StringBuilder();
        for (TrackedElement te : line) {
            if (numeric) {
                code.append(Integer.toString(te.attachedValue));
            } else {
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

    public boolean isCode(String inputCode) {
        StringBuilder sb = new StringBuilder(this.getStringCode(isNumeric(inputCode)));
        String c = sb.toString();
        String inverse = sb.reverse().toString();

        return inputCode.equals(c) || inputCode.equals(inverse);
    }

    private boolean isNumeric(String inputCode) {
        return inputCode.contains("0")
                || inputCode.contains("1")
                || inputCode.contains("2")
                || inputCode.contains("3")
                || inputCode.contains("4")
                || inputCode.contains("5");
    }

    public LineCluster asCode(String lineCode) {

        LineCluster out = new LineCluster(this);

        PVector border = this.getBorders()[0].getPosition();
        Collections.sort(out, new Comparator<TrackedElement>() {
            @Override
            public int compare(TrackedElement t1, TrackedElement t2) {
                return Float.compare(t2.getPosition().dist(border), t1.getPosition().dist(border));
            }
        });

        String c1 = getCodeFromSorted(out, isNumeric(lineCode));

        if (lineCode.equals(c1)) {
            return out;
        }
        Collections.reverse(out);
        String c2 = getCodeFromSorted(out, isNumeric(lineCode));
        if (lineCode.equals(c2)) {
            return out;
        }

        System.err.println("LineCluster: asCode, code mismatch.");
        return new LineCluster();
    }

    /**
     * 
     * Get the matching line, sorted is the correct order.
     * 
     * @param lineCode  Code, like "BBG" or "001"
     * @param lines  List of lines
     * @return the line, or LineCluster.INVALID_CLUSTER.
     */
    public static LineCluster findMatchingLine(String lineCode, ArrayList<LineCluster> lines) {
        for (LineCluster line : lines) {
            if (line.isCode(lineCode)) {
                return line.asCode(lineCode);
            }
        }
        return LineCluster.INVALID_CLUSTER;
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

    public double computeAngleVectors() {

        PVector directions = new PVector();
        PVector p0 = this.get(0).getPosition().get();

        for (int i = 1; i < this.size(); i++) {
            PVector p1 = this.get(i).getPosition();

            PVector d = p0.get().sub(p1);
//            directions.add(abs(d.x), abs(d.y));
            directions.add(d.x, d.y);
        }
//         System.out.println("d: " + directions);
        return atan2(directions.y, directions.x);
    }

    public double computeAngleBorders() {
        TrackedElement[] borders = getBorders();
        PVector first = borders[0].getPosition().copy();
        PVector last = borders[1].getPosition().copy();
        last.sub(first);
        double r = Math.atan2(last.y, last.x);
        return r;
    }

    public double computeAngleLimits() {
        TrackedElement[] borders = getBorders();
        PVector first = this.get(0).getPosition().copy();
        PVector last = this.get(this.size() - 1).getPosition().copy();
        last.sub(first);
        double r = Math.atan2(last.y, last.x);
        return r;
    }

    public double e0, e1;

    public double angle() {
//        angle = computeAngleVectors();
        angle = computeAngleBorders();
        return angle;
    }

//    public double angle() {
//        if (angle != Float.MIN_VALUE) {
//            return angle;
//        } else {
////            computeAnglePCA();
//            computeAngleVectors();
//        }
//        return angle;
//    }
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
    
    public static final PVector INVALID_VECTOR = new PVector();
    
    public static PVector findRotationTranslationFrom(ArrayList<LineCluster> lines, ArrayList<String> lineCode, ArrayList<PVector> linePositions) {
        if (lines.isEmpty()) {
//            return new PMatrix3D();
            return INVALID_VECTOR;
        }

        ArrayList<LineCluster> sortedLines = new ArrayList<>();

//  Get all the lines, sorted code        
        lineCode.forEach((code) -> sortedLines.add(findMatchingLine(code, lines)));

        PVector averagePosition = new PVector();
        double averageRotation = 0;
        int nbValidPositions = 0;

//        PVector[] imagePoints = new PVector[sortedLines.size()];
//        PVector[] objectPoints = new PVector[sortedLines.size()];
//        float[] rotations = new float[sortedLines.size()];
        int nbPI = 0;

        for (int i = 0; i < sortedLines.size(); i++) {
            if (sortedLines.get(i) == LineCluster.INVALID_CLUSTER) {
                continue;
            }
            LineCluster line = sortedLines.get(i);
//            PVector p = line.getPosition().copy();
//            p.sub(positionOffset);
            double r = line.computeAngleLimits();
//            rotations[nbValidPositions] = (float) r;

//            imagePoints[nbValidPositions] = p;
//            objectPoints[nbValidPositions] = positionOffset;
            if (Math.abs(r) > Math.PI * 2 - 0.8 || r < 0.8) {
                r = r + Math.PI;
                nbPI++;
            }
            averageRotation += r;

            // Warning, rotations close to PI and -PI could be the same
            nbValidPositions++;
        }

        averageRotation = averageRotation - Math.PI * nbPI;
        averageRotation = averageRotation / nbValidPositions;

        if (nbValidPositions == 0) {
            return INVALID_VECTOR;
        }

        for (int i = 0; i < sortedLines.size(); i++) {
            if (sortedLines.get(i) == LineCluster.INVALID_CLUSTER) {
                continue;
            }
            LineCluster line = sortedLines.get(i);

            PMatrix2D transfo = new PMatrix2D();
            PVector positionOffset = linePositions.get(i);

//            PVector p = line.getPosition().copy();
            PVector p = line.get(0).getPosition().copy();
            transfo.translate(p.x, p.y);
            transfo.rotate((float) averageRotation);
            transfo.translate(-positionOffset.x, -positionOffset.y);

            PVector pos = new PVector(transfo.m02, transfo.m12);
            averagePosition.add(pos);
//            System.out.println("pos: " + transfo.m02 + " " + transfo.m12);
        }

//        HomographyCreator hc = new HomographyCreator(2, 2, nbValidPositions);
//        for (int i = 0; i < nbValidPositions; i++) {
//            hc.addPoint(imagePoints[i], objectPoints[i]);
//        }
//        if(hc.isComputed()){
//            HomographyCalibration homography = hc.getHomography();
//
//            // Matrix!
//            return homography.getHomography();
//        }
//        return new PMatrix3D();
//        if (nbValidPositions == 0) {
//            return new PVector();
//        }
        averagePosition.mult(1f / nbValidPositions);
        averagePosition.z = (float) averageRotation;
        return averagePosition;
    }
    

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
        if (filters == null) {
            return this.position();
        } else {
            return this.filteredPosition;
        }
    }

    /**
     * Not supported yet.
     *
     * @return
     */
    @Override
    public PVector getPreviousPosition() {
        return this.position();
    }

    /**
     * Not supported yet
     *
     * @return
     */
    @Override
    public PVector getSpeed() {
        return new PVector();
    }

    private PVector filteredPosition = null;
    private OneEuroFilter[] filters;
    public static float filterFreq = 30f;
    public static float filterCut = 0.02f;
    public static float filterBeta = 0.2000f;

    protected int NUMBER_OF_FILTERS = 3;

    /**
     * Not supported yet.
     */
    @Override
    public void filter() {
        if (filters == null) {
            initFilters();
        }
        PVector p = this.position();
        try {
            filteredPosition.x = (float) filters[0].filter(p.x);
            filteredPosition.y = (float) filters[1].filter(p.y);
            filteredPosition.z = (float) filters[2].filter(p.z);
        } catch (Exception e) {
            System.out.println("OneEuro init Exception. Pay now." + e);
        }
    }

    /**
     * @param updateTime
     */
    @Override
    public void filter(int updateTime) {
        if (filters == null) {
            initFilters();
        }
        PVector p = this.position();

        try {
            filteredPosition.x = (float) filters[0].filter(p.x, updateTime);
            filteredPosition.y = (float) filters[1].filter(p.y, updateTime);
            filteredPosition.z = (float) filters[2].filter(p.z, updateTime);
        } catch (Exception e) {
            System.out.println("OneEuro init Exception. Pay now." + e);
        }
    }

    private void initFilters() {
        try {
            filters = new OneEuroFilter[NUMBER_OF_FILTERS];
            for (int i = 0; i < NUMBER_OF_FILTERS; i++) {
                filters[i] = new OneEuroFilter(filterFreq, filterCut, filterBeta, 0.5f);
            }
            filteredPosition = new PVector();
        } catch (Exception e) {
            System.out.println("OneEuro Exception. Pay now." + e);
        }
    }

    /**
     * Not supported yet.
     */
    @Override
    public void forceID(int id) {
        this.id = id;
    }

    /**
     * Not supported yet.
     */
    @Override
    public int getID() {
        return this.id;
    }

    protected int updateTime;
    protected int deletionTime;
    protected int createTime = NO_TIME;
    boolean isUpdated = false, toDelete = false;
    int shortTime = 200;
    int forgetTime = 400;
    float maxDistance = 60f;

    /**
     * Not supported yet.
     *
     * @param timeStamp
     */
    @Override
    public void setCreationTime(int timeStamp) {
        this.createTime = timeStamp;
        this.updateTime = timeStamp;
    }

    @Override
    public int getAge(int currentTime) {
        if (this.createTime == NO_TIME) {
            return NO_TIME;
        } else {
            return currentTime - this.createTime;
        }
    }

    @Override
    public boolean isYoung(int currentTime) {
        return getAge(currentTime) > 200;
    }

    @Override
    public void setUpdated(boolean updated) {
        this.isUpdated = updated;
    }

    @Override
    public boolean isUpdated() {
        return this.isUpdated;
    }

    @Override
    public int lastUpdate() {
        return updateTime;
    }

    @Override
    public boolean updateWith(Trackable tp) {
        return updateWith((LineCluster) tp);
    }

    // TODO: match previous previous points with current ones, 
    // If most of them are the same, it is the same cluster.
    public static boolean hasSameCode(LineCluster l1, LineCluster l2) {
        String c1 = l1.getStringCode(true);
        StringBuilder invB = new StringBuilder(c1);
        String c1inv = invB.reverse().toString();

        String c2 = l2.getStringCode(true);
        StringBuilder inv2B = new StringBuilder(c2);
        String c2inv = inv2B.reverse().toString();

        return c1.equals(c2)
                || c1.equals(c2inv)
                || c1inv.equals(c2)
                || c1inv.equals(c2inv);
    }

    public boolean updateWith(LineCluster lc) {

        if (isUpdated || lc.isUpdated) {
            return false;
        }
        if (this.createTime == lc.createTime) {
            return false;
        }

//        assert (this.createTime <= lc.createTime);
        // TODO: 
        // Check if the IDs are comparable.
        if (!hasSameCode(this, lc)) {
            return false;
        }

        // these points are used for update. They will not be used again.
        this.setUpdated(true);
        lc.setUpdated(true);

        // mark the last update as the creation of the other point. 
        this.updateTime = lc.createTime;
        // not deleted soon, TODO: -> need better way
        this.deletionTime = lc.createTime;

        // delete the updating point (keep the existing one)
        lc.toDelete = true;

        checkAndSetID();

        updateWithLineContents(lc);
        return true;
    }

    private void updateWithLineContents(LineCluster lc) {

        // Easy overkill update where all points are replaced.
        this.clear();
        this.addAll(lc);
    }

    private int id = NO_ID;
    private static int count = 0;
    private static int globalID = 1;

    protected void checkAndSetID() {
        // The touchPoint gets an ID, it is a grown up now. 
        if (this.id == NO_ID) {
            if (count == 0) {
                globalID = 1;
            }
            this.id = globalID++;
            count++;
        }
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
