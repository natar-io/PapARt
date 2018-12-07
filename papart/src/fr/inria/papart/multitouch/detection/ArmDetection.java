/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
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
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.calibration.PlanarTouchCalibration;
import tech.lity.rea.nectar.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.DepthData.DepthSelection;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.analysis.Compute3D;
import fr.inria.papart.depthcam.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.utils.WithSize;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class ArmDetection extends TouchDetectionDepth {

    protected int MINIMUM_COMPONENT_SIZE_3D = 50;
    protected int COMPONENT_SIZE_FOR_POSITION = 400;
    private final Compute3D touchRecognition;

    public ArmDetection(DepthAnalysisImpl depthAnalysisImpl, PlanarTouchCalibration calib) {
        super(depthAnalysisImpl, calib);
        currentPointValidityCondition = new CheckTouchPoint3D();
        touchRecognition = new Compute3D(depthAnalysisImpl);
    }

    public DepthSelection getDepthSelection() {
        return touchRecognition.getSelection();
    }

    public class CheckTouchPoint3D implements PointValidityCondition {

        Plane plane;

        public void updatePlane(Plane p) {
            plane = p;
        }

        @Override
        public boolean checkPoint(int candidate, int currentPoint) {

            return !assignedPoints[candidate] // not assigned  
                    && touchRecognition.getSelection().validPointsMask[candidate] // is valid, necessary ?
                    && (depthData.depthPoints[candidate].distanceTo(DepthAnalysis.INVALID_POINT) > 1) // NON zero points
                    && (depthData.depthPoints[candidate] != DepthAnalysis.INVALID_POINT) // not invalid point (invalid depth)

                    && depthData.depthPoints[initialPoint].distanceTo(depthData.depthPoints[candidate]) < calib.getMaximumDistanceInit()
                    && depthData.depthPoints[candidate].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistance()
                    && depthData.projectedPoints[candidate].z() > calib.getTest2();
//                    && plane.getDistanceToPoint(depthData.depthPoints[candidate]) > calib.getTest2();

        }
        private int initialPoint;

        @Override
        public void setInitialPoint(int offset) {
            this.initialPoint = offset;
        }
    }

    @Override
    public ArrayList<TrackedDepthPoint> compute(ProjectedDepthData dData) {
        this.setDepthData(dData);

        if (!hasCCToFind()) {
            return new ArrayList<TrackedDepthPoint>();
        }

        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents();
        ArrayList<TrackedDepthPoint> touchPoints = this.createTouchPointsFrom(connectedComponents);
        newTipPoints = this.createTipPointsFrom(connectedComponents);

        return touchPoints;
    }

    @Override
    public boolean hasCCToFind() {
        return !touchRecognition.getSelection().validPointsList.isEmpty();
    }

    @Override
    protected void setSearchParameters() {
        this.toVisit.clear();
        this.toVisit.addAll(touchRecognition.getSelection().validPointsList);

//        int firstPoint = toVisit.iterator().next();
//        setPrecisionFrom(firstPoint);
//        searchDepth = precision * 7;// TODO: FIX this value !
//        maximumRecursion = 1000; // TODO: fix this value.
    }

    //    protected abstract void setSearchParameters();
    protected ArrayList<TrackedDepthPoint> createTipPointsFrom(ArrayList<ConnectedComponent> connectedComponents) {
        ArrayList<TrackedDepthPoint> newPoints = new ArrayList<TrackedDepthPoint>();
        for (ConnectedComponent connectedComponent : connectedComponents) {

            float height = connectedComponent.getHeight(depthData.projectedPoints);
            if (connectedComponent.size() < calib.getMinimumComponentSize()
                    || height < calib.getMinimumHeight()) {

                continue;
            }
            TrackedDepthPoint tp = createTipPoint(connectedComponent);
            newPoints.add(tp);
        }
        return newPoints;
    }

    protected TrackedDepthPoint createTipPoint(ConnectedComponent connectedComponent) {
        ClosestComparatorY closestComparator = new ClosestComparatorY(depthData.projectedPoints);
//        ClosestComparatorHeight closestComparator = new ClosestComparatorHeight(depthData.projectedPoints, depthData.planeAndProjectionCalibration.getPlaneCalibration());
        // get a subset of the points.
        Collections.sort(connectedComponent, closestComparator);

        // First remove the X closest points (fingers) 
//        int max = (int) calib.getTest5() > connectedComponent.size() ? connectedComponent.size() : (int) calib.getTest5();
//        //  Get a sublist
        int size = 3;
        if (connectedComponent.size() < size) {
            size = connectedComponent.size();
        }
        List<Integer> subList = connectedComponent.subList(0, size);
        ConnectedComponent subCompo = new ConnectedComponent();
        subCompo.addAll(subList);
//        int maxYOffset = subCompo.get(0);
//        Vec3D maxY = depthData.depthPoints[maxYOffset];
        // Sublist with distance filter instead of number filter
        // Remove from a distance
        TrackedDepthPoint tp = super.createTouchPoint(subCompo);
//        TrackedDepthPoint tp = super.createTouchPoint(connectedComponent);

        // TODO: use this, add another with only the ones of the touch ?!
//        tp.setDepthDataElements(depthData, connectedComponent);
        tp.set3D(true);
        return tp;
    }

    @Override
    protected TrackedDepthPoint createTouchPoint(ConnectedComponent connectedComponent) {
        TrackedDepthPoint tp = super.createTouchPoint(connectedComponent);
        tp.set3D(true);
        return tp;
    }

    public void findTouch(PlaneAndProjectionCalibration planeAndProjCalibration) {

//        System.out.println("ARM precision: " + getPrecision());
//        Instant start = Instant.now();
        ((CheckTouchPoint3D) currentPointValidityCondition).updatePlane(planeAndProjCalibration.getPlane());
        touchRecognition.find3DTouch(planeAndProjCalibration, getPrecision());

//        Instant find3D = Instant.now();
        ArrayList<TrackedDepthPoint> newList = this.compute(depthAnalysis.getDepthData());

//        Instant findList = Instant.now();
//        System.out.println("3D:  " + Duration.between(start, find3D).toMillis() + " milliseconds");
//        System.out.println("list: " + Duration.between(find3D, findList).toMillis() + " milliseconds");
//        // Filter low points ?
        Iterator<TrackedDepthPoint> iterator = newList.iterator();
        while (iterator.hasNext()) {
            TrackedDepthPoint next = iterator.next();
            if (next.getPosition().z < calib.getTest1()) {
                iterator.remove();
            }
        }

        int imageTime = this.depthAnalysis.getDepthData().timeStamp;
//        System.out.println("Tracking: " + imageTime);
//        for(TrackedDepthPoint pt: newList){
//            System.out.println("pt: " + pt.getPosition());
//        }
//        TouchPointTracker.trackPoints(touchPoints, newList, imageTime);

        touchPoints.clear();
        touchPoints.addAll(newList);
        // TODO: activate tracking ?! Super slow for some reason...
//        TouchPointTracker.trackPoints(tipPoints, newTipPoints, imageTime);
        tipPoints.clear();
        tipPoints.addAll(newTipPoints);

        lastTipPoints.clear();
        lastTipPoints.addAll(tipPoints);
    }

    protected ArrayList<TrackedDepthPoint> newTipPoints = new ArrayList<>();
    protected final ArrayList<TrackedDepthPoint> tipPoints = new ArrayList<>();
    ArrayList<TrackedDepthPoint> lastTipPoints = new ArrayList<>();

    public ArrayList<TrackedDepthPoint> getTipPoints() {
        return lastTipPoints;
    }

}
