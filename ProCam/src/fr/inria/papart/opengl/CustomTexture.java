/**
 * **
 *
 * Code from GLTexture to add advanced features to Processing's Texture class.
 *
 *
 *
 */
package fr.inria.papart.opengl;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import processing.core.PConstants;
import processing.opengl.PGL;
import processing.opengl.Texture;

/**
 *
 * @author jiii
 */
public class CustomTexture extends Texture implements PConstants {

    private int texTarget;

    public CustomTexture(int width, int height) {
        super(width, height);
    }

    /*
     * Puts a Float buffer for float textures
     */
    public void putBuffer(float[] floatArray) {

        // Bind the texture in OpenGL
        pgl.bindTexture(glTarget, glName);

        // Load the data into openGL memory 
//        public void texImage2D(  int target, int level, int internalFormat, int width, int height, int border, int format, int type, Buffer data) {
//        public void texSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, Buffer data) {

        // Target : texture2D 
//        pgl.texImage2D(glTarget, 0, GL.GL_RGB, width, height, 0, GL.GL_RGB, GL.GL_FLOAT, FloatBuffer.wrap(floatArray));
        pgl.texSubImage2D(glTarget, 0, 0, 0, width, height, GL.GL_RGB, GL.GL_FLOAT, FloatBuffer.wrap(floatArray));

        // Unbind the texture ? 
        pgl.bindTexture(glTarget, 0);
    }

    public void putBuffer(int glFormat, int glType, IntBuffer buffer) {

        pgl.bindTexture(glTarget, glName);

//        pgl.texSubImage2D(texTarget, 0, 0, 0, width, height, glFormat, glType, buffer);
        pgl.texImage2D(texTarget, 0, 0, 0, width, height, glFormat, glType, buffer);

        pgl.bindTexture(glTarget, 0);
    }
}
