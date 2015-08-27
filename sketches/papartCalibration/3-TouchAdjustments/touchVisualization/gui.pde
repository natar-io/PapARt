
void initGui(){
    
    skatolo = new Skatolo(this);
    skatolo.addSlider("recursion")
	.setPosition(30, 50)
	.setValue(touchCalibration.getMaximumRecursion())
	.setRange(1, 500)
	.setSize(200, 12);

    skatolo.addSlider("searchDepth")
	.setPosition(30, 70)
	.setValue(touchCalibration.getSearchDepth())
	.setRange(1, 50)
	.setSize(200, 12);
  
    skatolo.addSlider("maxDistance")
	.setPosition(30, 90)
	.setValue(touchCalibration.getMaximumDistance())
	.setRange(0, 1000)
	.setSize(400, 12);

  
    skatolo.addSlider("minCompoSize")
	.setPosition(30, 110)
	.setValue(touchCalibration.getMinimumComponentSize())
	.setRange(1, 50)
	.setSize(200, 12);
  
    skatolo.addSlider("minHeight")
	.setPosition(30, 130)
	.setValue(touchCalibration.getMinimumHeight())
	.setRange(0, 50)
	.setSize(200, 12);

    skatolo.addSlider("forgetTime")
	.setPosition(30, 150)
	.setValue(touchCalibration.getTrackingForgetTime())
	.setRange(0, 1000)
	.setSize(200, 12);
    skatolo.addSlider("trackingMaxDistance")
	.setPosition(30, 160)
	.setValue(touchCalibration.getTrackingMaxDistance())
	.setRange(10, 1000)
	.setSize(200, 12);

    skatolo.addSlider("precision")
	.setPosition(30, 170)
	.setValue(touchCalibration.getPrecision())
	.setRange(1, 8)
	.setSize(200, 12);

    
    skatolo.addSlider("planeHeight")
	.setPosition(30, 190)
	.setValue(planeCalibration.getHeight())
	.setRange(1, 2000)
	.setSize(300, 12);

    skatolo.addSlider("planeUpAmount")
	.setPosition(30, 210)
	.setValue(1.0f)
	.setRange(1, 50)
	.setSize(200, 12);

    
    skatolo.addButton("planeUp")
	.setPosition(30, 220)
	.setSize(30, 30);
    skatolo.addButton("planeDown")
	.setPosition(30, 250)
	.setSize(30, 30);

    

    // Manual draw. 
    skatolo.setAutoDraw(false);

    textFont(createFont("",15));
}

float planeUpAmount;

void planeUp(){
    planeCalibration.moveAlongNormal(-planeUpAmount);
}

void planeDown(){
    planeCalibration.moveAlongNormal(planeUpAmount);
}
