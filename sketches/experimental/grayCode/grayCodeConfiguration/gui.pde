
import fr.inria.papart.calibration.*;

import fr.inria.skatolo.*;
import fr.inria.skatolo.gui.controllers.*;


Skatolo skatolo;

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

    skatolo = new Skatolo(this);

    // add a horizontal sliders, the value of this slider will be linked
    // to variable 'sliderValue' 
    skatolo.addSlider("displayTime")
	.setPosition(frameSizeX + 10, 20)
	.setRange(30,1200)
	.setValue(displayTime)
	;
    
    skatolo.addSlider("captureTime")
	.setPosition(frameSizeX + 10, 40)
	.setRange(30, 1200)
	.setValue(captureTime)
	;
    
    skatolo.addSlider("delay")
	.setPosition(frameSizeX + 10, 60)
	.setRange(0, 300)
	.setValue(delay)
	;
    
    
    skatolo.addSlider("sc")
	.setPosition(frameSizeX + 10, 80)
	.setRange(1, 8)
	.setValue(sc)
	.setLabel("pixel scale")
	;
    
    skatolo.addRadioButton("decodeType")
    	.setPosition(frameSizeX + 10, 100)
    	.addItem("reference", GrayCode.DECODE_REF)
    	.addItem("absolute", GrayCode.DECODE_ABS)
    	.activate(1)
    	.setNoneSelectedAllowed(true) 
    	;

    skatolo.addSlider("decodeValue")
    	.setPosition(frameSizeX + 10, 130)
    	.setRange(0, 255)
    	.setValue(decodeValue)
    	.setLabel("Decode value")
    	;

    skatolo.addBang("startButton")
	.setPosition(frameSizeX + 10, 200)
	.setSize(20, 20)
	.setLabel("Start")
	;
    
    
    skatolo.addBang("saveCalib")
	.setPosition(frameSizeX + 10, 400)
	.setSize(20, 20)
	.setLabel("Save Calibration")
	;
    

    decodeBang = skatolo.addBang("decodeBang")
	.setPosition(frameSizeX + 10, 260)
	.setSize(20, 20)
	.setLabel("Decode again")
	;
    
    saveScanBang = skatolo.addBang("saveScan")
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
