/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author jeremy
 */
public class GLTextureDepth extends GLTexture implements PConstants, GLConstants {

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
     * @param internalFormat int
     */
    public GLTextureDepth(PApplet parent, int width, int height, int internalFormat) {
        super(parent, width, height);
        this.parent = parent;

        pgl = (PGraphicsOpenGL) parent.g;
        gl = pgl.gl;
        glstate = new GLState(gl);
        setTextureParams(new GLTextureParameters(format), internalFormat);

        initTexture(width, height);
    }

    protected void setTextureParams(GLTextureParameters params, int interalFormat) {
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
        } else if (interalFormat == GL.GL_DEPTH_COMPONENT) {
            texInternalFormat = GL.GL_DEPTH_COMPONENT;
        }

//        texInternalFormat = interalFormat;


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
     * @invisible Creates the opengl texture object.
     * @param w int
     * @param h int
     */
    @Override
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
        if (texTarget == GL.GL_TEXTURE_1D) {
            gl.glTexImage1D(texTarget, 0, texInternalFormat, w, 0, GL.GL_DEPTH_COMPONENT,
                    GL.GL_UNSIGNED_BYTE, null);
        } else {
            if (texInternalFormat == GL.GL_DEPTH_COMPONENT) {

                // GL_LINEAR does not make sense for depth texture. However, next tutorial shows usage of GL_LINEAR and PCF. Using GL_NEAREST
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);

                // Remove artefact on the edges of the shadowmap
                gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
                gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
//
//                // This is to allow usage of shadow2DProj function in the shader
//                gl.glTexParameteri(texTarget, GL.GL_TEXTURE_COMPARE_MODE_ARB,
//                        GL.GL_COMPARE_R_TO_TEXTURE_ARB);
//                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_COMPARE_FUNC, GL.GL_LEQUAL);
//                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_DEPTH_TEXTURE_MODE, GL.GL_INTENSITY);

                gl.glTexImage2D(texTarget, 0, GL.GL_DEPTH_COMPONENT, w, h, 0, GL.GL_DEPTH_COMPONENT,
//                        GL.GL_UNSIGNED_BYTE, null);
                        GL.GL_UNSIGNED_INT, null);

                System.out.println("Init Texture " + w + "  " + h);

            } else {
                gl.glTexImage2D(texTarget, 0, texInternalFormat, w, h, 0, GL.GL_RGBA,
                        GL.GL_UNSIGNED_BYTE, null);
            }
        }
        gl.glBindTexture(texTarget, 0);

        if (texTarget == GL.GL_TEXTURE_RECTANGLE_ARB) {
            maxTexCoordS = w;
            maxTexCoordT = h;
        } else {
            maxTexCoordS = 1.0f;
            maxTexCoordT = 1.0f;
        }
    }

    @Override
    public void setTexUniform(int tu) {
    texUniform = tu;
  }

}
