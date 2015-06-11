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

import static fr.inria.papart.depthcam.DepthAnalysis.INVALID_COLOR;
import static fr.inria.papart.depthcam.DepthAnalysis.INVALID_POINT;
import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.ProjectiveDeviceP;
import java.util.ArrayList;
import java.util.Arrays;
import org.bytedeco.javacv.ProjectiveDevice;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class KinectDepthData extends DepthData{


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

    
    public KinectDepthData(DepthAnalysis source) {
        this(source, true);
    }

    public KinectDepthData(DepthAnalysis source, boolean is3D) {
        super(source);
        
        int size = source.getSize();
        projectedPoints = new Vec3D[size];
        touchAttributes = new TouchAttributes[size];
        validPointsList = new ArrayList();
        if (is3D) {
            validPointsMask3D = new boolean[size];
            validPointsList3D = new ArrayList();
        }
        connexity = new Connexity(depthPoints, source.getWidth(), source.getHeight());
//        connexity = new Connexity(projectedPoints, width, height);
    }
    
    public DepthDataElementKinect getElementKinect(int i){
        DepthDataElementKinect dde = new DepthDataElementKinect();
        fillDepthDataElement(dde, i);
        return dde;
    }

    protected void fillDepthDataElement(DepthDataElementKinect ddek, int i ){
        super.fillDepthDataElement(ddek, i);
        ddek.projectedPoint = projectedPoints[i];
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
    void clearDepth() {
        super.clearDepth();
        Arrays.fill(this.projectedPoints, INVALID_POINT);
    }

    @Override
    void clear2D() {
        Arrays.fill(this.validPointsMask, false);
        this.validPointsList.clear();
    }

    void clear3D() {
        Arrays.fill(this.validPointsMask3D, false);
        this.validPointsList3D.clear();
    }

}
