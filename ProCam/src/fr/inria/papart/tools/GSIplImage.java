/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.tools;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.lang.reflect.Method;
import java.nio.*;
import java.util.LinkedList;
import java.util.NoSuchElementException;


/**
 * // Insipired by GLTexture implementation.
 *
 * @author jiii
 */
public class GSIplImage {

    // For direct buffer copy.
    protected Method disposePixelsMethod = null;
    protected Object diposePixelsHandler = null;
    protected LinkedList<PixelData> pixelBuffer = null;
    protected int maxBuffSize = 3;
    protected boolean disposeFramesWhenPixelBufferFull = false;
    protected IplImage image;
    int width, height;

    public GSIplImage(int width, int height) {

        this.width = width;
        this.height = height;

        image = IplImage.create(width, height, opencv_core.IPL_DEPTH_8U, 4);
    }

    public IplImage getImage() {
        return image;
    }

    /**
     * This is the method used by the pixel source object to add frames to the
     * buffer.
     *
     * @param natBuf Object
     * @param rgbBuf IntBuffer
     * @param w int
     * @param h int
     */
    public void addPixelsToBuffer(Object natBuf, IntBuffer rgbBuf, int w, int h) {
        if (pixelBuffer == null) {
            pixelBuffer = new LinkedList<PixelData>();
        }

        if (pixelBuffer.size() + 1 <= maxBuffSize) {
            pixelBuffer.add(new PixelData(natBuf, rgbBuf, w, h));
        } else if (disposeFramesWhenPixelBufferFull) {
            // The buffer reached the maximum size, so we just dispose the new frame.
            try {
                disposePixelsMethod.invoke(diposePixelsHandler, new Object[]{natBuf});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Direct buffer input.  
    /**
     * Sets obj as the pixel source for this texture. The object must have a
     * public method called disposeBuffer(Object obj) that the texture will use
     * to release the int buffers after they are copied to the texture.
     *
     * @param obj Object
     */
    public void setPixelSource(Object obj) {
        diposePixelsHandler = obj;
        try {
            disposePixelsMethod = obj.getClass().getMethod("disposeBuffer", new Class[]{Object.class});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * If there are frames stored in the buffer, it removes the last and copies
     * the pixels to the texture. It returns true if that is the case, otherwise
     * it returns false.
     *
     * @return boolean
     */
    public boolean putPixelsToImage() {
        if (pixelBuffer != null && 0 < pixelBuffer.size() && disposePixelsMethod != null) {

            PixelData data = null;
            try {
                data = pixelBuffer.remove(0);
            } catch (NoSuchElementException ex) {
                System.err.println("Don't have pixel data to copy to texture");
            }

            if (data != null) {
//                if ((data.w != width) || (data.h != height)) {
//                    init(data.w, data.h, new GLTextureParameters());
//                }
//image.getIntBuffer().array();
                
                ByteBuffer imageBuffer = image.getByteBuffer();
                ByteBuffer incomingBuffer = ((org.gstreamer.Buffer) data.natBuf).getByteBuffer();

//                System.out.println("Size of incoming data " + incomingBuffer.remaining());
//                System.out.println("Size of image data " + imageBuffer.remaining());
                
                imageBuffer.put(incomingBuffer);

                data.dispose();

                return true;
            } else {
                return false;
            }

        }
        return false;
    }

    /**
     * This class stores a frame to be copied to the texture.
     *
     */
    protected class PixelData {

        int w, h;
        // Native buffer object.
        Object natBuf;
        // Buffer viewed as int.
        IntBuffer rgbBuf;

        PixelData(Object nat, IntBuffer rgb, int w, int h) {
            natBuf = nat;
            rgbBuf = rgb;
            this.w = w;
            this.h = h;
        }

        void dispose() {
            try {
                // Disposing the native buffer.        
                disposePixelsMethod.invoke(diposePixelsHandler, new Object[]{natBuf});
                natBuf = null;
                rgbBuf = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
