// TODO: change the modes to the Mode library.
// TODO: change the Button to the "first" Skatolo buttons !


import fr.inria.skatolo.*;
import fr.inria.skatolo.events.*;
import fr.inria.skatolo.gui.controllers.*;
import fr.inria.skatolo.gui.Pointer;

public class LapinInterface extends PaperTouchScreen{

    PVector paperSize = new PVector(160, 160);
    Skatolo skatolo;

    // TODO: Full integration !
    // TODO: use the pointerList ?
    ArrayList<Integer> pointers = new ArrayList();

    public void settings(){
	setDrawingSize((int) paperSize.x, (int) paperSize.y);
	loadMarkerBoard(sketchPath() + "/data/markers/lapinInterface.svg",
			paperSize.x, paperSize.y);
    }

    public void setup(){
        Mode.add("rotate");
        Mode.add("light");
        Mode.add("lock");
        createButtons();
    }

    void createButtons(){

	int button2PosX = 87;
	int button2PosY = 100;
	int buttonSize = 64;

        skatolo = new Skatolo(this.parent, this);

	skatolo.getMousePointer().disable();
	skatolo.setAutoDraw(false);

	// skatolo.addSlider("Sliiiide")
	//     .setPosition(0, 0)
	//     .setSize(130, 10)
	//     ;

	skatolo.addHoverButton("rotate")
	    .setPosition(0, button2PosY)
	    .setSize(buttonSize,buttonSize)
	    ;

        skatolo.addHoverButton("lock")
	    .setPosition(button2PosX, button2PosY)
	    .setSize(buttonSize,buttonSize)
	    ;

	skatolo.addHoverButton("light")
	    .setPosition(button2PosX, 0)
	    .setSize(buttonSize,buttonSize)
	    ;

        // PImage glow = loadImage("glow.png");
	// PImage glowDark = loadImage("glow-dark.png");
    }

    public void rotate(){
        Mode.set("rotate");
        println("Mode is rotate !");
    }

    public void light(){
        Mode.set("light");
        println("Mode is light !");
    }

    public void lock(){
        Mode.set("lock");
    }

    public void resetPos(){
	screen.resetPos();
    }

    public void drawOnPaper(){
	// setLocation(0, -90, 0);
//	clear();

        try{
            background(0);

            updateTouch();

            updateButtons();

            noStroke();

        } catch(Exception e){
            println("eexception " +e );
            e.printStackTrace();
        }
    }

    void updateButtons(){
        skatolo.draw();
        SkatoloLink.updateTouch(touchList, skatolo);
    }

}
