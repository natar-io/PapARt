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

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import processing.core.PConstants;

import com.sun.opengl.cg.CGcontext;
import com.sun.opengl.cg.CGeffect;
import com.sun.opengl.cg.CGprogram;
import com.sun.opengl.cg.CgGL;

import java.util.*;

/**
 * @invisible This class that offers some utility methods to set and restore
 *            opengl state.
 */
public class GLState implements GLConstants, PConstants {
  public static boolean multiTexAvailable = true;
  public static boolean vbosAvailable = true;
  public static boolean fbosAvailable = true;
  public static boolean shadersAvailable = true;
  public static boolean geoShadersAvailable = true;
  public static boolean vertShadersAvailable = true;
  public static boolean fragShadersAvailable = true;
  public static boolean glsl100Available = true;
  public static boolean floatTexAvailable = true;
  public static boolean nonTwoPowTexAvailable = true;
  public static boolean fboMultisampleAvailable = true;

  public static GL gl;
  public static GLU glu;
  protected static long INSTANCES_COUNT = 0;

  // OPENGL renderer blending setup is blend enabled in BLEND mode.
  protected static boolean blendEnabled = true;
  protected static int blendMode = BLEND;
  protected static boolean blendEnabled0;
  protected static int blendMode0;  
  
  protected static boolean singleFBO;
  protected static boolean popFramebufferEnabled = true;
  protected static boolean pushFramebufferEnabled = true;
  protected static boolean framebufferFixed = false;

  protected static String glVersion;
  protected static int glMajor;
  protected static int glMinor;
  protected static String glslVersion;
  protected static int glMaxColorAttach;

  protected static GLFramebufferObject FBO;
  protected static GLFramebufferObject currentFBO;
  protected static GLFramebufferObject screenFBO;
  protected static GLTexture[] currentDestTex;
  protected static GLTexture[] emptyDestTex;
  protected static Stack<GLFramebufferObject> fboStack;
  protected static Stack<GLTexture[]> destTexStack;  
  
  static protected Set<Integer> glTextureObjects;
  static protected Set<Integer> glVertexBuffers;
  static protected Set<Integer> glFrameBuffers;
  static protected Set<Integer> glRenderBuffers;
  static protected Set<Integer> glslPrograms;
  static protected Set<Integer> glslShaders;
  static protected Set<Object> cgContexts;
  static protected Set<Object> cgPrograms;
  static protected Set<Object> cgEffects;
  
  public GLState(GL gl) {
    if (INSTANCES_COUNT == 0) {
      GLState.gl = gl;

      glTextureObjects = new HashSet<Integer>();
      glVertexBuffers = new HashSet<Integer>();
      glFrameBuffers = new HashSet<Integer>();
      glRenderBuffers = new HashSet<Integer>();
      glslPrograms = new HashSet<Integer>();
      glslShaders = new HashSet<Integer>();    
      cgContexts = new HashSet<Object>();      
      cgPrograms = new HashSet<Object>();
      cgEffects = new HashSet<Object>();
      
      getVersionNumbers();
      getAvailableExtensions();

      int[] val = { 0 };
      gl.glGetIntegerv(GL.GL_MAX_COLOR_ATTACHMENTS_EXT, val, 0);
      glMaxColorAttach = val[0];

      glu = new GLU();
      FBO = new GLFramebufferObject(gl);

      fboStack = new Stack<GLFramebufferObject>();
      destTexStack = new Stack<GLTexture[]>();
      screenFBO = new GLFramebufferObject(gl, true);
      currentFBO = screenFBO;
      emptyDestTex = new GLTexture[0];
      currentDestTex = emptyDestTex;

      singleFBO = false;
    }
    INSTANCES_COUNT++;
  }

  static public int createGLResource(int type) {
    return createGLResource(type, 0);
  }
  
