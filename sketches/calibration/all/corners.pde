String cornersFileName;

PVector[] corners = new PVector[4];
PVector[] objectPoints = new PVector[4];


//float objectWidth = 420, objectHeight = 297;
//float objectWidth = 350, objectHeight = 250;
float objectWidth = 297, objectHeight = 210;
int rectAroundWidth = 10;

PMatrix3D objectProjectorTransfo;

void activateCameraCorners(){
    cornersFileName = "data/cameraCorners.json";

    controlFrame.showCorners();
    controlFrame.showObjectSize();
    isObjectSize = true;
    areCorners = true;
    controlFrame.showSaveCameraButton();
    isSaveButtonShowed = true;
    try{
        loadCorners();
    } catch(Exception e){};
}


void activateCameraKinectCorners(){
    cornersFileName = "data/cameraKinectCorners.json";
    controlFrame.showCorners();
    controlFrame.showObjectSize();
    isObjectSize = true;
    areCorners = true;

    isSaveButtonShowed = true;
    try{
        loadCorners();
    } catch(Exception e){};
}

void activateProjectorCornersObject(){
    cornersFileName = "data/projectorCornersObject.json";
    controlFrame.showCorners();
    controlFrame.showObjectSize();

    areCorners = true;
    isObjectSize = true;

    try{
        loadCorners();
    } catch(Exception e){};
}

void activateProjectorCorners(){

    // second Window
    areProjectorCorners = true;
    projectorCorners.getSurface().setVisible(true);

    // sliders
    controlFrame.showCorners();
    areCorners = true;

    cornersFileName = "data/projectorCorners.json";
    try{
        loadCorners();
    } catch(Exception e){};
}



void draw3DCorners(){

    ProjectiveDeviceP pdp;
    ARDisplay display = null;
    if(Mode.is("ProjManual")){
        display = projector;
    }
    if(Mode.is("CamManual")){
        display = arDisplay;
    }
    if(Mode.is("KinectManual")){
        display = arDisplayKinect;
    }


    assert(display != null);

    pdp = display.getProjectiveDeviceP();
    updateObjectPointsSizes();

    objectProjectorTransfo = pdp.estimateOrientation(objectPoints, corners);

    controlFrame.setText(objectProjectorTransfo);

    PGraphicsOpenGL g1 = display.beginDraw();
    g1.background(69, 145, 181, 100);
    g1.modelview.apply(objectProjectorTransfo);

    g1.rectMode(CORNER);
    g1.fill(50, 50, 200, 100);
    // g1.translate(-10, -10, 0);
    g1.rect(-rectAroundWidth,
            -rectAroundWidth,
            objectWidth + rectAroundWidth*2,
            objectHeight + rectAroundWidth*2);

    g1.stroke(0);
    g1.strokeWeight(1);
    g1.rectMode(CENTER);

    drawRectAt(g1, -36.5f, -33.125);
    drawRectAt(g1, 313.017f, -33.125);
    drawRectAt(g1, 313.017f, 224.482);
    drawRectAt(g1, -36.5f, 224.482);

    display.endDraw();

    DrawUtils.drawImage((PGraphicsOpenGL) g,
                        display.render(),
                        0, 0, width, height);
}

void drawRectAt(PGraphicsOpenGL g1, float px, float py){
    float rectWidth = 15f;

    g1.pushMatrix();
    g1.translate(px, py, 0);
    g1.translate(rectWidth / 2, rectWidth /2);
    g1.fill(#bcd0c0);
    g1.rect(0,0, rectWidth -2 , rectWidth -2);
    g1.rect(0,0, rectWidth + 5 , rectWidth +5);
    g1.popMatrix();
}


void initCorners() {
    // Corners of the image of the projector
    corners[0] = new PVector(100, 100);
    corners[1] = new PVector(200, 100);
    corners[2] = new PVector(200, 200);
    corners[3] = new PVector(100, 200);

    objectPoints[0] = new PVector();
    objectPoints[1] = new PVector();
    objectPoints[2] = new PVector();
    objectPoints[3] = new PVector();
    updateObjectPointsSizes();
}

void updateObjectPointsSizes(){
    objectPoints[0].set(0, 0, 0);
    objectPoints[1].set(objectWidth, 0, 0);
    objectPoints[2].set(objectWidth, objectHeight, 0);
    objectPoints[3].set(0, objectHeight, 0);
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


boolean showCornerZoom = false;
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

    println("key Pressed in main.");

    if(Mode.is("ProjManual") ||
       Mode.is("CamManual") ||
       Mode.is("KinectManual") ||
       Mode.is("ProjMarker")
        ){
        if(key == '1' ){
            controlFrame.activateCornerNo(1);
        }

        if(key == '2' ){
            controlFrame.activateCornerNo(2);
        }

        if(key == '3' ){
            controlFrame.activateCornerNo(3);
        }

        if(key == '0' ){
            controlFrame.activateCornerNo(0);
        }

        if(key == 'l'){
            loadCorners();
        }
        if(key == 's'){
            saveCorners();
        }

        if (key == CODED) {
            if (keyCode == UP) {
                corners[currentCorner].y -= 1;
            }

            if (keyCode == DOWN) {
                corners[currentCorner].y += 1;
            }

            if (keyCode == LEFT) {
                corners[currentCorner].x -= 1;
            }

            if (keyCode == RIGHT) {
                corners[currentCorner].x += 1;
            }
        }

    }
}
