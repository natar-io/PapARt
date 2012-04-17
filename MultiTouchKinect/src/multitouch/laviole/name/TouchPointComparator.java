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

public class TouchPointComparator implements Comparator{

    public int compare(Object tp1, Object tp2){
    Vec3D orig = new Vec3D(0, 0, 0);
    Vec3D pos1 = ((TouchPoint) tp1).v;
    Vec3D pos2 = ((TouchPoint) tp2).v;

    if(pos2.distanceTo(orig)  < pos1.distanceTo(orig))
      return 1;
    return -1;
  }
}