  static public int createGLResource(int type, int param) {
    int id = 0;
    if (type == GL_TEXTURE_OBJECT) {
      int[] temp = new int[1];
      gl.glGenTextures(1, temp, 0);
      id = temp[0];
      glTextureObjects.add(id);
    } else if (type == GL_VERTEX_BUFFER) {
      int[] temp = new int[1];
      gl.glGenBuffersARB(1, temp, 0);
      id = temp[0];
      glVertexBuffers.add(id);
    } else if (type == GL_FRAME_BUFFER) {
      int[] temp = new int[1];
      gl.glGenFramebuffersEXT(1, temp, 0);
      id = temp[0];
      glFrameBuffers.add(id);    
    } else if (type == GL_RENDER_BUFFER) {
      int[] temp = new int[1];
      gl.glGenRenderbuffersEXT(1, temp, 0);
      id = temp[0];
      glRenderBuffers.add(id);
    } else if (type == GLSL_PROGRAM) {      
      id = gl.glCreateProgram();
      glslPrograms.add(id);
    } else if (type == GLSL_SHADER) {
      id = gl.glCreateShader(param);
      glslShaders.add(id);
    }    
    
    return id;
  }
  
  static public Object createCGResource(int type) {
    return createCGResource(type, null, null, 0);
  }
  
  static public Object createCGResource(int type, CGcontext context, String source) {
    return createCGResource(type, context, source, 0);
  }  
  
  static public Object createCGResource(int type, CGcontext context, String source, int profile) {
    Object id = null;
    
    if (type == CG_CONTEXT) {
      id = CgGL.cgCreateContext();
      cgContexts.add((CGcontext)id);
    } else if (type == CG_PROGRAM) {
      id = CgGL.cgCreateProgram(context, CgGL.CG_SOURCE, source, profile, null, null);
      cgPrograms.add((CGprogram)id);
    } else if (type == CG_EFFECT) {
      id = CgGL.cgCreateEffect(context, source, null);
      cgEffects.add((CGeffect)id);
    }
    
    return id;
  }
  
  static public void deleteGLResource(int id, int type) {
    if (type == GL_TEXTURE_OBJECT) {
      if (glTextureObjects.contains(id)) {
        int[] temp = { id };
        gl.glDeleteTextures(1, temp, 0);
        glTextureObjects.remove(id);
      }
    } else if (type == GL_VERTEX_BUFFER) {
      if (glVertexBuffers.contains(id)) {
        int[] temp = { id };
        gl.glDeleteBuffersARB(1, temp, 0);
        glVertexBuffers.remove(id);
      }
    } else if (type == GL_FRAME_BUFFER) {
      if (glFrameBuffers.contains(id)) {
        int[] temp = { id };
        gl.glDeleteFramebuffersEXT(1, temp, 0);
        glFrameBuffers.remove(id);
      }
    } else if (type == GL_RENDER_BUFFER) {
      if (glRenderBuffers.contains(id)) {
        int[] temp = { id };
        gl.glDeleteRenderbuffersEXT(1, temp, 0);
        glRenderBuffers.remove(id);
      }      
    } else if (type == GLSL_PROGRAM) {
      if (glslPrograms.contains(id)) {
        gl.glDeleteProgram(id);
        glslPrograms.remove(id);
      }
    } else if (type == GLSL_SHADER) {
      if (glslShaders.contains(id)) {
        gl.glDeleteShader(id);
        glslShaders.remove(id);
      }
    }    
  }  

  static public void deleteCGResource(Object id, int type) {
    if (type == CG_CONTEXT) {
      if (cgContexts.contains(id)) {
        CgGL.cgDestroyContext((CGcontext)id);
        cgContexts.remove(id);
      }
    } else if (type == CG_PROGRAM) {
      if (cgPrograms.contains(id)) {
        CgGL.cgDestroyProgram((CGprogram)id);
        cgPrograms.remove(id);
      }
    } else if (type == CG_EFFECT) {
      if (cgEffects.contains(id)) {
        CgGL.cgDestroyEffect((CGeffect)id);
        cgEffects.remove(id);
      }
    }    
  }
  
