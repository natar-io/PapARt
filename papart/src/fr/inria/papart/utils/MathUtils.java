/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.utils;

import fr.inria.papart.depthcam.PixelOffset;
import fr.inria.papart.depthcam.analysis.DepthAnalysisPImageView;
import fr.inria.papart.procam.PaperTouchScreen;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.tracking.DetectedMarker;
import fr.inria.papart.tracking.MarkerBoard;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.bytedeco.javacpp.opencv_core;
import processing.core.PApplet;
import static processing.core.PApplet.abs;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jérémy Laviole
 */
public class MathUtils {

    // ---- Vector Utility ---- 
    /**
     * Vec3D to PVector
     *
     * @param p
     * @return
     */
    public static PVector toPVector(Vec3D p) {
        return new PVector(p.x, p.y, p.z);
    }

    /**
     * PVector to Vec3D
     *
     * @param p
     * @return
     */
    public static Vec3D toVec(PVector p) {
        return new Vec3D(p.x, p.y, p.z);
    }

    // ---- Matrix Utility ----
    // TODO: finish this, find another source...
    //   
    /**
     * *
     * Get the Rotations, in the Unity3D format. [WARNING] Work in progress.
     * http://planning.cs.uiuc.edu/node103.html
     *
     * @param mat
     * @return
     */
    public static PVector getRotations(PMatrix3D mat) {
        PVector r = new PVector();
        r.z = PApplet.atan(mat.m10 / mat.m00);
        r.y = PApplet.atan(-mat.m21 / PApplet.sqrt(mat.m21 * mat.m21 + mat.m22 * mat.m22));
        r.x = PApplet.atan(-mat.m21 / PApplet.sqrt(mat.m21 * mat.m21 + mat.m22 * mat.m22));
        return null;
    }

    /**
     * Add two matrices, src += toAdd
     *
     * @param src
     * @param toAdd
     */
    public static void addPMatrix3D(PMatrix3D src, PMatrix3D toAdd) {
        src.m00 += toAdd.m00;
        src.m01 += toAdd.m01;
        src.m02 += toAdd.m02;
        src.m03 += toAdd.m03;
        src.m10 += toAdd.m10;
        src.m11 += toAdd.m11;
        src.m12 += toAdd.m12;
        src.m13 += toAdd.m13;
        src.m20 += toAdd.m20;
        src.m21 += toAdd.m21;
        src.m22 += toAdd.m22;
        src.m23 += toAdd.m23;
        src.m30 += toAdd.m30;
        src.m31 += toAdd.m31;
        src.m32 += toAdd.m32;
        src.m33 += toAdd.m33;
    }

    /**
     * Multiply a vector by a Matrix.
     *
     * @param mat source matrix
     * @param source vector to multiply
     * @param target output, will be created if null.
     * @return
     */
    public static PVector mult(PMatrix3D mat, PVector source, PVector target) {
        if (target == null) {
            target = new PVector();
        }
        target.x = mat.m00 * source.x + mat.m01 * source.y + mat.m02 * source.z + mat.m03;
        target.y = mat.m10 * source.x + mat.m11 * source.y + mat.m12 * source.z + mat.m13;
        target.z = mat.m20 * source.x + mat.m21 * source.y + mat.m22 * source.z + mat.m23;
        float tw = mat.m30 * source.x + mat.m31 * source.y + mat.m32 * source.z + mat.m33;
        if (tw != 0 && tw != 1) {
            target.div(tw);
        }
        return target;
    }

