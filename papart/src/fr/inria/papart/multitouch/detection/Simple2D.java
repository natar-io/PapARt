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

import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.depthcam.devices.KinectDepthData;
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
public class Simple2D extends TouchDetectionDepth {

    public Simple2D(WithSize size) {
        super(size);
        currentPointValidityCondition = new CheckTouchPoint();
    }

    @Override
    public ArrayList<TrackedDepthPoint> compute(KinectDepthData dData) {
        this.setDepthData(depthData);

        if (!hasCCToFind()) {
            return new ArrayList<TrackedDepthPoint>();
        }

        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents();
        ArrayList<TrackedDepthPoint> touchPoints = this.createTouchPointsFrom(connectedComponents);
        return touchPoints;
    }

    
    @Override
    protected void setSearchParameters() {
        this.toVisit.clear();
        this.toVisit.addAll(depthData.validPointsList);
    }

 
}
