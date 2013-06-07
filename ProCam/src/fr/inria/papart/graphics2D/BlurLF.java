/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.graphics2D;

import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import processing.core.PApplet;

/**
 *
 * @author jiii
 */
public class BlurLF extends LayerFilter {

    public BlurLF(PApplet parent, String path) {
        this.textureFilter = new GLTextureFilter(parent, path + "blur.xml");
        this.textureFilterZone = new GLTextureFilter(parent, path + "blurZone.xml");
    }

    @Override
    public void applyFilter(GLTexture source, GLTexture destination) {
        // Full image filter

        float[] wh = new float[2];
        wh[0] = 1f / source.width;
        wh[1] = 1f / source.height;
        textureFilter.setParameterValue("wh", wh);

        textureFilter.apply(source, destination);
    }

    @Override
    public void applyZoneFilter(GLTexture source, GLTexture zone, GLTexture destination) {
        GLTexture[] src = {source, zone};
        
        float[] wh = new float[2];
        wh[0] = 1f / source.width;
        wh[1] = 1f / source.height;
        textureFilterZone.setParameterValue("wh", wh);
        
        textureFilterZone.apply(src, destination);
    }
}
