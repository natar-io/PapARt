import fr.inria.papart.procam.camera.*;

public class MyApp  extends PaperScreen {

    TrackedView boardView;

    // 5cm  ->  50 x 50 pixels
    PVector captureSize = new PVector(50, 50);
    PVector origin = new PVector(40, 40);
    int picSize = 64; // Works better with power  of 2

    void settings(){
	setDrawingSize(297, 210);
	loadMarkerBoard(Papart.markerFolder + "A3-small1.cfg", 297, 210);
    }

    void setup() {

	boardView = new TrackedView(this);
	boardView.setCaptureSizeMM(captureSize);

	boardView.setImageWidthPx(picSize);
	boardView.setImageHeightPx(picSize);

	boardView.setBottomLeftCorner(origin);

	boardView.init();
    }

    void drawOnPaper() {
        clear();

	fill(200, 100, 20);
	rect(10, 10, 10, 10);
	PImage out = boardView.getViewOf(cameraTracking);

	if(out != null){
	    image(out, 120, 40, picSize, picSize);
	}
    }
}
