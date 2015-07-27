/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.Utils;
import java.awt.Image;
import java.nio.ByteBuffer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import static processing.core.PConstants.ARGB;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class CamImageColor extends CamImage {

    protected ByteBuffer argbBuffer;

        
    public CamImageColor(PApplet parent, Image img) {
        super(parent, img);
    }
    
    public CamImageColor(PApplet parent, int width, int height) {
        super(parent, width, height, ARGB);
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

        argbBuffer = ByteBuffer.allocateDirect(this.pixels.length * 4);
    }

    @Override
    public void update(IplImage iplImage) {
        Texture tex = ((PGraphicsOpenGL) parent.g).getTexture(this);
        ByteBuffer bgrBuffer = iplImage.getByteBuffer();
        Utils.byteBufferBRGtoARGB(bgrBuffer, argbBuffer);
        tex.copyBufferFromSource(null, argbBuffer, width, height);
    }

}
