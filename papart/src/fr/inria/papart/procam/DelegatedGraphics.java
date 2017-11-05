/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
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
package fr.inria.papart.procam;

import java.awt.Image;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PStyle;
import processing.core.PSurface;
import processing.opengl.FrameBuffer;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShader;
import processing.opengl.Texture;

/**
 *
 * @author JeremyLaviole
 */
public class DelegatedGraphics {
    
    protected PGraphicsOpenGL currentGraphics;

    public void setPrimary(boolean primary) {
        currentGraphics.setPrimary(primary);
    }

    public void setSize(int iwidth, int iheight) {
        currentGraphics.setSize(iwidth, iheight);
    }

    public void dispose() {
        System.out.println("Dispose of PaperScreen");
        currentGraphics.dispose();
    }

    public PSurface createSurface() {
        return currentGraphics.createSurface();
    }

    public void setCache(PImage image, Object storage) {
        currentGraphics.setCache(image, storage);
    }

    public Object getCache(PImage image) {
        return currentGraphics.getCache(image);
    }

    public void removeCache(PImage image) {
        currentGraphics.removeCache(image);
    }

//    public void beginDraw() {
//        currentGraphics.beginDraw();
//    }
//
//    public void endDraw() {
//        currentGraphics.endDraw();
//    }
    public PGL beginPGL() {
        return currentGraphics.beginPGL();
    }

    public void endPGL() {
        currentGraphics.endPGL();
    }

    public void updateProjmodelview() {
        currentGraphics.updateProjmodelview();
    }

    public void hint(int which) {
        currentGraphics.hint(which);
    }

    public void beginShape(int kind) {
        currentGraphics.beginShape(kind);
    }

    public void endShape(int mode) {
        currentGraphics.endShape(mode);
    }

    public void textureWrap(int wrap) {
        currentGraphics.textureWrap(wrap);
    }

    public void textureSampling(int sampling) {
        currentGraphics.textureSampling(sampling);
    }

    public void beginContour() {
        currentGraphics.beginContour();
    }

    public void endContour() {
        currentGraphics.endContour();
    }

    public void vertex(float x, float y) {
        currentGraphics.vertex(x, y);
    }

    public void vertex(float x, float y, float u, float v) {
        currentGraphics.vertex(x, y, u, v);
    }

    public void vertex(float x, float y, float z) {
        currentGraphics.vertex(x, y, z);
    }

    public void vertex(float x, float y, float z, float u, float v) {
        currentGraphics.vertex(x, y, z, u, v);
    }

    public void attribPosition(String name, float x, float y, float z) {
        currentGraphics.attribPosition(name, x, y, z);
    }

    public void attribNormal(String name, float nx, float ny, float nz) {
        currentGraphics.attribNormal(name, nx, ny, nz);
    }

    public void attribColor(String name, int color) {
        currentGraphics.attribColor(name, color);
    }

    public void attrib(String name, float... values) {
        currentGraphics.attrib(name, values);
    }

    public void attrib(String name, int... values) {
        currentGraphics.attrib(name, values);
    }

    public void attrib(String name, boolean... values) {
        currentGraphics.attrib(name, values);
    }

    public void noClip() {
        currentGraphics.noClip();
    }

    public void flush() {
        currentGraphics.flush();
    }

    public void bezierVertex(float x2, float y2, float x3, float y3, float x4, float y4) {
        currentGraphics.bezierVertex(x2, y2, x3, y3, x4, y4);
    }

    public void bezierVertex(float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        currentGraphics.bezierVertex(x2, y2, z2, x3, y3, z3, x4, y4, z4);
    }

    public void quadraticVertex(float cx, float cy, float x3, float y3) {
        currentGraphics.quadraticVertex(cx, cy, x3, y3);
    }

    public void quadraticVertex(float cx, float cy, float cz, float x3, float y3, float z3) {
        currentGraphics.quadraticVertex(cx, cy, cz, x3, y3, z3);
    }

