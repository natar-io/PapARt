/**
 * This package provides classes to facilitate the handling of opengl textures, glsl shaders and 
 * off-screen rendering in Processing.
 * @author Andres Colubri
 * @author Ilias Bergstrom (http://www.onar3d.com/): Multisampled FBO option.
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

import com.sun.opengl.util.BufferUtil;

/**
 * This class implements OpenGL renderer for off-screen rendering. The result of the
 * frame rendering is available as a texture that can be obtained by calling the
 * @getTexture method. Multisampling for antialiased rendering can be used depending
 * on the hardware capabilities. 
 */
public class GLGraphicsOffScreen extends PGraphicsOpenGL implements GLConstants {
  // Framebuffer object and textures needed for the color, depth and stencil
  // buffers.
  protected GLFramebufferObject multisampleFBO;
  protected GLFramebufferObject FBO;
  protected int colorBufferMulti = 0;
  protected int depthStencilBuffer = 0;
  protected GLTexture colorTex;
  protected GLState glstate;
  protected GLCapabilities glcaps;

  protected int multisampleLevel = 4;
  protected boolean multisampleEnabled;
  protected GLTextureParameters colorTexParams;

  protected boolean glMode = false;
  protected float[] modelviewTM;

  // Light-related variables, taken out from PGraphics3D.

  /**
   * Maximum lights by default is 8, the minimum defined by OpenGL.
   */
  protected static final int MAX_LIGHTS_GL = 8;

  protected boolean lightsGL = false;  
  
  protected int lightCountGL = 0;

  /** Light types */
  protected int[] lightTypeGL;

  /** Light positions */
  protected float[][] lightPositionGL;

  /** Light direction (normalized vector) */
  protected float[][] lightNormalGL;

  /** Light falloff */
  protected float[] lightFalloffConstantGL;
  protected float[] lightFalloffLinearGL;
  protected float[] lightFalloffQuadraticGL;

  /** Light spot angle */
  protected float[] lightSpotAngleGL;

  /** Cosine of light spot angle */
  protected float[] lightSpotAngleCosGL;

  /** Light spot concentration */
  protected float[] lightSpotConcentrationGL;

  /**
   * Diffuse colors for lights. For an ambient light, this will hold the ambient
   * color. Internally these are stored as numbers between 0 and 1.
   */
  protected float[][] lightDiffuseGL;

  /**
   * Specular colors for lights. Internally these are stored as numbers between
   * 0 and 1.
   */
  protected float[][] lightSpecularGL;

  /** Current specular color for lighting */
  protected float[] currentLightSpecularGL;

  /** Current light falloff */
  protected float currentLightFalloffConstantGL;
  protected float currentLightFalloffLinearGL;
  protected float currentLightFalloffQuadraticGL;

  protected float[] zeroLightGL = { 0.0f, 0.0f, 0.0f, 1.0f };

  protected int saveLightCount;
  
  protected boolean lightsAllocated = false;

  protected GLGraphics mainRenderer;
    
  //////////////////////////////////////////////////////////////////////////////
  
  // INITIALIZATION, DRAWING
  
  /**
   * Default constructor.
   */
  /*
  public GLGraphicsOffScreen() {
    super();
  }
  */
  
  /**
   * Creates an instance of GLGraphicsOffScreen with specified size, without mulisampling.
   * 
   * @param parent the PApplet object of the calling sketch
   * @param iwidth desired width of the offscreen canvas.
   * @param iheight desired height of the offscreen canvas.
   */
  public GLGraphicsOffScreen(PApplet parent, int iwidth, int iheight) {
    super();
    multisampleEnabled = false;
    colorTexParams = new GLTextureParameters();
    multisampleLevel = 4;
    setParent(parent);
    setSize(iwidth, iheight);
  }

  /**
   * Creates an instance of GLGraphicsOffScreen with specified size and
   * enabled/disabled multisampling, according to the value of the argument
   * multi. If multisampling is enabled the multisampling level is set by
   * default to 4x.
   * 
   * @param parent the PApplet object of the calling sketch
   * @param iwidth desired width of the offscreen canvas.
   * @param iheight desired height of the offscreen canvas.
   * @param multi true or false to enable/disable multisampling.
   */
  public GLGraphicsOffScreen(PApplet parent, int iwidth, int iheight, boolean multi) {
    super();
    multisampleEnabled = multi;
    colorTexParams = new GLTextureParameters();
    multisampleLevel = 4;
    setParent(parent);
    setSize(iwidth, iheight);
  }

  /**
   * Creates an instance of GLGraphicsOffScreen with specified size and
   * enabled/disabled multisampling and multisampling level, according to the
   * values of the arguments multi and level.
   * 
   * @param parent the PApplet object of the calling sketch
   * @param iwidth desired width of the offscreen canvas.
   * @param iheight desired height of the offscreen canvas.
   * @param multi true or false to enable/disable multisampling.
   * @param level multisampling level (usually 2 or 4).
   */
  public GLGraphicsOffScreen(PApplet parent, int iwidth, int iheight,
      boolean multi, int level) {
    super();
    multisampleEnabled = multi;
    colorTexParams = new GLTextureParameters();
    multisampleLevel = level;
    setParent(parent);
    setSize(iwidth, iheight);
  }

  /**
   * Creates an instance of GLGraphicsOffScreen with specified size and
   * parameters for the drawing texture. Multisampling is disabled.
   * 
   * @param parent the PApplet object of the calling sketch
   * @param iwidth desired width of the offscreen canvas.
   * @param iheight desired height of the offscreen canvas.
   * @param params parameters of the texture used as drawing surface
   */
  public GLGraphicsOffScreen(PApplet parent, int iwidth, int iheight,
      GLTextureParameters params) {
    super();
    multisampleEnabled = false;
    colorTexParams = params;
    multisampleLevel = 4;
    setParent(parent);
    setSize(iwidth, iheight);
  }

