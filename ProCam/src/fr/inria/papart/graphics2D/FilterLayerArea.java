/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.graphics2D;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import processing.core.PApplet;

/**
 *
 * @author jiii
 */
public class FilterLayerArea {

    protected LayerFilter filter;
    protected Layer layer;
    protected GLGraphicsOffScreen mask = null;
    protected GLTexture output;
    private PApplet parent;

    public FilterLayerArea(PApplet parent, Layer l, LayerFilter f) {
        this.filter = f;
        this.layer = l;
        this.parent = parent;
        output = new GLTexture(parent, l.width, l.height);
        mask = new GLGraphicsOffScreen(parent, layer.width, layer.height);
    }
    
    public void clear(boolean b){
        mask.beginDraw();
        mask.background( b? 255 : 0, 0, 0,0);
//         mask.background(255, 0, 0);
        mask.endDraw();
    }

    public GLGraphicsOffScreen getPartialFilter() {
        return mask;
    }
    
    public GLTexture applyFilter(GLTexture currentTex) {
        if (mask == null) {
            filter.applyFilter(currentTex, output);
        } else {
            filter.applyZoneFilter(currentTex, mask.getTexture(), output);
        }

        return output;
    }
}
