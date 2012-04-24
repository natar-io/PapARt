/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package codeanticode.glgraphics;

import java.nio.DoubleBuffer;
import javax.media.opengl.GL;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

/**
 * DEPRECATED
 * @author jeremy
 */
public class GLShadowMap {

    GLFramebufferObject shadowFBO;
//    GLTextureDepth shadowTex;
    GLTextureDepth shadowTex;
    boolean init = false;
    PApplet parent;
    GLState glstate;
    PGraphicsOpenGL pgl;
    GL gl;
    protected float[] modelviewTM, projectionTM;
    PMatrix3D oldProj;
    public int texLocation;
    public int shadowLocation;
    float[] matF;

    public GLShadowMap(PApplet parent, GLSLShader shader) {
        this.parent = parent;
        pgl = (PGraphicsOpenGL) parent.g;
        matF = new float[16];

//        shadowTex = new GLTextureDepth(parent, 512, 512, GL.GL_RGB);
        shadowTex = new GLTextureDepth(parent, 512, 512, GL.GL_DEPTH_COMPONENT);

        pgl = (PGraphicsOpenGL) parent.g;
        gl = pgl.gl;

        init(gl);

        shader.start();
        shadowTex.setName("ShadowMap");
        texLocation = shader.getUniformLocation("tex");
        shadowLocation = shader.getUniformLocation("ShadowMap");
//shadowMapUniform = glGetUniformLocationARB(shadowShaderId,"ShadowMap");
        shadowTex.setTexUniform(shadowLocation);
        System.out.println("loc " + texLocation + " " + shadowLocation);
        shader.stop();

    }

//  http://www.opengl.org/wiki/Framebuffer_Object_Examples
    private void init(GL gl) {

        shadowFBO = new GLFramebufferObject(gl, false);
//        System.out.println("FBO created : " + shadowFBO.fbo);
        GLState glstate = new GLState(gl);
        glstate.pushFramebuffer();
        glstate.setFramebuffer(shadowFBO);

        if (shadowTex.texInternalFormat == GL.GL_DEPTH_COMPONENT) {
            gl.glDrawBuffer(GL.GL_NONE);
            gl.glReadBuffer(GL.GL_NONE);

            gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT,
                    GL.GL_TEXTURE_2D, shadowTex.getTextureID(), 0);
        } else {
            gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT,
                    GL.GL_TEXTURE_2D, shadowTex.getTextureID(), 0);
        }

        glstate.popFramebuffer();

        init = true;
    }

    public void beginDraw(GL gl, PVector camPos, PVector camDir, float fov) {
//        if (!init) {
//            init(gl);
//        }

//        this.gl = pgl.beginGL();
        this.gl = gl;

        glstate = new GLState(gl);
        glstate.pushFramebuffer();
        glstate.setFramebuffer(shadowFBO);

        gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
//        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
//
        gl.glColorMask(true, true, true, true);

        if (shadowTex.texInternalFormat == GL.GL_DEPTH_COMPONENT) {
            gl.glColorMask(false, false, false, false);
        } else {
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }
//        gl.glCullFace(GL.GL_FRONT);

        oldProj = pgl.projection.get();

//        System.out.println(parent.millis() * 0.1f);
        parent.perspective(fov, 1, 100, 500f);

        parent.camera(camPos.x, camPos.y, camPos.z,
                camDir.x, camDir.y, camDir.z,
                0, 1, 0);

//        pgl.projection.invert();
//        gl.glMatrixMode(GL.GL_PROJECTION);
//        gl.glLoadMatrixf(pgl.projection.get(matF), 0);
//        pgl.projection.invert();
////        // concatating all matrice into one.
////        gl.glMultMatrixf(pr.get(matF), 0);
////        gl.glMultMatrixf(mv.get(matF), 0);
//
//        pgl.camera(400, 200, 200,
//                0, 0, 0,
//                0, 1, 0);
//
//        pgl.perspective(fov, 1f, 5f, 2000f);//400f);
//        pgl.box(20);

        gl.glMatrixMode(GL.GL_TEXTURE);
//        gl.glActiveTexture(GL.GL_TEXTURE0 + 7);
        gl.glActiveTexture(GL.GL_TEXTURE7);

        PMatrix3D bias = new PMatrix3D(
                0.5f, 0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.0f, 0.5f,
                0.0f, 0.0f, 0.5f, 0.5f,
                0.0f, 0.0f, 0.0f, 1.0f);

        PMatrix3D mat = new PMatrix3D(bias);
        mat.apply(pgl.projection);
        mat.apply(pgl.modelview);
        mat.transpose();
        gl.glLoadIdentity();
        gl.glLoadMatrixf(mat.get(matF), 0);

        //////////////////////////////////////////////
        // other method,, everything computed by OpenGL

//        PMatrix3D mv = new PMatrix3D(pgl.modelview);
//        PMatrix3D pr = new PMatrix3D(pgl.projection);
//
//        bias.transpose();
//        mv.transpose();
//        pr.transpose();
//
//        gl.glLoadIdentity();
//        gl.glLoadMatrixf(bias.get(matF), 0);
//
//        // concatating all matrice into one.
//        gl.glMultMatrixf(pr.get(matF), 0);
//        gl.glMultMatrixf(mv.get(matF), 0);

        // concatating all matrice into one.
        gl.glMatrixMode(GL.GL_MODELVIEW);

    }

    public void endDraw() {

        gl.glColorMask(true, true, true, true);

        if (shadowTex.texInternalFormat == GL.GL_DEPTH_COMPONENT) {
            gl.glDrawBuffer(GL.GL_NONE);
            gl.glReadBuffer(GL.GL_NONE);
        }

        // reinit camera -> DIRTY WAY
        parent.camera((float) parent.width / 2.0f,
                (float) parent.height / 2.0f,
                (float) (parent.height / 2.0f) / PApplet.tan(PConstants.PI * 60.0f / 360.0f),
                (float) parent.width / 2.0f,
                (float) parent.height / 2.0f, 0f, 0f, 1f, 0f);

        pgl.projection.set(oldProj);
        glstate.popFramebuffer();

    }

    public GLTexture getTexture() {
        return shadowTex;
    }

    public int getTextureId() {
        return shadowTex.tex;
    }

    public void loadTexture(GLSLShader shader) {
//            gl.glUniform1iARB(0, 3);
//        gl.glActiveTexture(3);
//        shadowTex.bind(3);
//        shader.setTexUniform("ShadowMap", shadowTex);
//        shader.setTexUniform("ShadowMap", 3);

//        shadowLocation = shader.getUniformLocation("ShadowMap");
//        texLocation = shader.getUniformLocation("tex");
//                System.out.println("loc " + texLocation + " " + shadowLocation);

        int mapLoc = gl.glGetUniformLocationARB(shader.programObject, "ShadowMap");
        gl.glUniform1iARB(mapLoc, 7);

//        int texLoc = gl.glGetUniformLocationARB(shader.programObject, "tex");
//        gl.glUniform1iARB(texLoc, 3);
        gl.glUniform1iARB(texLocation, 3);

//        gl.glActiveTexture(GL.GL_TEXTURE7);
        gl.glActiveTexture(GL.GL_TEXTURE0 + 7);
        gl.glBindTexture(GL.GL_TEXTURE_2D, shadowTex.tex);

//        System.out.println("loc " + texLoc + " " + mapLoc);

//        GL.GL_TEXTURE0;
        //        shader.setTexUniform("ShadowMap", GL.GL_TEXTURE7);
        //        shader.setTexUniform("tex", GL.GL_TEXTURE0);
        //        shadowTex.bind(GL.GL_TEXTURE7);
        //        texLocation = shader.getUniformLocation("tex");
        //        shadowLocation = shader.getUniformLocation("ShadowMap");
        ////shadowMapUniform = glGetUniformLocationARB(shadowShaderId,"ShadowMap");
        //
        //        shadowTex.setTexUniform(shadowLocation);
        //        gl.glTex

    }

    public void loadTexture(GLSLShader shader, GLTexture tex) {
//        shader.setTexUniform("ShadowMap", tex);

        int mapLoc = gl.glGetUniformLocationARB(shader.programObject, "ShadowMap");
        gl.glUniform1iARB(mapLoc, 7);

        int texLoc = gl.glGetUniformLocationARB(shader.programObject, "tex");
        gl.glUniform1iARB(texLoc, 3);

//        gl.glActiveTexture(GL.GL_TEXTURE7);
        gl.glActiveTexture(GL.GL_TEXTURE0 + 7);
        gl.glBindTexture(GL.GL_TEXTURE_2D, tex.tex);
    }
}
