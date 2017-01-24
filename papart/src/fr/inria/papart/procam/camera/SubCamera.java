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

import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
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
    public SubCamera(CameraRGBIRDepth mainCamera, Type type) {
        this.mainCamera = mainCamera;
        this.type = type;
    }

    public Camera getMainCamera() {
        return mainCamera;
    }

    @Override
    public void start() {
        if (!mainCamera.isStarting) {
            throw new RuntimeException("Cannot start a subCamera: start the main camera instead.");
        }
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

    /**
     * It makes the camera update continuously.
     */
    public void setThread() {
        // thread must be of the main camera...
        mainCamera.setThread(this);
        
        if (thread == null) {
            thread = new CameraThread(this);
            thread.setCompute(this.trackSheets);
            thread.start();
        } else {
            System.err.println("Camera: Error Thread already launched");
        }
    }

    @Override
    public PImage getPImage() {
        // TODO: time management to avoid to send multiple times the same 
        // frame to the graphics card. 
        this.checkCamImage();
        if (currentImage != null) {
            camImage.update(currentImage);
//            return camImage;
        }
        return camImage;
    }
}
