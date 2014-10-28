/*
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.depthcam.calibration;

import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.data.XML;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class HomographyCalibration extends Calibration {

    static final String HOMOGRAPHY_XML_NAME = "Homography";

    protected Matrix4x4 mat;
    protected PMatrix3D pmatrix;
    protected PMatrix3D invPmatrix;

    public void setMatrix(PMatrix3D inputMatrix) {
        checkAndCreateMatrix(inputMatrix);
        this.initMat();
    }

    private void checkAndCreateMatrix(PMatrix3D inputMatrix) {
        if (this.pmatrix == null) {
            this.pmatrix = inputMatrix.get();
        } else {
            this.pmatrix.set(inputMatrix);
        }
    }

    private void initMat() {
        float[] valuesFloat = new float[16];
        double[] valuesDouble = new double[16];
        pmatrix.get(valuesFloat);

        for (int i = 0; i < valuesFloat.length; i++) {
            valuesDouble[i] = (double) valuesFloat[i];
        }
        mat = new Matrix4x4(valuesDouble);
        invPmatrix = pmatrix.get();
        invPmatrix.invert();
    }

    public Vec3D applyTo(Vec3D src) {
        return this.mat.applyTo(src);
    }

    public PVector applyTo(PVector src) {
        PVector out = new PVector();
        this.pmatrix.mult(src, out);
        return out;
    }

    @Override
    public void addTo(XML xml) {
        XML homographyNode = new XML(HOMOGRAPHY_XML_NAME);
        setHomographyTo(homographyNode);
        xml.addChild(homographyNode);
    }

    @Override
    public void replaceIn(XML xml) {
        XML homographyNode = xml.getChild(HOMOGRAPHY_XML_NAME);
        setHomographyTo(homographyNode);
    }

    private void setHomographyTo(XML xml) {
        xml.setFloat("m00", pmatrix.m00);
        xml.setFloat("m01", pmatrix.m01);
        xml.setFloat("m02", pmatrix.m02);
        xml.setFloat("m03", pmatrix.m03);
        xml.setFloat("m10", pmatrix.m10);
        xml.setFloat("m11", pmatrix.m11);
        xml.setFloat("m12", pmatrix.m12);
        xml.setFloat("m13", pmatrix.m13);
        xml.setFloat("m20", pmatrix.m20);
        xml.setFloat("m21", pmatrix.m21);
        xml.setFloat("m22", pmatrix.m22);
        xml.setFloat("m23", pmatrix.m23);
        xml.setFloat("m30", pmatrix.m30);
        xml.setFloat("m31", pmatrix.m31);
        xml.setFloat("m32", pmatrix.m32);
        xml.setFloat("m33", pmatrix.m33);
    }

    @Override
    public void loadFrom(PApplet parent, String fileName) {
        XML root = parent.loadXML(fileName);
        XML homographyNode = root.getChild(HOMOGRAPHY_XML_NAME);
        pmatrix = new PMatrix3D();
        pmatrix.m00 = homographyNode.getFloat("m00");
        pmatrix.m01 = homographyNode.getFloat("m01");
        pmatrix.m02 = homographyNode.getFloat("m02");
        pmatrix.m03 = homographyNode.getFloat("m03");
        pmatrix.m10 = homographyNode.getFloat("m10");
        pmatrix.m11 = homographyNode.getFloat("m11");
        pmatrix.m12 = homographyNode.getFloat("m12");
        pmatrix.m13 = homographyNode.getFloat("m13");
        pmatrix.m20 = homographyNode.getFloat("m20");
        pmatrix.m21 = homographyNode.getFloat("m21");
        pmatrix.m22 = homographyNode.getFloat("m22");
        pmatrix.m23 = homographyNode.getFloat("m23");
        pmatrix.m30 = homographyNode.getFloat("m30");
        pmatrix.m31 = homographyNode.getFloat("m31");
        pmatrix.m32 = homographyNode.getFloat("m32");
        pmatrix.m33 = homographyNode.getFloat("m33");
        initMat();
    }

    @Override
    public boolean isValid() {
        return this.pmatrix != null;
    }

    @Deprecated
    public Matrix4x4 getHomographyMat4x4() {
        return mat;
    }

    public PMatrix3D getHomography() {
        return this.pmatrix;
    }

    public PMatrix3D getHomographyInv() {
        return this.invPmatrix;
    }

    @Override
    public String toString() {
        return this.mat.toString();
    }

}
