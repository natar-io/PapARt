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

import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import static fr.inria.papart.multitouch.ConnectedComponent.INVALID_COMPONENT;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import java.util.ArrayList;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class HandDetection extends TouchDetectionDepth {

    private DepthData.DepthSelection depthSelection;

    public HandDetection(DepthAnalysisImpl depthAnalysisImpl, PlanarTouchCalibration calib) {
        super(depthAnalysisImpl, calib);
        currentPointValidityCondition = new CheckTouchPoint3D();
    }

    void setDepthSelection(DepthData.DepthSelection depthSelection) {
        this.depthSelection = depthSelection;
    }

    public class CheckTouchPoint3D implements PointValidityCondition {

        @Override
        public boolean checkPoint(int candidate, int currentPoint) {

            return !assignedPoints[candidate] // not assigned  

                    // Use the previous depthSelection.
                    && depthSelection.validPointsMask[candidate] // is valid, necessary ?
                    && (depthData.depthPoints[candidate] != DepthAnalysis.INVALID_POINT) // not invalid point (invalid depth)

                    && depthData.planeAndProjectionCalibration.distanceTo(depthData.depthPoints[candidate]) < calib.getTest1()
                    && depthData.planeAndProjectionCalibration.distanceTo(depthData.depthPoints[candidate]) > calib.getTest2()
                    && depthData.depthPoints[initialPoint].distanceTo(depthData.depthPoints[candidate]) < calib.getMaximumDistanceInit()
                    && depthData.depthPoints[candidate].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistance();

        }
    }

    @Override
    protected ConnectedComponent findConnectedComponent(int startingPoint) {

        // searchDepth is by precision steps. 
        searchDepth = calib.getSearchDepth() * calib.getPrecision();
        precision = calib.getPrecision();

        w = imgSize.getWidth();
        h = imgSize.getHeight();
        ConnectedComponent cc = findNeighboursFloodFill(startingPoint);
//        ConnectedComponent cc = findNeighboursRec(startingPoint, 0, getX(startingPoint), getY(startingPoint));

        // Do not accept 1 point compo ?!
        if (cc.size() == 1) {
            connectedComponentImage[startingPoint] = NO_CONNECTED_COMPONENT;
            return INVALID_COMPONENT;
        }

        cc.setId(currentCompo);
        currentCompo++;
        return cc;
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
        return !depthSelection.validPointsList.isEmpty();
    }

    @Override
    protected void setSearchParameters() {
    }

    @Override
    protected ArrayList<TrackedDepthPoint> createTouchPointsFrom(ArrayList<ConnectedComponent> connectedComponents) {
        ArrayList<TrackedDepthPoint> newPoints = new ArrayList<TrackedDepthPoint>();
        for (ConnectedComponent connectedComponent : connectedComponents) {
            if (connectedComponent.size() < calib.getMinimumComponentSize()) {
                continue;
            }

            TrackedDepthPoint tp = createTouchPoint(connectedComponent);
            newPoints.add(tp);
        }
        return newPoints;
    }

    @Override
    protected TrackedDepthPoint createTouchPoint(ConnectedComponent connectedComponent) {

        TrackedDepthPoint tp = super.createTouchPoint(connectedComponent);

        // TODO: use this, add another with only the ones of the touch ?!
        tp.setDepthDataElements(depthData, connectedComponent);
        tp.set3D(true);
        tp.setParent(currentArm);
        return tp;
    }

    private TrackedDepthPoint currentArm;

    public ArrayList<TrackedDepthPoint> findTouch(ArmDetection armDetection, PlaneAndProjectionCalibration planeAndProjCalibration) {

        // WARNING  No tracking 
        this.touchPoints.clear();

        // Start from the arm, find the lower part. 
        for (TrackedDepthPoint arm : armDetection.getTouchPoints()) {
            DepthElementList allElements = new DepthElementList();

            currentArm = arm;

            // No boundaries.
//            this.setDepthSelection(armDetection.getDepthSelection());
//            this.toVisit.addAll(depthSelection.validPointsList);

// Simple copy of current cc.
//            DepthElementList depthElements = arm.getDepthDataElements();
//            this.toVisit.addAll(depthElements.toConnectedComponent());
//            this.setDepthSelection(armDetection.getDepthSelection());

//            // Boundaries. 
//            // Fill the valid points with arm boundaries
            for (DepthDataElementProjected dde : arm.getDepthDataElements()) {

                if (armDetection.boundaries[dde.offset]) {
                    float d = planeAndProjCalibration.distanceTo(dde.depthPoint);
//                    if (d < calib.getTest3()) {
                        allElements.add(dde);
//                    }
                }
            }
            // Select from the known points
            this.setDepthSelection(armDetection.getDepthSelection());
            depthSelection.validPointsList.clear();

            ConnectedComponent selectedList = allElements.toConnectedComponent();
            depthSelection.validPointsList.addAll(selectedList);
            this.toVisit.addAll(depthSelection.validPointsList);
            
            
            // Find the connected components
            ArrayList<TrackedDepthPoint> newList = this.compute(depthAnalysis.getDepthData());

            this.touchPoints.addAll(newList);
            if (!newList.isEmpty()) {

                // Select the biggest compo as the hand.
//                int maxSize = newList.get(0).getDepthDataElements().size();
//                int maxId = 0;
//                for (int i = 1; i < newList.size(); i++) {
//                    if (newList.size() > 1) {
//                        int size = newList.get(0).getDepthDataElements().size();
//                        if (size > maxSize) {
//                            maxSize = size;
//                            maxId = i;
//                        }
//                    }
//                }
//                this.touchPoints.add(newList.get(maxId));
            }
        }

//        System.out.println("nbHands: " + newList.size());
//        this.touchPoints.addAll(newList);
        return this.touchPoints;
    }

}
