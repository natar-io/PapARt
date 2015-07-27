/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.calibration;

import java.util.Arrays;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.data.XML;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class ProjectiveDeviceCalibration extends Calibration {

    static final String INTRINSICS_XML_NAME = "Intrinsics";
    static final String EXTRINSICS_XML_NAME = "Extrinsics";
    static final String RESOLUTION_XML_NAME = "Resolution";
    static final String WIDTH_XML_NAME = "Width";
    static final String HEIGHT_XML_NAME = "Height";

    // TO implement...
//    static final String DISTORSION_XML_NAME = "Distorsions";
    protected final PMatrix3D intrinsics = new PMatrix3D();
    protected final PMatrix3D extrinsics = new PMatrix3D();
    private int width, height;
    private boolean hasExtrinsics = false;

    @Override
    public void addTo(XML xml) {
        xml.addChild(resolutionNode());
        xml.addChild(intrinsicNode());

        if (hasExtrinsics) {
            xml.addChild(extrinsicsNode());
        }
    }

    private XML resolutionNode() {
        XML node = new XML(RESOLUTION_XML_NAME);
        node.setInt(WIDTH_XML_NAME, width);
        node.setInt(HEIGHT_XML_NAME, height);
        return node;
    }
    
    private XML intrinsicNode() {
        XML node = new XML(INTRINSICS_XML_NAME);
        setXmlTo(node, intrinsics);
        return node;
    }

    private XML extrinsicsNode() {
        XML node = new XML(EXTRINSICS_XML_NAME);
        setXmlTo(node, extrinsics);
        return node;
    }

    @Override
    public void replaceIn(XML xml) {

        XML intrNode = xml.getChild(INTRINSICS_XML_NAME);
        xml.removeChild(intrNode);

        XML extrNode = xml.getChild(EXTRINSICS_XML_NAME);
        xml.removeChild(extrNode);

        XML resNode = xml.getChild(RESOLUTION_XML_NAME);
        xml.removeChild(resNode);

        addTo(xml);
    }

    private void setXmlTo(XML xml, PMatrix3D matrix) {
        xml.setFloat("m00", matrix.m00);
        xml.setFloat("m01", matrix.m01);
        xml.setFloat("m02", matrix.m02);
        xml.setFloat("m03", matrix.m03);
        xml.setFloat("m10", matrix.m10);
        xml.setFloat("m11", matrix.m11);
        xml.setFloat("m12", matrix.m12);
        xml.setFloat("m13", matrix.m13);
        xml.setFloat("m20", matrix.m20);
        xml.setFloat("m21", matrix.m21);
        xml.setFloat("m22", matrix.m22);
        xml.setFloat("m23", matrix.m23);
        xml.setFloat("m30", matrix.m30);
        xml.setFloat("m31", matrix.m31);
        xml.setFloat("m32", matrix.m32);
        xml.setFloat("m33", matrix.m33);
    }

    private void getMatFrom(XML node, PMatrix3D matrix) {
        matrix.m00 = node.getFloat("m00");
        matrix.m01 = node.getFloat("m01");
        matrix.m02 = node.getFloat("m02");
        matrix.m03 = node.getFloat("m03");
        matrix.m10 = node.getFloat("m10");
        matrix.m11 = node.getFloat("m11");
        matrix.m12 = node.getFloat("m12");
        matrix.m13 = node.getFloat("m13");
        matrix.m20 = node.getFloat("m20");
        matrix.m21 = node.getFloat("m21");
        matrix.m22 = node.getFloat("m22");
        matrix.m23 = node.getFloat("m23");
        matrix.m30 = node.getFloat("m30");
        matrix.m31 = node.getFloat("m31");
        matrix.m32 = node.getFloat("m32");
        matrix.m33 = node.getFloat("m33");
    }

    @Override
    public void loadFrom(PApplet parent, String fileName) {
        XML root = parent.loadXML(fileName);
        XML resolutionNode = root.getChild(RESOLUTION_XML_NAME);
        this.width = resolutionNode.getInt(WIDTH_XML_NAME);
        this.height = resolutionNode.getInt(HEIGHT_XML_NAME);
        
        
        XML intrinsicsNode = root.getChild(INTRINSICS_XML_NAME);
        getMatFrom(intrinsicsNode, intrinsics);

        XML extrinsicsNode = root.getChild(EXTRINSICS_XML_NAME);
        if(extrinsicsNode == null){
            this.hasExtrinsics = false;
            return;
        }
        getMatFrom(extrinsicsNode, extrinsics);

        checkExtrinsics();
    }

    private void checkExtrinsics() {
        this.hasExtrinsics = !isIdentity(extrinsics);
    }

    public boolean isIdentity(PMatrix3D mat) {
        PMatrix3D identity = new PMatrix3D();
        identity.reset();
        float[] identityArray = new float[16];
        float[] matArray = new float[16];
        identity.get(identityArray);
        mat.get(matArray);

        return Arrays.equals(identityArray, matArray);
    }

    public void setExtrinsics(PMatrix3D mat) {
        this.extrinsics.set(mat);
        checkExtrinsics();
    }
    
    public void setIntrinsics(PMatrix3D mat) {
        this.intrinsics.set(mat);
    }
    
    public PMatrix3D getExtrinsics(){
        return this.extrinsics.get();
    }
    
    public PMatrix3D getIntrinsics(){
        return this.intrinsics.get();
    }
    
    public boolean hasExtrinsics(){
        return this.hasExtrinsics;
    }
    
    public int getWidth(){
        return width;
    }
    
    public int getHeight(){
        return height;
    }

    public void setWidth(int width){
        this.width = width;
    }
    
    public void setHeight(int height){
        this.height = height;
    }
    
    @Override
    public boolean isValid() {
        return true;
//        return this.pmatrix != null;
    }

    @Override
    public String toString() {
        return this.intrinsics.toString() + this.extrinsics.toString();
    }

}
