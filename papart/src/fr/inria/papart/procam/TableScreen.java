/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import fr.inria.papart.procam.camera.TrackedView;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author realitytech
 */
public class TableScreen extends PaperTouchScreen {

    protected float width, height;
    protected PVector translation;
    protected PMatrix3D location;

    public TableScreen(PVector location, float width, float height) {
        this.width = width;
        this.height = height;
        this.translation = new PVector(location.x, location.y, location.z);
    }

    @Override
    public void settings() {
        // Test with this...
        setDrawOnPaper();
        setDrawingSize(width, height);
        setQuality(2f);
    }

    @Override
    public void setup() {
        // View on all of the image
        location = table.get();
        location.translate(translation.x, translation.y);
        this.setLocation(location);
    }
    
    public void translatePos(PVector t){
        getExtrinsics().translate(t.x, t.y);
    }
    public void rotatePos(float v){
        getExtrinsics().rotate(v);
    }

}
