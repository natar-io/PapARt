/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

/**
 *
 * @author jeremylaviole
 */
import codeanticode.glgraphics.GLTexture;
import codeanticode.gsvideo.GSCapture;
import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import fr.inria.papart.tools.GSIplImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.media.opengl.GL;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

public class Camera {

    public ARTagDetector art;
    // Camera parameters
    protected PMatrix3D camIntrinsicsP3D;
    protected PApplet parent;
    private ArrayList<TrackedView> trackedViews;
    private MarkerBoard[] sheets;
    private ARTThread thread = null;
    protected ProjectiveDeviceP pdp;
    // GStreamer  Video input
    protected GSCapture gsCapture;
    protected GSIplImage converter;
    // OpenCV  video input 
    private FrameGrabber grabber;
    // Texture for video visualization (OpenCV generally)
    protected IplImage iimg = null, copyUndist;
    protected GLTexture tex = null;
    public final static int OPENCV_VIDEO = 1;
    public final static int GSTREAMER_VIDEO = 2;
    public static int videoInput = OPENCV_VIDEO;
    protected int width, height;
    protected int videoInputType;
    protected int frameRate;
    protected boolean autoUpdate = false;

    static public void convertARParams(PApplet parent, String calibrationYAML,
            String calibrationData, int width, int height) {
        try {
            fr.inria.papart.procam.Utils.convertARParam(parent, calibrationYAML, calibrationData, width, height);
        } catch (Exception e) {
            PApplet.println("Conversion error. " + e);
        }
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height) {
        this(parent, camDevice, width, height, 30, null, videoInput);
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height, int videoInput) {
        this(parent, camDevice, width, height, 30, null, videoInput);
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height, String calibrationYAML) {
        this(parent, camDevice, width, height, 30, calibrationYAML, videoInput);
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height, String calibrationYAML, int videoInputType) {
        this(parent, camDevice, width, height, 30, calibrationYAML, videoInputType);
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height, int frameRate, String calibrationYAML, int videoInputType) {

        this.parent = parent;
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.videoInputType = videoInputType;

        if (videoInputType == OPENCV_VIDEO) {
            OpenCVFrameGrabber grabberCV = new OpenCVFrameGrabber(Integer.parseInt(camDevice));

            grabberCV.setImageWidth(width);
            grabberCV.setImageHeight(height);
            grabberCV.setImageMode(FrameGrabber.ImageMode.COLOR);

            try {
                grabberCV.start();
            } catch (Exception e) {
                System.err.println("Could not start frameGrabber... " + e);
            }

            this.grabber = grabberCV;
        }

        if (videoInputType == GSTREAMER_VIDEO) {

            gsCapture = new GSCapture(parent, width, height, camDevice);
            converter = new GSIplImage(width, height);
            gsCapture.setPixelDest(converter, false);

            gsCapture.setEventHandlerObject(this);
            gsCapture.start();
        }


        if (calibrationYAML != null) {

            // Load the camera parameters. 
            try {
                pdp = ProjectiveDeviceP.loadCameraDevice(calibrationYAML, 0);
                camIntrinsicsP3D = pdp.getIntrinsics();
            } catch (Exception e) {
                parent.die("Error reading the calibration file : " + calibrationYAML + " \n" + e);
            }
        }

    }

    public boolean useGStreamer() {
        return this.videoInputType == GSTREAMER_VIDEO;
    }

    public boolean useOpenCV() {
        return this.videoInputType == OPENCV_VIDEO;
    }

    public int getFrameRate() {
        return this.frameRate;
    }

    public void captureEvent(GSCapture cam) {
        cam.read();
    }

    public void initMarkerDetection(String calibrationARToolkit, MarkerBoard[] paperSheets) {
        art = new ARTagDetector(calibrationARToolkit, width, height, paperSheets, videoInputType);
        this.sheets = paperSheets;
        this.trackedViews = new ArrayList<TrackedView>();
    }

    // For GStreamer ! 
    /**
     * It makes the camera update continuously.
     */
    public void setThread() {
        setThread(true);
    }

    /**
     * It makes the camera update continuously.
     */
    public void setThread(boolean undistort) {

        if (thread == null) {
            thread = new ARTThread(this, sheets, undistort);
            thread.start();
        } else {
            System.err.println("Camera: Error Thread already launched");
        }
    }

