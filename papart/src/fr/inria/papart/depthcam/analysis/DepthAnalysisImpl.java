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
import fr.inria.papart.depthcam.PixelOffset;
import fr.inria.papart.depthcam.TouchAttributes;
import fr.inria.papart.depthcam.devices.Kinect360;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.depthcam.devices.KinectOne;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.INVALID_POINT;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.papplet;
import fr.inria.papart.depthcam.devices.RealSense;
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
    protected ProjectiveDeviceP calibDepth, calibRGB;

    // private variables 
    // Raw data from the Kinect Sensor
    protected ShortBuffer depthRawShortBuffer;
    protected ByteBuffer depthRawBuffer;
    protected byte[] depthRaw;
    protected byte[] colorRaw;
    protected float[] depth;

    // static values
    protected static final float INVALID_DEPTH = -1;

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
        updateCalibrations(depthCamera);
        // initThreadPool();
    }

    public void updateCalibrations(DepthCameraDevice depthCamera) {
        depthCameraDevice = depthCamera;
        setDepthMethod();
        if (depthCamera.getMainCamera().isUseIR()) {
            colorCamera = depthCamera.getIRCamera();
        }
        if (depthCamera.getMainCamera().isUseColor()) {
            colorCamera = depthCamera.getColorCamera();
        }

        calibRGB = colorCamera.getProjectiveDevice();
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

        colorRaw = new byte[getColorSize() * 3];
        depthRaw = new byte[depthCameraDevice.rawDepthSize()];
        depth = new float[depthCameraDevice.getDepthCamera().width() * depthCameraDevice.getDepthCamera().height()];

        depthData = new ProjectedDepthData(this);
        depthData.projectiveDevice = this.calibDepth;
        System.out.println("ColorRaw initialized !" + colorRaw.length);

        PixelOffset.initStaticMode(getWidth(), getHeight());
    }

    private void setDepthMethod() {
        if (depthCameraDevice instanceof Kinect360) {
            depthComputationMethod = new Kinect360Depth();
        }
        if (depthCameraDevice instanceof KinectOne) {
            depthComputationMethod = new KinectOneDepth();
        }
        if (depthCameraDevice instanceof RealSense) {
            if (((CameraRealSense) ((RealSense) depthCameraDevice).getMainCamera()).isStarted()) {
                float depthScale = ((CameraRealSense) ((RealSense) depthCameraDevice).getMainCamera()).getDepthScale();
                depthComputationMethod = new RealSenseDepth(depthScale);
            }
        }
    }

    @Deprecated
    public void update(IplImage depth) {
        update(depth, 1);
    }

    @Deprecated
    public void update(opencv_core.IplImage depth, int skip) {
        depthData.clearDepth();
        updateRawDepth(depth);
        computeDepthAndDo(skip, new DoNothing());
    }

    public void updateMT(opencv_core.IplImage depth, opencv_core.IplImage color, PlaneAndProjectionCalibration calib, int skip2D, int skip3D) {
        updateRawDepth(depth);
        // optimisation no Color. 
        //        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();
        depthData.planeAndProjectionCalibration = calib;
//        computeDepthAndDo(skip2D, new DoNothing());
        computeDepthAndDo(skip2D, new Select2DPointPlaneProjection());
//        doForEachPoint(skip2D, new Select2DPointPlaneProjection());
        doForEachPoint(skip3D, new Select3DPointPlaneProjection());

        // Optimisations -- for demos
        //        depthData.connexity.setPrecision(skip3D);
        //        doForEachValid3DPoint(skip3D, new ComputeNormal());
//        depthData.connexity.computeAll();
//        doForEachPoint(1, new ComputeNormal());
//        doForEachPoint(skip2D, new SetImageData());
        // Optimisation no Color
        // doForEachValidPoint(skip2D, new SetImageData());
        // doForEachValid3DPoint(skip3D, new SetImageData());
    }

    public void updateMT2D(opencv_core.IplImage depth, opencv_core.IplImage color, PlaneAndProjectionCalibration calib, int skip) {
        updateRawDepth(depth);

        // TechFest Hacks
//        updateRawColor(color);
        depthData.clear();
//        depthData.clearDepth();
//        depthData.clear2D();
//        depthData.clearColor();
        depthData.timeStamp = papplet.millis();
        depthData.planeAndProjectionCalibration = calib;
        computeDepthAndDo(skip, new Select2DPointPlaneProjection());

        // TechFest Hacks
//        doForEachValidPoint(skip, new SetImageData());
    }

    public void updateMT3D(opencv_core.IplImage depth, opencv_core.IplImage color, PlaneAndProjectionCalibration calib, int skip) {
        updateRawDepth(depth);
        // TechFest Hack
//        updateRawColor(color);
        depthData.clear();
//        depthData.clearDepth();
//        depthData.clear3D();
//        depthData.clearColor();
        depthData.timeStamp = papplet.millis();
        depthData.planeAndProjectionCalibration = calib;
        computeDepthAndDo(skip, new Select3DPointPlaneProjection());
//        doForEachValidPoint(skip, new SetImageData());
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

    protected void doForEachValidPoint(int precision, DepthPointManiplation manip) {
        if (precision <= 0) {
            return;
        }

        PixelList pixels = new PixelList(precision);

        for (PixelOffset px : pixels) {
            Vec3D pKinect = depthData.depthPoints[px.offset];
            if (pKinect != INVALID_POINT && depthData.validPointsMask[px.offset] == true) {
                manip.execute(pKinect, px);
            }
        }
    }

    protected void doForEachValid3DPoint(int precision, DepthPointManiplation manip) {
        if (precision <= 0) {
            return;
        }
        PixelList pixels = new PixelList(precision);

        for (PixelOffset px : pixels) {
            Vec3D pKinect = depthData.depthPoints[px.offset];
            if (pKinect != INVALID_POINT && depthData.validPointsMask3D[px.offset] == true) {
                manip.execute(pKinect, px);
            }
        }
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
        if (getDepthCameraDevice().type() == Camera.Type.REALSENSE) {
            depthRawBuffer = depthImage.getByteBuffer();
            depthRawShortBuffer = depthRawBuffer.asShortBuffer();
        } else {
            depthImage.getByteBuffer().get(depthRaw);
        }
    }

    protected void updateRawColor(opencv_core.IplImage colorImage) {
        ByteBuffer colBuff = colorImage.getByteBuffer();
        colBuff.get(colorRaw);
    }

    public void setNearFarValue(float near, float far) {
        this.closeThreshold = near;
        this.farThreshold = far;
    }

    class Select2DPointPlaneProjection implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p)) {

//                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
//                depthData.projectedPoints[px.offset] = projected;
                depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

                if (isInside(depthData.projectedPoints[px.offset], 0.f, 1.f, 0.0f)) {
                    depthData.validPointsMask[px.offset] = true;
                    depthData.validPointsList.add(px.offset);
                }
            }
        }
    }

    class Select2DPointPlaneProjectionSR300Error implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            float error = Math.abs(p.x / 50f) + p.z / 400f;

            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p, error)) {
//                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
//                depthData.projectedPoints[px.offset] = projected;
                depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

                if (isInside(depthData.projectedPoints[px.offset], 0.f, 1.f, 0.0f)) {
                    depthData.validPointsMask[px.offset] = true;
                    depthData.validPointsList.add(px.offset);
                }
            }
        }
    }

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

    class UndistortSR300Depth implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {

            if (p.z > 300) {
//                float depthCoeff = ((p.z) / 200000f) +  1f;
                float xCoeff;
                if (p.x < 0) {
                    xCoeff = (1f - (p.x / 10000f)) ;
                } else {
                    xCoeff = (1f + (p.x / 10000f)) ;
                }
//                if (px.x == px.y) {
//                    System.out.println("coeff: " +xCoeff);
//                }
                depthData.depthPoints[px.offset].z = p.z * xCoeff;

            }
        }
    }

    class SelectPlaneTouchHand implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {

            boolean overTouch = depthData.planeAndProjectionCalibration.hasGoodOrientation(p);
            boolean underTouch = depthData.planeAndProjectionCalibration.isUnderPlane(p);
            boolean touchSurface = depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p);

            Vec3D projected = depthData.planeAndProjectionCalibration.project(p);

            if (isInside(projected, 0.f, 1.f, 0.0f)) {

                depthData.projectedPoints[px.offset] = projected;
                depthData.touchAttributes[px.offset] = new TouchAttributes(touchSurface, underTouch, overTouch);
                depthData.validPointsMask[px.offset] = touchSurface;

                if (touchSurface) {
                    depthData.validPointsList.add(px.offset);
                }
            }
        }
    }

    class Select2DPointOverPlane implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeCalibration.hasGoodOrientation(p)) {
                depthData.validPointsMask[px.offset] = true;
                depthData.validPointsList.add(px.offset);
            }
        }
    }

    class Select2DPointOverPlaneDist implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeCalibration.hasGoodOrientationAndDistance(p)) {
                depthData.validPointsMask[px.offset] = true;
                depthData.validPointsList.add(px.offset);
            }
        }
    }

    class Select2DPointCalibratedHomography implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {

            PVector projected = new PVector();
            PVector init = new PVector(p.x, p.y, p.z);

            depthData.homographyCalibration.getHomographyInv().mult(init, projected);

            // TODO: Find how to select the points... 
            if (projected.z > 10 && projected.x > 0 && projected.y > 0) {
                depthData.validPointsMask[px.offset] = true;
                depthData.validPointsList.add(px.offset);
            }
        }
    }

    class Select3DPointPlaneProjection implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeAndProjectionCalibration.hasGoodOrientation(p)) {
//                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
//                depthData.projectedPoints[px.offset] = projected;

                depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

                if (isInside(depthData.projectedPoints[px.offset], 0.f, 1.f, 0.1f)) {
                    depthData.validPointsMask3D[px.offset] = true;
                    depthData.validPointsList3D.add(px.offset);
                }
            }
        }
    }

    class SetImageData implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            depthData.pointColors[px.offset] = getPixelColor(px.offset);

        }
    }

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
        int begin = 0;
        int end;

        public PixelList(int precision) {
            this.precision = precision;
            this.begin = 0;
            this.end = calibDepth.getHeight();
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
            this.begin = begin;
            this.end = end;
        }

        @Override
        public Iterator<PixelOffset> iterator() {
            Iterator<PixelOffset> it = new Iterator<PixelOffset>() {

                private int x = 0;
                private int y = begin;
                private int offset = 0;
                private final int width = calibDepth.getWidth();

                @Override
                public boolean hasNext() {
                    return offset < width * end;
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

    class Kinect360Depth implements DepthComputation {

        @Override
        public float findDepth(int offset) {
            float d = (depthRaw[offset * 2] & 0xFF) << 8
                    | (depthRaw[offset * 2 + 1] & 0xFF);

            return d;
        }
    }

    public static final float KINECT_ONE_DEPTH_RATIO = 10f;

    class KinectOneDepth implements DepthComputation {

        @Override
        public float findDepth(int offset) {
            float d = (depthRaw[offset * 3 + 1] & 0xFF) * 256
                    + (depthRaw[offset * 3] & 0xFF);

            return d / KINECT_ONE_DEPTH_RATIO; // / 65535f * 10000f;
        }
    }

    class RealSenseDepth implements DepthComputation {

        private float depthRatio;

        public RealSenseDepth(float depthRatio) {
            this.depthRatio = depthRatio;
        }

        @Override
        public float findDepth(int offset) {
            float d = depthRawShortBuffer.get(offset) * depthRatio * 1000f;
            return d;
        }
    }

    public void undistortRGB(opencv_core.IplImage rgb, opencv_core.IplImage out) {
        calibRGB.getDevice().undistort(rgb, out);
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
        return calibRGB;
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
