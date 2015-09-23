import fr.inria.skatolo.*;
import fr.inria.skatolo.gui.group.*;
import fr.inria.skatolo.gui.controllers.*;
import fr.inria.skatolo.gui.controllers.Slider;
import fr.inria.skatolo.gui.controllers.Button;

public class ControlFrame extends PApplet {

    public ControlFrame() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
	size(800, 600, P3D);
    }

    Skatolo skatolo;

    RadioButton corners, camRadio, projRadio;
    Slider objectWidth, objectHeight;

    public void hideCorners(){
        corners.hide();
    }

    public void showCorners(){
        corners.show();
    }

    public void hideObjectSize(){
        objectWidth.hide();
        objectHeight.hide();
    }

    public void showObjectSize(){
        objectWidth.show();
        objectHeight.show();
    }

    public void resetCamRadio(){
        camRadio.deactivateAll();
    }

    public void resetProjRadio(){
        projRadio.deactivateAll();
    }

    public void setup() {
        frameRate(25);
        skatolo = new Skatolo(this);

        // add a horizontal sliders, the value of this slider will be linked
        // to variable 'sliderValue'
        skatolo.addBang("calibrate").plugTo(mainApplet, "calibrate")
            .setPosition(10, 10)
            ;

        Mode.add("CamManual");
        Mode.add("CamView");
        Mode.add("CamMarker");

        camRadio = skatolo.addRadioButton("Camera calibration").plugTo(mainApplet, "camMode")
            .setPosition(100, 100)
            .setItemWidth(20)
            .setItemHeight(20)
            .addItem("CamView", 0)
            .addItem("CamMarker", 1)
            .addItem("CamManual", 2)
            .setColorLabel(color(255))
            ;

        Mode.add("ProjManual");
        Mode.add("ProjMarker");
        Mode.add("ProjView");

        projRadio = skatolo.addRadioButton("Projector calibration").plugTo(mainApplet, "projMode")
            .setPosition(100, 250)
            .setItemWidth(20)
            .setItemHeight(20)
            .addItem("ProjManual", 0)
            .addItem("ProjMarker", 1)
            .addItem("ProjView", 2)
            .setColorLabel(color(255))
            ;


        corners = skatolo.addRadioButton("Corners")
            .setPosition(400,50 )
            .setItemWidth(20)
            .setItemHeight(20)
            .addItem("bottom Left", 0) // 0, y
            .addItem("bottom Right", 1) // x ,y
            .addItem("top right", 2)  // x, 0
            .addItem("Top Left ", 3)  // 0, 0
            .activate(0)
            .plugTo(mainApplet, "activeCorner")
            ;

        objectWidth = skatolo.addSlider("ObjectWidth")
            .setPosition(400, 150 )
            .setValue(420)
            .setRange(200, 500)
            .setSize(300, 12)
        .plugTo(mainApplet, "objectWidth")
             ;

        objectHeight = skatolo.addSlider("ObjectHeight")
            .setPosition(400,180 )
            .setValue(297)
            .setRange(200, 400)
            .setSize(200, 12)
        .plugTo(mainApplet, "objectHeight")
             ;


        hideCorners();
        hideObjectSize();

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
