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
package fr.inria.papart.apps;

import fr.inria.papart.procam.*;
import fr.inria.papart.apps.MyApp2D;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.tracking.DetectedMarker;
import processing.core.PApplet;

public class MarkerDetectionDebug extends PApplet {

    boolean useProjector = false;
    Papart papart;

    @Override
    public void settings() {
        size(900, 600, P3D);
//        fullScreen(P3D, 2);
    }

    public static void main(String args[]) {
        PApplet.main(new String[]{"--present", "fr.inria.papart.apps.MarkerDetectionDebug"});
    }

    @Override
    public void setup() {

        papart = Papart.seeThrough(this);
//        papart.loadTouchInput();

        PaperScreen app = new MyApp2D();
        papart.startTracking();
        
        papart.getARDisplay().manualMode();

    }

    public void draw() {
        
        Camera camera = papart.getCameraTracking();
        
        image(camera.getImage(), 0, 0, camera.width(), camera.height());
        
        DetectedMarker[] detectedMarkers = camera.getDetectedMarkers();
        if(detectedMarkers != null){
            
            fill(255);
            stroke(0);
            strokeWeight(1);
            
            for(DetectedMarker marker : detectedMarkers){
                marker.drawSelf(g, 4);
            }
        }
    }

//    public void keyPressed(){
//        if(key == 'c'){
//           papart.calibration();
//        }
//    }
}
