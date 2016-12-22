/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.utils.ImageUtils;
import fr.inria.papart.utils.ARToolkitPlusUtils;
import java.awt.Image;
import java.nio.ByteBuffer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import static processing.core.PConstants.ARGB;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class CamImageColor extends CamImage {

    protected ByteBuffer argbBuffer;

    public CamImageColor(PApplet parent, Image img) {
        super(parent, img);
    }

    public CamImageColor(PApplet parent, int width, int height, Camera.PixelFormat format) {
        super(parent, width, height, ARGB, format);
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
        if (this.incomingFormat != Camera.PixelFormat.ARGB) {
            argbBuffer = ByteBuffer.allocateDirect(this.pixels.length * 4);
        }
    }

    @Override
    public void update(IplImage iplImage) {
        Texture tex = ((PGraphicsOpenGL) parent.g).getTexture(this);
        ByteBuffer imageBuffer = iplImage.getByteBuffer();

        if (this.incomingFormat == Camera.PixelFormat.BGR) {
            ImageUtils.byteBufferBRGtoARGB(imageBuffer, argbBuffer);
        }
        if (this.incomingFormat == Camera.PixelFormat.RGB) {
            ImageUtils.byteBufferRGBtoARGB(imageBuffer, argbBuffer);
        }
        if (this.incomingFormat == Camera.PixelFormat.ARGB) {
            argbBuffer = iplImage.getByteBuffer();
        }
        tex.copyBufferFromSource(null, argbBuffer, width, height);
        argbBuffer.rewind();
    }

}
