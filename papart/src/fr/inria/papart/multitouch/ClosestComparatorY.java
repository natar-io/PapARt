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

import java.util.Comparator;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
*/

public class ClosestComparatorY implements Comparator{

  public Vec3D[] projPoints;

  public ClosestComparatorY(Vec3D[] proj){
    projPoints = proj;
  }

  public int compare(Object tp1, Object tp2){

    Vec3D pos1 = projPoints[(Integer)tp1];
    Vec3D pos2 = projPoints[(Integer)tp2];
    if(pos1.y < pos2.y)
      return 1;
    return -1;
  }
}
