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
import java.net.URL;

import processing.core.PApplet;
import com.sun.opengl.cg.CGcontext;
import com.sun.opengl.cg.CGparameter;
import com.sun.opengl.cg.CGprogram;
import com.sun.opengl.cg.CgGL;

/**
 * This class encapsulates a Cg shader. Based in the code by Victor Martins
 * (http://www.pixelnerve.com/v/)
 */
public class GLCgShader extends GLShader {
  protected boolean vertexShaderEnabled;
  protected boolean fragmentShaderEnabled;
  protected boolean geometryShaderEnabled;
  
  protected CGcontext context;

  protected int vertexProfile;
  protected int fragmentProfile;
  protected int geometryProfile;

  protected CGprogram currProgram; // current active program
  protected CGprogram vertexProgram;
  protected CGprogram fragmentProgram;
  protected CGprogram geometryProgram;
  
  protected boolean initialized;  
  
  /**
   * Creates an instance of GLCgShader.
   * 
   * @param parent PApplet
   */
  public GLCgShader(PApplet parent) {
    super(parent);
    
    context = (CGcontext)GLState.createCGResource(CG_CONTEXT);    
    CgGL.cgGLRegisterStates(context);
    // CgGL.cgSetParameterSettingMode(context, CgGL.CG_DEFERRED_PARAMETER_SETTING );
    CgGL.cgGLSetManageTextureParameters(context, true);

    vertexShaderEnabled = false;
    fragmentShaderEnabled = false;
    geometryShaderEnabled = false;
    
    vertexProgram = null;
    fragmentProgram = null;
    geometryProgram = null;

    currProgram = null;
    
    initialized = false;
  }  
    
  /**
   * Creates a read-to-use instance of GLCgShader with vertex and fragment shaders
   * 
   * @param parent PApplet
   * @param vertexFN String
   * @param fragmentFN String          
   */  
  public GLCgShader(PApplet parent, String vertexFN, String fragmentFN) {
    this(parent);
    
    loadVertexShader(vertexFN);
    
    loadFragmentShader(fragmentFN);
    
    setup();
    currProgram = vertexProgram;    
    // Shader ready to use...
  }
  
  /**
   * Creates a read-to-use instance of GLCgShader with vertex, geometry and fragment shaders
   * 
   * @param parent PApplet
   * @param vertexFN String
   * @param fragmentFN String          
   */   
  public GLCgShader(PApplet parent, String vertexFN, String geometryFN, String fragmentFN) {
    this(parent);
    loadVertexShader(vertexFN);
    loadGeometryShader(geometryFN);
    loadFragmentShader(fragmentFN);
    setup();
    currProgram = vertexProgram;    
    // Shader ready to use...  
  }  
  
  public void delete() {
    release();
  }    

  /**
   * Reads vertex shader from file.
   * 
   * @param file String
   */  
  public void loadVertexShader(String file) {    
    String[] lines = parent.loadStrings(file);
    replaceIncludes(lines);
    String source = PApplet.join(lines, "\n");    
    createVertexProgram(source, file);
  }
  
  /**
   * Reads vertex shader from url.
   * 
   * @param url URL
   */  
  public void loadVertexShader(URL url) {
    String source;
    try {
      source = PApplet.join(PApplet.loadStrings(url.openStream()), "\n");
      createVertexProgram(source, url.getFile());
    } catch (IOException e) {
      System.err.println("Cannot load file " + url.getFile());
    }    
  }
  
  /**
   * Reads geometry shader from file.
   * 
   * @param file String
   */    
  public void loadGeometryShader(String file) {
    String[] lines = parent.loadStrings(file);
    replaceIncludes(lines);
    String source = PApplet.join(lines, "\n");    
    createGeometryProgram(source, file);
  }
  
  /**
   * Reads geometry shader from url.
   * 
   * @param url URL
   */    
  public void loadGeometryShader(URL url) {
    String source;
    try {
      source = PApplet.join(PApplet.loadStrings(url.openStream()), "\n");
      createGeometryProgram(source, url.getFile());
    } catch (IOException e) {
      System.err.println("Cannot load file " + url.getFile());
    }    
  }
  
  /**
   * Reads fragment shader from file.
   * 
   * @param file String
   */      
  public void loadFragmentShader(String file) {
    String[] lines = parent.loadStrings(file);
    replaceIncludes(lines);
    String source = PApplet.join(lines, "\n");    
    createFragmentProgram(source, file);    
  }
  
