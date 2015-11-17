/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.DepthAnalysis;
import fr.inria.papart.depthcam.devices.KinectDepthData;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class TouchDetectionSimple2D extends TouchDetection {

    public TouchDetectionSimple2D(int size) {
        super(size);
        currentPointValidityCondition = new CheckTouchPoint();
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
    protected void setSearchParameters() {
        this.toVisit.clear();
        this.toVisit.addAll(depthData.validPointsList);
        
//        int firstPoint = toVisit.iterator().next();
//        maxDistance = 10;
////         setPrecisionFrom(firstPoint);
//        searchDepth = 40;// TODO: FIX this value !
//        maximumRecursion = 100; // TODO: fix this value.
    }

 
}
