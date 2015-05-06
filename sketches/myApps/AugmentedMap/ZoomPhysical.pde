import org.bytedeco.javacpp.ARToolKitPlus;
import org.bytedeco.javacpp.ARToolKitPlus.TrackerMultiMarker;

public class ZoomPhysical extends PaperTouchScreen {
    TrackedView zoomView;
    PVector magicalLocation = new PVector(45, 50, 0);
    PVector magicalLocationCfg = new PVector(30, 30, 0);

    //For the catpure
    PVector captureSize = new PVector(ZoomSize.x, ZoomSize.y);
    PVector origin = magicalLocationCfg;
    //PVector origin = new PVector(0, 0, 0);
    //int picSizeX = (int) A4BoardSize.x - menuWidth; // Works better with power  of 2 (initially 64)
    //int picSizeY = (int) A4BoardSize.y; // Works better with power  of 2 (initially 64)
    int picSizeX = 128;
    int picSizeY = 224;

    int lastCaptTime;
    
    color ref = color(130, 18, 0);

    void setup(){
	setDrawingSize((int) ZoomSize.x, (int) ZoomSize.y);
	//loadMarkerBoard(sketchPath + "/data/markers/zoom.png",
	//		210, 297);
	loadMarkerBoard(sketchPath + "/data/markers/zoom.cfg",
			210, 297);
	zoomView = new TrackedView(markerBoard, origin, captureSize, picSizeX, picSizeY);
	lastCaptTime = millis();
    }


    void draw() {
	setLocation(magicalLocationCfg);
	beginDraw2D();

	//background(255);
	if((keyPressed) && (key == 't')){
	    PImage capt = capture();
	    capt.save(sketchPath + "/todelete/" + minute() + second() + ".png");
	    //image(capt, 0, 0, ZoomSize.x, ZoomSize.y);
	}
	getZoomLevel();
	endDraw();
    }

    PImage capture(){
	PImage out = cameraTracking.getPView(zoomView);
	return out;
    }

    void getZoomLevel(){
	if(millis() - lastCaptTime > 200){
	    PImage capt = capture();
	    int stepSize = capt.height / 13;
	    
	    int minStep = -1;
	    Color minColor = new Color();
	    float minDist = Float.MAX_VALUE;
	    for(int step = 0; step <= 12; step++){
		Color avg = new Color();
		avg.getAverageColor(capt, 0, capt.width, step * stepSize, (step + 1) * stepSize);
		//image(capt, 0, 0, capt.width, capt.height);
		//System.out.println(step * stepSize);
		//avg.print();
		//float curRed = avg.getRed();//TODO use colors instead of red !!! (white has a lot of red...)
		float curDist = avg.dist(ref);
		if(curDist < minDist){
		    minColor.set(avg);
		    minDist = curDist;
		    minStep = step;
		}
	    }
	    //System.out.println(minDist);
	    if((minStep >= 0) && (minDist < 65)){
		//fill(minColor.getColor());
		//rect(0, capt.height - 20, capt.width, 20);
		physicalZoomLevel = 18 - constrain(minStep + 9, 12, 18) + 12;
		//System.out.println(minStep + 9);
	    }
	    lastCaptTime = millis();
	}
    }

}

