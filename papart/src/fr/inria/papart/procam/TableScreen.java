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

    public TableScreen(PVector loc, float width, float height) {
        this.width = width;
        this.height = height;
        this.translation = new PVector(loc.x, loc.y, loc.z);

        setDrawOnPaper();
        setDrawingSize(width, height);
        this.location = table.get();
        this.location.translate(translation.x, translation.y);
//        this.setLocation(location);
        this.useManualLocation(this.location);
    }

//    @Override
//    public void settings() {
//        // Test with this...
//
////        setQuality(2f);
//    }
//
//    @Override
//    public void setup() {
//        // View on all of the image
//
//    }
//    
//    public void translatePos(PVector t){
//        getExtrinsics().translate(t.x, t.y);
//    }
//    public void rotatePos(float v){
//        getExtrinsics().rotate(v);
//    }

}
