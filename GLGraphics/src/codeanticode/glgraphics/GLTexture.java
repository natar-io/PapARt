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

import com.sun.opengl.util.*;

import java.lang.reflect.Method;
import java.nio.*;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * This class adds an opengl texture to a PImage object. The texture is handled
 * in a similar way to the pixels property: image data can be copied to and from
 * the texture using loadTexture and updateTexture methods. However, bringing
 * the texture down to image or pixels data can slow down the application
 * considerably (since involves copying texture data from GPU to CPU),
 * especially when handling large textures. So it is recommended to do all the
 * texture handling without calling updateTexture, and doing so only at the end
 * if the texture is needed as a regular image.
 */
public class GLTexture extends PImage implements PConstants, GLConstants {
  protected GL gl;
  protected PGraphicsOpenGL pgl;
  protected int tex = 0;
  protected int texTarget;
  protected int texInternalFormat;
  
  protected int texUnit;
  protected int texUniform;
  
  protected int minFilter;
  protected int magFilter;
  protected int wrapModeS;
  protected int wrapModeT;  
  protected boolean usingMipmaps;
  protected float maxTexCoordS;
  protected float maxTexCoordT;
  protected boolean flippedX;
  protected boolean flippedY;
  protected int pbo = 0;
  protected int pboTarget;
  protected int pboUsage;
  protected GLState glstate;
  protected String name = "";
  
  // For direct buffer copy.
  protected Method disposePixelsMethod = null;
  protected Object diposePixelsHandler = null;
  protected LinkedList<PixelData> pixelBuffer = null;  
  protected int maxBuffSize = 3;
  protected boolean disposeFramesWhenPixelBufferFull = false;

  /**
   * Default constructor.
   */  
  public GLTexture() {
    super();
  }
  
