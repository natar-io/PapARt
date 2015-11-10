public class ScaleMenu{
    PImage img;
    float size;
    boolean visible;
    boolean stateChanged;
    int startingZoomLevel;
    int curZoomLevel;
    int lastTouchTime;
    PVector lastTouchPos = new PVector(0, 0, 0);//Last position of the touch while the scale was visible
    PVector firstTouchPos = new PVector(0, 0, 0);//Position of the touch that made the scale appear
    PVector scalePosition = new PVector(0, 0, 0);//Position where to display the scale

    ScaleMenu(){
	img = loadImage(sketchPath + "/data/images/scale.png");
	size = 0.2;
	visible = false;
	stateChanged = false;
	startingZoomLevel = 12;
	curZoomLevel = 12;
	lastTouchTime = -1;
    }

    ScaleMenu(int _startingZoomLevel){
	img = loadImage(sketchPath + "/data/images/scale.png");
	size = 0.2;
	visible = false;
	stateChanged = false;
	startingZoomLevel = _startingZoomLevel;
	curZoomLevel = _startingZoomLevel;
	lastTouchTime = -1;
    }

    public PImage getImage(){
	return img;
    }

    public float getSize(){
	return size;
    }

    public boolean isVisible(){
	return visible;
    }

    public int getZoomLevel(){
	return curZoomLevel;
    }

    public PVector getPosition(){
	return scalePosition;
    }

    public PVector getZoomCenter(){
	return firstTouchPos;
    }

    public boolean getStateChanged(){
	boolean result = stateChanged;
	stateChanged = false;
	return result;
    }

    public void update(PVector touch){
	if(!visible){
	    visible = true;
	    firstTouchPos.set(touch);
	    startingZoomLevel = curZoomLevel;
	    //TODO update position of the scale
	    float posX = touch.x - size * img.width / 2;
	    float posY = touch.y - size * (100 + (18 - startingZoomLevel) * 50);
	    scalePosition = new PVector(posX, posY, 0);
	    System.out.println("INIT");
	}
	System.out.println("UPDATING TOUCH");
	lastTouchPos.set(touch);
	lastTouchTime = millis();
	updateZoomLevel();
	System.out.println(firstTouchPos.y);
	System.out.println(lastTouchPos.y);
    }

    public void update(boolean empty){
	/*if(visible){
	    System.out.println("SHUTTING DOWN TOUCH");
	}*/
	if((visible) && (lastTouchTime >= 0) && (millis() - lastTouchTime > 200)){
	    visible = false;
	    //Update zoomLevel according to the last position for the touch
	    //updateZoomLevel();
	    System.out.println("Zoom Level " + curZoomLevel);
	}
    }

    private void updateZoomLevel(){
	if(lastTouchPos.x - firstTouchPos.x <= size * img.width){
	    System.out.println("Hauteur: " + img.height);
	    int newZoomLevel = startingZoomLevel - (int) ((lastTouchPos.y - firstTouchPos.y) / (size * 50.0));
	    //System.out.println("Écart : " + (- lastTouchPos.y + firstTouchPos.y));
	    //System.out.println("Écart divided: " + ((- lastTouchPos.y + firstTouchPos.y) / (size * 45.0)));
	    //System.out.println("Zoom Level before constrain: " + curZoomLevel);
	    newZoomLevel = constrain(newZoomLevel, 12, 18);
	    if(newZoomLevel != curZoomLevel){
		stateChanged = true;
		curZoomLevel = newZoomLevel;
	    }
	}
    }



}
