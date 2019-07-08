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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import tech.lity.rea.nectar.camera.CameraFactory;
import processing.core.PApplet;
import processing.data.XML;
import tech.lity.rea.nectar.camera.Camera;
import tech.lity.rea.nectar.camera.CannotCreateCameraException;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class CameraConfiguration  extends Calibration {
    
    static final String CAMERA_XML_NAME = "Camera";
    static final String CAMERA_ID_XML_NAME = "CameraID";
    static final String CAMERA_NAME_XML_NAME = "CameraName";
    static final String CAMERA_FORMAT_XML_NAME = "CameraFormat";
    static final String CAMERA_TYPE_XML_NAME = "CameraType";

    private String cameraName = "";
    private String cameraFormat = "";
    private Camera.Type cameraType = Camera.Type.OPENCV;

    @Override
    public boolean isValid() {
        // todo check ID, name & type ?
        return true;
    }

    @Override
    public void addTo(XML xml) {
        xml.addChild(createCameraNode());
    }

    @Override
    public void replaceIn(XML xml) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Camera createCamera() throws CannotCreateCameraException{
       return CameraFactory.createCamera(cameraType, cameraName, cameraFormat);
    }

    @Override
    public void loadFrom(String data) {
        try {
            // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            XML root = XML.parse(data);
            XML cameraNode = root.getChild(CAMERA_XML_NAME);
            loadCameraFrom(cameraNode);
        } catch (IOException ex) {
            Logger.getLogger(CameraConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CameraConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(CameraConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void loadFrom(PApplet parent, String fileName) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        XML root = parent.loadXML(fileName);
        XML cameraNode = root.getChild(CAMERA_XML_NAME);
        loadCameraFrom(cameraNode);
    }


    private void loadCameraFrom(XML cameraNode) {
        this.cameraName = cameraNode.getString(CAMERA_NAME_XML_NAME);
        this.cameraFormat = cameraNode.getString(CAMERA_FORMAT_XML_NAME);
        this.cameraType = Camera.Type.valueOf(cameraNode.getString(CAMERA_TYPE_XML_NAME));
    }

    private XML createCameraNode() {
        XML cameraNode = new XML(CAMERA_XML_NAME);
        cameraNode.setString(CAMERA_NAME_XML_NAME, cameraName);
        cameraNode.setString(CAMERA_FORMAT_XML_NAME, cameraFormat);
        String type = this.cameraType.name();
        cameraNode.setString(CAMERA_TYPE_XML_NAME, type);

        return cameraNode;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getCameraFormat() {
        return cameraFormat;
    }

    public void setCameraFormat(String cameraFormat) {
        this.cameraFormat = cameraFormat;
    }

    public Camera.Type getCameraType() {
        return cameraType;
    }

    public void setCameraType(Camera.Type cameraType) {
        this.cameraType = cameraType;
    }

    @Override
    public void addTo(StringBuilder yaml) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
