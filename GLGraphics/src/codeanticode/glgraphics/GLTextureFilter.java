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
import processing.xml.*;

import javax.media.opengl.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * This class defines a 2D filter to apply on GLTexture objects. A filter is
 * basically a glsl shader program with a set of predefined uniform attributes
 * and a 2D grid where the input textures are mapped on. The points of the 2D
 * grid can be altered in the vertex stage of the filter, allowing for arbitrary
 * distortions in the shape of the mesh. The filter is specified in a xml file
 * where the files names of the vertex and fragment shaders stored, as well as
 * the definition of the grid (resolution and spacing).
 */
public class GLTextureFilter implements GLConstants, PConstants {
  protected PApplet parent;
  protected GL gl;
  int polyMode;
  protected PGraphicsOpenGL pgl;
  protected GLState glstate;
  protected GLFramebufferObject destFBO;
  protected GLTextureGrid grid;
  protected HashMap<String, GLTextureFilterParameter> paramsHashMap;
  protected GLTextureFilterParameter[] paramsArray;
  protected String filterName;
  protected String description;
  protected boolean blend;
  protected int blendMode;
  protected GLShader shader;
  protected int numInputTex;
  protected int numOutputTex;
  
  protected String[] srcTexNames;
  protected String[] srcTexOffsetNames;
  protected String clockDataName;
  protected String destColorName;
  protected String destTexSizeName;
  protected GLTextureFilterParameter[] srcTexUnitParams;
  protected GLTextureFilterParameter[] srcTexOffsetParams;
  protected GLTextureFilterParameter clockDataParam;
  protected GLTextureFilterParameter destColorParam;
  protected GLTextureFilterParameter destTexSizeParam;
  
  protected String vertexFN;
  protected String geometryFN;
  protected String fragmentFN;
  
  protected String inGeoPrim;
  protected String outGeoPrim;
  protected int maxNumOutVert;  
  
  /**
   * Default constructor.
   */
  public GLTextureFilter() {
    this.parent = null;
  }

  /**
   * Creates an instance of GLTextureFilter, loading the filter from filename.
   * 
   * @param parent PApplet
   * @param filename String
   */
  public GLTextureFilter(PApplet parent, String filename) {
    this.parent = parent;
    initFilter(filename);
  }
  
  public void delete() {
    destFBO.delete();
    grid.delete();
    shader.delete();
  }

  /**
   * Creates an instance of GLTextureFilter, loading the filter from a URL.
   */
  public GLTextureFilter(PApplet parent, URL url) {
    this.parent = parent;
    initFilter(url);
  }

  /**
   * Returns the name of the filter.
   * 
   * @return String
   */
  public String getName() {
    return filterName;
  }
  
  /**
   * Returns the description of the filter.
   * 
   * @return String
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the maximum number of input or source textures supported by this
   * filter. It can be called with less than that number
   * 
   * @return int
   */
  public int getNumInputTextures() {
    return numInputTex;
  }

  /**
   * Returns the maximum number of output or destination textures supported by
   * this filter.
   * 
   * @return int
   */
  public int getNumOutputTextures() {
    return numOutputTex;
  }

  /**
   * Applies the shader program on texture srcTex, writing the output to the
   * texture destTex. Sets fade constant to 1.
   * 
   * @param srcTex GLTexture
   * @param destTex GLTexture
   */
  public void apply(GLTexture srcTex, GLTexture destTex) {
    apply(new GLTexture[] { srcTex }, new GLTexture[] { destTex }, null, 1.0f,
        1.0f, 1.0f, 1.0f);
  }

  /**
   * Applies the shader program on texture srcTex, writing the output to the
   * texture destTex and model destModel. Sets fade constant to 1.
   * 
   * @param srcTex GLTexture
   * @param destTex GLTexture
   * @param destTex GLModel
   */
  public void apply(GLTexture srcTex, GLTexture destTex, GLModel destModel) {
    apply(new GLTexture[] { srcTex }, new GLTexture[] { destTex }, destModel,
        1.0f, 1.0f, 1.0f, 1.0f);
  }

  /**
   * Applies the shader program on texture srcTex, writing the output to the
   * texture destTex.
   * 
   * @param srcTex GLTexture
   * @param destTex GLTexture
   * @param destA float
   */
  public void apply(GLTexture srcTex, GLTexture destTex, float destA) {
    apply(new GLTexture[] { srcTex }, new GLTexture[] { destTex }, null, 1.0f,
        1.0f, 1.0f, destA);
  }

