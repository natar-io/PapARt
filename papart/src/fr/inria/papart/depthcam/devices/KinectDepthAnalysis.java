/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.DepthAnalysis;
import fr.inria.papart.depthcam.PixelOffset;
import fr.inria.papart.depthcam.TouchAttributes;
import static fr.inria.papart.depthcam.DepthAnalysis.INVALID_POINT;
import static fr.inria.papart.depthcam.DepthAnalysis.papplet;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class KinectDepthAnalysis extends DepthAnalysis {

    // Protected values, important data. 
    protected float[] depthLookUp = null;

    // Configuration 
    private float closeThreshold = 300f, farThreshold = 12000f;
    protected ProjectiveDeviceP calibIR, calibRGB;
    private final PMatrix3D KinectRGBIRCalibration = new PMatrix3D(1, 0, 0, 15,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1);

    // private variables 
    // Raw data from the Kinect Sensor
    protected byte[] depthRaw;
    protected byte[] colorRaw;

    // static values
    protected static final float INVALID_DEPTH = -1;

    protected KinectDevice kinectDevice;

    public KinectDevice kinectDevice() {
        return this.kinectDevice;
    }

    @Override
    public int getDepthSize() {
        return kinectDevice().depthSize();
    }
    @Override
    public int getDepthWidth() {
        return kinectDevice().depthWidth();
    }
    @Override
    public int getDepthHeight() {
        return kinectDevice().depthHeight();
    }

    // TODO: only one constructor -- or a better factorization. 
    public KinectDepthAnalysis(PApplet parent, CameraOpenKinect camera) {

        // todo: better than this...
        kinectDevice = new Kinect360();

        DepthAnalysis.papplet = parent;

        calibRGB = camera.getProjectiveDevice();
        calibIR = camera.getDepthCamera().getProjectiveDevice();
        initMemory();

        // initThreadPool();
    }

    public KinectDepthAnalysis(PApplet parent, KinectOne kinect) {
        kinectDevice = kinect;

        DepthAnalysis.papplet = parent;

        calibRGB = kinect.cameraRGB.getProjectiveDevice();
        calibIR = kinect.cameraIR.getProjectiveDevice();
        useKinectOne = true;
        initMemory();

        // initThreadPool();
    }

    private boolean useKinectOne = false;

    // Thread version... No bonus whatsoever for now.
    private int nbThreads = 16;
    private ExecutorService threadPool;

    private void initThreadPool() {
        threadPool = Executors.newFixedThreadPool(nbThreads);
    }

    private void initMemory() {
        
        System.out.println("Init color memory " + kinectDevice.colorSize());
        colorRaw = new byte[kinectDevice.colorSize() * 3];
//        if (useKinectOne) {
        depthRaw = new byte[kinectDevice.depthSize() * 3];
//        } else {
//            depthRaw = new byte[size * 2];
//        }

        depthData = new KinectDepthData(this);
        depthData.projectiveDevice = this.calibIR;
        
        System.out.println("Width " + kinectDevice.depthWidth());
        
        PixelOffset.initStaticMode(kinectDevice.depthWidth(), kinectDevice.depthHeight());
    }

    public void update(IplImage depth) {
        update(depth, 1);
    }

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
            if (d != INVALID_DEPTH) {

//                Vec3D pKinect = calibIR.pixelToWorld(px.x, px.y, d);
//                depthData.depthPoints[px.offset] = pKinect;
                calibIR.pixelToWorld(px.x, px.y, d, depthData.depthPoints[px.offset]);
//                 System.out.println("Depth " + depthData.depthPoints[px.offset]);
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

                calibIR.pixelToWorld(px.x, px.y, d, depthData.depthPoints[px.offset]);
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

    protected void updateRawDepth(opencv_core.IplImage depthImage) {
//        ByteBuffer depthBuff = depthImage.imageData().asBuffer();
//        System.out.println("depthRaw size " + depthRaw.length + " image data size " + depthImage.getByteBuffer().capacity());
//        depthBuff.get(depthRaw);

        depthImage.getByteBuffer().get(depthRaw);
//        depthImage.imageData().get(depthRaw);
    }

    protected void updateRawColor(opencv_core.IplImage colorImage) {
        ByteBuffer colBuff = colorImage.getByteBuffer();
        colBuff.get(colorRaw);
    }

    public void setStereoCalibration(String fileName) {
        HomographyCalibration calib = new HomographyCalibration();
        calib.loadFrom(papplet, fileName);
        setStereoCalibration(calib.getHomography());
    }

    public void setStereoCalibration(PMatrix3D matrix) {
        KinectRGBIRCalibration.set(matrix);
    }

    public PMatrix3D getStereoCalibration() {
        return KinectRGBIRCalibration;
    }

    public int findColorOffset(Vec3D v) {
        return findColorOffset(v.x, v.y, v.z);
    }

    public int findColorOffset(PVector v) {
        return findColorOffset(v.x, v.y, v.z);
    }

    private PVector vt = new PVector();
    private PVector vt2 = new PVector();

    public int findColorOffset(float x, float y, float z) {
        vt.set(x, y, z);
        vt2.set(0, 0, 0);
        //  Ideally use a calibration... 
//        kinectCalibRGB.getExtrinsics().mult(vt, vt2);       
        KinectRGBIRCalibration.mult(vt, vt2);
        return calibRGB.worldToPixel(vt2.x, vt2.y, vt2.z);
    }

    /**
     * @param offset
     * @return the depth (float).
     */
    protected float getDepth(int offset) {

        int r = (int) (depthRaw[offset * 3] & 0xFF);
        int g = (int) (depthRaw[offset * 3 + 1] & 0xFF);
        int b = (int) (depthRaw[offset * 3 + 2] & 0xFF);

//        System.out.println("r " + r + " g " + g +" b " + b );
        float d = (depthRaw[offset * 3 +1 ] & 0xFF) << 8
                | (depthRaw[offset * 3 ] & 0xFF);

//        d = d / 100f;
//        System.out.println(d + " ");
        return d;
    }

    // TODO: TO activate 
    public interface DepthComputation {

        public float findDepth(int offset);
    }

    class Kinect360Depth implements DepthComputation {

        @Override
        public float findDepth(int offset) {
            float d = (depthRaw[offset * 2] & 0xFF) << 8
                    | (depthRaw[offset * 2 + 1] & 0xFF);

            return d;
        }

    }

    class KinectOneDepth implements DepthComputation {

        @Override
        public float findDepth(int offset) {
            float d = (depthRaw[offset * 2] & 0xFF) << 8
                    | (depthRaw[offset * 2 + 1] & 0xFF);

            return d;
        }
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
        int colorOffset = this.findColorOffset(depthData.depthPoints[offset]) * 3;
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
            this.end = calibIR.getHeight();
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
                private final int width = calibIR.getWidth();

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
            int partSize = nbParts / calibIR.getHeight();
            int begin = partSize * part;

            int end;
            if (part == nbThreads - 1) {
                end = calibIR.getHeight();
            } else {
                end = partSize * (part + 1);
            }

            PixelList pixels = new PixelList(precision, begin, end);

            for (PixelOffset px : pixels) {
                float d = getDepth(px.offset);
                if (d != INVALID_DEPTH) {
//                    Vec3D pKinect = calibIR.pixelToWorld(px.x, px.y, d);
//                    depthData.depthPoints[px.offset] = pKinect;

                    calibIR.pixelToWorld(px.x, px.y, d, depthData.depthPoints[px.offset]);
                    manip.execute(depthData.depthPoints[px.offset], px);
                }
            }
            return null;
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
        calibIR.getDevice().undistort(ir, out);
    }

    public ProjectiveDeviceP getColorProjectiveDevice() {
        return calibRGB;
    }

    public ProjectiveDeviceP getDepthProjectiveDevice() {
        return calibIR;
    }

    public byte[] getColorBuffer() {
        return this.colorRaw;
    }

    protected boolean isGoodDepth(float rawDepth) {
        return (rawDepth >= closeThreshold && rawDepth < farThreshold);
    }

}
