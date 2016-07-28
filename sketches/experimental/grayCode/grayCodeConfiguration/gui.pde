import fr.inria.papart.calibration.*;

import fr.inria.skatolo.*;
import fr.inria.skatolo.gui.controllers.*;


Skatolo skatolo;

Mode waitForStartMode, startPressed, displayCodeMode, displayResult;
//   wait            , start       , code           , result

Bang saveScanBang, decodeBang;
ControlFrame cf;

PApplet mainSketch;

void initGui(){

    Mode.init(this);
    waitForStartMode = Mode.add("wait");
    startPressed = Mode.add("start");
    displayCodeMode = Mode.add("code");
    displayResult = Mode.add("result");

    Mode.set("wait");
    mainSketch = this;
    cf = new ControlFrame();
}


// the ControlFrame class extends PApplet, so we
// are creating a new processing applet inside a
// new frame with a skatolo object loaded
public class ControlFrame extends PApplet {
    Skatolo skatolo;

    public ControlFrame() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
	size(400, 400, P2D);
    }

    public void setup(){

        skatolo = new Skatolo(this);

        // add a horizontal sliders, the value of this slider will be linked
        // to variable 'sliderValue'
        skatolo.addSlider("displayTime")
            .setPosition( 10, 20)
            .setRange(30,1200)
            .setValue(displayTime)
            .plugTo(mainSketch, "displayTime")
            ;

        skatolo.addSlider("captureTime")
            .setPosition( 10, 40)
            .setRange(30, 1200)
            .setValue(captureTime)
            .plugTo(mainSketch, "captureTime")
            ;

        skatolo.addSlider("delay")
            .setPosition( 10, 60)
            .setRange(0, 300)
            .setValue(delay)
            .plugTo(mainSketch, "delay")
            ;


        skatolo.addSlider("sc")
            .setPosition( 10, 80)
            .setRange(1, 8)
            .setValue(sc)
            .plugTo(mainSketch, "sc")
            .setLabel("pixel scale")
            ;

        skatolo.addSlider("blackColor")
            .setPosition(10, 100)
            .setRange(0, 255)
            .setValue(blackColor)
            .plugTo(mainSketch, "blackColor")
            ;

        skatolo.addSlider("whiteColor")
            .setPosition(10, 110)
            .setRange(0, 255)
            .setValue(whiteColor)
            .plugTo(mainSketch, "whiteColor")
            ;



        skatolo.addRadioButton("decodeType")
            .setPosition( 10, 140)
            .addItem("reference", GrayCode.DECODE_REF)
            .addItem("absolute", GrayCode.DECODE_ABS)
            .activate(1)
            .setNoneSelectedAllowed(true)
            .plugTo(mainSketch, "decodeType")
            ;

        skatolo.addSlider("decodeValue")
            .setPosition( 10, 160)
            .setRange(0, 255)
            .setValue(decodeValue)
            .setLabel("Decode value")
            .plugTo(mainSketch, "decodeValue")
            ;


        skatolo.addBang("startButton")
            .setPosition( 10, 200)
            .setSize(20, 20)
            .setLabel("Start")
            .plugTo(mainSketch, "checkStart")
            ;


        skatolo.addBang("saveCalib")
            .setPosition(100, 300)
            .setSize(20, 20)
            .setLabel("Save Calibration")
            ;

        decodeBang = skatolo.addBang("decodeBang")
            .setPosition( 10, 260)
            .setSize(20, 20)
            .setLabel("Decode again")
            .plugTo(mainSketch, "decode")
            ;

        saveScanBang = skatolo.addBang("saveScan")
            .setPosition(10, 300)
            .setSize(20, 20)
            .setLabel("Save decoded")
            ;

        saveScanBang.hide();
        decodeBang.hide();
    }




    int saveID = 0;
    public void saveScan(){
        grayCode.save("scan"+saveID);
        saveID++;
        saveScanBang.hide();
    }


    public void loadCalib(){

    }

    public void saveCalib(){

        CameraProjectorSync cps = new CameraProjectorSync(displayTime, captureTime, delay);
        cps.setDecodeParameters(decodeType, decodeValue);

        cps.saveTo(this, "sync.xml");
    }


    public void draw(){
        background(0);
    }

    public Skatolo control() {
        return skatolo;
    }
}