  /**
   * Applies the shader program on texture srcTex, writing the output to the
   * texture destTex.
   * 
   * @param srcTex GLTexture
   * @param destTex GLTexture
   * @param destR float
   * @param destG float
   * @param destB float
   * @param destA float
   */
  public void apply(GLTexture srcTex, GLTexture destTex, float destR,
      float destG, float destB, float destA) {
    apply(new GLTexture[] { srcTex }, new GLTexture[] { destTex }, null, destR,
        destG, destB, destA);
  }

  /**
   * Applies the shader program on textures srcTex, writing the output to the
   * texture destTex. Sets fade constant to 1.
   * 
   * @param srcTex GLTexture[]
   * @param destTex GLTexture
   */
  public void apply(GLTexture[] srcTex, GLTexture destTex) {
    apply(srcTex, new GLTexture[] { destTex }, null, 1.0f, 1.0f, 1.0f, 1.0f);
  }

  /**
   * Applies the shader program on textures srcTex, writing the output to the
   * texture destTex.
   * 
   * @param srcTex GLTexture[]
   * @param destTex GLTexture
   * @param destA float
   */
  public void apply(GLTexture[] srcTex, GLTexture destTex, float destA) {
    apply(srcTex, new GLTexture[] { destTex }, null, 1.0f, 1.0f, 1.0f, destA);
  }

  /**
   * Applies the shader program on textures srcTex, writing the output to the
   * texture destTex.
   * 
   * @param srcTex GLTexture[]
   * @param destTex GLTexture
   * @param destR float
   * @param destG float
   * @param destB float
   * @param destA float
   */
  public void apply(GLTexture[] srcTex, GLTexture destTex, float destR,
      float destG, float destB, float destA) {
    apply(srcTex, new GLTexture[] { destTex }, null, destR, destG, destB, destA);
  }

  /**
   * Applies the shader program on textures srcTex, writing the output to the
   * textures destTex. Sets color constant to 1.
   * 
   * @param srcTex GLTexture[]
   * @param destTex GLTexture[]
   */
  public void apply(GLTexture[] srcTex, GLTexture[] destTex) {
    apply(srcTex, destTex, null, 1.0f, 1.0f, 1.0f, 1.0f);
  }

  /**
   * Applies the shader program to generate texture destTex, without any source texture.
   * 
   * @param destTex GLTexture
   */
  public void apply(GLTexture destTex) {
    apply(new GLTexture[] {}, new GLTexture[] { destTex }, null, 1.0f, 1.0f, 1.0f, 1.0f);
  }  
  
  /**
   * Applies the shader program to generate an array of destination textures, without
   * using any source texture.
   * 
   * @param destTex GLTexture[]
   */  
  public void apply(GLTexture[] destTex) {
    apply(new GLTexture[] {}, destTex, null, 1.0f, 1.0f, 1.0f, 1.0f);
  }  
  
  /**
   * Applies the shader program on textures srcTex, writing the output to the
   * textures destTex. Color (r, g, b, a) can be used by the shader to colorize
   * the output texture(s).
   * 
   * @param srcTex GLTexture[]
   * @param destTex GLTexture[]
   * @param destR float
   * @param destG float
   * @param destB float
   * @param destA float
   */
  public void apply(GLTexture[] srcTex, GLTexture[] destTex, GLModel destModel,
                    float destR, float destG, float destB, float destA) {    
    int srcWidth, srcHeight;
    int destWidth, destHeight;
    
    if (0 < srcTex.length) {
      srcWidth = srcTex[0].width;
      srcHeight = srcTex[0].height;      
    } else {
      srcWidth = destTex[0].width;
      srcHeight = destTex[0].height;
    }

    destWidth = destTex[0].width;
    destHeight = destTex[0].height;

    checkDestTex(destTex, srcWidth, srcHeight);

    setGLConf(destWidth, destHeight);

    bindDestFBO();

    bindDestTexToFBO(destTex);

    shader.start();

    setupShader(srcTex, destWidth, destHeight, destR, destG, destB, destA);

    if (0 < srcTex.length) {    
      bindSrcTex(srcTex);
    } else {
      gl.glEnable(destTex[0].getTextureTarget());
    }

    if (grid.isUsingSrcTexRes() && 0 < srcTex.length) {
      srcWidth = srcTex[grid.srcTexInUse()].width;
      srcHeight = srcTex[grid.srcTexInUse()].height;
    }
    grid.render(srcWidth, srcHeight, destWidth, destHeight, srcTex.length);

    if (0 < srcTex.length) {
      unbindSrcTex(srcTex);
    } else {
      gl.glDisable(destTex[0].getTextureTarget());
    }

    shader.stop();

    if (destModel != null)
      copyToModel(0, destTex[0], destModel);

    unbindDestFBO();

    restoreGLConf();
  }

