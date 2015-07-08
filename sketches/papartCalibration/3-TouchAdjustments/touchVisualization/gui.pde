
void initGui(){
    
    cp5 = new ControlP5(this);
    cp5.addSlider("recursion")
	.setPosition(30, 50)
	.setValue(touchCalibration.getMaximumRecursion())
	.setRange(1, 50)
	.setSize(200, 12);

    cp5.addSlider("searchDepth")
	.setPosition(30, 70)
	.setValue(touchCalibration.getSearchDepth())
	.setRange(1, 50)
	.setSize(200, 12);
  
    cp5.addSlider("maxDistance")
	.setPosition(30, 90)
	.setValue(touchCalibration.getMaximumDistance())
	.setRange(0, 1000)
	.setSize(400, 12);

  
    cp5.addSlider("minCompoSize")
	.setPosition(30, 110)
	.setValue(touchCalibration.getMinimumComponentSize())
	.setRange(1, 50)
	.setSize(200, 12);
  
    cp5.addSlider("minHeight")
	.setPosition(30, 130)
	.setValue(touchCalibration.getMinimumHeight())
	.setRange(0, 50)
	.setSize(200, 12);

    cp5.addSlider("forgetTime")
	.setPosition(30, 150)
	.setValue(touchCalibration.getTrackingForgetTime())
	.setRange(0, 1000)
	.setSize(200, 12);
    cp5.addSlider("trackingMaxDistance")
	.setPosition(30, 160)
	.setValue(touchCalibration.getTrackingMaxDistance())
	.setRange(10, 1000)
	.setSize(200, 12);

    cp5.addSlider("precision")
	.setPosition(30, 170)
	.setValue(touchCalibration.getPrecision())
	.setRange(1, 8)
	.setSize(200, 12);

    
    cp5.addSlider("planeHeight")
	.setPosition(30, 190)
	.setValue(planeCalibration.getHeight())
	.setRange(1, 2000)
	.setSize(300, 12);

    cp5.addSlider("planeUpAmount")
	.setPosition(30, 210)
	.setValue(1.0f)
	.setRange(1, 50)
	.setSize(200, 12);

    
    cp5.addButton("planeUp")
	.setPosition(30, 220)
	.setSize(30, 30);
    cp5.addButton("planeDown")
	.setPosition(30, 250)
	.setSize(30, 30);

    

    // Manual draw. 
    cp5.setAutoDraw(false);

    textFont(createFont("",15));
}

float planeUpAmount;

void planeUp(){
    planeCalibration.moveAlongNormal(-planeUpAmount);
}

void planeDown(){
    planeCalibration.moveAlongNormal(planeUpAmount);
}