  // Releases any remaining OpenGL resources (including CG resources).
  static public void deleteAllGLResources() {
    if (!glTextureObjects.isEmpty()) {
      Object[] glids = glTextureObjects.toArray();
      for (int i = 0; i < glids.length; i++) {
        int id = ((Integer)glids[i]).intValue();
        int[] temp = { id };
        gl.glDeleteTextures(1, temp, 0);
      }
      glTextureObjects.clear();
    }
    
    if (!glVertexBuffers.isEmpty()) {
      Object[] glids = glVertexBuffers.toArray();
      for (int i = 0; i < glids.length; i++) {
        int id = ((Integer)glids[i]).intValue();
        int[] temp = { id };
        gl.glDeleteBuffersARB(1, temp, 0);
      }
      glVertexBuffers.clear();
    }
    
    if (!glFrameBuffers.isEmpty()) {
      Object[] glids = glFrameBuffers.toArray();
      for (int i = 0; i < glids.length; i++) {
        int id = ((Integer)glids[i]).intValue();
        int[] temp = { id };
        gl.glDeleteFramebuffersEXT(1, temp, 0);
      }
      glFrameBuffers.clear();
    }
    
    if (!glRenderBuffers.isEmpty()) {
      Object[] glids = glRenderBuffers.toArray();
      for (int i = 0; i < glids.length; i++) {
        int id = ((Integer)glids[i]).intValue();
        int[] temp = { id };
        gl.glDeleteRenderbuffersEXT(1, temp, 0);
      }
      glRenderBuffers.clear();
    }
    
    if (!glslPrograms.isEmpty()) {
      Object[] glids = glslPrograms.toArray();
      for (int i = 0; i < glids.length; i++) {
        int id = ((Integer)glids[i]).intValue();
        gl.glDeleteProgram(id);
      }
      glslPrograms.clear();
    }
    
    if (!glslShaders.isEmpty()) {
      Object[] glids = glslShaders.toArray();
      for (int i = 0; i < glids.length; i++) {
        int id = ((Integer)glids[i]).intValue();
        gl.glDeleteShader(id);
      }
      glslShaders.clear();
    }
    
    if (!cgContexts.isEmpty()) {
      Object[] glids = cgContexts.toArray();
      for (int i = 0; i < glids.length; i++) {
        Object id = glids[i];
        CgGL.cgDestroyContext((CGcontext)id);
      }
      cgContexts.clear();
    }
    
    if (!cgPrograms.isEmpty()) {
      Object[] glids = cgPrograms.toArray();
      for (int i = 0; i < glids.length; i++) {
        Object id = glids[i];
        CgGL.cgDestroyProgram((CGprogram)id);
      }      
      cgPrograms.clear();
    }
    
    if (!cgEffects.isEmpty()) {
      Object[] glids = cgEffects.toArray();
      for (int i = 0; i < glids.length; i++) {
        Object id = glids[i];
        CgGL.cgDestroyEffect((CGeffect)id);
      }
      cgEffects.clear();
    }
  }
    
  public void setOrthographicView(int w, int h) {
    gl.glViewport(0, 0, w, h);

    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();

    glu.gluOrtho2D(0.0, w, 0.0, h);

    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
  }

  public void saveView() {
    gl.glPushAttrib(GL.GL_VIEWPORT_BIT);
    saveGLMatrices();
  }

  public void restoreView() {
    restoreGLMatrices();
    gl.glPopAttrib();
  }

  public void saveGLState() {
    gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
    saveGLMatrices();
  }

  public void restoreGLState() {
    restoreGLMatrices();
    gl.glPopAttrib();
  }

  public void saveGLMatrices() {
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glPushMatrix();
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glPushMatrix();
  }

  public void restoreGLMatrices() {
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glPopMatrix();
  }

