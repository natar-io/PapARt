int projectionStart = 2000;
int projectionDuration = 400;

boolean isProjectPoint = true;
boolean isSelectPoint = false;

void setProjection(){
    isProjectPoint = true;
    isSelectPoint = false;

    projectionStart = millis();
}

void setSelection(){
    isProjectPoint = false;
    isSelectPoint = true;
}

void setEnded(){
    isProjectPoint = false;
    isSelectPoint = false;
}

void reProject(){
    setProjection();
}

void checkProjection(){
    if(millis() > projectionStart + projectionDuration){
	setSelection();
    }

}