  /**
   * Reads fragment shader from url.
   * 
   * @param url URL
   */        
  public void loadFragmentShader(URL url) {
    String source;
    try {
      source = PApplet.join(PApplet.loadStrings(url.openStream()), "\n");
      createFragmentProgram(source, url.getFile());
    } catch (IOException e) {
      System.err.println("Cannot load file " + url.getFile());
    }       
  }  
  
  /**
   * Compiles all the shaders.
   */        
  public void setup() {
    if (vertexShaderEnabled) {
      CgGL.cgGLLoadProgram(vertexProgram);
    }
    if (fragmentShaderEnabled) {
      CgGL.cgGLLoadProgram(fragmentProgram);
    }
    if (geometryShaderEnabled) {
      CgGL.cgGLLoadProgram(geometryProgram);
    }
    initialized = true;
  }  
  
  /**
   * Returns true or false depending on whether the shader is initialized or not.
   */  
  public boolean isInitialized() {
    return initialized;
  }
  
  /**
   * Starts shader execution.
   */   
  public void start() {
    if (!initialized) {
      System.err.println("This shader is not properly initialized. Call the setup() method first");
    }    
    if (vertexShaderEnabled) {
      CgGL.cgGLBindProgram(vertexProgram);
      CgGL.cgGLEnableProfile(vertexProfile);
    }
    if (fragmentShaderEnabled) {
      CgGL.cgGLBindProgram(fragmentProgram);
      CgGL.cgGLEnableProfile(fragmentProfile);
    }
    if (geometryShaderEnabled) {
      CgGL.cgGLBindProgram(geometryProgram);
      CgGL.cgGLEnableProfile(geometryProfile);
    }
  }

  /**
   * Stops shader execution.
   */   
  public void stop() {
    if (vertexShaderEnabled) {
      CgGL.cgGLDisableProfile(vertexProfile);
    }
    if (fragmentShaderEnabled) {
      CgGL.cgGLDisableProfile(fragmentProfile);
    }
    if (geometryShaderEnabled) {
      CgGL.cgGLDisableProfile(geometryProfile);
    }
  }

 /**
   * Returns the parameter in the vertex shader with the sepcified name.
   * 
   * @param name String
   */    
  public CGparameter getVertexParameter(String name) {
    return CgGL.cgGetNamedParameter(vertexProgram, name);
  }

 /**
   * Returns the parameter in the geometry shader with the sepcified name.
   * 
   * @param name String
   */    
  public CGparameter getGeometryParameter(String name) {
    return CgGL.cgGetNamedParameter(vertexProgram, name);
  }
  
 /**
   * Returns the parameter in the fragment shader with the sepcified name.
   * 
   * @param name String
   */      
  public CGparameter getFragmentParameter(String name) {
    return CgGL.cgGetNamedParameter(fragmentProgram, name);
  }  

 /**
   * Sets the current program which we pass parameters to. p could be
   * VERTEX_PROGRAM, FRAGMENT_PROGRAM or GEOMETRY_PROGRAM
   * 
   * @param p int
   */      
  public void setProgram(int p) {
    switch( p ) {
    case VERTEX_PROGRAM:
      currProgram = vertexProgram;
      break;
    case FRAGMENT_PROGRAM:
      currProgram = fragmentProgram;
      break;
    case GEOMETRY_PROGRAM:
      currProgram = geometryProgram;
      break;
    default:
      currProgram = vertexProgram;
      break;
    }
  }

