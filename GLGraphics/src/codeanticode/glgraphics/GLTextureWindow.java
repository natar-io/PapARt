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

import java.awt.Frame;
import javax.media.opengl.*;

/**
 * An undecorated ligheweight AWT window to show just a single texture created
 * in the main OpenGL context of the parent PApplet. The renderer in parent must
 * be of type GLGraphics.
 */
public class GLTextureWindow extends GLWindow {
  protected PApplet parent;
  protected String name;
  protected boolean hasBorder;
  protected boolean resizable;
  protected GLRenderer renderer;
  protected GLCanvas canvas;
  protected Frame frame;  
  protected GLContext mainContext;
  protected GLCapabilities mainCaps;  
  protected GLTexture outTex;
  protected GLGraphics pgl;
  protected boolean restoreVisibility = false;
  
  /**
   * Creates a visible instance of GLTextureWindow with the specified size (w, h) and
   * position (x, y), which will show texture tex in it.
   * 
   * @param parent PApplet
   * @param tex GLTexture
   * @param x int
   * @param y int
   * @param w int
   * @param h int
   */  
  public GLTextureWindow(PApplet parent, int x, int y, int w, int h) {
    this(parent, x, y, w, h, true);
  }
  
  public GLTextureWindow(PApplet parent, int x, int y, int w, int h, boolean v) {
    this(parent, "texture window", x, y, w, h, v, false, false);
  }

  public GLTextureWindow(PApplet parent, int x, int y, int w, int h, boolean v, boolean b) {
    this(parent, "texture window", x, y, w, h, v, b, false);
  }
  
  /**
   * Creates an instance of GLTextureWindow with the specified size (w, h) and
   * position (x, y), which will show texture tex in it.
   * 
   * @param parent PApplet
   * @param tex GLTexture
   * @param name String          
   * @param x int
   * @param y int
   * @param w int
   * @param h int
   * @param v boolean
   * @param d boolean    
   * @param r boolean          
   */
  public GLTextureWindow(PApplet parent, String s, int x, int y, int w, int h, 
                         boolean v, boolean b, boolean r) {
    this.parent = parent;
    pgl = (GLGraphics) parent.g;

    name = s;
    x0 = x;
    y0 = y;
    width = w;
    height = h;
    visible = v;
    hasBorder = b;
    resizable = r;
    
    initialized = false;
    override = false;
    outTex = null;   
    
    pgl.addWindow(this);
  }
  
  public void init() {
    if (!initialized) {
      // The GL canvas of the AWT window must have exactly the same GL capabilities
      // as the main renderer for sharing with the main context to be possible.      
      mainContext = pgl.getContext(); 
      mainCaps = pgl.getCapabilities();
      
      initImpl(x0, y0, width, height);      
      frame.setVisible(visible);
      
      initialized = true; 
    } else if (mainContext != pgl.getContext()) {
      // The window has been initialized, but the main context in the      
      // renderer has changed. The frame, canvas, and renderer are
      // recreated.
      
      // Getting current context and capabilities of the main renderer.
      mainContext = pgl.getContext();  
      mainCaps = pgl.getCapabilities();
      
      // Getting current parameters of the frame.
      boolean v = frame.isVisible();
      int x = frame.getX();
      int y = frame.getY();
      int w = frame.getWidth();
      int h = frame.getHeight();

      frame.setVisible(false);
      initImpl(x, y, w, h);
      restoreVisibility = v;
    } else if (restoreVisibility) {
      frame.setVisible(true);
    }
  }

  
  protected void initImpl(int x, int y, int w, int h) {
    frame = new Frame(name);
    frame.setSize(w, h);
    frame.setLocation(x, y);
    if (!hasBorder) {
      frame.setUndecorated(true);        
    } else {
      if (!resizable) {
        frame.setResizable(false);
      }
    }      
    
    canvas = new GLCanvas(mainCaps, null, mainContext, null);
    renderer = new GLRenderer();
    canvas.addGLEventListener(renderer);
    frame.add(canvas);
  }
 
  
  public void delete() {
    if (frame != null) {
      frame.setVisible(false);
      //frame.removeAll();
      //frame.dispose();
      frame = null;
    }
    
    if (canvas != null) {
      canvas.removeGLEventListener(renderer);
      canvas.setEnabled(false);
      canvas = null;
    }
    
    if (renderer != null) {
      renderer.delete();
      renderer = null;
    }
    
    pgl.removeWindow(this);
  }
  
  public void setTexture(GLTexture tex) {
    outTex = tex;
  }

  /**
   * Sets the override property to the desired value.
   * 
   * @param override boolean
   */
  public void setOverride(boolean override) {
    this.override = override;
  }

  /**
   * Shows the window.
   */
  public void show() {
    if (frame != null) {
      frame.setVisible(true);
    }
  }
  
  /**
   * Hides the window.
   */
  public void hide() {
    if (frame != null) {
      frame.setVisible(false);
    }
  }

  /**
   * Returns whether the window is visible or not.
   * 
   * @return boolean
   */
  public boolean isVisible() {
    if (frame != null) {
      return frame.isVisible();
    } 
    return false;
  }

