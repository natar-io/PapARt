/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam;

import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import static fr.inria.papart.depthcam.DepthAnalysis.INVALID_POINT;
import static fr.inria.papart.depthcam.DepthAnalysis.papplet;
import fr.inria.papart.procam.ProjectiveDeviceP;
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

    private int mode;

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

    public KinectDepthAnalysis(PApplet parent, CameraOpenKinect camera) {
        this.width = Kinect.WIDTH;
        this.height = Kinect.HEIGHT;
        this.size = Kinect.WIDTH * Kinect.HEIGHT;

        DepthAnalysis.papplet = parent;
        this.mode = Kinect.KINECT_MM;

        calibRGB = camera.getProjectiveDevice();
        calibIR = camera.getDepthCamera().getProjectiveDevice();
        initMemory();
        PixelOffset.initStaticMode(width, height);
        initThreadPool();
    }

    private int nbThreads = 16;
    ExecutorService threadPool;

    private void initThreadPool() {
        threadPool = Executors.newFixedThreadPool(nbThreads);
    }

    private void initMemory() {
        colorRaw = new byte[size * 3];
        depthRaw = new byte[size * 2];

        depthData = new KinectDepthData(this);
        depthData.projectiveDevice = this.calibIR;

        // Hack : no more depthLookup...
//        if (depthLookUp == null) {
//            depthLookUp = new float[2048];
//            if (this.mode == Kinect.KINECT_10BIT) {
//                for (int i = 0; i < depthLookUp.length; i++) {
//                    depthLookUp[i] = rawDepthToMeters10Bits(i);
//                }
//            }
//            if (this.mode == Kinect.KINECT_MM) {
//                for (int i = 0; i < depthLookUp.length; i++) {
//                    depthLookUp[i] = rawDepthToMetersNoChange(i);
//                }
//            }
//        }
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
        computeDepthAndDo(skip2D, new DoNothing());
        doForEachPoint(skip2D, new Select2DPointPlaneProjection());
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

    protected void computeDepthAndDo(int precision, DepthPointManiplation manip) {
        PixelList pixels = new PixelList(precision);

        for (PixelOffset px : pixels) {
            float d = getDepth(px.offset);
            if (d != INVALID_DEPTH) {
                Vec3D pKinect = calibIR.pixelToWorld(px.x, px.y, d);
                depthData.depthPoints[px.offset] = pKinect;
                manip.execute(pKinect, px);
            }
        }
    }

    protected void computeDepthAndDo(int precision, DepthPointManiplation manip, InvalidPointManiplation invalidManip) {

        PixelList pixels = new PixelList(precision);

        for (PixelOffset px : pixels) {

            float d = getDepth(px.offset);
            if (d != INVALID_DEPTH) {
                Vec3D pKinect = calibIR.pixelToWorld(px.x, px.y, d);
                depthData.depthPoints[px.offset] = pKinect;
                manip.execute(pKinect, px);
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
        ByteBuffer depthBuff = depthImage.getByteBuffer();
        depthBuff.get(depthRaw);
    }

    protected void updateRawColor(opencv_core.IplImage colorImage) {
        ByteBuffer colBuff = colorImage.getByteBuffer();
        colBuff.get(colorRaw);
    }

    public static int getKinectSize() {
        return Kinect.WIDTH * Kinect.HEIGHT;
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

    public int findColorOffset(float x, float y, float z) {
        PVector vt = new PVector(x, y, z);
        PVector vt2 = new PVector();
        //  Ideally use a calibration... 
//        kinectCalibRGB.getExtrinsics().mult(vt, vt2);       
        KinectRGBIRCalibration.mult(vt, vt2);
        return calibRGB.worldToPixel(new Vec3D(vt2.x, vt2.y, vt2.z));
    }

    /**
     * @param offset
     * @return the depth (float) or INVALID_DEPTH if it failed.
     */
    protected float getDepth(int offset) {
        float d = (depthRaw[offset * 2] & 0xFF) << 8
                | (depthRaw[offset * 2 + 1] & 0xFF);

        return d;
    }

    public void setNearFarValue(float near, float far) {
        this.closeThreshold = near;
        this.farThreshold = far;
    }

    class Select2DPointPlaneProjection implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p)) {
                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
                depthData.projectedPoints[px.offset] = projected;
                if (isInside(projected, 0.f, 1.f, 0.0f)) {
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
                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
                depthData.projectedPoints[px.offset] = projected;
                if (isInside(projected, 0.f, 1.f, 0.1f)) {
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
                    Vec3D pKinect = calibIR.pixelToWorld(px.x, px.y, d);
                    depthData.depthPoints[px.offset] = pKinect;
                    manip.execute(pKinect, px);
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

    public static float rawDepthToMeters10Bits(int depthValue) {
        if (depthValue < 2047) {
            return (float) (1.0 / ((float) (depthValue) * -0.0030711016f + 3.3309495161f));
        }
        return 0.0f;
    }

    public static float rawDepthToMeters10Bits2(int depthValue) {
        if (depthValue < 2047) {
            return 0.1236f * (float) Math.tan((double) depthValue / 2842.5 + 1.1863);
        }
        return 0.0f;
    }
    ////////////// WORKS WITH   DEPTH- REGISTERED - MM ////////

    public static float rawDepthToMetersNoChange(int depthValue) {
        if (depthValue < 2047) {
            return (float) depthValue / 1000f;
        }
        return 0.0f;
    }

}
