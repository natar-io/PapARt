import java.awt.Frame;

ControlFrame addControlFrame(String theName, int theWidth, int theHeight) {
  Frame f = new Frame(theName);
  ControlFrame p = new ControlFrame(this, theWidth, theHeight);
  f.add(p);
  p.init();
  f.setTitle(theName);
  f.setSize(p.w, p.h);
  f.setLocation(100, 100);
  f.setResizable(false);
  f.setVisible(true);
  return p;
}


// the ControlFrame class extends PApplet, so we 
// are creating a new processing applet inside a
// new frame with a controlP5 object loaded
public class ControlFrame extends PApplet {
  int w, h;
  ControlP5 cp5;
  Object parent;
  
  public void setup() {
    size(w, h);
    frameRate(25);
    cp5 = new ControlP5(this);

    // add a horizontal sliders, the value of this slider will be linked
    // to variable 'sliderValue' 
    cp5.addSlider("displayTime").plugTo(parent, "displayTime")
	.setPosition(10, 20)
	.setRange(30,1200)
	.setValue(displayTime)
	;
    
    cp5.addSlider("captureTime").plugTo(parent, "captureTime")
	.setPosition(10, 40)
	.setRange(30, 1200)
	.setValue(captureTime)
	;
    
    cp5.addSlider("delay").plugTo(parent, "delay")
	.setPosition(10, 60)
	.setRange(0, 300)
	.setValue(delay)
	;
    
    
    cp5.addSlider("downScale").plugTo(parent, "downScale")
	.setPosition(10, 80)
	.setRange(1, 8)
	.setValue(downScale)
	.setLabel("pixel scale")
	;
    
    cp5.addRadioButton("decodeType").plugTo(parent, "decodeType")
    	.setPosition(10, 100)
    	.addItem("reference", GrayCode.DECODE_REF)
    	.addItem("absolute", GrayCode.DECODE_ABS)
    	.activate(1)
    	.setNoneSelectedAllowed(true) 
    	;

    cp5.addSlider("decodeValue").plugTo(parent, "decodeValue")
    	.setPosition(10, 130)
    	.setRange(0, 255)
    	.setValue(decodeValue)
    	.setLabel("Decode value")
    	;

    cp5.addBang("startButton").plugTo(parent, "startButton")
	.setPosition(10, 200)
	.setSize(20, 20)
	.setLabel("Start")
	;
    
    
    cp5.addBang("saveCalib").plugTo(parent, "saveCalib")
	.setPosition(10, 400)
	.setSize(20, 20)
	.setLabel("Save Calibration")
	;
    

    decodeBang = cp5.addBang("decodeBang").plugTo(parent, "decodeBang")
	.setPosition(10, 260)
	.setSize(20, 20)
	.setLabel("Decode again")
	;
    
    saveScanBang = cp5.addBang("saveScan").plugTo(parent, "saveScan")
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
 
  private ControlFrame() {
  }

  public ControlFrame(Object theParent, int theWidth, int theHeight) {
    parent = theParent;
    w = theWidth;
    h = theHeight;
  }
  public ControlP5 control() {
    return cp5;
  }
}




