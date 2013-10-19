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
 * This class stores the parameters for a texture: target, internal format,
 * minimization filter and magnification filter.
 */
public class GLTextureParameters implements GLConstants, PConstants {
  public int target;
  public int format;
  public int minFilter;
  public int magFilter;
  public int wrappingU;
  public int wrappingV;
  public int internalFormat = -1;

  /**
   * Creates an instance of GLTextureParameters, setting all the parameters to
   * default values.
   */
  public GLTextureParameters() {
    target = TEX_NORM;
    format = ARGB;
    minFilter = LINEAR_SAMPLING;
    magFilter = LINEAR_SAMPLING;
    wrappingU = CLAMP;
    wrappingV = CLAMP;
  }

  public GLTextureParameters(int format) {
    target = TEX_NORM;
    this.format = format;
    minFilter = LINEAR_SAMPLING;
    magFilter = LINEAR_SAMPLING;
    wrappingU = CLAMP;
    wrappingV = CLAMP;    
  }

  public GLTextureParameters(int format, int filter) {
    target = TEX_NORM;
    this.format = format;
    minFilter = filter;
    magFilter = filter;
    wrappingU = CLAMP;
    wrappingV = CLAMP;
  }

  public GLTextureParameters(int format, int filter, int internalFormat) {
    this(format, filter);
    this.internalFormat = internalFormat;
  }
}
