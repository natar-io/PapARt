/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import com.googlecode.javacv.cpp.opencv_core;
import java.nio.ByteBuffer;
import processing.core.PApplet;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class KinectOpenCV extends Kinect {

    public opencv_core.IplImage validPointsIpl;
    public byte[] validPointsRaw;

    public KinectOpenCV(PApplet parent, String calibIR, String calibRGB, int id, int mode) {
        super(parent, calibIR, calibRGB, id, mode);
    }

    protected void init() {
        super.init();
        validPointsIpl = opencv_core.IplImage.create(
                new opencv_core.CvSize(
                        kinectCalibIR.getWidth(),
                        kinectCalibIR.getHeight()),
                opencv_core.IPL_DEPTH_8U, 3);
        validPointsRaw = new byte[kinectCalibIR.getWidth() * kinectCalibIR.getHeight() * 3];
    }
    
     public opencv_core.IplImage update(opencv_core.IplImage depth, opencv_core.IplImage color) {
        return update(depth, color, 1);
    }

    public opencv_core.IplImage update(opencv_core.IplImage depth, opencv_core.IplImage color, int skip) {

        this.currentSkip = skip;

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();

        depthBuff.get(depthRaw);
        outputBuff.get(validPointsRaw);
        colorBuff.get(colorRaw);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;
                int outputOffset = offset * 3;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                if (d >= 2047) {
                    depthData.validPointsMask[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                depthData.validPointsMask[offset] = false;
                validPointsRaw[outputOffset + 2] = 0;
                validPointsRaw[outputOffset + 1] = 0;
                validPointsRaw[outputOffset + 0] = 0;

                if (isGoodDepth(d)) {
                    depthData.validPointsMask[offset] = true;

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);
                    depthData.kinectPoints[offset] = p;

                    int colorOffset = this.findColorOffset(p) * 3;

//                    int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
//                            | (colorRaw[colorOffset + 1] & 0xFF) << 8
//                            | (colorRaw[colorOffset + 0] & 0xFF);
//                    validPointsPImage.pixels[offset] = c;
                    validPointsRaw[outputOffset + 2] = colorRaw[colorOffset + 2];
                    validPointsRaw[outputOffset + 1] = colorRaw[colorOffset + 1];
                    validPointsRaw[outputOffset + 0] = colorRaw[colorOffset + 0];

//                    int colorOffset = offset * 3;
//                    validPointsRaw[outputOffset + 2] = colorRaw[colorOffset + 2];
//                    validPointsRaw[outputOffset + 1] = colorRaw[colorOffset + 1];
//                    validPointsRaw[outputOffset + 0] = colorRaw[colorOffset + 0];
                }

            }
        }

        outputBuff = (ByteBuffer) outputBuff.rewind();
        outputBuff.put(validPointsRaw);

        return validPointsIpl;
    }

    
    public opencv_core.IplImage getDepthColorIpl() {
        return validPointsIpl;
    }

}
