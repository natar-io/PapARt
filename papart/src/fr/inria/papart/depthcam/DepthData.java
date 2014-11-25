/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.depthcam;

import static fr.inria.papart.depthcam.Kinect.INVALID_COLOR;
import static fr.inria.papart.depthcam.Kinect.INVALID_POINT;
import fr.inria.papart.depthcam.calibration.HomographyCalibration;
import fr.inria.papart.depthcam.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.calibration.PlaneCalibration;
import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.ProjectiveDeviceP;
import java.util.ArrayList;
import java.util.Arrays;
import org.bytedeco.javacv.ProjectiveDevice;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class DepthData {

    /**
     * 3D points viewed by the kinects
     */
    public Vec3D[] kinectPoints;

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
    public boolean[] validPointsMask;
    public boolean[] validPointsMask3D;

    /**
     * Not sure if used...
     */
    public int[] pointColors;

    /**
     * List of valid points
     */
    public ArrayList<Integer> validPointsList;
    public ArrayList<Integer> validPointsList3D;

    public ProjectiveDeviceP projectiveDevice;
    public PlaneAndProjectionCalibration planeAndProjectionCalibration;
    public HomographyCalibration homographyCalibration;
    public PlaneCalibration planeCalibration;

    public int timeStamp;
    
    public DepthData(int size) {
        this(size, true);
    }

    public DepthData(int size, boolean is3D) {
        kinectPoints = new Vec3D[size];
        projectedPoints = new Vec3D[size];
        validPointsMask = new boolean[size];
        pointColors = new int[size];
        touchAttributes = new TouchAttributes[size];
        validPointsList = new ArrayList();
        if (is3D) {
            validPointsMask3D = new boolean[size];
            validPointsList3D = new ArrayList();
        }
    }
    
    public DepthDataElement getElement(int i){
        DepthDataElement dde = new DepthDataElement();
        dde.pointColor = pointColors[i];
        dde.kinectPoint = kinectPoints[i];
        dde.projectedPoint = projectedPoints[i];
        dde.touchAttribute = touchAttributes[i];
        dde.validPoint = validPointsMask[i];
        dde.validPoint3D = validPointsMask3D[i];
        dde.offset = i;
        return dde;
    }

    public void clear() {
        clearDepth();
        clear2D();
        clear3D();
        clearColor();
        Arrays.fill(touchAttributes, TouchAttributes.NO_ATTRIBUTES);
    }
    
    void clearColor(){
        Arrays.fill(this.pointColors, INVALID_COLOR);
    }

    void clearDepth() {
        Arrays.fill(this.kinectPoints, INVALID_POINT);
        Arrays.fill(this.projectedPoints, INVALID_POINT);
    }

    void clear2D() {
        Arrays.fill(this.validPointsMask, false);
        this.validPointsList.clear();
    }

    void clear3D() {
        Arrays.fill(this.validPointsMask3D, false);
        this.validPointsList3D.clear();
    }

}
