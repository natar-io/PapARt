int startTime = 6000;
boolean started = false;
boolean start = false;


int nextCapture;

int nbCaptured = 0;
int nbCodes;

int code;
int codeProjected;

void updateCodes(){
    code = (currentTime() / displayTime) %  nbCodes;
    codeProjected = ((millis() - startTime - delay) / displayTime) %  nbCodes;
}

boolean allCodesCaptured(){
    return nbCaptured >= nbCodes;
}


boolean captureOK(){
    return millis() >= nextCapture;
}

void setNextCaptureTime(){
    nextCapture = startTime + displayTime * (codeProjected+1) + captureTime;
}

int currentTime() {
    return millis() - startTime;
}


void checkStart(){

  if(key == 's')
      start = true;
}

private void startCapture(){
    grayCode.setRefImage(cameraTracking.getPImageCopy());
    startTime = millis() + 500;
    println("start Time " + millis());
    started = true;
}


boolean checkIsStarted() {
    if(start == false) 
	return false;
    if(started == true)
	return true;

    if(start && getCameraImage()){
	startCapture();
	started = true;
	return true;
    } else {
	return false;
    }
}
