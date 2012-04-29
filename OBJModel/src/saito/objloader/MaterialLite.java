package saito.objloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.nio.*;

import javax.media.opengl.GL;

import processing.core.*;

/**
 *
 * @author jeremy
 */
public class MaterialLite {

    // public PImage map_Ka;
    public PImage map_Kd;
    private boolean hasTexture;
//	public GLTexture map_Kd_GL;
    // private Texture map_Kd_Gl;
    public float[] Ka;
    public float[] Kd;
    public float[] Ks;
    public float d;
    protected int[] tex;
    public String mtlName;

    public MaterialLite(Material material) {
        this.Ka = material.Ka;
        this.Kd = material.Kd;
        this.Ks = material.Ks;
        this.d = material.d;
        this.tex = material.tex;
        this.mtlName = material.mtlName;
        this.hasTexture = material.map_Kd != null;
    }

    public void beginDrawGL(GL gl, boolean useMaterial, boolean useTexture) {
        if (useMaterial) {
            gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, Ka, 0);

            if (Kd[3] == 1) {
                gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, Kd, 0);
            } else {
                gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, Kd, 0);
                gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, Ks, 0);
            }

            if (hasTexture) {
                gl.glBindTexture(GL.GL_TEXTURE_2D, tex[0]);
                gl.glEnable(GL.GL_TEXTURE_2D);
            }
        }
    }

    public void endDrawGL(GL gl, boolean useMaterial, boolean useTexture) {
        if (useMaterial) {
            if (hasTexture) {
                gl.glDisable(GL.GL_TEXTURE_2D);
            }
        }
    }
}
