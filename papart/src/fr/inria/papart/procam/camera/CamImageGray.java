/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import java.awt.Image;
import java.nio.ByteBuffer;
import org.bytedeco.javacpp.opencv_core;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class CamImageGray extends CamImage {

    public CamImageGray(PApplet parent, Image img) {
        super(parent, img);
    }

    public CamImageGray(PApplet parent, int width, int height) {
        super(parent, width, height, GRAY);
    }

    @Override
    protected final void camInit(PApplet parent) {
        this.parent = parent;
        Texture tex = ((PGraphicsOpenGL) parent.g).getTexture(this);
        if (tex == null) {
            throw new RuntimeException("CamImage: Impossible to get the Processing Texture. "
                    + "Check the size arguments, or input image.");
        }
        tex.setBufferSource(this);
        // Second time with bufferSource.
        tex = ((PGraphicsOpenGL) parent.g).getTexture(this);

//        imageBuffer = ByteBuffer.allocateDirect(this.pixels.length);
    }

    @Override
    public void update(opencv_core.IplImage iplImage) {

        Texture tex = ((PGraphicsOpenGL) parent.g).getTexture(this);
        ByteBuffer buffer = iplImage.getByteBuffer();
        
//         Utils.byteBufferBRGtoARGB(bgrBuffer, argbBuffer);
        tex.copyBufferFromSource(null, buffer, width, height);
        buffer.rewind();
    }

}
