/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
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

import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class Simple3D extends TouchDetectionDepth {

    protected int MINIMUM_COMPONENT_SIZE_3D = 50;
    protected int COMPONENT_SIZE_FOR_POSITION = 400;

    public Simple3D(WithSize size) {
        super(size);
        currentPointValidityCondition = new CheckTouchPoint3D();
    }

    @Override
    public ArrayList<TrackedDepthPoint> compute(ProjectedDepthData dData) {
        this.setDepthData(dData);

        if (!hasCCToFind()) {
            return new ArrayList<TrackedDepthPoint>();
        }

        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents();
        ArrayList<TrackedDepthPoint> touchPoints = this.createTouchPointsFrom(connectedComponents);

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
    protected TrackedDepthPoint createTouchPoint(ConnectedComponent connectedComponent) {
        
//        ClosestComparatorY closestComparator = new ClosestComparatorY(depthData.projectedPoints);
        ClosestComparatorHeight closestComparator = new ClosestComparatorHeight(depthData.projectedPoints, depthData.planeAndProjectionCalibration.getPlaneCalibration());

        // get a subset of the points.
        Collections.sort(connectedComponent, closestComparator);

        int max = COMPONENT_SIZE_FOR_POSITION > connectedComponent.size() ? connectedComponent.size() : COMPONENT_SIZE_FOR_POSITION;
        //  Get a sublist
        List<Integer> subList = connectedComponent.subList(0, max);
        ConnectedComponent subCompo = new ConnectedComponent();
        subCompo.addAll(subList);

        TrackedDepthPoint tp = super.createTouchPoint(subCompo);

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