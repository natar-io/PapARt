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

import processing.core.PApplet;

/**
 * This class stores the uniform parameter for a texture filter.
 */
public class GLSLTextureFilterParameter extends GLTextureFilterParameter {
  protected GLSLShaderUniform uniform;
  
  public GLSLTextureFilterParameter(PApplet parent, String name, String label,
      int type, int len) {
    super(parent, name, label, type);
    uniform = new GLSLShaderUniform(parent, name, label, type, len);
  }  
  
  public GLSLTextureFilterParameter(PApplet parent, GLShader shader,
      String name, String label, int type, int len) {
    super(parent, name, label, type);
    uniform = new GLSLShaderUniform(parent, shader, name, label, type,len);    
  }  
  
  public void setShader(GLShader shader) {
    uniform.setShader(shader);
  }

  /**
   * Returns true or false depending on whether this variable is available for use.
   * 
   * @return boolean
   */  
  public boolean available() {
    return uniform.available();
  }
  
  /**
   * Initializes this parameter..
   */
  public void init() {
    uniform.init();
  }

  /**
   * Sets the parameter value when the type is int.
   * 
   * @param value int
   */
  public void setValue(int value) {
    uniform.setValue(value);  
  }

  /**
   * Sets the parameter value when the type is float.
   * 
   * @param value float
   */
  public void setValue(float value) {
    uniform.setValue(value);  
  }

  /**
   * Sets the parameter value for any type. When the type is int or float, the
   * first element of the value array is considered.
   * 
   * @param value float[]
   */
  public void setValue(float[] value) {
    uniform.setValue(value);
  }

  /**
   * Sets the ith value for the parameter (only valid for vec or mat types).
   * 
   * @param int i
   * @param value float
   */
  public void setValue(int i, float value) {
    uniform.setValue(i, value);
  }

  public void setValue(int i, int j, float value) {
    uniform.setValue(i, j, value);
  }

  /**
   * Copies variable values to shader.
   */
  public void copyToShader() {
    uniform.copyToShader();  
  }

  /**
   * Returns parameter type.
   * 
   * @return int
   */
  int getType() {
    return uniform.getType();
  }

  /**
   * Returns parameter name.
   * 
   * @return String
   */
  public String getName() {
    return uniform.getName();  
  }

  /**
   * Returns parameter label.
   * 
   * @return String
   */
  public String getLabel() {
    return uniform.getLabel();
  }
  
  /**
   * Returns array length of parameter.
   * 
   * @return int
   */
  public int getArrayLength() {
    return uniform.getArrayLength();
  }
}
