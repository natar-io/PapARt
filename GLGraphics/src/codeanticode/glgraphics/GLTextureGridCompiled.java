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

import com.sun.opengl.util.*;
import java.nio.*;

/**
 * @invisible This class defines a 2D grid to map and distort textures. The
 *            vertices stored in a static Vertex Buffer Object in GPU memory.
 */
public class GLTextureGridCompiled extends GLTextureGrid {
  protected boolean built;
  protected int mode;
  protected int width, height;
  protected int resX, resY; // Number of points in the grid, along each
                            // direction.
  protected GLTexturedPoint[] points; // Number of points in the grid, along
                                      // each direction.
  protected int numLayers; // Number of layers. Each layer is one textured point
                           // assigned to each node of the grid.

  protected int vertCoordsVBO;
  protected int[] texCoordsVBO;
  protected int numVertices;
  protected int numTexCoords;
  protected int numTextures;  
  
  public GLTextureGridCompiled(GL gl) {
    super(gl);
    initGrid();

    width = height = 0;
    built = false;
  }

  public void delete() {
    releaseGrid();
  }

  public GLTextureGridCompiled(GL gl, XMLElement xml) {
    super(gl);
    initGrid(xml);

    width = height = 0;
    built = false;
  }

  public void render(int sW, int sH, int dW, int dH, int l) {
    if (!built || (usingSrcTexRes && ((sW != width) || (sH != height)))
        || (!usingSrcTexRes && ((dW != width) || (dH != height))))
      if (usingSrcTexRes)
        buildGrid(sW, sH);
      else
        buildGrid(dW, dH);

    // Enable VBO Pointers
    gl.glEnableClientState(GL.GL_VERTEX_ARRAY); // Enable Vertex Arrays
    gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY); // Enable Texture Coord
                                                       // Arrays

