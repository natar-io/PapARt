/**
 * diewald_PS3 - Processing Library.
 * 
 * this processing-library provides access to sonys PS3eye camera.
 * 
 * its basically a wrapper of the ps3-library from CodeLaboratories.
 * http://codelaboratories.com/products/eye/driver/
 * http://codelaboratories.com/products/eye/sdk/
 * 
 * 
 * Copyright (c) 2011 Thomas Diewald
 *
 *
 * This source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License is available on the World
 * Wide Web at <http://www.gnu.org/copyleft/gpl.html>. You can also
 * obtain it by writing to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package diewald_PS3.constants;

/**
 * VIDEO_MODE contains all available video-modes for the video.<br>
 * <br>
 * 
 * @author thomas diewald (c) 2011
 *
 */
public enum VIDEO_MODE {
  QVGA (0),
  VGA  (1),
  ;
  
  private int index_;
  private VIDEO_MODE(int idx){
    index_ = idx;
  }
  
  public final int getIndex(){
    return index_;
  }
}

