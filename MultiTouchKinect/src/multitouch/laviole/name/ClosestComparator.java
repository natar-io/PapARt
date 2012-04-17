/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multitouch.laviole.name;

import java.util.Comparator;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
*/

public class ClosestComparator implements Comparator{

  public Vec3D[] projPoints;

  public ClosestComparator(Vec3D[] proj){
    projPoints = proj;
  }

  public int compare(Object tp1, Object tp2){

    Vec3D pos1 = projPoints[(Integer)tp1];
    Vec3D pos2 = projPoints[(Integer)tp2];
    if(pos1.z > pos2.z)
      return 1;
    return -1;
  }
}
