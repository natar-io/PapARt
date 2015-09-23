String cornersFileName;

void activateCameraCorners(){
    cornersFileName = "data/cameraCorners.json";
    try{
        loadCorners();
    } catch(Exception e){};
}

void activateProjectorCornersObject(){
    cornersFileName = "data/projectorCornersObject.json";
    controlFrame.showCorners();
    areCorners = true;
    controlFrame.showObjectSize();
    isObjectSize = true;

    try{
        loadCorners();
    } catch(Exception e){};
}

void activateProjectorCorners(){

    projectorCorners.getSurface().setVisible(true);
    areProjectorCorners = true;
    areCorners = true;

    cornersFileName = "data/projectorCorners.json";
    try{
        loadCorners();
    } catch(Exception e){};
}


void draw3DCorners(){
    // ProjectiveDeviceP pdp = projector.getProjectiveDeviceP();
    // PMatrix3D objectProjectorTransfo = pdp.estimateOrientation(object, imageScaled);

    // PGraphicsOpenGL g1 = projector.beginDraw();
    // g1.background(69, 145, 181);
    // g1.modelview.apply(objectProjectorTransfo);

    // g1.fill(50, 50, 200, 100);
    // // g1.translate(-10, -10, 0);
    // g1.rect(-rectAroundWidth,
    //         -rectAroundWidth,
    //         objectWidth + rectAroundWidth*2,
    //         objectHeight + rectAroundWidth*2);

    // g1.translate(objectWidth + 100, objectHeight + 100, 0);
    // g1.fill(0, 191, 100, 100);
    // g1.rect(150, 80, 100, 100);

    // projector.endDraw();

    // DrawUtils.drawImage((PGraphicsOpenGL) g,
    //                     projector.render(),
    //                     0, 0, width, height);
}


void saveCorners(){
    JSONArray values = new JSONArray();
    for(int i = 0; i < corners.length; i++){
        JSONObject cornerJSON = new JSONObject();
        cornerJSON.setFloat("x", corners[i].x);
        cornerJSON.setFloat("y", corners[i].y);
        values.setJSONObject(i, cornerJSON);
    }
    saveJSONArray(values, cornersFileName);
}

void loadCorners(){
    JSONArray values = loadJSONArray(cornersFileName);
    for (int i = 0; i < values.size(); i++) {
        JSONObject cornerJSON = values.getJSONObject(i);
        corners[i].set(cornerJSON.getFloat("x"),
                       cornerJSON.getFloat("y"));
    }
}



int currentCorner = 0;
void activeCorner(int value){
    if(value == -1)
        value = 0;
    currentCorner = value;
}

void mouseDragged() {
    if(areCorners){
        corners[currentCorner].set(mouseX, mouseY);
    }
}

void keyPressed(){
    if(key == 'l'){
        loadCorners();
    }
    if(key == 's'){
        saveCorners();
    }
}
