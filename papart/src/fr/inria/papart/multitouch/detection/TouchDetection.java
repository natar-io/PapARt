/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.calibration.PlanarTouchCalibration;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.TrackedElement;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole - laviole@rea.lity.tech
 */
public abstract class TouchDetection {

    // Variable parameters... 
    protected PlanarTouchCalibration calib;

    protected boolean[] assignedPoints = null;
    protected byte[] connectedComponentImage = null;

    protected final byte NO_CONNECTED_COMPONENT = 0;
    protected final byte STARTING_CONNECTED_COMPONENT = 1;

    protected byte currentCompo = STARTING_CONNECTED_COMPONENT;

// set by calling function
    protected WithSize imgSize;

    protected final HashSet<Integer> toVisit = new HashSet<>();
    protected PointValidityCondition currentPointValidityCondition;
    protected int currentTime;

    protected void setCurrentTime(int timestamp) {
        this.currentTime = timestamp;
    }

    /**
     * Used to check if a nearby point can be in the same connected component or
     * not.
     */
    public interface PointValidityCondition {

        /**
         *
         * @param offset candidate point to join connected component
         * @param currentPoint Current point in the valid connected component
         * @return true if the offset point can join.
         */
        public boolean checkPoint(int offset, int currentPoint);
    }

    public TouchDetection(WithSize imgSize) {
        allocateMemory(imgSize);
    }

    protected void allocateMemory(WithSize imgSize) {
        this.imgSize = imgSize;
        assignedPoints = new boolean[imgSize.getSize()];
        connectedComponentImage = new byte[imgSize.getSize()];
        this.calib = new PlanarTouchCalibration();
    }

    protected void clearMemory() {
        Arrays.fill(assignedPoints, false);
        Arrays.fill(connectedComponentImage, NO_CONNECTED_COMPONENT);
        currentCompo = STARTING_CONNECTED_COMPONENT;
    }

    protected ArrayList<ConnectedComponent> findConnectedComponents() {
        clearMemory();
        setSearchParameters();
        ArrayList<ConnectedComponent> connectedComponents = computeAllConnectedComponents();
        return connectedComponents;
    }

    protected abstract void setSearchParameters();

//    public abstract ArrayList<TrackedDepthPoint> compute(KinectDepthData dData);
//    protected abstract ArrayList<TrackedDepthPoint> createTouchPointsFrom(ArrayList<ConnectedComponent> connectedComponents);
    protected ArrayList<ConnectedComponent> computeAllConnectedComponents() {

        ArrayList<ConnectedComponent> connectedComponents = new ArrayList<ConnectedComponent>();

        // recursive search for each component. 
        while (toVisit.size() > 0) {
            int startingPoint = toVisit.iterator().next();
            ConnectedComponent cc = findConnectedComponent(startingPoint);
            connectedComponents.add(cc);
        }
        return connectedComponents;
    }

    // TODO: optimisations here ?
    int w, h;
    protected int searchDepth;
    protected int precision;

    // TODO: chec if currentCompo ++ is relevent. 
    protected ConnectedComponent findConnectedComponent(int startingPoint) {

        // searchDepth is by precision steps. 
        searchDepth = calib.getSearchDepth() * calib.getPrecision();
        precision = calib.getPrecision();

        w = imgSize.getWidth();
        h = imgSize.getHeight();
        ConnectedComponent cc = findNeighboursRec(startingPoint, 0, getX(startingPoint), getY(startingPoint));
        cc.setId(currentCompo);
        currentCompo++;
        return cc;
    }

    private void addPointInConnectedComponent(ConnectedComponent cc, int point) {
        assignedPoints[point] = true;
        connectedComponentImage[point] = currentCompo;
        toVisit.remove(point);
        cc.add(point);
    }

    public int getX(int currentPoint) {
        return currentPoint % w;
    }

    public int getY(int currentPoint) {
        return currentPoint / w;
    }

    public ConnectedComponent findNeighboursRec(int currentPoint, int recLevel, int x, int y) {

        ConnectedComponent neighbourList = new ConnectedComponent();

        // At least one point in connected compo !
        if (recLevel == 0) {
            addPointInConnectedComponent(neighbourList, currentPoint);
        }

        if (recLevel == calib.getMaximumRecursion()) {
            addPointInConnectedComponent(neighbourList, currentPoint);
            return neighbourList;
        }

        // do nothing on borders -> dead zone optimization ?!
        if (x - searchDepth < 0 || x + searchDepth > w - 1
                || y - searchDepth < 0 || y + searchDepth > h - 1) {
            return neighbourList;
        }

        assert (assignedPoints[currentPoint] == true);

        // Usual...
//        int minX = PApplet.constrain(x - searchDepth, 0, w - 1);
//        int maxX = PApplet.constrain(x + searchDepth, 0, w - 1);
//        int minY = PApplet.constrain(y - searchDepth, 0, h - 1);
//        int maxY = PApplet.constrain(y + searchDepth, 0, h - 1);
        int minX = x - searchDepth;
        int maxX = x + searchDepth;
        int minY = y - searchDepth;
        int maxY = y + searchDepth;
        for (int j = minY; j <= maxY; j += precision) {
            boolean isBorderY = (j == y - searchDepth) || (j == y + searchDepth);

            for (int i = minX; i <= maxX; i += precision) {
                boolean isBorderX = (i == x - searchDepth) || (i == x + searchDepth);

                int offset = j * w + i;

                // Avoid getting ouside the limits
                if (currentPointValidityCondition.checkPoint(offset, currentPoint)) {

//                    assignedPoints[offset] = true;
//                    connectedComponentImage[offset] = currentCompo;
                    // Remove If present -> it might not be the case often. 
//                    toVisit.remove(offset);
                    addPointInConnectedComponent(neighbourList, offset);
                    neighbourList.add((Integer) offset);

                    if (isBorderY || isBorderX) {
                        ConnectedComponent subNeighbours = findNeighboursRec(offset, recLevel + 1, i, j);
                        neighbourList.addAll(subNeighbours);
                    }
//                    ConnectedComponent subNeighbours = findNeighboursRec(offset, recLevel + 1, i, j);
//                    neighbourList.addAll(subNeighbours);

                } // if is ValidPoint
            } // for j
        } // for i

        return neighbourList;
    }

    static public final int constrain(int amt, int low, int high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    abstract protected <T extends TrackedElement> T createTouchPoint(ConnectedComponent connectedComponent);

    public float ERROR_DISTANCE_MULTIPLIER = 1.3f;
    public float NOISE_ESTIMATION = 1.5f; // in millimeter. 

    @Deprecated
    protected void setDistance(float distance) {
        calib.setMaximumDistance((distance + NOISE_ESTIMATION) * ERROR_DISTANCE_MULTIPLIER);
    }

    public float sideError = 0.2f;

    public boolean isInside(Vec3D v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    public boolean isInside(PVector v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    public void setCalibration(PlanarTouchCalibration calibration) {
//        this.calib.setTo(calibration);
        this.calib = calibration;
    }

    public PlanarTouchCalibration getCalibration() {
        return this.calib;
    }

    public int getPrecision() {
        return calib.getPrecision();
    }

    public int getTrackingForgetTime() {
        return calib.getTrackingForgetTime();
    }

    public float getTrackingMaxDistance() {
        return calib.getTrackingMaxDistance();
    }

    
}
