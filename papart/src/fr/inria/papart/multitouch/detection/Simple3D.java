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

import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.analysis.Touch3D;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class Simple3D extends TouchDetectionDepth {

    protected int MINIMUM_COMPONENT_SIZE_3D = 50;
    protected int COMPONENT_SIZE_FOR_POSITION = 400;
    private final Touch3D touchRecognition;

    public Simple3D(DepthAnalysisImpl depthAnalysisImpl) {
        super(depthAnalysisImpl);
        currentPointValidityCondition = new CheckTouchPoint3D();
        touchRecognition = new Touch3D(depthAnalysisImpl);
    }
    
      public class CheckTouchPoint3D implements PointValidityCondition {

        // Not used yet here.
        private int inititalPoint;

        public void setInitalPoint(int offset) {
            this.inititalPoint = offset;
        }

        @Override
        public boolean checkPoint(int offset, int currentPoint) {
            float distanceToCurrent = depthData.depthPoints[offset].distanceTo(depthData.depthPoints[currentPoint]);

            return !assignedPoints[offset] // not assigned  
                    && touchRecognition.getSelection().validPointsMask[offset] // is valid, necessary ?
                    && (depthData.depthPoints[offset] != DepthAnalysis.INVALID_POINT) // not invalid point (invalid depth)
                    && distanceToCurrent < calib.getMaximumDistance();
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

    public void findTouch(PlaneAndProjectionCalibration planeAndProjCalibration) {

        touchRecognition.find3DTouch(planeAndProjCalibration, getPrecision());
        ArrayList<TrackedDepthPoint> newList = this.compute(depthAnalysis.getDepthData());

        int imageTime = this.depthAnalysis.getDepthData().timeStamp;
        TouchPointTracker.trackPoints(touchPoints, newList, imageTime);
    }

  
}
