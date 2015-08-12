float imageIntens = 200;


public class DrawingAppInterface  extends PaperTouchScreen {

    float buttonLoc = 30;
    float buttonWidth = 60;

    Button intensityButton;
    Button lockButton;
    
    boolean lockOn;

    void setup(){
	setDrawingSize((int) interfaceSize.x, (int) interfaceSize.y);
	loadMarkerBoard(sketchPath() + "/data/markers/drawing.cfg",
		    (int) interfaceSize.x, (int) interfaceSize.y);


	intensityButton = new Button("Intensity", 30, 20, 60, 20);
	buttons.add(intensityButton);
    }

    public void draw(){
        screen.setDrawing(true);
	this.setLocation(0, 210, 0);
	
	if(intensityButton.isTouched()){
	    if (!touchList.isEmpty()) {
		for (Touch t : touchList) {
		    if(t.is3D) 
			continue;

		    // Check t.p == null ? 
		    float p = t.position.x * drawingSize.x; 
		    if(p > (buttonLoc - buttonWidth /2) &&
		       p < (buttonLoc + buttonWidth /2)){
			
			imageIntens = 255f * (p - buttonLoc + buttonWidth/2) / buttonWidth;
		    }
		}
	    }
	}
	    // Check where it is touched... 
	beginDraw2D();
	clear();
	background(0);

	fill(255);
	rect(0, 0, imageIntens / 255f * buttonWidth, 5);

	for(Button b : buttons)
	    b.drawSelf(getGraphics());

	drawTouch(10);

	endDraw();
    }


}
