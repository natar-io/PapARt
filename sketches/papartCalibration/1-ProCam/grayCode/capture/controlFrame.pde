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
        skatolo.addSlider("displayTime").plugTo(mainApplet, "displayTime")
            .setPosition(10, 20)
	.setRange(30,1200)
	.setValue(displayTime)
	;

    skatolo.addSlider("captureTime").plugTo(mainApplet, "captureTime")
	.setPosition(10, 40)
	.setRange(30, 1200)
	.setValue(captureTime)
	;

    skatolo.addSlider("delay").plugTo(mainApplet, "delay")
	.setPosition(10, 60)
	.setRange(0, 300)
	.setValue(delay)
	;


    skatolo.addSlider("downScale").plugTo(mainApplet, "downScale")
	.setPosition(10, 80)
	.setRange(1, 8)
	.setValue(downScale)
	.setLabel("pixel scale")
	;

    skatolo.addRadioButton("decodeType").plugTo(mainApplet, "decodeType")
    	.setPosition(10, 100)
    	.addItem("reference", GrayCode.DECODE_REF)
    	.addItem("absolute", GrayCode.DECODE_ABS)
    	.activate(1)
    	.setNoneSelectedAllowed(true)
    	;

    skatolo.addSlider("decodeValue").plugTo(mainApplet, "decodeValue")
    	.setPosition(10, 130)
    	.setRange(0, 255)
    	.setValue(decodeValue)
    	.setLabel("Decode value")
    	;

    skatolo.addBang("startButton").plugTo(mainApplet, "startButton")
	.setPosition(10, 200)
	.setSize(20, 20)
	.setLabel("Start")
	;


    skatolo.addBang("saveCalib").plugTo(mainApplet, "saveCalib")
	.setPosition(10, 400)
	.setSize(20, 20)
	.setLabel("Save Calibration")
	;


    decodeBang = skatolo.addBang("decodeBang").plugTo(mainApplet, "decodeBang")
	.setPosition(10, 260)
	.setSize(20, 20)
	.setLabel("Decode again")
	;

    saveScanBang = skatolo.addBang("saveScan").plugTo(mainApplet, "saveScan")
	.setPosition(10, 300)
	.setSize(20, 20)
	.setLabel("Save decoded")
	;

    saveScanBang.hide();
    decodeBang.hide();


  }

  public void draw() {
      background(100);
  }

  public Skatolo control() {
    return skatolo;
  }
}
