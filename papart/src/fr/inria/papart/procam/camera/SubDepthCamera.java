/*
 * Copyright (C) 2016  RealityTech. 
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

import fr.inria.papart.multitouch.DepthTouchInput;
import fr.inria.papart.multitouch.TouchInput;
import org.bytedeco.javacpp.opencv_core;

/**
 *
 * @author Jeremy Laviole
 */
public class SubDepthCamera extends SubCamera implements WithTouchInput{
    
    private DepthTouchInput touchInput;

    public SubDepthCamera(CameraRGBIRDepth mainCamera) {
        super(mainCamera);
    }
    
    public SubDepthCamera(CameraRGBIRDepth mainCamera, Type type) {
        super(mainCamera, type);
    }
    
    @Override
    public DepthTouchInput getTouchInput() {
        return touchInput;
    }

    @Override
    public void setTouchInput(DepthTouchInput touchInput) {
        this.touchInput = touchInput;
    }

    @Override
    public void newTouchImageWithColor(opencv_core.IplImage colorImage) {
        if (touchInput != null) {
            touchInput.lock();
            touchInput.update();
            touchInput.getTouch2DColors(colorImage);
            touchInput.unlock();
        } else if (touchInput != null) {
            System.err.println("Error, the TouchInput is set, but no DepthImg is grabbed.");
        }
    }
    @Override
    public void newTouchImage() {
        if (touchInput != null) {
            touchInput.lock();
            touchInput.update();
            touchInput.unlock();
        } else if (touchInput != null) {
            System.err.println("Error, the TouchInput is set, but no DepthImg is grabbed.");
        }
    }
}
