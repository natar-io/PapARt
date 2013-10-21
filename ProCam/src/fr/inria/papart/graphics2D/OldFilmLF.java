/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.graphics2D;

import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import java.util.Random;
import processing.core.PApplet;

/**
 *
 * @author jiii
 */
public class OldFilmLF extends LayerFilter {

    private PApplet parent;
    
    public OldFilmLF(PApplet parent, String path) {
        this.textureFilter = new GLTextureFilter(parent, path + "oldFilm.xml");
        this.textureFilterZone = new GLTextureFilter(parent, path + "oldFilmZone.xml");
        this.name = "OldFilm";
        this.parent = parent;
    }

    @Override
    public void applyFilter(GLTexture source, GLTexture destination) {
        // Full image filter

//        float[] wh = new float[2];
//        wh[0] = 1f / source.width;
//        wh[1] = 1f / source.height;
//        textureFilter.setParameterValue("wh", wh);

        
        textureFilter.apply(source, destination);
    }

    @Override
    public void applyZoneFilter(GLTexture source, GLTexture zone, GLTexture destination) {
        GLTexture[] src = {source, zone};
        
//        float[] wh = new float[2];
//        wh[0] = 1f / source.width;
//        wh[1] = 1f / source.height;
//        textureFilterZone.setParameterValue("wh", wh);
        
//        uniform float SepiaValue;
//uniform float NoiseValue;
//uniform float ScratchValue;
///* uniform float InnerVignetting; */
///* uniform float OuterVignetting; */
//uniform float RandomValue;
//uniform float TimeLapse;
        
        
        textureFilterZone.setParameterValue("RandomValue", parent.random(0, 1));
        textureFilterZone.setParameterValue("TimeLapse", (parent.millis() % 1000) / 1000f );
        
        textureFilterZone.apply(src, destination);
    }
}
