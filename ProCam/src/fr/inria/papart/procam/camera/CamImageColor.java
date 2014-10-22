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
