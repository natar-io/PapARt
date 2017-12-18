/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2016 Jérémy Laviole
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
package fr.inria.papart.depthcam.analysis;

import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.PixelOffset;
import fr.inria.papart.depthcam.TouchAttributes;
import fr.inria.papart.depthcam.devices.Kinect360;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.depthcam.devices.KinectOne;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.INVALID_POINT;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.papplet;
import fr.inria.papart.depthcam.devices.Kinect360Depth;
import fr.inria.papart.depthcam.devices.KinectOneDepth;
import fr.inria.papart.depthcam.devices.RealSense;
import fr.inria.papart.depthcam.devices.RealSenseDepth;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.utils.MathUtils;
import fr.inria.papart.utils.ARToolkitPlusUtils;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraRealSense;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class DepthAnalysisImpl extends DepthAnalysis {

    // Configuration 
    private float closeThreshold = 300f, farThreshold = 12000f;
    protected ProjectiveDeviceP calibDepth, calibColor;

    // private variables 
    // Raw data from the Kinect Sensor
    protected ShortBuffer depthRawShortBuffer;
    protected ByteBuffer depthRawBuffer;
    protected byte[] colorRaw;
    protected float[] depth;

    // static values
    public static final float INVALID_DEPTH = -1;

    protected DepthCameraDevice depthCameraDevice;
    protected Camera colorCamera;

    public DepthCameraDevice getDepthCameraDevice() {
        return this.depthCameraDevice;
    }

    @Override
    public int getSize() {
        return getWidth() * getHeight();
    }

    @Override
    public int getWidth() {
        return getDepthCameraDevice().getDepthCamera().width();
    }

    @Override
    public int getHeight() {
        return getDepthCameraDevice().getDepthCamera().height();
    }

    public int getColorWidth() {
        return colorCamera.width();
    }

    public int getColorHeight() {
        return colorCamera.height();
    }

    public int getColorSize() {
        return getColorWidth() * getColorHeight();
    }

    private boolean memoryInitialized = false;

    public DepthAnalysisImpl(PApplet parent, DepthCameraDevice depthCamera) {
        DepthAnalysis.papplet = parent;
        this.depthCameraDevice = depthCamera;

        // DepthCamera sizes should be good here...   (says TouchVisu)
        // why not ?   (calib?)
        //-----
//        initMemory();
        initWithCalibrations(depthCamera);
        // initThreadPool();
    }

    public void initWithCalibrations(DepthCameraDevice depthCamera) {
        depthCameraDevice = depthCamera;
        depthComputationMethod = depthCameraDevice.createDepthComputation();
        
        if (depthCamera.getMainCamera().isUseIR()) {
            colorCamera = depthCamera.getIRCamera();
        }
        if (depthCamera.getMainCamera().isUseColor()) {
            colorCamera = depthCamera.getColorCamera();
        }

        calibColor = colorCamera.getProjectiveDevice();
        calibDepth = depthCamera.getDepthCamera().getProjectiveDevice();

        initMemory();
    }

    // Thread version... No bonus whatsoever for now.
    private int nbThreads = 16;
    private ExecutorService threadPool;

    private void initThreadPool() {
        threadPool = Executors.newFixedThreadPool(nbThreads);
    }

    private void initMemory() {
//        System.out.println("Allocations: " + getColorSize() + " " + depthCameraDevice.rawDepthSize());

        if (depthCameraDevice.getMainCamera().isPixelFormatGray()) {
            colorRaw = new byte[getColorSize()];
        } else {
            colorRaw = new byte[getColorSize() * 3];
        }
        depth = new float[depthCameraDevice.getDepthCamera().width() * depthCameraDevice.getDepthCamera().height()];

        depthData = new ProjectedDepthData(this);
        depthData.projectiveDevice = this.calibDepth;
        System.out.println("ColorRaw initialized !" + colorRaw.length);

        PixelOffset.initStaticMode(getWidth(), getHeight());
    }

    @Deprecated
    private void setDepthMethod() {
        
        // Replaced by this line:
        depthComputationMethod = depthCameraDevice.createDepthComputation();
        
        // Old code -- to test before deletion
//        if (depthCameraDevice instanceof Kinect360) {
//            depthComputationMethod = ((Kinect360) depthCameraDevice).new Kinect360Depth();
//        }
//        if (depthCameraDevice instanceof KinectOne) {
//            depthComputationMethod = ((KinectOne) depthCameraDevice).new KinectOneDepth();
//        }
//        if (depthCameraDevice instanceof RealSense) {
//            RealSense rs = ((RealSense) depthCameraDevice);
//            if (((CameraRealSense) ((RealSense) depthCameraDevice).getMainCamera()).isStarted()) {
//                depthComputationMethod = rs.new RealSenseDepth();
//            }
//        }
//        if (depthCameraDevice instanceof RealSense) {
//            if (((CameraRealSense) ((RealSense) depthCameraDevice).getMainCamera()).isStarted()) {
//                float depthScale = ((CameraRealSense) ((RealSense) depthCameraDevice).getMainCamera()).getDepthScale();
//                depthComputationMethod = new RealSenseDepth(depthScale);
//            }
//        }
    }


    public void computeDepthAndNormals(opencv_core.IplImage depth, opencv_core.IplImage color, int skip2D) {
        updateRawDepth(depth);
        // optimisation no Color. 
        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();

        depthData.connexity.setPrecision(skip2D);

        computeDepthAndDo(skip2D, new ComputeNormal());

        if (this.colorCamera.getPixelFormat() == Camera.PixelFormat.GRAY) {
            doForEachPoint(skip2D, new SetImageDataGRAY());
        }
        if (this.colorCamera.getPixelFormat() == Camera.PixelFormat.RGB) {
            doForEachPoint(skip2D, new SetImageDataRGB());
        }
        if (this.colorCamera.getPixelFormat() == Camera.PixelFormat.BGR) {
            doForEachPoint(skip2D, new SetImageData());
        }
//        doForEachPoint(skip2D, new ComputeNormal());
    }

    public void computeDepthAndDo(int precision, DepthPointManiplation manip) {
        PixelList pixels = new PixelList(precision);

        for (PixelOffset px : pixels) {
            float d = getDepth(px.offset);
            // Experimental
            depth[px.offset] = d;

            if (d != INVALID_DEPTH) {
                // Compute the depth point.
                calibDepth.pixelToWorld(px.x, px.y, d, depthData.depthPoints[px.offset]);
                manip.execute(depthData.depthPoints[px.offset], px);
            }
        }
    }

    protected void computeDepthAndDo(int precision, DepthPointManiplation manip, InvalidPointManiplation invalidManip) {
        PixelList pixels = new PixelList(precision);

        for (PixelOffset px : pixels) {

            float d = getDepth(px.offset);
            if (d != INVALID_DEPTH) {
//                Vec3D pKinect = calibIR.pixelToWorld(px.x, px.y, d);
//                depthData.depthPoints[px.offset] = pKinect;
//                manip.execute(pKinect, px);

                calibDepth.pixelToWorld(px.x, px.y, d, depthData.depthPoints[px.offset]);
                manip.execute(depthData.depthPoints[px.offset], px);

            } else {
                invalidManip.execute(px);
            }
        }
    }

    protected void doForEachPoint(int precision, DepthPointManiplation manip) {
        if (precision <= 0) {
            return;
        }
        PixelList pixels = new PixelList(precision);

        for (PixelOffset px : pixels) {
            Vec3D pKinect = depthData.depthPoints[px.offset];
            if (pKinect != INVALID_POINT) {
                manip.execute(pKinect, px);
            }

        }
    }

    public void computeDepthAndDoAround(int precision, int offset, int dist, DepthPointManiplation manip) {
        PixelListAroundPoint pixels = new PixelListAroundPoint(precision, offset, dist);
        for (PixelOffset px : pixels) {
            float d = getDepth(px.offset);
            // Experimental
            depth[px.offset] = d;

            if (d != INVALID_DEPTH) {
                // Compute the depth point.
                calibDepth.pixelToWorld(px.x, px.y, d, depthData.depthPoints[px.offset]);
                manip.execute(depthData.depthPoints[px.offset], px);
            }
        }
    }

    protected void doForEachPointAround(int precision, int offset, int dist, DepthPointManiplation manip) {
        if (precision <= 0) {
            return;
        }
        PixelListAroundPoint pixels = new PixelListAroundPoint(precision, offset, dist);

        for (PixelOffset px : pixels) {
            Vec3D pKinect = depthData.depthPoints[px.offset];
            if (pKinect != INVALID_POINT) {
                manip.execute(pKinect, px);
            }

        }
    }

    protected void doForEachValidPoint(int precision, DepthPointManiplation manip,
            DepthData.DepthSelection selection) {
        if (precision <= 0) {
            return;
        }

        PixelList pixels = new PixelList(precision);

        for (PixelOffset px : pixels) {
            Vec3D pKinect = depthData.depthPoints[px.offset];
            if (pKinect != INVALID_POINT && selection.validPointsMask[px.offset] == true) {
                manip.execute(pKinect, px);
            }
        }
    }

    class SetImageData implements DepthPointManiplation {

        public SetImageData() {
            super();
        }

        @Override
        public void execute(Vec3D p, PixelOffset px) {
//            depthData.validPointsMask[px.offset] = true;
            setPixelColor(px.offset);
        }
    }

    class SetImageDataRGB implements DepthPointManiplation {

        public SetImageDataRGB() {
            super();
        }

        @Override
        public void execute(Vec3D p, PixelOffset px) {
//            depthData.validPointsMask[px.offset] = true;
            setPixelColorRGB(px.offset);
        }
    }

    protected class SetImageDataGRAY implements DepthPointManiplation {

        public SetImageDataGRAY() {
            super();
        }

        @Override
        public void execute(Vec3D p, PixelOffset px) {
//            depthData.validPointsMask[px.offset] = true;
            setPixelColorGRAY(px.offset);
        }
    }

    // TODO: Generalization here, same functions as those to convert the pixels for OpenGL. 
    protected int setPixelColor(int offset) {

        // TODO: Get a cleaner way go obtain the color... 
        int colorOffset = depthCameraDevice.findColorOffset(depthData.depthPoints[offset]) * 3;

        int c;
        // Do not set invalid pixels
        if (colorOffset < 0 || colorOffset > colorRaw.length) {
            c = 255;
        } else {
            c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
                    | (colorRaw[colorOffset + 0] & 0xFF);
        }
        depthData.pointColors[offset] = c;
        return c;
    }

    protected int setPixelColorRGB(int offset) {

        // TODO: Get a cleaner way go obtain the color... 
        int colorOffset = depthCameraDevice.findColorOffset(depthData.depthPoints[offset]) * 3;

        int c;
        // Do not set invalid pixels
        if (colorOffset < 0 || colorOffset > colorRaw.length) {
            c = 255;
        } else {

            c = (colorRaw[colorOffset + 0] & 0xFF) << 16
                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
                    | (colorRaw[colorOffset + 2] & 0xFF);
        }
        depthData.pointColors[offset] = c;
        return c;
    }

    protected int setPixelColorGRAY(int offset) {
        int colorOffset = depthCameraDevice.findMainImageOffset(depthData.depthPoints[offset]);

        int c;
        // Do not set invalid pixels
        if (colorOffset < 0 || colorOffset > colorRaw.length) {
            c = 255;
        } else {

            c = (colorRaw[colorOffset + 0] & 0xFF) << 16
                    | (colorRaw[colorOffset + 0] & 0xFF) << 8
                    | (colorRaw[colorOffset + 0] & 0xFF);
        }
        depthData.pointColors[offset] = c;
        return c;
    }

    public PVector findDepthAtRGB(PVector v) {
        return findDepthAtRGB(v.x, v.y, v.z);
    }

    public PVector findDepthAtRGB(float x, float y, float z) {
        PVector v = new PVector(x, y, z);
        PVector v2 = new PVector();

        // Point is now in the Depth coordinates
        depthCameraDevice.getStereoCalibrationInv().mult(v, v2);

        // v2 is now the location in KinectDepth instead of KinectRGB coordinates.
        // Point is now in pixel coordinates 
        int worldToPixel = getDepthCameraDevice().getDepthCamera().getProjectiveDevice().worldToPixel(v2);

        // Point viewed in the depth camera point of view. 
        PVector pointDepth = MathUtils.toPVector(depthData.depthPoints[worldToPixel]);

        return pointDepth;
        // get it back in the RGB point of view.
//        PVector out = new PVector();
//        depthCameraDevice.getStereoCalibration().mult(pointDepth, out);
//        return out;
//        return Utils.toPVector(depthData.depthPoints[worldToPixel]);
    }

    protected void updateRawDepth(opencv_core.IplImage depthImage) {
        
        depthComputationMethod.updateDepth(depthImage);
        
//        // Realsense 
//        if (getDepthCameraDevice().type() == Camera.Type.REALSENSE) {
//            depthRawBuffer = depthImage.getByteBuffer();
//            depthRawShortBuffer = depthRawBuffer.asShortBuffer();
//            
//            depthComputationMethod.setDepthSource(depthRawShortBuffer);
//        // Kinect
//        } else {
//            depthImage.getByteBuffer().get(depthRaw);
//            depthComputationMethod.setDepthSource(depthRaw);
//        }
    }

    protected void updateRawColor(opencv_core.IplImage colorImage) {
        ByteBuffer colBuff = colorImage.getByteBuffer();
        colBuff.get(colorRaw);
    }

    public void setNearFarValue(float near, float far) {
        this.closeThreshold = near;
        this.farThreshold = far;
    }

    /**
     * Experimental Class to filter depth points.
     *
     */
    class TimeFilterDepth implements DepthPointManiplation {

        private final int frameNumber;
//        private final Vec3D[][] depthHistory;
        private final LinkedList<float[]> depthHistory;

        public TimeFilterDepth() {
            this(3);
        }

        public TimeFilterDepth(int frameNumber) {
            this.frameNumber = frameNumber;
            depthHistory = new LinkedList<float[]>();
        }

        private float getAverageValue(Vec3D p, PixelOffset px) {
            int offset = px.offset;
            float average = 0;
            int sumAmount = 0;

            float current = p.z;

            float variance = 0;
//            Vec3D average = p.copy();
//            int sumAmount = 1;
            for (float[] oldValues : depthHistory) {
                float oldValue = oldValues[offset];
                if (oldValue != INVALID_DEPTH) {
                    sumAmount++;
                    average += oldValue;

                    variance += Math.abs(oldValue - current);
                }
            }
            if (sumAmount == 0 || variance / frameNumber > 6) {
                return INVALID_DEPTH;
            }
            average = average / sumAmount;
            return average;
        }

        public void addCurrentDepthPoints(float[] points) {
            float[] memoryPoints;
            if (depthHistory.size() == frameNumber) {
                memoryPoints = depthHistory.removeLast();
            } else {
                memoryPoints = new float[points.length];
            }

            System.arraycopy(points, 0, memoryPoints, 0, points.length);
            depthHistory.addFirst(memoryPoints);
        }

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            depthData.depthPoints[px.offset].z = getAverageValue(p, px);
        }
    }

    /**
     * Experimental Class to filter handle SR300 distorsions.
     *
     */
    class UndistortSR300Depth implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {

            if (p.z > 300) {
//                float depthCoeff = ((p.z) / 200000f) +  1f;
                float xCoeff;
                if (p.x < 0) {
                    xCoeff = (1f - (p.x / 10000f));
                } else {
                    xCoeff = (1f + (p.x / 10000f));
                }
//                if (px.x == px.y) {
//                    System.out.println("coeff: " +xCoeff);
//                }
                depthData.depthPoints[px.offset].z = p.z * xCoeff;

            }
        }
    }

    /**
     * Experimental Class to find hands (old).
     *
     */