  /**
   * Returns the current width of the window.
   * 
   * @return int
   */  
  public int getWidth() {
    if (frame != null) {
      return frame.getWidth();
    }
    return 0;
  }

  /**
   * Returns the current height of the window.
   * 
   * @return int
   */  
  public int getHeight() {
    if (frame != null) {
      return frame.getHeight();
    }
    return 0;
  }

  /**
   * Returns the current X coordinate of the window (upper left corner).
   * 
   * @return int
   */  
  public int getX() {
    if (frame != null) {
      return frame.getX();
    } 
    return 0;
  }

  /**
   * Returns the current Y coordinate of the window (upper left corner).
   * 
   * @return int
   */    
  public int getY() {
    if (frame != null) {
      return frame.getY();
    }
    return 0;
  }

  /**
   * Returns the number of frames drawn so far.
   * 
   * @return int
   */    
  public int getFrameCount() {
    if (renderer != null) {
      return renderer.frameCount;
    }
    return 0;
  } 
  
  /**
   * Sets the texture tint color.
   * 
   * @param int color
   */
  public void tint(int color) {
    if (renderer != null) {
      int ir, ig, ib, ia;

      ia = (color >> 24) & 0xff;
      ir = (color >> 16) & 0xff;
      ig = (color >> 8) & 0xff;
      ib = color & 0xff;

      renderer.a = ia / 255.0f;
      renderer.r = ir / 255.0f;
      renderer.g = ig / 255.0f;
      renderer.b = ib / 255.0f;
    }
  }

  /**
   * Returns true or false depending on whether or not the internal renderer has
   * been initialized.
   * 
   * @return boolean
   */
  public boolean ready() {
    return initialized && frame != null && renderer != null && renderer.initalized;
  }

  /**
   * Draws the window, if the renderer has been initialized.
   */
  public void render() {
    if (ready()) {
      renderer.started = true;
      canvas.display();
    } 
  }

  /**
   * Sets the window location.
   * 
   * @param x int
   * @param y int          
   */  
  public void setLocation(int x, int y) {
    if (frame != null) {
      frame.setLocation(x, y);
    }
  }
  
  protected class GLRenderer implements GLEventListener {
    float r, g, b, a;
    GL gl;
    GLContext context;
    boolean initalized;
    boolean started;
    int frameCount;
    
    public GLRenderer() {
      super();
      initalized = false;
      started = false;
      r = g = b = a = 1.0f;
      frameCount = 0;
    }

    public void delete() {
      if (context != null) {
        context.destroy();
        context = null;
      }
    }
    
    public void init(GLAutoDrawable drawable) {
      gl = drawable.getGL();
      context = drawable.getContext();

      gl.glClearColor(0, 0, 0, 0);
      initalized = true;
    }

    public void display(GLAutoDrawable drawable) {
      if (!initalized || !started || (outTex == null))
        return;

      frameCount++;
      
      int w = drawable.getWidth();
      int h = drawable.getHeight();

      gl = drawable.getGL();
      context = drawable.getContext();
      detainContext();

      // Setting orthographics view to display the texture.
      gl.glViewport(0, 0, w, h);
      gl.glMatrixMode(GL.GL_PROJECTION);
      gl.glLoadIdentity();
      gl.glOrtho(0.0, w, 0.0, h, -100.0, +100.0);
      gl.glMatrixMode(GL.GL_MODELVIEW);
      gl.glLoadIdentity();

      float uscale = outTex.getMaxTextureCoordS();
      float vscale = outTex.getMaxTextureCoordT();

      float cx = 0.0f;
      float sx = +1.0f;
      if (outTex.isFlippedX()) {
        cx = 1.0f;
        sx = -1.0f;
      }

      float cy = 0.0f;
      float sy = +1.0f;
      if (outTex.isFlippedY()) {
        cy = 1.0f;
        sy = -1.0f;
      }

      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
      gl.glEnable(outTex.getTextureTarget());
      gl.glActiveTexture(GL.GL_TEXTURE0);
      gl.glBindTexture(outTex.getTextureTarget(), outTex.getTextureID());
      gl.glColor4f(r, g, b, a);
      gl.glBegin(GL.GL_QUADS);
      gl.glTexCoord2f((cx + sx * 0.0f) * uscale, (cy + sy * 1.0f) * vscale);
      gl.glVertex2f(0.0f, 0.0f);

      gl.glTexCoord2f((cx + sx * 1.0f) * uscale, (cy + sy * 1.0f) * vscale);
      gl.glVertex2f(w, 0.0f);

      gl.glTexCoord2f((cx + sx * 1.0f) * uscale, (cy + sy * 0.0f) * vscale);
      gl.glVertex2f(w, h);

      gl.glTexCoord2f((cx + sx * 0.0f) * uscale, (cy + sy * 0.0f) * vscale);
      gl.glVertex2f(0.0f, h);
      gl.glEnd();
      gl.glBindTexture(outTex.getTextureTarget(), 0);

      gl.glFlush();

      releaseContext();
      
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
        int height) {
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
        boolean deviceChanged) {   
    }

    protected void detainContext() {
      try {
        while (context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT) {
          Thread.sleep(10);          
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    protected void releaseContext() {
      context.release();
    }
  }
}
