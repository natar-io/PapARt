/**
 * This package provides classes to facilitate the handling of opengl textures, glsl shaders and 
 * off-screen rendering in Processing.
 * @author Andres Colubri
 * @version 0.95
 *
 * Copyright (c) 2008 Andres Colubri
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

package codeanticode.glgraphics;

import processing.core.*;

/**
 * This class stores the parameter for a texture filter.
 */
public abstract class GLTextureFilterParameter {
  public GLTextureFilterParameter(PApplet parent, String name, String label,
      int type) {
  }

  /**
   * Creates an instance of GLTextureFilterParameter using the specified
   * parameters.
   * 
   * @param parent PApplet
   * @param shader GLShader
   * @param name String
   * @param label String
   * @param type int
   */
  public GLTextureFilterParameter(PApplet parent, GLShader shader,
      String name, String label, int type) {    
  }
  
  abstract public void setShader(GLShader shader);

  /**
   * Returns true or false depending on whether this variable is available for use.
   * @return boolean
   */  
  abstract public boolean available();
  
  /**
   * Initializes this parameter.
   */
  abstract public void init();

  /**
   * Sets the parameter value when the type is int.
   * 
   * @param value int
   */
  abstract public void setValue(int value);

  /**
   * Sets the parameter value when the type is float.
   * 
   * @param value float
   */
  abstract public void setValue(float value);

  /**
   * Sets the parameter value for any type. When the type is int or float, the
   * first element of the value array is considered.
   * 
   * @param value float[]
   */
  abstract public void setValue(float[] value);

  /**
   * Sets the ith value for the parameter (only valid for vec or mat types).
   * 
   * @param int i
   * @param value float
   */
  abstract public void setValue(int i, float value);

  /**
   * Sets the (ith, jth) value for the parameter (only valid for mat types).
   * 
   * @param int i
   * @param int j
   * @param value float
   */
  abstract public void setValue(int i, int j, float value);

  /**
   * Copies variable values to shader.
   */
  abstract public void copyToShader();  

  /**
   * Returns parameter type.
   * 
   * @return int
   */
  abstract int getType();

  /**
   * Returns parameter name.
   * 
   * @return String
   */
  abstract public String getName();

  /**
   * Returns parameter label.
   * 
   * @return String
   */
  abstract public String getLabel();  
}
