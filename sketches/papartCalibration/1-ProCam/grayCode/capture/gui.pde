import fr.inria.papart.calibration.*;

import fr.inria.skatolo.*;
import fr.inria.skatolo.gui.controllers.*;


ControlFrame cf;

// Modes
//   wait , start , code , result

Bang saveScanBang, decodeBang;

void initGui(){

    Mode.init(this);
    Mode.add("wait");
    Mode.add("start");
    Mode.add("code");
    Mode.add("result");

    Mode.set("wait");
    cf = new ControlFrame();
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
