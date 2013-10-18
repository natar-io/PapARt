/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.imanalysis;

import java.util.ArrayList;
import processing.core.PImage;

/**
 *
 * @author jiii
 */

// TODO: implement PImage ? 
public class PixelRectangle extends Pixel implements IPixelGroup<Pixel> {

    int width, height;

    public PixelRectangle(PImage image, int x, int y, int width, int height) throws SizeException{
        super(image, x ,y);
        
        if(x + width > image.width || 
           y + height > image.height)
            throw new SizeException("Pixel Rectangle: invalid size.");
        
        this.width = width;
        this.height = height;
    }

    public ArrayList<Pixel> getElements() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    
    
}
