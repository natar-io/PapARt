class Patient extends PaperInterface{


    PImage patientImg;
    PImage donneesImg;
    float scrollPos = 0;


    public Patient(PApplet parent, MarkerBoard board, PVector size, Camera cam, float resolution, Projector proj){
	super(parent, board, size, cam, resolution, proj);
    }


    public void init(){
	patientImg = loadImage("patient/patient.png");
	donneesImg = loadImage("patient/donnees.jpg");
    }


    public void draw(){

	
	GLGraphicsOffScreen g;
		
	g = screen.getGraphics();
	
	g.beginDraw();
	g.scale(screenResolution);
	
	g.imageMode(CENTER);

	
	DrawUtils.drawImage(g, patientImg,  (int) (boardSize.x /2f), (int)(boardSize.y /2f),
				(int) boardSize.x, (int) boardSize.y);



	for(PVector p : touch.speed2D){
	    scrollPos += p.y * boardSize.y;
	}

	scrollPos = constrain(scrollPos, -180, 140);
	

	g.translate(10, 80, 0);	
	g.translate(0, scrollPos, 0);	

	DrawUtils.drawText(g, "02/08/13 : 13h30  Glycemie: 1.2 g/L  (min : 0.5, moy 1.0,  max 1.6)", 
			   font, 
			   10, 
			   0, 0);

	g.translate(0, -12, 0);	
	DrawUtils.drawText(g, "02/08/13 : 13h30  Glycemie: 1.2 g/L  (min : 0.5, moy 1.0,  max 1.6)", 
			   font, 
			   10, 
			   0, 0);

	
	DrawUtils.drawImage(g, donneesImg,  
			    (int) (boardSize.x /2f), 0, 
			    (int) (boardSize.x),   
			    (int) ( boardSize.x / donneesImg.width * donneesImg.height));



	for(Button b : buttons){
	    b.drawSelf(g);
	}

	g.endDraw();

    }


}

