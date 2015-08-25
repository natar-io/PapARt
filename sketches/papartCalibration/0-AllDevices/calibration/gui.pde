
import fr.inria.controlP5.*;
import fr.inria.controlP5.gui.controllers.*;
import java.awt.Frame;


Bang saveScanBang, decodeBang;

void initGui(){
    ControlFrame controlFrame = new ControlFrame(this);

}


// the ControlFrame class extends PApplet, so we 
// are creating a new processing applet inside a
// new frame with a controlP5 object loaded
public class ControlFrame extends PApplet {
  int w, h;
  ControlP5 cp5;
  Object parent;

    public ControlFrame(PApplet parent){
	super();
	this.parent = parent;
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }
    
    public void settings(){
	size(1000, 300);
    }
    
  public void setup() {
    cp5 = new ControlP5(this);

    // add a horizontal sliders, the value of this slider will be linked
    // to variable 'sliderValue' 

    int width = ardisplay.getWidth();
    int height = ardisplay.getHeight();
    
    cp5.addSlider("focal").plugTo(parent, "focal")
	.setPosition(10, 20)
	.setRange(500, 3000)
	.setSize(800,20)
	.setValue(1000)
	;

    cp5.addSlider("cx").plugTo(parent, "cx")
	.setPosition(10, 60)
	.setRange(0, width  *2)
	.setSize(800,20)
	.setValue(width / 2)
	;

    cp5.addSlider("cy").plugTo(parent, "cy")
	.setPosition(10, 100)
	.setSize(800,20)
	.setRange(0 , height  *3)
	.setValue(height / 2)
	;

  }

  public void draw() {
      background(100);
  }
 
}




