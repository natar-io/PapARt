/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.multitouch.tracking;

import fr.inria.papart.multitouch.Touch;
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
