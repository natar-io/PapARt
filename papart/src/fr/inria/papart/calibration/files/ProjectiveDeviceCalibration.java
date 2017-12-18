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
import java.util.Arrays;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.data.XML;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
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
    private boolean isCamera;

    @Override
    public void addTo(XML xml) {
        xml.addChild(resolutionNode());
        xml.addChild(intrinsicNode());

        if (hasExtrinsics) {
            xml.addChild(extrinsicsNode());
        }
    }

    @Override
    public void addTo(StringBuilder yaml) {

        if (this.isCamera) {
            yaml.append("Cameras:\n   - Camera  0\n");
            yaml.append("Camera  0:\n");

        } else {
            yaml.append("Projectors:  \n   - Projector 0\n");
            yaml.append("Projector 0:\n");
        }

        yaml.append("   imageWidth: " + Integer.toString(width) + "\n");
        yaml.append("   imageHeight: " + Integer.toString(height) + "\n");
        yaml.append("   responseGamma: 0.\n");

        yaml.append("   cameraMatrix: !!opencv-matrix\n");
        yaml.append("      rows: 3\n");
        yaml.append("      cols: 3\n");
        yaml.append("      dt: d\n");
        yaml.append("      data: [");
        
        yaml.append(Float.toString(intrinsics.m00) + ", ");
        yaml.append(Float.toString(intrinsics.m01) + ", ");
        yaml.append(Float.toString(intrinsics.m02) + ", ");
        
        yaml.append(Float.toString(intrinsics.m10) + ", ");
        yaml.append(Float.toString(intrinsics.m11) + ", ");
        yaml.append(Float.toString(intrinsics.m12) + ", ");
        
        yaml.append(Float.toString(intrinsics.m20) + ", ");
        yaml.append(Float.toString(intrinsics.m21) + ", ");
        yaml.append(Float.toString(intrinsics.m22) + " ]\n");
        
    }
        
//x       %YAML:1.0
//x Cameras:
//x   - Camera  0
//xProjectors:
//x   []
//xCamera  0:
//x   imageWidth: 1920
//x   imageHeight: 1080
// x  responseGamma: 0.
//   cameraMatrix: !!opencv-matrix
//      rows: 3
//      cols: 3
//      dt: d
//      data: [ 1.6246585547708776e+03, 0., 9.8597508076737813e+02, 0.,
//          1.6243998128297719e+03, 4.7161319611021008e+02, 0., 0., 1. ]
//   distortionCoeffs: !!opencv-matrix
//      rows: 1
//      cols: 4
//      dt: d
//      data: [ 1.7632444304289237e-02, -1.6174386935444651e-01,
//          -3.9711000431226348e-03, 1.4023625659419674e-03 ]
//   extrParams: !!opencv-matrix
//      rows: 20
//      cols: 6
//      dt: d
//      data: [ -4.3244266294843653e-01, -5.
//        
//   R: !!opencv-matrix
//      rows: 3
//      cols: 3
//      dt: d
//      data: [ 9.9982646476206072e-01, 5.1573525372682521e-03,
//          1.7900895960986062e-02, -3.6903720890462477e-03,
//          9.9670431223524325e-01, -8.1036381493223425e-02,
//          -1.8259833384894363e-02, 8.0956257858655928e-02,
//          9.9655038146512132e-01 ]
//   T: !!opencv-matrix
//      rows: 3
//      cols: 1
//      dt: d
//      data: [ -1.0030940723190477e+02, -6.6307072972750172e+00,
//          8.5795561307515520e+00 ]

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
        if (extrinsicsNode == null) {
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

    public PMatrix3D getExtrinsics() {
        return this.extrinsics.get();
    }

    public PMatrix3D getIntrinsics() {
        return this.intrinsics.get();
    }

    public boolean hasExtrinsics() {
        return this.hasExtrinsics;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
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

    public void isCamera(boolean b) {
        this.isCamera = b;
    }

}
