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

import fr.inria.papart.procam.MarkerBoard;
import fr.inria.papart.procam.camera.Camera;
import org.bytedeco.javacpp.opencv_core.IplImage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jeremylaviole
 */
class CameraThread extends Thread {

    private final Camera camera;
    private boolean compute;
    public boolean stop;

    public CameraThread(Camera camera) {
        this.camera = camera;
        stop = false;
    }

    @Override
    public void run() {
        while (!stop) {
            camera.grab();
            IplImage img = camera.getIplImage();
            // TODO: check if img can be null...        
            if (img != null && compute && !camera.getTrackedSheets().isEmpty()) {
                this.compute(img);
            }

        }
    }

    public void compute(IplImage img) {
        try {
            camera.sheetsSemaphore.acquire();

            for (MarkerBoard sheet : camera.getTrackedSheets()) {
                sheet.updatePosition(camera, img);
            }
            camera.sheetsSemaphore.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(CameraThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isCompute() {
        return compute;
    }

    public void setCompute(boolean compute) {
        this.compute = compute;
    }

    public void stopThread() {
        stop = true;
    }
}
