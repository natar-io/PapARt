// import javax.swing.JFrame;
// import javax.swing.JLabel;
import jpen.demo.StatusReport;
import jpen.event.PenListener;
import jpen.owner.multiAwt.AwtPenToolkit;
import jpen.PButtonEvent;
import jpen.PKindEvent;
import jpen.PLevelEvent;
import jpen.PScrollEvent;
import jpen.PLevel;


int TABLET_MODE = 2344;

GLGraphicsOffScreen tabletScreen;
List tabletPos;

// TODO: fichier de config ?... 
// PVector tabletMin = new PVector(2307, 2587);
// PVector tabletMax = new PVector(16727.0, 12450.0);
// 0 0 14720 9200
// 0 0 21648 13700

// PVector tabletMin = new PVector(0, 0);
// PVector tabletMax = new PVector(21648, 13700);

PImage tabletImg, clearImg;

void initTabletHandler(PApplet parent){

    new JPenHandlerObject(parent);
}

void initTablet(){

    tabletImg = loadImage(sketchPath + "/images/tablet.png");
    clearImg = loadImage(sketchPath + "/images/clear.png");

    tabletScreen = new 
	GLGraphicsOffScreen(this,
			    (int) (drawSize.x * screenResolution),
			    (int) (drawSize.y * screenResolution));
    
    tabletPos = (List) Collections.synchronizedList(new ArrayList());
}

boolean toClear = false;


PVector lastInput;
float lastInputTime;
float lineDelay = 150;  // in ms

void updateTablet(){

    tabletScreen.beginDraw();

    if(toClear){
	tabletScreen.clear(0, 0);
	toClear = false;
    }

    tabletScreen.stroke(255);
    tabletScreen.fill(255);


    if(millis() - lastInputTime > lineDelay)
	lastInput = null;

    synchronized(tabletPos){
	
	if(!tabletPos.isEmpty()){
	    lastTouchMouse = millis();
	}

	for(Object o : tabletPos){
	    PVector v = (PVector) o;

	    v.x = v.x / width * drawSize.x * screenResolution;
	    v.y = v.y / height * drawSize.y * screenResolution;

	    if(lastInput == null){
		tabletScreen.ellipse(v.x, v.y, v.z * 5, v.z * 5);
	    }
	    else{
		tabletScreen.strokeWeight((int) (v.z * 5));
		tabletScreen.line(lastInput.x, lastInput.y, v.x, v.y);
	    }	

	    lastInput = v;
	}

	if(!tabletPos.isEmpty()){
	    lastInputTime = millis();
	    tabletPos.clear();
	}
    }
    
    //    ellipse(20, 20, 10, 10);

    tabletScreen.endDraw();
    
}


void keyTablet(){

   if(key == '1'){
       toClear = true;
    }
}


void enterTabletMode(){
    resetFullNumPad();
}


void drawTablet(GLGraphicsOffScreen g){

    if(globalMode == TABLET_MODE){
	numPadImages[0] = clearImg;
    }

    g.imageMode(CENTER);
    g.pushMatrix();
    g.translate(drawSize.x / 2f, drawSize.y/2f);
    g.scale(1, -1);
    g.image(tabletScreen.getTexture(), 0, 0, drawSize.x, drawSize.y);
    g.popMatrix();

}

void drawCursor(GLGraphicsOffScreen graphics, float size){

  // graphics.beginDraw();
  graphics.pushStyle();
  graphics.noFill();
  graphics.stroke(255);

  tabletScreen.scale(screenResolution);

  //  graphics.translate(screen1.getSize().x / 2f, screen1.getSize().y / 2f , 0);

  graphics.ellipse((float)mouseX / (float)width * drawSize.x,
			 (1-((float)mouseY / (float)height)) * drawSize.y, 
			 1, 1);


  graphics.ellipse((float)mouseX / (float)width * drawSize.x,
			 (1-((float)mouseY / (float)height)) * drawSize.y, 
			 size, size);
  graphics.popStyle();
  // graphics.endDraw();

}


void leaveTabletMode(){


}



class JPenHandlerObject implements PenListener{

    JPenHandlerObject(PApplet pa){
	
	AwtPenToolkit.addPenListener(pa, this);
	System.out.println(new StatusReport(AwtPenToolkit.getPenManager()));
    }
    
    //@Override
    public void penButtonEvent(PButtonEvent ev) {
	//	System.out.println("Button " + ev);
    }
    //@Override
    public void penKindEvent(PKindEvent ev) {
	//	System.out.println("Kind " + ev);
    }
    //@Override
    public void penLevelEvent(PLevelEvent ev) {

	//	println("Level Event " );

	if(ev.levels.length == 3){

	    PVector v = new PVector();
	    
	    v.x = ev.levels[2].value;
	    v.y = ev.levels[1].value;
	    v.z = ev.levels[0].value;
	    //	    println(v);
	    tabletPos.add(v);

	}


	// for(PLevel level : ev.levels){
	//     if(level.typeNumber == 2)
	// 	println("Pressure ? " + level.value);
	//     //	    println(level.typeNumber + " " + level.value );
	// }

	// if(ev.containsLevelOfType(PLevel.Type.PRESSURE)){
	//     println("Pressure !");
	// }

	//	System.out.println("Level :" + ev);
    }

    //@Override
    public void penScrollEvent(PScrollEvent ev) {
	//System.out.println("Scroll" + ev);
    }
    //@Override
    public void penTock(long availableMillis) {
	//	System.out.println("TOCK - available period fraction: "+availableMillis);
    }
}
