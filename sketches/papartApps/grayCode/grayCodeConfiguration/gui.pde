
import fr.inria.papart.depthcam.calibration.*;

import fr.inria.controlP5.*;
import fr.inria.controlP5.gui.controllers.*;


ControlP5 cp5;

Mode waitForStartMode, startPressed, displayCodeMode, displayResult;
//   wait            , start       , code           , result      

Bang saveScanBang, decodeBang;

void initGui(){
    
    Mode.init(this);
    waitForStartMode = Mode.add("wait");
    startPressed = Mode.add("start");
    displayCodeMode = Mode.add("code");
    displayResult = Mode.add("result");

    Mode.set("wait");

    cp5 = new ControlP5(this);

    // add a horizontal sliders, the value of this slider will be linked
    // to variable 'sliderValue' 
    cp5.addSlider("displayTime")
	.setPosition(frameSizeX + 10, 20)
	.setRange(30,1200)
	.setValue(displayTime)
	;
    
    cp5.addSlider("captureTime")
	.setPosition(frameSizeX + 10, 40)
	.setRange(30, 1200)
	.setValue(captureTime)
	;
    
    cp5.addSlider("delay")
	.setPosition(frameSizeX + 10, 60)
	.setRange(0, 300)
	.setValue(delay)
	;
    
    
    cp5.addSlider("sc")
	.setPosition(frameSizeX + 10, 80)
	.setRange(1, 8)
	.setValue(sc)
	.setLabel("pixel scale")
	;
    
    cp5.addRadioButton("decodeType")
    	.setPosition(frameSizeX + 10, 100)
    	.addItem("reference", GrayCode.DECODE_REF)
    	.addItem("absolute", GrayCode.DECODE_ABS)
    	.activate(1)
    	.setNoneSelectedAllowed(true) 
    	;

    cp5.addSlider("decodeValue")
    	.setPosition(frameSizeX + 10, 130)
    	.setRange(0, 255)
    	.setValue(decodeValue)
    	.setLabel("Decode value")
    	;

    cp5.addBang("startButton")
	.setPosition(frameSizeX + 10, 200)
	.setSize(20, 20)
	.setLabel("Start")
	;
    
    
    cp5.addBang("saveCalib")
	.setPosition(frameSizeX + 10, 400)
	.setSize(20, 20)
	.setLabel("Save Calibration")
	;
    

    decodeBang = cp5.addBang("decodeBang")
	.setPosition(frameSizeX + 10, 260)
	.setSize(20, 20)
	.setLabel("Decode again")
	;
    
    saveScanBang = cp5.addBang("saveScan")
	.setPosition(frameSizeX + 10, 300)
	.setSize(20, 20)
	.setLabel("Save decoded")
	;

    saveScanBang.hide();
    decodeBang.hide();

}


public void startButton() {
    checkStart();
}


public void decodeBang(){
    decode();
}

public void decodeType(int a){
    println("Decode type " +  a );
    this.decodeType = a;
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
