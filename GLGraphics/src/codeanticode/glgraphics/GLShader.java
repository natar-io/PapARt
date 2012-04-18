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

import java.net.URL;
import javax.media.opengl.GL;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;

/**
 * This class defines the basic API of a shader (GLSL or Cg).
 */
abstract public class GLShader implements GLConstants {
  protected PApplet parent;
  protected PGraphicsOpenGL pgl;  
  protected GL gl;
  
  GLShader(PApplet parent) {
    this.parent = parent;
    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;    
  }
  
  public void delete() {}
  
  abstract public void loadVertexShader(String file);
  abstract public void loadVertexShader(URL url);
  abstract public void loadGeometryShader(String file);
  abstract public void loadGeometryShader(URL url);  
  abstract public void loadFragmentShader(String file);
  abstract public void loadFragmentShader(URL url);  
  abstract public boolean isInitialized();
  abstract public void setup();
  abstract public void start();
  abstract public void stop();
}