    /**
     * Load a PMatrix3D from a file. Really simple file format, using
     * loadStrings.
     *
     * @param pa
     * @param filename
     * @return
     * @throws FileNotFoundException
     */
    public static PMatrix3D loadPMatrix3D(PApplet pa, String filename) throws FileNotFoundException {
        String[] lines = pa.loadStrings(filename);
        if (lines == null) {
            throw new FileNotFoundException(filename);
        }
        PMatrix3D mat = new PMatrix3D(Float.parseFloat(lines[0]), Float.parseFloat(lines[1]), Float.parseFloat(lines[2]), Float.parseFloat(lines[3]), Float.parseFloat(lines[4]), Float.parseFloat(lines[5]), Float.parseFloat(lines[6]), Float.parseFloat(lines[7]), Float.parseFloat(lines[8]), Float.parseFloat(lines[9]), Float.parseFloat(lines[10]), Float.parseFloat(lines[11]), Float.parseFloat(lines[12]), Float.parseFloat(lines[13]), Float.parseFloat(lines[14]), Float.parseFloat(lines[15]));
        return mat;
    }

    // TODO:  throws ...
    /**
     * Save a PMatrix3D to a file. Really simple file format, using saveStrings.
     *
     * @param pa
     * @param mat
     * @param filename
     */
    public static void savePMatrix3D(PApplet pa, PMatrix3D mat, String filename) {
        String[] lines = new String[16];
        lines[0] = Float.toString(mat.m00);
        lines[1] = Float.toString(mat.m01);
        lines[2] = Float.toString(mat.m02);
        lines[3] = Float.toString(mat.m03);
        lines[4] = Float.toString(mat.m10);
        lines[5] = Float.toString(mat.m11);
        lines[6] = Float.toString(mat.m12);
        lines[7] = Float.toString(mat.m13);
        lines[8] = Float.toString(mat.m20);
        lines[9] = Float.toString(mat.m21);
        lines[10] = Float.toString(mat.m22);
        lines[11] = Float.toString(mat.m23);
        lines[12] = Float.toString(mat.m30);
        lines[13] = Float.toString(mat.m31);
        lines[14] = Float.toString(mat.m32);
        lines[15] = Float.toString(mat.m33);
        pa.saveStrings(filename, lines);
    }

    /**
     * Multiply each element of a PMatrix3D by the scale. mat.m00 *= scale and
     * so on...
     *
     * @param mat
     * @param scale
     */
    public static void scaleMat(PMatrix3D mat, float scale) {
        mat.m00 *= scale;
        mat.m01 *= scale;
        mat.m02 *= scale;
        mat.m03 *= scale;
        mat.m10 *= scale;
        mat.m11 *= scale;
        mat.m12 *= scale;
        mat.m13 *= scale;
        mat.m20 *= scale;
        mat.m21 *= scale;
        mat.m22 *= scale;
        mat.m23 *= scale;
        mat.m30 *= scale;
        mat.m31 *= scale;
        mat.m32 *= scale;
        mat.m33 *= scale;
    }

    // ---- Marker Utility ---- 
    public static PMatrix3D compute3DPos(DetectedMarker detected, float markerWidth, Camera camera) {
        // We create a pair model ( markersFromSVG) -> observation (markers) 

        if (detected.confidence < 1.0) {
            return null;
        }
        // Build object corners
        PVector[] object = new PVector[4];
        object[0] = new PVector(0, 0);
        object[1] = new PVector(markerWidth, 0);

        object[2] = new PVector(markerWidth, -markerWidth);
        object[3] = new PVector(0, -markerWidth);

        PVector[] image = detected.getCorners();

        ProjectiveDeviceP pdp = camera.getProjectiveDevice();
        return pdp.estimateOrientation(object, image);
//        return pdp.estimateOrientationRansac(objectArray, imageArray);
    }
    
    // ---- Color Utility ----
    /**
     * Get a pixel from a PImage.
     *
     * @param img incoming image.
     * @param x image space coordinate.
     * @param y image space coordinate.
     * @param RGB true if RGB, false if BGR
     * @return
     */
    public static int getColor(opencv_core.IplImage img, int x, int y, boolean RGB) {
        if (img.nChannels() == 3) {
            ByteBuffer buff = img.getByteBuffer();
            int offset = (img.width() * y + x) * 3;
            if (RGB) {
                return (buff.get(offset) & 255) << 16 | (buff.get(offset + 1) & 255) << 8 | (buff.get(offset + 2) & 255);
            } else {
                return (buff.get(offset + 2) & 255) << 16 | (buff.get(offset + 1) & 255) << 8 | (buff.get(offset) & 255);
            }
        }
        // Operation not supported
        return 0;
    }

