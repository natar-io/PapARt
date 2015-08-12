import fr.inria.papart.graphics2D.*;

Layer rootLayer;

ArrayList<Layer> allLayers = new ArrayList<Layer>();
ArrayList<MyMovie> allMovies = new ArrayList<MyMovie>();

void initDrawing(){

    GLGraphicsOffScreen g = drawingScreen.getGraphics();

    rootLayer = new Layer(this, g.getTexture(), 
    			  drawingBoardSizeHalf,
    			  drawingBoardSize);

    allLayers.add(rootLayer);
    editLayer.setCurrentLayer(rootLayer);

    // testLayer = new Layer(this, 300, 300, 
    // 			  new PVector(-50, -50),
    // 			  new PVector(50, 50));

    //    rootLayer.addSubLayer(testLayer);

    //    rootLayer.addSubLayer(empathy.layer);
    // rootLayer.addSubLayer(fireDrag.layer);
    // rootLayer.addSubLayer(recTree.layer);


    //       testLayer.putImage( loadImage(sketchPath + "/images/terre.jpg"));
    //    testLayer.addFilter(this, blurLF);
    // testLayer.addFilter(this, alphaLF);
    // rootLayer.addFilter(this, alphaLF);

    //    allLayers.add(testLayer);



   
}


void drawDrawing(){
    
    //    rootLayer.setPosition(new PVector((millis() / 100f) % 200, 100));
    
    //    testLayer.setRotation(((float)millis() / 1000f));
    
    for(MyMovie myMov : allMovies)
	myMov.update(this);

    // testLayer.putImage(movie);


    //    // Transparent for debug 
    // GLGraphicsOffScreen gf = rootLayer.getFilterPartialArea(alphaLF);
    // gf.beginDraw();
    // gf.noStroke();
    // gf.fill(255, 0, 0);
    
    // // if(drawingTouch.position2D != null){
    // // 	for(PVector p : drawingTouch.position2D){
    // // 	    PVector p1 = new PVector(p.x * drawingScreen.getSize().x,
    // // 				     p.y * drawingScreen.getSize().y);
	    
		    
    // // 	    PVector outPos = rootLayer.projectDraw(p1);
	    
    // // 	    outPos = rootLayer.displayToImage(outPos);
    // // 	    gf.ellipseMode(CENTER);
    // // 	    gf.ellipse(outPos.x, outPos.y, 20, 20);
    // // 	}
    // // }
    // gf.endDraw();
    

    // // testLayer.rotateBy(PI / 1000f);
    // // rootLayer.rotateBy(-PI / 2000f);
    // // if(test){
    // // 	rootLayer.translateBy(new PVector(0.01f, 0.04f));
    // // 	testLayer.translateBy(new PVector(0.02f, 0.02f));

    // // }
    // // else {
    // // 	rootLayer.translateBy(new PVector(-0.01f, -0.04f));    	
    // // 	testLayer.translateBy(new PVector(-0.02f, -0.02f));
    // // }       


    // GLGraphicsOffScreen g2  = rootLayer.getBuffer();

    // g2.beginDraw();
    // g2.scale(screenResolution);
    // g2.noStroke();
    // g2.background(0, 200, 0);
    // g2.fill(200, 0, 0);
    // g2.noStroke();
    // g2.rectMode(CENTER);
    // g2.rect(80, 20, 10, 10);
    // g2.rect(50, 50, 40, 40);
    // g2.endDraw();

    /////// Inpaint for a shader /////////

    FilterLayerArea fla = filters.getEditableArea();
    if(fla != null){

	Layer layer = editLayer.getCurrentLayer();
	GLGraphicsOffScreen g = fla.getPartialFilter();
	
	g.beginDraw();
	g.noStroke();
	g.fill(255, 0, 0);


	if(drawingTouch.position2D != null){
	    for(PVector p : drawingTouch.position2D){
		// PVector p1 = new PVector((p.x - 0.5) * drawingScreen.getSize().x,
		// 			     (p.y - 0.5) * drawingScreen.getSize().y);
		PVector p1 = new PVector(p.x * drawingScreen.getSize().x,
					 p.y * drawingScreen.getSize().y);
		p1 = layer.project(p1);
		PVector outPos = layer.displayToImage(p1);
		g.ellipseMode(CENTER);
		g.ellipse(outPos.x, outPos.y, 20, 20);
	    }
	}
	g.endDraw();
	
    }


    // // Transparent for debug 
    // gf = testLayer.getFilterPartialArea(alphaLF);
    // gf.beginDraw();
    // gf.noStroke();
    // gf.fill(255, 0, 0);

    // if(drawingTouch.position2D != null){
    // 	for(PVector p : drawingTouch.position2D){

    // 	    // PVector p1 = new PVector((p.x - 0.5) * drawingScreen.getSize().x,
    // 	    // 			     (p.y - 0.5) * drawingScreen.getSize().y);

    // 	    PVector p1 = new PVector(p.x * drawingScreen.getSize().x,
    // 	    			     p.y * drawingScreen.getSize().y);

    // 	    p1 = testLayer.project(p1);

    // 	    PVector outPos = testLayer.displayToImage(p1);

    // 	    gf.ellipseMode(CENTER);
    // 	    gf.ellipse(outPos.x, outPos.y, 20, 20);
	    
    // 	}
    // }
    // gf.endDraw();


    GLGraphicsOffScreen g = drawingScreen.getGraphics();
    g.beginDraw();
    g.clear(0, 0);
    g.scale(screenResolution);

    //    g.translate(drawingBoardSizeHalf.x, drawingBoardSizeHalf.y, 0);

    g.background(0, 0, 0);
    g.imageMode(CENTER);
    
    rootLayer.drawSelf(g);

    g.ellipseMode(CENTER);


    // if(drawingTouch.position2D != null){

    // 	for(int i = 0; i < drawingTouch.position2D.size(); i++){
    // 	    PVector p = drawingTouch.position2D.get(i);
    // 	    TouchPoint tp = drawingTouch.points2D.get(i);

    // 	    int size = tp.confidence;

    // 	    println(size);

    // 	    PVector p1 = new PVector(p.x * drawingScreen.getSize().x,
    // 				     p.y * drawingScreen.getSize().y);

    // 	    // PVector p1 = new PVector((p.x - 0.5) * drawingScreen.getSize().x,
    // 	    // 			     (p.y - 0.5) * drawingScreen.getSize().y);

    // 	    g.ellipse(p1.x, p1.y, size, size);

    // 	}

    // 	// for(PVector p : drawingTouch.position2D){
    // 	//     PVector p1 = new PVector(p.x * drawingScreen.getSize().x,
    // 	// 			     p.y * drawingScreen.getSize().y);
    // 	//     // PVector p1 = new PVector((p.x - 0.5) * drawingScreen.getSize().x,
    // 	//     // 			     (p.y - 0.5) * drawingScreen.getSize().y);
    // 	//     g.ellipse(p1.x, p1.y, 4, 4);
    // 	// }
    // }


    
    Layer currentLayer = editLayer.getCurrentLayer();
    if(editLayer.isEditMode() && currentLayer != null && currentLayer != rootLayer){

	////////// Moving and changing the current layer //////////
	
	// 1 finger +  -> Move 
	// Hand + 1 finger+ -> scale (x) & rotate (y) 


	// Find the Hand ?
	for(int j = 0; j < drawingTouch.position3D.size(); j++){
	    
	    PVector p3d = drawingTouch.position3D.get(j);
	    PVector pHand = new PVector(p3d.x * drawingScreen.getSize().x,
					p3d.y * drawingScreen.getSize().y);

	    //////// Hand drawing ?! ////////

	    float handSize = (p3d.z > 80) ? 20 : p3d.z / 4f;
	    //	handSize *= 0.5;
	    
	    // 5cm diameter hand
	    g.noStroke();
	    g.fill(30, 30, 200, 20);
	    g.ellipse(pHand.x, pHand.y, 30 + handSize , 30 + handSize); 
	    
	    if(currentLayer.contains(pHand)){
		currentLayer.setDrawBorders(true, 200);
	    }


	    //////////// Find the fingers //////////

	    boolean hasPalm = false;

	    ////// First pass : find if there is a hand on the paper. 
	    for(int i = 0; i < drawingTouch.position2D.size(); i++){
	    
		PVector p = drawingTouch.position2D.get(i);
		TouchPoint tp = drawingTouch.points2D.get(i);
		
		float dist = dist(p3d.x, p3d.y, p.x, p.y);
		int size = tp.confidence;

		if(size > 80)
		    hasPalm = true;
		
	    }

	    ////////// Second pass : interaction ///////////////

	    // all touch have speed
	    if(drawingTouch.position2D.size() == drawingTouch.speed2D.size()){

		for(int i = 0; i < drawingTouch.position2D.size(); i++){
		    
		    PVector p = drawingTouch.position2D.get(i);
		    TouchPoint tp = drawingTouch.points2D.get(i);
		    PVector speed = drawingTouch.speed2D.get(i);
		    
		    float dist = dist(p3d.x, p3d.y, p.x, p.y);
		    int size = tp.confidence;
		    
		    PVector pFinger = new PVector(p.x * drawingScreen.getSize().x,
						  p.y * drawingScreen.getSize().y);
		    PVector speedMm = new PVector(speed.x * drawingScreen.getSize().x,
						 speed.y * drawingScreen.getSize().y);
		    
		    // Finger touching the current Layer...
		    if(hasPalm){
			g.strokeWeight(4);
			g.stroke(200, 0, 0);
		    }
		    
		    if(currentLayer.contains(pFinger)){
			
			if(!hasPalm){
			    currentLayer.translateBy(PVector.mult(speedMm, 0.5f));
			    
			}else {
			    currentLayer.rotateBy(speedMm.y / 400f);

			    currentLayer.scaleBy(1.0 + speedMm.x / 500 );
			}
			
			currentLayer.setDrawBorders(true, 200);
			g.fill(200, 200, 0);	    
			g.ellipse(pFinger.x, pFinger.y, 2, 2);
			
			
			
		} else {
			g.fill(200);	    
			g.ellipse(pFinger.x, pFinger.y, 2, 2);
		    }
		    
		}
	    }
	}   
    }


   







    ///////////// Child selection /////////////

    if(editLayer.isChildrenMode() && currentLayer != null){

	////////// Moving and changing the current layer //////////
	
	// 1 finger +  -> Move 
	// Hand + 1 finger+ -> scale (x) & rotate (y) 


	// Find the Hand ?
	for(int j = 0; j < drawingTouch.position3D.size(); j++){
	    
	    PVector p3d = drawingTouch.position3D.get(j);
	    PVector pHand = new PVector(p3d.x * drawingScreen.getSize().x,
					p3d.y * drawingScreen.getSize().y);

	    //////// Hand drawing ?! ////////

	    float handSize = (p3d.z > 80) ? 20 : p3d.z / 4f;
	    //	handSize *= 0.5;
	    
	    // 5cm diameter hand
	    g.noStroke();
	    g.fill(30, 30, 200, 20);
	    g.ellipse(pHand.x, pHand.y, 30 + handSize , 30 + handSize); 
	    
	    if(currentLayer.contains(pHand)){
		currentLayer.setDrawBorders(true, 200);
	    }


	    for(int i = 0; i < drawingTouch.position2D.size(); i++){
		
		PVector p = drawingTouch.position2D.get(i);
		// TouchPoint tp = drawingTouch.points2D.get(i);
		// PVector speed = drawingTouch.speed2D.get(i);
		
		// float dist = dist(p3d.x, p3d.y, p.x, p.y);
		// int size = tp.confidence;
		
		PVector pFinger = new PVector(p.x  * drawingScreen.getSize().x,
					      p.y  * drawingScreen.getSize().y);
		    
		    for(Layer l : currentLayer.getChildren()){
			
			
			// PVector p1 = l.project(pFinger);
			// println("P " + pFinger + " Projected " + p1);


			if(l.contains(pFinger)){
			    println("Child Layer contains touch");
			    editLayer.setCurrentLayer(l);
			    break;
			}
			
		    }
		    
	    }
	}

    }

    g.endDraw();
}


PVector previousPos = null;
