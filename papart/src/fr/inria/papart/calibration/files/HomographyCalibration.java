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

import fr.inria.papart.calibration.HomographyCreator;
import fr.inria.papart.calibration.files.Calibration;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.data.XML;
import toxi.geom.Matrix4x4;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;
public class HomographyCalibration extends Calibration {

  public static final String HOMOGRAPHY_XML_NAME = "Homography";
  public static final HomographyCalibration INVALID = new HomographyCalibration();

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

  public Vec3D applyTo(ReadonlyVec3D src) {
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
      loadData(root);
  }

  public void loadFromFile(String fileName) {
      try {
          XML root = new XML(new File(fileName));
          loadData(root);
      } catch (IOException ex) {
          Logger.getLogger(HomographyCalibration.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ParserConfigurationException ex) {
          Logger.getLogger(HomographyCalibration.class.getName()).log(Level.SEVERE, null, ex);
      } catch (SAXException ex) {
          Logger.getLogger(HomographyCalibration.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
  
  public void loadFrom(String data) {
      try {
          System.out.println("Loading... : " + data);
          XML root = XML.parse(data);
          loadData(root);
      } catch (IOException ex) {
          Logger.getLogger(HomographyCalibration.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ParserConfigurationException ex) {
          Logger.getLogger(HomographyCalibration.class.getName()).log(Level.SEVERE, null, ex);
      } catch (SAXException ex) {
          Logger.getLogger(HomographyCalibration.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
  
  private void loadData(XML root){
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

  public void loadFromJSONString(String data) {
      JSONObject root = JSONObject.parse(data);
      JSONArray matrix = root.getJSONArray("matrix");

      pmatrix = new PMatrix3D();
      pmatrix.m00 = matrix.getFloat(0);
      pmatrix.m01 = matrix.getFloat(1);
      pmatrix.m02 = matrix.getFloat(2);
      pmatrix.m03 = matrix.getFloat(3);
      pmatrix.m10 = matrix.getFloat(4);
      pmatrix.m11 = matrix.getFloat(5);
      pmatrix.m12 = matrix.getFloat(6);
      pmatrix.m13 = matrix.getFloat(7);
      pmatrix.m20 = matrix.getFloat(8);
      pmatrix.m21 = matrix.getFloat(9);
      pmatrix.m22 = matrix.getFloat(10);
      pmatrix.m23 = matrix.getFloat(11);
      pmatrix.m30 = matrix.getFloat(12);
      pmatrix.m31 = matrix.getFloat(13);
      pmatrix.m32 = matrix.getFloat(14);
      pmatrix.m33 = matrix.getFloat(15);
      initMat();
  }

  public String exportToJSONString() {
      JSONObject root = new JSONObject();
      JSONArray matrix = new JSONArray();

      matrix.append(pmatrix.m00);
      matrix.append(pmatrix.m01);
      matrix.append(pmatrix.m02);
      matrix.append(pmatrix.m03);
      matrix.append(pmatrix.m10);
      matrix.append(pmatrix.m11);
      matrix.append(pmatrix.m12);
      matrix.append(pmatrix.m13);
      matrix.append(pmatrix.m20);
      matrix.append(pmatrix.m21);
      matrix.append(pmatrix.m22);
      matrix.append(pmatrix.m23);
      matrix.append(pmatrix.m30);
      matrix.append(pmatrix.m31);
      matrix.append(pmatrix.m32);
      matrix.append(pmatrix.m33);
      root.setJSONArray("matrix", matrix);

      return root.toString();
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

  public static void saveMatTo(PApplet applet, PMatrix3D mat, String fileName) {
      HomographyCalibration hc = new HomographyCalibration();
      hc.setMatrix(mat);
      hc.saveTo(applet, fileName);
  }

  public static PMatrix3D getMatFrom(PApplet applet, String fileName) {
      HomographyCalibration hc = new HomographyCalibration();
      hc.loadFrom(applet, fileName);
      return hc.getHomography();
  }

  public static PMatrix3D getMatFrom(String fileName) throws IOException, ParserConfigurationException, SAXException {
      HomographyCalibration hc = new HomographyCalibration();
      hc.loadFromFile(fileName);
      return hc.getHomography();
  }

  public static PMatrix3D getMatFromData(String data) {
      HomographyCalibration hc = new HomographyCalibration();
      try {
          hc.loadFromJSONString(data);
      } catch (Exception e) {
          System.out.println("Cannot read the homograpy: ");
          System.out.println(data);
          e.printStackTrace();
      }
      return hc.getHomography();
  }

  public static PMatrix3D CreateMatrixFrom(String data) {
      HomographyCalibration hc = new HomographyCalibration();
      hc.loadFromJSONString(data);
      return hc.getHomography();
  }

  public static HomographyCalibration CreateHomographyCalibrationFrom(PMatrix3D mat, PVector size) {
      float step = 0.5f;
      int nbPoints = (int) ((1 + 1f / step) * (1 + 1f / step));
      HomographyCreator homographyCreator = new HomographyCreator(3, 2, nbPoints);
      for (float i = 0; i <= 1.0; i += step) {
          for (float j = 0; j <= 1.0; j += step) {
              mat.translate(-i * size.x, -j * size.y);
              homographyCreator.addPoint(new PVector(mat.m03, mat.m13, mat.m23),
                      new PVector(i, j));
              mat.translate(i * size.x, j * size.y);
          }
      }
      assert (homographyCreator.isComputed());
      return homographyCreator.getHomography();
  }

  @Override
  public void addTo(StringBuilder yaml) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}