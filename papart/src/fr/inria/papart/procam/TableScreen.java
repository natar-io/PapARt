package fr.inria.papart.procam;

import fr.inria.papart.procam.camera.TrackedView;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author realitytech
 */
public class TableScreen extends PaperTouchScreen {

    protected PVector translation;
    protected PMatrix3D location;

    public TableScreen(float width, float height) {
        this(new PVector(), width, height);
    }

    public TableScreen(PVector loc, float width, float height) {
        this(loc, new PVector(width, height));
    }

    public TableScreen(PVector loc, PVector size) {
        setDrawOnPaper();
        setDrawingSize(size.x, size.y);
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

}
