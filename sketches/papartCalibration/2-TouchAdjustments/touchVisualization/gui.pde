
void initGui(){
    
    cp5 = new ControlP5(this);
    cp5.addSlider("recursion")
	.setPosition(30, 50)
	.setValue(100)
	.setRange(1, 20)
	.setSize(200, 12);

    cp5.addSlider("searchDepth")
	.setPosition(30, 70)
	.setValue(40)
	.setRange(1, 20)
	.setSize(200, 12);
  
    cp5.addSlider("maxDistance")
	.setPosition(30, 90)
	.setValue(10.0f)
	.setRange(0, 20)
	.setSize(200, 12);

  
    cp5.addSlider("minCompoSize")
	.setPosition(30, 110)
	.setValue(2)
	.setRange(1, 50)
	.setSize(200, 12);
  
    cp5.addSlider("minHeight")
	.setPosition(30, 130)
	.setValue(1.0f)
	.setRange(0, 50)
	.setSize(200, 12);

    cp5.addSlider("forgetTime")
	.setPosition(30, 150)
	.setValue(250)
	.setRange(0, 1000)
	.setSize(200, 12);

    cp5.addSlider("precision")
	.setPosition(30, 170)
	.setValue(2)
	.setRange(1, 8)
	.setSize(200, 12);

    
    cp5.addSlider("planeHeight")
	.setPosition(30, 190)
	.setValue(planeCalibration.getHeight())
	.setRange(1, 30)
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

void planeUp(){
    planeCalibration.moveAlongNormal(-1.f);
}

void planeDown(){
    planeCalibration.moveAlongNormal(1.f);
}
