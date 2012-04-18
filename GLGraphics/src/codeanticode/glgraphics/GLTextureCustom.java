package codeanticode.glgraphics;

import codeanticode.glgraphics.GLTexture;
import javax.media.opengl.GL;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;

public class GLTextureCustom extends GLTexture {

    public GLTextureCustom(PApplet parent, String filename) {
        super(parent, filename);
    }

    public void setTextureUnit(int unit){
        this.texUnit = unit;
    }

    //  protected void bind(int tu) {
    public void bind(int texUniform, int tu) {
//        texUnit = tu;
        gl.glActiveTexture(GL.GL_TEXTURE0 + texUnit);
        gl.glBindTexture(texTarget, tex);
//        if (-1 < texUniform) {
            gl.glUniform1iARB(texUniform, tu);
//        }
    }

    public void unbind() {
        if (-1 < texUnit) {
            gl.glActiveTexture(GL.GL_TEXTURE0 + texUnit);
            gl.glBindTexture(texTarget, 0);
            texUnit = -1;
            if (-1 < texUniform) {
                texUniform = -1;
            }
        }
    }

//  protected void setTexUniform(int tu) {
    public void setTexUniform(int tu) {
        texUniform = tu;
    }
}