    public void curveVertex(float x, float y) {
        currentGraphics.curveVertex(x, y);
    }

    public void curveVertex(float x, float y, float z) {
        currentGraphics.curveVertex(x, y, z);
    }

    public void point(float x, float y) {
        currentGraphics.point(x, y);
    }

    public void point(float x, float y, float z) {
        currentGraphics.point(x, y, z);
    }

    public void line(float x1, float y1, float x2, float y2) {
        currentGraphics.line(x1, y1, x2, y2);
    }

    public void line(float x1, float y1, float z1, float x2, float y2, float z2) {
        currentGraphics.line(x1, y1, z1, x2, y2, z2);
    }

    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        currentGraphics.triangle(x1, y1, x2, y2, x3, y3);
    }

    public void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        currentGraphics.quad(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void ellipseImpl(float a, float b, float c, float d) {
        currentGraphics.ellipseImpl(a, b, c, d);
    }

    public void box(float w, float h, float d) {
        currentGraphics.box(w, h, d);
    }

    public void sphere(float r) {
        currentGraphics.sphere(r);
    }

    public PShape loadShape(String filename) {
        return currentGraphics.loadShape(filename);
    }

    public float textAscent() {
        return currentGraphics.textAscent();
    }

    public float textDescent() {
        return currentGraphics.textDescent();
    }

    public void textSize(float size) {
        currentGraphics.textSize(size);
    }

    public void pushMatrix() {
        currentGraphics.pushMatrix();
    }

    public void popMatrix() {
        currentGraphics.popMatrix();
    }

    public void translate(float tx, float ty) {
        currentGraphics.translate(tx, ty);
    }

    public void translate(float tx, float ty, float tz) {
        currentGraphics.translate(tx, ty, tz);
    }

    public void rotate(float angle) {
        currentGraphics.rotate(angle);
    }

    public void rotateX(float angle) {
        currentGraphics.rotateX(angle);
    }

    public void rotateY(float angle) {
        currentGraphics.rotateY(angle);
    }

    public void rotateZ(float angle) {
        currentGraphics.rotateZ(angle);
    }

    public void rotate(float angle, float v0, float v1, float v2) {
        currentGraphics.rotate(angle, v0, v1, v2);
    }

    public void scale(float s) {
        currentGraphics.scale(s);
    }

    public void scale(float sx, float sy) {
        currentGraphics.scale(sx, sy);
    }

    public void scale(float sx, float sy, float sz) {
        currentGraphics.scale(sx, sy, sz);
    }

    public void shearX(float angle) {
        currentGraphics.shearX(angle);
    }

    public void shearY(float angle) {
        currentGraphics.shearY(angle);
    }

    public void resetMatrix() {
        currentGraphics.resetMatrix();
    }

    public void applyMatrix(PMatrix2D source) {
        currentGraphics.applyMatrix(source);
    }

    public void applyMatrix(float n00, float n01, float n02, float n10, float n11, float n12) {
        currentGraphics.applyMatrix(n00, n01, n02, n10, n11, n12);
    }

    public void applyMatrix(PMatrix3D source) {
        currentGraphics.applyMatrix(source);
    }

    public void applyMatrix(float n00, float n01, float n02, float n03, float n10, float n11, float n12, float n13, float n20, float n21, float n22, float n23, float n30, float n31, float n32, float n33) {
        currentGraphics.applyMatrix(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33);
    }

    public PMatrix getMatrix() {
        return currentGraphics.getMatrix();
    }

    public PMatrix3D getMatrix(PMatrix3D target) {
        return currentGraphics.getMatrix(target);
    }

    public void setMatrix(PMatrix2D source) {
        currentGraphics.setMatrix(source);
    }

    public void setMatrix(PMatrix3D source) {
        currentGraphics.setMatrix(source);
    }

    public void printMatrix() {
        currentGraphics.printMatrix();
    }

    public void pushProjection() {
        currentGraphics.pushProjection();
    }

    public void popProjection() {
        currentGraphics.popProjection();
    }

    public void resetProjection() {
        currentGraphics.resetProjection();
    }

    public void applyProjection(PMatrix3D mat) {
        currentGraphics.applyProjection(mat);
    }

    public void applyProjection(float n00, float n01, float n02, float n03, float n10, float n11, float n12, float n13, float n20, float n21, float n22, float n23, float n30, float n31, float n32, float n33) {
        currentGraphics.applyProjection(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33);
    }

    public void setProjection(PMatrix3D mat) {
        currentGraphics.setProjection(mat);
    }

    public void beginCamera() {
        currentGraphics.beginCamera();
    }

    public void endCamera() {
        currentGraphics.endCamera();
    }

    public void camera() {
        currentGraphics.camera();
    }

    public void camera(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        currentGraphics.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    public void printCamera() {
        currentGraphics.printCamera();
    }

    public void ortho() {
        currentGraphics.ortho();
    }

    public void ortho(float left, float right, float bottom, float top) {
        currentGraphics.ortho(left, right, bottom, top);
    }

    public void ortho(float left, float right, float bottom, float top, float near, float far) {
        currentGraphics.ortho(left, right, bottom, top, near, far);
    }

    public void perspective() {
        currentGraphics.perspective();
    }

    public void perspective(float fov, float aspect, float zNear, float zFar) {
        currentGraphics.perspective(fov, aspect, zNear, zFar);
    }

    public void frustum(float left, float right, float bottom, float top, float znear, float zfar) {
        currentGraphics.frustum(left, right, bottom, top, znear, zfar);
    }

    public void printProjection() {
        currentGraphics.printProjection();
    }

    public float screenX(float x, float y) {
        return currentGraphics.screenX(x, y);
    }

    public float screenY(float x, float y) {
        return currentGraphics.screenY(x, y);
    }

    public float screenX(float x, float y, float z) {
        return currentGraphics.screenX(x, y, z);
    }

    public float screenY(float x, float y, float z) {
        return currentGraphics.screenY(x, y, z);
    }

    public float screenZ(float x, float y, float z) {
        return currentGraphics.screenZ(x, y, z);
    }

    public float modelX(float x, float y, float z) {
        return currentGraphics.modelX(x, y, z);
    }

    public float modelY(float x, float y, float z) {
        return currentGraphics.modelY(x, y, z);
    }

    public float modelZ(float x, float y, float z) {
        return currentGraphics.modelZ(x, y, z);
    }

    public void popStyle() {
        currentGraphics.popStyle();
    }

    public void strokeWeight(float weight) {
        currentGraphics.strokeWeight(weight);
    }

    public void strokeJoin(int join) {
        currentGraphics.strokeJoin(join);
    }

    public void strokeCap(int cap) {
        currentGraphics.strokeCap(cap);
    }

    public void lights() {
        currentGraphics.lights();
    }

    public void noLights() {
        currentGraphics.noLights();
    }

    public void ambientLight(float r, float g, float b) {
        currentGraphics.ambientLight(r, g, b);
    }

    public void ambientLight(float r, float g, float b, float x, float y, float z) {
        currentGraphics.ambientLight(r, g, b, x, y, z);
    }

    public void directionalLight(float r, float g, float b, float dx, float dy, float dz) {
        currentGraphics.directionalLight(r, g, b, dx, dy, dz);
    }

    public void pointLight(float r, float g, float b, float x, float y, float z) {
        currentGraphics.pointLight(r, g, b, x, y, z);
    }

    public void spotLight(float r, float g, float b, float x, float y, float z, float dx, float dy, float dz, float angle, float concentration) {
        currentGraphics.spotLight(r, g, b, x, y, z, dx, dy, dz, angle, concentration);
    }

    public void lightFalloff(float constant, float linear, float quadratic) {
        currentGraphics.lightFalloff(constant, linear, quadratic);
    }

    public void lightSpecular(float x, float y, float z) {
        currentGraphics.lightSpecular(x, y, z);
    }

    public boolean isGL() {
        return currentGraphics.isGL();
    }

    public void loadPixels() {
        currentGraphics.loadPixels();
    }

    public int get(int x, int y) {
        return currentGraphics.get(x, y);
    }

    public void set(int x, int y, int argb) {
        currentGraphics.set(x, y, argb);
    }

    public boolean save(String filename) {
        return currentGraphics.save(filename);
    }

    public void loadTexture() {
        currentGraphics.loadTexture();
    }

    public void updateTexture() {
        currentGraphics.updateTexture();
    }

    public void updateTexture(int x, int y, int w, int h) {
        currentGraphics.updateTexture(x, y, w, h);
    }

    public void updateDisplay() {
        currentGraphics.updateDisplay();
    }

    public void mask(PImage alpha) {
        currentGraphics.mask(alpha);
    }

    public void filter(int kind) {
        currentGraphics.filter(kind);
    }

    public void filter(int kind, float param) {
        currentGraphics.filter(kind, param);
    }

    public void filter(PShader shader) {
        currentGraphics.filter(shader);
    }

    public void copy(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        currentGraphics.copy(sx, sy, sw, sh, dx, dy, dw, dh);
    }

    public void copy(PImage src, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        currentGraphics.copy(src, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    public Texture getTexture() {
        return currentGraphics.getTexture();
    }

    public Texture getTexture(boolean load) {
        return currentGraphics.getTexture(load);
    }

    public Texture getTexture(PImage img) {
        return currentGraphics.getTexture(img);
    }

    public FrameBuffer getFrameBuffer() {
        return currentGraphics.getFrameBuffer();
    }

    public FrameBuffer getFrameBuffer(boolean multi) {
        return currentGraphics.getFrameBuffer(multi);
    }

    public void resize(int wide, int high) {
        currentGraphics.resize(wide, high);
    }

    public PShader loadShader(String fragFilename) {
        return currentGraphics.loadShader(fragFilename);
    }

    public PShader loadShader(String fragFilename, String vertFilename) {
        return currentGraphics.loadShader(fragFilename, vertFilename);
    }

    public void shader(PShader shader) {
        currentGraphics.shader(shader);
    }

    public void shader(PShader shader, int kind) {
        currentGraphics.shader(shader, kind);
    }

    public void resetShader() {
        currentGraphics.resetShader();
    }

    public void resetShader(int kind) {
        currentGraphics.resetShader(kind);
    }

    public void setParent(PApplet parent) {
        currentGraphics.setParent(parent);
    }

    public void setPath(String path) {
        currentGraphics.setPath(path);
    }

    public void beginShape() {
        currentGraphics.beginShape();
    }

    public void edge(boolean edge) {
        currentGraphics.edge(edge);
    }

    public void normal(float nx, float ny, float nz) {
        currentGraphics.normal(nx, ny, nz);
    }

    public void textureMode(int mode) {
        currentGraphics.textureMode(mode);
    }

    public void texture(PImage image) {
        currentGraphics.texture(image);
    }

    public void noTexture() {
        currentGraphics.noTexture();
    }

    public void vertex(float[] v) {
        currentGraphics.vertex(v);
    }

    public void endShape() {
        currentGraphics.endShape();
    }

    public PShape loadShape(String filename, String options) {
        return currentGraphics.loadShape(filename, options);
    }

    public PShape createShape() {
        return currentGraphics.createShape();
    }

    public PShape createShape(int type) {
        return currentGraphics.createShape(type);
    }

    public PShape createShape(int kind, float... p) {
        return currentGraphics.createShape(kind, p);
    }

    public void clip(float a, float b, float c, float d) {
        currentGraphics.clip(a, b, c, d);
    }

    public void blendMode(int mode) {
        currentGraphics.blendMode(mode);
    }

    public void rectMode(int mode) {
        currentGraphics.rectMode(mode);
    }

    public void rect(float a, float b, float c, float d) {
        currentGraphics.rect(a, b, c, d);
    }

    public void rect(float a, float b, float c, float d, float r) {
        currentGraphics.rect(a, b, c, d, r);
    }

    public void rect(float a, float b, float c, float d, float tl, float tr, float br, float bl) {
        currentGraphics.rect(a, b, c, d, tl, tr, br, bl);
    }

    public void ellipseMode(int mode) {
        currentGraphics.ellipseMode(mode);
    }

    public void ellipse(float a, float b, float c, float d) {
        currentGraphics.ellipse(a, b, c, d);
    }

    public void arc(float a, float b, float c, float d, float start, float stop) {
        currentGraphics.arc(a, b, c, d, start, stop);
    }

    public void arc(float a, float b, float c, float d, float start, float stop, int mode) {
        currentGraphics.arc(a, b, c, d, start, stop, mode);
    }

    public void box(float size) {
        currentGraphics.box(size);
    }

    public void sphereDetail(int res) {
        currentGraphics.sphereDetail(res);
    }

    public void sphereDetail(int ures, int vres) {
        currentGraphics.sphereDetail(ures, vres);
    }

    public float bezierPoint(float a, float b, float c, float d, float t) {
        return currentGraphics.bezierPoint(a, b, c, d, t);
    }

    public float bezierTangent(float a, float b, float c, float d, float t) {
        return currentGraphics.bezierTangent(a, b, c, d, t);
    }

    public void bezierDetail(int detail) {
        currentGraphics.bezierDetail(detail);
    }

    public void bezier(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        currentGraphics.bezier(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void bezier(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        currentGraphics.bezier(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
    }

    public float curvePoint(float a, float b, float c, float d, float t) {
        return currentGraphics.curvePoint(a, b, c, d, t);
    }

    public float curveTangent(float a, float b, float c, float d, float t) {
        return currentGraphics.curveTangent(a, b, c, d, t);
    }

    public void curveDetail(int detail) {
        currentGraphics.curveDetail(detail);
    }

    public void curveTightness(float tightness) {
        currentGraphics.curveTightness(tightness);
    }

    public void curve(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        currentGraphics.curve(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void curve(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        currentGraphics.curve(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
    }

    public void smooth() {
        currentGraphics.smooth();
    }

    public void smooth(int quality) {
        currentGraphics.smooth(quality);
    }

    public void noSmooth() {
        currentGraphics.noSmooth();
    }

    public void imageMode(int mode) {
        currentGraphics.imageMode(mode);
    }

    public void image(PImage img, float a, float b) {
        currentGraphics.image(img, a, b);
    }

    public void image(PImage img, float a, float b, float c, float d) {
        currentGraphics.image(img, a, b, c, d);
    }

    public void image(PImage img, float a, float b, float c, float d, int u1, int v1, int u2, int v2) {
        currentGraphics.image(img, a, b, c, d, u1, v1, u2, v2);
    }

    public void shapeMode(int mode) {
        currentGraphics.shapeMode(mode);
    }

    public void shape(PShape shape) {
        currentGraphics.shape(shape);
    }

    public void shape(PShape shape, float x, float y) {
        currentGraphics.shape(shape, x, y);
    }

    public void shape(PShape shape, float a, float b, float c, float d) {
        currentGraphics.shape(shape, a, b, c, d);
    }

    public void textAlign(int alignX) {
        currentGraphics.textAlign(alignX);
    }

    public void textAlign(int alignX, int alignY) {
        currentGraphics.textAlign(alignX, alignY);
    }

    public void textFont(PFont which) {
        currentGraphics.textFont(which);
    }

    public void textFont(PFont which, float size) {
        currentGraphics.textFont(which, size);
    }

    public void textLeading(float leading) {
        currentGraphics.textLeading(leading);
    }

    public void textMode(int mode) {
        currentGraphics.textMode(mode);
    }

    public float textWidth(char c) {
        return currentGraphics.textWidth(c);
    }

    public float textWidth(String str) {
        return currentGraphics.textWidth(str);
    }

    public float textWidth(char[] chars, int start, int length) {
        return currentGraphics.textWidth(chars, start, length);
    }

    public void text(char c, float x, float y) {
        currentGraphics.text(c, x, y);
    }

    public void text(char c, float x, float y, float z) {
        currentGraphics.text(c, x, y, z);
    }

    public void text(String str, float x, float y) {
        currentGraphics.text(str, x, y);
    }

    public void text(char[] chars, int start, int stop, float x, float y) {
        currentGraphics.text(chars, start, stop, x, y);
    }

    public void text(String str, float x, float y, float z) {
        currentGraphics.text(str, x, y, z);
    }

    public void text(char[] chars, int start, int stop, float x, float y, float z) {
        currentGraphics.text(chars, start, stop, x, y, z);
    }

    public void text(String str, float x1, float y1, float x2, float y2) {
        currentGraphics.text(str, x1, y1, x2, y2);
    }

    public void text(int num, float x, float y) {
        currentGraphics.text(num, x, y);
    }

    public void text(int num, float x, float y, float z) {
        currentGraphics.text(num, x, y, z);
    }

    public void text(float num, float x, float y) {
        currentGraphics.text(num, x, y);
    }

    public void text(float num, float x, float y, float z) {
        currentGraphics.text(num, x, y, z);
    }

    public void applyMatrix(PMatrix source) {
        currentGraphics.applyMatrix(source);
    }

    public PMatrix2D getMatrix(PMatrix2D target) {
        return currentGraphics.getMatrix(target);
    }

    public void setMatrix(PMatrix source) {
        currentGraphics.setMatrix(source);
    }

    public void pushStyle() {
        currentGraphics.pushStyle();
    }

    public void style(PStyle s) {
        currentGraphics.style(s);
    }

    public PStyle getStyle() {
        return currentGraphics.getStyle();
    }

    public PStyle getStyle(PStyle s) {
        return currentGraphics.getStyle(s);
    }

    public void noStroke() {
        currentGraphics.noStroke();
    }

    public void stroke(int rgb) {
        currentGraphics.stroke(rgb);
    }

    public void stroke(int rgb, float alpha) {
        currentGraphics.stroke(rgb, alpha);
    }

    public void stroke(float gray) {
        currentGraphics.stroke(gray);
    }

    public void stroke(float gray, float alpha) {
        currentGraphics.stroke(gray, alpha);
    }

    public void stroke(float v1, float v2, float v3) {
        currentGraphics.stroke(v1, v2, v3);
    }

    public void stroke(float v1, float v2, float v3, float alpha) {
        currentGraphics.stroke(v1, v2, v3, alpha);
    }

    public void noTint() {
        currentGraphics.noTint();
    }

    public void tint(int rgb) {
        currentGraphics.tint(rgb);
    }

    public void tint(int rgb, float alpha) {
        currentGraphics.tint(rgb, alpha);
    }

    public void tint(float gray) {
        currentGraphics.tint(gray);
    }

    public void tint(float gray, float alpha) {
        currentGraphics.tint(gray, alpha);
    }

    public void tint(float v1, float v2, float v3) {
        currentGraphics.tint(v1, v2, v3);
    }

    public void tint(float v1, float v2, float v3, float alpha) {
        currentGraphics.tint(v1, v2, v3, alpha);
    }

    public void noFill() {
        currentGraphics.noFill();
    }

    public void fill(int rgb) {
        currentGraphics.fill(rgb);
    }

    public void fill(int rgb, float alpha) {
        currentGraphics.fill(rgb, alpha);
    }

    public void fill(float gray) {
        currentGraphics.fill(gray);
    }

    public void fill(float gray, float alpha) {
        currentGraphics.fill(gray, alpha);
    }

    public void fill(float v1, float v2, float v3) {
        currentGraphics.fill(v1, v2, v3);
    }

    public void fill(float v1, float v2, float v3, float alpha) {
        currentGraphics.fill(v1, v2, v3, alpha);
    }

    public void ambient(int rgb) {
        currentGraphics.ambient(rgb);
    }

    public void ambient(float gray) {
        currentGraphics.ambient(gray);
    }

    public void ambient(float v1, float v2, float v3) {
        currentGraphics.ambient(v1, v2, v3);
    }

    public void specular(int rgb) {
        currentGraphics.specular(rgb);
    }

    public void specular(float gray) {
        currentGraphics.specular(gray);
    }

    public void specular(float v1, float v2, float v3) {
        currentGraphics.specular(v1, v2, v3);
    }

    public void shininess(float shine) {
        currentGraphics.shininess(shine);
    }

    public void emissive(int rgb) {
        currentGraphics.emissive(rgb);
    }

    public void emissive(float gray) {
        currentGraphics.emissive(gray);
    }

    public void emissive(float v1, float v2, float v3) {
        currentGraphics.emissive(v1, v2, v3);
    }

    public void background(int rgb) {
        currentGraphics.background(rgb);
    }

    public void background(int rgb, float alpha) {
        currentGraphics.background(rgb, alpha);
    }

    public void background(float gray) {
        currentGraphics.background(gray);
    }

    public void background(float gray, float alpha) {
        currentGraphics.background(gray, alpha);
    }

    public void background(float v1, float v2, float v3) {
        currentGraphics.background(v1, v2, v3);
    }

    public void background(float v1, float v2, float v3, float alpha) {
        currentGraphics.background(v1, v2, v3, alpha);
    }

    public void clear() {
        currentGraphics.clear();
    }

    public void background(PImage image) {
        currentGraphics.background(image);
    }

    public void colorMode(int mode) {
        currentGraphics.colorMode(mode);
    }

    public void colorMode(int mode, float max) {
        currentGraphics.colorMode(mode, max);
    }

    public void colorMode(int mode, float max1, float max2, float max3) {
        currentGraphics.colorMode(mode, max1, max2, max3);
    }

    public void colorMode(int mode, float max1, float max2, float max3, float maxA) {
        currentGraphics.colorMode(mode, max1, max2, max3, maxA);
    }

    public final int color(int c) {
        return currentGraphics.color(c);
    }

    public final int color(float gray) {
        return currentGraphics.color(gray);
    }

    public final int color(int c, int alpha) {
        return currentGraphics.color(c, alpha);
    }

    public final int color(int c, float alpha) {
        return currentGraphics.color(c, alpha);
    }

    public final int color(float gray, float alpha) {
        return currentGraphics.color(gray, alpha);
    }

    public final int color(int v1, int v2, int v3) {
        return currentGraphics.color(v1, v2, v3);
    }

    public final int color(float v1, float v2, float v3) {
        return currentGraphics.color(v1, v2, v3);
    }

    public final int color(int v1, int v2, int v3, int a) {
        return currentGraphics.color(v1, v2, v3, a);
    }

    public final int color(float v1, float v2, float v3, float a) {
        return currentGraphics.color(v1, v2, v3, a);
    }

    public final float alpha(int rgb) {
        return currentGraphics.alpha(rgb);
    }

    public final float red(int rgb) {
        return currentGraphics.red(rgb);
    }

    public final float green(int rgb) {
        return currentGraphics.green(rgb);
    }

    public final float blue(int rgb) {
        return currentGraphics.blue(rgb);
    }

    public final float hue(int rgb) {
        return currentGraphics.hue(rgb);
    }

    public final float saturation(int rgb) {
        return currentGraphics.saturation(rgb);
    }

    public final float brightness(int rgb) {
        return currentGraphics.brightness(rgb);
    }

    public int lerpColor(int c1, int c2, float amt) {
        return currentGraphics.lerpColor(c1, c2, amt);
    }

    public static int lerpColor(int c1, int c2, float amt, int mode) {
        return PGraphics.lerpColor(c1, c2, amt, mode);
    }

    public void beginRaw(PGraphics rawGraphics) {
        currentGraphics.beginRaw(rawGraphics);
    }

    public void endRaw() {
        currentGraphics.endRaw();
    }

    public boolean haveRaw() {
        return currentGraphics.haveRaw();
    }

    public PGraphics getRaw() {
        return currentGraphics.getRaw();
    }

    public static void showWarning(String msg) {
        PGraphics.showWarning(msg);
    }

    public static void showWarning(String msg, Object... args) {
        PGraphics.showWarning(msg, args);
    }

    public static void showDepthWarning(String method) {
        PGraphics.showDepthWarning(method);
    }

    public static void showDepthWarningXYZ(String method) {
        PGraphics.showDepthWarningXYZ(method);
    }

    public static void showMethodWarning(String method) {
        PGraphics.showMethodWarning(method);
    }

    public static void showVariationWarning(String str) {
        PGraphics.showVariationWarning(str);
    }

    public static void showMissingWarning(String method) {
        PGraphics.showMissingWarning(method);
    }

    public static void showException(String msg) {
        PGraphics.showException(msg);
    }

    public boolean displayable() {
        return currentGraphics.displayable();
    }

    public boolean is2D() {
        return currentGraphics.is2D();
    }

    public boolean is3D() {
        return currentGraphics.is3D();
    }

    public boolean is2X() {
        return currentGraphics.is2X();
    }

    public void init(int width, int height, int format) {
        currentGraphics.init(width, height, format);
    }

    public void init(int width, int height, int format, int factor) {
        currentGraphics.init(width, height, format, factor);
    }

    public Image getImage() {
        return currentGraphics.getImage();
    }

    public Object getNative() {
        return currentGraphics.getNative();
    }

    public boolean isModified() {
        return currentGraphics.isModified();
    }

    public void setModified() {
        currentGraphics.setModified();
    }

    public void setModified(boolean m) {
        currentGraphics.setModified(m);
    }

    public int getModifiedX1() {
        return currentGraphics.getModifiedX1();
    }

    public int getModifiedX2() {
        return currentGraphics.getModifiedX2();
    }

    public int getModifiedY1() {
        return currentGraphics.getModifiedY1();
    }

    public int getModifiedY2() {
        return currentGraphics.getModifiedY2();
    }

    public void updatePixels() {
        currentGraphics.updatePixels();
    }

    public void updatePixels(int x, int y, int w, int h) {
        currentGraphics.updatePixels(x, y, w, h);
    }

    public Object clone() throws CloneNotSupportedException {
        return currentGraphics.clone();
    }

    public boolean isLoaded() {
        return currentGraphics.isLoaded();
    }

    public void setLoaded() {
        currentGraphics.setLoaded();
    }

    public void setLoaded(boolean l) {
        currentGraphics.setLoaded(l);
    }

    public PImage get(int x, int y, int w, int h) {
        return currentGraphics.get(x, y, w, h);
    }

    public PImage get() {
        return currentGraphics.get();
    }

    public PImage copy() {
        return currentGraphics.copy();
    }

    public void set(int x, int y, PImage img) {
        currentGraphics.set(x, y, img);
    }

    public void mask(int[] maskArray) {
        currentGraphics.mask(maskArray);
    }

    public static int blendColor(int c1, int c2, int mode) {
        return PImage.blendColor(c1, c2, mode);
    }

    public void blend(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, int mode) {
        currentGraphics.blend(sx, sy, sw, sh, dx, dy, dw, dh, mode);
    }

    public void blend(PImage src, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, int mode) {
        currentGraphics.blend(src, sx, sy, sw, sh, dx, dy, dw, dh, mode);
    }

}
