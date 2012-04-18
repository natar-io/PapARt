package codeanticode.glgraphics;

import processing.core.*;
import processing.opengl.*;
import javax.media.opengl.*;

import com.sun.opengl.util.BufferUtil;

/**
 * This class implements OpenGL renderer for off-screen depth rendering. The result of the
 * frame rendering is available as a texture that can be obtained by calling the
 * @getTexture method. Multisampling for antialiased rendering can be used depending
 * on the hardware capabilities.
 */
public class GLGraphicsOffscreenDepth extends GLGraphicsOffScreen implements GLConstants {

    GLTextureDepth depthTex;

    public GLGraphicsOffscreenDepth(PApplet parent, int iwidth, int iheight) {
        super(parent, iwidth, iheight);
        multisampleEnabled = false;
        colorTexParams = new GLTextureParameters();
        multisampleLevel = 4;
        setParent(parent);
        setSize(iwidth, iheight);
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
        } else {
            reapplySettings();
        }
    }

    @Override
    protected void initFramebuffer() {
        int stat;

        gl = context.getGL();

        glstate = new GLState(gl);

        // Creating arrays for FBO and depth&stencil buffer.
        FBO = new GLFramebufferObject(gl);

        // Create depth and stencil buffers.
        depthStencilBuffer = GLState.createGLResource(GL_RENDER_BUFFER);

        // Creating color texture.
        depthTex = new GLTextureDepth(parent, width, height, GL.GL_DEPTH_COMPONENT);


        // TODO: impossible
//        if (multisampleEnabled) {
//            multisampleFBO = new GLFramebufferObject(gl);
//
//            // Creating handle for multisample color buffer.
//            colorBufferMulti = GLState.createGLResource(GL_RENDER_BUFFER);
//
//            // Bind render buffer.
//            gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, colorBufferMulti);
//
//            gl.glRenderbufferStorageMultisampleEXT(GL.GL_RENDERBUFFER_EXT, multisampleLevel,
//                    GL.GL_RGBA8, width, height);
//
//            // Bind depth-stencil buffer.
//            gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);
//
//            // Allocating space for multisample depth buffer
//            gl.glRenderbufferStorageMultisampleEXT(GL.GL_RENDERBUFFER_EXT, multisampleLevel,
//                    GL_DEPTH24_STENCIL8, width, height);
//
//            // Creating handle for multisample FBO
//            glstate.pushFramebuffer();
//            glstate.setFramebuffer(multisampleFBO);
//
//            gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT,
//                    GL.GL_RENDERBUFFER_EXT, colorBufferMulti);
//            gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT,
//                    GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);
//            gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_STENCIL_ATTACHMENT_EXT,
//                    GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);
//
//            // Clearing all the bound buffers.
//            gl.glClearColor(0f, 0f, 0f, 0.0f);
//            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
//
//            glstate.pushFramebuffer();
//            glstate.setFramebuffer(FBO);
//
//            gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT,
//                    GL.GL_TEXTURE_2D, colorTex.getTextureID(), 0);
//
//            stat = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
//            GLUtils.printFramebufferError(stat);
//
//            // Clearing color buffer only.
//            gl.glClearColor(0f, 0f, 0f, 0.0f);
//            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
//
//            glstate.popFramebuffer();
//            glstate.popFramebuffer();
//        } else {
        // / Regular, no multisampling, FBO setup.

        gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);
        gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, GL_DEPTH24_STENCIL8, width, height);

        glstate.pushFramebuffer();
        glstate.setFramebuffer(FBO);

//        gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT,
//                colorTex.getTextureTarget(), colorTex.getTextureID(), 0);
        gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT,
                GL.GL_RENDERBUFFER_EXT, depthTex.getTextureID());
//        gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_STENCIL_ATTACHMENT_EXT,
//                GL.GL_RENDERBUFFER_EXT, depthStencilBuffer);

        FBO.checkFBO();

        // Clearing all the bound buffers.
        gl.glClearColor(0f, 0f, 0f, 0.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

        glstate.popFramebuffer();
//        }
    }

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
            eyeX = (float) w / 2.0f;
            eyeY = (float) h / 2.0f;
            halfFov = PI * screenFov / 360.0f;
            theTan = (float) Math.tan(halfFov);
            dist = eyeY / theTan;
            float nearDist = dist / 10.0f; // near / far clip plane
            float farDist = dist * 10.0f;
            aspect = (float) w / (float) h;

            gl.glLoadIdentity();
            perspective(screenFov * DEG_TO_RAD, aspect, nearDist, farDist);
        }

//        ((PGraphicsOpenGL)).beginDraw();

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
//        if (multisampleEnabled) {
//            glstate.pushFramebuffer();
//            glstate.setFramebuffer(multisampleFBO);
//            gl.glDrawBuffer(GL.GL_COLOR_ATTACHMENT0_EXT);
//        } else {
        glstate.pushFramebuffer();
        glstate.setFramebuffer(FBO);
// HERE
        gl.glDrawBuffer(GL.GL_NONE);
        gl.glReadBuffer(GL.GL_NONE);

        gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT,
                GL.GL_TEXTURE_2D, depthTex.getTextureID(), 0);
//            FBO.setDrawBuffer(colorTex);


        // Clearing Z-buffer to ensure that the new elements are drawn properly.
        gl.glClearColor(0f, 0f, 0f, 0.0f);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
        gl.glColorMask(false, false, false, false);
        gl.glCullFace(GL.GL_FRONT);

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

//        super.endDraw();
//        ((PGraphicsOpenGL) this).endDraw();

        // Restoring render to previous framebuffer.
//        if (multisampleEnabled) {
//            // In the case of multisampling, first copying the contents of the multisampled render buffer into the
//            // normal color buffer.
//            gl.glBindFramebufferEXT(GL.GL_READ_FRAMEBUFFER_EXT, multisampleFBO.getFramebufferID()); // Source
//            gl.glBindFramebufferEXT(GL.GL_DRAW_FRAMEBUFFER_EXT, FBO.getFramebufferID());            // Destination
//            gl.glBlitFramebufferEXT(0, 0, width, height, 0, 0, width, height, GL.GL_COLOR_BUFFER_BIT, GL.GL_NEAREST);
//            glstate.popFramebuffer();
//        } else {
        glstate.popFramebuffer();
//        }

        if (resize) {
            // Restoring previous viewport.
            gl.glPopAttrib();
        }

        gl.glCullFace(GL.GL_FRONT);
        gl.glColorMask(true, true, true, true);
        // Restoring previous GL matrices.
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();

        // System.out.println("  End Draw");
    }

    public GLTexture getTexture() {
        return depthTex;
    }
}
