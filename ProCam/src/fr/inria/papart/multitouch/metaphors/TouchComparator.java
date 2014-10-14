/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.papart.multitouch.metaphors;

import fr.inria.papart.multitouch.Touch;
import java.util.Comparator;

/**
 *
 */
public class TouchComparator implements Comparator{

  public TouchComparator(){
  }

  @Override
  public int compare(Object touch0, Object touch1){
    Touch t0 = (Touch) touch0;
    Touch t1 = (Touch) touch1;
    if(t0.position.y < t1.position.y)
      return 1;
    return -1;
  }
}
