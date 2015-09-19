import java.awt.Frame;


public class ControlFrame extends PApplet {

    public ControlFrame() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
	size(300, 500, P3D);
    }

    Skatolo skatolo;

    public void setup() {
        frameRate(25);
        skatolo = new Skatolo(this);

        // add a horizontal sliders, the value of this slider will be linked
        // to variable 'sliderValue'
        skatolo.addBang("Camera").plugTo(mainApplet, "setCorners")
            .setPosition(10, 20)
            ;

        skatolo.addBang("Projector").plugTo(mainApplet, "setProjection")
            .setPosition(10, 60)
            ;

        skatolo.addRadioButton("activeCorner")
            .setPosition(10,100 )
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
