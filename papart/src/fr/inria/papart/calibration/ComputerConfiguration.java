/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.calibration;

import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.camera.CameraFactory;
import processing.core.PApplet;
import processing.data.XML;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/** 
* @author Jeremy Laviole <jeremy.laviole@inria.fr>
* @deprecated 
 */
public class ComputerConfiguration extends Calibration {

    private ScreenConfiguration screen;

    private CameraConfiguration camera;

    @Override
    public boolean isValid() {
        // todo check ID, name & type ?
        return screen.isValid() && camera.isValid();
    }

    @Override
    public void addTo(XML xml) {
        screen.addTo(xml);
        camera.addTo(xml);
    }

    @Override
    public void replaceIn(XML xml) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void loadFrom(PApplet parent, String fileName) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        screen = new ScreenConfiguration();
        screen.loadFrom(parent, fileName);
        
        camera = new CameraConfiguration();
        camera.loadFrom(parent, fileName);
    }

    public ScreenConfiguration getScreen() {
        return screen;
    }

    public CameraConfiguration getCamera() {
        return camera;
    }
}