  /**
   * Sets the texture parameter name with the OpenGL id of the 
   * provided texture object.
   * 
   * @param name String
   * @param tex GLTexture
   */               
  public void setTexParameter(String name, GLTexture tex) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) CgGL.cgGLSetTextureParameter(p, tex.getTextureID());
  }
  
  /**
   * Sets the int parameter with name to the given value 
   * 
   * @param name String
   * @param x int
   */              
  public void setIntParameter(String name, int x) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) CgGL.cgSetParameter1i(p, x);    
  }

  /**
   * Sets the float parameter with name to the given value 
   * 
   * @param name String
   * @param x float
   */
  public void setFloatParameter(String name, float x) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) CgGL.cgSetParameter1f(p, x);    
  }

  /**
   * Sets the vec2 parameter with name to the given values
   * 
   * @param name String
   * @param x float
   * @param y float          
   */    
  public void setVecParameter(String name, float x, float y) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) CgGL.cgSetParameter2f(p, x, y);    
  }  

  /**
   * Sets the vec3 parameter with name to the given values
   * 
   * @param name String
   * @param x float
   * @param y float
   * @param z float    
   */  
  public void setVecParameter(String name, float x, float y, float z) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) CgGL.cgSetParameter3f(p, x, y, z);    
  }  

  /**
   * Sets the vec4 parameter with name to the given values 
   * 
   * @param name String
   * @param x float
   * @param y float
   * @param z float
   * @param w float
   */    
  public void setVecParameter(String name, float x, float y, float z, float w) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) CgGL.cgSetParameter4f(p, x, y, z, w);
  }  
  
  /**
   * Sets the mat2 parameter with name to the given values
   * 
   * @param name String
   * @param m00 float
   *        ...
   */    
  public void setMatParameter(String name,  float m00, float m01,
                                            float m10, float m11) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) {
      float[] mat = new float[4];
      mat[0] = m00;
      mat[1] = m10;
      mat[4] = m01;
      mat[5] = m11;          
      CgGL.cgSetMatrixParameterfc(p, mat, 0);
    }
  }  
  
  /**
   * Sets the mat3 parameter with name to the given value 
   * 
   * @param name String
   * @param m00 float
   *        ...
   */      
  public void setMatParameter(String name, float m00, float m01, float m02,
                                           float m10, float m11, float m12,
                                           float m20, float m21, float m22) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) { 
      float[] mat = new float[9];
      mat[0] = m00;
      mat[1] = m10;
      mat[2] = m20;
      mat[4] = m01;
      mat[5] = m11;
      mat[6] = m21;
      mat[8] = m02;
      mat[9] = m12;
      mat[10] =m22;             
      CgGL.cgSetMatrixParameterfc(p, mat, 0);    
    }
  }
  
  /**
   * Sets the mat4 parameter with name to the given value 
   * 
   * @param name String
   * @param m00 float
   *        ...
   */      
  public void setMatParameter(String name, float m00, float m01, float m02, float m03,
                                           float m10, float m11, float m12, float m13,
                                           float m20, float m21, float m22, float m23,
                                           float m30, float m31, float m32, float m33) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) {
      float[] mat = new float[16];      
      mat[0] = m00;
      mat[1] = m10;
      mat[2] = m20;
      mat[3] = m30;
      mat[4] = m01;
      mat[5] = m11;
      mat[6] = m21;
      mat[7] = m31;
      mat[8] = m02;
      mat[9] = m12;
      mat[10] = m22;
      mat[11] = m32;
      mat[12] = m03;
      mat[13] = m13;
      mat[14] = m23;
      mat[15] = m33;      
      CgGL.cgSetMatrixParameterfc(p, mat, 0);    
    }
  }  
  
  /**
   * Copies the modelview matrix from OpenGL to parameter name in the shader. 
   * 
   * @param name String
   */        
  public void setModelviewMatrix(String name) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
  }

  /**
   * Copies the projection matrix from OpenGL to parameter name in the shader. 
   * 
   * @param name String
   */          
  public void setProjectionMatrix(String name) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_PROJECTION_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
  }  

  /**
   * Copies the modelview-projection matrix from OpenGL to parameter name in the shader. 
   * 
   * @param name String
   */            
  public void setModelviewProjectionMatrix(String name) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
  }
  
  /**
   * Copies the texture matrix from OpenGL to parameter name in the shader. 
   * 
   * @param name String
   */  
  public void setTextureMatrix(String name) {
    CGparameter p = CgGL.cgGetNamedParameter(currProgram, name);
    if (p != null) CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_TEXTURE_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
  }  
  
  private void createVertexProgram(String source, String file) {  
    vertexProfile = CgGL.cgGLGetLatestProfile(CgGL.CG_GL_VERTEX);
    if (vertexProfile == CgGL.CG_PROFILE_UNKNOWN)
      System.err.println("GLCgShader:  Vertex profile could not be created");
    else
      CgGL.cgGLSetOptimalOptions(vertexProfile);
    
    vertexProgram = (CGprogram)GLState.createCGResource(CG_PROGRAM, context, source, vertexProfile);

    checkErrorInfo(file, vertexProgram);
    vertexShaderEnabled = (vertexProgram != null)
        && (vertexProfile != CgGL.CG_PROFILE_UNKNOWN);

    if (!vertexShaderEnabled) {
      System.err
          .println("GLCgShader: Failed to loaded vertex program: " + file);
    }
  }

  private void createGeometryProgram(String source, String file) {    
    // geometryProfile = CgGL.cgGLGetLatestProfile( CgGL.CG_GL_GEOMETRY );
    // if (geometryProfile == CgGL.CG_PROFILE_UNKNOWN)
    // println("Geometry profile could not be created");
    // else CgGL.cgGLSetOptimalOptions( geometryProfile );
    
    geometryProgram = (CGprogram)GLState.createCGResource(CG_PROGRAM, context, source, CgGL.CG_PROFILE_ARBFP1);     
     
    // String shaderSource = join(loadStrings(file), "\n");
    // ragmentProgram = CgGL.cgCreateProgram(context, CgGL.CG_SOURCE,
    // shaderSource, geometryProfile, null, null);
    checkErrorInfo(file, geometryProgram);
    geometryShaderEnabled = (geometryProgram != null)
        && (geometryProfile != CgGL.CG_PROFILE_UNKNOWN);

    if (!geometryShaderEnabled) {
      System.err.println("GLCgShader:  Failed to loaded geometry program: "
          + file);
    }  
  }

  private void createFragmentProgram(String source, String file) {    
    fragmentProfile = CgGL.cgGLGetLatestProfile(CgGL.CG_GL_FRAGMENT);
    if (fragmentProfile == CgGL.CG_PROFILE_UNKNOWN)
      System.err.println("GLCgShader:  Fragment profile could not be created");
    else
      CgGL.cgGLSetOptimalOptions(fragmentProfile);

    fragmentProgram = (CGprogram)GLState.createCGResource(CG_PROGRAM, context, source, fragmentProfile);
    
    checkErrorInfo(file, fragmentProgram);
    fragmentShaderEnabled = (fragmentProgram != null)
        && (fragmentProfile != CgGL.CG_PROFILE_UNKNOWN);

    if (!fragmentShaderEnabled) {
      System.err.println("GLCgShader:  Failed to loaded fragment program: "
          + file);
    }
  }  
  
  protected void release() {
    if (vertexShaderEnabled) {
      GLState.deleteCGResource(vertexProgram, CG_PROGRAM);
      vertexProgram = null;
    }
    if (fragmentShaderEnabled) {
      GLState.deleteCGResource(fragmentProgram, CG_PROGRAM);
      fragmentProgram = null;
    }
    if (geometryShaderEnabled) {
      GLState.deleteCGResource(geometryProgram, CG_PROGRAM);
      geometryProgram = null;
    }
    if (context != null) {
      GLState.deleteCGResource(context, CG_CONTEXT);
      context = null;
    }
  }
  
  private void checkErrorInfo(String fn, CGprogram program) {
    if (program == null) {
      int error = CgGL.cgGetError();
      System.err.println("GLCgShader:  Cg error(s) in " + fn);
      System.err.println("GLCgShader:  " + CgGL.cgGetErrorString(error));
      System.err.println("GLCgShader:  " + CgGL.cgGetLastListing(context));
    }
  }  
  
  protected void checkProfiles() {
    // VERTEX PROFILES
    if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_ARBVP1)) {
      vertexProfile = CgGL.CG_PROFILE_ARBVP1;
      System.out.println("CG_PROFILE_ARBVP1 supported");
    } else if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_VP40)) {
      vertexProfile = CgGL.CG_PROFILE_VP40;
      System.out.println("CG_PROFILE_VP40 supported");
    } else if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_VP30)) {
      vertexProfile = CgGL.CG_PROFILE_VP30;
      System.out.println("CG_PROFILE_VP30 supported");
    } else if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_VP20)) {
      vertexProfile = CgGL.CG_PROFILE_VP20;
      System.out.println("CG_PROFILE_VP20 supported");
    }

    // FRAGMENT PROFILES
    if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_ARBFP1)) {
      fragmentProfile = CgGL.CG_PROFILE_ARBFP1;
      System.out.println("CG_PROFILE_ARBFP1 supported");
    } else if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_FP40)) {
      fragmentProfile = CgGL.CG_PROFILE_FP40;
      System.out.println("CG_PROFILE_FP40 supported");
    } else if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_FP30)) {
      fragmentProfile = CgGL.CG_PROFILE_FP30;
      System.out.println("CG_PROFILE_FP30 supported");
    } else if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_FP20)) {
      fragmentProfile = CgGL.CG_PROFILE_FP20;
      System.out.println("CG_PROFILE_FP20 supported");
    }

    // GEOMETRY PROFILES
    // CgGL.cgGLGetLatestProfile( CgGL.CG_GL_GEOMETRY );
  }
  
  protected void replaceIncludes(String[] lines) {
    // Replacing preprocessor includes with the actual contents of the reference file,
    // otherwise the Cg compiler won't be able to load them.
    String line, fn, include;
    String[] inclines;
    for (int i = 0; i < lines.length; i++) {
      line = lines[i];
      int n = line.indexOf("#include");
      if (-1 < n) {
        fn = line.substring(n + 8, line.length());
        fn = fn.replace( '"' , ' ');
        fn = fn.trim();
        inclines = parent.loadStrings(fn);    
        include = PApplet.join(inclines, "\n");    
        lines[i] = include;
      }
    }    
  }
}
