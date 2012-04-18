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
import processing.opengl.*;

import javax.media.opengl.*;

/**
 * This class stores the variable of a GLSL shader (attribute or uniform). It
 * can be of type int, float, vec2, vec3, vec4, mat2, mat3 or mat4.
 */
abstract class GLShaderVariable implements GLConstants {
  protected GL gl;  
  protected GLShader shader;
  protected String name;
  protected String label;
  protected int type;
  protected int valueInt;
  protected float valueFloat;
  protected float[] valueArray;
  protected PApplet parent;
  protected int arrayLength;
    
  /**
   * Implicit constructor.
   */
  public GLShaderVariable() {
    gl = null;

    this.shader = null;
    this.parent = null;
    this.name = "";
    this.type = 0;
    this.label = "";
    this.arrayLength = 0;
  }

  /**
   * Creates an instance of GLShaderVariable using the specified parameters. The
   * shader is set to null.
   * 
   * @param parent PApplet
   * @param name String
   * @param label String
   * @param type int
   */
  public GLShaderVariable(PApplet parent, String name, String label, int type, int len) {
    this(parent, null, name, label, type, len);
  }

  /**
   * Creates an instance of GLShaderVariable using the specified parameters.
   * 
   * @param parent PApplet
   * @param shader GLShader
   * @param name String
   * @param label String
   * @param type int
   * @param len int 
   */
  public GLShaderVariable(PApplet parent, GLShader shader, String name,
      String label, int type, int len) {
    PGraphicsOpenGL pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;

    this.shader = shader;
    this.parent = parent;
    this.name = name;
    this.type = type;
    this.label = label;
    this.arrayLength = len;
    allocateValueArray();
  }  

  /**
   * Sets the shader this parameter corresponds to.
   * 
   * @param shader GLShader
   */
  public void setShader(GLShader shader) {
    this.shader = shader;
  }

  /**
   * Returns true or false depending on whether this variable is available for use.
   * @return boolean
   */  
  abstract public boolean available();
  
  /**
   * Initializes this shader variable.
   */
  abstract public void init();

  /**
   * Sets the parameter value when the type is int.
   * 
   * @param value  int
   */
  public void setValue(int value) {
    if (type == SHADER_VAR_INT)
      valueInt = value;
    else
      System.err.println("Error in shader variable " + name + ": Wrong type.");
  }

  /**
   * Sets the parameter value when the type is float.
   * 
   * @param value float
   */
  public void setValue(float value) {
    if (type == SHADER_VAR_FLOAT)
      valueFloat = value;
    else
      System.err.println("Error in shader variable " + name + ": Wrong type.");
  }

  /**
   * Sets the parameter value for any type. When the type is int or float, the
   * first element of the value array is considered.
   * 
   * @param value float[]
   */
  public void setValue(float[] value) {
    int l = value.length;
    if ((l == 1)
        && ((type == SHADER_VAR_INT) || (type == SHADER_VAR_FLOAT))) {
      if (type == SHADER_VAR_INT)
        valueInt = (int) value[0];
      else
        valueFloat = value[0];
    } else if (((l == 2) && (type == SHADER_VAR_VEC2))
        || ((l == 3) && (type == SHADER_VAR_VEC3))
        || ((l == 4) && (type == SHADER_VAR_VEC4))
        || ((l == 4) && (type == SHADER_VAR_MAT2))
        || ((l == 9) && (type == SHADER_VAR_MAT3))
        || ((l == 16) && (type == SHADER_VAR_MAT4))
        || ((l == arrayLength) && (type == SHADER_VAR_ARRAY)))    
      PApplet.arrayCopy(value, valueArray);
    else
      System.err.println("Error in shader variable " + name + ": Wrong type.");
  }

