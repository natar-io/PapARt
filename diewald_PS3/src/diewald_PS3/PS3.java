/**
 * diewald_PS3 - Processing Library.
 *
 * this processing-library provides access to sonys PS3eye camera.
 *
 * its basically a wrapper of the ps3-library from CodeLaboratories.
 * http://codelaboratories.com/products/eye/driver/
 * http://codelaboratories.com/products/eye/sdk/
 *
 *
 * Copyright (c) 2011 Thomas Diewald
 *
 *
 * This source is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * A copy of the GNU General Public License is available on the World Wide Web
 * at <http://www.gnu.org/copyleft/gpl.html>. You can also obtain it by writing
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */
package diewald_PS3;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import diewald_PS3.constants.COLOR_MODE;
import diewald_PS3.constants.PS3_PARAM;
import diewald_PS3.constants.VIDEO_MODE;
import diewald_PS3.logger.PS3Logger;
import processing.core.PImage;

/**
 * PS3 is the main entry class of the library.<br>
 * <br>
 * it creates a PS3eye instance, and provides access to different parameters for
 * the video-stream.<br>
 *
 *
 * @author thomas diewald (c) 2011
 *
 */
public class PS3 {

    private static final ArrayList<PS3> PS3_LIST_ = new ArrayList<PS3>();

    private static PS3_Library.CLEyeMulticam LIBRARY;

    static {
        if (libraryLoaded()) {
            LIBRARY = PS3_Library.get();
        }
    }

    private int index_;
    private VIDEO_MODE mode_;
    private COLOR_MODE color_mode_;
    private int frameRate_;

    private int width_, height_;

    private PS3_Library.GUID guid_;
    private PS3_Library.Camera camera_;

    private ByteBuffer pixel_buffer_;
    private IplImage iplImage;
    private boolean hasNewImage = false;

    private int pixels_[];

    /**
     * returns number of available cameras.
     *
     * @return number of available cameras.
     */
    public static final int getCameraCount() {
        if (!libraryLoaded()) {
            return -1;
        }
        return LIBRARY.CLEyeGetCameraCount();
    }

    /**
     * method, to create a PS3-instance from a given index.
     *
     *
     * @param index the index of the ps3-device.
     * @return a ps3-instance on success. null on failure.
     */
    public static final PS3 create(int index) {
        if (!libraryLoaded()) {
            System.out.println("PS3-ERROR: cannot create camera, dll not loaded");
            return null;
        }

        PS3_Library.GUID guid = LIBRARY.CLEyeGetCameraUUID(index);
        if (guid.Data1 == 0) {
            System.out.println("PS3-ERROR: index(" + index + ") is not valid");
            return null;
        }
        return new PS3(index, guid);
    }

    // constructor
    private PS3() {
    }

    private PS3(int index, PS3_Library.GUID guid) {
        index_ = index;
        guid_ = guid;
        init(VIDEO_MODE.VGA, COLOR_MODE.COLOR_PROCESSED, 30);
    }

    /**
     * define a mode, specified by the video-mode (QVGA, or VGA), color-mode and
     * a framerate. the default mode is:<br>
     * init(VIDEOMODE.VGA, COLORMODE.COLOR_PROCESSED, 30);
     *
     * @param mode video-mode.
     * @param color_mode color-mode.
     * @param frameRate framerate.
     * @return the current instance.
     */
    public final PS3 init(VIDEO_MODE mode, COLOR_MODE color_mode, int frameRate) {
        if (guid_ == null) {
            return null;
        }
        if (camera_ != null) {
            destroy();
        }

        mode_ = mode;
        color_mode_ = color_mode;
        frameRate_ = frameRate;

        camera_ = LIBRARY.CLEyeCreateCamera(
                guid_,
                color_mode_.getIndex(),
                mode_.getIndex(),
                frameRate_
        );
        PS3_Library.Dimension width = new PS3_Library.Dimension();
        PS3_Library.Dimension height = new PS3_Library.Dimension();;
        LIBRARY.CLEyeCameraGetFrameDimensions(camera_, width, height);
        width_ = width.getValue();
        height_ = height.getValue();
        pixels_ = new int[width_ * height_];
        pixel_buffer_ = ByteBuffer.allocateDirect(width_ * height_ * color_mode_.getSize());
        setLed(true);
//    System.out.println("guid_.Data1 = "+guid_.Data1);
//    System.out.println("VIDEOMODE = "+mode);
//    System.out.println("COLORMODE = "+color_mode);
//    System.out.println("frameRate = "+frameRate);
//    System.out.println("width_    = "+width_);
//    System.out.println("height_   = "+height_);
        PS3_LIST_.add(this);

        opencv_core.CvSize outSize = new opencv_core.CvSize();
        outSize.width(width_);
        outSize.height(height_);
        iplImage = cvCreateImage(outSize, // size
                opencv_core.IPL_DEPTH_8U, // depth
                3);

        return this;
    }

    static public IplImage createImageFrom(IplImage imgIn, PImage Pout) {
        // TODO: avoid this creation !!
        opencv_core.CvSize outSize = new opencv_core.CvSize();
        outSize.width(Pout.width);
        outSize.height(Pout.height);
        IplImage imgOut = cvCreateImage(outSize, // size
                imgIn.depth(), // depth
                imgIn.nChannels());
//        imgIn.w
        return imgOut;
    }

    /**
     * check if the library got loaded.
     *
     * @return true if the library got loaded successfully.
     */
    public static final boolean libraryLoaded() {
        return PS3_Library.loaded();
    }

    /**
     * load the library with a custom path and name.
     *
     * @param dll_path path of the dll.
     * @param dll_name name of the dll.
     */
    public static final void loadLibrary(String dll_path, String dll_name) {
        PS3_Library.loadLibrary(dll_path, dll_name);
    }

