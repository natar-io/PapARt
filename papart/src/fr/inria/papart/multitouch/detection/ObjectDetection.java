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

import fr.inria.papart.calibration.PlanarTouchCalibration;
import tech.lity.rea.nectar.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.DepthDataElement;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.INVALID_POINT;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.isValidPoint;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.analysis.Compute2D;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.depthcam.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import static fr.inria.papart.multitouch.ConnectedComponent.INVALID_COMPONENT;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.procam.camera.TrackedView;
import tech.lity.rea.utils.WithSize;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class ObjectDetection extends TouchDetectionDepth {

    private final Compute2D touchRecognition;
    private final HashMap<Byte, ConnectedComponent> contactPoints;

    TrackedView trackedView;
    
    public ObjectDetection(DepthAnalysisImpl depthAnalysisImpl, PlanarTouchCalibration calib) {
        super(depthAnalysisImpl, calib);
        this.contactPoints = new HashMap<>();
        touchRecognition = new Compute2D(depthAnalysisImpl);
        currentPointValidityCondition = new CheckTouchPoint();
    }

    public class CheckTouchPoint implements PointValidityCondition {

        private int initialPoint;

        public ProjectedDepthData getData() {
            return depthData;
        }

        @Override
        public boolean checkPoint(int candidate, int currentPoint) {
            boolean classicCheck = !assignedPoints[candidate] // not assigned   

                    && touchRecognition.getSelection().validPointsMask[candidate] // is valid, necessary ?
                    && (depthData.depthPoints[candidate].distanceTo(DepthAnalysis.INVALID_POINT) > 1) // NON zero points
                    && (depthData.depthPoints[candidate] != DepthAnalysis.INVALID_POINT) // not invalid point (invalid depth)
                    && depthData.depthPoints[initialPoint].distanceTo(depthData.depthPoints[candidate]) < calib.getMaximumDistanceInit()
                    && depthData.depthPoints[candidate].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistance();
            
            return classicCheck;
        }

        @Override
        public void setInitialPoint(int offset) {
             this.initialPoint = offset;
                // todo:implement this better
        }
    }

    public void findTouch(PlaneAndProjectionCalibration planeAndProjCalibration) {
        // Search for 2D slices over a plane
        touchRecognition.find2DTouch(planeAndProjCalibration, getPrecision());

        // Generate a touch list from these points. 
        ArrayList<TrackedDepthPoint> newList = this.compute(this.depthAnalysis.getDepthData());
        int imageTime = this.depthAnalysis.getDepthData().timeStamp;

        // Track the points and update the touchPoints2D variable.
        TouchPointTracker.trackPoints(touchPoints, newList, imageTime);
        // Uncomment to disable tracking.
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
        contactPoints.clear();
    }

}
