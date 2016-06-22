/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.devices.KinectDepthData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class TouchDetectionSimple3D extends TouchDetection {

    protected int MINIMUM_COMPONENT_SIZE_3D = 50;
    protected int COMPONENT_SIZE_FOR_POSITION = 10;

    public TouchDetectionSimple3D(int size) {
        super(size);
        currentPointValidityCondition = new CheckTouchPoint3D();
    }

    @Override
    public ArrayList<TouchPoint> compute(KinectDepthData dData) {
        this.depthData = dData;

        if (!hasCCToFind()) {
            return new ArrayList<TouchPoint>();
        }

        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents();
        ArrayList<TouchPoint> touchPoints = this.createTouchPointsFrom(connectedComponents);

        return touchPoints;
    }

    @Override
    public boolean hasCCToFind() {
        return !depthData.validPointsList3D.isEmpty();
    }

    @Override
    protected void setSearchParameters() {
        this.toVisit.clear();
        this.toVisit.addAll(depthData.validPointsList3D);

//        int firstPoint = toVisit.iterator().next();
//        setPrecisionFrom(firstPoint);
//        searchDepth = precision * 7;// TODO: FIX this value !
//        maximumRecursion = 1000; // TODO: fix this value.
    }

    @Override
    protected TouchPoint createTouchPoint(ConnectedComponent connectedComponent) {
        
//        ClosestComparatorY closestComparator = new ClosestComparatorY(depthData.projectedPoints);
        ClosestComparatorHeight closestComparator = new ClosestComparatorHeight(depthData.projectedPoints, depthData.planeAndProjectionCalibration.getPlaneCalibration());

        // get a subset of the points.
        Collections.sort(connectedComponent, closestComparator);

        int max = COMPONENT_SIZE_FOR_POSITION > connectedComponent.size() ? connectedComponent.size() : COMPONENT_SIZE_FOR_POSITION;
        //  Get a sublist
        List<Integer> subList = connectedComponent.subList(0, max);
        ConnectedComponent subCompo = new ConnectedComponent();
        subCompo.addAll(subList);

        TouchPoint tp = super.createTouchPoint(subCompo);

        // TODO: use this, add another with only the ones of the touch ?!
        tp.setDepthDataElements(depthData, connectedComponent);
        tp.set3D(true);
        return tp;
    }
    
         public class CheckTouchPoint3D implements PointValidityCondition {

        @Override
        public boolean checkPoint(int offset, int currentPoint) {
            float distanceToCurrent = depthData.depthPoints[offset].distanceTo(depthData.depthPoints[currentPoint]);

            return !assignedPoints[offset] // not assigned  
                    && depthData.validPointsMask3D[offset] // is valid
                    && (depthData.depthPoints[offset] != DepthAnalysis.INVALID_POINT) // not invalid point (invalid depth)
                    && distanceToCurrent < calib.getMaximumDistance();
        }
    }


}