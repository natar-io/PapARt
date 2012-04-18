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
 * This class encapsulates two GLTextures, that are labeled as read and write.
 * The role of each texture is swaped upon calling the swap function. This class
 * is intended to simplify the code in GP-GPU applications, where there is the
 * need to apply iterative operations on textures holding float data that
 * represents coordinates or velocities of particles, etc. This technique of
 * alternatively swaping the textures to allow for read/write operations is
 * called "Ping-Pong".
 */
public class GLTexturePingPong {
  protected GLTexture[] textures;
  protected int readTex;
  protected int writeTex;  
  
  /**
   * The constructor of the class. Sets the two textures to use.
   * 
   * @param tex0 GLTexture
   * @param tex1 GLTexture
   */
  public GLTexturePingPong(GLTexture tex0, GLTexture tex1) {
    readTex = 0;
    writeTex = 1;

    textures = new GLTexture[2];
    textures[0] = tex0;
    textures[1] = tex1;
  }

  /**
   * Returns the current read texture.
   * 
   * @return GLTexture
   */
  public GLTexture getReadTex() {
    return textures[readTex];
  }

  /**
   * Sets the value for the read texture.
   * 
   * @param idx int
   */
  public void setReadTex(int idx) {
    if ((idx == 0) || (idx == 1))
      readTex = idx;
  }

  /**
   * Returns the current write texture.
   * 
   * @return GLTexture
   */
  public GLTexture getWriteTex() {
    return textures[writeTex];
  }

  /**
   * Sets the value for the write texture.
   * 
   * @param idx int
   */
  public void setWriteTex(int idx) {
    if ((idx == 0) || (idx == 1))
      writeTex = idx;
  }

  /**
   * Returns the current read texture.
   * 
   * @return GLTexture
   */
  public GLTexture getOldTex() {
    return getReadTex();
  }

  /**
   * Sets the value for the read texture.
   * 
   * @param idx int
   */
  public void setOldTex(int idx) {
    setReadTex(idx);
  }

  /**
   * Returns the current write texture.
   * 
   * @return GLTexture
   */
  public GLTexture getNewTex() {
    return getWriteTex();
  }

  /**
   * Sets the value for the write texture.
   * 
   * @param idx int
   */
  public void setNewTex(int idx) {
    setWriteTex(idx);
  }

  /**
   * Inits the read and write indices.
   */
  public void init() {
    readTex = 0;
    writeTex = 1;
  }

  /**
   * Swaps the two textures, read becomes write and viceversa.
   */
  public void swap() {
    int t = readTex;
    readTex = writeTex;
    writeTex = t;
  }
}
