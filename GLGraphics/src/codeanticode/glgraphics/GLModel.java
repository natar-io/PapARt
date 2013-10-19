/**
 * This package provides classes to facilitate the handling of opengl textures, glsl shaders and 
 * off-screen rendering in Processing.
 * @author Andres Colubri. Suggestions and contributions from Aaron Koblin and sigg mus
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
import processing.xml.XMLElement;
import javax.media.opengl.*;

import com.sun.opengl.util.BufferUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 * This class holds a 3D model composed of vertices, normals, colors (per
 * vertex) and texture coordinates (also per vertex). All this data is stored in
 * Vertex Buffer Objects (VBO) for fast access. This is class is still
 * undergoing development, the API will probably change quickly in the following
 * months as features are tested and refined. In particular, with the settings
 * of the VBOs in this first implementation (GL.GL_DYNAMIC_DRAW_ARB) it is
 * assumed that the coordinates will change often during the lifetime of the
 * model. For static models a different VBO setting (GL.GL_STATIC_DRAW_ARB)
 * should be used.
 */
public class GLModel implements PConstants, GLConstants {
  protected PApplet parent;
  protected GL gl;
  protected PGraphicsOpenGL pgl;
  protected GLState glstate;
  protected int size;
  protected int vertexMode;
  protected int vboUsage;
  protected int[] vertCoordsVBO = { 0 };
  protected String description;

  protected float[] tmpVertArray;
  protected float[] tmpColorArray;
  protected float[] tmpNormalsArray;
  protected float[] tmpTexCoordsArray;
  protected float[] tmpAttributesArray;
  protected int[] tmpIndexArray;
  
  protected int[] colorsVBO = null;
  protected int[] normCoordsVBO = null;
  protected int[] texCoordsVBO = null;
  protected int[] indicesVBO = null;
  
  protected boolean autoIndexBoundCalc;
  
  protected float tintR, tintG, tintB, tintA;  
  protected float[] specularColor = { 1.0f, 1.0f, 1.0f, 1.0f };
  protected float[] emissiveColor = { 0.0f, 0.0f, 0.0f, 1.0f };
  protected float[] shininess = { 0 };

  protected float pointSize = 1;
  protected float lineWidth = 1;
  protected float maxPointSize;

  protected boolean blend;
  protected int blendMode;
  
  protected boolean usingPointSprites;  
  
  protected int numAttributes;
  protected int[] attribVBO = null;
  protected String[] attribName;
  protected int[] attribSize;
  protected int curtAttrSize;

  protected int numTextures;

  protected int minIndex;
  protected int maxIndex;
  protected int maxIndicesCount;
  protected int indicesCount;
  
  public GLTexture[] textures;

  public FloatBuffer vertices;
  public FloatBuffer colors;
  public FloatBuffer normals;
  public FloatBuffer texCoords;
  public FloatBuffer attributes;
  public IntBuffer indices;
  
  // Dimensions of the model:
  public float width;
  public float height;
  public float depth;
  // Bounding box: 
  public float xmin, xmax;
  public float ymin, ymax;
  public float zmin, zmax;  
  
  public static final int STATIC = 0;
  public static final int DYNAMIC = 1;
  public static final int STREAM = 2;
  
  float maxSpriteSize;
  // Coefficients for point sprite distance attenuation function.  
  // These default values correspond to the constant sprite size.
  protected float spriteDistAtt[] = { 1.0f, 0.0f, 0.0f };
  
