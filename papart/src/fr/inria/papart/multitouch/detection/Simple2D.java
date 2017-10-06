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

import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.DepthDataElement;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.INVALID_POINT;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.isValidPoint;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.analysis.Compute2D;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import static fr.inria.papart.multitouch.ConnectedComponent.INVALID_COMPONENT;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import java.util.HashMap;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class Simple2D extends TouchDetectionDepth {

    private final Compute2D touchRecognition;
    private final HashMap<Byte, ConnectedComponent> contactPoints;

    public Simple2D(DepthAnalysisImpl depthAnalysisImpl, PlanarTouchCalibration calib) {
        super(depthAnalysisImpl, calib);
        this.contactPoints = new HashMap<>();
        touchRecognition = new Compute2D(depthAnalysisImpl);
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
        public boolean checkPoint(int candidate, int currentPoint) {
//            float distanceToCurrent = depthData.depthPoints[offset].distanceTo(depthData.depthPoints[currentPoint]);

            // DEBUG
//            assert (isValidPoint(depthData.depthPoints[currentPoint]));
//            assert (isValidPoint(depthData.projectedPoints[currentPoint]));
            boolean classicCheck = !assignedPoints[candidate] // not assigned   
                    //                    && depthData.validPointsMask[offset] // is valid
                    //                                        && depthData.depthPoints[offset] != INVALID_POINT // is valid
                    //                                        && depthData.depthPoints[offset].distanceTo(INVALID_POINT) >= 0.01f
                    && depthData.normals[candidate] != null //  good normal is good health
                    && isValidPoint(depthData.projectedPoints[candidate]) //  TODO WHY "0" and non invalidpoints here.
                    && isValidPoint(depthData.depthPoints[candidate]) //  TODO WHY "0" and non invalidpoints here.
                    && depthData.depthPoints[inititalPoint].distanceTo(depthData.depthPoints[candidate]) < calib.getMaximumDistanceInit()
                    && depthData.depthPoints[candidate].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistance();

            // A close one does not have a correct normal.
//            if (classicCheck && !goodNormal) {
//                if (!contactPoints.containsKey(currentCompo)) {
//                    contactPoints.put(currentCompo, new ConnectedComponent());
//                }
//                ConnectedComponent list = contactPoints.get(currentCompo);
//                if (!list.contains(currentPoint)) {
//                    list.add(currentPoint);
//                }
////                System.out.println("Adding non normal point...");
//            }/
            boolean goodNormal = true;

            if (depthData.normals[candidate] != null) {
                float dN = (depthData.planeAndProjectionCalibration.getPlane().normal).distanceToSquared(depthData.normals[candidate]);
                float d1 = (depthData.planeAndProjectionCalibration.getPlane().getDistanceToPoint(depthData.depthPoints[candidate]));

                // WARNING MAGIC NUMBER HERE
//                boolean higher = depthData.projectedPoints[candidate].z < depthData.projectedPoints[currentPoint].z;
                goodNormal = (depthData.normals[candidate] != null && dN > calib.getNormalFilter()) || d1 > 20f;  // Higher  than Xmm
            }
            return classicCheck && goodNormal;
        }
    }

    public void findTouch(PlaneAndProjectionCalibration planeAndProjCalibration) {
        // Search for 2D slices over a plane
        touchRecognition.find2DTouch(planeAndProjCalibration, getPrecision());

        // Generate a touch list from these points. 
        ArrayList<TrackedDepthPoint> newList;

        newList = this.compute(this.depthAnalysis.getDepthData());

        int imageTime = this.depthAnalysis.getDepthData().timeStamp;
        // Track the points and update the touchPoints2D variable.
        TouchPointTracker.trackPoints(touchPoints, newList, imageTime);

        // Uncomment to disable tracking.
//        touchPoints.clear();
//        touchPoints.addAll(newList);
    }

    @Override
    protected ConnectedComponent findConnectedComponent(int startingPoint) {

        // searchDepth is by precision steps. 
        searchDepth = calib.getSearchDepth() * calib.getPrecision();
        precision = calib.getPrecision();

        w = imgSize.getWidth();
        h = imgSize.getHeight();
        initialPoint = startingPoint;
        // DEBUG
        assert (isValidPoint(depthData.depthPoints[startingPoint]));
        assert (isValidPoint(depthData.projectedPoints[startingPoint]));

        ConnectedComponent cc = findNeighboursRec(startingPoint, 0, getX(startingPoint), getY(startingPoint));

        // Do not accept 1 point compo ?!
        if (cc.size() <= calib.getMinimumComponentSize()) {

            clearPoints(cc);
            // Remove all points
            contactPoints.remove(currentCompo);
            connectedComponentImage[startingPoint] = NO_CONNECTED_COMPONENT;
            return INVALID_COMPONENT;
        }

        cc.setId(currentCompo);
        currentCompo++;
        return cc;
    }

    protected void clearPoints(ConnectedComponent cc) {
        for (Integer pt : cc) {
            connectedComponentImage[pt] = NO_CONNECTED_COMPONENT;
        }
    }

    // Disabled for testing distance from hand
    protected ArrayList<TrackedDepthPoint> createTouchPointsWithContacts(ArrayList<ConnectedComponent> connectedComponents) {

        // Bypass this step and use our now found points.
        ArrayList<TrackedDepthPoint> newPoints = new ArrayList<TrackedDepthPoint>();
        for (ConnectedComponent connectedComponent : connectedComponents) {
//        for (ConnectedComponent connectedComponent : contactPoints.values()) {

            float height = connectedComponent.getHeight(depthData.projectedPoints);
            if (connectedComponent.size() < calib.getMinimumComponentSize()
                    || height < calib.getMinimumHeight()
                    || !contactPoints.containsKey((byte) connectedComponent.getId())) {

                continue;
            }

            TrackedDepthPoint tp = createTouchPoint(contactPoints.get((byte) connectedComponent.getId()));
            tp.setDepthDataElements(depthData, connectedComponent);
            newPoints.add(tp);
        }

        return newPoints;
    }

    @Override
    protected TrackedDepthPoint createTouchPoint(ConnectedComponent connectedComponent) {

//        ConnectedComponent cc2 = contactPoints.get((byte) connectedComponent.getId());
        Vec3D meanProj, meanKinect;
//        if (cc2 != null) {
//            System.out.println("Points: ");
//            for (int offset : cc2) {
//                System.out.print(depthData.projectedPoints[offset] + " ");
////            }
//            connectedComponent = cc2;
//        }
        meanProj = connectedComponent.getMean(depthData.projectedPoints);
        meanKinect = connectedComponent.getMean(depthData.depthPoints);

        TrackedDepthPoint tp = new TrackedDepthPoint();
        tp.setDetection(this);
        tp.setPosition(meanProj);
        tp.setPositionKinect(meanKinect);
        tp.setCreationTime(depthData.timeStamp);
        tp.set3D(false);
        tp.setConfidence(connectedComponent.size() / calib.getMinimumComponentSize());
        // TODO: re-enable this one day ?
//        tp.setConnectedComponent(connectedComponent);
        tp.setDepthDataElements(depthData, connectedComponent);
        return tp;
    }

    @Override
    public ArrayList<TrackedDepthPoint> compute(ProjectedDepthData dData) {
        this.setDepthData(dData);
        if (!hasCCToFind()) {
            return new ArrayList<TrackedDepthPoint>();
        }

        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents();
        ArrayList<TrackedDepthPoint> newPoints = this.createTouchPointsFrom(connectedComponents);
        return newPoints;
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
