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
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.files.PlaneCalibration;
import fr.inria.papart.depthcam.analysis.Connexity;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import fr.inria.papart.depthcam.TouchAttributes;
import java.util.Arrays;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class ProjectedDepthData extends DepthData {

    /**
     * Normalized version of the 3D points
     */
    public Vec3D[] projectedPoints;

    /**
     * Attributes of the 3D points
     */
    public TouchAttributes[] touchAttributes;

    public PlaneAndProjectionCalibration planeAndProjectionCalibration;
    public HomographyCalibration homographyCalibration;
    public PlaneCalibration planeCalibration;

    public ProjectedDepthData(DepthAnalysis source) {
        this(source, true);
    }

    public ProjectedDepthData(DepthAnalysis source, boolean is3D) {
        super(source);

        int size = source.getSize();
        projectedPoints = new Vec3D[size];
        for (int i = 0; i < size; i++) {
            projectedPoints[i] = new Vec3D();
        }
        
        touchAttributes = new TouchAttributes[size];
        connexity = new Connexity(depthPoints, source.getWidth(), source.getHeight());
//        connexity = new Connexity(projectedPoints, width, height);
    }

    @Deprecated
    public DepthDataElementProjected getElementKinect(int i) {
        return getDepthElement(i);
    }
    
    public DepthDataElementProjected getDepthElement(int i) {
        DepthDataElementProjected dde = new DepthDataElementProjected();
        fillDepthDataElement(dde, i);
        return dde;
    }

    protected void fillDepthDataElement(DepthDataElementProjected ddek, int i) {
        super.fillDepthDataElement(ddek, i);
        ddek.projectedPoint = projectedPoints[i].copy();
        ddek.touchAttribute = touchAttributes[i];
    }

    @Override
    public void clear() {
        clearDepth();
        clearColor();
        connexity.reset();
        Arrays.fill(touchAttributes, TouchAttributes.NO_ATTRIBUTES);
    }

    @Override
    public void clearDepth() {
        super.clearDepth();

        for (Vec3D pt : projectedPoints) {
            pt.clear();
        }
//        Arrays.fill(this.projectedPoints, INVALID_POINT);
    }

}
