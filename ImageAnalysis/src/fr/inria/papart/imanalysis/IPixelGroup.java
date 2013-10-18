/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.imanalysis;

import java.util.ArrayList;

/**
 *
 * @author jiii
 */

// TODO: implement PImage ? 
public interface IPixelGroup<T> extends IPixel{
    
    public ArrayList<T> getElements();
    
}