  /**
   * Creates an instance of GLGraphicsOffScreen with specified size and
   * parameters for the destination texture. Multisampling is enabled/disabled
   * according to the value of the argument multi. If multisampling is enabled,
   * the multisampling level is set by default to 4x.
   * 
   * @param parent the PApplet object of the calling sketch
   * @param iwidth desired width of the offscreen canvas.
   * @param iheight desired height of the offscreen canvas.
   * @param params parameters of the texture used as drawing surface
   * @param multi true or false to enable/disable multisampling.
   */
  public GLGraphicsOffScreen(PApplet parent, int iwidth, int iheight,
      GLTextureParameters params, boolean multi) {
    super();
    multisampleEnabled = multi;
    colorTexParams = params;
    multisampleLevel = 4;
    setParent(parent);
    setSize(iwidth, iheight);
  }

  /**
   * Creates an instance of GLGraphicsOffScreen with specified size and
   * parameters for the destination texture. Multisampling is enabled/disabled
   * according to the value of the argument multi, and its level is contolled by
   * the argument level.
   * 
   * @param parent the PApplet object of the calling sketch
   * @param iwidth desired width of the offscreen canvas.
   * @param iheight desired height of the offscreen canvas.
   * @param params parameters of the texture used as drawing surface
   * @param multi true or false to enable/disable multisampling.
   * @param level multisampling level (usually 2 or 4).
   */
  public GLGraphicsOffScreen(PApplet parent, int iwidth, int iheight,
      GLTextureParameters params, boolean multi, int level) {
    super();
    multisampleEnabled = multi;
    colorTexParams = params;
    multisampleLevel = level;
    setParent(parent);
    setSize(iwidth, iheight);
  }

  /**
   * Returns the texture where this offscreen renderer draws to.
   * 
   * @return GLTexture object used as drawing surface 
   */
  public GLTexture getTexture() {
    return colorTex;
  }

  /**
   * Returns the current OpenGL capabilities associated to this renderer.
   * 
   * @return JOGL capabilities object
   */
  public GLCapabilities getCapabilities() {
    return glcaps;
  }  
  
  /**
   * Sets the size of the renderer's drawing surface.
   * 
   * @param iwidth new width
   * @param iwidth new height 
   */  
  public void setSize(int iwidth, int iheight) {
    super.setSize(iwidth, iheight);

    if (!lightsAllocated) {
      // Allocate lights arrays (in resize() instead of allocate() b/c needed by opengl).
      lightTypeGL = new int[MAX_LIGHTS_GL];
      lightPositionGL = new float[MAX_LIGHTS_GL][4];
      lightNormalGL = new float[MAX_LIGHTS_GL][4];
      lightDiffuseGL = new float[MAX_LIGHTS_GL][4];
      lightSpecularGL = new float[MAX_LIGHTS_GL][4];
      lightFalloffConstantGL = new float[MAX_LIGHTS_GL];
      lightFalloffLinearGL = new float[MAX_LIGHTS_GL];
      lightFalloffQuadraticGL = new float[MAX_LIGHTS_GL];
      lightSpotAngleGL = new float[MAX_LIGHTS_GL];
      lightSpotAngleCosGL = new float[MAX_LIGHTS_GL];
      lightSpotConcentrationGL = new float[MAX_LIGHTS_GL];
      currentLightSpecularGL = new float[4];
      lightsAllocated = true;
    }
  }

  /**
   * Frees all OpenGL resources associated to this renderer.
   */   
  public void delete() {
    releaseFramebuffer();    
  }

  /**
   * Starts a block of direct OpenGL calls. Saves the projection and
   * modelview matrices.
   */
  public GL beginGL() {
    super.beginGL();
    glMode = true;
    // We need to setup light falloff and specular 
    // coefficients because in beginDraw is done
    // for the parent's lights (non-OpenGL).
    lightFalloff(1, 0, 0);
    lightSpecular(0, 0, 0);    
    return gl;
  }

  /**
   * Ends a block of direct OpenGL calls.
   */   
  public void endGL() {
    glMode = false;
    super.endGL();
  }

