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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.media.opengl.GL;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.opengl.PGraphicsOpenGL;
import processing.xml.XMLElement;

public class GLModelEffect implements PConstants {
  protected PApplet parent;
  protected String description;
  protected GL gl;
  protected PGraphicsOpenGL pgl;
  protected GLState glstate;
  protected GLSLShader shader;
  protected String vertexFN;
  protected String geometryFN;
  protected String fragmentFN;
  protected String inGeoPrim;
  protected String outGeoPrim;
  protected int maxNumOutVert;
  protected int numTextures;
  protected HashMap<String, Integer> texHashMap;
  protected int[] texUnitUniform;
  protected String[] texNameArray;
  protected int numParams;
  protected HashMap<String, GLModelEffectParameter> paramsHashMap;  
  protected GLModelEffectParameter[] paramsArray;
  protected int numVertAttribs;
  protected HashMap<String, GLModelEffectVertexAttrib> vertAttribHashMap;
  protected GLModelEffectVertexAttrib[] vertAttribArray;
  
  public GLModelEffect() {
    this.parent = null;
  }

  public GLModelEffect(PApplet parent, String filename) {
    this.parent = parent;
    initEffect(filename);
  }

  /**
   * Creates an instance of GLTextureFilter, loading the filter from a URL.
   */
  public GLModelEffect(PApplet parent, URL url) {
    this.parent = parent;
    initEffect(url);
  }
  
  public void delete() {
    shader.delete();    
  }

  /**
   * Returns the description of the filter.
   * 
   * @return String
   */
  public String getDescription() {
    return description;
  }

  public void apply(GLModel model) {
    model.render(this);
  }

  public void apply(GLModel[] models) {
    for (int i = 0; i < models.length; i++)
      models[i].render(this);
  }

  public void start() {
    shader.start();

    for (int i = 0; i < numParams; i++)
      paramsArray[i].copyToShader();
  }

  public void stop() {
    shader.stop();
  }

  public void setTextures(GLTexture[] textures) {
    for (int i = 0; i < textures.length; i++) {
      if (-1 < texUnitUniform[i]) {
        String name = textures[i].getName(); 
        if (!name.equals("") && texHashMap.containsKey(name)) {
          Integer texUnit = (Integer)texHashMap.get(name);
          gl.glUniform1iARB(texUnitUniform[i], texUnit);           
        } else {
           // The texture doesn't have a name so assuming
          // the texture unit is just i.
          gl.glUniform1iARB(texUnitUniform[i], i);
        }        
      }
    }
  }

