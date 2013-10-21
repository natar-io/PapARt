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

public class ClosestComparatorY implements Comparator{

  public Vec3D[] projPoints;

  public ClosestComparatorY(Vec3D[] proj){
    projPoints = proj;
  }

  public int compare(Object tp1, Object tp2){

    Vec3D pos1 = projPoints[(Integer)tp1];
    Vec3D pos2 = projPoints[(Integer)tp2];
    if(pos1.y > pos2.y)
      return 1;
    return -1;
  }
}
