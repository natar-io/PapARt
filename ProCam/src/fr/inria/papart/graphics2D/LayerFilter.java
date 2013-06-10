/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.graphics2D;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;

/**
 *
 * @author jiii
 */
public abstract class LayerFilter {

    public GLTextureFilter textureFilter;
    public GLTextureFilter textureFilterZone;
    public String name;
    
    public void applyFilter(GLTexture source, GLTexture destination) {
        // Full image filter
        textureFilter.apply(source, destination);
    }

    public void applyZoneFilter(GLTexture source, GLTexture zone, GLTexture destination) {
        GLTexture[] src = {source, zone};
        textureFilter.apply(src, destination);
    }
    
}
