/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.calibration;

import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.camera.CameraFactory;
import processing.core.PApplet;
import processing.data.XML;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class ComputerConfiguration extends Calibration {

    static final String SCREEN_XML_NAME = "Screen";
    static final String SCREEN_WIDTH_XML_NAME = "Width";
    static final String SCREEN_HEIGHT_XML_NAME = "Height";
    static final String SCREEN_OFFSET_X_XML_NAME = "OffsetX";
    static final String SCREEN_OFFSET_Y_XML_NAME = "OffsetY";
    static final String CAMERA_XML_NAME = "Camera";
    static final String CAMERA_ID_XML_NAME = "CameraID";
    static final String CAMERA_NAME_XML_NAME = "CameraName";
    static final String CAMERA_TYPE_XML_NAME = "CameraType";

    private int projectionScreenWidth = 0;
    private int projectionScreenHeight = 0;
    private int projectionScreenOffsetX = 0;

    private int projectionScreenOffsetY = 0;
    private String cameraName = "";
    private Camera.Type cameraType = Camera.Type.OPENCV;

    @Override
    public boolean isValid() {
        // todo check ID, name & type ?
        return true;
    }

    @Override
    public void addTo(XML xml) {
        xml.addChild(createScreenNode());
        xml.addChild(createCameraNode());
    }

    @Override
    public void replaceIn(XML xml) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Camera createCamera(){
       return CameraFactory.createCamera(cameraType, cameraName);
    }

    @Override
    public void loadFrom(PApplet parent, String fileName) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        XML root = parent.loadXML(fileName);

        XML screenNode = root.getChild(SCREEN_XML_NAME);
        loadScreenFrom(screenNode);

        XML cameraNode = root.getChild(CAMERA_XML_NAME);
        loadCameraFrom(cameraNode);

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

    private void loadCameraFrom(XML cameraNode) {
        this.cameraName = cameraNode.getString(CAMERA_NAME_XML_NAME);
        this.cameraType = Camera.Type.valueOf(cameraNode.getString(CAMERA_TYPE_XML_NAME));
    }

    private XML createCameraNode() {
        XML cameraNode = new XML(CAMERA_XML_NAME);
        cameraNode.setString(CAMERA_NAME_XML_NAME, cameraName);
        String type = this.cameraType.name();
        cameraNode.setString(CAMERA_TYPE_XML_NAME, type);

        return cameraNode;
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

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public Camera.Type getCameraType() {
        return cameraType;
    }

    public void setCameraType(Camera.Type cameraType) {
        this.cameraType = cameraType;
    }

}
