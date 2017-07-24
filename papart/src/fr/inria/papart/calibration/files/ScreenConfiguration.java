/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.calibration.files;

import fr.inria.papart.calibration.files.Calibration;
import processing.core.PApplet;
import processing.data.XML;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
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

    @Override
    public void addTo(StringBuilder yaml) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