  /**
   * Creates an instance of GLModel with the specified parameters: number of
   * vertices, mode to draw the vertices (as points, sprites, lines, etc) and
   * usage (static if the vertices will never change after the first time are
   * initialized, dynamic if they will change frequently or stream if they will
   * change at every frame).
   * 
   * @param parent  PApplet
   * @param numVert int
   * @param mode int
   * @param usage int
   */
  public GLModel(PApplet parent, int numVert, int mode, int usage) {
    initModelCommon(parent);
    size = numVert;
        int POINT_SPRITES = 30;
        int LINE_STRIP = 40;
        int LINE_LOOP = 50;

    if (mode == POINTS)
      vertexMode = GL.GL_POINTS;
    else if (mode == POINT_SPRITES) {
      vertexMode = GL.GL_POINTS;
      usingPointSprites = true;
      float[] tmp = { 0.0f };
      gl.glGetFloatv(GL.GL_POINT_SIZE_MAX_ARB, tmp, 0);
      maxPointSize = tmp[0];
      maxSpriteSize = maxPointSize;
    } else if (mode == LINES)
      vertexMode = GL.GL_LINES;
    else if (mode == LINE_STRIP)
      vertexMode = GL.GL_LINE_STRIP;
    else if (mode == LINE_LOOP)
      vertexMode = GL.GL_LINE_LOOP;
    else if (mode == TRIANGLES)
      vertexMode = GL.GL_TRIANGLES;
    else if (mode == TRIANGLE_FAN)
      vertexMode = GL.GL_TRIANGLE_FAN;
    else if (mode == TRIANGLE_STRIP)
      vertexMode = GL.GL_TRIANGLE_STRIP;
    else if (mode == QUADS)
      vertexMode = GL.GL_QUADS;
    else if (mode == QUAD_STRIP)
      vertexMode = GL.GL_QUAD_STRIP;
    else if (mode == POLYGON)
      vertexMode = GL.GL_POLYGON;

    if (usage == STATIC)
      vboUsage = GL.GL_STATIC_DRAW_ARB;
    else if (usage == DYNAMIC)
      vboUsage = GL.GL_DYNAMIC_DRAW_ARB;
    else if (usage == STREAM)
      vboUsage = GL.GL_STREAM_COPY;

    vertCoordsVBO[0] = GLState.createGLResource(GL_VERTEX_BUFFER);    
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertCoordsVBO[0]);
    gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, size * 4
        * BufferUtil.SIZEOF_FLOAT, null, vboUsage);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);

    description = "Just another GLModel";
  }

  public GLModel(PApplet parent, int numVert, int mode, int usage, GL context) {
      this.gl = context;
//      this(parent, numVert, mode, usage);

    initModelCommon(parent);
    size = numVert;
        int POINT_SPRITES = 30;
        int LINE_STRIP = 40;
        int LINE_LOOP = 50;

    if (mode == POINTS)
      vertexMode = GL.GL_POINTS;
    else if (mode == POINT_SPRITES) {
      vertexMode = GL.GL_POINTS;
      usingPointSprites = true;
      float[] tmp = { 0.0f };
      gl.glGetFloatv(GL.GL_POINT_SIZE_MAX_ARB, tmp, 0);
      maxPointSize = tmp[0];
      maxSpriteSize = maxPointSize;
    } else if (mode == LINES)
      vertexMode = GL.GL_LINES;
    else if (mode == LINE_STRIP)
      vertexMode = GL.GL_LINE_STRIP;
    else if (mode == LINE_LOOP)
      vertexMode = GL.GL_LINE_LOOP;
    else if (mode == TRIANGLES)
      vertexMode = GL.GL_TRIANGLES;
    else if (mode == TRIANGLE_FAN)
      vertexMode = GL.GL_TRIANGLE_FAN;
    else if (mode == TRIANGLE_STRIP)
      vertexMode = GL.GL_TRIANGLE_STRIP;
    else if (mode == QUADS)
      vertexMode = GL.GL_QUADS;
    else if (mode == QUAD_STRIP)
      vertexMode = GL.GL_QUAD_STRIP;
    else if (mode == POLYGON)
      vertexMode = GL.GL_POLYGON;

    if (usage == STATIC)
      vboUsage = GL.GL_STATIC_DRAW_ARB;
    else if (usage == DYNAMIC)
      vboUsage = GL.GL_DYNAMIC_DRAW_ARB;
    else if (usage == STREAM)
      vboUsage = GL.GL_STREAM_COPY;

    vertCoordsVBO[0] = GLState.createGLResource(GL_VERTEX_BUFFER);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertCoordsVBO[0]);
    gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, size * 4
        * BufferUtil.SIZEOF_FLOAT, null, vboUsage);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);

    description = "Just another GLModel";

      
  }

  public GLModel(PApplet parent, float[] vertArray, int mode, int usage) {
    this(parent, vertArray.length / 4, mode, usage);
    updateVertices(vertArray);
  }

  public GLModel(PApplet parent, ArrayList<PVector> vertArrayList, int mode,
      int usage) {
    this(parent, vertArrayList.size(), mode, usage);
    updateVertices(vertArrayList);
  }

  public GLModel(PApplet parent, String filename) {
    initModelCommon(parent);
    this.parent = parent;

    filename = filename.replace('\\', '/');
    XMLElement xml = new XMLElement(parent, filename);

    loadXML(xml);
  }

  public GLModel(PApplet parent, URL url) {
    initModelCommon(parent);
    this.parent = parent;

    try {
      String xmlText = PApplet
          .join(PApplet.loadStrings(url.openStream()), "\n");
      XMLElement xml = new XMLElement(xmlText);
      loadXML(xml);
    } catch (IOException e) {
      System.err.println("Error loading effect: " + e.getMessage());
    }
  }

  public void delete() {
    releaseModel();
  }

  public int getMode() {
    return vertexMode;
  }
  
  public int getSize() {
    return size;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Returns the OpenGL identifier of the Vertex Buffer Object holding the
   * coordinates of this model.
   * 
   * @return int
   */
  public int getCoordsVBO() {
    return vertCoordsVBO[0];
  }

  /**
   * This method creates the normals, i.e.: it creates the internal OpenGL
   * variables to store normal data.
   */
  public void initNormals() {
    normCoordsVBO = new int[1];
    normCoordsVBO[0] = GLState.createGLResource(GL_VERTEX_BUFFER);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, normCoordsVBO[0]);
    gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, size * 4
        * BufferUtil.SIZEOF_FLOAT, null, vboUsage);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  /**
   * This method creates the colors, i.e.: it creates the internal OpenGL
   * variables to store color data.
   */
  public void initColors() {
    colorsVBO = new int[1];
    colorsVBO[0] = GLState.createGLResource(GL_VERTEX_BUFFER); 
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, colorsVBO[0]);
    gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, size * 4
        * BufferUtil.SIZEOF_FLOAT, null, vboUsage);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  /**
   * Sets the number of attributes for this model. An attribute is a vector
   * value associated to each vertex of the model, and can be used for any
   * custom calculation (lighting, deformations, etc). This custom calculations
   * can be performed using a GLModelEffect.
   * 
   * @param n int
   */
  public void initAttributes(int n) {
    numAttributes = n;
    attribVBO = new int[n];
    attribName = new String[n];
    attribSize = new int[n];
    for (int i = 0; i < n; i++) {
      attribVBO[i] = GLState.createGLResource(GL_VERTEX_BUFFER);
    }
  }

  /**
   * Sets the name and size (number of components per vertex) of attribute i.
   * 
   * @param i int
   * @param aname String
   * @param asize int
   */
  public void setAttribute(int i, String aname, int asize) {
    attribName[i] = aname;
    attribSize[i] = asize;

    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, attribVBO[i]);
    gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, size * asize
        * BufferUtil.SIZEOF_FLOAT, null, vboUsage);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  /**
   * This method creates n textures, i.e.: it creates the internal OpenGL
   * variables to store n textures.
   * 
   * @param n int
   */
  public void initTextures(int n) {
    numTextures = n;

    texCoordsVBO = new int[numTextures];
    textures = new GLTexture[numTextures];
    for (n = 0; n < numTextures; n++) {
      texCoordsVBO[n] = GLState.createGLResource(GL_VERTEX_BUFFER);
      gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsVBO[n]); // Bind the
                                                                   // buffer.
      gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, size * 2
          * BufferUtil.SIZEOF_FLOAT, null, vboUsage);
    }
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  /**
   * This method initializes the index array in this GLModel, capable
   * to hold up to n vertex indices. The mode of the indices is STATIC,
   * meaning that they are optimized under the assumption that won't be
   * modified.
   * 
   * @param n int
   */
  public void initIndices(int n) {
    initIndices(n, STATIC);
  }

  /**
   * This method initializes the index array in this GLModel, capable
   * to hold up to n vertex indices. The usage mode of the array can
   * also be indicated, different from the usage of the model geometry
   * itself.
   * 
   * @param n maximum size of index array
   * @param usage could be STATIC, DYNAMIC, or STREAM 
   */  
  public void initIndices(int n, int usage) {
    int glUsage = GL.GL_STATIC_DRAW_ARB;
    if (usage == STATIC)
      vboUsage = GL.GL_STATIC_DRAW_ARB;
    else if (usage == DYNAMIC)
      vboUsage = GL.GL_DYNAMIC_DRAW_ARB;
    else if (usage == STREAM)
      vboUsage = GL.GL_STREAM_COPY;
    
    maxIndicesCount = n;
    indicesVBO = new int[1];
    indicesVBO[0] = GLState.createGLResource(GL_VERTEX_BUFFER);
    gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER, indicesVBO[0]);
    gl.glBufferDataARB(GL.GL_ELEMENT_ARRAY_BUFFER, maxIndicesCount * BufferUtil.SIZEOF_INT, null, glUsage);
    gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
    
    minIndex = 0;
    maxIndex = size;
    autoIndexBoundCalc = true;
  }
  
  /**
   * This method enables/disables the automatic calculation of
   * index bounds (maximum and minimum).
   * 
   * @param val true or false 
   */   
  public void autoIndexBounds(boolean val) {
    autoIndexBoundCalc = val;
  }
  
  /**
   * Prepares the indices for updating. 
   */     
  public void beginUpdateIndices() {
    gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER, indicesVBO[0]);
    ByteBuffer temp = gl.glMapBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
    indices = temp.asIntBuffer();   
  }

  /**
   * Cleans-up index updating. 
   */       
  public void endUpdateIndices() {
    if (tmpIndexArray != null) {
      indices.put(tmpIndexArray);
      tmpIndexArray = null;      
    }
    indices.position(0);
    gl.glUnmapBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER);
    gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
  }
    
  /**
   * Updates the indices with all the values provided in the array.
   * 
   * @param indexArray array with new indices 
   */
  public void updateIndices(int[] indexArray) {
    updateIndices(indexArray, indexArray.length);
  }

  /**
   * Updates the indices with the values provided in the array,
   * up to the element len-1.
   * 
   * @param indexArray array with new indices
   * @param len number of values to read from intexArray, starting at zero. 
   */  
  public void updateIndices(int[] indexArray, int len) {
    if (maxIndicesCount < len) {
      System.err.println("Wrong number of indices!");
    }    
    
    indicesCount = len;
    beginUpdateIndices();
    indices.put(indexArray, 0, len);
    endUpdateIndices();

    if (autoIndexBoundCalc) {
      // Looking for the minimum and maximum indices in the array provided:
      
      // This to make sure we find the new minimum and maximum indices.
      minIndex = size;
      maxIndex = -size;
      
      for (int i = 0; i < len; i++) {
        if (indexArray[i] < minIndex) minIndex = indexArray[i];
        if (maxIndex < indexArray[i]) maxIndex = indexArray[i];
      }      
    }
  }

  /**
   * Sets the minimum index value to use when rendering the model.
   * 
   * @param min minimum index 
   */  
  public void setMinIndex(int min) {
    minIndex = min;
  }  

  /**
   * Sets the maximum index value to use when rendering the model.
   * 
   * @param max maximum index 
   */    
  public void setMaxIndex(int max) {
    maxIndex = max;
  }  
  
  /**
   * Sets the i-th texture.
   * 
   * @param i int
   */
  public void setTexture(int i, GLTexture tex) {
    textures[i] = tex;
  }

  /**
   * Returns the number of textures.
   * 
   * @return int
   */
  public int getNumTextures() {
    return numTextures;
  }

  /**
   * Returns the i-th texture.
   * 
   * @return GLTexture
   */
  public GLTexture getTexture(int i) {
    return textures[i];
  }

  /**
   * Enables vertex updating, to be done with the
   * updateVertex()/displaceVertex() methods.
   */
  public void beginUpdateVertices() {
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertCoordsVBO[0]);
    ByteBuffer temp = gl.glMapBufferARB(GL.GL_ARRAY_BUFFER_ARB, GL.GL_WRITE_ONLY);
    vertices = temp.asFloatBuffer();
  }

  /**
   * Disables vertex updating.
   */
  public void endUpdateVertices() {
    if (tmpVertArray != null) {
      vertices.put(tmpVertArray);
      tmpVertArray = null;      
    }
    vertices.position(0);
    gl.glUnmapBufferARB(GL.GL_ARRAY_BUFFER_ARB);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  /**
   * Returns array list with vertices stored in binary file.
   * 
   * @param String filename
   * @return ArrayList<PVector>
   */
  public ArrayList<PVector> getVertices(String filename) {
    ArrayList<PVector> res = new ArrayList<PVector>();
    loadPVectorArrayListFromBinary(filename, res, 3);
    return res;
  }

  /**
   * Saves vertices in the given array list to binary file.
   * 
   * @param String  filename
   * @param ArrayList<PVector> verts
   */
  public void saveVertices(String filename, ArrayList<PVector> verts) {
    savePVectorArrayListToBinary(filename, verts, 3);
  }

  /**
   * Loads vertices from binary file into model.
   * 
   * @param String filename
   */
  public void loadVertices(String filename) {
    float[] tmp = new float[4 * size];
    for (int i = 0; i < size; i++)
      tmp[4 * i + 3] = 1.0f;

    beginUpdateVertices();
    loadFloatArrayFromBinary(filename, tmp, size, 4, 3);
    vertices.put(tmp);
    endUpdateVertices();
  }

  /**
   * Saves vertices in the model to binary file.
   */
  public void saveVertices(String filename) {
    float[] tmp = new float[4 * size];

    beginUpdateVertices();
    vertices.get(tmp);
    saveFloatArrayToBinary(filename, tmp, size, 4, 3);
    endUpdateVertices();
  }

  /**
   * Updates the coordinates of vertex idx.
   * 
   * @param idx int
   * @param x float
   * @param y float
   */
  public void updateVertex(int idx, float x, float y) {
    updateVertex(idx, x, y, 0, 1);
  }

  /**
   * Updates the coordinates of vertex idx.
   * 
   * @param idx int
   * @param x float
   * @param y float
   * @param z float
   */
  public void updateVertex(int idx, float x, float y, float z) {
    updateVertex(idx, x, y, z, 1);
  }

  /**
   * Updates the coordinates of vertex idx.
   * 
   * @param idx int
   * @param x float
   * @param y float
   * @param z  float
   * @param w float
   */
  public void updateVertex(int idx, float x, float y, float z, float w) {
    if (tmpVertArray == null) {
      tmpVertArray = new float[4 * size];
      vertices.get(tmpVertArray);
      vertices.rewind();
    }

    tmpVertArray[4 * idx + 0] = x;
    tmpVertArray[4 * idx + 1] = y;
    tmpVertArray[4 * idx + 2] = z;
    tmpVertArray[4 * idx + 3] = w;
  }

  /**
   * Adds a displacement (dx, dy) to the vertex idx.
   * 
   * @param idx int
   * @param dx float
   * @param dy float
   */
  public void displaceVertex(int idx, float dx, float dy) {
    displaceVertex(idx, dx, dy, 0, 0);
  }

  /**
   * Adds a displacement (dx, dy, dz) to the vertex idx.
   * 
   * @param idx int
   * @param dx float
   * @param dy float
   * @param dz float
   */
  public void displaceVertex(int idx, float dx, float dy, float dz) {
    displaceVertex(idx, dx, dy, dz, 0);
  }

  /**
   * Adds a displacement (dx, dy, dz, dw) to the vertex idx.
   * 
   * @param idx int
   * @param dx float
   * @param dy float
   * @param dz float
   * @param dw float
   */
  public void displaceVertex(int idx, float dx, float dy, float dz, float dw) {
    if (tmpVertArray == null) {
      tmpVertArray = new float[4 * size];
      vertices.get(tmpVertArray);
      vertices.rewind();
    }

    tmpVertArray[4 * idx + 0] += dx;
    tmpVertArray[4 * idx + 1] += dy;
    tmpVertArray[4 * idx + 2] += dz;
    tmpVertArray[4 * idx + 3] += dw;
  }

  /**
   * Updates all the vertices using the coordinates provided in the array
   * vertArray.
   * 
   * @param vertArray float[]
   */
  public void updateVertices(float[] vertArray) {
    beginUpdateVertices();
    vertices.put(vertArray);
    endUpdateVertices();
  }

  /**
   * Updates all the vertices using the coordinates provided in the array
   * vertArray.
   * 
   * @param vertArrayList ArrayList<PVector>
   */
  public void updateVertices(ArrayList<PVector> vertArrayList) {
    if (vertArrayList.size() != size) {
      System.err.println("Wrong number of vertices in the array list.");
      return;
    }

    float p[] = new float[4 * size];
    for (int i = 0; i < vertArrayList.size(); i++) {
      PVector point = (PVector) vertArrayList.get(i);
      p[4 * i + 0] = point.x;
      p[4 * i + 1] = point.y;
      p[4 * i + 2] = point.z;
      p[4 * i + 3] = 1.0f;
    }
    updateVertices(p);
  }

  /**
   * Centers the model to (0, 0, 0).
   */
  public void centerVertices() {
    centerVertices(0, 0, 0);
  }

  /**
   * Centers the model to (xc, yc, 0).
   * 
   * @param xc float
   * @param yc float
   */
  public void centerVertices(float xc, float yc) {
    centerVertices(xc, yc, 0);
  }

  /**
   * Centers the model to (xc, yc, zc).
   * 
   * @param xc float
   * @param yc float
   * @param zc float
   */
  public void centerVertices(float xc, float yc, float zc) {
    beginUpdateVertices();
    tmpVertArray = new float[4 * size];
    vertices.get(tmpVertArray);
    vertices.rewind();

    float xave, yave, zave;
    xave = yave = zave = 0;
    for (int i = 0; i < size; i++) {
      xave += tmpVertArray[4 * i + 0];
      yave += tmpVertArray[4 * i + 1];
      zave += tmpVertArray[4 * i + 2];
    }
    xave /= size;
    yave /= size;
    zave /= size;

    for (int i = 0; i < size; i++) {
      tmpVertArray[4 * i + 0] += xc - xave;
      tmpVertArray[4 * i + 1] += yc - yave;
      tmpVertArray[4 * i + 2] += zc - zave;
    }

    endUpdateVertices();
  }

  public void updateGL(PGraphicsOpenGL screen){
      this.gl = screen.gl;
  }

  public void updateBounds() {
    beginUpdateVertices();
    tmpVertArray = new float[4 * size];
    vertices.get(tmpVertArray);
    vertices.rewind();
    
    resetBounds();
    for (int i = 0; i < size; i++) {
      updateBounds(tmpVertArray[4 * i + 0], tmpVertArray[4 * i + 1], tmpVertArray[4 * i + 2]);      
    }
    
    endUpdateVertices();
  }
  
  public void updateBounds(int indices[], int len) {
    beginUpdateVertices();
    tmpVertArray = new float[4 * size];
    vertices.get(tmpVertArray);
    vertices.rewind();
    
    resetBounds();
    for (int n = 0; n < len; n++) {
      int i = indices[n];
      updateBounds(tmpVertArray[4 * i + 0], tmpVertArray[4 * i + 1], tmpVertArray[4 * i + 2]);      
    }
    
    endUpdateVertices();
  }  
    
  protected void resetBounds() {
    width = height = depth = 0;
    xmin = ymin = zmin = 10000;
    xmax = ymax = zmax = -10000;    
  }
  
  
  protected void updateBounds(float x, float y, float z) {
    xmin = PApplet.min(xmin, x);
    xmax = PApplet.max(xmax, x);
    
    ymin = PApplet.min(ymin, y);
    ymax = PApplet.max(ymax, y);

    zmin = PApplet.min(zmin, z);
    zmax = PApplet.max(zmax, z);

    width = xmax - xmin;
    height = ymax - ymin;
    depth = zmax - zmin;
  }  
    
  public void beginUpdateColors() {
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, colorsVBO[0]);
    ByteBuffer temp = gl.glMapBufferARB(GL.GL_ARRAY_BUFFER_ARB, GL.GL_WRITE_ONLY);
    colors = temp.asFloatBuffer();    
  }

  public void endUpdateColors() {
    if (tmpColorArray != null) {
      colors.put(tmpColorArray);
      tmpColorArray = null;
    }
    colors.position(0);
    gl.glUnmapBufferARB(GL.GL_ARRAY_BUFFER_ARB);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  /**
   * Returns array list with colors stored in binary file.
   * 
   * @param String filename
   * @return ArrayList<PVector>
   */
  public ArrayList<float[]> getColors(String filename) {
    ArrayList<float[]> res = new ArrayList<float[]>();
    loadFloatArrayListFromBinary(filename, res, 4);
    return res;
  }

  /**
   * Saves colors in the given array list to binary file.
   * 
   * @param String filename
   * @param ArrayList<PVector> icolors
   */
  public void saveColors(String filename, ArrayList<float[]> icolors) {
    saveFloatArrayToBinary(filename, icolors, 4);
  }

  /**
   * Loads colors from binary file into model.
   * 
   * @param String filename
   */
  public void loadColors(String filename) {
    float[] tmp = new float[4 * size];

    beginUpdateColors();
    loadFloatArrayFromBinary(filename, tmp, size, 4, 4);
    colors.put(tmp);
    endUpdateColors();
  }

  /**
   * Saves colors in the model to binary file.
   */
  public void saveColors(String filename) {
    float[] tmp = new float[4 * size];

    beginUpdateColors();
    colors.get(tmp);
    saveFloatArrayToBinary(filename, tmp, size, 4, 4);
    endUpdateColors();
  }

  /**
   * Paints the vertex i with the specified gray tone.
   * 
   * @param gray float
   */
  public void updateColor(int i, float gray) {
    int c = parent.color(gray);
    putColorInTmpArray(i, c);
  }

  /**
   * Paints the vertex i with the specified gray tone and alpha value.
   * 
   * @param gray int
   * @param alpha int
   */
  public void updateColor(int i, int gray, int alpha) {
    int c = parent.color(gray, alpha);
    putColorInTmpArray(i, c);
  }

  /**
   * Paints the vertex i with the specified rgb color and alpha value.
   * 
   * @param rgb int
   * @param alpha float
   */
  public void updateColor(int i, int rgb, float alpha) {
    int c = parent.color(rgb, alpha);
    putColorInTmpArray(i, c);
  }

  /**
   * Paints the vertex i with the specified gray tone and alpha value.
   * 
   * @param gray float
   * @param alpha float
   */
  public void updateColor(int i, float gray, float alpha) {
    int c = parent.color(gray, alpha);
    putColorInTmpArray(i, c);
  }

  /**
   * Paints the vertex i with the specified color components.
   * 
   * @param x int
   * @param y int
   * @param z int
   */
  public void updateColor(int i, int x, int y, int z) {
    int c = parent.color(x, y, z);
    putColorInTmpArray(i, c);
  }

  /**
   * Paints the vertex i with the specified color components.
   * 
   * @param x float
   * @param y float
   * @param z float
   */
  public void updateColor(int i, float x, float y, float z) {
    int c = parent.color(x, y, z);
    putColorInTmpArray(i, c);
  }

  /**
   * Paints the vertex i with the specified color components and alpha
   * component.
   * 
   * @param x int
   * @param y int
   * @param z int
   * @param a int
   */
  public void updateColor(int i, int x, int y, int z, int a) {
    int c = parent.color(x, y, z, a);
    putColorInTmpArray(i, c);
  }

  /**
   * Paints the vertex i with the specified color components and alpha
   * component.
   * 
   * @param x float
   * @param y float
   * @param z float
   * @param a float
   */
  public void updateColor(int i, float x, float y, float z, float a) {
    int c = parent.color(x, y, z, a);
    putColorInTmpArray(i, c);
  }

  /**
   * Paints all vertices with the specified gray tone.
   * 
   * @param gray float
   */
  public void setColors(float gray) {
    int c = parent.color(gray);
    updateAllColors(c);
  }

  /**
   * Paints all vertices with the specified gray tone and alpha value.
   * 
   * @param gray  int
   * @param alpha int
   */
  public void setColors(int gray, int alpha) {
    int c = parent.color(gray, alpha);
    updateAllColors(c);
  }

  /**
   * Paints all vertices with the specified rgb color and alpha value.
   * 
   * @param rgb int
   * @param alpha float
   */
  public void setColors(int rgb, float alpha) {
    int c = parent.color(rgb, alpha);
    updateAllColors(c);
  }

  /**
   * Paints all vertices with the specified gray tone and alpha value.
   * 
   * @param gray float
   * @param alpha float
   */
  public void setColors(float gray, float alpha) {
    int c = parent.color(gray, alpha);
    updateAllColors(c);
  }

  /**
   * Paints all vertices with the specified color components.
   * 
   * @param x int
   * @param y int
   * @param z int
   */
  public void setColors(int x, int y, int z) {
    int c = parent.color(x, y, z);
    updateAllColors(c);
  }

  /**
   * Paints all vertices with the specified color components.
   * 
   * @param x float
   * @param y float
   * @param z float
   */
  public void setColors(float x, float y, float z) {
    int c = parent.color(x, y, z);
    updateAllColors(c);
  }

  /**
   * Paints all vertices with the specified color components and alpha
   * component.
   * 
   * @param x int
   * @param y int
   * @param z int
   * @param a int
   */
  public void setColors(int x, int y, int z, int a) {
    int c = parent.color(x, y, z, a);
    updateAllColors(c);
  }

  /**
   * Paints all vertices with the specified color components and alpha
   * component.
   * 
   * @param x float
   * @param y float
   * @param z float
   * @param a float
   */
  public void setColors(float x, float y, float z, float a) {
    int c = parent.color(x, y, z, a);
    updateAllColors(c);
  }

  public void updateColors(float[] colArray) {
    beginUpdateColors();
    colors.put(colArray);
    endUpdateColors();
  }

  public void updateColors(ArrayList<float[]> colArrayList) {
    if (colArrayList.size() != size) {
      System.err.println("Wrong number of colors in the array list.");
      return;
    }

    float p[] = new float[4 * size];
    for (int i = 0; i < colArrayList.size(); i++) {
      float[] c = (float[]) colArrayList.get(i);

      if (c.length == 4) {
        p[4 * i + 0] = c[0];
        p[4 * i + 1] = c[1];
        p[4 * i + 2] = c[2];
        p[4 * i + 3] = c[3];
      }
    }
    updateColors(p);
  }

  public void beginUpdateTexCoords(int n) {
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsVBO[n]);
    ByteBuffer temp = gl.glMapBufferARB(GL.GL_ARRAY_BUFFER_ARB, GL.GL_WRITE_ONLY);
    texCoords = temp.asFloatBuffer();    
  }

  public void updateTexCoord(int idx, float s, float t) {
    if (tmpTexCoordsArray == null) {
      tmpTexCoordsArray = new float[2 * size];
      texCoords.get(tmpTexCoordsArray);
      texCoords.rewind();
    }

    tmpTexCoordsArray[2 * idx + 0] = s;
    tmpTexCoordsArray[2 * idx + 1] = t;
  }

  /**
   * Returns array list with texture coordinates stored in binary file.
   * 
   * @param String filename
   * @return ArrayList<PVector>
   */
  public ArrayList<PVector> getTexCoords(String filename) {
    ArrayList<PVector> res = new ArrayList<PVector>();
    loadPVectorArrayListFromBinary(filename, res, 2);
    return res;
  }

  /**
   * Saves texture coordinates in the given array list to binary file.
   * 
   * @param String filename
   * @param ArrayList <PVector> icolors
   */
  public void saveTexCoords(String filename, ArrayList<PVector> itexCoords) {
    savePVectorArrayListToBinary(filename, itexCoords, 2);
  }

  /**
   * Loads texture coordinates of nth texture from binary file into model.
   * 
   * @param n
   * @param String filename
   */
  public void loadTexCoords(int n, String filename) {
    float[] tmp = new float[2 * size];

    beginUpdateTexCoords(n);
    loadFloatArrayFromBinary(filename, tmp, size, 2, 2);
    texCoords.put(tmp);
    endUpdateTexCoords();
  }

  /**
   * Saves colors in the model to binary file.
   */
  public void saveTexCoords(int n, String filename) {
    float[] tmp = new float[2 * size];

    beginUpdateTexCoords(n);
    texCoords.get(tmp);
    saveFloatArrayToBinary(filename, tmp, size, 2, 2);
    endUpdateTexCoords();
  }

  public void displaceTexCoord(int idx, float ds, float dt) {
    if (tmpTexCoordsArray == null) {
      tmpTexCoordsArray = new float[2 * size];
      texCoords.get(tmpTexCoordsArray);
      texCoords.rewind();
    }

    tmpTexCoordsArray[2 * idx + 0] += ds;
    tmpTexCoordsArray[2 * idx + 1] += dt;
  }

  public void endUpdateTexCoords() {
    if (tmpTexCoordsArray != null) {
      texCoords.put(tmpTexCoordsArray);
      tmpTexCoordsArray = null;
    }
    texCoords.position(0);
    gl.glUnmapBufferARB(GL.GL_ARRAY_BUFFER_ARB);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  public void updateTexCoords(int n, float[] texCoordsArray) {
    beginUpdateTexCoords(n);
    texCoords.put(texCoordsArray);
    endUpdateTexCoords();
  }

  public void updateTexCoords(int n, ArrayList<PVector> texCoordsArrayList) {
    if (texCoordsArrayList.size() != size) {
      System.err
          .println("Wrong number of texture coordinates in the array list.");
      return;
    }

    float p[] = new float[2 * size];
    for (int i = 0; i < texCoordsArrayList.size(); i++) {
      PVector point = (PVector) texCoordsArrayList.get(i);
      p[2 * i + 0] = point.x;
      p[2 * i + 1] = point.y;
    }
    updateTexCoords(n, p);
  }

  public void beginUpdateNormals() {
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, normCoordsVBO[0]);
    ByteBuffer temp = gl.glMapBufferARB(GL.GL_ARRAY_BUFFER_ARB, GL.GL_WRITE_ONLY);
    normals = temp.asFloatBuffer();
  }

  public void endUpdateNormals() {
    if (tmpNormalsArray != null) {
      normals.put(tmpNormalsArray);
      tmpNormalsArray = null;
    }
    normals.position(0);

    gl.glUnmapBufferARB(GL.GL_ARRAY_BUFFER_ARB);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  /**
   * Returns array list with normals stored in binary file.
   * 
   * @param String filename
   * @return ArrayList<PVector>
   */
  public ArrayList<PVector> getNormals(String filename) {
    ArrayList<PVector> res = new ArrayList<PVector>();
    loadPVectorArrayListFromBinary(filename, res, 3);
    return res;
  }

  /**
   * Saves normals in the given array list to binary file.
   * 
   * @param String filename
   * @param ArrayList<PVector> verts
   */
  public void saveNormals(String filename, ArrayList<PVector> norms) {
    savePVectorArrayListToBinary(filename, norms, 3);
  }

  /**
   * Loads normals from binary file into model.
   * 
   * @param String filename
   */
  public void loadNormals(String filename) {
    float[] tmp = new float[4 * size];

    beginUpdateNormals();
    loadFloatArrayFromBinary(filename, tmp, size, 4, 3);
    normals.put(tmp);
    endUpdateNormals();
  }

  /**
   * Saves normals in the model to binary file.
   */
  public void saveNormals(String filename) {
    float[] tmp = new float[4 * size];

    beginUpdateNormals();
    normals.get(tmp);
    saveFloatArrayToBinary(filename, tmp, size, 4, 3);
    endUpdateNormals();
  }

  public void updateNormal(int idx, float x, float y) {
    updateNormal(idx, x, y, 0, 0);
  }

  public void updateNormal(int idx, float x, float y, float z) {
    updateNormal(idx, x, y, z, 0);
  }

  public void updateNormal(int idx, float x, float y, float z, float w) {
    if (tmpNormalsArray == null) {
      tmpNormalsArray = new float[4 * size];
      normals.get(tmpNormalsArray);
      normals.rewind();
    }

    tmpNormalsArray[4 * idx + 0] = x;
    tmpNormalsArray[4 * idx + 1] = y;
    tmpNormalsArray[4 * idx + 2] = z;
    tmpNormalsArray[4 * idx + 3] = w;
  }

  public void displaceNormal(int idx, float dx, float dy) {
    displaceNormal(idx, dx, dy, 0, 0);
  }

  public void displaceNormal(int idx, float dx, float dy, float dz) {
    displaceNormal(idx, dx, dy, dz, 0);
  }

  public void displaceNormal(int idx, float dx, float dy, float dz, float dw) {
    if (tmpNormalsArray == null) {
      tmpNormalsArray = new float[4 * size];
      normals.get(tmpNormalsArray);
      normals.rewind();
    }

    tmpNormalsArray[4 * idx + 0] += dx;
    tmpNormalsArray[4 * idx + 1] += dy;
    tmpNormalsArray[4 * idx + 2] += dz;
    tmpNormalsArray[4 * idx + 3] += dw;
  }

  public void updateNormals(float[] normArray) {
    beginUpdateNormals();
    normals.put(normArray);
    endUpdateNormals();
  }

  public void updateNormals(ArrayList<PVector> normArrayList) {
    if (normArrayList.size() != size) {
      System.err.println("Wrong number of normals in the array list.");
      return;
    }

    float p[] = new float[4 * size];
    for (int i = 0; i < normArrayList.size(); i++) {
      PVector point = (PVector) normArrayList.get(i);
      p[4 * i + 0] = point.x;
      p[4 * i + 1] = point.y;
      p[4 * i + 2] = point.z;
      p[4 * i + 3] = 0.0f;
    }
    updateNormals(p);
  }

  public void beginUpdateAttributes(int n) {
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, attribVBO[n]);
    ByteBuffer temp = gl.glMapBufferARB(GL.GL_ARRAY_BUFFER_ARB, GL.GL_WRITE_ONLY);
    attributes = temp.asFloatBuffer();
    curtAttrSize = attribSize[n];
  }

  public void endUpdateAttributes() {
    if (tmpAttributesArray != null) {
      attributes.put(tmpAttributesArray);
      tmpAttributesArray = null;
    }
    attributes.position(0);
    gl.glUnmapBufferARB(GL.GL_ARRAY_BUFFER_ARB);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  /**
   * Returns array list with attributes stored in binary file. Each attribute
   * has size len.
   * 
   * @param String filename
   * @return int len
   */
  public ArrayList<float[]> getAttributes(String filename, int len) {
    ArrayList<float[]> res = new ArrayList<float[]>();
    loadFloatArrayListFromBinary(filename, res, len);
    return res;
  }

  /**
   * Saves attributes in the given array list to binary file. Each attribute has
   * size len.
   * 
   * @param String filename
   * @param ArrayList<PVector> iattrib
   */
  public void saveAttributes(String filename, ArrayList<float[]> iattrib,
      int len) {
    saveFloatArrayToBinary(filename, iattrib, len);
  }

  /**
   * Loads attribute nth from binary file into model.
   * 
   * @param String filename
   */
  public void loadAttributes(int n, String filename) {
    int len = attribSize[n];
    float[] tmp = new float[len * size];

    beginUpdateAttributes(n);
    loadFloatArrayFromBinary(filename, tmp, size, len, len);
    attributes.put(tmp);
    endUpdateAttributes();
  }

  /**
   * Saves attribute n-th in the model to binary file.
   */
  public void saveAttributes(int n, String filename) {
    int len = attribSize[n];
    float[] tmp = new float[len * size];

    beginUpdateAttributes(n);
    attributes.get(tmp);
    saveFloatArrayToBinary(filename, tmp, size, len, len);
    endUpdateAttributes();
  }

  public void updateAttribute(int idx, float x) {
    updateAttribute(idx, new float[] { x });
  }

  public void updateAttribute(int idx, float x, float y) {
    updateAttribute(idx, new float[] { x, y });
  }

  public void updateAttribute(int idx, float x, float y, float z) {
    updateAttribute(idx, new float[] { x, y, z });
  }

  public void updateAttribute(int idx, float x, float y, float z, float w) {
    updateAttribute(idx, new float[] { x, y, z, w });
  }

  public void updateAttribute(int idx, float[] values) {
    if (values.length == curtAttrSize) {
      if (tmpAttributesArray == null) {
        tmpAttributesArray = new float[curtAttrSize * size];
        attributes.get(tmpAttributesArray);
        attributes.rewind();
      }

      for (int i = 0; i < curtAttrSize; i++)
        tmpAttributesArray[curtAttrSize * idx + i] = values[i];
    }
  }

  public void displaceAttribute(int idx, float dx) {
    displaceAttribute(idx, new float[] { dx });
  }

  public void displaceAttribute(int idx, float dx, float dy) {
    displaceAttribute(idx, new float[] { dx, dy });
  }

  public void displaceAttribute(int idx, float x, float y, float z) {
    displaceAttribute(idx, new float[] { x, y, z });
  }

  public void displaceAttribute(int idx, float x, float y, float z, float w) {
    displaceAttribute(idx, new float[] { x, y, z, w });
  }

  public void displaceAttribute(int idx, float[] dvalues) {
    int l = attribSize[idx];
    if (dvalues.length == l) {
      if (tmpAttributesArray == null) {
        tmpAttributesArray = new float[l * size];
        attributes.get(tmpAttributesArray);
        attributes.rewind();
      }

      for (int i = 0; i < l; i++)
        tmpAttributesArray[l * idx + i] += dvalues[i];
    }
  }

  public void updateAttributes(int n, float[] attributesArray) {
    beginUpdateAttributes(n);
    attributes.put(attributesArray);
    endUpdateAttributes();
  }

  public void updateAttributes(int n, ArrayList<float[]> vertAttribsArrayList) {
    if (vertAttribsArrayList.size() != size) {
      System.err
          .println("Wrong number of vertex attributes in the array list.");
      return;
    }

    int l = attribSize[n];
    float p[] = new float[l * size];
    for (int i = 0; i < vertAttribsArrayList.size(); i++) {
      float[] attrib = (float[]) vertAttribsArrayList.get(i);

      for (int j = 0; j < l; j++)
        p[l * i + j] = attrib[j];
    }
    updateAttributes(n, p);
  }

  public void setLineWidth(float w) {
    lineWidth = w;
  }

  public void setPointSize(float s) {
    pointSize = PApplet.min(maxPointSize, s);
  }
  
  public float getMaxPointSize() {
    return maxPointSize;
  }
  
  /**
   * Sets the maximum sprite size (which is capped by the
   * maximum point size).
   */    
  public void setMaxSpriteSize(float s) {
    maxSpriteSize = PApplet.min(maxPointSize, s);
  }

  /**
   * Sets the sprite size to be constant and equal to s.
   */  
  public void setSpriteSize(float s) {
    setMaxSpriteSize(s);
    spriteDistAtt[1] = 0;
    spriteDistAtt[2] = 0;    
  }
  
  /**
   * Sets the sprite distance attenuation function for sprites using
   * quadratic dependence on the distance.
   */
  public void setSpriteSize(float s, float d) {
    setSpriteSize(s, d, true);
  }
  
  /**
   * Sets the sprite distance attenuation function for sprites such that
   * the sprite size at distance d from the camera is exactly s. The dependence
   * on the distance is quadratic (smax/(1+d^2)) or linear (smax/(1+d)) 
   * depending on the value of the argument quadratic. smax is the maximum
   * sprite size.
   */  
  public void setSpriteSize(float s, float d, boolean quadratic) {
    float s0 = maxSpriteSize;
    if (quadratic) {
      spriteDistAtt[1] = 0; 
      spriteDistAtt[2] = (s0 - s) / (d * d * s);      
    } else {
      spriteDistAtt[1] = (s0 - s) / (d * s);
      spriteDistAtt[2] = 0;
    }  
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
   * Set the tint color to the specified gray tone.
   * 
   * @param gray float
   */
  public void setTint(float gray) {
    int c = parent.color(gray);
    setTintColor(c);
  }

  /**
   * Set the tint color to the specified gray tone and alpha value.
   * 
   * @param gray int
   * @param alpha int
   */
  public void setTint(int gray, int alpha) {
    int c = parent.color(gray, alpha);
    setTintColor(c);
  }

  /**
   * Set the tint color to the specified rgb color and alpha value.
   * 
   * @param rgb int
   * @param alpha float
   */
  public void setTint(int rgb, float alpha) {
    int c = parent.color(rgb, alpha);
    setTintColor(c);
  }

  /**
   * Set the tint color to the specified gray tone and alpha value.
   * 
   * @param gray float
   * @param alpha float
   */
  public void setTint(float gray, float alpha) {
    int c = parent.color(gray, alpha);
    setTintColor(c);
  }

  /**
   * Set the tint color to the specified color components.
   * 
   * @param x int
   * @param y int
   * @param z int
   */
  public void setTint(int x, int y, int z) {
    int c = parent.color(x, y, z);
    setTintColor(c);
  }

  /**
   * Set the tint color to the specified color components.
   * 
   * @param x float
   * @param y float
   * @param z float
   */
  public void setTint(float x, float y, float z) {
    int c = parent.color(x, y, z);
    setTintColor(c);
  }

  /**
   * Set the tint color to the specified color components and alpha component.
   * 
   * @param x int
   * @param y int
   * @param z int
   * @param a int
   */
  public void setTint(int x, int y, int z, int a) {
    int c = parent.color(x, y, z, a);
    setTintColor(c);
  }

  /**
   * Set the tint color to the specified color components and alpha component.
   * 
   * @param x float
   * @param y float
   * @param z float
   * @param a float
   */
  public void setTint(float x, float y, float z, float a) {
    int c = parent.color(x, y, z, a);
    setTintColor(c);
  }

  protected void setTintColor(int color) {
    int ir, ig, ib, ia;

    ia = (color >> 24) & 0xff;
    ir = (color >> 16) & 0xff;
    ig = (color >> 8) & 0xff;
    ib = color & 0xff;

    tintA = ia / 255.0f;
    tintR = ir / 255.0f;
    tintG = ig / 255.0f;
    tintB = ib / 255.0f;
  }

  /**
   * Set the specular color to the specified gray tone.
   * 
   * @param gray float
   */
  public void setReflection(float gray) {
    int c = parent.color(gray);
    setSpecularColor(c);
  }

  /**
   * Set the specular color to the specified gray tone and alpha value.
   * 
   * @param gray int
   * @param alpha int
   */
  public void setReflection(int gray, int alpha) {
    int c = parent.color(gray, alpha);
    setSpecularColor(c);
  }

  /**
   * Set the specular color to the specified rgb color and alpha value.
   * 
   * @param rgb int
   * @param alpha float
   */
  public void setReflection(int rgb, float alpha) {
    int c = parent.color(rgb, alpha);
    setSpecularColor(c);
  }

  /**
   * Set the specular color to the specified gray tone and alpha value.
   * 
   * @param gray float
   * @param alpha float
   */
  public void setReflection(float gray, float alpha) {
    int c = parent.color(gray, alpha);
    setSpecularColor(c);
  }

  /**
   * Set the specular color to the specified color components.
   * 
   * @param x int
   * @param y int
   * @param z int
   */
  public void setReflection(int x, int y, int z) {
    int c = parent.color(x, y, z);
    setSpecularColor(c);
  }

  /**
   * Set the specular color to the specified color components.
   * 
   * @param x float
   * @param y float
   * @param z float
   */
  public void setReflection(float x, float y, float z) {
    int c = parent.color(x, y, z);
    setSpecularColor(c);
  }

  /**
   * Set the specular color to the specified color components and alpha
   * component.
   * 
   * @param x int
   * @param y int
   * @param z int
   * @param a int
   */
  public void setReflection(int x, int y, int z, int a) {
    int c = parent.color(x, y, z, a);
    setSpecularColor(c);
  }

  /**
   * Set the specular color to the specified color components and alpha
   * component.
   * 
   * @param x float
   * @param y float
   * @param z float
   * @param a float
   */
  public void setReflection(float x, float y, float z, float a) {
    int c = parent.color(x, y, z, a);
    setSpecularColor(c);
  }

  protected void setSpecularColor(int color) {
    int ir, ig, ib, ia;

    ia = (color >> 24) & 0xff;
    ir = (color >> 16) & 0xff;
    ig = (color >> 8) & 0xff;
    ib = color & 0xff;

    specularColor[0] = ir / 255.0f;
    specularColor[1] = ig / 255.0f;
    specularColor[2] = ib / 255.0f;
    specularColor[3] = ia / 255.0f;
  }

  /**
   * Set the emissive color to the specified gray tone.
   * 
   * @param gray float
   */
  public void setEmission(float gray) {
    int c = parent.color(gray);
    setEmissiveColor(c);
  }

  /**
   * Set the emissive color to the specified gray tone and alpha value.
   * 
   * @param gray int
   * @param alpha int
   */
  public void setEmission(int gray, int alpha) {
    int c = parent.color(gray, alpha);
    setEmissiveColor(c);
  }

  /**
   * Set the emissive color to the specified rgb color and alpha value.
   * 
   * @param rgb int
   * @param alpha float
   */
  public void setEmission(int rgb, float alpha) {
    int c = parent.color(rgb, alpha);
    setEmissiveColor(c);
  }

  /**
   * Set the emissive color to the specified gray tone and alpha value.
   * 
   * @param gray float
   * @param alpha float
   */
  public void setEmission(float gray, float alpha) {
    int c = parent.color(gray, alpha);
    setEmissiveColor(c);
  }

  /**
   * Set the emissive color to the specified color components.
   * 
   * @param x int
   * @param y int
   * @param z int
   */
  public void setEmission(int x, int y, int z) {
    int c = parent.color(x, y, z);
    setEmissiveColor(c);
  }

  /**
   * Set the emissive color to the specified color components.
   * 
   * @param x float
   * @param y float
   * @param z float
   */
  public void setEmission(float x, float y, float z) {
    int c = parent.color(x, y, z);
    setEmissiveColor(c);
  }

  /**
   * Set the emissive color to the specified color components and alpha
   * component.
   * 
   * @param x int
   * @param y int
   * @param z int
   * @param a int
   */
  public void setEmission(int x, int y, int z, int a) {
    int c = parent.color(x, y, z, a);
    setEmissiveColor(c);
  }

  /**
   * Set the emissive color to the specified color components and alpha
   * component.
   * 
   * @param x float
   * @param y float
   * @param z float
   * @param a float
   */
  public void setEmission(float x, float y, float z, float a) {
    int c = parent.color(x, y, z, a);
    setEmissiveColor(c);
  }

  public void setShininess(float val) {
    shininess[0] = val;
  }
  
  protected void setEmissiveColor(int color) {
    int ir, ig, ib, ia;

    ia = (color >> 24) & 0xff;
    ir = (color >> 16) & 0xff;
    ig = (color >> 8) & 0xff;
    ib = color & 0xff;

    emissiveColor[0] = ir / 255.0f;
    emissiveColor[1] = ig / 255.0f;
    emissiveColor[2] = ib / 255.0f;
    emissiveColor[3] = ia / 255.0f;
  }

  public void render() {
    if (indicesVBO == null) {
      render(0, size - 1, null);
    } else {
      render(0, indicesCount, null);
    }
  }

  public void render(GLModelEffect effect) {
    if (indicesVBO == null) {
      render(0, size - 1, effect);
    } else {
      render(0, indicesCount, effect);
    }
  }

  public void render(int first, int last) {
    render(first, last, null);
  }

  public void render(int first, int last, GLModelEffect effect) {
    // Note about color/lights in Processing/OPENGL/GLGraphics.
    // Lighting is based on the assumption that color material tracking is enabled
    // and set to AMBIENT_AND_DIFFUSE, meaning that the ambient and diffuse components
    // of the material properties of vertices will be taken from the color set with
    // glColor() (or copied with a color buffer). This works in coordination with 
    // the light setup in GLGraphics (see the implementation of glEnableLights
    // where you have:
    // gl.glEnable(GL.GL_COLOR_MATERIAL);
    // gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
    // For more flexible material/lighting setups, the best option is just to 
    // override the built-in behaviour using OpenGL. This tutorial is quite
    // useful in this regard:
    // http://www.sjbaker.org/steve/omniv/opengl_lighting.html
    if (colorsVBO == null) {
      gl.glColor4f(tintR, tintG, tintB, tintA);
    }
    
    glstate.saveBlendConfig();
    if (blend) {
     glstate.enableBlend();
     glstate.setupBlending(blendMode);
    } else {
      glstate.disableBlend();
    }
    
    // Setting specular and emissive colors.     
    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specularColor, 0);    
    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, emissiveColor, 0);
    
    // Setting shininess.
    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess, 0);

    gl.glLineWidth(lineWidth);
    if (!usingPointSprites)
      gl.glPointSize(pointSize);

    if (effect != null)
      effect.start();

    if (normCoordsVBO != null) {
      gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
      gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, normCoordsVBO[0]);
      gl.glNormalPointer(GL.GL_FLOAT, 4 * BufferUtil.SIZEOF_FLOAT, 0);
    }

    if (colorsVBO != null) {
      gl.glEnableClientState(GL.GL_COLOR_ARRAY);
      gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, colorsVBO[0]);
      gl.glColorPointer(4, GL.GL_FLOAT, 0, 0);
    }

    if (indicesVBO != null) {
      gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER, indicesVBO[0]);
    }
    
    if (texCoordsVBO != null) {
      gl.glEnable(textures[0].getTextureTarget());

      // Binding texture units.
      for (int n = 0; n < numTextures; n++) {
        textures[n].bind(n);
      }

      if (usingPointSprites) {
        // Texturing with point sprites.
        
        // The alpha of a point is calculated to allow the fading of points 
        // instead of shrinking them past a defined threshold size. The threshold 
        // is defined by GL_POINT_FADE_THRESHOLD_SIZE and is not clamped to the 
        // minimum and maximum point sizes.
        gl.glPointParameterfARB(GL.GL_POINT_FADE_THRESHOLD_SIZE_ARB, 0.6f * maxSpriteSize);
        gl.glPointParameterfARB(GL.GL_POINT_SIZE_MIN_ARB, 1.0f);
        gl.glPointParameterfARB(GL.GL_POINT_SIZE_MAX_ARB, maxSpriteSize);
        gl.glPointSize(maxSpriteSize);
        
        // This is how will our point sprite's size will be modified by 
        // distance from the viewer:
        // actualSize = pointSize / sqrt(p[0] + p[1] * d + p[2] * d * d)
        // where pointSize is the value set with glPointSize(), clamped to the extreme values
        // in glPointParameterf(GL11.GL_POINT_SIZE_MIN/GL11.GL_POINT_SIZE_MAX. 
        // d is the distance from the point sprite to the camera and p is the array parameter 
        // passed in the following call: 
        gl.glPointParameterfvARB(GL.GL_POINT_DISTANCE_ATTENUATION, spriteDistAtt, 0); 

        // Specify point sprite texture coordinate replacement mode for each
        // texture unit
        gl.glTexEnvf(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_TRUE);

        gl.glEnable(GL.GL_POINT_SPRITE_ARB);
      } else {
        // Regular texturing.
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        for (int n = 0; n < numTextures; n++) {
          gl.glClientActiveTexture(GL.GL_TEXTURE0 + n);
          gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsVBO[n]);
          gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);
        }
      }

      if (effect != null)
        effect.setTextures(textures);
    }

    // Drawing the vertices:
    gl.glEnableClientState(GL.GL_VERTEX_ARRAY);

    // ...with their attributes.
    if (effect != null) {
      effect.enableVertexAttribs();
      
      effect.setVertexAttribs(attribVBO, attribName, attribSize);      
    }

    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertCoordsVBO[0]);

    // The vertices in the array have 4 components: x, y, z, w. If the user
    // doesn't explicity specify w, then it is set to 1 by default.
    gl.glVertexPointer(4, GL.GL_FLOAT, 0, 0);

    if (indicesVBO == null) {
      gl.glDrawArrays(vertexMode, first, last - first + 1);
    } else {
      // The meaning for the minIndex and maxIndex arguments is discussed in this thread:
      // http://www.gamedev.net/topic/331317-trouble-with-gldrawrangeelements/
      gl.glDrawRangeElements(vertexMode, minIndex, maxIndex, last - first + 1, GL.GL_UNSIGNED_INT, first * BufferUtil.SIZEOF_INT);
    }

    if (effect != null)
      effect.disableVertexAttribs();

    gl.glBindBuffer(GL.GL_ARRAY_BUFFER_ARB, 0);
    gl.glDisableClientState(GL.GL_VERTEX_ARRAY);

    if (texCoordsVBO != null) {
      if (usingPointSprites) {
        gl.glDisable(GL.GL_POINT_SPRITE_ARB);
      } else {
        for (int n = 0; n < numTextures; n++) {
          gl.glClientActiveTexture(GL.GL_TEXTURE0 + n);
          gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
        }
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
      }      
      for (int n = 0; n < numTextures; n++) {
        textures[n].unbind();
      }             
      gl.glDisable(textures[0].getTextureTarget());
    }
    if (colorsVBO != null) {
      gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
      gl.glDisableClientState(GL.GL_COLOR_ARRAY);
    }
    if (normCoordsVBO != null) {
      gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
      gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
    }
    if (indicesVBO != null) {
      gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    if (effect != null)
      effect.stop();
    
    glstate.restoreBlendConfig();
  }
  
  // /////////////////////////////////////////////////////////////////////////////////
  // Saving, loading from binary.

  protected void loadPVectorArrayListFromBinary(String filename,
      ArrayList<PVector> data, int nelem) {
    try {
      FileInputStream fis = new FileInputStream(parent.dataPath("") + filename);
      DataInputStream dis = new DataInputStream(fis);

      while (dis.available() > 2) {
        float x, y, z;
        x = y = z = 0;
        if (1 <= nelem)
          x = dis.readShort() / 100f;
        if (2 <= nelem)
          y = dis.readShort() / 100f;
        if (3 <= nelem)
          z = dis.readShort() / 100f;

        PVector thisLoc = new PVector(x, y, z);
        data.add(thisLoc);
      }
      fis.close();
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
  }

  protected void savePVectorArrayListToBinary(String fileName,
      ArrayList<PVector> data, int nelem) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    for (int i = 0; i < data.size(); i++) {
      PVector loc = (PVector) data.get(i);
      try {
        if (1 <= nelem)
          dos.writeShort((short) (loc.x * 100f));
        if (2 <= nelem)
          dos.writeShort((short) (loc.y * 100f));
        if (3 <= nelem)
          dos.writeShort((short) (loc.z * 100f));
        dos.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    byte[] saveBytes = baos.toByteArray();
    parent.saveBytes(fileName, saveBytes);
  }

  protected void loadFloatArrayListFromBinary(String filename,
      ArrayList<float[]> data, int nelem) {
    try {
      FileInputStream fis = new FileInputStream(parent.dataPath("") + filename);
      DataInputStream dis = new DataInputStream(fis);

      while (dis.available() > 2) {
        float[] loc = new float[nelem];
        for (int k = 0; k < nelem; k++)
          loc[k] = dis.readShort() / 100f;
        data.add(loc);
      }
      fis.close();
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
  }

  protected void saveFloatArrayToBinary(String fileName,
      ArrayList<float[]> data, int nelem) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    for (int i = 0; i < data.size(); i++) {
      float[] loc = (float[]) data.get(i);
      try {
        for (int k = 0; k < nelem; k++)
          dos.writeShort((short) (loc[k] * 100f));
        dos.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    byte[] saveBytes = baos.toByteArray();
    parent.saveBytes(fileName, saveBytes);
  }

  protected void loadFloatArrayFromBinary(String filename, float[] data,
      int nsize, int nelem, int nsave) {
    try {
      FileInputStream fis = new FileInputStream(parent.dataPath("") + filename);
      DataInputStream dis = new DataInputStream(fis);

      for (int n = 0; n < nsize; n++) {
        for (int k = 0; k < nsave; k++)
          //data[nelem * n + k] = dis.readShort() / 100f;
          data[nelem * n + k] = dis.readFloat();
        if (dis.available() <= 2)
          break;
      }

      fis.close();
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
  }

  protected void saveFloatArrayToBinary(String fileName, float[] data,
      int nsize, int nelem, int nsave) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    for (int n = 0; n < nsize; n++) {
      try {
        for (int k = 0; k < nsave; k++)
          dos.writeFloat(data[nelem * n + k]);  
          //dos.writeShort((short) (data[nelem * n + k] * 100f));
        dos.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    byte[] saveBytes = baos.toByteArray();
    parent.saveBytes(fileName, saveBytes);
  }

  /**
   * Sets the positions corresponding to vertex i in the tmpColorArray to the
   * specified color.
   * 
   * @param i int
   * @param color int
   */
  protected void putColorInTmpArray(int i, int color) {
    int ir, ig, ib, ia;

    if (tmpColorArray == null) {
      tmpColorArray = new float[4 * size];
      colors.get(tmpColorArray);
      colors.rewind();
    }

    ia = (color >> 24) & 0xff;
    ir = (color >> 16) & 0xff;
    ig = (color >> 8) & 0xff;
    ib = color & 0xff;

    tmpColorArray[4 * i + 0] = ir / 255.0f;
    tmpColorArray[4 * i + 1] = ig / 255.0f;
    tmpColorArray[4 * i + 2] = ib / 255.0f;
    tmpColorArray[4 * i + 3] = ia / 255.0f;
  }

  /**
   * Sets all the vertices with the specified color.
   * 
   * @param color int
   */
  protected void updateAllColors(int color) {
    int ir, ig, ib, ia;
    float fr, fg, fb, fa;

    beginUpdateColors();
    if (tmpColorArray == null) {
      tmpColorArray = new float[4 * size];
      colors.get(tmpColorArray);
      colors.rewind();
    }

    ia = (color >> 24) & 0xff;
    ir = (color >> 16) & 0xff;
    ig = (color >> 8) & 0xff;
    ib = color & 0xff;

    fa = ia / 255.0f;
    fr = ir / 255.0f;
    fg = ig / 255.0f;
    fb = ib / 255.0f;

    for (int i = 0; i < size; i++) {
      tmpColorArray[4 * i + 0] = fr;
      tmpColorArray[4 * i + 1] = fg;
      tmpColorArray[4 * i + 2] = fb;
      tmpColorArray[4 * i + 3] = fa;
    }
    endUpdateColors();
  }

  protected void initModelCommon(PApplet parent) {
    this.parent = parent;
    pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;

    glstate = new GLState(gl);
    
    tintR = tintG = tintB = tintA = 1.0f;
    shininess[0] = 0.0f;

    pointSize = 1.0f;
    lineWidth = 1.0f;
    usingPointSprites = false;
    
    blend = true;
    blendMode = BLEND;

    tmpVertArray = null;
    tmpColorArray = null;
    tmpNormalsArray = null;
    tmpTexCoordsArray = null;
    tmpAttributesArray = null;
  }

  protected void releaseModel() {
    if (vertCoordsVBO[0] != 0) {
      GLState.deleteGLResource(vertCoordsVBO[0], GL_VERTEX_BUFFER);
    }

    if (colorsVBO != null && colorsVBO[0] != 0) {
      GLState.deleteGLResource(colorsVBO[0], GL_VERTEX_BUFFER);
    }

    if (normCoordsVBO != null && normCoordsVBO[0] != 0) {
      GLState.deleteGLResource(normCoordsVBO[0], GL_VERTEX_BUFFER);
    }

    if (texCoordsVBO != null && texCoordsVBO[0] != 0) {
      for (int n = 0; n < numTextures; n++) {
        GLState.deleteGLResource(texCoordsVBO[n], GL_VERTEX_BUFFER);  
      }
    }

    if (indicesVBO != null && indicesVBO[0] != 0) {
      GLState.deleteGLResource(indicesVBO[0], GL_VERTEX_BUFFER);
    }    
    
    if (attribVBO != null && attribVBO[0] != 0) {
      for (int n = 0; n < numAttributes; n++) {
        GLState.deleteGLResource(attribVBO[n], GL_VERTEX_BUFFER);  
      }
    }    
  }
  
  protected void loadXML(XMLElement xml) {
    int n = xml.getChildCount();
    String name, content;
    XMLElement child;

    GLTexture[] texturesList;
    ArrayList<PVector> verticesList;
    ArrayList<PVector>[] texCoordsList;
    ArrayList<float[]>[] vertexAttribsList;
    ArrayList<PVector> normalsList;
    ArrayList<float[]> colorsList;
    String[] texNames;
    String[] attrNames;
    int[] attrSizes;

    texturesList = null;
    verticesList = new ArrayList<PVector>();
    texCoordsList = null;
    vertexAttribsList = null;
    normalsList = new ArrayList<PVector>();
    colorsList = new ArrayList<float[]>();
    texNames = null;
    attrNames = null;
    attrSizes = null;

    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("description")) {
        description = child.getContent();
      } else if (name.equals("size")) {
        size = PApplet.parseInt(child.getContent());
      } else if (name.equals("geometry")) {
        content = child.getContent();
        vertexMode = GLUtils.parsePrimitive(content);
        if (vertexMode == GL.GL_POINTS && content.equals("POINT_SPRITES")) {
          vertexMode = GL.GL_POINTS;
          usingPointSprites = true;
          float[] tmp = { 0.0f };
          gl.glGetFloatv(GL.GL_POINT_SIZE_MAX_ARB, tmp, 0);
          maxPointSize = tmp[0];
          maxSpriteSize = maxPointSize;
        }
      } else if (name.equals("mode")) {
        vboUsage = GLUtils.parseVBOMode(child.getContent());
      } else if (name.equals("textures")) {
        int ntex = child.getChildCount();
        texturesList = new GLTexture[ntex];
        texNames = new String[ntex];
        texCoordsList = new ArrayList[ntex];

        loadTextures(child, texturesList, texCoordsList, texNames);
      } else if (name.equals("vertexattribs")) {
        int nattr = child.getChildCount();

        vertexAttribsList = new ArrayList[nattr];
        attrNames = new String[nattr];
        attrSizes = new int[nattr];

        loadVertexAttribs(child, vertexAttribsList, attrNames, attrSizes);
      } else if (name.equals("vertices")) {
        String binfile = child.getStringAttribute("file");
        if (binfile != null)
          loadVertices(binfile, verticesList);
        else
          loadVertices(child, verticesList);
      } else if (name.equals("texcoords")) {
        if (texCoordsList != null) {
          int unit = child.getIntAttribute("unit");
          if (texCoordsList[unit] != null) {
            String binfile = child.getStringAttribute("file");
            if (binfile != null)
              loadTexCoords(binfile, texCoordsList[unit]);
            else
              loadTexCoords(child, texCoordsList[unit]);
          }
        }
      } else if (name.equals("colors")) {
        String binfile = child.getStringAttribute("file");
        if (binfile != null)
          loadColors(binfile, colorsList);
        else
          loadColors(child, colorsList);
      } else if (name.equals("normals")) {
        String binfile = child.getStringAttribute("file");
        if (binfile != null)
          loadNormals(binfile, normalsList);
        else
          loadNormals(child, normalsList);
      } else if (name.equals("attribs")) {
        if (vertexAttribsList != null && attrSizes != null) {
          int num = child.getIntAttribute("number");
          if (vertexAttribsList[num] != null) {
            String binfile = child.getStringAttribute("file");
            if (binfile != null)
              loadVertexAttrib(binfile, vertexAttribsList[num], attrSizes[num]);
            else
              loadVertexAttrib(child, vertexAttribsList[num], attrSizes[num]);
          }
        }
      }
    }

    vertCoordsVBO[0] = GLState.createGLResource(GL_VERTEX_BUFFER);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertCoordsVBO[0]);
    gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, size * 4
        * BufferUtil.SIZEOF_FLOAT, null, vboUsage);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);

    updateVertices(verticesList);

    int ntex = texturesList.length;
    if (0 < ntex) {
      initTextures(ntex);
      GLTexture tex;
      ArrayList<PVector> tcoords;

      for (int j = 0; j < ntex; j++) {
        tex = texturesList[j];
        setTexture(j, tex);
        // It should be something like:
        // setTexture(j, tex, texNames[j]);
        // but texture names are still not used.

        tcoords = (ArrayList<PVector>) texCoordsList[j];
        if (tcoords.size() == size)
          updateTexCoords(j, tcoords);
      }
    }

    if (normalsList.size() == size) {
      initNormals();
      updateNormals(normalsList);
    }

    if (colorsList.size() == size) {
      initColors();
      updateColors(colorsList);
    }

    int nattr = vertexAttribsList.length;
    if (0 < nattr) {
      initAttributes(nattr);
      ArrayList<float[]> attribs;

      for (int j = 0; j < nattr; j++) {
        setAttribute(j, attrNames[j], attrSizes[j]);

        attribs = vertexAttribsList[j];
        updateAttributes(j, attribs);
      }
    }
  }

  protected void loadTextures(XMLElement xml, GLTexture[] texturesList,
      ArrayList<PVector>[] texCoordsList, String[] texNames) {
    int n = xml.getChildCount();
    XMLElement child;
    String name;

    String unitStr, fn;
    int unit;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("texture")) {
        unitStr = child.getContent();
        unit = PApplet.parseInt(unitStr);

        texCoordsList[unit] = new ArrayList<PVector>();

        texNames[unit] = child.getStringAttribute("name");
        fn = child.getStringAttribute("file");
        texturesList[unit] = new GLTexture(parent, fn);
        texturesList[unit].setName(texNames[unit]);
      }
    }
  }

  protected void loadVertexAttribs(XMLElement xml,
      ArrayList<float[]>[] vertexAttribsList, String[] attrNames,
      int[] attrSizes) {
    int n = xml.getChildCount();
    XMLElement child;
    String name;

    String numStr;
    int num;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("vertexattrib")) {
        numStr = child.getContent();
        num = PApplet.parseInt(numStr);

        vertexAttribsList[num] = new ArrayList<float[]>();

        attrNames[num] = child.getStringAttribute("name");
        attrSizes[num] = child.getIntAttribute("size");
      }
    }
  }

  protected void loadVertices(XMLElement xml, ArrayList<PVector> verticesList) {
    int n = xml.getChildCount();
    XMLElement child;
    String name;

    String coordStr;
    float[] coord;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("vertex")) {
        coordStr = child.getContent();
        coord = PApplet.parseFloat(PApplet.split(coordStr, ' '));

        if (coord.length == 3)
          verticesList.add(new PVector(coord[0], coord[1], coord[2]));
      }
    }
  }

  protected void loadVertices(String binaryFN, ArrayList<PVector> verticesList) {
    loadPVectorArrayListFromBinary(binaryFN, verticesList, 3);
  }

  protected void loadTexCoords(XMLElement xml, ArrayList<PVector> texCoordsList) {
    int n = xml.getChildCount();
    XMLElement child;
    String name;

    String coordStr;
    float[] coord;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("texcoord")) {
        coordStr = child.getContent();
        coord = PApplet.parseFloat(PApplet.split(coordStr, ' '));

        if (coord.length == 2)
          texCoordsList.add(new PVector(coord[0], coord[1]));
      }
    }
  }

  protected void loadTexCoords(String binaryFN, ArrayList<PVector> texCoordsList) {
    loadPVectorArrayListFromBinary(binaryFN, texCoordsList, 2);
  }

  protected void loadColors(XMLElement xml, ArrayList<float[]> colorsList) {
    int n = xml.getChildCount();
    XMLElement child;
    String name;

    String coordStr;
    float[] coord;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("color")) {
        coordStr = child.getContent();
        coord = PApplet.parseFloat(PApplet.split(coordStr, ' '));

        if (coord.length == 4)
          colorsList.add(coord);
      }
    }
  }

  protected void loadColors(String binaryFN, ArrayList<float[]> colorsList) {
    loadFloatArrayListFromBinary(binaryFN, colorsList, 4);
  }

  protected void loadNormals(XMLElement xml, ArrayList<PVector> normalsList) {
    int n = xml.getChildCount();
    XMLElement child;
    String name;

    String coordStr;
    float[] coord;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("normal")) {
        coordStr = child.getContent();
        coord = PApplet.parseFloat(PApplet.split(coordStr, ' '));

        if (coord.length == 3)
          normalsList.add(new PVector(coord[0], coord[1], coord[2]));
      }
    }
  }

  protected void loadNormals(String binaryFN, ArrayList<PVector> normalsList) {
    loadPVectorArrayListFromBinary(binaryFN, normalsList, 3);
  }

  protected void loadVertexAttrib(XMLElement xml,
      ArrayList<float[]> vertexAttribsList, int attrSize) {
    int n = xml.getChildCount();
    XMLElement child;
    String name;

    String coordStr;
    float[] coord;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("attrib")) {
        coordStr = child.getContent();
        coord = PApplet.parseFloat(PApplet.split(coordStr, ' '));

        if (coord.length == attrSize)
          vertexAttribsList.add(coord);
      }
    }
  }

  protected void loadVertexAttrib(String binaryFN,
      ArrayList<float[]> vertexAttribsList, int attrSize) {
    loadFloatArrayListFromBinary(binaryFN, vertexAttribsList, attrSize);
  }
}
