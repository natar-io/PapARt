/*
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.Kinect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class TouchDetectionSimple3D extends TouchDetection {

    protected float MINIMUM_COMPONENT_SIZE_3D = 50;
    protected int COMPONENT_SIZE_FOR_POSITION = 10;

    public TouchDetectionSimple3D(int size) {
        super(size);
        MINIMUM_COMPONENT_SIZE = MINIMUM_COMPONENT_SIZE_3D;
    }

    @Override
    public ArrayList<TouchPoint> compute(DepthData dData, int skip) {
        this.depthData = dData;
        this.precision = skip;

        if (!hasCCToFind()) {
            return new ArrayList<TouchPoint>();
        }

        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents();
        ArrayList<TouchPoint> touchPoints = this.createTouchPointsFrom(connectedComponents);

        return touchPoints;
    }
    
    @Override
    public boolean hasCCToFind(){
             return !depthData.validPointsList3D.isEmpty();
    }

    @Override
    protected void setSearchParameters() {
        this.toVisit = new HashSet<Integer>();
        this.toVisit.addAll(depthData.validPointsList3D);

        currentPointValidityCondition = new CheckTouchPoint3D();

        int firstPoint = toVisit.iterator().next();
        setPrecisionFrom(firstPoint);
        searchDepth = precision * 7;// TODO: FIX this value !
        MAX_REC = 1000; // TODO: fix this value.
    }

    @Override
    protected TouchPoint createTouchPoint(ConnectedComponent connectedComponent) {
        ClosestComparatorY closestComparator = new ClosestComparatorY(depthData.projectedPoints);

        // get a subset of the points.
        Collections.sort(connectedComponent, closestComparator);

        //  Get a sublist
        List<Integer> subList = connectedComponent.subList(0, COMPONENT_SIZE_FOR_POSITION);
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
            float distanceToCurrent = depthData.kinectPoints[offset].distanceTo(depthData.kinectPoints[currentPoint]);

            return !assignedPoints[offset] // not assigned  
                    && depthData.validPointsMask3D[offset] // is valid
                    && (depthData.kinectPoints[offset] != Kinect.INVALID_POINT) // not invalid point (invalid depth)
                    && distanceToCurrent < maxDistance;
        }
    }

}
