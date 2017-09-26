/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 20017 RealityTech
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
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.analysis.Touch2D;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class Simple2D extends TouchDetectionDepth {

    private final Touch2D touchRecognition;

    public Simple2D(DepthAnalysisImpl depthAnalysisImpl) {
        super(depthAnalysisImpl);
        touchRecognition = new Touch2D(depthAnalysisImpl);
        currentPointValidityCondition = new CheckTouchPoint();
    }

    public class CheckTouchPoint implements PointValidityCondition {

        private int inititalPoint;

        public void setInitalPoint(int offset) {
            this.inititalPoint = offset;
        }

        public ProjectedDepthData getData() {
            return depthData;
        }

        @Override
        public boolean checkPoint(int offset, int currentPoint) {
//            float distanceToCurrent = depthData.depthPoints[offset].distanceTo(depthData.depthPoints[currentPoint]);

            float dN = (depthData.planeAndProjectionCalibration.getPlane().normal).distanceToSquared(depthData.normals[currentPoint]);
            float d1 = (depthData.planeAndProjectionCalibration.getPlane().getDistanceToPoint(depthData.depthPoints[currentPoint]));
//            System.out.println("d1: " + d1 + " dN: " + dN);
//TODO: Magic numbers !!
            boolean goodNormal = (depthData.normals[offset] != null && dN > 3f) || (d1 > 8f);  // Higher  than Xmm
            return !assignedPoints[offset] // not assigned   
                    //                    && depthData.validPointsMask[offset] // is valid
                    //                    && depthData.depthPoints[offset] != INVALID_POINT // is valid
                    //                    && depthData.depthPoints[offset].distanceTo(INVALID_POINT) >= 0.01f //  TODO WHY invalidpoints here.
                    //                    && DepthAnalysis.isValidPoint(depthData.depthPoints[offset])
                    && depthData.depthPoints[inititalPoint].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistanceInit()
                    && depthData.depthPoints[offset].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistance()
                    && goodNormal;
        }
    }

    public void findTouch(PlaneAndProjectionCalibration planeAndProjCalibration) {
        // Search for 2D slices over a plane
        touchRecognition.find2DTouch(planeAndProjCalibration, getPrecision());

        // Generate a touch list from these points. 
        ArrayList<TrackedDepthPoint> newList;
        newList = this.compute(this.depthAnalysis.getDepthData());

        // Track the points and update the touchPoints2D variable.
        TouchPointTracker.trackPoints(touchPoints, newList, currentTime);
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
    }

}
