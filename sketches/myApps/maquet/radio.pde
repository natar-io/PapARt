import fr.inria.papart.drawingapp.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.multitouch.metaphors.*;

public class Radio extends PaperTouchScreen{

    PImage[] radioImages;
    int currentImage = 0;

    Button nextButton, prevButton, lockButton;
    boolean locked = false;
    TwoFingersRST rst;

    void setup(){

	setDrawingSize(297, 210);
	setResolution(boardResolution);
	loadMarkerBoard(sketchPath + "/radio/radio.cfg", 297, 210);

	rst = new TwoFingersRST(this.drawingSize);
	rst.setDisabledYZone(50);

	radioImages = new PImage[5];
	
	radioImages[0] = loadImage("radio/radio1.png");
	radioImages[1] = loadImage("radio/radio2.png");
	radioImages[2] = loadImage("radio/radio3.jpg");
	radioImages[3] = loadImage("radio/radio4.jpg");
	radioImages[4] = loadImage("radio/radio5.jpg");
	
	nextButton = new Button("Next" , 
				275, 170, 
				40, 20);
	
	prevButton = new Button("Prev" , 
				275, 150, 
				40, 20);

	lockButton = new Button("Lock" , 
				275, 130, 
				40, 20,
				"UnLock");

	nextButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    currentImage = constrain(currentImage +1, 0, radioImages.length);
		    nextButton.reset();
		}
		public void ButtonReleased(){
		}
	    });
	
	prevButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    currentImage = constrain(currentImage -1, 0, radioImages.length);
		    prevButton.reset();
		}
		public void ButtonReleased(){
		}
	    });

	
	lockButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    locked = true;
		}
		public void ButtonReleased(){
		    locked = false;
		}
	    });

	buttons.add(nextButton);
	buttons.add(prevButton);
	buttons.add(lockButton);
    }


    void draw(){

	//		updateMultiTouch(this.screen, this.touchInput);
	beginDraw2D();
	
	background(0);

	pushMatrix();

	if(!locked) 
	    rst.update(touchList, millis());
	rst.applyTransformationTo(this);
	translate(0,0, - 1);
	imageMode(CENTER);
	image(radioImages[currentImage % radioImages.length],
	      0, 0,
	      drawingSize.x, drawingSize.y);
	popMatrix();

	//	drawTouchSpeed();

	fill(58, 71, 198);
	int k = 0;
	int ellipseSize = 10;
	for (Touch t : touchList) {
            if (t.is3D)
                continue;
            ellipse(t.position.x, t.position.y, ellipseSize, ellipseSize);
	    k++;
	    if(k == 2) 
		break;
        }



	drawButtons();
	endDraw();
    }


}


