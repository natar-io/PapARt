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
public class Filter {

    GLTextureFilter textureFilter;
    GLTextureFilter textureFilterZone;

    public void Filter(GLTextureFilter tf, GLTextureFilter tfZone) {
        this.textureFilter = tf;
        this.textureFilterZone = tfZone;
    }

    public void applyFilter(GLTexture source, GLTexture destination) {
        // Full image filter
        textureFilter.apply(source, destination);
    }

    public void applyZoneFilter(GLTexture source, GLTexture zone, GLTexture destination) {

        // Full image filter
        GLTexture[] src = new GLTexture[2];
        src[0] = source;
        src[1] = zone;
        textureFilter.apply(src, destination);

    }
}
