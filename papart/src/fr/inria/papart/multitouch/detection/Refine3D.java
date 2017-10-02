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
import fr.inria.papart.depthcam.analysis.Compute3D;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import static fr.inria.papart.multitouch.ConnectedComponent.INVALID_COMPONENT;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class Refine3D extends TouchDetectionDepth {

    private DepthData.DepthSelection depthSelection;

    public Refine3D(DepthAnalysisImpl depthAnalysisImpl, PlanarTouchCalibration calib) {
        super(depthAnalysisImpl, calib);
        currentPointValidityCondition = new CheckTouchPoint3D();
    }

    void setDepthSelection(DepthData.DepthSelection depthSelection) {
        this.depthSelection = depthSelection;
    }

    public class CheckTouchPoint3D implements PointValidityCondition {

        // Not used yet here.
        private int inititalPoint;

        public void setInitalPoint(int offset) {
            this.inititalPoint = offset;
        }

        @Override
        public boolean checkPoint(int candidate, int currentPoint) {

            return !assignedPoints[candidate] // not assigned  

                    // Use the previous depthSelection.
                    && depthSelection.validPointsMask[candidate] // is valid, necessary ?
                    && (depthData.depthPoints[candidate] != DepthAnalysis.INVALID_POINT) // not invalid point (invalid depth)
                    && depthData.planeAndProjectionCalibration.distanceTo(depthData.depthPoints[candidate]) < calib.getTest1()
                    && depthData.depthPoints[inititalPoint].distanceTo(depthData.depthPoints[candidate]) < calib.getMaximumDistanceInit()
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
        currentPointValidityCondition.setInitalPoint(startingPoint);

        ConnectedComponent cc = findNeighboursRec(startingPoint, 0, getX(startingPoint), getY(startingPoint));

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

//            float height = connectedComponent.getHeight(depthData.projectedPoints);
            if (connectedComponent.size() < calib.getMinimumComponentSize()) {
//                    || height > calib.getMinimumHeight()) {
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
        return tp;
    }

    public void findTouch(Simple3D touchDetection3D, PlaneAndProjectionCalibration planeAndProjCalibration) {

        ArrayList<DepthDataElementProjected> allElements = new ArrayList<>();

        // Filter the 3D touchs... 
        for (TrackedDepthPoint touchPoint : touchDetection3D.getTouchPoints()) {
//            touchPoint.removeElementsAwayFromTable(depthAnalysis.getDepthData(),
//                    planeAndProjCalibration.getPlane(),
//                    (int) calib.getTest1());
            ArrayList<DepthDataElementProjected> selected = touchPoint.removeElementsAwayFromCenterDist(depthAnalysis.getDepthData(),
                    touchDetection3D.getDepthSelection(), getCalibration().getTest2());
            allElements.addAll(selected);
        }

        this.setDepthSelection(touchDetection3D.getDepthSelection());
        depthSelection.validPointsList.clear();

        ConnectedComponent selectedList = TrackedDepthPoint.ListToCC(allElements);
        depthSelection.validPointsList.addAll(selectedList);

        // Add again all the points for the CC computation.
//        for (TrackedDepthPoint touchPoint : touchDetection3D.getTouchPoints()) {
//            ConnectedComponent selectedList = TrackedDepthPoint.ListToCC(allElements);
//            depthSelection.validPointsList.addAll(touchPoint.getDepthDataAsConnectedComponent());
////            depthSelection.validPointsList.addAll(touchPoint.getDepthDataAsConnectedComponent());
//        }
        this.toVisit.addAll(depthSelection.validPointsList);
        ArrayList<TrackedDepthPoint> newList = this.compute(depthAnalysis.getDepthData());
        this.touchPoints.clear();
        this.touchPoints.addAll(newList);
    }

}