  /**
   * Disables blending.
   */
  public void noBlend() {
    blend = false;
  }

  /**
   * Enables blending and sets the mode.
   * 
   * @param MODE int
   */
  public void setBlendMode(int MODE) {
    blend = true;
    blendMode = MODE;
  }

  /**
   * Returns true of false depending on whether or not
   * the filter has the specified parameter.
   * 
   * @param String paramName
   */  
  public boolean hasParameter(String paramName) {
    return paramsHashMap.containsKey(paramName);
  }
  
  /**
   * Sets the parameter value when the type is int.
   * 
   * @param String paramName
   * @param int value
   */
  public void setParameterValue(String paramName, int value) {
    if (paramsHashMap.containsKey(paramName)) {
      GLTextureFilterParameter param = (GLTextureFilterParameter) paramsHashMap
          .get(paramName);
      param.setValue(value);
    }
  }

  /**
   * Sets the parameter value when the type is float.
   * 
   * @param String paramName
   * @param float value
   */
  public void setParameterValue(String paramName, float value) {
    if (paramsHashMap.containsKey(paramName)) {
      GLTextureFilterParameter param = (GLTextureFilterParameter) paramsHashMap
          .get(paramName);
      param.setValue(value);
    }
  }

  /**
   * Sets the parameter value for any type. When the type is int or float, the
   * first element of the value array is considered.
   * 
   * @param String paramName
   * @param value float[]
   */
  public void setParameterValue(String paramName, float[] value) {
    if (paramsHashMap.containsKey(paramName)) {
      GLTextureFilterParameter param = (GLTextureFilterParameter) paramsHashMap
          .get(paramName);
      param.setValue(value);
    }
  } 
  
  /**
   * Sets the ith value for the parameter (only valid for vec or mat types).
   * 
   * @param String paramName
   * @param int i
   * @param value float
   */
  public void setParameterValue(String paramName, int i, float value) {
    if (paramsHashMap.containsKey(paramName)) {
      GLTextureFilterParameter param = (GLTextureFilterParameter) paramsHashMap
          .get(paramName);
      param.setValue(i, value);
    }
  }

  /**
   * Sets the (ith, jth) value for the parameter (only valid for mat types).
   * 
   * @param String paramName
   * @param int i
   * @param int j
   * @param value float
   */
  public void setParameterValue(String paramName, int i, int j, float value) {
    if (paramsHashMap.containsKey(paramName)) {
      GLTextureFilterParameter param = (GLTextureFilterParameter) paramsHashMap
          .get(paramName);
      param.setValue(i, j, value);
    }
  }

  /**
   * Sets all the value for all the parameters, by means of a parameter list of
   * variable length. values is an array of float[].
   * 
   * @param float[] values
   */
  public void setParameterValues(float[]... values) {
    float[] value;
    for (int i = 0; i < values.length; i++) {
      value = values[i];
      paramsArray[i].setValue(value);
    }
  }

  /**
   * Get number of parameters.
   * 
   * @return int
   */
  public int getParameterCount() {
    return paramsArray.length;
  }

  /**
   * Returns the type of the i-th parameter.
   * 
   * @return int
   */
  public int getParameterType(int i) {
    return paramsArray[i].getType();
  }

  /**
   * Returns the name of the i-th parameter.
   * 
   * @return String
   */
  public String getParameterName(int i) {
    return paramsArray[i].getName();
  }

  /**
   * Returns the label of the i-th parameter.
   * 
   * @return String
   */
  public String getParameterLabel(int i) {
    return paramsArray[i].getLabel();
  }

  /**
   * Returns the i-th parameter.
   * 
   * @return GLTextureFilterParameter
   */
  public GLTextureFilterParameter getParameter(int i) {
    return paramsArray[i];
  }

