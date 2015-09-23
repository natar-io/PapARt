boolean isCameraMode = false;
boolean isProjectorMode = false;
boolean isARCam = false;
boolean isARProj= false;
boolean areCorners = false;
boolean areProjectorCorners = false;
boolean isObjectSize = false;

void cameraMode(){

    papart.forceCameraSize();
    isCameraMode = true;
    isProjectorMode = false;
}

void projectorMode(){
    // projector.automaticMode();
    papart.forceProjectorSize();
    isCameraMode = false;
    isProjectorMode = true;
}

void noMode(){
    Mode.set("None");

    if(isARCam){
        arDisplay.manualMode();
        isARCam = false;
    }

    if(isARProj){
        projector.manualMode();
        isARProj = false;
    }

    if(areCorners){
        controlFrame.hideCorners();
        areCorners = false;
    }

    if(areProjectorCorners){
        projectorCorners.getSurface().setVisible(false);
        areProjectorCorners = false;
    }

    if(isObjectSize){
        controlFrame.hideObjectSize();
        isObjectSize = false;
    }

    isCameraMode = false;
    isProjectorMode = false;
}

public void camMode(int value){

    if(value == -1){
        noMode();
        return;
    }

    controlFrame.resetProjRadio();
    noMode();

    if(!isCameraMode){
        cameraMode();
    }

    switch(value) {
    case 0:
        Mode.set("CamView");
        break;
    case 1:
        Mode.set("CamMarker");
        arDisplay.automaticMode();
        isARCam = true;
        break;
    case 2:
        Mode.set("CamManual");
        activateCameraCorners();
        controlFrame.showCorners();
        controlFrame.showObjectSize();
        isObjectSize = true;
        areCorners = true;
        break;
    }
}



public void projMode(int value){


    if(value == -1){
        noMode();
        return;
    }

    controlFrame.resetCamRadio();
    noMode();

    switch(value) {
    case 0:
        Mode.set("ProjManual");
        projectorMode();
        activateProjectorCornersObject();
        break;
    case 1:
        Mode.set("ProjMarker");
        cameraMode();
        activateProjectorCorners();
        break;
    case 2:
        Mode.set("ProjView");
        projectorMode();
        projector.automaticMode();
        isARProj = true;
        break;
    }
}
