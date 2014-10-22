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
package fr.inria.papart.multitouch;

import fr.inria.papart.depthcam.KinectScreenCalibration;
import java.util.Comparator;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
*/

public class ClosestComparatorHeight implements Comparator{

  public Vec3D[] points;
  KinectScreenCalibration calibration;
  
  public ClosestComparatorHeight(Vec3D points[],
          KinectScreenCalibration calib){
    this.points = points;
    this.calibration = calib;
  }

  public int compare(Object tp1, Object tp2){

    float d1 = calibration.plane().distanceTo(points[(Integer)tp1]);
    float d2 = calibration.plane().distanceTo(points[(Integer)tp2]);
    if(d1 > d2)
      return 1;
    return -1;
  }
}
