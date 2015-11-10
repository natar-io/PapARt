
int numDrips = 0;
int maxPointsPerTouch = 3;
int leap = 18; //how far the point travels each iteration also controls opacity
int sizeMax = 40; //how far the point travels each iteration also controls opacity

ArrayList<Drop> drips = new ArrayList<Drop>();

void initBlood(){

    bloodGraphics = (PGraphicsOpenGL) createGraphics(width, height, OPENGL);
    bloodGraphics.beginDraw();
    bloodGraphics.background(0);
    bloodGraphics.endDraw();
}

void drawBlood(){

    bloodGraphics.beginDraw();


    for(TouchPoint tp : touchs2D){
	PVector pos = tp.getPosition();

	int nbPoints = (int) (maxPointsPerTouch * random(1));

	for(int i=0; i < nbPoints ;i++){
	    int px = (int) (pos.x* width +random(-leap,leap));
	    int py = (int) (pos.y* height +random(-leap,leap));
	    
	    bloodGraphics.stroke(255-i, 30, 30, 100 + random(100));
	    bloodGraphics.fill(255-i, 30, 30, 100 + random(100));
	    // bloodGraphics.strokeWeight(random(sizeMax));
	    // bloodGraphics.point(px, py);

	    int splat = round(random(0,20));
	    bloodGraphics.textFont(font, random(sizeMax));
	    bloodGraphics.text(splat, px, py); 

	}

	if(perCentChance(8)){
	    drips.add(new Drop((int) (pos.x* width), 
			       (int) (pos.y* height), 
			       (int) (100 + random(100))));
	}
    }

    for (Iterator<Drop> it = drips.iterator(); it.hasNext();) {
	Drop drop = it.next();
	drop.drip();
	drop.show((PGraphicsOpenGL) bloodGraphics);
	drop.tryStop();
	if(!drop.isMoving){
	    it.remove();
	}
    }
    bloodGraphics.endDraw();

}
