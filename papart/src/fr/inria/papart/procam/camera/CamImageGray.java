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
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.Utils;
import java.awt.Image;
import java.nio.ByteBuffer;
import org.bytedeco.javacpp.opencv_core;
import processing.core.PApplet;
import static processing.core.PConstants.ARGB;
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

        System.out.println("ICI 1");
        Texture tex = ((PGraphicsOpenGL) parent.g).getTexture(this);
        ByteBuffer buffer = iplImage.getByteBuffer();
        System.out.println("ICI 2");
        
//         Utils.byteBufferBRGtoARGB(bgrBuffer, argbBuffer);
        tex.copyBufferFromSource(null, buffer, width, height);
        
        System.out.println("ICI 3");
        buffer.rewind();
    }

}
