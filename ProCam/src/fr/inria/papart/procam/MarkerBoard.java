/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import org.bytedeco.javacpp.ARToolKitPlus;
import org.bytedeco.javacpp.ARToolKitPlus.TrackerMultiMarker;
import org.bytedeco.javacpp.opencv_core.IplImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;
import org.bytedeco.javacv.ObjectFinder;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class MarkerBoard {

    private final String fileName, name;
    protected int width;
    protected int height;
    protected ArrayList<Camera> cameras;
    protected ArrayList<Boolean> drawingMode;
    protected ArrayList<Float> minDistanceDrawingMode;
    protected ArrayList transfos;
    protected ArrayList trackers;
    protected ArrayList<OneEuroFilter[]> filters;
    protected ArrayList<PVector> lastPos;
    protected ArrayList<Integer> nextTimeEvent;
    protected ArrayList<Integer> updateStatus;
    protected PApplet applet;

    protected MarkerType type = null;

    private PVector[] objectPoints;
    private PVector[] imagePoints;
    private PVector botLeft, botRight, topLeft, topRight;

    private static final int updateTime = 1000; // 1 sec
    static public final int NORMAL = 1;
    static public final int BLOCK_UPDATE = 2;
    static public final int FORCE_UPDATE = 3;

    public enum MarkerType {

        ARTOOLKITPLUS, OPENCV_SURF
    }

    public MarkerBoard(String fileName, String name, PVector size) {
        this(fileName, name, (int) size.x, (int) size.y);
    }

    public MarkerBoard(String fileName, String name, int width, int height) {
        this.fileName = fileName;
        this.width = width;
        this.height = height;
        this.name = name;
        checkType(fileName);

        cameras = new ArrayList<Camera>();

        if (type == MarkerType.ARTOOLKITPLUS) {
            transfos = new ArrayList<float[]>();
            trackers = new ArrayList<ARToolKitPlus.TrackerMultiMarker>();
        }
        if (type == MarkerType.OPENCV_SURF) {
            transfos = new ArrayList<PMatrix3D>();
            trackers = new ArrayList<ObjectFinder>();
            imagePoints = new PVector[4];
            objectPoints = new PVector[4];
            // botLeft
            objectPoints[0] = new PVector(0, 0, 0);
            // botRight
            objectPoints[1] = new PVector(width, 0, 0);
            // topLeft
            objectPoints[2] = new PVector(0, height, 0);
            // topRight
            objectPoints[3] = new PVector(width, height, 0);
            Logger logger = Logger.getLogger(ObjectFinder.class.getName());
            logger.setLevel(Level.OFF);
        }

        filters = new ArrayList<OneEuroFilter[]>();
        drawingMode = new ArrayList<Boolean>();
        minDistanceDrawingMode = new ArrayList<Float>();
        lastPos = new ArrayList<PVector>();
        nextTimeEvent = new ArrayList<Integer>();
        updateStatus = new ArrayList<Integer>();

    }

    private void checkType(String name) {
        if (name.endsWith("cfg")) {
            this.type = MarkerType.ARTOOLKITPLUS;
        }
        if (name.endsWith("png") || name.endsWith("jpg")) {
            this.type = MarkerType.OPENCV_SURF;
        }
        assert (type != null);
    }

    public void addTracker(PApplet applet, Camera camera) {
//    public void addTracker(PApplet applet, Camera camera, ARToolKitPlus.TrackerMultiMarker tracker, float[] transfo) {
        this.applet = applet;

        if (type == MarkerType.ARTOOLKITPLUS) {
            // create a tracker that does:
            //  - 6x6 sized marker images (required for binary markers)
            //  - samples at a maximum of 6x6 
            //  - works with luminance (gray) images
            //  - can load a maximum of 0 non-binary pattern
            //  - can detect a maximum of 8 patterns in one image
            TrackerMultiMarker tracker = new ARToolKitPlus.TrackerMultiMarker(camera.width(), camera.height(), 10, 6, 6, 6, 0);

            // ARToolKit 2.1.1 - version
            // MultiTracker tracker = new MultiTracker(w, h);
            //            int pixfmt = ARToolKitPlus.PIXEL_FORMAT_LUM;
            int pixfmt = 0;

            if (camera.videoInputType == Camera.OPENCV_VIDEO) {
                pixfmt = ARToolKitPlus.PIXEL_FORMAT_BGR;
            }
            if (camera.videoInputType == Camera.PROCESSING_VIDEO) {
                // Works on MacBook
                pixfmt = ARToolKitPlus.PIXEL_FORMAT_ABGR;
            }

            tracker.setPixelFormat(pixfmt);
            tracker.setBorderWidth(0.125f);
            tracker.activateAutoThreshold(true);
            tracker.setUndistortionMode(ARToolKitPlus.UNDIST_NONE);
            tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);
            tracker.setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);
            tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
//            tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_HALF_RES);
            tracker.setUseDetectLite(false);
//            tracker.setUseDetectLite(true);

            // Initialize the tracker, with camera parameters and marker config. 
            if (!tracker.init(camera.calibrationARToolkit, this.getFileName(), 1.0f, 1000.f)) {
                System.err.println("Init ARTOOLKIT Error " + camera.calibrationARToolkit + " " + this.getFileName() + " " + getName());
            }

            float[] transfo = new float[16];
            for (int i = 0; i < 3; i++) {
                transfo[12 + i] = 0;
            }
            transfo[15] = 0;
            this.trackers.add(tracker);
            this.transfos.add(transfo);
        }
        if (this.type == MarkerType.OPENCV_SURF) {

            ObjectFinder.Settings settings = new ObjectFinder.Settings();

            IplImage imgToFind = cvLoadImage(this.fileName);

            // TODO: tweak these.
            settings.setObjectImage(imgToFind);
            settings.setUseFLANN(true);
            settings.setRansacReprojThreshold(5);
           settings.setMatchesMin(16);
            ObjectFinder finder = new ObjectFinder(settings);

            this.trackers.add(finder);
            this.transfos.add(new PMatrix3D());
        }

        this.cameras.add(camera);

        this.drawingMode.add(false);
        this.lastPos.add(new PVector());
        this.minDistanceDrawingMode.add(2f);
        this.nextTimeEvent.add(0);
        this.updateStatus.add(NORMAL);

        OneEuroFilter[] filter = null;
        this.filters.add(filter);
    }

    private int getId(Camera camera) {
        return cameras.indexOf(camera);
    }

    public void setFiltering(Camera camera, double freq, double minCutOff) {
        int id = cameras.indexOf(camera);
        OneEuroFilter[] filter = createFilter(freq, minCutOff);
        filters.set(id, filter);
    }

    public void setDrawingMode(Camera camera, boolean dm) {
        setDrawingMode(camera, dm, 2);
    }

    public void setDrawingMode(Camera camera, boolean dm, float dist) {
        int id = getId(camera);

        drawingMode.set(id, dm);
        minDistanceDrawingMode.set(id, dist);
    }

    public void setFakeLocation(Camera camera, PMatrix3D location) {
        int id = cameras.indexOf(camera);

        if (type == MarkerType.ARTOOLKITPLUS) {
            float transfo[] = (float[]) transfos.get(id);
            location.get(transfo);
        }
        if (type == MarkerType.OPENCV_SURF) {
            PMatrix3D transfo = (PMatrix3D) transfos.get(id);
            transfo.set(location);
        }

    }

    private OneEuroFilter[] createFilter(double freq, double minCutOff) {
        OneEuroFilter[] f = new OneEuroFilter[12];
        try {
            for (int i = 0; i < 12; i++) {
                f[i] = new OneEuroFilter(freq);
                f[i].setFrequency(freq);
                f[i].setMinCutoff(minCutOff);
            }
        } catch (Exception e) {
            System.out.println("Filter init error" + e);
        }

        return f;
    }

