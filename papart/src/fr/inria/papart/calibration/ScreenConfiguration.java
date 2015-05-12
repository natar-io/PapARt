/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.papart.calibration;

import processing.core.PApplet;
import processing.data.XML;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class ScreenConfiguration extends Calibration{
    
    static final String SCREEN_XML_NAME = "Screen";
    static final String SCREEN_WIDTH_XML_NAME = "Width";
    static final String SCREEN_HEIGHT_XML_NAME = "Height";
    static final String SCREEN_OFFSET_X_XML_NAME = "OffsetX";
    static final String SCREEN_OFFSET_Y_XML_NAME = "OffsetY";
    
    private int projectionScreenWidth = 0;
    private int projectionScreenHeight = 0;
    private int projectionScreenOffsetX = 0;
    private int projectionScreenOffsetY = 0;

      @Override
    public boolean isValid() {
        // todo check ID, name & type ?
        return true;
    }

    @Override
    public void addTo(XML xml) {
        xml.addChild(createScreenNode()); 
   }

    @Override
    public void replaceIn(XML xml) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    


    @Override
    public void loadFrom(PApplet parent, String fileName) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        XML root = parent.loadXML(fileName);

        XML screenNode = root.getChild(SCREEN_XML_NAME);
        loadScreenFrom(screenNode);


    }

    private void loadScreenFrom(XML screenNode) {
        this.projectionScreenWidth = screenNode.getInt(SCREEN_WIDTH_XML_NAME);
        this.projectionScreenHeight = screenNode.getInt(SCREEN_HEIGHT_XML_NAME);
        this.projectionScreenOffsetX = screenNode.getInt(SCREEN_OFFSET_X_XML_NAME);
        this.projectionScreenOffsetY = screenNode.getInt(SCREEN_OFFSET_Y_XML_NAME);
    }

    private XML createScreenNode() {
        XML screenNode = new XML(SCREEN_XML_NAME);
        screenNode.setInt(SCREEN_WIDTH_XML_NAME, projectionScreenWidth);
        screenNode.setInt(SCREEN_HEIGHT_XML_NAME, projectionScreenHeight);
        screenNode.setInt(SCREEN_OFFSET_X_XML_NAME, projectionScreenOffsetX);
        screenNode.setInt(SCREEN_OFFSET_Y_XML_NAME, projectionScreenOffsetY);
        return screenNode;
    }

    public int getProjectionScreenOffsetX() {
        return projectionScreenOffsetX;
    }

    public void setProjectionScreenOffsetX(int projectionScreenOffsetX) {
        this.projectionScreenOffsetX = projectionScreenOffsetX;
    }

    public int getProjectionScreenOffsetY() {
        return projectionScreenOffsetY;
    }

    public void setProjectionScreenOffsetY(int projectionScreenOffsetY) {
        this.projectionScreenOffsetY = projectionScreenOffsetY;
    }

    public int getProjectionScreenWidth() {
        return projectionScreenWidth;
    }

    public void setProjectionScreenWidth(int projectionScreenWidth) {
        this.projectionScreenWidth = projectionScreenWidth;
    }

    public int getProjectionScreenHeight() {
        return projectionScreenHeight;
    }

    public void setProjectionScreenHeight(int projectionScreenHeight) {
        this.projectionScreenHeight = projectionScreenHeight;
    }

}