    for (int n = 0; n < numTextures; n++) {
      gl.glClientActiveTexture(GL.GL_TEXTURE0 + n);
      gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsVBO[n]);
      gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0); // Set The TexCoord Pointer To
                                                  // The TexCoord Buffer
    }

    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertCoordsVBO);
    gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0); // Set The Vertex Pointer To The
                                              // Vertex Buffer

    gl.glDrawArrays(mode, 0, numVertices);

    // Disable Vertex Arrays
    gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    // Disable Texture Coord Arrays
    gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
  }

  // Creates a 1x1 grid (just 2 points on each direction).
  protected void initGrid() {
    numLayers = 1;
    resX = resY = 2;
    mode = GL.GL_POINTS;

    points = new GLTexturedPoint[1];
    points[0] = new GLTexturedPoint();
    points[0].setAsUndefined();
  }

  /*
   * The format of the grid parameters is the following: <grid> <resolution
   * nx=10 ny=10></resolution> <point> <coord x="0.5" y="+0.5"></coord>
   * <texcoord s="0.0" t="1.0"></texcoord> </point> <point> <coord x="x"
   * y="y"></coord> <texcoord s="s" t="t"></texcoord> </point> </grid>
   */
  protected void initGrid(XMLElement xml) {
    int n = xml.getChildCount();

    // if there is only one parameter line, it should contain the
    // grid dimensions.
    if (n == 1) {
      numLayers = 1;
    } else {
      numLayers = n - 1;
    }

    points = new GLTexturedPoint[numLayers];

    XMLElement child;
    String name, wStr, hStr, modeName;
    int ptLayer = 0;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("resolution")) {
        wStr = child.getStringAttribute("nx");
        hStr = child.getStringAttribute("ny");

        usingSrcTexRes = false;

        if (wStr.equals("w"))
          resX = 0;
        else if (wStr.equals("w0"))
          resX = -1;
        else if (wStr.equals("w1"))
          resX = -2;
        else if (wStr.equals("w2"))
          resX = -3;
        else if (wStr.equals("w3"))
          resX = -4;
        else if (wStr.equals("w4"))
          resX = -5;
        else if (wStr.equals("w5"))
          resX = -6;
        else
          resX = child.getIntAttribute("nx");

        if (hStr.equals("h"))
          resY = 0;
        else if (hStr.equals("h0"))
          resY = -1;
        else if (hStr.equals("h1"))
          resY = -2;
        else if (hStr.equals("h2"))
          resY = -3;
        else if (hStr.equals("h3"))
          resY = -4;
        else if (hStr.equals("h4"))
          resY = -5;
        else if (hStr.equals("h5"))
          resY = -6;
        else
          resY = child.getIntAttribute("ny");

        if ((resX <= 0) || (resY <= 0))
          if (resX != resY)
            System.err.println("Source width and height are different!");
          else if (resX < 0) {
            usingSrcTexRes = true;
            srcTexIdx = -(resX + 1);
          }

        modeName = child.getStringAttribute("mode");
        mode = GLUtils.parsePrimitive(modeName);
      } else if (name.equals("point")) {
        points[ptLayer] = new GLTexturedPoint(child);
        ptLayer++;
      }
    }

    if (n == 1) {
      points[0] = new GLTexturedPoint();
      points[0].setAsUndefined();
    }
  }

  protected void buildGrid(int w, int h) {
    int i, j, k, n;
    int numX, numY;

    if (built) {
      releaseGrid();
    }

    numX = resX;
    numY = resY;

    if (numX <= 0)
      numX = w;
    if (numY <= 0)
      numY = h;

    numTextures = 100;

    for (k = 0; k < numLayers; k++) {
      if (points[k].s.length < numTextures)
        numTextures = points[k].s.length;
    }

    numVertices = numX * numY * numLayers;
    numTexCoords = numX * numY * numLayers;

    FloatBuffer vertCoordsBuffer;
    FloatBuffer[] texCoordsBuffer;

    vertCoordsBuffer = BufferUtil.newFloatBuffer(numVertices * 3);
    texCoordsBuffer = new FloatBuffer[numTextures];
    for (n = 0; n < numTextures; n++)
      texCoordsBuffer[n] = BufferUtil.newFloatBuffer(numTexCoords * 2);

    float x, y, s0, t0, s, t;
    float dx, dy, ds0, dt0, ds, dt;

    if ((mode == GL.GL_LINE_STRIP) || (mode == GL.GL_LINE_LOOP)
        || (mode == GL.GL_TRIANGLE_STRIP) || (mode == GL.GL_TRIANGLE_FAN)
        || (mode == GL.GL_QUAD_STRIP)) {
      if (1 < numX)
        ds0 = 1.0f / (numX - 1);
      else
        ds0 = 1.0f;
      if (1 < numY)
        dt0 = 1.0f / (numY - 1);
      else
        dt0 = 1.0f;
    } else {
      ds0 = 1.0f / numX;
      dt0 = 1.0f / numY;
    }

    for (j = 0; j < numY; j++)
      for (i = 0; i < numX; i++)
        for (k = 0; k < numLayers; k++) {
          s0 = (float) i * ds0;
          t0 = (float) j * dt0;

          x = points[k].x;
          y = points[k].y;
          dx = points[k].dx;
          dy = points[k].dy;

          if (x == -1.0f)
            x = s0 * w;
          if (y == -1.0f)
            y = t0 * h;
          if (dx == -1.0f)
            dx = ds0 * w;
          if (dy == -1.0f)
            dy = dt0 * h;

          vertCoordsBuffer.put(x + dx);
          vertCoordsBuffer.put(y + dy);
          vertCoordsBuffer.put(0.0f);

          for (n = 0; n < numTextures; n++) {
            s = points[k].s[n];
            t = points[k].t[n];
            ds = points[k].ds[n];
            dt = points[k].dt[n];

            if (s == -1.0f)
              s = s0;
            if (t == -1.0f)
              t = t0;
            if (ds == -1.0f)
              ds = ds0;
            if (dt == -1.0f)
              dt = dt0;

            texCoordsBuffer[n].put(s + ds);
            texCoordsBuffer[n].put(t + dt);
          }
        }

    vertCoordsBuffer.flip();
    for (n = 0; n < numTextures; n++)
      texCoordsBuffer[n].flip();
    
    texCoordsVBO = new int[numTextures];
    
    // Generate and bind The Vertex Buffer Object.
    vertCoordsVBO = GLState.createGLResource(GL_VERTEX_BUFFER); // Get a valid OpenGL ID.
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertCoordsVBO);  // Bind the
                                                                // buffer.
    gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, numVertices * 3
        * BufferUtil.SIZEOF_FLOAT, vertCoordsBuffer, GL.GL_STATIC_DRAW_ARB); // Load
                                                                             // the
                                                                             // data.

    // Generate and bind the Texture Coordinate Buffers
    for (n = 0; n < numTextures; n++) {
      texCoordsVBO[n] = GLState.createGLResource(GL_VERTEX_BUFFER);
      gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsVBO[n]); // Bind the
                                                                   // buffer.
      gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, numTexCoords * 2
          * BufferUtil.SIZEOF_FLOAT, texCoordsBuffer[n], GL.GL_STATIC_DRAW_ARB); // Load
                                                                                 // the
                                                                                 // data.
    }

    built = true;
    width = w;
    height = h;
  }
  
  protected void releaseGrid() {
    if (vertCoordsVBO != 0) {
      GLState.deleteGLResource(vertCoordsVBO, GL_VERTEX_BUFFER);
    }

    if (0 < numTextures) {
      for (int i = 0; i < numTextures; i++) {
        GLState.deleteGLResource(texCoordsVBO[i], GL_VERTEX_BUFFER);
      }
    }
  }
}