    /**
     * RGB distance of two colors. Return true if all channels differences are
     * below the difference threshold.
     *
     * @param c1
     * @param c2
     * @param threshold
     * @return
     */
    public static boolean colorDistRGB(int c1, int c2, int threshold) {
        int r1 = c1 >> 16 & 255;
        int g1 = c1 >> 8 & 255;
        int b1 = c1 >> 0 & 255;
        int r2 = c2 >> 16 & 255;
        int g2 = c2 >> 8 & 255;
        int b2 = c2 >> 0 & 255;
        int dr = PApplet.abs(r1 - r2);
        int dg = PApplet.abs(g1 - g2);
        int db = PApplet.abs(b1 - b2);
        return dr < threshold && dg < threshold && db < threshold;
    }
    /**
     * RGB distance of two colors. Return true if all channels differences are
     * below the difference threshold.
     *
     * @param c1
     * @param c2
     * @param tr
     * @param tg
     * @param tb
     * @return
     */
    public static boolean colorDistRGB(int c1, int c2, int tr, int tg,int tb) {
        int r1 = c1 >> 16 & 255;
        int g1 = c1 >> 8 & 255;
        int b1 = c1 >> 0 & 255;
        int r2 = c2 >> 16 & 255;
        int g2 = c2 >> 8 & 255;
        int b2 = c2 >> 0 & 255;
        int dr = PApplet.abs(r1 - r2);
        int dg = PApplet.abs(g1 - g2);
        int db = PApplet.abs(b1 - b2);
        return dr < tr && dg < tg && db < tb;
    }

    /**
     * RGB distance of two colors. Return true if all channels differences are
     * below the difference threshold.
     *
     * @param c1
     * @param c2
     * @param threshold
     * @return
     */
    public static boolean colorDistRGBAverage(int c1, int c2, int threshold) {
        int r1 = c1 >> 16 & 255;
        int g1 = c1 >> 8 & 255;
        int b1 = c1 >> 0 & 255;
        int r2 = c2 >> 16 & 255;
        int g2 = c2 >> 8 & 255;
        int b2 = c2 >> 0 & 255;
        int dr = PApplet.abs(r1 - r2);
        int dg = PApplet.abs(g1 - g2);
        int db = PApplet.abs(b1 - b2);
        return dr + dg + db < threshold;
    }

    /**
     * Color distance on the HSB scale. The incomingPix is compared with the
     * baseline. The method returns true if each channel validates the condition
     * for the given threshold.
     *
     * @param g
     * @param baseline
     * @param incomingPix
     * @param hueTresh
     * @param saturationTresh
     * @param brightnessTresh
     * @return
     */
    public static boolean colorDistHSB(PGraphics g, int baseline, int incomingPix,
            float hueTresh, float saturationTresh, float brightnessTresh) {
        float h1 = g.hue(baseline);
        float h2 = g.hue(incomingPix);

        return abs(h1 - h2) < hueTresh
                && // Avoid desaturated pixels
                abs(g.saturation(incomingPix) - g.saturation(baseline)) > saturationTresh
                && // avoid pixels not bright enough
                abs(g.brightness(incomingPix) - g.brightness(baseline)) > brightnessTresh;
    }

    /**
     * Color distance on the HSB scale. The incomingPix is compared with the
     * baseline. The method returns true if each channel validates the condition
     * for the given threshold.
     *
     * @param g
     * @param baseline
     * @param incomingPix
     * @param hueTresh
     * @param saturationTresh
     * @param brightnessTresh
     * @return
     */
    public static boolean colorFinderHSB(PGraphics g, int baseline, int incomingPix,
            float hueTresh, float saturationTresh, float brightnessTresh) {
        float h1 = g.hue(baseline);
        float h2 = g.hue(incomingPix);

        return abs(h1 - h2) < hueTresh
                && // Avoid desaturated pixels
                abs(g.saturation(incomingPix) - g.saturation(baseline)) < saturationTresh
                && // avoid pixels not bright enough
                abs(g.brightness(incomingPix) - g.brightness(baseline)) < brightnessTresh;
    }
    
