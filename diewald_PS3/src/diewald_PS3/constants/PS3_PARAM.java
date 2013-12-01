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
 * PS3_PARAM contains a list of all available video-parameters and their min/max values.<br>
 * <br>
 * 
 * @author thomas diewald (c) 2011
 *
 */
public enum PS3_PARAM {
  // camera sensor parameters
  /*** 0 to 1 */
  AUTO_GAIN           ( 0,    0,   1 ),   // [false, true]
  /*** 0 to 79 */
  GAIN                ( 1,    0,  79 ),   // [0, 79]
  /*** 0 to 1 */
  AUTO_EXPOSURE       ( 2,    0,   1 ),   // [false, true]
  /*** 0 to 511 */
  EXPOSURE            ( 3,    0, 511 ),   // [0, 511]
  /*** 0 to 1 */
  AUTO_WHITEBALANCE   ( 4,    0,   1 ),   // [false, true]
  /*** 0 to 255 */
  WHITEBALANCE_RED    ( 5,    0, 255 ),   // [0, 255]
  /*** 0 to 255 */
  WHITEBALANCE_GREEN  ( 6,    0, 255 ),   // [0, 255]
  /*** 0 to 255 */
  WHITEBALANCE_BLUE   ( 7,    0, 255 ),   // [0, 255]
  
  // camera linear transform parameters (valid for CLEYE_MONO_PROCESSED, CLEYE_COLOR_PROCESSED modes)
//  /*** 0 to 1 */
  HFLIP               ( 8,    0,   1 ),   // [false, true]
//  /*** 0 to 1 */
  VFLIP               ( 9,    0,   1 ),   // [false, true]
//  /*** -500 to 500 */
  HKEYSTONE           (10, -500, 500 ),   // [-500, 500]
//  /*** -500 to 500 */
  VKEYSTONE           (11, -500, 500 ),   // [-500, 500]
//  /*** -500 to 500 */
  XOFFSET             (12, -500, 500 ),   // [-500, 500]
//  /*** -500 to 500 */
  YOFFSET             (13, -500, 500 ),   // [-500, 500]
//  /*** -500 to 500 */
  ROTATION            (14, -500, 500 ),   // [-500, 500]
//  /*** -500 to 500 */
  ZOOM                (15, -500, 500 ),   // [-500, 500]
  
  // camera non-linear transform parameters (valid for CLEYE_MONO_PROCESSED, CLEYE_COLOR_PROCESSED modes)
//  /*** -500 to 500 */
  LENSCORRECTION1     (16, -500, 500 ),   // [-500, 500]
//  /*** -500 to 500 */
  LENSCORRECTION2     (17, -500, 500 ),   // [-500, 500]
//  /*** -500 to 500 */
  LENSCORRECTION3     (18, -500, 500 ),   // [-500, 500]
//  /*** -500 to 500 */
  LENSBRIGHTNESS      (19, -500, 500 ),   // [-500, 500]
  
  ;
  
  int index_;
  int value_min_;
  int value_max_;
  
  PS3_PARAM(int index, int value_min, int value_max){
    index_     = index;
    value_min_ = value_min;
    value_max_ = value_max;
  }
  public final int getIndex(){
    return index_;
  }
  public final int getMaxVal(){
    return value_max_;
  }
  public final int getMinVal(){
    return value_min_;
  }
  
}