  /**
   * Prepares the renderer for drawing a new frame.
   */  
  public void beginDraw() {
    // System.out.println("  Begin Draw");
    
    // We need to do some additional camera and projection handling when
    // the size of the offscreen surface is different from the main surface.
    boolean resize = (parent.width != width) || (parent.height != height);
    float eyeX, eyeY, dist; 
    eyeX = eyeY = dist = 0;
    
    // Saving current projection matrix.
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glPushMatrix();

    if (resize) {
      // Setting perspective projection with the appropriate parameters
      // for the size of this offscreen surface. Thanks to Aaron Meyers
      // for pointing out to this one.
      
      int w = width;
      int h = height;
      float halfFov, theTan, aspect;
      float screenFov = 60;
      eyeX = (float)w / 2.0f;
      eyeY = (float)h / 2.0f;
      halfFov = PI * screenFov / 360.0f;
      theTan = (float)Math.tan(halfFov);
      dist = eyeY / theTan;
      float nearDist = dist / 10.0f; // near / far clip plane
      float farDist = dist * 10.0f;
      aspect = (float)w/(float)h;
      
      gl.glLoadIdentity();
      perspective(screenFov * DEG_TO_RAD, aspect, nearDist, farDist);
    }
    
    super.beginDraw();

    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glPushMatrix();
    
    gl.glScalef(1.0f, -1.0f, 1.0f);
    
    if (resize) {
      gl.glLoadIdentity();
      camera(eyeX, eyeY, dist, eyeX, eyeY, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    if (resize) {
      // Saving current viewport.
      gl.glPushAttrib(GL.GL_VIEWPORT_BIT);

      // Setting the appropriate viewport.
      gl.glViewport(0, 0, width, height);
    }
        
    // Directing rendering to the texture...
    if (multisampleEnabled) {
      glstate.pushFramebuffer();
      glstate.setFramebuffer(multisampleFBO);
      gl.glDrawBuffer(GL.GL_COLOR_ATTACHMENT0_EXT);
    } else {
      glstate.pushFramebuffer();
      glstate.setFramebuffer(FBO);
      FBO.setDrawBuffer(colorTex);
    }

    // Clearing Z-buffer to ensure that the new elements are drawn properly.
    gl.glClearColor(0f, 0f, 0f, 0.0f);
    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

    // Setting the offscreen renderer in the main (GLGraphics) renderer so that
    // calls to transformation routines made to the GLGraphics renderer when
    // the offscreen renderer is enabled are directed to the offscreen renderer.
    if (mainRenderer != null) {
      mainRenderer.setOffScreenRenderer(this);

      // While doing off-screen rendering, the current light configuration of
      // the main renderer is shut-off
      // (but saved so it can restored after endDraw()).
      mainRenderer.saveLights();
      mainRenderer.disableLights();      
    }

    lightsGL = false;
    lightCountGL = 0;
    lightFalloff(1, 0, 0);
    lightSpecular(0, 0, 0);
  }

  /**
   * Cleans-up the drawing of last frame.
   */  
  public void endDraw() {
    boolean resize = (parent.width != width) || (parent.height != height);
    
    if (mainRenderer != null) {
      mainRenderer.setOffScreenRenderer(null);
      mainRenderer.restoreLights();
    }

    super.endDraw();

    // Restoring render to previous framebuffer.
    if (multisampleEnabled) {
      // In the case of multisampling, first copying the contents of the multisampled render buffer into the 
      // normal color buffer.
      gl.glBindFramebufferEXT(GL.GL_READ_FRAMEBUFFER_EXT, multisampleFBO.getFramebufferID()); // Source
      gl.glBindFramebufferEXT(GL.GL_DRAW_FRAMEBUFFER_EXT, FBO.getFramebufferID());            // Destination
      gl.glBlitFramebufferEXT(0, 0, width, height, 0, 0, width, height, GL.GL_COLOR_BUFFER_BIT, GL.GL_NEAREST);
      glstate.popFramebuffer();
    } else {
      glstate.popFramebuffer();
    }

    if (resize) {
      // Restoring previous viewport.
      gl.glPopAttrib();
    }       
    
    // Restoring previous GL matrices.
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glPopMatrix();
    
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glPopMatrix(); 

    // System.out.println("  End Draw");
  }

  /**
   * Enables/disables the use of the depth mask.
   * 
   * @param value the desired state for the use of depth masking. 
   */   
  public void setDepthMask(boolean value) {
    gl.glDepthMask(value);
  }
    
  /**
   * Switches multisampling back on when the renderer was created with 
   * multisampling enabled.
   */   
  public void enableMultisampling() {
    gl.glEnable(GL.GL_MULTISAMPLE);
  }  
  
  /**
   * Switches multisampling off when the renderer was created with 
   * multisampling enabled. This can be useful when trying to draw
   * piece of geometry without antialiasing on a multisampled offscreen
   * surface.
   */   
  public void disableMultisampling() {
    gl.glDisable(GL.GL_MULTISAMPLE);
  }
    
  //////////////////////////////////////////////////////////////////////////////
  
  // GEOMETRIC TRANSFORMATIONS   
  
  /**
   * Saves the current modelview matrix.
   */   
  public void pushMatrix() {
    if (glMode) gl.glPushMatrix();
    else super.pushMatrix();
  }

  /**
   * Restores the modelview matrix saved with the last call to
   * @see pushMatrix.
   */  
  public void popMatrix() {
    if (glMode) gl.glPopMatrix();
    else super.popMatrix();
  }

  public void translate(float tx, float ty) {
    if (glMode) gl.glTranslatef(tx, ty, 0.0f);
    else super.translate(tx, ty, 0.0f);
  }

  public void translate(float tx, float ty, float tz) {
    if (glMode) gl.glTranslatef(tx, ty, tz);
    else super.translate(tx, ty, tz);
  }

  public void rotate(float angle) {
    if (glMode) gl.glRotatef(PApplet.degrees(angle), 0.0f, 0.0f, 1.0f);
    else super.rotate(angle);
  }

  public void rotateX(float angle) {
    if (glMode) gl.glRotatef(PApplet.degrees(angle), 1.0f, 0.0f, 0.0f);
    else super.rotateX(angle);
  }

  public void rotateY(float angle) {
    if (glMode) gl.glRotatef(PApplet.degrees(angle), 0.0f, 1.0f, 0.0f);
    else super.rotateY(angle);
  }

  public void rotateZ(float angle) {
    if (glMode) gl.glRotatef(PApplet.degrees(angle), 0.0f, 0.0f, 1.0f);
    else super.rotateZ(angle);
  }

  public void rotate(float angle, float vx, float vy, float vz) {
    if (glMode) gl.glRotatef(PApplet.degrees(angle), vx, vy, vz);
    else super.rotate(angle, vx, vy, vz);
  }

  public void scale(float s) {
    if (glMode) gl.glScalef(s, s, s);
    else super.scale(s);
  }

  public void scale(float sx, float sy) {
    if (glMode) gl.glScalef(sx, sy, 1.0f);
    else super.scale(sx, sy);
  }

  public void scale(float x, float y, float z) {
    if (glMode) gl.glScalef(x, y, x);
    else super.scale(x, y, z);
  }

  //////////////////////////////////////////////////////////////////////////////
  
  // LIGHTS    
  
  public void lights() {
    if (glMode) {
      glEnableLights();

      // need to make sure colorMode is RGB 255 here
      int colorModeSaved = colorMode;
      colorMode = RGB;

      lightFalloff(1, 0, 0);
      lightSpecular(0, 0, 0);

      // No lights by default.
      ambientLight(colorModeX * 0.5f, colorModeY * 0.5f, colorModeZ * 0.5f);
      directionalLight(colorModeX * 0.5f, colorModeY * 0.5f, colorModeZ * 0.5f, 0, 0, -1);

      colorMode = colorModeSaved;
    } else
      super.lights();
  }

  public void saveLights() {
    if (glMode)
      saveLightCount = lightCountGL;
    else
      saveLightCount = lightCount;
  }

  public void restoreLights() {
    if (glMode) {
      lightCountGL = saveLightCount;
      for (int i = 0; i < lightCountGL; i++) {
        glLightEnable(i);
        if (lightTypeGL[i] == AMBIENT) {
          glLightAmbient(i);
          glLightPosition(i);
          glLightFalloff(i);
          glLightNoSpot(i);
          glLightNoDiffuse(i);
          glLightNoSpecular(i);                     
        } else if (lightTypeGL[i] == DIRECTIONAL) {
          glLightNoAmbient(i);
          glLightDirection(i);
          glLightDiffuse(i);
          glLightSpecular(i);
          glLightFalloff(i);
          glLightNoSpot(i);
        } else if (lightTypeGL[i] == POINT) {
          glLightNoAmbient(i);
          glLightPosition(i);
          glLightDiffuse(i);
          glLightSpecular(i);
          glLightFalloff(i);
          glLightNoSpot(i);
        } else if (lightTypeGL[i] == SPOT) {
          glLightNoAmbient(i);
          glLightPosition(i);
          glLightDirection(i);
          glLightDiffuse(i);
          glLightSpecular(i);
          glLightFalloff(i);
          glLightSpotAngle(i);
          glLightSpotConcentration(i);
        }
      }
    } else {
      lightCount = saveLightCount;
    }
  }

  public void noLights() {
    if (glMode) {
      glDisableLights();
      lightCountGL = 0;
    } else
      super.noLights();
  }

  /**
   * Add an ambient light based on the current color mode.
   */
  public void ambientLight(float r, float g, float b) {
    if (glMode)
      ambientLight(r, g, b, 0, 0, 0);
    else
      super.ambientLight(r, g, b, 0, 0, 0);
  }

  /**
   * Add an ambient light based on the current color mode. This version includes
   * an (x, y, z) position for situations where the falloff distance is used.
   */
  public void ambientLight(float r, float g, float b, float x, float y, float z) {
    if (!glMode) {
      super.ambientLight(r, g, b, x, y, z);
      return;
    }

    if (!lightsGL) {
      glEnableLights();
    }
        
    if (lightCountGL == MAX_LIGHTS_GL) {
      throw new RuntimeException("can only create " + MAX_LIGHTS_GL + " lights");
    }
    colorCalc(r, g, b);
    lightDiffuseGL[lightCountGL][0] = calcR;
    lightDiffuseGL[lightCountGL][1] = calcG;
    lightDiffuseGL[lightCountGL][2] = calcB;
    lightDiffuseGL[lightCountGL][3] = 1.0f;

    lightTypeGL[lightCountGL] = AMBIENT;
    lightFalloffConstantGL[lightCountGL] = currentLightFalloffConstantGL;
    lightFalloffLinearGL[lightCountGL] = currentLightFalloffLinearGL;
    lightFalloffQuadraticGL[lightCountGL] = currentLightFalloffQuadraticGL;
    lightPositionGL[lightCountGL][0] = x;
    lightPositionGL[lightCountGL][1] = y;
    lightPositionGL[lightCountGL][2] = z;
    lightPositionGL[lightCountGL][3] = 1.0f;

    glLightEnable(lightCountGL);
    glLightAmbient(lightCountGL);
    glLightPosition(lightCountGL);
    glLightFalloff(lightCountGL);
    glLightNoSpot(lightCountGL);
    glLightNoDiffuse(lightCountGL);
    glLightNoSpecular(lightCountGL);    

    lightCountGL++;
  }

  public void directionalLight(float r, float g, float b, float nx, float ny,
      float nz) {
    if (!glMode) {
      super.directionalLight(r, g, b, nx, ny, nz);
      return;
    }

    if (!lightsGL) {
      glEnableLights();
    }
        
    if (lightCountGL == MAX_LIGHTS_GL) {
      throw new RuntimeException("can only create " + MAX_LIGHTS_GL + " lights");
    }
    colorCalc(r, g, b);
    lightDiffuseGL[lightCountGL][0] = calcR;
    lightDiffuseGL[lightCountGL][1] = calcG;
    lightDiffuseGL[lightCountGL][2] = calcB;
    lightDiffuseGL[lightCountGL][3] = 1.0f;

    lightTypeGL[lightCountGL] = DIRECTIONAL;
    lightFalloffConstantGL[lightCountGL] = currentLightFalloffConstantGL;
    lightFalloffLinearGL[lightCountGL] = currentLightFalloffLinearGL;
    lightFalloffQuadraticGL[lightCountGL] = currentLightFalloffQuadraticGL;
    lightSpecularGL[lightCountGL][0] = currentLightSpecularGL[0];
    lightSpecularGL[lightCountGL][1] = currentLightSpecularGL[1];
    lightSpecularGL[lightCountGL][2] = currentLightSpecularGL[2];
    lightSpecularGL[lightCountGL][3] = currentLightSpecularGL[3];

    float invn = 1.0f / PApplet.dist(0, 0, 0, nx, ny, nz);
    lightNormalGL[lightCountGL][0] = invn * nx;
    lightNormalGL[lightCountGL][1] = invn * ny;
    lightNormalGL[lightCountGL][2] = invn * nz;
    lightNormalGL[lightCountGL][3] = 0.0f;

    glLightEnable(lightCountGL);
    glLightNoAmbient(lightCountGL);
    glLightDirection(lightCountGL);
    glLightDiffuse(lightCountGL);
    glLightSpecular(lightCountGL);
    glLightFalloff(lightCountGL);
    glLightNoSpot(lightCountGL);

    lightCountGL++;
  }

  public void pointLight(float r, float g, float b, float x, float y, float z) {
    if (!glMode) {
      super.pointLight(r, g, b, x, y, z);
      return;
    }

    if (!lightsGL) {
      glEnableLights();
    }
        
    if (lightCountGL == MAX_LIGHTS_GL) {
      throw new RuntimeException("can only create " + MAX_LIGHTS_GL + " lights");
    }
    colorCalc(r, g, b);
    lightDiffuseGL[lightCountGL][0] = calcR;
    lightDiffuseGL[lightCountGL][1] = calcG;
    lightDiffuseGL[lightCountGL][2] = calcB;
    lightDiffuseGL[lightCountGL][3] = 1.0f;

    lightTypeGL[lightCountGL] = POINT;
    lightFalloffConstantGL[lightCountGL] = currentLightFalloffConstantGL;
    lightFalloffLinearGL[lightCountGL] = currentLightFalloffLinearGL;
    lightFalloffQuadraticGL[lightCountGL] = currentLightFalloffQuadraticGL;
    lightSpecularGL[lightCountGL][0] = currentLightSpecularGL[0];
    lightSpecularGL[lightCountGL][1] = currentLightSpecularGL[1];
    lightSpecularGL[lightCountGL][2] = currentLightSpecularGL[2];

    lightPositionGL[lightCountGL][0] = x;
    lightPositionGL[lightCountGL][1] = y;
    lightPositionGL[lightCountGL][2] = z;
    lightPositionGL[lightCountGL][3] = 1.0f;

    glLightEnable(lightCountGL);
    glLightNoAmbient(lightCountGL);
    glLightPosition(lightCountGL);
    glLightDiffuse(lightCountGL);
    glLightSpecular(lightCountGL);
    glLightFalloff(lightCountGL);
    glLightNoSpot(lightCountGL);

    lightCountGL++;
  }

  public void spotLight(float r, float g, float b, float x, float y, float z,
      float nx, float ny, float nz, float angle, float concentration) {
    if (!glMode) {
      super.spotLight(r, g, b, x, y, z, nx, ny, nz, angle, concentration);
      return;
    }

    if (!lightsGL) {
      glEnableLights();
    }
        
    if (lightCountGL == MAX_LIGHTS_GL) {
      throw new RuntimeException("can only create " + MAX_LIGHTS_GL + " lights");
    }
    colorCalc(r, g, b);
    lightDiffuseGL[lightCountGL][0] = calcR;
    lightDiffuseGL[lightCountGL][1] = calcG;
    lightDiffuseGL[lightCountGL][2] = calcB;
    lightDiffuseGL[lightCountGL][3] = 1.0f;

    lightTypeGL[lightCountGL] = SPOT;
    lightFalloffConstantGL[lightCountGL] = currentLightFalloffConstantGL;
    lightFalloffLinearGL[lightCountGL] = currentLightFalloffLinearGL;
    lightFalloffQuadraticGL[lightCountGL] = currentLightFalloffQuadraticGL;
    lightSpecularGL[lightCountGL][0] = currentLightSpecularGL[0];
    lightSpecularGL[lightCountGL][1] = currentLightSpecularGL[1];
    lightSpecularGL[lightCountGL][2] = currentLightSpecularGL[2];

    lightPositionGL[lightCountGL][0] = x;
    lightPositionGL[lightCountGL][1] = y;
    lightPositionGL[lightCountGL][2] = z;
    lightPositionGL[lightCountGL][3] = 1.0f;

    float invn = 1.0f / PApplet.dist(0, 0, 0, nx, ny, nz);
    lightNormalGL[lightCountGL][0] = invn * nx;
    lightNormalGL[lightCountGL][1] = invn * ny;
    lightNormalGL[lightCountGL][2] = invn * nz;
    lightNormalGL[lightCountGL][3] = 0.0f;

    lightSpotAngleGL[lightCountGL] = PApplet.degrees(angle);
    lightSpotAngleCosGL[lightCountGL] = Math.max(0, (float) Math.cos(angle));
    lightSpotConcentrationGL[lightCountGL] = concentration;

    glLightEnable(lightCountGL);
    glLightNoAmbient(lightCountGL);
    glLightPosition(lightCountGL);
    glLightDirection(lightCountGL);
    glLightDiffuse(lightCountGL);
    glLightSpecular(lightCountGL);
    glLightFalloff(lightCountGL);
    glLightSpotAngle(lightCountGL);
    glLightSpotConcentration(lightCountGL);

    lightCountGL++;
  }

  /**
   * Set the light falloff rates for the last light that was created. Default is
   * lightFalloff(1, 0, 0).
   */
  public void lightFalloff(float constant, float linear, float quadratic) {
    if (!glMode) {
      super.lightFalloff(constant, linear, quadratic);
      return;
    }

    currentLightFalloffConstantGL = constant;
    currentLightFalloffLinearGL = linear;
    currentLightFalloffQuadraticGL = quadratic;
  }

  /**
   * Set the specular color of the last light created.
   */
  public void lightSpecular(float x, float y, float z) {
    if (!glMode) {
      super.lightSpecular(x, y, z);
      return;
    }

    colorCalc(x, y, z);
    currentLightSpecularGL[0] = calcR;
    currentLightSpecularGL[1] = calcG;
    currentLightSpecularGL[2] = calcB;
    currentLightSpecularGL[3] = 1.0f;
  }

  private void glEnableLights() {
    lightsGL = true;
    gl.glEnable(GL.GL_LIGHTING);
    gl.glEnable(GL.GL_COLOR_MATERIAL);
    gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);    
  }

  private void glDisableLights() {
    lightsGL = false;
    gl.glDisable(GL.GL_LIGHTING);    
  }
    
  private void glLightAmbient(int num) {
    gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_AMBIENT, lightDiffuseGL[num], 0);
  }

