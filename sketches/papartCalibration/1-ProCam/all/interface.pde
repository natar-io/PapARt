import fr.inria.skatolo.*;
import fr.inria.skatolo.gui.controllers.*;
import fr.inria.skatolo.gui.controllers.Button;

public class ControlFrame extends PApplet {

    public ControlFrame() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
	size(300, 500, P3D);
    }

    Skatolo skatolo;

    public Button kinect360Button, kinectOneButton;

    public void hideKinectButtons(){
        kinect360Button.hide();
        kinectOneButton.hide();
    }

    public void setup() {
        frameRate(25);
        skatolo = new Skatolo(this);

        // add a horizontal sliders, the value of this slider will be linked
        // to variable 'sliderValue'
        skatolo.addBang("calibrate").plugTo(mainApplet, "calibrate")
            .setPosition(10, 10)
            ;

        kinectOneButton = skatolo.addButton("KinectOne").plugTo(mainApplet, "setKinectOne")
            .setPosition(10, 60)
            ;

        kinect360Button = skatolo.addButton("Kinect360").plugTo(mainApplet, "setKinect360")
            .setPosition(10, 90)
            ;

        skatolo.addRadioButton("Projection Corners")
            .setPosition(10,150 )
            .setItemWidth(20)
            .setItemHeight(20)
            .addItem("bottom Left", 0) // 0, y
            .addItem("bottom Right", 1) // x ,y
            .addItem("top right", 2)  // x, 0
            .addItem("Top Left ", 3)  // 0, 0
            .activate(0)
            .plugTo(mainApplet, "activeCorner")
            ;

        // skatolo.addSlider("captureTime").plugTo(mainApplet, "captureTime")
        //     .setPosition(10, 40)
        //     .setRange(30, 1200)
        //     .setValue(captureTime)
        //     ;

        // skatolo.addSlider("delay").plugTo(mainApplet, "delay")
        //     .setPosition(10, 60)
        //     .setRange(0, 300)
        //     .setValue(delay)
        //     ;

  }

  public void draw() {
      background(100);
  }

  public Skatolo control() {
    return skatolo;
  }
}
