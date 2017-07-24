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

import fr.inria.papart.scanner.GrayCode;
import processing.core.PApplet;
import processing.data.XML;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class CameraProjectorSync extends Calibration {

    private int displayDuration;
    private int captureTime;
    private int delay;

    private int decodeType = GrayCode.DECODE_NOT_SET;
    private int decodeValue = -1;

    static final String CAM_PROJ_SYNC_XML_NAME = "CamProjSync";
    static final String CAM_PROJ_SYNC_CAPTURE_XML_NAME = "Capture";
    static final String CAM_PROJ_SYNC_DELAY_XML_NAME = "Delay";
    static final String CAM_PROJ_SYNC_DISPLAY_XML_NAME = "Display";
    static final String CAM_PROJ_SYNC_TYPE_XML_NAME = "Type";
    static final String CAM_PROJ_SYNC_VALUE_XML_NAME = "Value";

    public CameraProjectorSync(int displayDuration, int captureTime, int delay) {
        this.displayDuration = displayDuration;
        this.captureTime = captureTime;
        this.delay = delay;
    }

    public void setDecodeParameters(int type, int value) {
        this.decodeType = type;
        this.decodeValue = value;
    }

    // TODO: max value ?
    @Override
    public boolean isValid() {
        if (displayDuration <= 0 || delay < 0 || captureTime < 0) {
            return false;
        }
        if (decodeType == GrayCode.DECODE_NOT_SET || decodeValue <= 0) {
            return false;
        }

        return true;
    }

    @Override
    public void addTo(XML xml) {
        XML syncNode = new XML(CAM_PROJ_SYNC_XML_NAME);
        addElementsTo(syncNode);
        xml.addChild(syncNode);
    }

    @Override
    public void replaceIn(XML xml) {
        XML syncNode = xml.getChild(CAM_PROJ_SYNC_XML_NAME);
        addElementsTo(syncNode);
    }

    @Override
    public void loadFrom(PApplet parent, String fileName) {
        XML root = parent.loadXML(fileName);
        XML syncNode = root.getChild(CAM_PROJ_SYNC_XML_NAME);
        setThisFrom(syncNode);
    }

    private void setThisFrom(XML syncNode) {
        setCaptureTime(get(syncNode, CAM_PROJ_SYNC_CAPTURE_XML_NAME));
        setDelay(get(syncNode, CAM_PROJ_SYNC_DELAY_XML_NAME));
        setDisplayDuration(get(syncNode, CAM_PROJ_SYNC_DISPLAY_XML_NAME));
        setDecodeParameters(get(syncNode, CAM_PROJ_SYNC_TYPE_XML_NAME),
                get(syncNode, CAM_PROJ_SYNC_TYPE_XML_NAME));
    }

    private void addElementsTo(XML syncNode) {
        add(syncNode, CAM_PROJ_SYNC_DISPLAY_XML_NAME, this.displayDuration);
        add(syncNode, CAM_PROJ_SYNC_DELAY_XML_NAME, this.delay);
        add(syncNode, CAM_PROJ_SYNC_CAPTURE_XML_NAME, this.captureTime);
        add(syncNode, CAM_PROJ_SYNC_TYPE_XML_NAME, this.decodeType);
        add(syncNode, CAM_PROJ_SYNC_VALUE_XML_NAME, this.decodeValue);
    }

    private void add(XML root, String name, int value) {
        root.setInt(name, value);
    }

    private int get(XML root, String name) {
        return root.getInt(name);
    }

    public int getDisplayDuration() {
        return displayDuration;
    }

    public void setDisplayDuration(int displayDuration) {
        this.displayDuration = displayDuration;
    }

    public int getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(int captureTime) {
        this.captureTime = captureTime;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDecodeType() {
        return decodeType;
    }

    public int getDecodeValue() {
        return decodeValue;
    }

    @Override
    public void addTo(StringBuilder yaml) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