    public static boolean colorFinderHSBRedish(PGraphics g, int baseline, int incomingPix,
            float hueTresh, float saturationTresh, float brightnessTresh) {
        float h1 = g.hue(baseline);
        float h2 = g.hue(incomingPix);
        
        // If the hue is low but still red, shit it.
        if(h2 < 30){
            h2 = h2 + 255f;
        }

        return abs(h1 - h2) < hueTresh
                && // Avoid desaturated pixels
                abs(g.saturation(incomingPix) - g.saturation(baseline)) < saturationTresh
                && // avoid pixels not bright enough
                abs(g.brightness(incomingPix) - g.brightness(baseline)) < brightnessTresh;
    }

    public static boolean threshold(PGraphics g, int incomingPix, float threshold) {
        int r1 = incomingPix >> 16 & 255;
        return r1 > threshold;
    }
    public static boolean isRed(PGraphics g, int incomingPix, int baseline, float threshold) {
        int r1 = incomingPix >> 16 & 255;
        int r2 = baseline >> 16 & 255;
        return abs(r1 - r2) < threshold;
    }

    public static boolean isGreen(PGraphics g, int incomingPix, int baseline, int threshold) {
        int r1 = incomingPix >> 8 & 255;
        int r2 = baseline >> 8 & 255;
        return abs(r1 - r2) < threshold;
    }

    public static boolean isBlue(PGraphics g, int incomingPix, int baseline, float threshold) {
        int r1 = incomingPix >> 0 & 255;
        int r2 = baseline >> 0 & 255;
        return abs(r1 - r2) < threshold;
    }

    public static boolean isBlack(PGraphics g, int incomingPix,
            float brightnessTresh) {
        return colorDistRGBAverage(incomingPix, 0, (int) brightnessTresh);
    }
//    
//    public static boolean isBlack(PGraphics g, int incomingPix,
//             float brightnessTresh) {
//       return g.brightness(incomingPix) < brightnessTresh;
//    }

    public static boolean isWhite(PGraphics g, int incomingPix,
            float brightnessTresh) {
        return g.brightness(incomingPix) > brightnessTresh;
    }

    /**
     * Warning here, the threshold is the same for each channel. Often the range
     * depends on the declaration. To change the intensity of each, try to call
     * HSB(100, 100, 100) for even thresholding or HSB(100, 200, 50) to be less
     * picky on the brigthness and more picky on the saturation.
     *
     * @param g
     * @param baseline
     * @param incomingPix
     * @param threshold
     * @return
     */
    public static boolean colorDistHSBAutoThresh(PGraphics g, int baseline, int incomingPix,
            float threshold) {
        float h1 = g.hue(baseline);
        float h2 = g.hue(incomingPix);

        float d1 = abs(h1 - h2);
        float d2 = abs(g.saturation(baseline) - g.saturation(incomingPix));
        float d3 = abs(g.brightness(baseline) - g.brightness(incomingPix));

        return (d1 + d2 + d3) < (threshold * 3);
    }

    /**
     * Unsafe do not use unless you are sure.
     */
    public static int getColorOccurencesFrom(Camera camera, PVector coord, int radius, int col, int threshold, PaperTouchScreen paperTouchScreen) {
        int x = (int) coord.x;
        int y = (int) coord.y;
        int minX = PApplet.constrain(x - radius, 0, camera.width() - 1);
        int maxX = PApplet.constrain(x + radius, 0, camera.width() - 1);
        int minY = PApplet.constrain(y - radius, 0, camera.height() - 1);
        int maxY = PApplet.constrain(y + radius, 0, camera.height() - 1);
        ByteBuffer buff = camera.getIplImage().getByteBuffer();
        int k = 0;
        for (int j = minY; j <= maxY; j++) {
            for (int i = minX; i <= maxX; i++) {
                int offset = i + j * camera.width();
                int pxCol = getColor(buff, offset);
                if (MathUtils.colorDistRGB(col, pxCol, threshold)) {
                    k++;
                }
            }
        }
        return k;
    }

