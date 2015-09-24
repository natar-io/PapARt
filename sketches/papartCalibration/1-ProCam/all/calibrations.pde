boolean isCalibrated = false;

PMatrix3D  cameraPaperCalibration = null;
PMatrix3D  projectorPaperCalibration = null;
PMatrix3D  kinectPaperCalibration = null;

boolean isCamPaperSet = false;
boolean isProjPaperSet = false;
boolean isKinectPaperSet = false;

void saveCameraPaper(){

    assert(Mode.is("CamMarker") || Mode.is("CamManual"));

    if(Mode.is("CamMarker")){
        cameraPaperCalibration = currentCamBoard().get();
    }

    if(Mode.is("CamManual")){
        cameraPaperCalibration = objectProjectorTransfo.get();
    }

    isCamPaperSet = true;

    noMode();
}

void saveProjectorPaper(){
    assert(Mode.is("ProjMarker") || Mode.is("ProjManual"));

    if(Mode.is("ProjMarker")){
        projectorPaperCalibration = currentProjBoard().get();
    }

    if(Mode.is("ProjManual")){
        projectorPaperCalibration = objectProjectorTransfo.get();
    }

    isProjPaperSet = true;

    noMode();
}




PMatrix3D camBoard(){
    if(isCamPaperSet){
        return cameraPaperCalibration;
    }
    return currentCamBoard();
}


PMatrix3D kinect360Board(){
    assert(isKinect360Activated);
    if(isKinectPaperSet){
        return kinectPaperCalibration;
    }
    return currentKinect360Board();
}

PMatrix3D projBoard(){
    if(isProjPaperSet){
        return projectorPaperCalibration;
    }
    return currentProjBoard();
}


public void calibrate(){

    calibrateProCam();

    if(isKinectOne){
        calibrateKinectOne();
    }

    if(isKinect360){
        calibrateKinect360();
    }
}

private void calibrateProCam(){
    PMatrix3D camPaper = camBoard();
    PMatrix3D projPaper = projBoard();

    // camPaper.print();
    // projPaper.print();

    projPaper.invert();
    projPaper.preApply(camPaper);
    //    projPaper.print();
    projPaper.invert();

    papart.saveCalibration(Papart.cameraProjExtrinsics, projPaper);
    // projPaper.print();
    projector.setExtrinsics(projPaper);

}

private void calibrateKinectOne(){
    PMatrix3D kinectExtr = kinectDevice.getStereoCalibration().get();
    kinectExtr.invert();

    PMatrix3D boardViewFromDepth = camBoard();
    // boardViewFromDepth.apply(kinectExtr);

    // camBoard().print();
    // boardViewFromDepth.print();

    planeCalibCam = PlaneCalibration.CreatePlaneCalibrationFrom(boardViewFromDepth,
                                                                new PVector(297, 210));
    planeCalibCam.flipNormal();

    kinectCameraExtrinsics.set(kinectExtr);
    // kinectCameraExtrinsics.reset();

    boolean inter = computeScreenPaperIntersection(planeCalibCam);

    if(!inter){
        println("No intersection");
        return;
    }

    // move the plane up a little.
    planeCalibCam.moveAlongNormal(-7f);

    saveKinectCalibration(planeCalibCam);
}


private void calibrateKinect360(){
    PVector paperSize = new PVector(297, 210);

    PlaneCalibration planeCalibKinect =
        PlaneCalibration.CreatePlaneCalibrationFrom(kinect360Board(), paperSize);
    planeCalibCam = PlaneCalibration.CreatePlaneCalibrationFrom(camBoard(), paperSize);
    planeCalibCam.flipNormal();

    kinectCameraExtrinsics = camBoard();
    kinectCameraExtrinsics.invert();
    kinectCameraExtrinsics.preApply(kinect360Board());
    println("Kinect - Camera extrinsics : ");
    kinectCameraExtrinsics.print();

    boolean inter = computeScreenPaperIntersection(planeCalibCam);
    if(!inter){
        println("No intersection");
        kinect360Board().print();
        return;
    }

    // move the plane up a little.
    planeCalibKinect.flipNormal();
    planeCalibKinect.moveAlongNormal(-17f);

    saveKinectCalibration(planeCalibKinect);
}


void saveKinectCalibration(PlaneCalibration planeCalib){
    planeProjCalib.setPlane(planeCalib);
    planeProjCalib.setHomography(homographyCalibration);

    planeProjCalib.saveTo(this, Papart.planeAndProjectionCalib);
    HomographyCalibration.saveMatTo(this,
                                    kinectCameraExtrinsics,
                                    Papart.kinectTrackingCalib);

    papart.setTableLocation(camBoard());
    println("Calibration OK");
    isCalibrated = true;
}
