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
        setDrawOnPaper();
        setDrawingSize(width, height);
        setLocation(loc);
    }
     /**
     * Change the location relative to the table.
     *
     * @param v in millimeters
     */
    @Override
    public void setLocation(PVector v) {
        setLocation(v.x, v.y, v.z);
    }

    /**
     * Change the location relative to the table.
     *
     * @param x in millimeters
     * @param y in millimeters
     * @param z in millimeters
     */
    @Override
    public void setLocation(float x, float y, float z) {
        this.translation = new PVector(x, y, z);
        this.location = table.get();
        this.location.translate(translation.x, translation.y, translation.z);
        this.useManualLocation(this.location);
        this.computeWorldToScreenMat(cameraTracking);
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
