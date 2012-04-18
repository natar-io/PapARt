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
 * This class stores the (uniform) parameter for a model effect.
 */
public class GLModelEffectParameter extends GLSLShaderUniform {
  /**
   * Creates an instance of GLModelEffectParameter using the specified
   * parameters. The shader is set to null.
   * 
   * @param parent PApplet
   * @param name String
   * @param label String
   * @param type int
   * @param len int
   */
  public GLModelEffectParameter(PApplet parent, String name, String label,
      int type, int len) {
    super(parent, null, name, label, type, len);
  }

  /**
   * Creates an instance of GLModelEffectParameter using the specified
   * parameters.
   * 
   * @param parent PApplet
   * @param shader GLSLShader
   * @param name String
   * @param label String
   * @param type int
   * @param len int
   */
  public GLModelEffectParameter(PApplet parent, GLSLShader shader, String name,
      String label, int type, int len) {
    super(parent, shader, name, label, type, len);
  }
}
