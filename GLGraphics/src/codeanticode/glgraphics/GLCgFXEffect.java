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

import javax.media.opengl.GL;

import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
//import javax.media.opengl.GLException;
import com.sun.opengl.cg.*;

/**
 * This class encapsulates a CgFX shader effect. Based in the code by Victor Martins
 * (http://www.pixelnerve.com/v/)
 */
public class GLCgFXEffect implements GLConstants {
  protected PApplet parent;
  protected CGcontext context;
  protected CGeffect effect;
  
  protected CGtechnique currTechnique;
  protected CGpass currPass;
  protected GL gl;

 /**
   * Creates an instance of GLCgFXEffect, reading the effect from file.
   * 
   * @param parent
   *          PApplet
   * @param fn
   *          String
   */  
  public GLCgFXEffect(PApplet parent, String fn) {
    this.parent = parent;
    PGraphicsOpenGL pgl = (PGraphicsOpenGL) parent.g;
    gl = pgl.gl;
    
    context = (CGcontext)GLState.createCGResource(CG_CONTEXT);
    checkCgError();
    CgGL.cgGLRegisterStates(context);
    CgGL.cgGLSetManageTextureParameters(context, true);
    load(fn);
  }

  /**
   * Releases the OpenGL resources associated to this effect.
   */  
  public void delete() {
    release();
  }    
  
  /**
   * Returns the internal CGeffect object that encapsulates 
   * the CgFX effect.
   * 
   * @return CGeffect
   */       
  public CGeffect getEffect() {
    return effect;
  }  
  
  /**
   * Starts effect execution.
   */     
  public void start() {
  }

  /**
   * Stops effect execution.
   */     
  public void stop() {
  }

  /**
   * Sets for use the first technique contained in the effect.
   */   
  public void setFirstTechnique() {
    currTechnique = CgGL.cgGetFirstTechnique(effect);
    if (currTechnique == null) {
      System.err.println("GLCgFXEffect:  technique is null");
    }
  }

  /**
   * Sets for use the technique contained in the effect with the specified name.
   * 
   * @param name String 
   */     
  public void setTechnique(String name) {
    currTechnique = CgGL.cgGetNamedTechnique(effect, name);
    if (currTechnique == null) {
      System.err.println("GLCgFXEffect:  technique '" + name + "' is null");
    }
  }

  /**
   * Sets for use the currently selected pass in the current technique.
   */     
  public void setSelectedPass() {
    if (currPass != null) {
      CgGL.cgSetPassState(currPass);  
    }
  }
  
  /**
   * Resets the currently selected pass in the current technique.
   */     
  public void resetSelectedPass() {
    CgGL.cgResetPassState(currPass);
  }
    
  /**
   * Selects the first pass in the current technique.
   * 
   * @return boolean true or false depending if it was possible to select the first pass of the current technique 
   */      
  public boolean selectFirstPass() {
    currPass = CgGL.cgGetFirstPass(currTechnique);
    return currPass != null;    
  }

  /**
   * Selects the next pass in the current technique. Returns true if there is a pass
   * to select.
   * 
   * @return boolean 
   */        
  public boolean selectNextPass() {
    currPass = CgGL.cgGetNextPass(currPass);
    return currPass != null;
  }
    
  /**
   * Set the given texture for the parameter param.
   * 
   * @parameter param String 
   * @parameter tex GLTexture
   * 
   */        
  public void setTexParameter(String param, GLTexture tex) {
    int id = tex.getTextureID();
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if( p != null ) CgGL.cgGLSetTextureParameter(p, id);
    else System.err.println( "GLCgFXEffect:  Cant find texture parameter" );
    CgGL.cgSetSamplerState(p);
  }

