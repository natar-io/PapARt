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

import fr.inria.papart.depthcam.calibration.HomographyCalibration;
import fr.inria.papart.depthcam.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.calibration.PlaneCalibration;
import fr.inria.papart.procam.ProjectiveDeviceP;
import java.util.ArrayList;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class DepthDataElement {
    
    public int offset;
    public Vec3D kinectPoint;
    public Vec3D projectedPoint;
    public TouchAttributes touchAttribute;
    public boolean validPoint;
    public boolean validPoint3D;
    public int pointColor;

    public Vec3D normal;

    public byte neighbourSum;
    public byte neighbours;


    // Not sure if it will be used !
//    public ProjectiveDeviceP projectiveDevice;
//    public PlaneAndProjectionCalibration planeAndProjectionCalibration;
//    public HomographyCalibration homographyCalibration;
//    public PlaneCalibration planeCalibration;
    
}