  /**
   * Sets the parameter value when the type is int.
   * 
   * @param int n
   * @param int value
   */
  public void setParameterValue(int n, int value) {
    paramsArray[n].setValue(value);
  }

  /**
   * Sets the parameter value when the type is float.
   * 
   * @param int n
   * @param float value
   */
  public void setParameterValue(int n, float value) {
    paramsArray[n].setValue(value);
  }

  /**
   * Sets the parameter value for any type. When the type is int or float, the
   * first element of the value array is considered.
   * 
   * @param int n
   * @param value float[]
   */
  public void setParameterValue(int n, float[] value) {
    paramsArray[n].setValue(value);
  }

  /**
   * Sets the ith value for the parameter (only valid for vec or mat types).
   * 
   * @param int n
   * @param int i
   * @param value float
   */
  public void setParameterValue(int n, int i, float value) {
    paramsArray[n].setValue(i, value);
  }

  /**
   * Sets the (ith, jth) value for the parameter (only valid for mat types).
   * 
   * @param int n
   * @param int i
   * @param int j
   * @param value float
   */
  public void setParameterValue(int n, int i, int j, float value) {
    paramsArray[n].setValue(i, j, value);
  }

  /**
   * Returns the parameter with the provided name.
   * 
   * @return GLTextureFilterParameter
   */
  public GLTextureFilterParameter getParameter(String paramName) {
    if (paramsHashMap.containsKey(paramName)) {
      GLTextureFilterParameter param = (GLTextureFilterParameter) paramsHashMap
          .get(paramName);
      return param;
    }
    return null;
  }

  /**
   * Begins the iterative mode. In this mode, the FBO of this filter is
   * made current and then any framebuffer stack operation is disabled, 
   * to avoid subsequent framebuffer changes until endIterativeMode() is
   * called.
   * This mode is useful when using filters in an iterative or chained scheme: 
   * ...
   * filter[i].apply(tex[i], tex[i+1]);
   * filter[i+1].apply(tex[i+1], tex[i+2]);
   * ...
   * If all the textures have the same resolution, then there is no need to
   * change the framebuffer since the same will serve for all the filter operations.
   * Since FBO swap can introduce a performance hit, this mode could result 
   * in some optimization under this kind of usage patterns.
   */  
  public void beginIterativeMode() {
    bindDestFBO();
    GLState.disablePushFramebuffer();
    GLState.disablePopFramebuffer();
    GLState.setFramebufferFixed(true);   
  }

  /**
   * Ends the iterative mode.
   * 
   */  
  public void endIterativeMode() {
    GLState.enablePushFramebuffer();
    GLState.enablePopFramebuffer();
    GLState.setFramebufferFixed(false);
    unbindDestFBO();
  }
  
  /**
   * @invisible
   */
  protected void setGLConf(int w, int h) {    
    int[] buf = new int[1];
    gl.glGetIntegerv(GL.GL_POLYGON_MODE, buf, 0);
    polyMode = buf[0];
    
    glstate.saveBlendConfig();
    if (blend) {
     glstate.enableBlend();
     glstate.setupBlending(blendMode);
    } else {
      glstate.disableBlend();
    }

    gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);

