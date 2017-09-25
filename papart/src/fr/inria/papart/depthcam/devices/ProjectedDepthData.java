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

import static fr.inria.papart.depthcam.analysis.DepthAnalysis.INVALID_COLOR;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.INVALID_POINT;
import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.files.PlaneCalibration;
import fr.inria.papart.depthcam.analysis.Connexity;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import fr.inria.papart.depthcam.TouchAttributes;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.ProjectiveDeviceP;
import java.util.ArrayList;
import java.util.Arrays;
import org.bytedeco.javacv.ProjectiveDevice;
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

    /**
     * Mask of valid Points
     */
    public boolean[] validPointsMask3D;

    /**
     * List of valid points
     */
    public ArrayList<Integer> validPointsList3D;

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
        validPointsList = new ArrayList();
        if (is3D) {
            validPointsMask3D = new boolean[size];
            validPointsList3D = new ArrayList();
        }
        connexity = new Connexity(depthPoints, source.getWidth(), source.getHeight());
//        connexity = new Connexity(projectedPoints, width, height);
    }

    public DepthDataElementProjected getElementKinect(int i) {
        DepthDataElementProjected dde = new DepthDataElementProjected();
        fillDepthDataElement(dde, i);
        return dde;
    }

    protected void fillDepthDataElement(DepthDataElementProjected ddek, int i) {
        super.fillDepthDataElement(ddek, i);
        ddek.projectedPoint = projectedPoints[i].copy();
        ddek.touchAttribute = touchAttributes[i];
        ddek.validPoint3D = validPointsMask3D[i];
    }

    @Override
    public void clear() {
        clearDepth();
        clear2D();
        clear3D();
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

    @Override
    public void clear2D() {
        super.clear2D();
        this.validPointsList.clear();
    }

    void clear3D() {
        Arrays.fill(this.validPointsMask3D, false);
        this.validPointsList3D.clear();
    }

}
