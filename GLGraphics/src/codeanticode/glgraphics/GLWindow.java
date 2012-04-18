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

abstract public class GLWindow {
  protected boolean visible;
  protected int x0, y0;
  protected int width, height;
  protected boolean override;
  protected boolean initialized;
  
  abstract public void init();
  
  abstract public void show();

  abstract public void hide();

  abstract public boolean isVisible();

  abstract public boolean ready();

  abstract public void render();

  /**
   * Returns the value of the override variable, used to skip automatic
   * rendering in the GLGraphics renderer.
   * 
   * @return boolean
   */
  public boolean getOverride() {
    return override;
  }
  
  /**
   * Sets the value of the override variable, used to skip automatic rendering
   * in the GLGraphics renderer.
   * 
   * @param val boolean
   */
  public void setOverride(boolean val) {
    override = val;
  }
  
  /**
   * Returns the value of the initialized variable.
   * 
   * @return boolean
   */  
  public boolean isInitialized() {
    return initialized;
  }
  
  
  
}
