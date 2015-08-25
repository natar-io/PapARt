import fr.inria.papart.procam.camera.*;

public class MyApp  extends PaperScreen {

    TrackedView boardView;

    // 5cm  ->  50 x 50 pixels 
    PVector captureSize = new PVector(50, 50);
    PVector origin = new PVector(40, 40);
    int picSize = 64; // Works better with power  of 2
    
    void setup() {
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath() + "/data/A3-small1.cfg", 297, 210);
	
	boardView = new TrackedView(this);
	boardView.setCaptureSizeMM(captureSize);

	boardView.setImageWidthPx(picSize);
	boardView.setImageHeightPx(picSize);

	boardView.setBottomLeftCorner(origin);

	boardView.init();
    }


    void draw() {
	beginDraw2D();

	clear();

	fill(200, 100, 20);
	rect(10, 10, 10, 10);
	PImage out = boardView.getViewOf(cameraTracking);
    
	if(out != null){
	    image(out, 120, 40, picSize, picSize);
	}
	endDraw();
    }
}

