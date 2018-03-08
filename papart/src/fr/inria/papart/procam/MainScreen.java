package fr.inria.papart.procam;

import fr.inria.papart.multitouch.detection.CalibratedStickerTracker;
import fr.inria.papart.procam.camera.TrackedView;
import org.bytedeco.javacpp.helper.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;

/**
 * The MainScreen is a planar screen on the table. It is facing the user and
 * uses no MarkerBoard but the tableposition variable.
 *
 * @author Jeremy Laviole
 */
public class MainScreen extends PaperTouchScreen {

    public static float WIDTH = 800, HEIGHT = 500;
    public CalibratedStickerTracker stickerTracker;

    @Override
    public void settings() {
        // Test with this...
        setDrawOnPaper();
        setDrawingSize(WIDTH, HEIGHT);
        setQuality(2f);
    }

    TrackedView circleView;
    protected PMatrix3D location;

    @Override
    public void setup() {
        // View on all of the image
//        stickerTracker = new CalibratedStickerTracker(this, 5);
        stickerTracker = new CalibratedStickerTracker(this, 8);
//        stickerTracker.initTouchDetection();
        
        location = table.get();
        location.translate(-WIDTH / 2, -HEIGHT / 2);
        this.useManualLocation(location);
    }

}
