int startTime;
int nextCapture;

int nbCaptured = 0;
int nbCodes;

int code;
int codeProjected;

void updateCodes(){
    code = (currentTime() / displayTime) %  nbCodes;
    codeProjected = ((currentTime() - delay) / displayTime) %  nbCodes;
}

boolean allCodesCaptured(){
    return nbCaptured >= nbCodes;
}

boolean captureOK(){
    return millis() >= nextCapture;
}

void setNextCaptureTime(){
    int elapsed = displayTime * (codeProjected+1);
    nextCapture = startTime + elapsed + captureTime;
}

int currentTime() {
    return millis() - startTime;
}