  /**
   * Creates an instance of GLTexture with size 1x1. The texture is not
   * initialized.
   * 
   * @param parent PApplet
   */
  public GLTexture(PApplet parent) {
    super(1, 1, ARGB);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);
    setTextureParams(new GLTextureParameters());
  }

  /**
   * Creates an instance of GLTexture with size width x height. The texture is
   * initialized (empty) to that size.
   * 
   * @param parent  PApplet
   * @param width int
   * @param height int
   */
  public GLTexture(PApplet parent, int width, int height) {
    super(width, height, ARGB);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);
    setTextureParams(new GLTextureParameters());

    initTexture(width, height);
  }

  /**
   * Creates an instance of GLTexture with size width x height and with the
   * specified parameters. The texture is initialized (empty) to that size.
   * 
   * @param parent PApplet
   * @param width int
   * @param height int
   * @param params GLTextureParameters
   */
  public GLTexture(PApplet parent, int width, int height,
      GLTextureParameters params) {
    super(width, height, params.format);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);
    setTextureParams(params);
    initTexture(width, height);
  }

  /**
   * Creates an instance of GLTexture with size width x height and with the
   * specified format. The texture is initialized (empty) to that size.
   * 
   * @param parent PApplet
   * @param width int
   * @param heigh int
   * @param format int
   */
  public GLTexture(PApplet parent, int width, int height, int format) {
    super(width, height, format);
    this.parent = parent;
    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);
    setTextureParams(new GLTextureParameters(format));
    initTexture(width, height);
  }

  /**
   * Creates an instance of GLTexture with size width x height and with the
   * specified format and filtering. The texture is initialized (empty) to that
   * size.
   * 
   * @param parent PApplet
   * @param width int
   * @param height int
   * @param format int
   * @param filter int
   */
  public GLTexture(PApplet parent, int width, int height, int format, int filter) {
    super(width, height, format);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);
    setTextureParams(new GLTextureParameters(format, filter));

    initTexture(width, height);
  }

  /**
   * Creates an instance of GLTexture using image file filename as source.
   * 
   * @param parent PApplet
   * @param filename String
   */
  public GLTexture(PApplet parent, String filename) {
    super(1, 1, ARGB);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);

    loadTexture(filename);
  }

  public GLTexture(PApplet parent, int width, int height,
      GLTextureParameters params, int id) {
    super(width, height, params.format);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);
    setTextureParams(params);

    initTexture(width, height, id);
  }

  /**
   * Creates an instance of GLTexture using image file filename as source and
   * the specified texture parameters.
   * 
   * @param parent PApplet
   * @param filename String
   * @param params GLTextureParameters
   */
  public GLTexture(PApplet parent, String filename, GLTextureParameters params) {
    super(1, 1, params.format);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);

    loadTexture(filename, params);
  }

  /**
   * Creates an instance of GLTexture using image file filename as source and
   * the specified format.
   * 
   * @param parent PApplet
   * @param filename String
   * @param format int
   */
  public GLTexture(PApplet parent, String filename, int format) {
    super(1, 1, format);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);

    loadTexture(filename, format);
  }

  /**
   * Creates an instance of GLTexture using image file filename as source and
   * the specified format and filtering.
   * 
   * @param parent PApplet
   * @param filename String
   * @param format int
   * @param filter int
   */
  public GLTexture(PApplet parent, String filename, int format, int filter) {
    super(1, 1, format);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);

    loadTexture(filename, format, filter);
  }

  /**
   * Creates an instance of GLTexture with power-of-two width and height that
   * such that width * height is the closest to size. The texture is initialized
   * (empty) to that size.
   * 
   * @param parent PApplet
   * @param size int
   */
  public GLTexture(PApplet parent, int size) {
    super(1, 1, ARGB);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);

    calculateWidthHeight(size);
    init(width, height);
  }

  /**
   * Creates an instance of GLTexture with power-of-two width and height that
   * such that width height is the closest to size, and with the specified
   * parameters. The texture is initialized (empty) to that size.
   * 
   * @param parent PApplet
   * @param size int
   * @param params GLTextureParameters
   */
  public GLTexture(PApplet parent, int size, GLTextureParameters params) {
    super(1, 1, params.format);
    this.parent = parent;

    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);

    calculateWidthHeight(size);
    init(width, height, params);
  }

  public void delete() {
    if (tex != 0) {
      releaseTexture();
    }
    
    if (pixelBuffer != null && 0 < pixelBuffer.size() && disposePixelsMethod != null) {
      while (0 < pixelBuffer.size()) {
        PixelData data = pixelBuffer.remove(0);
        data.dispose();
      }
    }
  }

  /**
   * Sets the size of the image and texture to width x height. If the texture is
   * already initialized, it first destroys the current opengl texture object
   * and then creates a new one with the specified size.
   * 
   * @param width int
   * @param height int
   */
  public void init(int width, int height) {
    init(width, height, new GLTextureParameters());
  }

  /**
   * Sets the size of the image and texture to width x height, and the
   * parameters of the texture to params. If the texture is already initialized,
   * it first destroys the current opengl texture object and then creates a new
   * one with the specified size.
   * 
   * @param width int
   * @param height int
   * @param params GLTextureParameters
   */
  public void init(int width, int height, GLTextureParameters params) {
    super.init(width, height, params.format);
    setTextureParams(params);
    initTexture(width, height);
  }

  /**
   * Returns true if the texture has been initialized.
   * 
   * @return boolean
   */
  public boolean available() {
    return 0 < tex;
  }

  /**
   * Returns the name of the texture.
   * 
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets the name of the texture.
   * 
   */  
  public void setName(String str) {
    name = str;
  }
  
  /**
   * Provides the ID of the opegl texture object.
   * 
   * @return int
   */
  public int getTextureID() {
    return tex;
  }

  /**
   * Returns the texture unit this texture is currently
   * bound to.
   * 
   * @return int
   */  
  public int getTextureUnit() {
    return texUnit;
  }

  /**
   * Returns the texture target.
   * 
   * @return int
   */
  public int getTextureTarget() {
    return texTarget;
  }

  /**
   * Returns the texture internal format.
   * 
   * @return int
   */
  public int getTextureInternalFormat() {
    return texInternalFormat;
  }

  /**
   * Returns the texture minimization filter.
   * 
   * @return int
   */
  public int getTextureMinFilter() {
    return minFilter;
  }

  /**
   * Returns the texture magnification filter.
   * 
   * @return int
   */
  public int getTextureMagFilter() {
    return magFilter;
  }

  /**
   * Returns true or false whether or not the texture is using mipmaps.
   * 
   * @return boolean
   */
  public boolean usingMipmaps() {
    return usingMipmaps;
  }

  /**
   * Returns the maximum possible value for the texture coordinate S.
   * 
   * @return float
   */
  public float getMaxTextureCoordS() {
    return maxTexCoordS;
  }

  /**
   * Returns the maximum possible value for the texture coordinate T.
   * 
   * @return float
   */
  public float getMaxTextureCoordT() {
    return maxTexCoordT;
  }

  /**
   * Returns true if the texture is flipped along the horizontal direction.
   * 
   * @return boolean;
   */
  public boolean isFlippedX() {
    return flippedX;
  }

  /**
   * Sets the texture as flipped or not flipped on the horizontal direction.
   * 
   * @param v boolean;
   */
  public void setFlippedX(boolean v) {
    flippedX = v;
  }

  /**
   * Returns true if the texture is flipped along the vertical direction.
   * 
   * @return boolean;
   */
  public boolean isFlippedY() {
    return flippedY;
  }

  /**
   * Sets the texture as flipped or not flipped on the vertical direction.
   * 
   * @param v boolean;
   */
  public void setFlippedY(boolean v) {
    flippedY = v;
  }

  /**
   * Puts img into texture, pixels and image.
   * 
   * @param img PImage
   */
  public void putImage(PImage img) {
    putImage(img, new GLTextureParameters());
  }

  /**
   * Puts img into texture, pixels and image.
   * 
   * @param img PImage
   * @param format int
   */
  public void putImage(PImage img, int format) {
    putImage(img, new GLTextureParameters(format));
  }

  /**
   * Puts img into texture, pixels and image.
   * 
   * @param img PImage
   * @param format int
   * @param filter int
   */
  public void putImage(PImage img, int format, int filter) {
    putImage(img, new GLTextureParameters(format, filter));
  }

  /**
   * Puts img into texture, pixels and image.
   * 
   * @param img PImage
   * @param params GLTextureParameters
   */
  public void putImage(PImage img, GLTextureParameters params) {
    img.loadPixels();

    if ((img.width != width) || (img.height != height)) {
      init(img.width, img.height, params);
    }

    // Putting img into pixels...
    PApplet.arrayCopy(img.pixels, pixels);

    // ...into texture...
    loadTexture();

    // ...and into image.
    updatePixels();
  }

  /**
   * Puts pixels of img into texture only.
   * 
   * @param img PImage
   */
  public void putPixelsIntoTexture(PImage img) {
    if (img.width <= 1 || img.height <= 1) {
      // The source image hasn't been initialized.
      return;
    }
    
    if ((img.width != width) || (img.height != height)) {
      init(img.width, img.height, new GLTextureParameters());
    }

    if (img.pixels == null || img.pixels.length < img.width * img.height) {
      // The source pixels hasn't been initialized.
      return;
    }
    
    // Putting into texture.
    if (texInternalFormat == GL.GL_RGB) {
      putBuffer(img.pixels, RGB);
    }
    if (texInternalFormat == GL.GL_RGBA) {
      putBuffer(img.pixels, ARGB);
    }
    if (texInternalFormat == GL.GL_ALPHA) {
      putBuffer(img.pixels, ALPHA);
    }
  }

  /**
   * Puts the pixels of img inside the rectangle (x, y, x+w, y+h) into texture
   * only.
   * 
   * @param img PImage
   * @param x int
   * @param y int
   * @param w int
   * @param h int
   */
  public void putPixelsIntoTexture(PImage img, int x, int y, int w, int h) {
    if (img.width <= 1 || img.height <= 1) {
      // The source image hasn't been initialized.
      return;
    }
    
    x = PApplet.constrain(x, 0, img.width);
    y = PApplet.constrain(y, 0, img.height);

    w = PApplet.constrain(w, 0, img.width - x);
    h = PApplet.constrain(h, 0, img.height - y);

    if ((w != width) || (h != height)) {
      init(w, h, new GLTextureParameters());
    }

    if (img.pixels == null || img.pixels.length < img.width * img.height) {
      // The source pixels hasn't been initialized.
      return;
    }
        
    int p0;
    int dest[] = new int[w * h];
    for (int j = 0; j < h; j++) {
      p0 = y * img.width + x + (img.width - w) * j;
      PApplet.arrayCopy(img.pixels, p0 + w * j, dest, w * j, w);
    }

    // Putting into texture.
    if (texInternalFormat == GL.GL_RGB) {
      putBuffer(dest, RGB);
    }
    if (texInternalFormat == GL.GL_RGBA) {
      putBuffer(dest, ARGB);
    }
    if (texInternalFormat == GL.GL_ALPHA) {
      putBuffer(dest, ALPHA);
    }
  }

  /**
   * Copies texture to img.
   * 
   * @param img PImage
   */
  public void getImage(PImage img) {
    int w = width;
    int h = height;

    if ((img.width != w) || (img.height != h)) {
      img.init(w, h, ARGB);
    }

    int size = w * h;
    IntBuffer buffer = BufferUtil.newIntBuffer(size);
    gl.glBindTexture(texTarget, tex);
    gl.glGetTexImage(texTarget, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
    gl.glBindTexture(texTarget, 0);

    buffer.get(img.pixels);

    int[] pixelsARGB = convertToARGB(img.pixels);
    PApplet.arrayCopy(pixelsARGB, img.pixels);
    if (flippedX)
      flipArrayOnX(img.pixels, 1);
    if (flippedY)
      flipArrayOnY(img.pixels, 1);
    img.updatePixels();
  }

  /**
   * Load texture, pixels and image from file.
   * 
   * @param filename String
   */
  public void loadTexture(String filename) {
    PImage img = parent.loadImage(filename);
    putImage(img);
  }

  /**
   * Load texture, pixels and image from file using the specified texture
   * parameters.
   * 
   * @param filename String
   * @param params GLTextureParameters
   */
  public void loadTexture(String filename, GLTextureParameters params) {
    PImage img = parent.loadImage(filename);
    putImage(img, params);
  }

  /**
   * Load texture, pixels and image from file using the specified texture
   * format.
   * 
   * @param filename String
   * @param format int
   */
  public void loadTexture(String filename, int format) {
    PImage img = parent.loadImage(filename);
    putImage(img, format);
  }

  /**
   * Load texture, pixels and image from file using the specified texture format
   * and filtering.
   * 
   * @param filename String
   * @param format int
   * @param filter int
   */
  public void loadTexture(String filename, int format, int filter) {
    PImage img = parent.loadImage(filename);
    putImage(img, format, filter);
  }

  /**
   * Copy pixels to texture (loadPixels should have been called beforehand).
   */
  public void loadTexture() {
    // Putting into texture.
    if (texInternalFormat == GL.GL_RGB) {
      putBuffer(pixels, RGB);
    }
    if (texInternalFormat == GL.GL_RGBA) {
      putBuffer(pixels, ARGB);
    }
    if (texInternalFormat == GL.GL_ALPHA) {
      putBuffer(pixels, ALPHA);
    }
  }

  /**
   * Copy src texture into this.
   * 
   * @param src GLTexture
   */
  public void copy(GLTexture src) {
    glstate.copyTex(src, this);
  }

  /**
   * Copy pixels to texture using PBO.
   * TODO: Make this work for some release 1.x (perhaps). Or just remove.
   */
  public void loadTextureFast() {
    if (pbo == 0) {
      System.err.println("Fast texture load is not enabled!");
      return;
    }

    gl.glBindTexture(texTarget, tex);
    gl.glBindBuffer(pboTarget, pbo);

    // gl.glBufferDataARB(GL_PIXEL_UNPACK_BUFFER_ARB, DATA_SIZE, 0,
    // GL_STREAM_DRAW_ARB);
    // gl.glBufferDataARB(pboTarget, 4 * width * height, null, pboUsage);

    IntBuffer texels = gl.glMapBuffer(pboTarget, GL.GL_WRITE_ONLY)
        .asIntBuffer();

    System.out.println(texels);
    texels.put(pixels);

    gl.glUnmapBufferARB(pboTarget);
    gl.glBindBuffer(pboTarget, 0);
  }

  /**
   * Creates a Pixel Buffer Object (PBO) to allow for faster transfers of pixel
   * data in GPU memory (EXPERIMENTAL). For a detailed tutorial on PBOs look at:
   * http://www.songho.ca/opengl/gl_pbo.html
   */
  public void enableFastTextureLoad() {
    if (pbo != 0) {
      releasePBO();
    }

    pbo = GLState.createGLResource(GL_PIXEL_BUFFER);

    // GL.GL_PIXEL_UNPACK_BUFFER_ARB indicates that the PBO will be used to copy
    // data
    // to the framebuffer (unpack operations).
    pboTarget = GL.GL_PIXEL_UNPACK_BUFFER_ARB;

    // This usage setting means that the data will be changed frequently
    // ("dynamic") and
    // it will be sent to GPU in order to draw (application to GL).
    pboUsage = GL.GL_DYNAMIC_DRAW_ARB;
    pboUsage = GL.GL_STREAM_DRAW_ARB;

    gl.glBindBufferARB(pboTarget, pbo);

    // Reserve empty space for the PBO.
    gl.glBufferDataARB(pboTarget, 4 * width * height, null, pboUsage);

    gl.glBindBufferARB(pboTarget, 0);
  }

  /**
   * Copy texture to pixels (doesn't call updatePixels).
   */
  public void updateTexture() {
    int size = width * height;
    IntBuffer buffer = BufferUtil.newIntBuffer(size);

    gl.glBindTexture(texTarget, tex);
    gl.glGetTexImage(texTarget, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
    gl.glBindTexture(texTarget, 0);

    buffer.get(pixels);
    int[] pixelsARGB = convertToARGB(pixels);
    PApplet.arrayCopy(pixelsARGB, pixels);
    if (flippedX)
      flipArrayOnX(pixels, 1);
    if (flippedY)
      flipArrayOnY(pixels, 1);
  }

  /**
   * Applies filter texFilter using this texture as source and destTex as
   * destination.
   * 
   * @param texFilter GLTextureFilter
   * @param destTex GLTexture
   */
  public void filter(GLTextureFilter texFilter, GLTexture destTex) {
    texFilter.apply(new GLTexture[] { this }, new GLTexture[] { destTex });
  }

  /**
   * Applies filter texFilter using this texture as source and destTex as
   * destination. Sets all the value for all the parameters, by means of a
   * parameter list of variable length. values is an array of float[].
   * 
   * @param texFilter GLTextureFilter
   * @param destTex GLTexture
   * @param float[] values
   */
  public void filter(GLTextureFilter texFilter, GLTexture destTex,
      float[]... values) {
    texFilter.setParameterValues(values);
    texFilter.apply(new GLTexture[] { this }, new GLTexture[] { destTex });
  }

  /**
   * Applies filter texFilter using this texture as source, destTex as
   * destination and destA as the destination alpha value for the filter.
   * 
   * @param texFilter GLTextureFilter
   * @param destTex GLTexture
   * @param destA float
   */
  public void filter(GLTextureFilter texFilter, GLTexture destTex, float destA) {
    texFilter.apply(new GLTexture[] { this }, new GLTexture[] { destTex },
        null, 1.0f, 1.0f, 1.0f, destA);
  }

  /**
   * Applies filter texFilter using this texture as source, destTex as
   * destination and destA as the destination alpha value for the filter. Sets
   * all the value for all the parameters, by means of a parameter list of
   * variable length. values is an array of float[].
   * 
   * @param texFilter GLTextureFilter
   * @param destTex GLTexture
   * @param destA float
   * @param float[] values
   */
  public void filter(GLTextureFilter texFilter, GLTexture destTex, float destA,
      float[]... values) {
    texFilter.setParameterValues(values);
    texFilter.apply(new GLTexture[] { this }, new GLTexture[] { destTex },
        null, 1.0f, 1.0f, 1.0f, destA);
  }

  /**
   * Applies filter texFilter using this texture as source, destTex as multiple
   * destinations and destA as the destination alpha value constant for the
   * filter.
   * 
   * @param texFilter GLTextureFilter
   * @param destTexArray GLTexture[]
   * @param destA float
   */
  public void filter(GLTextureFilter texFilter, GLTexture[] destTexArray,
      float destA) {
    texFilter.apply(new GLTexture[] { this }, destTexArray, null, 1.0f, 1.0f,
        1.0f, destA);
  }

  /**
   * Applies filter texFilter using this texture as source, destTex as multiple
   * destinations and destA as the destination alpha value for the filter. Sets
   * all the value for all the parameters, by means of a parameter list of
   * variable length. values is an array of float[].
   * 
   * @param texFilter GLTextureFilter
   * @param destTexArray GLTexture[]
   * @param destA float
   * @param float[] values
   */
  public void filter(GLTextureFilter texFilter, GLTexture[] destTexArray,
      float destA, float[]... values) {
    texFilter.setParameterValues(values);
    texFilter.apply(new GLTexture[] { this }, destTexArray, null, 1.0f, 1.0f,
        1.0f, destA);
  }

  /**
   * Draws the texture using the opengl commands, inside a rectangle located at
   * the origin with the original size of the texture.
   */
  public void render() {
    render(0, 0, width, height);
  }

  /**
   * Draws the texture using the opengl commands, inside a rectangle located at
   * (x,y) with the original size of the texture.
   * 
   * @param x float
   * @param y float
   */
  public void render(float x, float y) {
    render(x, y, width, height);
  }

  /**
   * Draws the texture using the opengl commands, inside a rectangle of width w
   * and height h located at (x,y).
   * 
   * @param x float
   * @param y float
   * @param w float
   * @param h float
   */
  public void render(float x, float y, float w, float h) {
    render(pgl, x, y, w, h);
  }

  /**
   * Draws the texture using the opengl commands, inside a rectangle of width w
   * and height h located at (x,y) and using the specified renderer.
   * 
   * @param renderer PGraphicsOpenGL
   * @param x float
   * @param y float
   * @param w float
   * @param h float
   */
  public void render(PGraphicsOpenGL renderer, float x, float y, float w,
      float h) {
    render(renderer, x, y, w, h, 0, 0, 1, 1);
  }

  public void render(PGraphicsOpenGL renderer, float x, float y, float w,
      float h, float sx, float ty, float sw, float th) {
    float fw, fh;
    if (renderer.textureMode == IMAGE) {
      fw = width;
      fh = height;
    } else {
      fw = 1.0f;
      fh = 1.0f;
    }

    renderer.beginShape(QUADS);
    renderer.texture(this);
    renderer.vertex(x, y, fw * sx, fh * ty);
    renderer.vertex(x + w, y, fw * sw, fh * ty);
    renderer.vertex(x + w, y + h, fw * sw, fh * th);
    renderer.vertex(x, y + h, fw * sx, fh * th);
    renderer.endShape();
  }

  /**
   * Copies intArray into the texture, assuming that the array contains 4 color
   * components and pixels are unsigned bytes.
   * 
   * @param intArray int[]
   */
  public void putBuffer(int[] intArray) {
    putBuffer(intArray, ARGB, TEX_BYTE);
  }

  /**
   * Copies intArray into the texture, using the specified format and assuming
   * that the pixels are unsigned bytes.
   * 
   * @param intArray  int[]
   * @param format int
   */
  public void putBuffer(int[] intArray, int format) {
    putBuffer(intArray, format, TEX_BYTE);
  }

  /**
   * Copies intArray into the texture, using the specified format and assuming
   * that the pixels are unsigned bytes.
   * 
   * @param intArray int[]
   * @param format int
   */
  public void putByteBuffer(int[] intArray, int format) {
    putBuffer(intArray, format, TEX_BYTE);
  }

  /**
   * Copies intArray into the texture, using the specified format and assuming
   * that the pixels are integers.
   * 
   * @param intArray int[]
   * @param format int
   */
  public void putIntBuffer(int[] intArray, int format) {
    putBuffer(intArray, format, TEX_INT);
  }

  /**
   * Copies intArray into the texture, using the format and type specified.
   * 
   * @param intArray int[]
   * @param format int
   * @param type int
   */
  public void putBuffer(int[] intArray, int format, int type) {
    if (tex == 0) {
      initTexture(width, height);
    }

    int[] convArray = intArray;
    int glFormat;
    if (format == ALPHA) {
      glFormat = GL.GL_ALPHA;
      if (type == TEX_BYTE) {
        // Not sure why this works, because the integer values in intArray are
        // converted to signed bytes, but the buffer is specified
        // as GL_UNSIGNED_BYTE.
        byte[] convArray2 = convertToAlpha(intArray);
        gl.glBindTexture(texTarget, tex);
        gl.glTexSubImage2D(texTarget, 0, 0, 0, width, height, glFormat,
            GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(convArray2));
        gl.glBindTexture(texTarget, 0);
        return;
      }
    } else if (format == RGB) {
      // If in the previous case, we need to use a byte array, here with RGB we
      // should use a byte array with 3 bytes per color,
      // i.e.: byte[] convArray3 = byte[3 * widht * height] and then storing RGB
      // components from intArray as follows:
      // for (int i = 0; i < width * height; i++) {
      // convArray3[3 * i ] = red(intArray[i]);
      // convArray3[3 * i + 1] = green(intArray[i]);
      // convArray3[3 * i + 2] = blue(intArray[i]);
      // }
      // where the red, green, green and blue operators involve the correct bit
      // shifting to extract the component from the int value.
      glFormat = GL.GL_RGBA;
      if (type == TEX_BYTE)
        convArray = convertToRGBA(intArray, RGB);
    } else {
      glFormat = GL.GL_RGBA;
      if (type == TEX_BYTE)
        convArray = convertToRGBA(intArray, ARGB);
    }

    int glType;
    if (type == TEX_INT)
      glType = GL.GL_INT;
    else
      glType = GL.GL_UNSIGNED_BYTE;

    putBuffer(glFormat, glType, IntBuffer.wrap(convArray));
  }
  
  public void putBuffer(int glFormat, int glType, IntBuffer buffer) {
    gl.glBindTexture(texTarget, tex);

    if (texTarget == GL.GL_TEXTURE_1D) {
      gl.glTexSubImage1D(texTarget, 0, 0, width, glFormat, glType, buffer);
    } else {
      if (usingMipmaps)
        GLState.glu.gluBuild2DMipmaps(texTarget, texInternalFormat, width,
            height, glFormat, glType, buffer);
      else
        gl.glTexSubImage2D(texTarget, 0, 0, 0, width, height, glFormat, glType, buffer);
    }

    gl.glBindTexture(texTarget, 0);    
  }
  
  /**
   * Copies floatArray into the texture, assuming that the array has 4
   * components.
   * 
   * @param floatArray float[]
   * @param format int
   */
  public void putBuffer(float[] floatArray) {
    putBuffer(floatArray, 4);
  }

  /**
   * Copies floatArray into the texture, using the specified number of channels.
   * 
   * @param floatArray float[]
   * @param nchan int
   */
  public void putBuffer(float[] floatArray, int nchan) {
    if (tex == 0) {
      initTexture(width, height);
    }

    int glFormat;
    if (nchan == 1)
      glFormat = GL.GL_LUMINANCE;
    else if (nchan == 3)
      glFormat = GL.GL_RGB;
    else
      glFormat = GL.GL_RGBA;

    gl.glBindTexture(texTarget, tex);

    if (texTarget == GL.GL_TEXTURE_1D)
      gl.glTexSubImage1D(texTarget, 0, 0, width, glFormat, GL.GL_FLOAT,
          FloatBuffer.wrap(floatArray));
    else
      gl.glTexSubImage2D(texTarget, 0, 0, 0, width, height, glFormat,
          GL.GL_FLOAT, FloatBuffer.wrap(floatArray));

    gl.glBindTexture(texTarget, 0);
  }

  /**
   * Copies the texture into intArray, assuming that the array has 4 components
   * and the pixels are unsigned bytes.
   * 
   * @param intArray int[]
   */
  public void getBuffer(int[] intArray) {
    getBuffer(intArray, ARGB, TEX_BYTE);
  }

  /**
   * Copies the texture into intArray, using the specified format and assuming
   * that the pixels are unsigned bytes.
   * 
   * @param intArray int[]
   * @param format nchan
   */
  public void getBuffer(int[] intArray, int format) {
    getBuffer(intArray, format, TEX_BYTE);
  }

  /**
   * Copies the texture into intArray, using the specified format and assuming
   * that the pixels are unsigned bytes.
   * 
   * @param intArray int[]
   * @param format int
   */
  public void getByteBuffer(int[] intArray, int format) {
    getBuffer(intArray, format, TEX_BYTE);
  }

  /**
   * Copies the texture into intArray, using the specified format and assuming
   * that the pixels are integers.
   * 
   * @param intArray int[]
   * @param format int
   */
  public void getIntBuffer(int[] intArray, int format) {
    getBuffer(intArray, format, TEX_INT);
  }

  /**
   * Copies the texture into intArray, using the specified format and type. The
   * resulting array is not reordered into ARGB, it follows OpenGL color
   * component ordering (for instance, for 4 components, little-endian this
   * would be ABGR). Perhaps ordering should be done automatically?
   * 
   * @param intArray int[]
   * @param format  int
   * @param type int
   */
  public void getBuffer(int[] intArray, int format, int type) {
    int mult;
    int glFormat;
    if (format == ALPHA) {
      mult = 1;
      glFormat = GL.GL_LUMINANCE;
    } else if (format == RGB) {
      mult = 3;
      glFormat = GL.GL_RGB;
    } else {
      mult = 4;
      glFormat = GL.GL_RGBA;
    }

    int size;
    int glType;
    if (type == TEX_INT) {
      glType = GL.GL_INT;
    } else {
      mult = 1;
      glType = GL.GL_UNSIGNED_BYTE;
    }
    size = mult * width * height;

    if (intArray.length != size) {
      System.err.println("Wrong size of buffer!");
      return;
    }

    IntBuffer buffer = BufferUtil.newIntBuffer(size);

    gl.glBindTexture(texTarget, tex);
    gl.glGetTexImage(texTarget, 0, glFormat, glType, buffer);
    gl.glBindTexture(texTarget, 0);

    buffer.get(intArray);

    if (flippedX)
      flipArrayOnX(intArray, mult);
    if (flippedY)
      flipArrayOnY(intArray, mult);
  }

  /**
   * Copies the texture into floatArray.
   * 
   * @param floatArray float[]
   * @param format nchan
   */
  public void getBuffer(float[] floatArray, int nchan) {
    int mult;
    int glFormat;
    if (nchan == 1) {
      mult = 1;
      glFormat = GL.GL_LUMINANCE;
    } else if (nchan == 3) {
      mult = 3;
      glFormat = GL.GL_RGB;
    } else {
      mult = 4;
      glFormat = GL.GL_RGBA;
    }

    int size = mult * width * height;
    if (floatArray.length != size) {
      System.err.println("Wrong size of buffer!");
      return;
    }

    FloatBuffer buffer = BufferUtil.newFloatBuffer(size);

    gl.glBindTexture(texTarget, tex);
    gl.glGetTexImage(texTarget, 0, glFormat, GL.GL_FLOAT, buffer);
    gl.glBindTexture(texTarget, 0);

    buffer.get(floatArray);

    if (flippedX)
      flipArrayOnX(floatArray, mult);
    if (flippedY)
      flipArrayOnY(floatArray, mult);
  }

  /**
   * Sets the texture to have random values in the ranges specified for each
   * component.
   * 
   * @param r0 float
   * @param r1 float
   * @param g0 float
   * @param g1 float
   * @param b0 float
   * @param b1 float
   * @param a0 float
   * @param a1 float
   */
  public void setRandom(float r0, float r1, float g0, float g1, float b0,
      float b1, float a0, float a1) {
    float randBuffer[] = new float[4 * width * height];
    for (int j = 0; j < height; j++)
      for (int i = 0; i < width; i++) {
        randBuffer[i * 4 + j * width * 4] = parent.random(r0, r1);
        randBuffer[i * 4 + j * width * 4 + 1] = parent.random(g0, g1);
        randBuffer[i * 4 + j * width * 4 + 2] = parent.random(b0, b1);
        randBuffer[i * 4 + j * width * 4 + 3] = parent.random(a0, a1);
      }
    putBuffer(randBuffer);
  }

  /**
   * Sets the texture to have random values in the first two coordinates chosen
   * on the circular region defined by the parameters.
   * 
   * @param r0 float
   * @param r1 float
   * @param phi0 float
   * @param phi1 float
   */
  public void setRandomDir2D(float r0, float r1, float phi0, float phi1) {
    float r, phi;
    float randBuffer[] = new float[4 * width * height];
    for (int j = 0; j < height; j++)
      for (int i = 0; i < width; i++) {
        r = parent.random(r0, r1);
        phi = parent.random(phi0, phi1);
        randBuffer[i * 4 + j * width * 4] = r * PApplet.cos(phi);
        randBuffer[i * 4 + j * width * 4 + 1] = r * PApplet.sin(phi);
        randBuffer[i * 4 + j * width * 4 + 2] = 0.0f;
        randBuffer[i * 4 + j * width * 4 + 3] = 0.0f;
      }
    putBuffer(randBuffer);
  }

  /**
   * Sets the texture to have random values in the first three coordinates
   * chosen on the spherical region defined by the parameters.
   * 
   * @param r0 float
   * @param r1 float
   * @param phi0 float
   * @param phi1 float
   * @param theta0 float
   * @param theta1 float
   */
  public void setRandomDir3D(float r0, float r1, float phi0, float phi1,
      float theta0, float theta1) {
    float r, phi, theta;
    float randBuffer[] = new float[4 * width * height];
    for (int j = 0; j < height; j++)
      for (int i = 0; i < width; i++) {
        r = parent.random(r0, r1);
        phi = parent.random(phi0, phi1);
        theta = parent.random(theta0, theta1);

        randBuffer[i * 4 + j * width * 4] = r * PApplet.cos(phi)
            * PApplet.sin(theta);
        randBuffer[i * 4 + j * width * 4 + 1] = r * PApplet.sin(phi)
            * PApplet.sin(theta);
        randBuffer[i * 4 + j * width * 4 + 2] = r * PApplet.cos(theta);
        randBuffer[i * 4 + j * width * 4 + 3] = 0.0f;
      }
    putBuffer(randBuffer);
  }

  /**
   * Sets the texture to have the same given float value in each component.
   * 
   * @param r float
   * @param g float
   * @param b float
   * @param a float
   */
  public void setValue(float r, float g, float b, float a) {
    float valBuffer[] = new float[4 * width * height];
    for (int j = 0; j < height; j++)
      for (int i = 0; i < width; i++) {
        valBuffer[i * 4 + j * width * 4] = r;
        valBuffer[i * 4 + j * width * 4 + 1] = g;
        valBuffer[i * 4 + j * width * 4 + 2] = b;
        valBuffer[i * 4 + j * width * 4 + 3] = a;
      }
    putBuffer(valBuffer);
  }

  /**
   * Sets to zero all the pixels of the texture.
   */
  public void setZero() {
    setValue(0.0f, 0.0f, 0.0f, 0.0f);
  }

  /**
   * Fills the texture with the specified gray tone.
   * 
   * @param gray int
   */
  public void clear(int gray) {
    int c = parent.color(gray);
    glstate.clearTex(tex, texTarget, c);
  }

  /**
   * Fills the texture with the specified gray tone.
   * 
   * @param gray float
   */
  public void clear(float gray) {
    int c = parent.color(gray);
    glstate.clearTex(tex, texTarget, c);
  }

  /**
   * Fills the texture with the specified gray tone and alpha value.
   * 
   * @param gray  int
   * @param alpha int
   */
  public void clear(int gray, int alpha) {
    int c = parent.color(gray, alpha);
    glstate.clearTex(tex, texTarget, c);
  }

  /**
   * Fills the texture with the specified rgb color and alpha value.
   * 
   * @param rgb int
   * @param alpha float
   */
  public void clear(int rgb, float alpha) {
    int c = parent.color(rgb, alpha);
    glstate.clearTex(tex, texTarget, c);
  }

  /**
   * Fills the texture with the specified gray tone and alpha value.
   * 
   * @param gray float
   * @param alpha float
   */
  public void clear(float gray, float alpha) {
    int c = parent.color(gray, alpha);
    glstate.clearTex(tex, texTarget, c);
  }

  /**
   * Fills the texture with the specified color components.
   * 
   * @param x int
   * @param y int
   * @param z int
   */
  public void clear(int x, int y, int z) {
    int c = parent.color(x, y, z);
    glstate.clearTex(tex, texTarget, c);
  }

  /**
   * Fills the texture with the specified color components.
   * 
   * @param x float
   * @param y float
   * @param z float
   */
  public void clear(float x, float y, float z) {
    int c = parent.color(x, y, z);
    glstate.clearTex(tex, texTarget, c);
  }

  /**
   * Fills the texture with the specified color components and alpha component.
   * 
   * @param x int
   * @param y int
   * @param z int
   * @param a int
   */
  public void clear(int x, int y, int z, int a) {
    int c = parent.color(x, y, z, a);
    glstate.clearTex(tex, texTarget, c);
  }

  /**
   * Fills the texture with the specified color components and alpha component.
   * 
   * @param x float
   * @param y float
   * @param z float
   * @param a float
   */
  public void clear(float x, float y, float z, float a) {
    int c = parent.color(x, y, z, a);
    glstate.clearTex(tex, texTarget, c);
  }

  /**
   * Paints the texture with the specified gray tone.
   * 
   * @param gray int
   */
  public void paint(int gray) {
    int c = parent.color(gray);
    glstate.paintTex(tex, texTarget, width, height, c);
  }

  /**
   * Paints the texture with the specified gray tone.
   * 
   * @param gray float
   */
  public void paint(float gray) {
    int c = parent.color(gray);
    glstate.paintTex(tex, texTarget, width, height, c);
  }

  /**
   * Paints the texture with the specified gray tone and alpha value.
   * 
   * @param gray int
   * @param alpha int
   */
  public void paint(int gray, int alpha) {
    int c = parent.color(gray, alpha);
    glstate.paintTex(tex, texTarget, width, height, c);
  }

  /**
   * Paints the texture with the specified rgb color and alpha value.
   * 
   * @param rgb int
   * @param alpha float
   */
  public void paint(int rgb, float alpha) {
    int c = parent.color(rgb, alpha);
    glstate.paintTex(tex, texTarget, width, height, c);
  }

  /**
   * Paints the texture with the specified gray tone and alpha value.
   * 
   * @param gray float
   * @param alpha float
   */
  public void paint(float gray, float alpha) {
    int c = parent.color(gray, alpha);
    glstate.paintTex(tex, texTarget, width, height, c);
  }

  /**
   * Paints the texture with the specified color components.
   * 
   * @param x int
   * @param y int
   * @param z int
   */
  public void paint(int x, int y, int z) {
    int c = parent.color(x, y, z);
    glstate.paintTex(tex, texTarget, width, height, c);
  }

  /**
   * Paints the texture with the specified color components.
   * 
   * @param x float
   * @param y float
   * @param z float
   */
  public void paint(float x, float y, float z) {
    int c = parent.color(x, y, z);
    glstate.paintTex(tex, texTarget, width, height, c);
  }

  /**
   * Paints the texture with the specified color components and alpha component.
   * 
   * @param x int
   * @param y int
   * @param z int
   * @param a int
   */
  public void paint(int x, int y, int z, int a) {
    int c = parent.color(x, y, z, a);
    glstate.paintTex(tex, texTarget, width, height, c);
  }

  /**
   * Paints the texture with the specified color components and alpha component.
   * 
   * @param x float
   * @param y float
   * @param z float
   * @param a float
   */
  public void paint(float x, float y, float z, float a) {
    int c = parent.color(x, y, z, a);
    glstate.paintTex(tex, texTarget, width, height, c);
  }

  ///////////////////////////////////////////////////////////////////////////
  
  // Bind/unbind.
  
//  protected void bind(int tu) {
  public void bind(int tu) {
    texUnit = tu;
    gl.glActiveTexture(GL.GL_TEXTURE0 + texUnit);
    gl.glBindTexture(texTarget, tex);
    if (-1 < texUniform) {
      gl.glUniform1iARB(texUniform, texUnit);
    }
  }
  
  public void unbind() {
    if (-1 < texUnit) {
      gl.glActiveTexture(GL.GL_TEXTURE0 + texUnit);
      gl.glBindTexture(texTarget, 0);
      texUnit = -1;
      if (-1 < texUniform) {
        texUniform = -1;
      }      
    }
  }
  
//  protected void setTexUniform(int tu) {
  public void setTexUniform(int tu) {
    texUniform = tu;
  }  
  
  ///////////////////////////////////////////////////////////////////////////
  
  // Direct buffer input.  
  
  /**
   * Sets obj as the pixel source for this texture. The object must have
   * a public method called disposeBuffer(Object obj) that the texture will
   * use to release the int buffers after they are copied to the texture. 
   * 
   * @param obj Object
   */  
  public void setPixelSource(Object obj) {
    diposePixelsHandler = obj;
    try {
      disposePixelsMethod = obj.getClass().getMethod("disposeBuffer", new Class[] { Object.class});
    } catch (Exception e) {
      e.printStackTrace();
    }    
  }

  /**
   * If there are frames stored in the buffer, it removes the last
   * and copies the pixels to the texture. It returns true if that
   * is the case, otherwise it returns false.
   * 
   * @return boolean
   */    
  public boolean putPixelsIntoTexture() {
    if (pixelBuffer != null && 0 < pixelBuffer.size() && disposePixelsMethod != null) {
      
      PixelData data = null;
      try {
        data = pixelBuffer.remove(0);
      } catch (NoSuchElementException ex) {
        System.err.println("Don't have pixel data to copy to texture");
      }
      
      if (data != null) {
        if ((data.w != width) || (data.h != height)) {
          init(data.w, data.h, new GLTextureParameters());
        }
        putBuffer(GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, data.rgbBuf);
        data.dispose();
        
        return true;        
      } else {
        return false;
      }
      
    }
    return false;
  }

  /**
   * Sets the size of the pixel buffer, in number of frames. When this size is 
   * reached, new frames are dropped.
   * 
   * @param n int
   */  
  public void setPixelBufferSize(int n) {
    maxBuffSize = n;
  }
  
  /**
   * Returns how many frames are currently stored in the pixel buffer.
   */    
  public int getPixelBufferUse() {
    return pixelBuffer.size();
  }  

  /**
   * Determines the behavior of the buffering. When the argument is true,
   * then new frames are disposed when the buffer is already full, otherwise
   * they are not. In the later case, and depending on how the buffer generation
   * method works, might result in the buffers being stored somewhere else
   * and being resent later.
   */    
  public void delPixelsWhenBufferFull(boolean v) { 
    disposeFramesWhenPixelBufferFull = v;
  }

  /**
   * This is the method used by the pixel source object
   * to add frames to the buffer.
   * 
   * @param natBuf Object
   * @param rgbBuf IntBuffer
   * @param w int
   * @param h int
   */  
  public void addPixelsToBuffer(Object natBuf, IntBuffer rgbBuf, int w, int h) {
    if (pixelBuffer == null) {
      pixelBuffer = new LinkedList<PixelData>();
    }

    if (pixelBuffer.size() + 1 <= maxBuffSize) {
      pixelBuffer.add(new PixelData(natBuf, rgbBuf, w, h));
    } else if (disposeFramesWhenPixelBufferFull) {            
      // The buffer reached the maximum size, so we just dispose the new frame.
      try {
        disposePixelsMethod.invoke(diposePixelsHandler, new Object[] { natBuf });
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }
  }  
  
  ///////////////////////////////////////////////////////////////////////////  
  
  /**
   * Flips intArray along the X axis.
   * 
   * @param intArray int[]
   * @param mult int
   */
  protected void flipArrayOnX(int[] intArray, int mult) {
    int index = 0;
    int xindex = mult * (width - 1);
    for (int x = 0; x < width / 2; x++) {
      for (int y = 0; y < height; y++) {
        int i = index + mult * y * width;
        int j = xindex + mult * y * width;

        for (int c = 0; c < mult; c++) {
          int temp = intArray[i];
          intArray[i] = intArray[j];
          intArray[j] = temp;

          i++;
          j++;
        }

      }
      index += mult;
      xindex -= mult;
    }
  }

  /**
   * Flips intArray along the Y axis.
   * 
   * @param intArray int[]
   * @param mult int
   */
  protected void flipArrayOnY(int[] intArray, int mult) {
    int index = 0;
    int yindex = mult * (height - 1) * width;
    for (int y = 0; y < height / 2; y++) {
      for (int x = 0; x < mult * width; x++) {
        int temp = intArray[index];
        intArray[index] = intArray[yindex];
        intArray[yindex] = temp;

        index++;
        yindex++;
      }
      yindex -= mult * width * 2;
    }
  }

  /**
   * Flips floatArray along the X axis.
   * 
   * @param intArray int[]
   * @param mult int
   */
  protected void flipArrayOnX(float[] floatArray, int mult) {
    int index = 0;
    int xindex = mult * (width - 1);
    for (int x = 0; x < width / 2; x++) {
      for (int y = 0; y < height; y++) {
        int i = index + mult * y * width;
        int j = xindex + mult * y * width;

        for (int c = 0; c < mult; c++) {
          float temp = floatArray[i];
          floatArray[i] = floatArray[j];
          floatArray[j] = temp;

          i++;
          j++;
        }

      }
      index += mult;
      xindex -= mult;
    }
  }

  /**
   * Flips floatArray along the Y axis.
   * 
   * @param intArray int[]
   * @param mult int
   */
  protected void flipArrayOnY(float[] floatArray, int mult) {
    int index = 0;
    int yindex = mult * (height - 1) * width;
    for (int y = 0; y < height / 2; y++) {
      for (int x = 0; x < mult * width; x++) {
        float temp = floatArray[index];
        floatArray[index] = floatArray[yindex];
        floatArray[yindex] = temp;

        index++;
        yindex++;
      }
      yindex -= mult * width * 2;
    }
  }

  /**
   * @invisible Reorders a pixel array in ARGB format into the order required by
   *            OpenGL (RGBA).
   * @param intArray int[]
   * @param arrayFormat int
   */
  protected int[] convertToRGBA(int[] intArray, int arrayFormat) {
    int twidth = width;
    int t = 0;
    int p = 0;
    int[] tIntArray = new int[width * height];
    if (GLGraphics.BIG_ENDIAN) {
      switch (arrayFormat) {
      case ALPHA:

        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            tIntArray[t++] = 0xFFFFFF00 | intArray[p++];
          }
          t += twidth - width;
        }
        break;

      case RGB:

        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            int pixel = intArray[p++];
            tIntArray[t++] = (pixel << 8) | 0xff;
          }
          t += twidth - width;
        }
        break;

      case ARGB:

        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            int pixel = intArray[p++];
            tIntArray[t++] = (pixel << 8) | ((pixel >> 24) & 0xff);
          }
          t += twidth - width;
        }
        break;

      }
    } else {
      // LITTLE_ENDIAN
      // ARGB native, and RGBA opengl means ABGR on windows
      // for the most part just need to swap two components here
      // the sun.cpu.endian here might be "false", oddly enough..
      // (that's why just using an "else", rather than check for "little")

      switch (arrayFormat) {
      case ALPHA:
        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            tIntArray[t++] = (intArray[p++] << 24) | 0x00FFFFFF;
          }
          t += twidth - width;
        }
        break;

      case RGB:

        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            int pixel = intArray[p++];
            // needs to be ABGR, stored in memory xRGB
            // so R and B must be swapped, and the x just made FF
            tIntArray[t++] = 0xff000000
                | // force opacity for good measure
                ((pixel & 0xFF) << 16) | ((pixel & 0xFF0000) >> 16)
                | (pixel & 0x0000FF00);
          }
          t += twidth - width;
        }
        break;

      case ARGB:

        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            int pixel = intArray[p++];
            // needs to be ABGR stored in memory ARGB
            // so R and B must be swapped, A and G just brought back in
            tIntArray[t++] = ((pixel & 0xFF) << 16)
                | ((pixel & 0xFF0000) >> 16) | (pixel & 0xFF00FF00);
          }
          t += twidth - width;
        }
        break;

      }

    }

    return tIntArray;
  }

  /**
   * @invisible Reorders a pixel array in RGBA format into ARGB.
   * @param intArray int[]
   */
  protected int[] convertToARGB(int[] intArray) {
    int twidth = width;
    int t = 0;
    int p = 0;
    int[] tIntArray = new int[width * height];
    if (GLGraphics.BIG_ENDIAN) {
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int pixel = intArray[p++];
          tIntArray[t++] = (pixel >> 8) | ((pixel << 24) & 0xff);
        }
        t += twidth - width;
      }
    } else {
      // LITTLE_ENDIAN
      // ARGB native, and RGBA opengl means ABGR on windows
      // for the most part just need to swap two components here
      // the sun.cpu.endian here might be "false", oddly enough..
      // (that's why just using an "else", rather than check for "little")

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int pixel = intArray[p++];

          // needs to be ARGB stored in memory ABGR (RGBA = ABGR -> ARGB)
          // so R and B must be swapped, A and G just brought back in
          tIntArray[t++] = ((pixel & 0xFF) << 16) | ((pixel & 0xFF0000) >> 16)
              | (pixel & 0xFF00FF00);

        }
        t += twidth - width;
      }

    }

    return tIntArray;
  }

  /**
   * @invisible Creates a byte version of intArray..
   * @param intArray int[]
   */
  protected byte[] convertToAlpha(int[] intArray) {
    byte[] tByteArray = new byte[width * height];
    for (int i = 0; i < width * height; i++) {
      tByteArray[i] = (byte) (intArray[i]);
    }
    return tByteArray;
  }

  /**
   * @invisible Creates the opengl texture object.
   * @param w int
   * @param h int
   */
  protected void initTexture(int w, int h) {
    if (tex != 0) {
      releaseTexture();
    }
    tex = GLState.createGLResource(GL_TEXTURE_OBJECT);
    gl.glBindTexture(texTarget, tex);
    gl.glTexParameteri(texTarget, GL.GL_TEXTURE_MIN_FILTER, minFilter);
    gl.glTexParameteri(texTarget, GL.GL_TEXTURE_MAG_FILTER, magFilter);
    gl.glTexParameteri(texTarget, GL.GL_TEXTURE_WRAP_S, wrapModeS);
    gl.glTexParameteri(texTarget, GL.GL_TEXTURE_WRAP_T, wrapModeT);
    if (texTarget == GL.GL_TEXTURE_1D)
      gl.glTexImage1D(texTarget, 0, texInternalFormat, w, 0, GL.GL_RGBA,
          GL.GL_UNSIGNED_BYTE, null);
    else
      gl.glTexImage2D(texTarget, 0, texInternalFormat, w, h, 0, GL.GL_RGBA,
          GL.GL_UNSIGNED_BYTE, null);
    gl.glBindTexture(texTarget, 0);

    if (texTarget == GL.GL_TEXTURE_RECTANGLE_ARB) {
      maxTexCoordS = w;
      maxTexCoordT = h;
    } else {
      maxTexCoordS = 1.0f;
      maxTexCoordT = 1.0f;
    }
  }

  /**
   * @invisible Initializes the texture with a pre-existing OpenGL texture ID.
   * @param w int
   * @param h int
   * @param id int
   */
  protected void initTexture(int w, int h, int id) {
    if (tex != 0) {
      releaseTexture();
    }

    tex = id;
    gl.glBindTexture(texTarget, tex);
    gl.glTexParameteri(texTarget, GL.GL_TEXTURE_MIN_FILTER, minFilter);
    gl.glTexParameteri(texTarget, GL.GL_TEXTURE_MAG_FILTER, magFilter);
    gl.glTexParameteri(texTarget, GL.GL_TEXTURE_WRAP_S, wrapModeS);
    gl.glTexParameteri(texTarget, GL.GL_TEXTURE_WRAP_T, wrapModeT);
    if (texTarget == GL.GL_TEXTURE_1D)
      gl.glTexImage1D(texTarget, 0, texInternalFormat, w, 0, GL.GL_RGBA,
          GL.GL_UNSIGNED_BYTE, null);
    else
      gl.glTexImage2D(texTarget, 0, texInternalFormat, w, h, 0, GL.GL_RGBA,
          GL.GL_UNSIGNED_BYTE, null);
    gl.glBindTexture(texTarget, 0);

    if (texTarget == GL.GL_TEXTURE_RECTANGLE_ARB) {
      maxTexCoordS = w;
      maxTexCoordT = h;
    } else {
      maxTexCoordS = 1.0f;
      maxTexCoordT = 1.0f;
    }
  }

  /**
   * @invisible Deletes the opengl texture object.
   */
  protected void releaseTexture() {
    GLState.deleteGLResource(tex, GL_TEXTURE_OBJECT);
    tex = 0;
  }

  /**
   * @invisible Deletes the PBO.
   */
  protected void releasePBO() {
    GLState.deleteGLResource(pbo, GL_PIXEL_BUFFER);
    pbo = 0;
  }

  /**
   * @invisible Sets texture target and internal format according to the target
   *            and type specified.
   * @param target int
   * @param params GLTextureParameters
   */
  protected void setTextureParams(GLTextureParameters params) {
    System.out.println("Setting texture parameters...");

      if (params.target == TEX_NORM) {
      texTarget = GL.GL_TEXTURE_2D;
    } else if (params.target == TEX_RECT) {
      texTarget = GL.GL_TEXTURE_RECTANGLE_ARB;
    } else if (params.target == TEX_ONEDIM) {
      texTarget = GL.GL_TEXTURE_1D;
    }

    if (params.format == RGB) {
      texInternalFormat = GL.GL_RGB;
    }
    if (params.format == ARGB) {
      texInternalFormat = GL.GL_RGBA;
    }
    if (params.format == ALPHA) {
      texInternalFormat = GL.GL_ALPHA;
    } else if (params.format == FLOAT) {
      texInternalFormat = GL.GL_RGBA16F_ARB;
    } else if (params.format == DOUBLE) {
      texInternalFormat = GL.GL_RGBA32F_ARB;
    }

    if (params.minFilter == NEAREST_SAMPLING) {
      minFilter = GL.GL_NEAREST;
    } else if (params.minFilter == LINEAR_SAMPLING) {
      minFilter = GL.GL_LINEAR;
    } else if (params.minFilter == NEAREST_MIPMAP_NEAREST) {
      minFilter = GL.GL_NEAREST_MIPMAP_NEAREST;
    } else if (params.minFilter == LINEAR_MIPMAP_NEAREST) {
      minFilter = GL.GL_LINEAR_MIPMAP_NEAREST;
    } else if (params.minFilter == NEAREST_MIPMAP_LINEAR) {
      minFilter = GL.GL_NEAREST_MIPMAP_LINEAR;
    } else if (params.minFilter == LINEAR_MIPMAP_LINEAR) {
      minFilter = GL.GL_LINEAR_MIPMAP_LINEAR;
    }

    if (params.magFilter == NEAREST_SAMPLING) {
      magFilter = GL.GL_NEAREST;
    } else if (params.magFilter == LINEAR_SAMPLING) {
      magFilter = GL.GL_LINEAR;
    }

    if (params.wrappingU == CLAMP) {
      wrapModeS = GL.GL_CLAMP;
    } else if (params.wrappingU == REPEAT) {
      wrapModeS = GL.GL_REPEAT;
    }

    if (params.wrappingV == CLAMP) {
      wrapModeT = GL.GL_CLAMP;
    } else if (params.wrappingV == REPEAT) {
      wrapModeT = GL.GL_REPEAT;
    }
    
    usingMipmaps = (minFilter == GL.GL_NEAREST_MIPMAP_NEAREST)
        || (minFilter == GL.GL_LINEAR_MIPMAP_NEAREST)
        || (minFilter == GL.GL_NEAREST_MIPMAP_LINEAR)
        || (minFilter == GL.GL_LINEAR_MIPMAP_LINEAR);

    flippedX = false;
    flippedY = false;
    
    texUnit = -1;
    texUniform = -1;
  }

  /**
   * @invisible Generates a power-of-two box width and height so that width *
   *            height is closest to size.
   * @param size int
   */
  protected void calculateWidthHeight(int size) {
    int w, h;
    float l = PApplet.sqrt(size);
    for (w = 2; w < l; w *= 2)
      ;
    int n0 = w * w;
    int n1 = w * w / 2;
    if (PApplet.abs(n0 - size) < PApplet.abs(n1 - size))
      h = w;
    else
      h = w / 2;

    width = w;
    height = h;
  }
  
  /**
   * This class stores a frame to be copied to the texture.
   *
   */
  protected class PixelData {    
    int w, h;
    // Native buffer object.
    Object natBuf;
    // Buffer viewed as int.
    IntBuffer rgbBuf;
    
    PixelData(Object nat, IntBuffer rgb, int w, int h) {
      natBuf = nat;
      rgbBuf = rgb;
      this.w = w;
      this.h = h;
    }
    
    void dispose() {
      try {
        // Disposing the native buffer.        
        disposePixelsMethod.invoke(diposePixelsHandler, new Object[] { natBuf });
        natBuf = null;
        rgbBuf = null;
      } catch (Exception e) {
        e.printStackTrace();
      }      
    }
  }    
}