//    class SelectPlaneTouchHand implements DepthPointManiplation {
//
//        @Override
//        public void execute(Vec3D p, PixelOffset px) {
//
//            boolean overTouch = depthData.planeAndProjectionCalibration.hasGoodOrientation(p);
//            boolean underTouch = depthData.planeAndProjectionCalibration.isUnderPlane(p);
//            boolean touchSurface = depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p);
//
//            Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
//
//            if (isInside(projected, 0.f, 1.f, 0.0f)) {
//
//                depthData.projectedPoints[px.offset] = projected;
//                depthData.touchAttributes[px.offset] = new TouchAttributes(touchSurface, underTouch, overTouch);
//                depthData.validPointsMask[px.offset] = touchSurface;
//
//                if (touchSurface) {
//                    depthData.validPointsList.add(px.offset);
//                }
//            }
//        }
//    }
    protected int getPixelColor(int offset) {
        int colorOffset = depthCameraDevice.findColorOffset(depthData.depthPoints[offset]) * 3;
        int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                | (colorRaw[colorOffset + 1] & 0xFF) << 8
                | (colorRaw[colorOffset + 0] & 0xFF);
        return c;
    }

    // TODO: array ? or what instead ?
    public class PixelList implements Iterable<PixelOffset> {

        int precision = 1;
        int beginY = 0;
        int endY;

        public PixelList(int precision) {
            this.precision = precision;
            this.beginY = 0;
            this.endY = calibDepth.getHeight();
        }

        /**
         * Begin and end are on Y axis.
         *
         * @param precision
         * @param begin
         * @param end
         */
        public PixelList(int precision, int begin, int end) {
            this.precision = precision;
            this.beginY = begin;
            this.endY = end;
        }

        @Override
        public Iterator<PixelOffset> iterator() {
            Iterator<PixelOffset> it = new Iterator<PixelOffset>() {

                private int x = 0;
                private int y = beginY;
                private int offset = 0;
                private final int width = calibDepth.getWidth();

                @Override
                public boolean hasNext() {
                    return offset < width * endY;
                }

                @Override
                public PixelOffset next() {
                    // no allocation mode -- static
//                    PixelOffset out = new PixelOffset(x, y, offset);

                    PixelOffset out = PixelOffset.get(offset);
                    x += precision;
                    offset += precision;

                    if (x >= width) {
                        x = 0;
                        y += precision;
                        offset = y * width;
                    }

                    return out;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
            return it;
        }
    }

    public class PixelListAroundPoint implements Iterable<PixelOffset> {

        private final int width = calibDepth.getWidth();
        int precision = 1;
        private int beginX;
        private int endX;
        int beginY = 0;
        int endY;

        public PixelListAroundPoint(int precision, int pointOffset, int amount) {
            this.precision = precision;

            this.beginX = (pointOffset % width) - amount * precision;
            if (beginX < 0) {
                beginX = 0;
            }
            this.beginY = (pointOffset / width) - (amount * precision);
            if (beginY < 0) {
                beginY = 0;
            }

            this.endX = (pointOffset % width) + amount * precision;
            if (endX > calibDepth.getWidth() - precision) {
                endX = calibDepth.getWidth() - precision;
            }
            this.endY = (pointOffset / width) + (amount * precision);
            if (endY > calibDepth.getHeight() - precision) {
                endY = calibDepth.getHeight() - precision;
            }

        }

        @Override
        public Iterator<PixelOffset> iterator() {
            Iterator<PixelOffset> it = new Iterator<PixelOffset>() {

                private int x = beginX;
                private int y = beginY;

                private int offset = x + y * width;

                @Override
                public boolean hasNext() {
                    return offset < endX + (width * endY);
                }

                @Override
                public PixelOffset next() {
                    // no allocation mode -- static
//                    PixelOffset out = new PixelOffset(x, y, offset);

                    PixelOffset out = PixelOffset.get(offset);
                    x += precision;
                    offset += precision;

                    if (x >= endX) {
                        x = beginX;
                        y += precision;
                        offset = x + y * width;
                    }

                    return out;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
            return it;
        }
    }

    /**
     * Slower than sequential for now.
     *
     * @param precision
     * @param manip
     */
    protected void computeDepthAndDoParallel(int precision, DepthPointManiplation manip) {
        ArrayList<FutureTask<DepthPixelTask>> tasks = new ArrayList<>();
        for (int i = 0; i < nbThreads; i++) {
            DepthPixelTask depthPixelTask = new DepthPixelTask(i, nbThreads, precision, manip);
            FutureTask<DepthPixelTask> task = new FutureTask<DepthPixelTask>(depthPixelTask);
            threadPool.submit(task);
            tasks.add(task);
        }
        try {
            for (FutureTask<DepthPixelTask> task : tasks) {
                task.get();
            }
        } catch (ExecutionException e) {
        } catch (InterruptedException e) {
        }
    }

    class DepthPixelTask implements Callable {

        private int part;
        private int nbThreads;
        private int precision;
        private DepthPointManiplation manip;

        public DepthPixelTask(int part, int nbThreads, int precision, DepthPointManiplation manip) {
            this.part = part;
            this.nbThreads = nbThreads;
            this.precision = precision;
            this.manip = manip;
        }

        @Override
        public Object call() {

            int nbParts = nbThreads;
            int partSize = nbParts / calibDepth.getHeight();
            int begin = partSize * part;

            int end;
            if (part == nbThreads - 1) {
                end = calibDepth.getHeight();
            } else {
                end = partSize * (part + 1);
            }

            PixelList pixels = new PixelList(precision, begin, end);

            for (PixelOffset px : pixels) {
                float d = getDepth(px.offset);
                if (d != INVALID_DEPTH) {
//                    Vec3D pKinect = calibIR.pixelToWorld(px.x, px.y, d);
//                    depthData.depthPoints[px.offset] = pKinect;

                    calibDepth.pixelToWorld(px.x, px.y, d, depthData.depthPoints[px.offset]);
                    manip.execute(depthData.depthPoints[px.offset], px);
                }
            }
            return null;
        }

    }

    

    public static final float KINECT_ONE_DEPTH_RATIO = 10f;

    


    public void undistortRGB(opencv_core.IplImage rgb, opencv_core.IplImage out) {
        calibColor.getDevice().undistort(rgb, out);
    }

    // Not Working ! 
    /**
     * DO NOT USE - not working (distorsion estimation fail ?).
     *
     * @param ir
     * @param out
     */
    public void undistortIR(opencv_core.IplImage ir, opencv_core.IplImage out) {
        calibDepth.getDevice().undistort(ir, out);
    }

    public ProjectiveDeviceP getColorProjectiveDevice() {
        return calibColor;
    }

    public ProjectiveDeviceP getDepthProjectiveDevice() {
        return calibDepth;
    }

    public byte[] getColorBuffer() {
        return this.colorRaw;
    }

    protected boolean isGoodDepth(float rawDepth) {
        return (rawDepth >= closeThreshold && rawDepth < farThreshold);
    }

}
