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
import com.sun.opengl.cg.CGparameter;
import com.sun.opengl.cg.CgGL;

public class GLCgShaderParameter extends GLShaderVariable  {
  protected CGparameter par;
  protected int program;

  /**
   * Creates an instance of GLCgShaderParameter using the specified parameters.
   * The shader is set to null.
   * 
   * @param parent PApplet
   * @param name String
   * @param label String
   * @param type int
   */
  public GLCgShaderParameter(PApplet parent, String name, String label, int type) {
    super(parent, null, name, label, type, 1);
    par = null;
    program = -1;
  }  
  
  /**
   * Creates an instance of GLCgShaderParameter using the specified parameters.
   * 
   * @param parent PApplet
   * @param shader GLShader
   * @param name String
   * @param label String
   * @param type int
   */
  public GLCgShaderParameter(PApplet parent, GLShader shader, String name,
      String label, int type) {
    super(parent, shader, name, label, type, 1);
    par = null;
    program = -1;   
  }  
  
  public void setProgramType(int type) {
    program = type;
  }
  
  public boolean available() {
    return shader != null &&  par != null && - 1 < program;
  } 

  public void init() {
    if (shader != null && - 1 < program)
      if (program == FRAGMENT_PROGRAM) {
        par = ((GLCgShader)shader).getFragmentParameter(name);  
      } else if (program == GEOMETRY_PROGRAM) {
        par = ((GLCgShader)shader).getGeometryParameter(name);
      } else if (program == VERTEX_PROGRAM) {
        par = ((GLCgShader)shader).getVertexParameter(name);
      }
  }  
  
  /**
   * Copies the parameter value to the GPU.
   */
  public void copyToShader() {
    if (par == null) {
      System.err.println("Error in texture filter parameter " + name
          + ": no valid Cg ID.");
      return;
    }
    if (type == SHADER_VAR_INT)
      CgGL.cgSetParameter1i(par, valueInt);
    else if (type == SHADER_VAR_FLOAT)
      CgGL.cgSetParameter1f(par, valueFloat);
    else if (type == SHADER_VAR_VEC2)
      CgGL.cgSetParameter2fv(par, valueArray, 0);
    else if (type == SHADER_VAR_VEC3)
      CgGL.cgSetParameter3fv(par, valueArray, 0);
    else if (type == SHADER_VAR_VEC4)
      CgGL.cgSetParameter4fv(par, valueArray, 0);
    else if (type == SHADER_VAR_MAT2)
      CgGL.cgSetMatrixParameterfc(par, valueArray, 0);
    else if (type == SHADER_VAR_MAT3)
      CgGL.cgSetMatrixParameterfc(par, valueArray, 0);
    else if (type == SHADER_VAR_MAT4)
      CgGL.cgSetMatrixParameterfc(par, valueArray, 0);
    else if (type == SHADER_VAR_MODELVIEW_MAT)
      CgGL.cgGLSetStateMatrixParameter(par, CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);  
    else if (type ==  SHADER_VAR_PROJECTION_MAT)    
      CgGL.cgGLSetStateMatrixParameter(par, CgGL.CG_GL_PROJECTION_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);  
    else if (type == SHADER_VAR_MODELVIEW_PROJECTION_MAT)
      CgGL.cgGLSetStateMatrixParameter(par, CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);  
    else if (type == SHADER_VAR_TEXTURE_MAT)
      CgGL.cgGLSetStateMatrixParameter(par, CgGL.CG_GL_TEXTURE_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
    else
      System.err.println("Error in texture filter parameter " + name
          + ": Unknown type.");
  }
}