  /**
   * Sets the ith value for the parameter (only valid for vec or mat types).
   * 
   * @param int i
   * @param value float
   */
  public void setValue(int i, float value) {
    if (((i < 2) && (type == SHADER_VAR_VEC2))
        || ((i < 3) && (type == SHADER_VAR_VEC3))
        || ((i < 4) && (type == SHADER_VAR_VEC4))
        || ((i < 4) && (type == SHADER_VAR_MAT2))
        || ((i < 9) && (type == SHADER_VAR_MAT3))
        || ((i < 16) && (type == SHADER_VAR_MAT4))
        || ((i < arrayLength) && (type == SHADER_VAR_ARRAY))) {
      valueArray[i] = value;
    } else
      System.err.println("Error in shader variable " + name + ": Wrong type.");
  }

  /**
   * Sets the (ith, jth) value for the parameter (only valid for mat types).
   * 
   * @param int i
   * @param int j
   * @param value float
   */
  public void setValue(int i, int j, float value) {
    if ((i < 2) && (j < 2) && (type == SHADER_VAR_MAT2))
      valueArray[2 * i + j] = value;
    else if ((i < 3) && (j < 3) && (type == SHADER_VAR_MAT3))
      valueArray[3 * i + j] = value;
    else if ((i < 4) && (j < 4) && (type == SHADER_VAR_MAT3))
      valueArray[4 * i + j] = value;
    else
      System.err.println("Error in shader variable " + name + ": Wrong type.");
  }

  /**
   * Copies variable values to shader.
   */
  abstract public void copyToShader();  
  
  /**
   * Returns the int constant that identifies a type, given the corresponding
   * string.
   * 
   * @param String typeStr
   * @return int
   */
  public static int getType(String typeStr) {
    if (typeStr.equals("int"))
      return SHADER_VAR_INT;
    else if (typeStr.equals("float"))
      return SHADER_VAR_FLOAT;
    else if (typeStr.equals("vec2"))
      return SHADER_VAR_VEC2;
    else if (typeStr.equals("vec3"))
      return SHADER_VAR_VEC3;
    else if (typeStr.equals("vec4"))
      return SHADER_VAR_VEC4;
    else if (typeStr.equals("mat2"))
      return SHADER_VAR_MAT2;
    else if (typeStr.equals("mat3"))
      return SHADER_VAR_MAT3;
    else if (typeStr.equals("mat4"))
      return SHADER_VAR_MAT4;
    else if (typeStr.equals("texsampler"))
      return SHADER_VAR_TEXTURE_SAMPLER;    
    else if (typeStr.equals("modelview"))
      return SHADER_VAR_MODELVIEW_MAT;
    else if (typeStr.equals("projection"))
      return SHADER_VAR_PROJECTION_MAT;
    else if (typeStr.equals("modelview_projection"))
      return SHADER_VAR_MODELVIEW_PROJECTION_MAT;
    else if (typeStr.equals("texmatrix"))
      return SHADER_VAR_TEXTURE_MAT;
    else if (typeStr.equals("array"))
      return SHADER_VAR_ARRAY;    
    else
      return -1;
  }

  /**
   * Returns parameter type.
   * 
   * @return int
   */
  public int getType() {
    return type;
  }

  /**
   * Returns parameter name.
   * 
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * Returns parameter label.
   * 
   * @return String
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns array length.
   * 
   * @return String
   */
  public int getArrayLength() {
    return arrayLength;
  }  
  
  /**
   * Allocates valueArray to the size corresponding the the parameter type.
   */
  protected void allocateValueArray() {
    if (type == SHADER_VAR_VEC2)
      valueArray = new float[2];
    else if (type == SHADER_VAR_VEC3)
      valueArray = new float[3];
    else if (type == SHADER_VAR_VEC4)
      valueArray = new float[4];
    else if (type == SHADER_VAR_MAT2)
      valueArray = new float[4];
    else if (type == SHADER_VAR_MAT3)
      valueArray = new float[9];
    else if (type == SHADER_VAR_MAT4)
      valueArray = new float[16];
    else if (type == SHADER_VAR_ARRAY)
      valueArray = new float[arrayLength];
    else
      valueArray = null;
  }
}
