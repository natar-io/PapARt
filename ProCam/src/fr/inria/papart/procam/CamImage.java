/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import org.bytedeco.javacpp.opencv_core.IplImage;

// To remove :::
//import org.gstreamer.Pipeline;
//import org.gstreamer.elements.BufferDataAppSink;
//import org.gstreamer.elements.RGBDataAppSink;
import processing.core.PApplet;

import static processing.core.PConstants.ARGB;
import static processing.core.PConstants.RGB;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;

/**
 *
 * @author jiii
 */
public class CamImage extends PImage {

    protected static boolean useResMacHack = true;

    public float frameRate;

    protected Method captureEventMethod;
    protected Object eventHandler;

    protected Object bufferSink = null;
    protected Method sinkCopyMethod;
    protected Method sinkSetMethod;
    protected Method sinkDisposeMethod;
    protected Method sinkGetMethod;
    protected String copyMask;

    protected ByteBuffer natBuffer = null;
    protected ByteBuffer argbBuffer;

    public CamImage(PApplet parent, int width, int height) {
        this(parent, width, height, ARGB);
    }

    public CamImage(PApplet parent, int width, int height, int format) {
        super(width, height, format);
        camInit(parent);
    }

    public CamImage(PApplet parent, Image img) {
        super(img);
        camInit(parent);
    }

    public CamImage(PApplet parent, IplImage img) {
        this(parent, img.width(), img.height());
    }

    protected final void camInit(PApplet parent) {
        this.parent = parent;

        Texture tex = ((PGraphicsOpenGL) parent.g).getTexture(this);
        tex.setBufferSource(this);
        // Second time with bufferSource.
        tex = ((PGraphicsOpenGL) parent.g).getTexture(this);

        argbBuffer = ByteBuffer.allocateDirect(this.pixels.length * 4);
    }

    public synchronized void update(IplImage iplImage) {

        Texture tex = ((PGraphicsOpenGL) parent.g).getTexture(this);
        ByteBuffer bgrBuffer = iplImage.getByteBuffer();
        Utils.byteBufferBRGtoARGB(bgrBuffer, argbBuffer);
        tex.copyBufferFromSource(null, argbBuffer, width, height);
    }
    
    
    public synchronized void disposeBuffer(Object buf) {

    }
       
}