
import fr.inria.papart.drawingapp.Button; 

boolean lightOn = false;

public class RenneInterface extends PaperTouchScreen {

    Button lightButton;

    
    public RenneInterface(PApplet parent, 
			     MarkerBoard board, 
			     PVector size, 
			     float resolution, 
			     Camera cam, 
			     Projector proj, 
			     TouchInput touchinput) {
	
	super(parent, board, size, 
	      resolution, 
	      cam, proj, touchinput);
	
	lightButton = new Button("Light Off", 25, 20, 40, 30, "Light On");
	
      buttons.add(lightButton);
      
      lightButton.addListener(new ButtonListener() {
	      public void ButtonPressed(){
		  lightButtonPressed();
	      }
	      public void ButtonReleased(){
		  lightButtonReleased();
	      }
	  });
      
    }

    public void lightButtonPressed(){
	lightOn = true;
    }
    public void lightButtonReleased(){
	lightOn = false;
    }

    
    public void resetPos() {
	screen.resetPos();
    }
      

    public void draw(){

        screen.setDrawing(true);

	// move the screen.
	setLocation(0, 210, 0);

	PGraphicsOpenGL pg = screen.getGraphics();
	pg.beginDraw();
	pg.scale(resolution);

	pg.background(0);
	
	for(Button b : buttons){
	    b.drawSelf(pg);
	}
	
	pg.endDraw();
    }

}
