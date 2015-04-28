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

import java.awt.Image;
import java.nio.ByteBuffer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public abstract class CamImage extends PImage {

    protected Object bufferSink = null;
    protected ByteBuffer natBuffer = null;

    protected CamImage(PApplet parent, int width, int height, int format) {
        super(width, height, format);
        camInit(parent);
    }

    public CamImage(PApplet parent, Image img) {
        super(img);
        camInit(parent);
    }

    protected abstract void camInit(PApplet parent);

    public abstract void update(IplImage iplImage);

    public synchronized void disposeBuffer(Object buf) {

    }

}
