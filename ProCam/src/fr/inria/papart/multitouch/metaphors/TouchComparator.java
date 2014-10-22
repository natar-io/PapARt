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
