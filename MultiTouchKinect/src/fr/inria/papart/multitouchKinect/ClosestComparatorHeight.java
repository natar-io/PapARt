/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.papart.multitouchKinect;

import java.util.Comparator;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
*/

public class ClosestComparatorHeight implements Comparator{

  public Vec3D[] points;
  PlaneSelection planeSelection;
  
  public ClosestComparatorHeight(Vec3D points[],
          PlaneSelection planeSelection){
    this.points = points;
    this.planeSelection = planeSelection;
  }

  public int compare(Object tp1, Object tp2){

    float d1 = planeSelection.distanceTo(points[(Integer)tp1]);
    float d2 = planeSelection.distanceTo(points[(Integer)tp2]);
    if(d1 > d2)
      return 1;
    return -1;
  }
}