  /**
   * Sets the int parameter with name to the given value 
   * 
   * @param name String
   * @param x int
   */    
  public void setIntParameter(String param, int x) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if (p != null)
      CgGL.cgSetParameter1i(p, x);
    else
      System.err.println("GLCgFXEffect:  param1f is null");
  }  
    
  /**
   * Sets the float parameter with name to the given value 
   * 
   * @param name String
   * @param x float
   */  
  public void setFloatParameter(String param, float x) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if (p != null)
      CgGL.cgSetParameter1f(p, x);
    else
      System.err.println("GLCgFXEffect:  param1f is null");
  }
  
  /**
   * Sets the vec2 parameter with name to the given values
   * 
   * @param name String
   * @param x float
   * @param y float          
   */     
  public void setVecParameter(String param, float x, float y) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if (p != null)
      CgGL.cgSetParameter2f(p, x, y);
    else
      System.err.println("GLCgFXEffect:  param2f is null");
  }
    
  /**
   * Sets the vec3 parameter with name to the given values
   * 
   * @param name String
   * @param x float
   * @param y float
   * @param z float    
   */    
  public void setVecParameter(String param, float x, float y, float z) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if (p != null)
      CgGL.cgSetParameter3f(p, x, y, z);
    else
      System.err.println("GLCgFXEffect:  param3f is null");
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
  public void setVecParameter(String param, float x, float y, float z, float w) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if (p != null)
      CgGL.cgSetParameter4f(p, x, y, z, w);
    else
      System.err.println("GLCgFXEffect:  param4f is null");
  }

  /**
   * Sets the mat2 parameter with name to the given values
   * 
   * @param name String
   * @param m00 float
   *        ...
   */  
  public void setMatParameter(String param, float m00, float m01,
                                            float m10, float m11) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if (p != null) {
      float[] mat = new float[4];
      mat[0] = m00;
      mat[1] = m10;
      mat[4] = m01;
      mat[5] = m11;         
      CgGL.cgGLSetMatrixParameterfr(p, mat, 0);
    } else
      System.err.println("GLCgFXEffect:  matrix2x2f param is null");
  }  

 /**
   * Sets the mat3 parameter with name to the given value 
   * 
   * @param name String
   * @param m00 float
   *        ...
   */    
  public void setMatParameter(String param, float m00, float m01, float m02,
                                            float m10, float m11, float m12,
                                            float m20, float m21, float m22) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
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
      CgGL.cgGLSetMatrixParameterfr(p, mat, 0);
    } else
      System.err.println("GLCgFXEffect:  matrix3x3f param is null");
  }  

  /**
   * Sets the mat4 parameter with name to the given value 
   * 
   * @param name String
   * @param m00 float
   *        ...
   */       
  public void setMatParameter(String param, float m00, float m01, float m02, float m03,
                                            float m10, float m11, float m12, float m13,
                                            float m20, float m21, float m22, float m23,
                                            float m30, float m31, float m32, float m33) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
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
      CgGL.cgGLSetMatrixParameterfr(p, mat, 0);
    } else
      System.err.println("GLCgFXEffect:  matrix4x4f param is null");
  }  

  /**
   * Sets the parameter param with the current contents of the OpenGL modelview matrix. 
   * 
   * @param param String  
   */
  public void setModelviewMatrix(String param) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if (p != null) CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
    else  System.err.println( "GLCgFXEffect:  cgGLSetStateMatrixParameter semantic param is null" );
  }

  /**
   * Sets the parameter param with the current contents of the OpenGL projection matrix. 
   * 
   * @param param String  
   */  
  public void setProjectionMatrix(String param) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if (p != null) CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_PROJECTION_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
    else  System.err.println( "GLCgFXEffect:  cgGLSetStateMatrixParameter semantic param is null" );
  }  
  
  /**
   * Sets the parameter param with the current contents of the OpenGL modelview-projection matrix. 
   * 
   * @param param String  
   */  
  public void setModelviewProjectionMatrix(String param) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if (p != null) CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
    else  System.err.println( "GLCgFXEffect:  cgGLSetStateMatrixParameter semantic param is null" );
  }
  
  /**
   * Sets the parameter param with the current contents of the OpenGL texture matrix. 
   * 
   * @param param String  
   */    
  public void setTexMatrix(String param) {
    CGparameter p = CgGL.cgGetNamedEffectParameter(effect, param);
    if (p != null) CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_TEXTURE_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
    else  System.err.println( "GLCgFXEffect:  cgGLSetStateMatrixParameter semantic param is null" );
  }  

  /**
   * Sets the matrix parameter identified by semantic, and adding no post-transformation
   * to the matrix. 
   * 
   * @param semantic name string of the semantic attached to the parameter
   * @param matrix int
   */  
  public void setMatrixParameterBySemantic(String semantic, int matrix) {
    setMatrixParameterBySemantic(semantic, matrix, IDENTITY_MATRIX);
  }  
  
  /**
   * Sets the parameter with the given semantic with the current contents of the specified matrix,
   * applying a certain transformation. 
   * gltransform should be one of (IDENTITY_MATRIX, INVERSE_MATRIX, TRANSPOSE_MATRIX or 
   * INVERSE_TRANSPOSE_MATRIX. It is a transformation post-applied to the specified matrix.
   * 
   * @param semantic String
   * @param matrix int
   * @param gltransform int            
   */      
  public void setMatrixParameterBySemantic(String semantic, int matrix, int gltransform) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgGLSetStateMatrixParameter(p, matrix, getCgMatrixTransform(gltransform));
    else
      System.err
          .println("GLCgFXEffect:  cgGLSetStateMatrixParameter(mat, type) is null");
  }

  /**
   * Passes OpenGL's modelview matrix to the Cg parameter identified by semantic, 
   * and adding no post-transformation to the matrix. 
   * 
   * @param semantic name string of the semantic attached to the parameter
   */    
  public void setModelviewMatrixBySemantic(String semantic) {
    setModelviewMatrixBySemantic(semantic, IDENTITY_MATRIX);
  }  

  /**
   * Sets the parameter with the given semantic with the current contents of the modelview matrix,
   * applying a certain transformation. 
   * gltransform should be one of (IDENTITY_MATRIX, INVERSE_MATRIX, TRANSPOSE_MATRIX or 
   * INVERSE_TRANSPOSE_MATRIX. It is a transformation post-applied to the specified matrix.
   * 
   * @param semantic String
   * @param gltransform int            
   *
   */
  public void setModelviewMatrixBySemantic(String semantic, int gltransform) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_MODELVIEW_MATRIX, getCgMatrixTransform(gltransform));
    else
      System.err
          .println("GLCgFXEffect:  cgGLSetStateMatrixParameter(mat, type) is null");
  }

  /**
   * Passes OpenGL's projection matrix to the Cg parameter identified by semantic, 
   * and adding no post-transformation to the matrix. 
   * 
   * @param semantic name string of the semantic attached to the parameter
   */      
  public void setProjectionMatrixBySemantic(String semantic) {
    setProjectionMatrixBySemantic(semantic, IDENTITY_MATRIX);
  }
  
  /**
   * Sets the parameter with the given semantic with the current contents of the projection matrix,
   * applying a certain transformation. 
   * gltransform should be one of (IDENTITY_MATRIX, INVERSE_MATRIX, TRANSPOSE_MATRIX or 
   * INVERSE_TRANSPOSE_MATRIX. It is a transformation post-applied to the specified matrix.
   * 
   * @param semantic String
   * @param gltransform int
   */
  public void setProjectionMatrixBySemantic(String semantic, int gltransform) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_PROJECTION_MATRIX, getCgMatrixTransform(gltransform));
    else
      System.err
          .println("GLCgFXEffect:  cgGLSetStateMatrixParameter(mat, type) is null");
  }

  /**
   * Passes OpenGL's modelview-projection matrix to the Cg parameter identified by semantic, 
   * and adding no post-transformation to the matrix. 
   * 
   * @param semantic name string of the semantic attached to the parameter
   */        
  public void setModelviewProjectionMatrixBySemantic(String semantic) {
    setModelviewProjectionMatrixBySemantic(semantic, IDENTITY_MATRIX);
  }
  
  /**
   * Sets the parameter with the given semantic with the current contents of the modelview-projection matrix,
   * applying a certain transformation. 
   * gltransform should be one of (IDENTITY_MATRIX, INVERSE_MATRIX, TRANSPOSE_MATRIX or 
   * INVERSE_TRANSPOSE_MATRIX. It is a transformation post-applied to the specified matrix.
   * 
   * @param semantic String
   * @param gltransform int
   */
  public void setModelviewProjectionMatrixBySemantic(String semantic, int gltransform) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX, getCgMatrixTransform(gltransform));
    else
      System.err
          .println("GLCgFXEffect:  cgGLSetStateMatrixParameter(mat, type) is null");
  }  

  /**
   * Passes OpenGL's texture matrix to the Cg parameter identified by semantic, 
   * and adding no post-transformation to the matrix. 
   * 
   * @param semantic name string of the semantic attached to the parameter
   */          
  public void setTextureMatrixBySemantic(String semantic) {
    setTextureMatrixBySemantic(semantic, IDENTITY_MATRIX);
  }
  
  /**
   * Sets the parameter with the given semantic with the current contents of the texture matrix,
   * applying a certain transformation. 
   * gltransform should be one of (IDENTITY_MATRIX, INVERSE_MATRIX, TRANSPOSE_MATRIX or 
   * INVERSE_TRANSPOSE_MATRIX. It is a transformation post-applied to the specified matrix.
   * 
   * @param semantic String
   * @param gltransform int
   */
  public void setTextureMatrixBySemantic(String semantic, int gltransform) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgGLSetStateMatrixParameter(p, CgGL.CG_GL_TEXTURE_MATRIX, getCgMatrixTransform(gltransform));
    else
      System.err
          .println("GLCgFXEffect:  cgGLSetStateMatrixParameter(mat, type) is null");
  }
    
  /**
   * Sets the parameter with indicated semantic  to the given texture
   * 
   * @param semantic String
   * @param tex GLTexture
   */    
  public void setTexParameterBySemantic(String semantic, GLTexture tex) {
    int id = tex.getTextureID();
      CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
      if( p != null ) CgGL.cgGLSetTextureParameter( p, id);
      else System.err.println( "GLCgFXEffect:  Cant find texture semantic" );
      CgGL.cgSetSamplerState( p );    
  }

  /**
   * Sets the int parameter with indicated semantic  to the given value
   * 
   * @param semantic String
   * @param x int
   */      
  public void setIntParameterBySemantic(String semantic, int x) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgSetParameter1i(p, x);
    else
      System.err.println("GLCgFXEffect:  1f semantic is null");
  }    
  
  /**
   * Sets the float parameter with indicated semantic  to the given value
   * 
   * @param semantic String
   * @param x float
   */        
  public void setFloatParameterBySemantic(String semantic, float x) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgSetParameter1f(p, x);
    else
      System.err.println("GLCgFXEffect: 1f  semantic is null");
  }
  
  /**
   * Sets the vec2 parameter with indicated semantic  to the given values
   * 
   * @param semantic String
   * @param x float
   * @param y float          
   */   
  public void setVecParameterBySemantic(String semantic, float x, float y) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgSetParameter2f(p, x, y);
    else
      System.err.println("GLCgFXEffect:  2f semantic is null");
  }
    
  /**
   * Sets the vec3 parameter with indicated semantic  to the given values
   * 
   * @param semantic String
   * @param x float
   * @param y float
   * @param z float                         
   */     
  public void setVecParameterBySemantic(String semantic, float x, float y, float z) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgSetParameter3f(p, x, y, z);
    else
      System.err.println("GLCgFXEffect:  3f semantic is null");
  }
  
  /**
   * Sets the vec4 parameter with indicated semantic  to the given values
   * 
   * @param semantic String
   * @param x float
   * @param y float
   * @param z float  
   * @param w float                                                   
   */      
  public void setVecParameterBySemantic(String semantic, float x, float y, float z, float w) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgSetParameter4f(p, x, y, z, w);
    else
      System.err.println("GLCgFXEffect:  4f semantic is null");
  }
  
  /**
   * Sets the mat2 parameter with indicated semantic  to the given values
   * 
   * @param semantic String
   * @param m00 float
   *        ...                                        
   */    
  public void setMatParameterBySemantic(String semantic, float m00, float m01,
                                                         float m10, float m11) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null) {
      float[] mat = new float[4];
      mat[0] = m00;
      mat[1] = m10;
      mat[4] = m01;
      mat[5] = m11;       
      CgGL.cgGLSetMatrixParameterfr(p, mat, 0);
    } else
      System.err.println("GLCgFXEffect:  matrix2x2f semantic is null");
  }  

  /**
   * Sets the mat3 parameter with indicated semantic  to the given values
   * 
   * @param semantic String
   * @param m00 float
   *        ...                                        
   */   
  public void setMatParameterBySemantic(String semantic, float m00, float m01, float m02,
                                                         float m10, float m11, float m12,
                                                         float m20, float m21, float m22) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
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
      CgGL.cgGLSetMatrixParameterfr(p, mat, 0);
    } else
      System.err.println("GLCgFXEffect:  matrix3x3f semantic is null");
  }  

  /**
   * Sets the mat4 parameter with indicated semantic  to the given values
   * 
   * @param semantic String
   * @param m00 float
   *        ...                                        
   */   
  public void setMatParameterBySemantic(String semantic, float m00, float m01, float m02, float m03,
                                                         float m10, float m11, float m12, float m13,
                                                         float m20, float m21, float m22, float m23,
                                                         float m30, float m31, float m32, float m33) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
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
      CgGL.cgGLSetMatrixParameterfr(p, mat, 0);
    } else
      System.err.println("GLCgFXEffect:  matrix4x4f semantic is null");
  }
  
  /**
   * Sets the matrix parameter with indicated semantic  to the given values
   * 
   * @param semantic String
   * @param mat float[]                                   
   */   
  public void setMatrixParameterBySemantic(String semantic, float[] mat) {
    CGparameter p = CgGL.cgGetEffectParameterBySemantic(effect, semantic);
    if (p != null)
      CgGL.cgGLSetMatrixParameterfr(p, mat, 0);
    else
      System.err.println("GLCgFXEffect:  cgGLSetMatrixParameterfr(v[]) is null");
  }   
  
  public int getCgMatrixTransform(int glcode) {
    if (glcode == IDENTITY_MATRIX )
      return CgGL.CG_GL_MATRIX_IDENTITY;
    if (glcode == INVERSE_TRANSPOSE_MATRIX )
      return CgGL.CG_GL_MATRIX_INVERSE_TRANSPOSE;
    if (glcode == INVERSE_MATRIX )
      return CgGL.CG_GL_MATRIX_INVERSE;
    if (glcode == TRANSPOSE_MATRIX )
      return CgGL.CG_GL_MATRIX_TRANSPOSE;
    return -1;
  }
  
  protected void load(String file) {
    int error = 0;

    String[] lines = parent.loadStrings(file);
    replaceIncludes(lines);
    String source = PApplet.join(lines, "\n");
    
    effect = (CGeffect)GLState.createCGResource(CG_EFFECT, context, source);
    
    error = CgGL.cgGetError();
    //System.out.println("GLCgFXEffect:  " + CgGL.cgGetErrorString(error));
    //System.out.println("GLCgFXEffect:  " + CgGL.cgGetLastListing(context));
    if (effect == null) {
      System.err.println("GLCgFXEffect:  Effect file '" + file
          + "' not loaded correctly");
      error = CgGL.cgGetError();
      System.err.println("GLCgFXEffect:  Error String: "
          + CgGL.cgGetErrorString(error));
      System.err.println("GLCgFXEffect:  Last Listing: "
          + CgGL.cgGetLastListing(context));
    }
  }
  
  protected void release() {
    if (effect != null) {
      GLState.deleteCGResource(effect, CG_EFFECT);      
      effect = null;
    }
    if (context != null) {
      GLState.deleteCGResource(context, CG_CONTEXT);
      context = null;
    }
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
  
  private void checkCgError() {
    int err = CgGL.cgGetError();

    if (err != CgGL.CG_NO_ERROR) {
      throw new RuntimeException("CG error: " + CgGL.cgGetErrorString(err));
    }
  }  
}
