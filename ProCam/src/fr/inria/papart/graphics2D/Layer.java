/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.graphics2D;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import fr.inria.papart.drawingapp.DrawUtils;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix2D;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class Layer {

    private PApplet parent;
    protected int width, height;
    private ArrayList<FilterLayerArea> filters;
    private ArrayList<Layer> subLayers;
    private GLGraphicsOffScreen frameBuffer;
    private PVector drawSize, position;
    private float rotation = 0;
    private float scale = 1;
    private Layer parentLayer = null;
    private PMatrix2D transformation = new PMatrix2D();

    public Layer(PApplet parent, PImage image, PVector position, PVector drawSize) {
        this(parent, image.width, image.height, position, drawSize);
    }

    public Layer(PApplet parent, PVector size, PVector position, PVector drawSize) {
        this(parent, (int) size.x, (int) size.y, position, drawSize);
    }

    public Layer(PApplet parent, int width, int height, PVector position, PVector drawSize) {
        this.width = width;
        this.height = height;
        this.position = position.get();
        this.drawSize = drawSize.get();

        System.out.println("New Laye " + width + "  " + height + " " + drawSize);

//        this.transformation.translate(position.x, position.y);

        init(parent);
    }

    private void init(PApplet parent) {
        this.parent = parent;
        frameBuffer = new GLGraphicsOffScreen(parent, width, height);
        filters = new ArrayList<FilterLayerArea>();
        subLayers = new ArrayList<Layer>();
    }

    public Layer getParent() {
        return this.parentLayer;
    }

    public void addSubLayer(Layer l) {
        l.setParentLayer(this);
        this.subLayers.add(l);
    }

    public ArrayList<Layer> getChildren() {
        return this.subLayers;
    }

    protected void setParentLayer(Layer l) {
        this.parentLayer = l;
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

    public void putImage(PImage img) {
        frameBuffer.beginDraw();
        frameBuffer.imageMode(PApplet.CENTER);
        DrawUtils.drawImage(frameBuffer, img, width / 2, height / 2, width, height);
//        frameBuffer.image(img, 0, 0, width, height);
        frameBuffer.endDraw();
    }

    public void translateBy(PVector p) {
//        this.transformation.translate(p.x, p.y);
        this.position.add(p);
    }

    public void setPosition(PVector p) {
        this.transformation.m02 = p.x;
        this.transformation.m12 = p.y;

        this.position.x = p.x;
        this.position.y = p.y;
    }

    public void setDrawSize(PVector s) {
        this.drawSize = s.get();
    }

    public PVector getDrawSize(PVector s) {
        return this.drawSize;
    }

//    public void setRotation(float r) {
//        this.transformation.rotate(r);
//        this.rotation = r;
//    }
    public void rotateBy(float r) {
        this.transformation.rotate(r);
        this.rotation += r;
    }

    public void scaleBy(float r) {
        this.scale *= r;
//        this.drawSize.mult(r);
        this.transformation.scale(r);
    }

    public PVector displayToImage(PVector pos) {
        PVector out = pos.get();

        // get the good resolution
        out.x = out.x / drawSize.x * width;
        out.y = out.y / drawSize.y * height;
        return out;
    }

    public PVector project(PVector pos) {
        PVector out = new PVector();
        PVector p = pos.get();

        if (this.parentLayer != null) {

//            System.out.println("rec " + p);
            p = parentLayer.projectRec(p);
//            System.out.println("rec end " + p);
            // TODO: check all this...
//            p = parentLayer.project(p);
        }

        p.sub(this.position.x, this.position.y, 0);

        this.transformation.invert();
        this.transformation.mult(p, out);
        this.transformation.invert();

//        System.out.println("Before shift " + out);
        out.add(drawSize.x / 2f,
                drawSize.y / 2f, 0);
        return out;
    }

    public boolean contains(PVector pos) {
        PVector p = this.project(pos);
        return p.x < this.drawSize.x && p.x > 0
                && p.y < this.drawSize.y && p.y > 0;
    }

    // TODO: finish this... 
    public PVector projectRec(PVector pos) {
        PVector out = new PVector();
        PVector p = pos.get();

//        System.out.println("Project Rec pos " + this.position);

        if (this.parentLayer != null) {

//            System.out.println("Rec Before " + p);
            p = parentLayer.projectRec(p);
//            System.out.println("Rec after " + p);
        }
        p.sub(this.position.x, this.position.y, 0);

        this.transformation.invert();
        this.transformation.mult(p, out);
        this.transformation.invert();

        return out;
    }

    public PMatrix2D getTransformation() {
        return this.transformation;
    }

    public FilterLayerArea addFilter(PApplet parent, LayerFilter f) {
        FilterLayerArea fl = new FilterLayerArea(parent, this, f);
        filters.add(fl);
        return fl;
    }

    public void removeFilter(PApplet parent, LayerFilter lf) {
        FilterLayerArea toRemove = null;

        for (FilterLayerArea fla : filters) {
            if (fla.filter == lf) {
                toRemove = fla;
            }
        }

        if (toRemove != null) {
            filters.remove(toRemove);
        }
    }

    public void removeFilter(PApplet parent, FilterLayerArea lf) {
        filters.remove(lf);
    }

    public void removeAllFilters() {
        filters.clear();
    }

    public GLGraphicsOffScreen getFilterPartialArea(LayerFilter filter) {

        for (FilterLayerArea f : filters) {
            if (f.filter == filter) {
                return f.getPartialFilter();
            }
        }
        return null;
    }
    private boolean drawBorders = false;
    private int drawBordersTime = 0;
    private int drawBordersDuration = 0;

    public void setDrawBorders(boolean drawBorders, int duration) {
        this.drawBorders = drawBorders;
        this.drawBordersDuration = duration;
        this.drawBordersTime = parent.millis();

    }

    public void drawSelf(GLGraphicsOffScreen g) {
        drawSelf(g, new ArrayList<FilterLayerArea>());
    }

    public void drawSelf(GLGraphicsOffScreen g, ArrayList<FilterLayerArea> parentFilters) {

        ArrayList<FilterLayerArea> currentFilters = new ArrayList<FilterLayerArea>();
        currentFilters.addAll(parentFilters);
        currentFilters.addAll(this.filters);

        g.pushMatrix();
        g.translate(position.x, position.y);
        g.rotate(rotation);
        g.scale(scale);

        PImage toDraw;
        if (!currentFilters.isEmpty()) {

            GLTexture currentTex, output = null;

            currentTex = frameBuffer.getTexture();
            for (FilterLayerArea f : currentFilters) {
                output = f.applyFilter(currentTex);
                currentTex = output;
            }
            toDraw = output;

        } else {
            toDraw = frameBuffer.getTexture();
        }

//        g.imageMode(PApplet.CENTER);
//        g.image(toDraw, drawSize.x / 2f, drawSize.y / 2f, drawSize.x, drawSize.y);

//        g.imageMode(PApplet.CORNER);
//        g.image(toDraw, 0, 0, drawSize.x, drawSize.y);

        g.imageMode(PApplet.CENTER);
        g.image(toDraw, 0, 0, drawSize.x, drawSize.y);


        for (Layer l : subLayers) {
            l.drawSelf(g, currentFilters);
        }

        if (this.drawBorders) {
            int elapsed = parent.millis() - this.drawBordersTime;
            int left = drawBordersDuration - elapsed;
            float ratio = (float) left / (float) drawBordersDuration;
            float intens = ratio * 255f;

            if (ratio < 0.0) {
                this.drawBorders = false;
            } else {

                g.rectMode(PApplet.CENTER);

                g.noFill();
                g.strokeWeight(3);
                g.stroke(52, 46, 242, intens);
                g.rect(0, 0, drawSize.x + 2, drawSize.y + 2);

                g.stroke(38, 34, 167, intens);
                g.rect(0, 0, drawSize.x + 4, drawSize.y + 4);

                g.stroke(21, 19, 90, intens);
                g.rect(0, 0, drawSize.x + 6, drawSize.y + 6);

            }
        }

        g.popMatrix();
    }

    public void drawSelfPreview(GLGraphicsOffScreen g, int sx, int sy) {
        this.drawSelfPreview(g, new ArrayList<FilterLayerArea>(), sx, sy);
    }

    public void drawSelfPreview(GLGraphicsOffScreen g, ArrayList<FilterLayerArea> parentFilters, int sx, int sy) {

        ArrayList<FilterLayerArea> currentFilters = new ArrayList<FilterLayerArea>();
        currentFilters.addAll(parentFilters);
        currentFilters.addAll(this.filters);


        g.pushMatrix();

        PImage toDraw;
        if (!filters.isEmpty()) {
            GLTexture currentTex, output = null;

            currentTex = frameBuffer.getTexture();
            for (FilterLayerArea f : currentFilters) {
                output = f.applyFilter(currentTex);
                currentTex = output;
            }
            toDraw = output;

        } else {
            toDraw = frameBuffer.getTexture();
        }

//        g.imageMode(PApplet.CENTER);
//        g.image(toDraw, drawSize.x / 2f, drawSize.y / 2f, drawSize.x, drawSize.y);

//        g.imageMode(PApplet.CORNER);
//        g.image(toDraw, 0, 0, drawSize.x, drawSize.y);

        g.imageMode(PApplet.CENTER);
        g.image(toDraw, 0, 0, sx, sy);

//        for (Layer l : subLayers) {
//            l.drawSelf(g, currentFilters);
//        }

        g.popMatrix();
    }
}
