
package fr.inria.papart.procam.camera;


import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class CameraFFMPEG extends Camera {

    private FFmpegFrameGrabber grabber;
    private final OpenCVFrameConverter.ToIplImage converter;

    protected CameraFFMPEG(String description) {
        this.cameraDescription = description;
        this.setPixelFormat(Camera.PixelFormat.BGR);
        converter = new OpenCVFrameConverter.ToIplImage();
    }

    @Override
    public void start() {
        FFmpegFrameGrabber grabberFF = new FFmpegFrameGrabber(this.cameraDescription);
        grabberFF.setImageWidth(width());
        grabberFF.setImageHeight(height());
//        grabberCV.setFrameRate(60);
        grabberFF.setImageMode(FrameGrabber.ImageMode.COLOR);
 
        try {
            grabberFF.start();
            this.grabber = grabberFF;
            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not start frameGrabber... " + e);

            System.err.println("Could not camera start frameGrabber... " + e);
            System.err.println("Camera ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection, ID and resolution asked.");

            this.grabber = null;
        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        try {
            IplImage img = converter.convertToIplImage(grabber.grab());
            if (img != null) {
                this.updateCurrentImage(img);
            }
        } catch (Exception e) {
            System.err.println("Camera: OpenCV Grab() Error ! " + e);
        }
    }

    @Override
    public PImage getPImage() {

        if (currentImage != null) {
            this.checkCamImage();
            camImage.update(currentImage);
            return camImage;
        }
        // TODO: exceptions !!!
        return null;
    }

    @Override
    public void close() {
        this.setClosing();
        if (grabber != null) {
            try {
                grabber.stop();
                System.out.println("Stopping grabber (OpencV)");
               
            } catch (Exception e) {
                System.out.println("Impossible to close " + e);
            }
        }
    }

}