    /**
     * Stops the update thread.
     */
    public void stopThread() {
        if (thread != null) {
            thread.stopThread();
            thread = null;
        }
    }

    /**
     * If the video is threaded, this sets if the tracking is on or not.
     *
     * @param auto automatic Tag detection: ON if true.
     */
    public void setAutoUpdate(boolean auto) {
        this.autoUpdate = auto;

        if (thread != null) {
            thread.setCompute(auto);
        } else {
            System.err.println("Camera: Error AutoCompute only if threaded.");
        }

    }

    /**
     * Gets the 2D location in the image of a 3D point. TODO: undistort ?
     *
     * @param pt 3D point seen by the camera.
     * @return 2D location of the 3D point in the image.
     */
    public PVector getCamViewPoint(PVector pt) {
        PVector tmp = new PVector();
        camIntrinsicsP3D.mult(new PVector(pt.x, pt.y, pt.z), tmp);
        //TODO: lens distorsion ?
        return new PVector(tmp.x / tmp.z, tmp.y / tmp.z);
    }

    public boolean useThread() {
        return thread != null;
    }

    /**
     * Asks the camera to grab an image. Not to use with the threaded option.
     */
    public void grab() {
        grab(true);
    }

    public IplImage grab(boolean undistort) {

        IplImage img = null;

        if (videoInputType == OPENCV_VIDEO) {

            try {
                img = grabber.grab();

            } catch (Exception e) {
                System.err.println("Camera: Grab() Error ! " + e);
                e.printStackTrace();
            }

        } else {
            if (videoInputType == GSTREAMER_VIDEO) {

                if (converter.putPixelsToImage()) {
                    ;
                } else {

                    // System.err.println("Camera: GStreamer no Frame ?!");
                    try {
                        TimeUnit.MILLISECONDS.sleep((long) (1f / frameRate));
                    } catch (Exception e) {
                    }

                }
                img = converter.getImage();

            } else {
                // Crash !
                System.err.println("You must specify a valid video input.");
                assert (true);
//                return null;
            }
        }


        if (img != null) {
            if (undistort) {
                if (copyUndist == null) {
                    copyUndist = img.clone();
                }
                pdp.getDevice().undistort(img, copyUndist);
                iimg = copyUndist;
            } else {
                iimg = img;
            }
        }

        return iimg;

    }

    /**
     * Add a tracked view to the camera. This camera must be tracking the board
     * already. Returns true if the camera is already tracking.
     *
     * @param view
     * @return
     */
    public boolean addTrackedView(TrackedView view) {
        return trackedViews.add(view);
    }

    public IplImage getView(TrackedView trackedView) {
        return getViewIpl(trackedView);
    }

    public IplImage getViewIpl(TrackedView trackedView) {

        if (iimg == null) {
            return null;
        }

        trackedView.computeCorners(this);
        return trackedView.getImageIpl(iimg);
    }

    
    public void grabTo(PImage pimg) {
        try {
            IplImage img = grabber.grab();

            ByteBuffer buff1 = img.getByteBuffer();
            pimg.loadPixels();
            for (int i = 0; i
                    < img.width() * img.height(); i++) {
                int offset = i * 3;
                pimg.pixels[i] = (buff1.get(offset + 2) & 0xFF) << 16
                        | (buff1.get(offset + 1) & 0xFF) << 8
                        | (buff1.get(offset) & 0xFF);
            }

            pimg.updatePixels();
        } catch (Exception e) {
            System.err.println("Error while grabbing frame " + e);
            e.printStackTrace();
        }
    }

    public PImage getPImage() {
        if (tex == null) {
            tex = new GLTexture(parent, width, height);
        }


        if (iimg != null) {
            if (videoInputType == OPENCV_VIDEO) {
                tex.putBuffer(GL.GL_BGR, GL.GL_UNSIGNED_BYTE, iimg.getIntBuffer());
            } else {
                if (videoInputType == GSTREAMER_VIDEO) {
                    tex.putBuffer(GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, iimg.getIntBuffer());
                }
            }
        }

        return tex;
    }

    public IplImage getIplImage() {
        return iimg;
    }

    public void close() {
        if (grabber != null) {
            try {
                this.stopThread();
                grabber.stop();
            } catch (Exception e) {
            }
        }
    }
}
