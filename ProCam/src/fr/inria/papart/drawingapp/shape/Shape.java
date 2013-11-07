/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.drawingapp.shape;

import fr.inria.papart.drawingapp.BBox;
import fr.inria.papart.drawingapp.Descriptor;
import fr.inria.papart.drawingapp.Drawable;
import fr.inria.papart.drawingapp.DrawingApp;
import fr.inria.papart.drawingapp.PositionDescriptor;
import processing.opengl.PGraphicsOpenGL;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class Shape implements Drawable {

    protected Descriptor[] descriptors;
    protected BBox bbox;
    protected boolean isHidden = false;
    protected boolean isSelected = false;
    protected boolean isMovable = false;
    protected Descriptor centerDescriptor;
    protected PVector center;
    public int strokeWeight = 2;
    public int strokeColor = 255;

    public Shape(PVector pos) {
        centerDescriptor = new PositionDescriptor(new PVector(), pos, 50, 50, "cross.png");
        center = ((PositionDescriptor )centerDescriptor).getPosition();
    }

    @Override
    public void drawSelf(PGraphicsOpenGL graphics) {
        if (isMovable) {
            centerDescriptor.drawSelf(graphics);
        }
    }

    public Descriptor[] getDescriptors() {
        return descriptors;
    }

    public BBox getBBox() {
        return bbox;
    }

    public void setMovable(boolean mov) {
        if(mov == isMovable )
            return;
        isMovable = mov;
        if (isSelected) {
            if (isMovable) {
                DrawingApp.zones.add(centerDescriptor.getInteractiveZone());
            } else {
                DrawingApp.zones.remove(centerDescriptor.getInteractiveZone());
            }
        }
    }

    public void select(boolean s) {
        if(this.isSelected == s)
            return;
        this.isSelected = s;
        if (isSelected) {
            if (isMovable) {
                DrawingApp.zones.add(centerDescriptor.getInteractiveZone());
            }
            for (Descriptor d : descriptors) {
                DrawingApp.zones.add(d.getInteractiveZone());
            }
        } else {
            if (isMovable) {
                DrawingApp.zones.add(centerDescriptor.getInteractiveZone());
            }
            for (Descriptor d : descriptors) {
                DrawingApp.zones.remove(d.getInteractiveZone());
            }
        }
    }

    @Override
    public void show() {
        isHidden = false;
    }

    @Override
    public void hide() {
        isHidden = true;
    }
}
