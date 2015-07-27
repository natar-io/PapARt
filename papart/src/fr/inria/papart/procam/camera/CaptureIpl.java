/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import java.nio.ByteBuffer;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.video.Capture;

/**
 *
 * @author jeremylaviole
 * 
 * The same as Capture, except that the read method also 
 * copies the image into an IplImage. 
 * 
 */
public class CaptureIpl extends Capture implements PConstants {

    protected IplImage iplImage;

    public CaptureIpl(PApplet parent, int requestWidth, int requestHeight) {
        super(parent, requestWidth, requestHeight);

        init();
    }

    public CaptureIpl(PApplet parent, int requestWidth, int requestHeight,
            String cameraName) {
        super(parent, requestWidth, requestHeight, cameraName);
        init();

    }

    private void init() {

        iplImage = IplImage.create(width, height, opencv_core.IPL_DEPTH_8U, 4);
    }

    @Override
    public synchronized void read() {
        if (frameRate < 0) {
            // Framerate not set yet, so we obtain from stream,
            // which is already playing since we are in read().
            frameRate = getSourceFrameRate();
        }

        // Try catch to allow proper exit. 
        try {
            // TODO: check the sinkcopy methods to do this.
            ByteBuffer byteBuffer = natBuffer.getByteBuffer();
            ByteBuffer imageBuffer = iplImage.getByteBuffer();
            ByteBuffer incomingBuffer = byteBuffer;
            imageBuffer.put(incomingBuffer);

            byteBuffer.rewind();
            imageBuffer.rewind();


            if (useBufferSink) { // The native buffer from gstreamer is copied to the buffer sink.
                if (natBuffer == null) {
                    return;
                }

                if (firstFrame) {
                    super.init(bufWidth, bufHeight, ARGB);
                    firstFrame = false;
                }

                if (bufferSink == null) {
                    Object cache = parent.g.getCache(this);
                    if (cache == null) {
                        return;
                    }
                    setBufferSink(cache);
                    getSinkMethods();
                }

                // ByteBuffer byteBuffer = natBuffer.getByteBuffer();

                try {
                    sinkCopyMethod.invoke(bufferSink,
                            new Object[]{natBuffer, byteBuffer, bufWidth, bufHeight});
                } catch (Exception e) {
                    e.printStackTrace();
                }


                natBuffer = null;
            } else { // The pixels just read from gstreamer are copied to the pixels array.
                if (copyPixels == null) {
                    return;
                }

                if (firstFrame) {
                    super.init(bufWidth, bufHeight, RGB);
                    firstFrame = false;
                }

                int[] temp = pixels;
                pixels = copyPixels;
                updatePixels();
                copyPixels = temp;
            }

        } catch (Exception e) {
            System.out.println("Error");
        }

        available = false;
        newFrame = true;
    }
    
    @Override
    public void stop(){
        super.stop();
        iplImage.release();
    }

    public IplImage getIplImage() {
        return this.iplImage;
    }
}