  public void setVertexAttribs(int[] attribVBO, String[] attribName, int[] attribSize) {
    for (int i = 0; i < numVertAttribs; i++) {
      String name = attribName[i];
      gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, attribVBO[i]);
      if (!name.equals("") && vertAttribHashMap.containsKey(name)) {
        GLModelEffectVertexAttrib attribute = (GLModelEffectVertexAttrib)vertAttribHashMap.get(name);
        attribute.setAttribArrayPointer(attribSize[i], false, 0);
      } else {
        vertAttribArray[i].setAttribArrayPointer(attribSize[i], false, 0);
      }
    }
  }

  public void enableVertexAttribs() {
    for (int i = 0; i < numVertAttribs; i++) {
      vertAttribArray[i].enableVertexAttribArray();
    }
  }

  public void disableVertexAttribs() {
    for (int i = 0; i < numVertAttribs; i++) {
      vertAttribArray[i].disableVertexAttribArray();
    }
  }

  /**
   * Sets the parameter value when the type is int.
   * 
   * @param String paramName
   * @param int value
   */
  public void setParameterValue(String paramName, int value) {
    if (paramsHashMap.containsKey(paramName)) {
      GLModelEffectParameter param = (GLModelEffectParameter) paramsHashMap
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
      GLModelEffectParameter param = (GLModelEffectParameter) paramsHashMap
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
      GLModelEffectParameter param = (GLModelEffectParameter) paramsHashMap
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
      GLModelEffectParameter param = (GLModelEffectParameter) paramsHashMap
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
      GLModelEffectParameter param = (GLModelEffectParameter) paramsHashMap
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
    return numParams;
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
   * @return GLModelEffectParameter
   */
  public GLModelEffectParameter getParameter(int i) {
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
   * @return GLModelEffectParameter
   */
  public GLModelEffectParameter getParameter(String paramName) {
    if (paramsHashMap.containsKey(paramName)) {
      GLModelEffectParameter param = (GLModelEffectParameter) paramsHashMap
          .get(paramName);
      return param;
    }
    return null;
  }

  /**
   * @invisible
   */
  protected void initEffect(String filename) {
    initEffectCommon();

    filename = filename.replace('\\', '/');
    XMLElement xml = new XMLElement(parent, filename);

    loadXML(xml);

    initShader(filename, false);
  }

  /**
   * @invisible
   */
  protected void initEffect(URL url) {
    initEffectCommon();

    try {
      String xmlText = PApplet
          .join(PApplet.loadStrings(url.openStream()), "\n");
      XMLElement xml = new XMLElement(xmlText);
      loadXML(xml);
    } catch (IOException e) {
      System.err.println("Error loading effect: " + e.getMessage());
    }

    initShader(url.toString(), true);
  }

  /**
   * Common initialization code
   * 
   * @invisible
   */
  private void initEffectCommon() {
    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;

    glstate = new GLState(gl);

    numTextures = 0;
    texHashMap = new HashMap<String, Integer>();
    texUnitUniform = null;
    texNameArray = null;

    numParams = 0;
    paramsHashMap = new HashMap<String, GLModelEffectParameter>();
    paramsArray = null;

    numVertAttribs = 0;
    vertAttribHashMap = new HashMap<String, GLModelEffectVertexAttrib>();
    vertAttribArray = null;
  }

  protected void loadXML(XMLElement xml) {
    // Parsing xml configuration.

    int n = xml.getChildCount();
    String name;
    XMLElement child;
    vertexFN = geometryFN = fragmentFN = "";
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
        loadTextures(child);
      } else if (name.equals("vertexattribs")) {
        loadVertAttribs(child);
      } else if (name.equals("parameters")) {
        loadParams(child);
      } else {
        System.err.println("Unrecognized element in effect config file!");
      }
    }    
  }

  protected void loadTextures(XMLElement xml) {
    int n = xml.getChildCount();
    numTextures = n;
    texNameArray = new String[numTextures];
    texUnitUniform = new int[numTextures];

    XMLElement child;
    String name, texName, valueStr;
    int texUnit;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("texture")) {
        texName = child.getStringAttribute("name");
        valueStr = child.getContent();
        texUnit = PApplet.parseInt(PApplet.split(valueStr, ' '))[0];
        texHashMap.put(texName, new Integer(texUnit));
        texNameArray[i] = texName;
      }
    }
  }

  protected void loadVertAttribs(XMLElement xml) {
    int n = xml.getChildCount();
    numVertAttribs = n;
    vertAttribArray = new GLModelEffectVertexAttrib[numVertAttribs];

    XMLElement child;
    String name;
    String attrName, attrTypeStr, attrValueStr, attrLabelStr;
    int attrType;
    GLModelEffectVertexAttrib attrib;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("vertexattrib")) {
        float[] attrValue;
        attrName = child.getStringAttribute("name");
        attrTypeStr = child.getStringAttribute("type");
        attrLabelStr = child.getStringAttribute("label");
        attrValueStr = child.getContent();
        attrType = GLModelEffectVertexAttrib.getType(attrTypeStr);

        attrValue = PApplet.parseFloat(PApplet.split(attrValueStr, ' '));

        if ((-1 < attrType) && !paramsHashMap.containsKey(attrName)) {
          attrib = new GLModelEffectVertexAttrib(parent, attrName,
              attrLabelStr, attrType);
          attrib.setValue(attrValue);
          vertAttribHashMap.put(attrName, attrib);
          vertAttribArray[i] = attrib;
        }
      }
    }
  }

  protected void loadParams(XMLElement xml) {
    int n = xml.getChildCount();
    numParams = n;
    paramsArray = new GLModelEffectParameter[numParams];

    XMLElement child;
    String name;
    String parName, parTypeStr, parValueStr, parLabelStr;
    int parType;
    GLModelEffectParameter param;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("parameter")) {
        float[] parValue;
        parName = child.getStringAttribute("name");
        parTypeStr = child.getStringAttribute("type");
        parLabelStr = child.getStringAttribute("label");
        parValueStr = child.getContent();
        parType = GLModelEffectParameter.getType(parTypeStr);

        parValue = PApplet.parseFloat(PApplet.split(parValueStr, ' '));

        if ((-1 < parType) && !paramsHashMap.containsKey(parName)) {
          param = new GLModelEffectParameter(parent, parName, parLabelStr, parType, parValue.length);
          param.setValue(parValue);
          paramsHashMap.put(parName, param);
          paramsArray[i] = param;
        }
      }
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
    boolean usingGeom = false;
    String rootPath = "";
    idx = xmlFilename.lastIndexOf('/');
    if (-1 < idx) {
      rootPath = xmlFilename.substring(0, idx + 1);
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
      } else {
        shader.loadGeometryShader(geometryFN);
      }
      usingGeom = true;      
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

    if (usingGeom) {
      shader.setupGeometryShader(inGeoPrim, outGeoPrim, maxNumOutVert);
    } else {
      shader.setup();
    }

    for (int i = 0; i < numTextures; i++) {
      texUnitUniform[i] = shader.getUniformLocation(texNameArray[i]);
    }

    // Generating the parameter IDs.
    for (int i = 0; i < numParams; i++) {
      paramsArray[i].setShader(shader);
      paramsArray[i].init();
    }

    // Generating the vertex attribute IDs.
    for (int i = 0; i < numVertAttribs; i++) {
      vertAttribArray[i].setShader(shader);
      vertAttribArray[i].init();
    }
  }
}