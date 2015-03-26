public class MyApp  extends PaperScreen {

    TrackedView boardView;

    // 5cm  ->  50 x 50 pixels 
    PVector captureSize = new PVector(50, 50);
    PVector origin = new PVector(40, 40);
    int picSize = 64; // Works better with power  of 2
    
    void setup() {
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
	
	boardView = new TrackedView(markerBoard, 
				    origin,
				    captureSize,
				    picSize, picSize);

	// Register this view with the camera.
	cameraTracking.addTrackedView(boardView);
  }


  void draw() {
    beginDraw2D();
    //    background(0, 0, 0);
    clear();

    fill(200, 100, 20);
    rect(10, 10, 10, 10);

    PImage out = cameraTracking.getPView(boardView);
    
    if(out != null){
	image(out, 120, 40, picSize, picSize);
    }
    endDraw();
  }
}

