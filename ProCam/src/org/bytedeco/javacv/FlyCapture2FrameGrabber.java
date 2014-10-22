/*
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.bytedeco.javacv;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacv.FrameGrabber.SENSOR_PATTERN_BGGR;
import static org.bytedeco.javacv.FrameGrabber.SENSOR_PATTERN_GBRG;
import static org.bytedeco.javacv.FrameGrabber.SENSOR_PATTERN_GRBG;
import static org.bytedeco.javacv.FrameGrabber.SENSOR_PATTERN_RGGB;
import org.bytedeco.javacpp.FlyCapture2.Error;
import static org.bytedeco.javacpp.FlyCapture2.*;
import static org.bytedeco.javacpp.PGRFlyCapture.FLYCAPTURE_STIPPLEDFORMAT_BGGR;
import static org.bytedeco.javacpp.PGRFlyCapture.FLYCAPTURE_STIPPLEDFORMAT_GBRG;
import static org.bytedeco.javacpp.PGRFlyCapture.FLYCAPTURE_STIPPLEDFORMAT_GRBG;
import static org.bytedeco.javacpp.PGRFlyCapture.FLYCAPTURE_STIPPLEDFORMAT_RGGB;
import org.bytedeco.javacpp.annotation.Cast;
import static org.bytedeco.javacv.FrameGrabber.SENSOR_PATTERN_BGGR;
import static org.bytedeco.javacv.FrameGrabber.SENSOR_PATTERN_GBRG;
import static org.bytedeco.javacv.FrameGrabber.SENSOR_PATTERN_GRBG;
import static org.bytedeco.javacv.FrameGrabber.SENSOR_PATTERN_RGGB;

/**
 *
 * @author Samuel Audet
 */
public class FlyCapture2FrameGrabber extends FrameGrabber {

    public static String[] getDeviceDescriptions() throws FrameGrabber.Exception {
        tryLoad();

        int[] numCameras = new int[1];
        busMgr.GetNumOfCameras(numCameras);

        System.out.println("Number of cameras detected: " + numCameras[0]);

        String[] descriptions = new String[numCameras[0]];

        for (int i = 0; i < numCameras[0]; i++) {
            PGRGuid guid = new PGRGuid();
            error = busMgr.GetCameraFromIndex(i, guid);
            if (error.notEquals(PGRERROR_OK)) {
                PrintError(error);
                System.exit(-1);
            }

            Camera cam = new Camera();
            // Connect to a camera
            error = cam.Connect(guid);
            if (error.notEquals(PGRERROR_OK)) {
                PrintError(error);
            }

            // Get the camera information
            CameraInfo camInfo = new CameraInfo();
            error = cam.GetCameraInfo(camInfo);
            if (error.notEquals(PGRERROR_OK)) {
                PrintError(error);
            }
            descriptions[i] = CameraInfo(camInfo);
        }

        return descriptions;
    }

    static void PrintError(Error error) {
        error.PrintErrorTrace();
    }

    static String CameraInfo(CameraInfo pCamInfo) {
        return "\n*** CAMERA INFORMATION ***\n"
                + "Serial number - " + pCamInfo.serialNumber() + "\n"
                + "Camera model - " + pCamInfo.modelName().getString() + "\n"
                + "Camera vendor - " + pCamInfo.vendorName().getString() + "\n"
                + "Sensor - " + pCamInfo.sensorInfo().getString() + "\n"
                + "Resolution - " + pCamInfo.sensorResolution().getString() + "\n"
                + "Firmware version - " + pCamInfo.firmwareVersion().getString() + "\n"
                + "Firmware build time - " + pCamInfo.firmwareBuildTime().getString() + "\n";
    }

    public static FlyCaptureFrameGrabber createDefault(File deviceFile) throws FrameGrabber.Exception {
        return null;
    }

    public static FlyCaptureFrameGrabber createDefault(String devicePath) throws FrameGrabber.Exception {
        return null;
    }

    public static FlyCaptureFrameGrabber createDefault(int deviceNumber) throws FrameGrabber.Exception {
        return new FlyCaptureFrameGrabber(deviceNumber);
    }

    private static FrameGrabber.Exception loadingException = null;

