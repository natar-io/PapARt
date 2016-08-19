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

import fr.inria.papart.procam.camera.Camera.PixelFormat;
import java.awt.Image;
import java.nio.ByteBuffer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public abstract class CamImage extends PImage {

    protected Object bufferSink = null;
    protected ByteBuffer natBuffer = null;
    protected Camera.PixelFormat incomingFormat;

    protected CamImage(PApplet parent, int width, int height, int format, PixelFormat incomingFormat) {
        super(width, height, format);
        this.incomingFormat = incomingFormat;
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
