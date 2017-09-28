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
package fr.inria.papart.depthcam;

import fr.inria.papart.depthcam.analysis.Connexity;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.files.PlaneCalibration;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.INVALID_COLOR;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.INVALID_POINT;
import fr.inria.papart.procam.ProjectiveDeviceP;
import java.util.ArrayList;
import java.util.Arrays;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class DepthData {

    /**
     * 3D points viewed by the depth camera.
     */
    public Vec3D[] depthPoints;
    public Vec3D[] normals;
    public int[] pointColors;

    public Connexity connexity;

    public ProjectiveDeviceP projectiveDevice;

    public int timeStamp;
    public DepthAnalysis source;

//    public boolean[] validPointsMask;
//    public ArrayList<Integer> validPointsList;

    public class DepthSelection {

        /**
         * Mask of valid Points
         */
        public boolean[] validPointsMask;

        /**
         * List of valid points
         */
        public ArrayList<Integer> validPointsList;

    }

    public DepthData(DepthAnalysis source) {
        int width = source.getWidth();
        int height = source.getHeight();
        this.source = source;
        int size = width * height;
        depthPoints = new Vec3D[size];
        normals = new Vec3D[size];
        for (int i = 0; i < size; i++) {
            depthPoints[i] = new Vec3D();
            normals[i] = new Vec3D();
        }

        createSelection();
        pointColors = new int[size];
        connexity = new Connexity(depthPoints, width, height);
//        connexity = new Connexity(projectedPoints, width, height);
    }

    public DepthSelection createSelection() {
        DepthSelection selection = new DepthSelection();
        selection.validPointsMask = new boolean[depthPoints.length];
        selection.validPointsList = new ArrayList();
//        validPointsMask = new boolean[depthPoints.length];
//        validPointsList = new ArrayList();
//        selection.validPointsMask = validPointsMask;
//        selection.validPointsList = validPointsList;
        return selection;
    }

    public DepthDataElement getElement(int i) {
        DepthDataElement dde = new DepthDataElement();
        fillDepthDataElement(dde, i);
        return dde;
    }

    protected void fillDepthDataElement(DepthDataElement dde, int i) {
        dde.pointColor = pointColors[i];
        dde.depthPoint = depthPoints[i].copy();
        if (normals[i] != null) {
            dde.normal = normals[i].copy();
        }
        // Is the selection relevant afterwards ?
//        dde.validPoint = validPointsMask[i];
        dde.neighbourSum = connexity.connexitySum[i];
        dde.neighbours = connexity.connexity[i];
        dde.offset = i;
    }

    public void clear() {
        clearDepth();
//        clearValidPoints();
        clearColor();
        connexity.reset();
    }

    public void clearColor() {
        Arrays.fill(this.pointColors, INVALID_COLOR);
    }

    public void clearDepth() {
        for (Vec3D pt : depthPoints) {
            pt.clear();
        }
//        Arrays.fill(this.depthPoints, INVALID_POINT);
    }

//    public void clearValidPoints() {
//        Arrays.fill(this.validPointsMask, false);
//        this.validPointsList.clear();
//    }
}
