/*
 * Copyright (C) 2016 jiii.
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

import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class SubCamera extends Camera {

    public enum Type {
        IR, COLOR, DEPTH
    }

    protected CameraRGBIRDepth mainCamera;
    public Type type;

    public SubCamera(CameraRGBIRDepth mainCamera) {
        this.mainCamera = mainCamera;
    }

    public Camera getMainCamera() {
        return mainCamera;
    }

    @Override
    public void start() {
        if(!mainCamera.isStarting){
          throw new RuntimeException("Cannot start a subCamera: start the main camera instead.");
        }
        mainCamera.start(this);
        isConnected = true;
    }

    @Override
    public void grab() {
        if (isConnected) {
            mainCamera.grab(this);
        }
    }

    @Override
    public void close() {
        this.setClosing();
        this.isConnected = false;
        mainCamera.disable(this);
    }

    @Override
    public PImage getPImage() {
        this.checkCamImage();
        if (currentImage != null) {
            camImage.update(currentImage);
            currentImage = null;
            return camImage;
        }
        // TODO: exceptions !!!
        return null;
    }
}