    private static int getColor(ByteBuffer buff, int offset) {
        offset = offset * 3;
        return (buff.get(offset + 2) & 255) << 16 | (buff.get(offset + 1) & 255) << 8 | (buff.get(offset) & 255);
    }

    /**
     * Unsafe do not use unless you are sure.
     */
    public int getColorOccurencesFrom(PVector coord, PImage cameraImage, int radius, int col, int threshold, PaperTouchScreen paperTouchScreen) {
        int x = (int) coord.x;
        int y = (int) coord.y;
        int minX = PApplet.constrain(x - radius, 0, cameraImage.width - 1);
        int maxX = PApplet.constrain(x + radius, 0, cameraImage.width - 1);
        int minY = PApplet.constrain(y - radius, 0, cameraImage.height - 1);
        int maxY = PApplet.constrain(y + radius, 0, cameraImage.height - 1);
        int k = 0;
        for (int j = minY; j <= maxY; j++) {
            for (int i = minX; i <= maxX; i++) {
                int offset = i + j * cameraImage.width;
                int pxCol = cameraImage.pixels[offset];
                if (colorDistRGB(col, pxCol, threshold)) {
                    k++;
                }
            }
        }
        return k;
    }

    /**
     * Do a simple erosion.
     *
     * @param validList will be updated after erosion
     * @param arrayToErode eroded array.
     * @param buffer temporary buffer, same size of the arrayToErode.
     * @param skip quality skip 1/skip values.
     * @param invalidValue value to fill the invalid states
     * @param size size of the arrayToErode
     */
    public static void erodePoints2(HashSet<Integer> validList,
            byte[] arrayToErode,
            byte[] buffer,
            int skip, byte invalidValue,
            WithSize size) {

        Arrays.fill(buffer, invalidValue);

        ArrayList<Integer> toRemove = new ArrayList<>();

        for (Integer idx : validList) {
            int sum = 0;
            int x = idx % size.getWidth();
            int y = idx / size.getWidth();

            byte value = arrayToErode[idx];

//            for (int j = y * size.getWidth() - skip; j <= y * size.getWidth() + skip; j += size.getWidth() * skip) {
//                for (int i = x - skip; i <= x + skip; i += skip) {
            for (int j = -skip; j <= skip; j += skip) {
                for (int i = -skip; i <= skip; i += skip) {

                    if (x + i >= 0 && x + i < size.getWidth()
                            && j + y >= 0 && j + y < size.getHeight()) {
                        int currentIdx = (x + i) + (j + y) * size.getWidth();
                        sum += arrayToErode[currentIdx] == value ? 1 : 0;
                    }
                }
            }

            if (sum <= 8) {
                buffer[idx] = invalidValue;
                toRemove.add(idx);
            } else {
                buffer[idx] = value;
            }
        }
        validList.removeAll(toRemove);
        System.arraycopy(buffer, 0, arrayToErode, 0, arrayToErode.length);
        //        erodePoints(depthData.validPointsMask);
    }

    public void erodePoints2(ArrayList<Integer> validList,
            boolean[] arrayToErode,
            boolean[] buffer,
            int skip,
            WithSize size) {

        Arrays.fill(buffer, false);

        for (Integer idx : validList) {
            PixelOffset po = PixelOffset.get(idx);
            int sum = 0;
            int x = po.x;
            int y = po.y;
            for (int j = y * size.getWidth() - skip; j <= y * size.getWidth() + skip; j += size.getWidth() * skip) {
                for (int i = x - skip; i <= x + skip; i += skip) {
                    int currentIdx = i + j;
                    sum += arrayToErode[currentIdx] ? 1 : 0;
                }
            }
            buffer[idx] = sum >= 3;
        }
        System.arraycopy(buffer, 0, arrayToErode, 0, arrayToErode.length);
        //        erodePoints(depthData.validPointsMask);
    }

}
