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

import fr.inria.papart.depthcam.devices.KinectDepthData;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.TrackedDepthPoint;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import java.util.HashSet;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class Hand extends Simple2D {

    public Hand(WithSize size) {
        super(size);
    }

    @Override
    public ArrayList<TrackedDepthPoint> compute(KinectDepthData dData) {

        this.depthData = dData;

        if (!hasCCToFind()) {
            return new ArrayList<TrackedDepthPoint>();
        }

        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents();

        for (ConnectedComponent component : connectedComponents) {
            findGreaterComponent(component);

        }

        ArrayList<TrackedDepthPoint> touchPoints = this.createTouchPointsFrom(connectedComponents);

        return touchPoints;
    }

    
    @Override
    protected void setSearchParameters() {
        this.toVisit.clear();
        this.toVisit.addAll(depthData.validPointsList);

        currentPointValidityCondition = new CheckTouchPoint();
        int firstPoint = toVisit.iterator().next();
//        setPrecisionFrom(firstPoint);
//        calib.setSearchDepth(precision * 7);// TODO: FIX this value !
//        maximumRecursion = 100; // TODO: fix this value.
    }
    
    
    private ConnectedComponent findGreaterComponent(ConnectedComponent connectedComponent) {
        clearMemory();
        int startingPoint = connectedComponent.get(0);
        currentPointValidityCondition = new CheckOverPlane(startingPoint);

        ConnectedComponent neighbours = findNeighboursRec(
                startingPoint,
                0, getX(startingPoint), getY(startingPoint));
        return neighbours;
    }

    public class CheckOverPlane implements PointValidityCondition {

        private static final float MAX_HAND_SIZE = 200f; // 20 cm 
        private final Vec3D firstPoint;

        public CheckOverPlane(int offset) {
            firstPoint = depthData.depthPoints[offset];
        }

        @Override
        public boolean checkPoint(int offset, int currentPoint) {
            float distanceToFirst = firstPoint.distanceTo(depthData.depthPoints[currentPoint]);
            return !assignedPoints[offset] // not assigned  
                    && depthData.touchAttributes[offset].isOverTouch() // is a «Touch» point
                    && (depthData.depthPoints[offset] != DepthAnalysis.INVALID_POINT) // not invalid point (invalid depth)
                    && distanceToFirst < MAX_HAND_SIZE;
        }
    }


}
