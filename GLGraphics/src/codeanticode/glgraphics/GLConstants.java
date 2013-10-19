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

/**
 * This interface class defines constants used in the glgraphics package.
 */
public interface GLConstants {
  /**
   * This constant identifies the GLGraphics renderer.
   */
  static final String GLGRAPHICS = "codeanticode.glgraphics.GLGraphics";

  /**
   * This constant identifies the texture target GL_TEXTURE_2D, that is,
   * textures with normalized coordinates.
   */
  public static final int TEX_NORM = 0;
  /**
   * This constant identifies the texture target GL_TEXTURE_RECTANGLE, that is,
   * textures with non-normalized coordinates
   */
  public static final int TEX_RECT = 1;
  /**
   * This constant identifies the texture target GL_TEXTURE_1D, that is,
   * one-dimensional textures.
   */
  public static final int TEX_ONEDIM = 2;

  /**
   * This constant identifies the texture internal format GL_RGBA: 4 color
   * components of 8 bits each, identical as ARGB.
   */
  public static final int COLOR = 2;
  /**
   * This constant identifies the texture internal format GL_RGBA16F_ARB: 4
   * float compontents of 16 bits each.
   */
  public static final int FLOAT = 6;
  /**
   * This constant identifies the texture internal format GL_RGBA32F_ARB: 4
   * float compontents of 32 bits each.
   */
  public static final int DOUBLE = 7;

  /**
   * This constant identifies an image buffer that contains only RED channel
   * info.
   */
  // public static final int RED = 0;

  /**
   * This constant identifies an image buffer that contains only GREEN channel
   * info.
   */
  // public static final int GREEN = 0;

  /**
   * This constant identifies an image buffer that contains only BLUE channel
   * info.
   */
  // public static final int BLUE = 0;
  /**
   * This constant identifies an image buffer that contains only ALPHA channel
   * info.
   */
  // public static final int ALPHA = 0; Already defined in Processing with value
  // = 4

  /**
   * This constant identifies an integer texture buffer.
   */
  public static final int TEX_INT = 0;
  /**
   * This constant identifies an unsigned byte texture buffer.
   */
  public static final int TEX_BYTE = 1;

  /**
   * This constant identifies the nearest texture filter .
   */
  public static final int NEAREST_SAMPLING = 0;
  /**
   * This constant identifies the linear texture filter .
   */
  public static final int LINEAR_SAMPLING = 1;
  /**
   * This constant identifies the nearest/nearest function to build mipmaps .
   */
  public static final int NEAREST_MIPMAP_NEAREST = 2;
  /**
   * This constant identifies the linear/nearest function to build mipmaps .
   */
  public static final int LINEAR_MIPMAP_NEAREST = 3;
  /**
   * This constant identifies the nearest/linear function to build mipmaps .
   */
  public static final int NEAREST_MIPMAP_LINEAR = 4;
  /**
   * This constant identifies the linear/linear function to build mipmaps .
   */
  public static final int LINEAR_MIPMAP_LINEAR = 5;
  
  public static final int LINE_ADJACENCY = 20;
  public static final int TRIANGLE_ADJACENCY = 21;  

  /**
   * These constants identifies the shader variable types.
   */
  public static final int SHADER_VAR_INT = 0;
  public static final int SHADER_VAR_FLOAT = 1;
  public static final int SHADER_VAR_VEC2 = 2;
  public static final int SHADER_VAR_VEC3 = 3;
  public static final int SHADER_VAR_VEC4 = 4;
  public static final int SHADER_VAR_MAT2 = 5;
  public static final int SHADER_VAR_MAT3 = 6;
  public static final int SHADER_VAR_MAT4 = 7;
  public static final int SHADER_VAR_ARRAY = 8;
  public static final int SHADER_VAR_TEXTURE_SAMPLER = 9;
  public static final int SHADER_VAR_MODELVIEW_MAT = 10;
  public static final int SHADER_VAR_PROJECTION_MAT = 11;
  public static final int SHADER_VAR_MODELVIEW_PROJECTION_MAT = 12;
  public static final int SHADER_VAR_TEXTURE_MAT = 13;

  /**
   * Matrix operations used in Cg.
   */  
  public static final int IDENTITY_MATRIX = 0;
  public static final int INVERSE_MATRIX = 1;
  public static final int TRANSPOSE_MATRIX = 2;
  public static final int INVERSE_TRANSPOSE_MATRIX = 3;    
  
  /**
   * These constants identifies the shader program types.
   */  
  public static final int VERTEX_PROGRAM = 0;
  public static final int FRAGMENT_PROGRAM = 1;
  public static final int GEOMETRY_PROGRAM = 2;
  
  public static final int GL_DEPTH_STENCIL = 0x84F9;
  public static final int GL_UNSIGNED_INT_24_8 = 0x84FA;
  public static final int GL_DEPTH24_STENCIL8 = 0x88F0;

  public static final int BACKGROUND_ALPHA = 16384;
  
  /**
   * OpenGL resources.
   */    
  public static final int GL_TEXTURE_OBJECT = 0;
  public static final int GL_VERTEX_BUFFER = 1;
  public static final int GL_PIXEL_BUFFER = 1;
  public static final int GL_FRAME_BUFFER = 2;
  public static final int GL_RENDER_BUFFER = 3;
  public static final int GLSL_PROGRAM = 4;
  public static final int GLSL_SHADER = 5;
  public static final int CG_CONTEXT = 6;
  public static final int CG_PROGRAM = 7;
  public static final int CG_EFFECT = 8;
}
