/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.drawingapp;


/**
 * DEPRACTED
 * @author jeremy
 */

/////////////// DEPRECATED
public class PaperSheet {

    public int width;
    public int height;
    public int scale;  // pixels by mm
    public int drawingWidth;
    public int drawingHeight;


    public PaperSheet(int width, int height, int scale) {
 
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.drawingHeight = height * scale;
        this.drawingWidth = width * scale;
    }
}