    glstate.saveView();
    glstate.setOrthographicView(w, h);
  }

  /**
   * @invisible
   */
  protected void restoreGLConf() {
    glstate.restoreView();

    glstate.restoreBlendConfig();

    gl.glPolygonMode(GL.GL_FRONT, polyMode);
  }  
  
  /**
   * @invisible Copies the pixel data in destTex to destModel, by using the VBO
   *            of destModel as a PBO.
   */
  protected void copyToModel(int attachPt, GLTexture destTex, GLModel destModel) {
    // destTex is the texture currently attached to GL.GL_COLOR_ATTACHMENT0_EXT
    // attachment point.
    gl.glReadBuffer(GL.GL_COLOR_ATTACHMENT0_EXT + attachPt);

    // The VBO of destModel is used as a PBO to copy pixel data to.
    gl.glBindBuffer(GL.GL_PIXEL_PACK_BUFFER_EXT, destModel.getCoordsVBO());

    // The pixel read above should take place in GPU memory (from the draw
    // buffer

    // set to destTex to the VBO of destModel, viewed as a PBO).
    gl.glReadPixels(0, 0, destTex.width, destTex.height, GL.GL_RGBA,
        GL.GL_FLOAT, 0);
    // gl.glReadBuffer(GL.GL_NONE);
    gl.glBindBuffer(GL.GL_PIXEL_PACK_BUFFER_EXT, 0);
  }

  /**
   * @invisible
   */
  protected void bindSrcTex(GLTexture[] srcTex) {
    gl.glEnable(srcTex[0].getTextureTarget());

    for (int i = 0; i < srcTex.length; i++) {
      srcTex[i].bind(i);
    }
  }

  /**
   * @invisible
   */
  protected void unbindSrcTex(GLTexture[] srcTex) {
    for (int i = 0; i < srcTex.length; i++) {
      srcTex[i].unbind();
    }    

    gl.glDisable(srcTex[0].getTextureTarget());
  }

  /**
   * @invisible
   */
  protected void bindDestFBO() {
    glstate.pushFramebuffer();
    glstate.setFramebuffer(destFBO);
  }

  /**
   * @invisible
   */
  protected void unbindDestFBO() {
    glstate.popFramebuffer();
  }

  /**
   * @invisible
   */
  protected void bindDestTexToFBO(GLTexture[] destTex) {
    if (GLState.isFramebufferFixed()) {
      glstate.setDestTextures(destTex, numOutputTex);
    } else {
      destFBO.setDrawBuffers(destTex, numOutputTex);
    }
  }

  /**
   * @invisible
   */
  protected void initFBO() {
    destFBO = new GLFramebufferObject(gl);
  }

  /**
   * @invisible
   */
  protected void initFilter(String filename) {
    initFilterCommon();

    filename = filename.replace('\\', '/');
    XMLElement xml = new XMLElement(parent, filename);

    loadXML(xml);

    initShader(filename, false);
  }

  /**
   * @invisible
   */
  protected void initFilter(URL url) {
    initFilterCommon();

    try {
      String xmlText = PApplet
          .join(PApplet.loadStrings(url.openStream()), "\n");
      XMLElement xml = new XMLElement(xmlText);
      loadXML(xml);
    } catch (IOException e) {
      System.err.println("Error loading filter: " + e.getMessage());
    }

    initShader(url.toString(), true);
  }

  /**
   * Common initialization code
   * 
   * @invisible
   */
  private void initFilterCommon() {
    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    glstate = new GLState(gl);
    initFBO();

    blend = false;
    blendMode = BLEND;
    
    numInputTex = 1;
    numOutputTex = 1;

    grid = null;    
    
    srcTexNames = null;
    srcTexOffsetNames = null;    
    clockDataName = "clock_data";
    destColorName = "dest_color";
    destTexSizeName = "dest_tex_size";
    
    paramsHashMap = new HashMap<String, GLTextureFilterParameter>();
    paramsArray = new GLTextureFilterParameter[0];
  }

  /**
   * @invisible
   */
  protected void loadXML(XMLElement xml) {
    // Parsing xml configuration.

    int n = xml.getChildCount();
    String name, gridMode;
    XMLElement child;
    vertexFN = geometryFN = fragmentFN = "";
    filterName = xml.getStringAttribute("name");
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("description")) {
        description = child.getContent();
      } else if (name.equals("vertex")) {
        // vertexFN = fixShaderFilename(child.getContent(), rootPath);
        vertexFN = child.getContent();
      } else if (name.equals("geometry")) {
        // geometryFN = fixShaderFilename(child.getContent(), rootPath);
        geometryFN = child.getContent();
        inGeoPrim = child.getStringAttribute("input");
        outGeoPrim = child.getStringAttribute("output");
        maxNumOutVert = child.getIntAttribute("vertcount");
      } else if (name.equals("fragment")) {
        // fragmentFN = fixShaderFilename(child.getContent(), rootPath);
        fragmentFN = child.getContent();
      } else if (name.equals("textures")) {
        numInputTex = child.getIntAttribute("input");
        numOutputTex = child.getIntAttribute("output");
        srcTexNames = new String[numInputTex];
        srcTexOffsetNames = new String[numInputTex];
        for (int k = 0; k < numInputTex; k++) {
          srcTexNames[k] = "src_tex_unit" + k;
          srcTexOffsetNames[k] = "src_tex_offset" + k;
        }
        loadInputTextures(child);
      } else if (name.equals("specialpars")) {
        loadSpecialParameters(child);
      } else if (name.equals("parameters")) {
        loadParameters(child);
      } else if (name.equals("parameter")) {
        addParameter(child);
      } else if (name.equals("grid")) {
        gridMode = child.getStringAttribute("mode");

        if (gridMode == null)
          gridMode = "direct";

        if (gridMode.equals("direct")) {
          grid = new GLTextureGridDirect(gl, child);
        } else if (gridMode.equals("compiled")) {
          grid = new GLTextureGridCompiled(gl, child);
        } else {
          System.err.println("Unrecognized grid mode!");
        }
      } else {
        System.err.println("Unrecognized element in filter config file!");
      }
    }
  }

  /**
   * @invisible
   */
  protected void loadInputTextures(XMLElement xml) {
    int n = xml.getChildCount();

    if (n == 0)
      return;

    if (n != numInputTex) {
      System.err.println("Wrong number of textures in config file!");
      return;
    }

    XMLElement child;
    String name, texName, offsetName, valueStr;
    int texUnit;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("intexture")) {
        texName = child.getStringAttribute("name");
        offsetName = child.getStringAttribute("offset");
        valueStr = child.getContent();
        texUnit = PApplet.parseInt(PApplet.split(valueStr, ' '))[0];

        if ((0 <= texUnit) && (texUnit < n)) {
          srcTexNames[texUnit] = texName;
          if (offsetName != null)
            srcTexOffsetNames[texUnit] = offsetName;
        } else
          System.err.println("Wrong texture unit!");
      }
    }
  }

  /**
   * @invisible
   */
  protected void loadSpecialParameters(XMLElement xml) {
    int n = xml.getChildCount();
    XMLElement child;
    String name, parType, uniformName;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("specialpar")) {
        parType = child.getStringAttribute("type");
        uniformName = child.getStringAttribute("name");

        if (parType.equals("clock"))
          clockDataName = uniformName;
        else if (parType.equals("tint"))
          destColorName = uniformName;
        else if (parType.equals("size"))
          destTexSizeName = uniformName;
      }
    }
  }

  /**
   * @invisible
   */
  protected void loadParameters(XMLElement xml) {
    int n = xml.getChildCount();
    XMLElement child;
    String name;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("parameter"))
        addParameter(child);
    }
  }

  /**
   * @invisible
   */
  protected void addParameter(XMLElement xml) {
    String parName, parTypeStr, parValueStr, parLabelStr;
    int parType;
    GLTextureFilterParameter param;
    float[] parValue;
    parName = xml.getStringAttribute("name");
    parTypeStr = xml.getStringAttribute("type");
    parLabelStr = xml.getStringAttribute("label");
    parValueStr = xml.getContent();
    parType = GLShaderVariable.getType(parTypeStr);

    parValue = PApplet.parseFloat(PApplet.split(parValueStr, ' '));

    if ((-1 < parType) && !paramsHashMap.containsKey(parName)) {
      param = new GLSLTextureFilterParameter(parent, parName, parLabelStr, parType, parValue.length);
      param.setValue(parValue);
      paramsHashMap.put(parName, param);
      paramsArray = (GLTextureFilterParameter[]) PApplet.append(paramsArray, param);
    }
  }

  /**
   * @invisible
   */
  String fixShaderFilename(String filename, String rootPath) {
    String fixedFN = filename.replace('\\', '/');
    if (!rootPath.equals("") && (fixedFN.indexOf(rootPath) != 0))
      fixedFN = rootPath + fixedFN;
    return fixedFN;
  }

  /**
   * Initialize the GLSLShader object.
   * 
   * @param xmlFilename
   *          the XML filename for this filter, used to generate the proper path
   *          for the shader's programs
   * @param useURL
   *          if true, URL objects will be created to load the shader programs
   *          instead of direct filenames
   * @invisible
   */
  protected void initShader(String xmlFilename, boolean useURL) {
    // Getting the root path of the xml file
    int idx;
    String rootPath = "";
    idx = xmlFilename.lastIndexOf('/');
    if (-1 < idx) {
      rootPath = xmlFilename.substring(0, idx + 1);
    }

    if (grid == null) {
      // Creates a 1x1 grid.
      grid = new GLTextureGridDirect(gl);
    }

    // Initializing shader.
    shader = new GLSLShader(parent);

    if (!vertexFN.equals("")) {
      vertexFN = fixShaderFilename(vertexFN, rootPath);
      if (useURL) {
        try {
          shader.loadVertexShader(new URL(vertexFN));
        } catch (MalformedURLException e) {
          System.err.println(e.getMessage());
        }
      } else
        shader.loadVertexShader(vertexFN);
    }

    if (!geometryFN.equals("")) {
      geometryFN = fixShaderFilename(geometryFN, rootPath);
      if (useURL) {
        try {
          shader.loadGeometryShader(new URL(geometryFN));
        } catch (MalformedURLException e) {
          System.err.println(e.getMessage());
        }
      } else
        shader.loadGeometryShader(geometryFN);
        ((GLSLShader)shader).setupGeometryShader(inGeoPrim, outGeoPrim, maxNumOutVert);
    }

    if (!fragmentFN.equals("")) {
      fragmentFN = fixShaderFilename(fragmentFN, rootPath);
      if (useURL) {
        try {
          shader.loadFragmentShader(new URL(fragmentFN));
        } catch (MalformedURLException e) {
          System.err.println(e.getMessage());
        }
      } else
        shader.loadFragmentShader(fragmentFN);
    }

    shader.setup();

    // Initializing special parameters.
    srcTexUnitParams = new GLSLTextureFilterParameter[numInputTex];
    srcTexOffsetParams = new GLSLTextureFilterParameter[numInputTex];
    for (int i = 0; i < numInputTex; i++) {
      srcTexUnitParams[i] = new GLSLTextureFilterParameter(parent, srcTexNames[i], "Texture unit " + i,  SHADER_VAR_INT, 1);
      srcTexUnitParams[i].setShader(shader);
      srcTexUnitParams[i].init();
      srcTexOffsetParams[i] = new GLSLTextureFilterParameter(parent, srcTexOffsetNames[i], "Texture offset " + i,  SHADER_VAR_VEC2, 1);
      srcTexOffsetParams[i].setShader(shader);
      srcTexOffsetParams[i].init();      
    }
    clockDataParam = new GLSLTextureFilterParameter(parent, clockDataName, "Clock data",  SHADER_VAR_VEC2, 1);
    destColorParam = new GLSLTextureFilterParameter(parent, destColorName, "Destination color",  SHADER_VAR_VEC4, 1);
    destTexSizeParam = new GLSLTextureFilterParameter(parent, destTexSizeName, "Destination size",  SHADER_VAR_VEC2, 1);
    clockDataParam.setShader(shader);
    clockDataParam.init();
    destColorParam.setShader(shader);
    destColorParam.init();
    destTexSizeParam.setShader(shader);
    destTexSizeParam.init();
    
    // Putting the parameters into an array.
    for (int i = 0; i < paramsArray.length; i++) {
      paramsArray[i].setShader(shader);
      paramsArray[i].init();
    }
  }

  /**
   * @invisible
   */
  void setupShader(GLTexture[] srcTex, int w, int h, float destR, float destG,
      float destB, float destA) {
    int n = PApplet.min(numInputTex, srcTex.length);

    for (int i = 0; i < n; i++) {
      if (srcTexUnitParams[i].available()) {
        srcTexUnitParams[i].setValue(i);
        srcTexUnitParams[i].copyToShader();
      }
      if (srcTexOffsetParams[i].available()) {
        srcTexOffsetParams[i].setValue(0, 1.0f / srcTex[i].width);
        srcTexOffsetParams[i].setValue(1, 1.0f / srcTex[i].height);
        srcTexOffsetParams[i].copyToShader();
      }
    }

    if (clockDataParam.available()) {
      int fcount = parent.frameCount;
      int msecs = parent.millis();
      clockDataParam.setValue(0, fcount);
      clockDataParam.setValue(1, msecs);
      clockDataParam.copyToShader();
    }

    if (destColorParam.available()) {
      destColorParam.setValue(0, destR);
      destColorParam.setValue(1, destG);      
      destColorParam.setValue(2, destB);
      destColorParam.setValue(3, destA);
      destColorParam.copyToShader();
    }

    if (destTexSizeParam.available()) {
      destTexSizeParam.setValue(0, w);
      destTexSizeParam.setValue(1, h);
      destTexSizeParam.copyToShader();
    }

    for (int i = 0; i < paramsArray.length; i++)
      paramsArray[i].copyToShader();
  }

  /**
   * @invisible
   */
  protected void checkDestTex(GLTexture[] destTex, int w, int h) {
    for (int i = 0; i < destTex.length; i++)
      if (!destTex[i].available()) {
        destTex[i].init(w, h);
      }
  }
}