  private void glLightNoAmbient(int num) {
    gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_AMBIENT, zeroLightGL, 0);
  }

  private void glLightNoSpot(int num) {
    gl.glLightf(GL.GL_LIGHT0 + num, GL.GL_SPOT_CUTOFF, 180);
    gl.glLightf(GL.GL_LIGHT0 + num, GL.GL_SPOT_EXPONENT, 0);
  }

  private void glLightDiffuse(int num) {
    gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_DIFFUSE, lightDiffuseGL[num], 0);
  }

  private void glLightNoDiffuse(int num) {
    gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_DIFFUSE, zeroLightGL, 0);
  }
    
  private void glLightDirection(int num) {
    if (lightTypeGL[num] == DIRECTIONAL) {
      gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_POSITION, lightNormalGL[num], 0);
    } else { // spotlight
      // this one only needs the 3 arg version
      gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_SPOT_DIRECTION,
          lightNormalGL[num], 0);
    }
  }

  private void glLightEnable(int num) {
    gl.glEnable(GL.GL_LIGHT0 + num);
  }

  private void glLightDisable(int num) {
    gl.glDisable(GL.GL_LIGHT0 + num);
  }

  private void glLightFalloff(int num) {
    gl.glLightf(GL.GL_LIGHT0 + num, GL.GL_CONSTANT_ATTENUATION,
        lightFalloffConstantGL[num]);
    gl.glLightf(GL.GL_LIGHT0 + num, GL.GL_LINEAR_ATTENUATION,
        lightFalloffLinearGL[num]);
    gl.glLightf(GL.GL_LIGHT0 + num, GL.GL_QUADRATIC_ATTENUATION,
        lightFalloffQuadraticGL[num]);
  }

  private void glLightPosition(int num) {
    gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_POSITION, lightPositionGL[num], 0);
  }

  private void glLightSpecular(int num) {
    gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_SPECULAR, lightSpecularGL[num], 0);
  }

  private void glLightNoSpecular(int num) {
    gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_SPECULAR, zeroLightGL, 0);
  }    
  
  private void glLightSpotAngle(int num) {
    gl.glLightf(GL.GL_LIGHT0 + num, GL.GL_SPOT_CUTOFF, lightSpotAngleGL[num]);
  }

  private void glLightSpotConcentration(int num) {
    gl.glLightf(GL.GL_LIGHT0 + num, GL.GL_SPOT_EXPONENT,
        lightSpotConcentrationGL[num]);
  }

  protected void fillFromCalc() {
    fill = true;
    fillR = calcR;
    fillG = calcG;
    fillB = calcB;
    fillA = calcA;
    fillRi = calcRi;
    fillGi = calcGi;
    fillBi = calcBi;
    fillAi = calcAi;
    fillColor = calcColor;
    fillAlpha = calcAlpha;
    
    ambientR = calcR;
    ambientG = calcG;
    ambientB = calcB;
    
    if (colorBuffer == null) {
      colorBuffer = new float[4];
    }
    colorBuffer[0] = calcR;
    colorBuffer[1] = calcG;
    colorBuffer[2] = calcB;
    colorBuffer[3] = calcA;
  
    // We don't call glMaterialfv because GL_COLOR_MATERIAL
    // is enabled, so the ambient and diffuse color is taken
    // from glColor().
    //gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, colorBuffer, 0);
  }  
  
  protected void ambientFromCalc() {
    ambientR = calcR;
    ambientG = calcG;
    ambientB = calcB;
    if (colorBuffer == null) {
      colorBuffer = new float[4];
    }
    colorBuffer[0] = calcR;
    colorBuffer[1] = calcG;
    colorBuffer[2] = calcB;
    colorBuffer[3] = calcA;
    
    // We don't call glMaterialfv because GL_COLOR_MATERIAL
    // is enabled, so the ambient and diffuse color is taken
    // from glColor().   
    //gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, colorBuffer, 0);
  }  
  
  //////////////////////////////////////////////////////////////////////////////
  
  // CAMERA, PROJECTION  
  
  public void endCamera() {
    super.endCamera();
    if (glMode) loadGLModelviewMatrix();
  }

  public void camera() {
    camera(cameraX, cameraY, cameraZ, cameraX, cameraY, 0, 0, 1, 0);
  }

  public void camera(float eyeX, float eyeY, float eyeZ, float centerX,
      float centerY, float centerZ, float upX, float upY, float upZ) {
    super.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    if (glMode) loadGLModelviewMatrix();
  }

  public void ortho() {
    ortho(0, width, 0, height, -10, 10);
  }

  public void ortho(float left, float right, float bottom, float top,
      float near, float far) {
    // PGraphicsOpenGL.ortho() Will call updateProjection(), which copies the
    // projection matrix to opengl, so we don't need to do ourselves here.
    super.ortho(left, right, bottom, top, near, far);
    gl.glMatrixMode(GL.GL_MODELVIEW); // We leave the modelview as the active
                                      // matrix.
  }

  public void perspective() {
    perspective(cameraFOV, cameraAspect, cameraNear, cameraFar);
  }

  public void perspective(float fov, float aspect, float near, float far) {
    super.perspective(fov, aspect, near, far); // Will call frustum()
  }

  public void frustum(float left, float right, float bottom, float top,
      float near, float far) {
    // PGraphicsOpenGL.frustum() Will call updateProjection(), which copies the
    // projection matrix to opengl, so we don't need to do ourselves here.
    super.frustum(left, right, bottom, top, near, far);
    gl.glMatrixMode(GL.GL_MODELVIEW); // We leave the modelview as the active
                                      // matrix.
  }

  //////////////////////////////////////////////////////////////////////////////
  
  // MODEL RENDERING   
  
  public void model(GLModel model) {
    model.render();
  }

  public void model(GLModel model, GLModelEffect effect) {
    model.render(effect);
  }

  public void model(GLModel model, int first, int last) {
    model.render(first, last, null);
  }

  public void model(GLModel model, int first, int last, GLModelEffect effect) {
    model.render(first, last, effect);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  
  // BLENDING    
  
  public void noBlend() {
    glstate.disableBlend();
  }

  public void setDefaultBlend() {
    glstate.enableBlend();
    glstate.setupDefaultBlending();
  }
    
  public void setBlendMode(int MODE) {
    glstate.enableBlend();
    glstate.setupBlending(MODE);
  }  

  //////////////////////////////////////////////////////////////////////////////
  
  // DRAWING SURFACE CLEAR
  
  public void clear(int gray) {
    int c = parent.color(gray);
    glstate.clearColorBuffer(c);
  }

  public void clear(float gray) {
    int c = parent.color(gray);
    glstate.clearColorBuffer(c);
  }

  public void clear(int gray, int alpha) {
    int c = parent.color(gray, alpha);
    glstate.clearColorBuffer(c);
  }

  public void clear(int rgb, float alpha) {
    int c = parent.color(rgb, alpha);
    glstate.clearColorBuffer(c);
  }

  public void clear(float gray, float alpha) {
    int c = parent.color(gray, alpha);
    glstate.clearColorBuffer(c);
  }

  public void clear(int x, int y, int z) {
    int c = parent.color(x, y, z);
    glstate.clearColorBuffer(c);
  }

  public void clear(float x, float y, float z) {
    int c = parent.color(x, y, z);
    glstate.clearColorBuffer(c);
  }

  public void clear(int x, int y, int z, int a) {
    int c = parent.color(x, y, z, a);
    glstate.clearColorBuffer(c);
  }

  public void clear(float x, float y, float z, float a) {
    int c = parent.color(x, y, z, a);
    glstate.clearColorBuffer(c);
  }
  
  //////////////////////////////////////////////////////////////

  // LOAD/UPDATE PIXELS  
  
  /**
   * Creates the pixels array for the surface and the internal texture.
   * But it doesn't copy the contents of the texture to them.
   */
  public void loadPixels() {
    if ((pixels == null) || (pixels.length != width*height)) {
      pixels = new int[width * height];
      pixelBuffer = BufferUtil.newIntBuffer(pixels.length);
    }    
    colorTex.loadPixels();   
  }

  /**
   * It copies the contents of the internal texture to both
   * pixels arrays of the surface and the texture object.
   */  
  public void loadTexture() {
    // Copies the contents of colorTex into its pixel array:
    colorTex.loadTexture();
    // and then in copies these pixels to the surface pixels.
    PApplet.arrayCopy(colorTex.pixels, pixels);
  }
  
  
  /**
   * It copies the contents of surface's pixels to the
   * internal texture.
   * 
   */   
  public void updatePixels() {
    colorTex.putBuffer(pixels);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  
  // OPENGL IMPLEMENTATION  
  
  // Setup offscreen framebuffer.
  protected void initFramebuffer() {
    int stat;

    gl = context.getGL();

    glstate = new GLState(gl);

    // Creating arrays for FBO and depth&stencil buffer.
    FBO = new GLFramebufferObject(gl);

    // Create depth and stencil buffers.
    depthStencilBuffer = GLState.createGLResource(GL_RENDER_BUFFER);

    // Creating color texture.
    colorTex = new GLTexture(parent, width, height, colorTexParams);

    if (multisampleEnabled) {
      multisampleFBO = new GLFramebufferObject(gl);

      // Creating handle for multisample color buffer.
      colorBufferMulti = GLState.createGLResource(GL_RENDER_BUFFER);
      
      // Bind render buffer.
      gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, colorBufferMulti);
      
      gl.glRenderbufferStorageMultisampleEXT(GL.GL_RENDERBUFFER_EXT, multisampleLevel, 
                                             GL.GL_RGBA8, width, height);

      // Bind depth-stencil buffer.
      gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);
      
      // Allocating space for multisample depth buffer
      gl.glRenderbufferStorageMultisampleEXT(GL.GL_RENDERBUFFER_EXT, multisampleLevel, 
                                             GL_DEPTH24_STENCIL8, width, height);

      // Creating handle for multisample FBO
      glstate.pushFramebuffer();
      glstate.setFramebuffer(multisampleFBO);

      gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, 
                                      GL.GL_RENDERBUFFER_EXT, colorBufferMulti);
      gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, 
                                      GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);
      gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_STENCIL_ATTACHMENT_EXT, 
                                      GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);

      // Clearing all the bound buffers.
      gl.glClearColor(0f, 0f, 0f, 0.0f);
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
      
      glstate.pushFramebuffer();
      glstate.setFramebuffer(FBO);

      gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, 
                                   GL.GL_TEXTURE_2D, colorTex.getTextureID(), 0);

          stat = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
      GLUtils.printFramebufferError(stat);

      // Clearing color buffer only.
      gl.glClearColor(0f, 0f, 0f, 0.0f);
      gl.glClear(GL.GL_COLOR_BUFFER_BIT);      
      
      glstate.popFramebuffer();
      glstate.popFramebuffer();
    } else {
      // / Regular, no multisampling, FBO setup.
      gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);
      gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, GL_DEPTH24_STENCIL8, width, height);

      glstate.pushFramebuffer();
      glstate.setFramebuffer(FBO);

      gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, 
                                   colorTex.getTextureTarget(), colorTex.getTextureID(), 0);
      gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, 
                                      GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);
      gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_STENCIL_ATTACHMENT_EXT, 
                                      GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);

      FBO.checkFBO();

      // Clearing all the bound buffers.
      gl.glClearColor(0f, 0f, 0f, 0.0f);
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
            
      glstate.popFramebuffer();
    }
  }

  protected void releaseFramebuffer() {
    if (colorBufferMulti != 0) {
      GLState.deleteGLResource(colorBufferMulti, GL_RENDER_BUFFER);
    }

    if (depthStencilBuffer != 0) {
      GLState.deleteGLResource(depthStencilBuffer, GL_RENDER_BUFFER);
    }    
  }  

  protected void allocate() {
    if (glstate == null) {
      PGraphicsOpenGL pgl = (PGraphicsOpenGL) parent.g;
      if (pgl instanceof GLGraphics) {
        glcaps = ((GLGraphics) pgl).getCapabilities();

        mainRenderer = (GLGraphics) pgl;
      } else {
        glcaps = null;

        mainRenderer = null;
      }
      context = pgl.getContext();
      drawable = null;

      initFramebuffer();
      settingsInited = false;
    } else
      reapplySettings();
  }
    
  protected void renderTriangles(int start, int stop) {
    report("render_triangles in");

    for (int i = start; i < stop; i++) {
      float a[] = vertices[triangles[i][VERTEX1]];
      float b[] = vertices[triangles[i][VERTEX2]];
      float c[] = vertices[triangles[i][VERTEX3]];

      // This is only true when not textured.
      // We really should pass specular straight through to triangle rendering.
      float ar = clamp(triangleColors[i][0][TRI_DIFFUSE_R]
          + triangleColors[i][0][TRI_SPECULAR_R]);
      float ag = clamp(triangleColors[i][0][TRI_DIFFUSE_G]
          + triangleColors[i][0][TRI_SPECULAR_G]);
      float ab = clamp(triangleColors[i][0][TRI_DIFFUSE_B]
          + triangleColors[i][0][TRI_SPECULAR_B]);
      float br = clamp(triangleColors[i][1][TRI_DIFFUSE_R]
          + triangleColors[i][1][TRI_SPECULAR_R]);
      float bg = clamp(triangleColors[i][1][TRI_DIFFUSE_G]
          + triangleColors[i][1][TRI_SPECULAR_G]);
      float bb = clamp(triangleColors[i][1][TRI_DIFFUSE_B]
          + triangleColors[i][1][TRI_SPECULAR_B]);
      float cr = clamp(triangleColors[i][2][TRI_DIFFUSE_R]
          + triangleColors[i][2][TRI_SPECULAR_R]);
      float cg = clamp(triangleColors[i][2][TRI_DIFFUSE_G]
          + triangleColors[i][2][TRI_SPECULAR_G]);
      float cb = clamp(triangleColors[i][2][TRI_DIFFUSE_B]
          + triangleColors[i][2][TRI_SPECULAR_B]);

      int textureIndex = triangles[i][TEXTURE_INDEX];
      if (textureIndex != -1) {
        report("before enable");
        gl.glEnable(GL.GL_TEXTURE_2D);
        report("after enable");

        float uscale = 1.0f;
        float vscale = 1.0f;

        PImage texture = textures[textureIndex];
        if (texture instanceof GLTexture) {
          GLTexture tex = (GLTexture) texture;
          tex.bind(0);
          //gl.glBindTexture(tex.getTextureTarget(), tex.getTextureID());

          uscale *= tex.getMaxTextureCoordS();
          vscale *= tex.getMaxTextureCoordT();

          float cx = 0.0f;
          float sx = +1.0f;
          if (tex.isFlippedX()) {
            cx = 1.0f;
            sx = -1.0f;
          }

          float cy = 0.0f;
          float sy = +1.0f;
          if (tex.isFlippedY()) {
            cy = 1.0f;
            sy = -1.0f;
          }

          gl.glBegin(GL.GL_TRIANGLES);

          gl.glColor4f(ar, ag, ab, a[A]);
          gl.glTexCoord2f((cx + sx * a[U]) * uscale, (cy + sy * a[V]) * vscale);
          gl.glNormal3f(a[NX], a[NY], a[NZ]);
          gl.glEdgeFlag(a[EDGE] == 1);
          gl.glVertex3f(a[VX], a[VY], a[VZ]);

          gl.glColor4f(br, bg, bb, b[A]);
          gl.glTexCoord2f((cx + sx * b[U]) * uscale, (cy + sy * b[V]) * vscale);
          gl.glNormal3f(b[NX], b[NY], b[NZ]);
          gl.glEdgeFlag(a[EDGE] == 1);
          gl.glVertex3f(b[VX], b[VY], b[VZ]);

          gl.glColor4f(cr, cg, cb, c[A]);
          gl.glTexCoord2f((cx + sx * c[U]) * uscale, (cy + sy * c[V]) * vscale);
          gl.glNormal3f(c[NX], c[NY], c[NZ]);
          gl.glEdgeFlag(a[EDGE] == 1);
          gl.glVertex3f(c[VX], c[VY], c[VZ]);

          gl.glEnd();

          //gl.glBindTexture(tex.getTextureTarget(), 0);
          tex.unbind();
        } else {
          // Default texturing using a PImage.

          report("before bind");
          bindTexture(texture);
          report("after bind");

          ImageCache cash = (ImageCache) texture.getCache(this);
          uscale = (float) texture.width / (float) cash.twidth;
          vscale = (float) texture.height / (float) cash.theight;

          gl.glBegin(GL.GL_TRIANGLES);

          // System.out.println(a[U] + " " + a[V] + " " + uscale + " " +
          // vscale);
          // System.out.println(ar + " " + ag + " " + ab + " " + a[A]);
          // ar = ag = ab = 1;
          gl.glColor4f(ar, ag, ab, a[A]);
          gl.glTexCoord2f(a[U] * uscale, a[V] * vscale);
          gl.glNormal3f(a[NX], a[NY], a[NZ]);
          gl.glEdgeFlag(a[EDGE] == 1);
          gl.glVertex3f(a[VX], a[VY], a[VZ]);

          gl.glColor4f(br, bg, bb, b[A]);
          gl.glTexCoord2f(b[U] * uscale, b[V] * vscale);
          gl.glNormal3f(b[NX], b[NY], b[NZ]);
          gl.glEdgeFlag(a[EDGE] == 1);
          gl.glVertex3f(b[VX], b[VY], b[VZ]);

          gl.glColor4f(cr, cg, cb, c[A]);
          gl.glTexCoord2f(c[U] * uscale, c[V] * vscale);
          gl.glNormal3f(c[NX], c[NY], c[NZ]);
          gl.glEdgeFlag(a[EDGE] == 1);
          gl.glVertex3f(c[VX], c[VY], c[VZ]);

          gl.glEnd();

          report("non-binding 6");

          gl.glDisable(GL.GL_TEXTURE_2D);
        }
      } else {
        // no texture
        gl.glBegin(GL.GL_TRIANGLES);

        gl.glColor4f(ar, ag, ab, a[A]);
        gl.glNormal3f(a[NX], a[NY], a[NZ]);
        gl.glEdgeFlag(a[EDGE] == 1);
        gl.glVertex3f(a[VX], a[VY], a[VZ]);

        gl.glColor4f(br, bg, bb, b[A]);
        gl.glNormal3f(b[NX], b[NY], b[NZ]);
        gl.glEdgeFlag(a[EDGE] == 1);
        gl.glVertex3f(b[VX], b[VY], b[VZ]);

        gl.glColor4f(cr, cg, cb, c[A]);
        gl.glNormal3f(c[NX], c[NY], c[NZ]);
        gl.glEdgeFlag(a[EDGE] == 1);
        gl.glVertex3f(c[VX], c[VY], c[VZ]);

        gl.glEnd();
      }
    }

    report("render_triangles out");
  }

  protected void loadGLModelviewMatrix() {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    copyModelviewTM();
    gl.glLoadMatrixf(modelviewTM, 0);
  }

  protected void multGLModelviewMatrix() {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    copyModelviewTM();
    gl.glMultMatrixf(modelviewTM, 0);
  }

  protected void copyModelviewTM() {
    if (modelviewTM == null)
      modelviewTM = new float[16];

    modelviewTM[0] = modelview.m00;
    modelviewTM[1] = modelview.m10;
    modelviewTM[2] = modelview.m20;
    modelviewTM[3] = modelview.m30;

    modelviewTM[4] = modelview.m01;
    modelviewTM[5] = modelview.m11;
    modelviewTM[6] = modelview.m21;
    modelviewTM[7] = modelview.m31;

    modelviewTM[8] = modelview.m02;
    modelviewTM[9] = modelview.m12;
    modelviewTM[10] = modelview.m22;
    modelviewTM[11] = modelview.m32;

    modelviewTM[12] = modelview.m03;
    modelviewTM[13] = modelview.m13;
    modelviewTM[14] = modelview.m23;
    modelviewTM[15] = modelview.m33;
  }  
}
