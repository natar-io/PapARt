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

import processing.xml.*;

import javax.media.opengl.*;

/**
 * @invisible This class defines a 2D grid to map and distort textures. The
 *            vertices are pushed in direct mode, this is, using the glVertex2f
 *            function.
 */
public class GLTextureGridDirect extends GLTextureGrid {
  protected int resX, resY; // Number of points in the grid, along each
                            // direction.
  protected int numLayers; // Number of layers. Layer 0 stores the actual grid,
                           // the other layers store the texture coordinates.
  protected float[] gridDX;
  protected float[] gridDY;
  
  public GLTextureGridDirect(GL gl) {
    super(gl);
    initGrid();
  }

  public GLTextureGridDirect(GL gl, XMLElement xml) {
    super(gl);
    initGrid(xml);
  }

  public void render(int sW, int sH, int dW, int dH, int l) {
    GLTexturedPoint p0, p1;
    p0 = new GLTexturedPoint();
    p1 = new GLTexturedPoint();

    for (int j = 0; j < resY - 1; j++) {
      gl.glBegin(GL.GL_QUAD_STRIP);
      for (int i = 0; i < resX; i++) {
        getPoint(0, i, j, p0);
        getPoint(0, i, j + 1, p1);

        p0.scaleCoords(dW, dH);
        p1.scaleCoords(dW, dH);

        setTexCoord(i, j, l);
        gl.glVertex2f(p0.x, p0.y);

        setTexCoord(i, j + 1, l);
        gl.glVertex2f(p1.x, p1.y);
      }
      gl.glEnd();
    }
  }

  private void getPoint(int layer, int i, int j, GLTexturedPoint tp) {
    if (layer < numLayers) {
      tp.s[0] = gridDX[layer];
      tp.t[0] = gridDY[layer];
    } else {
      // If there are not layers defined below the 0th, then the texture
      // coordinates
      // are set identical to the grid coordinates.
      tp.s[0] = gridDX[0];
      tp.t[0] = gridDY[0];
    }
    tp.x = i * tp.s[0];
    tp.y = j * tp.t[0];
  }

  protected void setTexCoord(int i, int j, int l) {
    GLTexturedPoint p = new GLTexturedPoint();
    for (int k = 1; k <= l; k++) {
      getPoint(k, i, j, p);
      gl.glMultiTexCoord2f(GL.GL_TEXTURE0 + k - 1, p.x, p.y);
    }
  }

  // Creates a 1x1 grid (just 2 points on each direction).
  protected void initGrid() {
    numLayers = 1;
    gridDX = new float[numLayers];
    gridDY = new float[numLayers];

    resX = resY = 2;

    gridDX[0] = 1.0f / (resX - 1); // i.e.: 1.0
    gridDY[0] = 1.0f / (resY - 1); // i.e.: 1.0
  }

  /**
   * The format of the grid parameters is the following: <grid> <resolution
   * nx=10 ny=10></resolution> <spacing dx=0.1 dy=0.1></spacing> <spacing dx=0.2
   * dy=0.2></spacing> .... <spacing dx=dxn dy=dyn></spacing> </grid> there nx
   * and ny are the number of grid quads along x and y, and dxi, dyi are the
   * grid spacings on each layer (the layer 0 represents the grid itself, the
   * rest of the layers represent the texture coordinates).
   */
  protected void initGrid(XMLElement xml) {
    int n = xml.getChildCount();

    // if there is only one parameter line, it should contain the
    // grid dimensions. The 0th layer is default to the grid spacing
    // corresponding to 1/(number of grid quads)
    if (n == 1) {
      numLayers = 1;
    } else {
      numLayers = n - 1;
    }

    // The number of layers should be less or equal than the maximum number of
    // textures supported by the video card.
    gridDX = new float[numLayers];
    gridDY = new float[numLayers];

    XMLElement child;
    String name;
    int texLayer = 0;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("resolution")) {
        resX = child.getIntAttribute("nx");
        resY = child.getIntAttribute("ny");
      } else if (name.equals("spacing")) {
        gridDX[texLayer] = child.getFloatAttribute("dx");
        gridDY[texLayer] = child.getFloatAttribute("dy");
        texLayer++;
      }
    }

    if (n == 1) {
      // No texture coordinates where specified in the xml config.
      // Using this default spacing:
      gridDX[0] = 1.0f / (resX - 1);
      gridDY[0] = 1.0f / (resY - 1);
    }
  }
}