    public static void tryLoad() throws FrameGrabber.Exception {
        if (loadingException != null) {
            throw loadingException;
        } else {
            try {
                Loader.load(org.bytedeco.javacpp.FlyCapture2.class);
            } catch (Throwable t) {
                throw loadingException = new FrameGrabber.Exception("Failed to load " + FlyCapture2FrameGrabber.class, t);
            }
        }
    }

    public FlyCapture2FrameGrabber(int deviceNumber) throws FrameGrabber.Exception {
        int[] numCameras = new int[1];
        busMgr.GetNumOfCameras(numCameras);

        // Get the camera
        PGRGuid guid = new PGRGuid();
        error = busMgr.GetCameraFromIndex(deviceNumber, guid);
        if (error.notEquals(PGRERROR_OK)) {
            PrintError(error);
            System.exit(-1);
        }

        camera = new Camera();

        // Connect to a camera
        error = camera.Connect(guid);
        if (error.notEquals(PGRERROR_OK)) {
            PrintError(error);
        }

        // Get the camera information
        cameraInfo = new CameraInfo();
        error = camera.GetCameraInfo(cameraInfo);
        if (error.notEquals(PGRERROR_OK)) {
            PrintError(error);
        }

    }

    public void release() throws FrameGrabber.Exception {
        if (camera != null) {
            stop();
            camera.Disconnect();
            camera = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    public static final int INITIALIZE = 0x000,
            TRIGGER_INQ = 0x530,
            IS_CAMERA_POWER = 0x400,
            CAMERA_POWER = 0x610,
            SOFTWARE_TRIGGER = 0x62C,
            SOFT_ASYNC_TRIGGER = 0x102C,
            IMAGE_DATA_FORMAT = 0x1048;

    private static BusManager busMgr = new BusManager();
    private static Error error;
    private Camera camera;
    private CameraInfo cameraInfo;
    private Image raw_image = new Image();
    private Image conv_image = new Image();
    private IplImage temp_image, return_image = null;
    private final int[] regOut = new int[1];
    private final float[] outFloat = new float[1];
    private final float[] gammaOut = new float[1];

    @Override
    public double getGamma() {
        return Float.isNaN(gammaOut[0]) || Float.isInfinite(gammaOut[0]) || gammaOut[0] == 0.0f ? 2.2 : gammaOut[0];
    }

    @Override
    public int getImageWidth() {
        return return_image == null ? super.getImageWidth() : return_image.width();
    }

    @Override
    public int getImageHeight() {
        return return_image == null ? super.getImageHeight() : return_image.height();
    }

    @Override
    public double getFrameRate() {  // TODO: check this. 
//        if (context == null || context.isNull()) {
        return super.getFrameRate();
//        } else {
//            flycaptureGetCameraAbsProperty(context, FRAME_RATE, outFloat);
//            return outFloat[0];
//        }
    }

    @Override
    public void setImageMode(FrameGrabber.ImageMode imageMode) {
        if (imageMode != this.imageMode) {
            temp_image = null;
            return_image = null;
        }
        super.setImageMode(imageMode);
    }
    static final int VIDEOMODE_ANY = -1;

    public void start() throws FrameGrabber.Exception {
        int f = FRAMERATE_30;  // TODO: Default 30 ? 
        if (frameRate <= 0) {
            f = FRAMERATE_30;
        } else if (frameRate <= 1.876) {
            f = FRAMERATE_1_875;
        } else if (frameRate <= 3.76) {
            f = FRAMERATE_3_75;
        } else if (frameRate <= 7.51) {
            f = FRAMERATE_7_5;
        } else if (frameRate <= 15.01) {
            f = FRAMERATE_15;
        } else if (frameRate <= 30.01) {
            f = FRAMERATE_30;
        } else if (frameRate <= 60.01) {
            f = FRAMERATE_60;
        } else if (frameRate <= 120.01) {
            f = FRAMERATE_120;
        } else if (frameRate <= 240.01) {
            f = FRAMERATE_240;
        }

        int c = VIDEOMODE_ANY;
        if (imageMode == FrameGrabber.ImageMode.COLOR || imageMode == FrameGrabber.ImageMode.RAW) {
            if (imageWidth <= 0 || imageHeight <= 0) {
                c = VIDEOMODE_ANY;
            } else if (imageWidth <= 640 && imageHeight <= 480) {
                c = VIDEOMODE_640x480RGB;
            } else if (imageWidth <= 800 && imageHeight <= 600) {
                c = VIDEOMODE_800x600RGB;
            } else if (imageWidth <= 1024 && imageHeight <= 768) {
                c = VIDEOMODE_1024x768RGB;
            } else if (imageWidth <= 1280 && imageHeight <= 960) {
                c = VIDEOMODE_1280x960RGB;
            } else if (imageWidth <= 1600 && imageHeight <= 1200) {
                c = VIDEOMODE_1600x1200RGB;
            }
        } else if (imageMode == FrameGrabber.ImageMode.GRAY) {
            if (imageWidth <= 0 || imageHeight <= 0) {
                c = VIDEOMODE_ANY;
            } else if (imageWidth <= 640 && imageHeight <= 480) {
                c = bpp > 8 ? VIDEOMODE_640x480Y16 : VIDEOMODE_640x480Y8;
            } else if (imageWidth <= 800 && imageHeight <= 600) {
                c = bpp > 8 ? VIDEOMODE_800x600Y16 : VIDEOMODE_800x600Y8;
            } else if (imageWidth <= 1024 && imageHeight <= 768) {
                c = bpp > 8 ? VIDEOMODE_1024x768Y16 : VIDEOMODE_1024x768Y8;
            } else if (imageWidth <= 1280 && imageHeight <= 960) {
                c = bpp > 8 ? VIDEOMODE_1280x960Y16 : VIDEOMODE_1280x960Y8;
            } else if (imageWidth <= 1600 && imageHeight <= 1200) {
                c = bpp > 8 ? VIDEOMODE_1600x1200Y16 : VIDEOMODE_1600x1200Y8;
            }
        }

        // set or reset trigger mode
        int[] iPolarity = new int[1];
        int[] iSource = new int[1];
        int[] iRawValue = new int[1];
        int[] iMode = new int[1];

        // TODO: convert this to FlyCapture2
//        int error = flycaptureGetTrigger(context, null, iPolarity, iSource, iRawValue, iMode, null);
//        if (error != PGRERROR_OK) {
//            throw new FrameGrabber.Exception("flycaptureGetTrigger() Error " + error);
//        }
//        error = flycaptureSetTrigger(context, triggerMode, iPolarity[0], 7, 14, 0);
//        if (error != PGRERROR_OK) {
//            // try with trigger mode 0 instead
//            error = flycaptureSetTrigger(context, true, iPolarity[0], 7, 0, 0);
//        }
//        if (error != PGRERROR_OK) {
//            throw new FrameGrabber.Exception("flycaptureSetTrigger() Error " + error);
//        }
//        if (triggerMode) {
//            waitForTriggerReady();
//        }
//
//        // try to match the endianness to our platform
//        error = flycaptureGetCameraRegister(context, IMAGE_DATA_FORMAT, regOut);
//        if (error != PGRERROR_OK) {
//            throw new FrameGrabber.Exception("flycaptureGetCameraRegister() Error " + error);
//        }
//        int reg;
//        if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) {
//            reg = regOut[0] | 0x1;
//        } else {
//            reg = regOut[0] & ~0x1;
//        }
//        error = flycaptureSetCameraRegister(context, IMAGE_DATA_FORMAT, reg);
//        if (error != PGRERROR_OK) {
//            throw new FrameGrabber.Exception("flycaptureSetCameraRegister() Error " + error);
//        }
//
//        error = flycaptureSetBusSpeed(context, BUSSPEED_S_FASTEST, BUSSPEED_S_FASTEST);
//        if (error != PGRERROR_OK) {
//            error = flycaptureSetBusSpeed(context,
//                    BUSSPEED_ANY, BUSSPEED_ANY);
//            if (error != PGRERROR_OK) {
//                throw new FrameGrabber.Exception("flycaptureSetBusSpeed() Error " + error);
//            }
//        }
//
//        if (gamma != 0.0) {
//            error = flycaptureSetCameraAbsProperty(context, GAMMA, (float) gamma);
//            if (error != PGRERROR_OK) {
//                throw new FrameGrabber.Exception("flycaptureSetCameraAbsProperty() Error " + error + ": Could not set gamma.");
//            }
//        }
//        error = flycaptureGetCameraAbsProperty(context, GAMMA, gammaOut);
//        if (error != PGRERROR_OK) {
//            gammaOut[0] = 2.2f;
//        }
//
//        error = flycaptureStart(context, c, f);
//        if (error != PGRERROR_OK) {
//            throw new FrameGrabber.Exception("flycaptureStart() Error " + error);
//        }
//        // Start capturing images
        error = camera.StartCapture();
        if (error.notEquals(PGRERROR_OK)) {
            PrintError(error);
        }

//        
//        error = flycaptureSetGrabTimeoutEx(context, timeout);
//        if (error != PGRERROR_OK) {
//            throw new FrameGrabber.Exception("flycaptureSetGrabTimeoutEx() Error " + error);
//        }
    }

    public void stop() throws FrameGrabber.Exception {
        error = camera.StopCapture();
        if (error.notEquals(PGRERROR_OK)) {
            throw new FrameGrabber.Exception("flycapture camera StopCapture() Error " + error);
        }
        temp_image = null;
        return_image = null;
        timestamp = 0;
        frameNumber = 0;
    }

    public void trigger() throws FrameGrabber.Exception {
        throw new RuntimeException("Not implemented yet");
//        waitForTriggerReady();
//        int error = flycaptureSetCameraRegister(context, SOFT_ASYNC_TRIGGER, 0x80000000);
//        if (error != PGRERROR_OK) {
//            throw new FrameGrabber.Exception("flycaptureSetCameraRegister() Error " + error);
//        }
//        
    }

//    private void waitForTriggerReady() throws FrameGrabber.Exception {
//        // wait for trigger to be ready...
//        long time = System.currentTimeMillis();
//        do {
//            int error = flycaptureGetCameraRegister(context, SOFTWARE_TRIGGER, regOut);
//           
//            if (error != PGRERROR_OK) {
//                throw new FrameGrabber.Exception("flycaptureGetCameraRegister() Error " + error);
//            }
//            if (System.currentTimeMillis() - time > timeout) {
//                break;
//                //throw new Exception("waitForTriggerReady() Error: Timeout occured.");
//            }
//        } while ((regOut[0] >>> 31) != 0);
//    }
    private int getNumChannels(int pixelFormat) {
        switch (pixelFormat) {
            case PIXEL_FORMAT_BGR:
            case PIXEL_FORMAT_RGB8:
            case PIXEL_FORMAT_RGB16:
            case PIXEL_FORMAT_S_RGB16:
                return 3;

            case PIXEL_FORMAT_MONO8:
            case PIXEL_FORMAT_MONO16:
            case PIXEL_FORMAT_RAW8:
            case PIXEL_FORMAT_RAW16:
            case PIXEL_FORMAT_S_MONO16:
                return 1;

            case PIXEL_FORMAT_BGRU:
                return 4;

            case PIXEL_FORMAT_411YUV8:
            case PIXEL_FORMAT_422YUV8:
            case PIXEL_FORMAT_444YUV8:
            default:
                return -1;
        }
    }

    private int getDepth(int pixelFormat) {
        switch (pixelFormat) {
            case PIXEL_FORMAT_BGR:
            case PIXEL_FORMAT_RGB8:
            case PIXEL_FORMAT_MONO8:
            case PIXEL_FORMAT_RAW8:
            case PIXEL_FORMAT_BGRU:
                return IPL_DEPTH_8U;

            case PIXEL_FORMAT_MONO16:
            case PIXEL_FORMAT_RAW16:
            case PIXEL_FORMAT_RGB16:
                return IPL_DEPTH_16U;

            case PIXEL_FORMAT_S_MONO16:
            case PIXEL_FORMAT_S_RGB16:
                return IPL_DEPTH_16S;

            case PIXEL_FORMAT_411YUV8:
            case PIXEL_FORMAT_422YUV8:
            case PIXEL_FORMAT_444YUV8:
            default:
                return IPL_DEPTH_8U;
        }
    }
    
    private void setPixelFormat(Image image, int pixelFormat){
//        SetDimensions(
//                    @Cast("unsigned int") int rows,
//                    @Cast("unsigned int") int cols,
//                    @Cast("unsigned int") int stride,
//                    @Cast("FlyCapture2::PixelFormat") int pixelFormat, 
//                    @Cast("FlyCapture2::BayerTileFormat") int bayerFormat );
        image.SetDimensions(image.GetRows(),
                            image.GetCols(),
                            image.GetStride(), 
                            pixelFormat, 
                            image.GetBayerTileFormat());
    }
    
    private void setStride(Image image, int stride){
//        SetDimensions(
//                    @Cast("unsigned int") int rows,
//                    @Cast("unsigned int") int cols,
//                    @Cast("unsigned int") int stride,
//                    @Cast("FlyCapture2::PixelFormat") int pixelFormat, 
//                    @Cast("FlyCapture2::BayerTileFormat") int bayerFormat );
        image.SetDimensions(image.GetRows(),
                            image.GetCols(),
                            stride, 
                            image.GetPixelFormat(), 
                            image.GetBayerTileFormat());
    }
    

    public IplImage grab() throws FrameGrabber.Exception {
        System.out.println("LA 1");  
        error = camera.RetrieveBuffer(raw_image);
        if (error.notEquals(PGRERROR_OK)) {
            throw new FrameGrabber.Exception("flycaptureGrabImage2() Error " + error + " (Has start() been called?)");
        }
        System.out.println("LA 2");  
        int w = raw_image.GetCols();
        int h = raw_image.GetRows();
        int format = raw_image.GetPixelFormat();
        int depth = getDepth(format);
        int stride = raw_image.GetStride();
        int size = h * stride;
        int numChannels = getNumChannels(format);
         System.out.println("LA 3");  
        error = camera.ReadRegister(IMAGE_DATA_FORMAT, regOut);
         if (error.notEquals(PGRERROR_OK)) {
            throw new FrameGrabber.Exception("flycaptureGetCameraRegister() Error " + error);
        }
         System.out.println("LA 4");  
        ByteOrder frameEndian = (regOut[0] & 0x1) != 0
                ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        boolean alreadySwapped = false;

 System.out.println("LA 5");        
// TODO: check bayer 
//        boolean colorbayer = raw_image.bStippled();
        boolean colorbayer = false;
        
        boolean colorrgb = format == PIXEL_FORMAT_RGB8 || format == PIXEL_FORMAT_RGB16
                || format == PIXEL_FORMAT_BGR || format == PIXEL_FORMAT_BGRU;
        boolean coloryuv = format == PIXEL_FORMAT_411YUV8 || format == PIXEL_FORMAT_422YUV8
                || format == PIXEL_FORMAT_444YUV8;
        BytePointer imageData = raw_image.GetData();

        if ((depth == IPL_DEPTH_8U || frameEndian.equals(ByteOrder.nativeOrder()))
                && (imageMode == FrameGrabber.ImageMode.RAW || (imageMode == FrameGrabber.ImageMode.COLOR && numChannels == 3)
                || (imageMode == FrameGrabber.ImageMode.GRAY && numChannels == 1 && !colorbayer))) {
            if (return_image == null) {
                return_image = IplImage.createHeader(w, h, depth, numChannels);
            }
            return_image.widthStep(stride);
            return_image.imageSize(size);
            return_image.imageData(imageData);
        } else {
            if (return_image == null) {
                return_image = IplImage.create(w, h, depth, imageMode == FrameGrabber.ImageMode.COLOR ? 3 : 1);
            }
            if (temp_image == null) {
                if (imageMode == FrameGrabber.ImageMode.COLOR
                        && (numChannels > 1 || depth > 8) && !coloryuv && !colorbayer) {
                    temp_image = IplImage.create(w, h, depth, numChannels);
                } else if (imageMode == FrameGrabber.ImageMode.GRAY && colorbayer) {
                    temp_image = IplImage.create(w, h, depth, 3);
                } else if (imageMode == FrameGrabber.ImageMode.GRAY && colorrgb) {
                    temp_image = IplImage.createHeader(w, h, depth, 3);
                } else if (imageMode == FrameGrabber.ImageMode.COLOR && numChannels == 1 && !coloryuv && !colorbayer) {
                    temp_image = IplImage.createHeader(w, h, depth, 1);
                } else {
                    temp_image = return_image;
                }
            }
            // conv_image.iRowInc(temp_image.widthStep());
            //conv_image.pData(temp_image.imageData());

                        setStride(conv_image, temp_image.widthStep());
 
//           TODO: Size to check -> naive value set. 
            conv_image.SetData(temp_image.imageData(), temp_image.width() * temp_image.height() * temp_image.depth());
            
            if (depth == IPL_DEPTH_8U) {
                
                setPixelFormat(conv_image,imageMode == FrameGrabber.ImageMode.RAW ? PIXEL_FORMAT_RAW8
                        : temp_image.nChannels() == 1 ? PIXEL_FORMAT_MONO8 : PIXEL_FORMAT_BGR);
            } else {
                 setPixelFormat(conv_image,imageMode == FrameGrabber.ImageMode.RAW ? PIXEL_FORMAT_RAW16
                        : temp_image.nChannels() == 1 ? PIXEL_FORMAT_MONO16 : PIXEL_FORMAT_RGB16);
            }
            if (depth != IPL_DEPTH_8U && conv_image.GetPixelFormat() == format && conv_image.GetStride() == stride) {
                // we just need a copy to swap bytes..
//                ShortBuffer in = raw_image.getByteBuffer().order(frameEndian).asShortBuffer();
                ShortBuffer in = raw_image.GetData().asByteBuffer().order(frameEndian).asShortBuffer();
                
                ShortBuffer out = temp_image.getByteBuffer().order(ByteOrder.nativeOrder()).asShortBuffer();
                out.put(in);
                alreadySwapped = true;
            } else if ((imageMode == FrameGrabber.ImageMode.GRAY && colorrgb)
                    || (imageMode == FrameGrabber.ImageMode.COLOR && numChannels == 1 && !coloryuv && !colorbayer)) {
                temp_image.widthStep(stride);
                temp_image.imageSize(size);
                temp_image.imageData(imageData);
            } else if (!colorrgb && (colorbayer || coloryuv || numChannels > 1)) {
                
                error = raw_image.Convert(conv_image);
//                error = flycaptureConvertImage(context, raw_image, conv_image);
                if (error.notEquals(PGRERROR_OK)) {
                    throw new FrameGrabber.Exception("flycaptureConvertImage() Error " + error);
                }
            }
            if (!alreadySwapped && depth != IPL_DEPTH_8U
                    && !frameEndian.equals(ByteOrder.nativeOrder())) {
                
                // ack, the camera's endianness doesn't correspond to our machine ...
                // swap bytes of 16-bit images
                ByteBuffer bb = temp_image.getByteBuffer();
                ShortBuffer in = bb.order(frameEndian).asShortBuffer();
                ShortBuffer out = bb.order(ByteOrder.nativeOrder()).asShortBuffer();
                out.put(in);
            }
            if (imageMode == FrameGrabber.ImageMode.COLOR && numChannels == 1 && !coloryuv && !colorbayer) {
                cvCvtColor(temp_image, return_image, CV_GRAY2BGR);
            } else if (imageMode == FrameGrabber.ImageMode.GRAY && (colorbayer || colorrgb)) {
                cvCvtColor(temp_image, return_image, CV_BGR2GRAY);
            }
        }

       int bayerFormat = cameraInfo.bayerTileFormat();
       switch (bayerFormat) {
            case BGGR: sensorPattern = SENSOR_PATTERN_BGGR; break;
            case GBRG: sensorPattern = SENSOR_PATTERN_GBRG; break;
            case GRBG: sensorPattern = SENSOR_PATTERN_GRBG; break;
            case RGGB: sensorPattern = SENSOR_PATTERN_RGGB; break;
            default: sensorPattern = -1L;
        }
        
        TimeStamp timeStamp = raw_image.GetTimeStamp();
        timestamp = timeStamp.seconds() * 1000000L + timeStamp.microSeconds();
        return return_image;
    }
}