//    public MultiTracker getTracker() {
//        return this.tracker;
//    }
    public void forceUpdate(Camera camera, int time) {
        int id = getId(camera);
        nextTimeEvent.set(id, applet.millis() + time);
        updateStatus.set(id, FORCE_UPDATE);
    }

    public void blockUpdate(Camera camera, int time) {
        int id = getId(camera);
        nextTimeEvent.set(id, applet.millis() + time);
        updateStatus.set(id, BLOCK_UPDATE);
    }

    public boolean isMoving(Camera camera) {
        int id = getId(camera);
//        nextTimeEvent.set(id, applet.millis() + time);
        int mode = updateStatus.get(id);

        if (mode == BLOCK_UPDATE) {
            return false;
        }
        if (mode == FORCE_UPDATE || mode == NORMAL) {
            return true;
        }

        return true;
    }

    private PVector getPositionVector(int id) {
        if (type == MarkerType.ARTOOLKITPLUS) {
            float transfo[] = (float[]) transfos.get(id);
            return new PVector(transfo[3], transfo[7], transfo[11]);
        }
        if (type == MarkerType.OPENCV_SURF) {
            PMatrix3D transfo = (PMatrix3D) transfos.get(id);
            return new PVector(transfo.m03, transfo.m13, transfo.m23);
        }
        return null;
    }

    public PVector getBoardLocation(Camera camera, Projector projector) {
        int id = cameras.indexOf(camera);

        PVector v = getPositionVector(id);
        PVector v2 = new PVector();
        projector.getExtrinsics().mult(v, v2);
        PVector px = projector.pdp.worldToPixel(v2, true);
        return px;
    }

    // We suppose that the ARDisplay is the one of the camera...
    public PVector getBoardLocation(Camera camera, ARDisplay display) {
        int id = cameras.indexOf(camera);
        PVector v = getPositionVector(id);

        // Apply extrinsics if required.
        PMatrix3D extr = display.getExtrinsics();
        if (extr != null) {
            PVector v2 = new PVector();
            extr.mult(v, v2);
            v = v2;
        }
        PVector px = display.pdp.worldToPixel(v, true);
        return px;
    }

    public boolean isSeenBy(Camera camera, Projector projector, float error) {
        PVector px = this.getBoardLocation(camera, projector);
        return !(px.x < (0 - error)
                || px.x > projector.frameWidth
                || px.y < (0 - error)
                || px.y > (projector.frameHeight + error));
    }

    public synchronized void updatePosition(Camera camera, IplImage img) {

        int id = cameras.indexOf(camera);
        if (id == -1) {
            throw new RuntimeException("The board " + this.name + " is not registered with the camera you asked");
        }

        int currentTime = applet.millis();
        int endTime = nextTimeEvent.get(id);
        int mode = updateStatus.get(id);

        // If the update is still blocked
        if (mode == BLOCK_UPDATE && currentTime < endTime) {
            return;
        }

        ///////////// SURF UPDATE ////////////////////
        if (type == MarkerType.OPENCV_SURF) {

            ObjectFinder finder = (ObjectFinder) trackers.get(id);

            // Find the markers
            double[] corners = finder.find(img);

            if (corners == null) {
                return;
            }

            PMatrix3D newPos = compute3DPos(corners, camera);

            // if the update is forced 
            if (mode == FORCE_UPDATE && currentTime < endTime) {
                update(newPos, id);
                return;
            }

            // the force and block updates are finished, revert back to normal
            if (mode == FORCE_UPDATE || mode == BLOCK_UPDATE && currentTime > endTime) {
                updateStatus.set(id, NORMAL);
            }

            PVector currentPos = new PVector(newPos.m03, newPos.m13, newPos.m23);
//        System.out.println("Current Pos " + currentPos);
//        System.out.println("Distance " + currentPos.dist(lastPos.get(id)));
            float distance = currentPos.dist(lastPos.get(id));

            // if it is a drawing mode
            if (drawingMode.get(id)) {

                if (distance > this.minDistanceDrawingMode.get(id)) {
                    update(newPos, id);

                    lastPos.set(id, currentPos);
                    updateStatus.set(id, FORCE_UPDATE);
                    nextTimeEvent.set(id, applet.millis() + MarkerBoard.updateTime);
//                    System.out.println("Next Update for x seconds");
                }

            } else {
                update(newPos, id);
            }
        }

        ///////////// ARTOOLKITPLUS UPDATE ////////////////////
        if (type == MarkerType.ARTOOLKITPLUS) {

            TrackerMultiMarker tracker = (TrackerMultiMarker) trackers.get(id);

            // Find the markers
            tracker.calc(img.imageData());

            if (tracker.getNumDetectedMarkers() <= 0) {
                return;
            }

            ARToolKitPlus.ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();

            // if the update is forced 
            if (mode == FORCE_UPDATE && currentTime < endTime) {
                update(multiMarkerConfig, id);
                return;
            }

            // the force and block updates are finished, revert back to normal
            if (mode == FORCE_UPDATE || mode == BLOCK_UPDATE && currentTime > endTime) {
                updateStatus.set(id, NORMAL);
            }

            PVector currentPos = new PVector((float) multiMarkerConfig.trans().get(3),
                    (float) multiMarkerConfig.trans().get(7),
                    (float) multiMarkerConfig.trans().get(11));
            float distance = currentPos.dist(lastPos.get(id));

            // if it is a drawing mode
            if (drawingMode.get(id)) {

                if (distance > this.minDistanceDrawingMode.get(id)) {
                    update(multiMarkerConfig, id);

                    lastPos.set(id, currentPos);
                    updateStatus.set(id, FORCE_UPDATE);
                    nextTimeEvent.set(id, applet.millis() + MarkerBoard.updateTime);
//                    System.out.println("Next Update for x seconds");
                }

            } else {
                update(multiMarkerConfig, id);
            }
        }

    }

    private PMatrix3D compute3DPos(double[] corners, Camera camera) {
        if (topLeft == null) {
            topLeft = new PVector();
            topRight = new PVector();
            botLeft = new PVector();
            botRight = new PVector();
        }

        botRight.x = (float) corners[0];
        botRight.y = (float) corners[1];

        botLeft.x = (float) corners[2];
        botLeft.y = (float) corners[3];

        topLeft.x = (float) corners[4];
        topLeft.y = (float) corners[5];
        topRight.x = (float) corners[6];
        topRight.y = (float) corners[7];

        imagePoints[0] = botLeft;
        imagePoints[1] = botRight;
        imagePoints[2] = topLeft;
        imagePoints[3] = topRight;

        ProjectiveDeviceP pdp = camera.getProjectiveDevice();
        return pdp.estimateOrientation(objectPoints, imagePoints);
    }

    private void update(ARToolKitPlus.ARMultiMarkerInfoT multiMarkerConfig, int id) {

        float transfo[] = (float[]) transfos.get(id);
        OneEuroFilter filter[] = filters.get(id);

        if (filter == null) {
            for (int i = 0; i < 12; i++) {
                transfo[i] = (float) multiMarkerConfig.trans().get(i);
            }
        } else {
            try {
                for (int i = 0; i < 12; i++) {
                    float v = (float) multiMarkerConfig.trans().get(i);
                    transfo[i] = (float) filter[i].filter(v);
                }
            } catch (Exception e) {
                System.out.println("Filtering error " + e);
            }
        }
//
//        // If z negation hack required...
//         PMatrix3D tmp = new PMatrix3D(transfo[0], transfo[1], transfo[2], transfo[3],
//                transfo[4], transfo[5], transfo[6], transfo[7],
//                transfo[8], transfo[9], transfo[10], transfo[11],
//                0, 0, 0, 1);
////         tmp.print();
//        tmp.scale(1, 1, -1);
//        transfo[11] = -transfo[11];
    }

    private void update(PMatrix3D newPos, int id) {

        PMatrix3D transfo = (PMatrix3D) transfos.get(id);
        OneEuroFilter filter[] = filters.get(id);

        if (filter == null) {
            transfo.set(newPos);
        } else {
            try {
                // Rotation
                transfo.m00 = (float) filter[0].filter(newPos.m00);
                transfo.m01 = (float) filter[1].filter(newPos.m01);
                transfo.m02 = (float) filter[2].filter(newPos.m02);
                transfo.m10 = (float) filter[3].filter(newPos.m10);
                transfo.m11 = (float) filter[4].filter(newPos.m11);
                transfo.m12 = (float) filter[5].filter(newPos.m12);
                transfo.m20 = (float) filter[6].filter(newPos.m20);
                transfo.m21 = (float) filter[7].filter(newPos.m21);
                transfo.m22 = (float) filter[8].filter(newPos.m22);

                // Translation
                transfo.m03 = (float) filter[9].filter(newPos.m03);
                transfo.m13 = (float) filter[10].filter(newPos.m13);
                transfo.m23 = (float) filter[11].filter(newPos.m23);

            } catch (Exception e) {
                System.out.println("Filtering error " + e);
            }
        }
//
//        // If z negation hack required...
//         PMatrix3D tmp = new PMatrix3D(transfo[0], transfo[1], transfo[2], transfo[3],
//                transfo[4], transfo[5], transfo[6], transfo[7],
//                transfo[8], transfo[9], transfo[10], transfo[11],
//                0, 0, 0, 1);
////         tmp.print();
//        tmp.scale(1, 1, -1);
//        transfo[11] = -transfo[11];
    }

    // TODO: Watch this for performance...
    public float[] getTransfo(Camera camera) {

        if (this.type == MarkerType.ARTOOLKITPLUS) {
            return (float[]) this.transfos.get(cameras.indexOf(camera));
        }
        if (this.type == MarkerType.OPENCV_SURF) {
            PMatrix3D mat = (PMatrix3D) this.transfos.get(cameras.indexOf(camera));
            
            float[] tmp = new float[12];
            mat.get(tmp);
            return tmp;
        }
        return null;
    }

    public PMatrix3D getTransfoMat(Camera camera) {
        if (this.type == MarkerType.ARTOOLKITPLUS) {
            float[] t = getTransfo(camera);
            return new PMatrix3D(t[0], t[1], t[2], t[3],
                    t[4], t[5], t[6], t[7],
                    t[8], t[9], t[10], t[11],
                    0, 0, 0, 1);
        }
        if (this.type == MarkerType.OPENCV_SURF) {
            
            PMatrix3D transfo = (PMatrix3D) this.transfos.get(cameras.indexOf(camera));
            return transfo;
//            return (PMatrix3D) this.transfos.get(cameras.indexOf(camera));
        }
        return null;
    }

    public PMatrix3D getTransfoRelativeTo(Camera camera, MarkerBoard board2) {

        PMatrix3D tr1 = getTransfoMat(camera);
        PMatrix3D tr2 = board2.getTransfoMat(camera);

        tr2.apply(tr1);
        return tr2;
    }

    public boolean usePMatrix(){
        return this.type == MarkerType.OPENCV_SURF;
    }
    
    public boolean useFloatArray(){
        return this.type == MarkerType.ARTOOLKITPLUS;
    }
    
    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "MarkerBoard " + getName();
    }
}