  public void clearColorBuffer(int color) {
    float r, g, b, a;
    int ir, ig, ib, ia;

    ia = (color >> 24) & 0xff;
    ir = (color >> 16) & 0xff;
    ig = (color >> 8) & 0xff;
    ib = color & 0xff;

    a = ia / 255.0f;
    r = ir / 255.0f;
    g = ig / 255.0f;
    b = ib / 255.0f;

    gl.glClearColor(r, g, b, a);
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);
  }

  public void copyTex(GLTexture srcTex, GLTexture destTex) {
    float uscale = srcTex.getMaxTextureCoordS();
    float vscale = srcTex.getMaxTextureCoordT();

    float cx = 0.0f;
    float sx = +1.0f;
    if (destTex.isFlippedX()) {
      cx = 1.0f;
      sx = -1.0f;
    }

    float cy = 0.0f;
    float sy = +1.0f;
    if (destTex.isFlippedY()) {
      cy = 1.0f;
      sy = -1.0f;
    }

    gl.glEnable(srcTex.getTextureTarget());

    gl.glActiveTexture(GL.GL_TEXTURE0);
    gl.glBindTexture(srcTex.getTextureTarget(), srcTex.getTextureID());

    pushFramebuffer();
    setFramebuffer(FBO);
    FBO.setDrawBuffer(destTex.getTextureTarget(), destTex.getTextureID());

    saveView();
    setOrthographicView(destTex.width, destTex.height);
    gl.glEnable(srcTex.getTextureTarget());
    gl.glActiveTexture(GL.GL_TEXTURE0);
    gl.glBindTexture(srcTex.getTextureTarget(), srcTex.getTextureID());
    gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    gl.glBegin(GL.GL_QUADS);
    gl.glTexCoord2f((cx + sx * 0.0f) * uscale, (cy + sy * 0.0f) * vscale);
    gl.glVertex2f(0.0f, 0.0f);

    gl.glTexCoord2f((cx + sx * 1.0f) * uscale, (cy + sy * 0.0f) * vscale);
    gl.glVertex2f(srcTex.width, 0.0f);

    gl.glTexCoord2f((cx + sx * 1.0f) * uscale, (cy + sy * 1.0f) * vscale);
    gl.glVertex2f(srcTex.width, srcTex.height);

    gl.glTexCoord2f((cx + sx * 0.0f) * uscale, (cy + sy * 1.0f) * vscale);
    gl.glVertex2f(0.0f, srcTex.height);
    gl.glEnd();
    gl.glBindTexture(srcTex.getTextureTarget(), 0);
    restoreView();

    popFramebuffer();
  }

  public void clearTex(int glid, int target, int color) {
    float r, g, b, a;
    int ir, ig, ib, ia;

    ia = (color >> 24) & 0xff;
    ir = (color >> 16) & 0xff;
    ig = (color >> 8) & 0xff;
    ib = color & 0xff;

    a = ia / 255.0f;
    r = ir / 255.0f;
    g = ig / 255.0f;
    b = ib / 255.0f;

    pushFramebuffer();
    setFramebuffer(FBO);
    FBO.setDrawBuffer(target, glid);

    gl.glClearColor(r, g, b, a);
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);

    popFramebuffer();
  }

  public void paintTex(int glid, int target, int w, int h, int color) {
    float r, g, b, a;
    int ir, ig, ib, ia;

    ia = (color >> 24) & 0xff;
    ir = (color >> 16) & 0xff;
    ig = (color >> 8) & 0xff;
    ib = color & 0xff;

    a = ia / 255.0f;
    r = ir / 255.0f;
    g = ig / 255.0f;
    b = ib / 255.0f;

    pushFramebuffer();
    setFramebuffer(FBO);
    FBO.setDrawBuffer(target, glid);

    saveView();
    setOrthographicView(w, h);
    gl.glColor4f(r, g, b, a);
    gl.glBegin(GL.GL_QUADS);
    gl.glVertex2f(0.0f, 0.0f);
    gl.glVertex2f(w, 0.0f);
    gl.glVertex2f(w, h);
    gl.glVertex2f(0.0f, h);
    gl.glEnd();
    restoreView();

    popFramebuffer();
  }

  protected void enableBlend() {
    blendEnabled = true;
    gl.glEnable(GL.GL_BLEND);
  }
  
  protected void disableBlend() {
    blendEnabled = false;
    gl.glDisable(GL.GL_BLEND);    
  }
  
  protected void saveBlendConfig() {
    blendEnabled0 = blendEnabled;
    blendMode0 = blendMode;
  }
  
  protected void restoreBlendConfig() {
    if (blendEnabled0) enableBlend(); 
    else disableBlend();
    setupBlending(blendMode0);
  }
  
  protected void setupBlending(int mode) {
    blendMode = mode; 
      
    if (blendMode == REPLACE) {  
      gl.glBlendEquation(GL.GL_FUNC_ADD);
      gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
    } else if (blendMode == BLEND) {
      gl.glBlendEquation(GL.GL_FUNC_ADD);
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    } else if (blendMode == ADD) {
      gl.glBlendEquation(GL.GL_FUNC_ADD);
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
    } else if (blendMode == SUBTRACT) {
      gl.glBlendEquation(GL.GL_FUNC_ADD);
      gl.glBlendFunc(GL.GL_ONE_MINUS_DST_COLOR, GL.GL_ZERO); 
    } else if (blendMode == LIGHTEST) {
      gl.glBlendEquation(GL.GL_MAX);
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_DST_ALPHA);
    } else if (blendMode == DARKEST) { 
      gl.glBlendEquation(GL.GL_MIN);      
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_DST_ALPHA);
    } else if (blendMode == DIFFERENCE) {
      gl.glBlendEquation(GL.GL_FUNC_REVERSE_SUBTRACT);
      gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE);
    } else if (blendMode == EXCLUSION) {
      gl.glBlendEquation(GL.GL_FUNC_ADD);
      gl.glBlendFunc(GL.GL_ONE_MINUS_DST_COLOR, GL.GL_ONE_MINUS_SRC_COLOR);
    } else if (blendMode == MULTIPLY) {
      gl.glBlendEquation(GL.GL_FUNC_ADD);      
      gl.glBlendFunc(GL.GL_DST_COLOR, GL.GL_SRC_COLOR);
    } else if (blendMode == SCREEN) {
      gl.glBlendEquation(GL.GL_FUNC_ADD);
      gl.glBlendFunc(GL.GL_ONE_MINUS_DST_COLOR, GL.GL_ONE);
    } else if (blendMode == BACKGROUND_ALPHA) {
      gl.glBlendColor(0.0f, 0.0f, 0.0f, 1.0f);
      gl.glBlendFunc(GL.GL_ONE, GL.GL_CONSTANT_COLOR);
    }
    // HARD_LIGHT, SOFT_LIGHT, OVERLAY, DODGE, BURN modes cannot be implemented
    // in fixed-function pipeline because they require conditional blending and
    // non-linear blending equations.    
  }
  
  protected void setupDefaultBlending() {
    // Default blending mode in PGraphicsOpenGL.
    blendMode = BLEND; 
    gl.glBlendEquation(GL.GL_FUNC_ADD);
    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);    
  }  
  
  static public void enableSingleFBO() {
    singleFBO = true;
  }

  static public void disableSingleFBO() {
    singleFBO = false;
  }

  static public boolean isSingleFBO() {
    return singleFBO;
  }

  static public void enablePopFramebuffer() {
    popFramebufferEnabled = true;
  }

  static public void disablePopFramebuffer() {
    popFramebufferEnabled = false;
  }

  static public boolean isPopFramebufferEnabled() {
    return popFramebufferEnabled;
  }

  static public void enablePushFramebuffer() {
    pushFramebufferEnabled = true;
  }

  static public void disablePushFramebuffer() {
    pushFramebufferEnabled = false;
  }

  static public boolean isPushFramebufferEnabled() {
    return pushFramebufferEnabled;
  }

  static public void setFramebufferFixed(boolean newVal) {
    framebufferFixed = newVal;
  }

  static public boolean isFramebufferFixed() {
    return framebufferFixed;
  }

  static public String getGLVersion() {
    return glVersion;
  }

  static public int getGLMajor() {
    return glMajor;
  }

  static public int getGLMinor() {
    return glMinor;
  }

  static public String getGLSLVersion() {
    return glslVersion;
  }

  public void pushFramebuffer() {
    if (pushFramebufferEnabled)
      fboStack.push(currentFBO);
  }

  public void setFramebuffer(GLFramebufferObject fbo) {
    if (framebufferFixed)
      return;

    currentFBO = fbo;
    currentFBO.bind();
  }

  public void popFramebuffer() {
    if (!popFramebufferEnabled)
      return;

    try {
      currentFBO = fboStack.pop();
      currentFBO.bind();
    } catch (EmptyStackException e) {
      System.out.println("Empty framebuffer stack");
    }
  }

  protected void pushDestTextures() {
    destTexStack.push(currentDestTex);
  }

  protected void setDestTexture(GLTexture destTex) {
    setDestTextures(new GLTexture[] { destTex }, 1);
  }

  protected void setDestTextures(GLTexture[] destTex) {
    setDestTextures(destTex, destTex.length);
  }

  protected void setDestTextures(GLTexture[] destTex, int n) {
    currentDestTex = destTex;
    currentFBO.setDrawBuffers(currentDestTex, n);
  }

  protected void popDestTextures() {
    try {
      currentDestTex = destTexStack.pop();
      currentFBO.setDrawBuffers(currentDestTex);
    } catch (EmptyStackException e) {
      System.out.println("Empty texture stack");
    }
  }

  protected void getVersionNumbers() {
    glVersion = gl.glGetString(GL.GL_VERSION);
    glMajor = Character.getNumericValue(glVersion.charAt(0));
    glMinor = Character.getNumericValue(glVersion.charAt(2));
    glslVersion = gl.glGetString(GL.GL_SHADING_LANGUAGE_VERSION_ARB);
  }

  protected void getAvailableExtensions() {
    // For a complete list of extensions, go to this sections in the openGL
    // registry:
    // http://www.opengl.org/registry/#arbextspecs
    // http://www.opengl.org/registry/#otherextspecs
    String extensions = gl.glGetString(GL.GL_EXTENSIONS);

    if (extensions.indexOf("GL_ARB_multitexture") == -1) {
      multiTexAvailable = false;
      System.out.println("GL_ARB_multitexture extension not available");
    }

    if (extensions.indexOf("GL_ARB_vertex_buffer_object") == -1) {
      vbosAvailable = false;
      System.out.println("GL_ARB_vertex_buffer_object extension not available");
    }

    if (extensions.indexOf("GL_EXT_framebuffer_object") == -1) {
      fbosAvailable = false;
      System.out.println("GL_EXT_framebuffer_object extension not available");
    }

    if (extensions.indexOf("GL_ARB_shader_objects") == -1) {
      shadersAvailable = false;
      System.out.println("GL_ARB_shader_objects extension not available");
    }

    if (extensions.indexOf("GL_EXT_geometry_shader4") == -1) {
      geoShadersAvailable = false;
      System.out.println("GL_ARB_geometry_shader4 extension not available");
    }

    if (extensions.indexOf("GL_ARB_vertex_shader") == -1) {
      vertShadersAvailable = false;
      System.out.println("GL_ARB_vertex_shader extension not available");
    }

    if (extensions.indexOf("GL_ARB_fragment_shader") == -1) {
      fragShadersAvailable = false;
      System.out.println("GL_ARB_fragment_shader extension not available");
    }

    if (extensions.indexOf("GL_ARB_shading_language_100") == -1) {
      glsl100Available = false;
      System.out.println("GL_ARB_shading_language_100 extension not available");
    }

    if (extensions.indexOf("GL_ARB_texture_float") == -1) {
      floatTexAvailable = false;
      System.out.println("GL_ARB_texture_float extension not available");
    }

    if (extensions.indexOf("GL_ARB_texture_non_power_of_two") == -1) {
      nonTwoPowTexAvailable = false;
      System.out
          .println("GL_ARB_texture_non_power_of_two extension not available");
    }

    if (extensions.indexOf("GL_EXT_framebuffer_multisample") == -1) {
      fboMultisampleAvailable = false;
      System.out
          .println("GL_EXT_framebuffer_multisample extension not available");
    }
  }
}
