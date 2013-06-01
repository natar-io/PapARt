/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.graphics2D;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class Layer {

    private PApplet parent;
    private int width, height;
    private PVector position = new PVector();
    private float rotation = 0;
    private ArrayList<FilterLayer> filters;
    private ArrayList<Layer> subLayers;
    private GLGraphicsOffScreen frameBuffer;

    public Layer(PApplet parent, GLGraphicsOffScreen screen) {
        this(parent, screen.getTexture());
    }

    public Layer(PApplet parent, GLTexture tex) {
        this(parent, (PImage) tex);
    }

    public Layer(PApplet parent, PImage image) {
        this(parent, image.width, image.height);
    }

    public Layer(PApplet parent, PVector size) {
        this(parent, (int) size.x, (int) size.y);
    }

    public Layer(PApplet parent, int width, int height) {
        this.width = width;
        this.height = height;
        init(parent);
    }

    private void init(PApplet parent) {
        this.parent = parent;
        frameBuffer = new GLGraphicsOffScreen(parent, width, height);
        filters = new ArrayList<FilterLayer>();
        subLayers = new ArrayList<Layer>();
    }

    public void addSubLayer(Layer l) {
        this.subLayers.add(l);
    }

    public void removeSubLayer(Layer l) {
        this.subLayers.remove(l);
    }

    public void removeSubLayers() {
        this.subLayers.clear();
    }

    public GLGraphicsOffScreen getBuffer() {
        return frameBuffer;
    }

    public void setPosition(PVector p) {
        this.position = p.get();
    }

    public void setRotation(float r) {
        this.rotation = r;
    }

    public void addFilter(Filter f) {
        FilterLayer fl = new FilterLayer(f);
        filters.add(fl);
    }

    public GLGraphicsOffScreen getFilterPartialArea(Filter filter) {

        for (FilterLayer f : filters) {
            if (f.filter == filter) {
                return f.getPartialFilter();
            }
        }
        return null;
    }

    public void drawSelf(GLGraphicsOffScreen g) {

        g.pushMatrix();

        g.translate(position.x, position.y);
        g.rotate(rotation);

        g.image(frameBuffer.getTexture(), width, height);

        for (Layer l : subLayers) {
            l.drawSelf(g);
        }

        for (FilterLayer f : filters) {
            f.applyFilter();
        }

        g.popMatrix();
    }

    class FilterLayer {

        protected Filter filter;
        protected GLGraphicsOffScreen partial = null;

        public FilterLayer(Filter f) {
            this.filter = f;
        }

        public GLGraphicsOffScreen getPartialFilter() {
            if (partial != null) {
                partial = new GLGraphicsOffScreen(parent, width, height);
            }
            return partial;
        }

        public void applyFilter() {
            if (partial == null) {
                filter.applyFilter(frameBuffer.getTexture(), frameBuffer.getTexture());
            } else {
                filter.applyZoneFilter(frameBuffer.getTexture(), partial.getTexture(), frameBuffer.getTexture());
            }
        }
    }
}
