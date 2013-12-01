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
 * 
 * @author thomas diewald (c) 2011
 *
 */
public enum LIBRARY {

  NAME    ("diewald_PS3.*"),
  VERSION ("01.00"),
  AUTHOR  ("thomas diewald"),
  NOTE    
      (
      "  CL-Eye-Driver-4.0.2.1017"+
      "\n  tested on winXP-x86/x64, win7-x86/x64"
      ),
  
  LABEL 
      (
      "\n=========================================================" +
      "\n  library: "+NAME.getValue() +
      "\n  version: "+VERSION.getValue()+
      "\n  author : (c) "+AUTHOR.getValue()+
      "\n"+
      "\n"+NOTE.getValue()+
      "\n=========================================================" 
      );
  
  private String value;
  private LIBRARY(String value) {
    this.value = value;
  }
  public final String getValue(){
    return value;
  }
}
