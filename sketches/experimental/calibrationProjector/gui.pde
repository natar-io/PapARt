import fr.inria.skatolo.*;
import fr.inria.skatolo.gui.controllers.*;
import java.awt.Frame;


ControlFrame cf;


Bang saveScanBang, decodeBang;

void initGui(){

    cf = new ControlFrame();
}

// the ControlFrame class extends PApplet, so we
// are creating a new processing applet inside a
// new frame with a skatolo object loaded
public class ControlFrame extends PApplet {
  int w, h;
  Skatolo skatolo;

    public void settings(){
        size(1000, 300);
    }

    public ControlFrame() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

  public void setup() {
    frameRate(25);
    skatolo = new Skatolo(this);

    // add a horizontal sliders, the value of this slider will be linked
    // to variable 'sliderValue'

    int width = ardisplay.getWidth();
    int height = ardisplay.getHeight();

    skatolo.addSlider("focal").plugTo(parent, "focal")
	.setPosition(10, 20)
	.setRange(500, 3000)
	.setSize(800,20)
	.setValue(1000)
	;

    skatolo.addSlider("cx").plugTo(parent, "cx")
	.setPosition(10, 60)
	.setRange(0, width  *2)
	.setSize(800,20)
	.setValue(width / 2)
	;

    skatolo.addSlider("cy").plugTo(parent, "cy")
	.setPosition(10, 100)
	.setSize(800,20)
	.setRange(0 , height  *3)
	.setValue(height / 2)
	;

  }

  public void draw() {
      background(100);
  }

  public Skatolo control() {
    return skatolo;
  }
}