    /**
     * get the index of the current instance.
     *
     * @return the index.
     */
    public final int getIndex() {
        return index_;
    }

    /**
     * get the width of the current instance.
     *
     * @return the width.
     */
    public final int getWidth() {
        return width_;
    }

    /**
     * get the height of the current instance.
     *
     * @return the height.
     */
    public final int getHeight() {
        return height_;
    }

    /**
     * copy the pixels of the current ps3-frame to the given pixel-array.
     * pixels[] has to have the same length as the frame of the current
     * instance. e.g. int pixels[] = new int[ps3.getWidth() * ps3.getHeight() ];
     * or just pass the pixels of a PImage instance.
     *
     * @param pixels
     */
    synchronized public final void getFrame(int pixels[]) {
//    synchronized (this){
        System.arraycopy(pixels_, 0, pixels, 0, pixels_.length);
//    }
    }

    /**
     */
    synchronized public final IplImage getIplImage() {
//    synchronized (this){
        if (hasNewImage) {
            hasNewImage = false;
            return iplImage;
        } else {
            return null;
        }
//    }
    }

    synchronized private final boolean updateFrame(int pixels[]) {
        if (pixels.length != pixel_buffer_.capacity() / color_mode_.getSize()) {
            return false;
        }

        LIBRARY.CLEyeCameraGetFrame(camera_, pixel_buffer_, 2000);

        // Copy in the IplImage
        ByteBuffer bb = iplImage.getByteBuffer();
        bb.rewind();
        bb.put(pixel_buffer_);
        hasNewImage = true;

        int s = color_mode_.getSize();
//    synchronized (this){
        if (s == 4) {
            int r, g, b, idx;
            for (int i = 0; i < pixels.length; i++) {
                idx = i * s;
                b = pixel_buffer_.get(idx + 0) & 0xFF;
                g = pixel_buffer_.get(idx + 1) & 0xFF;
                r = pixel_buffer_.get(idx + 2) & 0xFF;
                //        a = pixel_buffer_.get(i*s+0) & 0xFF;
                pixels[i] = 0xFF000000 | r << 16 | g << 8 | b << 0;
            }
        }
        if (s == 1) {
            int gray;
            for (int i = 0; i < pixels.length; i++) {
                gray = pixel_buffer_.get(i * s) & 0xFF;
                pixels[i] = 0xFF000000 | gray << 16 | gray << 8 | gray << 0;
            }
        }
//    }
        return true;
    }

    /**
     * start the ps3-device.
     */
    public final void start() {
        if (!event_thread_.active_) {
            LIBRARY.CLEyeCameraStart(camera_);
            event_thread_.startThread();
        }
    }

    /**
     * stop the ps3-device.
     */
    public final void stop() {
        if (event_thread_.active_) {
            event_thread_.stopThread();
            LIBRARY.CLEyeCameraStop(camera_);
        }
    }

    /**
     * completely destroys an ps3-context. to restart the current instance: use
     * init(...);
     */
    public final void destroy() {
        stop();
        LIBRARY.CLEyeDestroyCamera(camera_);
        camera_ = null;
        PS3_LIST_.remove(this);
    }

    /**
     * shutdown all running ps3-threads. use this method before you exit the
     * application.
     */
    public static void shutDown() {
        for (int i = PS3_LIST_.size() - 1; i >= 0; i--) {
            PS3 instance = PS3_LIST_.get(i);
            instance.setLed(false);
            instance.destroy();
        }
        PS3Logger.log(PS3Logger.TYPE.DEBUG, null, "PS3 SHUTDOWN");
    }

    /**
     * change the status of the red LED.
     *
     * @param on_off status of the led.
     */
    public final void setLed(boolean on_off) {
        LIBRARY.CLEyeCameraLED(camera_, on_off ? 1 : 0);
    }

    /**
     * set custom camera-parameters.
     *
     * @param param the parameter (enum).
     * @param value the new value of the parameter.
     */
    public final void setParameter(PS3_PARAM param, int value) {
        if (value > param.getMaxVal()) {
            value = param.getMaxVal();
        }
        if (value < param.getMinVal()) {
            value = param.getMinVal();
        }
        LIBRARY.CLEyeSetCameraParameter(camera_, param.getIndex(), value);
    }

    /**
     * get the current value of a parameter.
     *
     * @param param the parameter to get the value from.
     * @return the current alue of the given parameter.
     */
    public final int getParameter(PS3_PARAM param) {
        return LIBRARY.CLEyeGetCameraParameter(camera_, param.getIndex());
    }

//------------------------------------------------------------------------------ 
    // TO STRING
    @Override
    public String toString() {
        String class_name_ = this.getClass().getSuperclass().getSimpleName();
        if (this.getClass().getSuperclass() == Object.class) {
            class_name_ = this.getClass().getSimpleName();
        }

        String name = String.format("%-3s", class_name_);
        String index = String.format("%2d", this.getIndex());
        String code = String.format("@%-7s", Integer.toHexString(this.hashCode()));
        return name + " . index:" + index + " . " + code;
    }

    private final EventThread event_thread_ = new EventThread();

    private final class EventThread implements Runnable {

        private boolean active_ = false;

        public EventThread() {
        }
        public Thread thread_;

        public final void startThread() {
            active_ = true;
            thread_ = new Thread(this);
            thread_.start();
        }

        public final void stopThread() {
            this.active_ = false;
            if (thread_ != null) {
                try {
                    thread_.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public final void run() {
            PS3Logger.log(PS3Logger.TYPE.INFO, null, "STARTED Thread: updateFrame(...)");

            while (active_) {
                updateFrame(pixels_);
                Thread.yield();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    } // class EventThread implements Runnable

}
