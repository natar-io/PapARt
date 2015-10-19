import toxi.geom.Plane;
import toxi.geom.Triangle3D;

boolean isCalibrated = false;

PMatrix3D  cameraPaperCalibration = null;
PMatrix3D  projectorPaperCalibration = null;
PMatrix3D  kinectPaperCalibration = null;

boolean isCamPaperSet = false;
boolean isProjPaperSet = false;
boolean isKinectPaperSet = false;

boolean useDefautExtrinsics = false;

ArrayList<CalibrationSnapshot> snapshots = new ArrayList<CalibrationSnapshot>();
int calibrationNumber = 0;


public void useExtrinsicsFromProjector(){
    useDefautExtrinsics = true;
    controlFrame.showCalibrateProCam();
}


public void saveCameraPaper(){

    assert(Mode.is("CamMarker") || Mode.is("CamManual"));

    if(Mode.is("CamMarker")){
        cameraPaperCalibration = currentCamBoard().get();
    }

    if(Mode.is("CamManual")){
        cameraPaperCalibration = objectProjectorTransfo.get();
    }

    controlFrame.setCameraPaperLabel("Camera - Paper OK");
    isCamPaperSet = true;

    checkIfCalibrationPossible();
    noMode();
}

public void saveProjectorPaper(){
    assert(Mode.is("ProjMarker") || Mode.is("ProjManual"));

    if(Mode.is("ProjMarker")){
        projectorPaperCalibration = currentProjBoard().get();
    }

    if(Mode.is("ProjManual")){
        projectorPaperCalibration = objectProjectorTransfo.get();
    }

    isProjPaperSet = true;
    controlFrame.setProjectorPaperLabel("Projection - Paper OK");
    checkIfCalibrationPossible();

    noMode();
}

public void saveKinectPaper(){
    assert(Mode.is("KinectMarker") || Mode.is("KinectManual"));

    if(Mode.is("KinectMarker")){
        kinectPaperCalibration = currentKinect360Board().get();
    }

    if(Mode.is("KinectManual")){
        kinectPaperCalibration = objectProjectorTransfo.get();
    }

    isKinectPaperSet = true;
    controlFrame.setKinectPaperLabel("Kinect - Paper OK");
    checkIfCalibrationPossible();

    noMode();
}

void checkIfCalibrationPossible(){
    if(isProjPaperSet && isCamPaperSet){
        controlFrame.showAddProCamCalibration();

        checkIfKinectCalibrationPossible();
    }
}

void checkIfKinectCalibrationPossible(){
    if(isKinectPaperSet || isKinectOne){
        controlFrame.showCalibrateKinectCam();
    }

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


public void calibrateKinectCam(){
    if(isKinectOne){
        calibrateKinectOne();
    }
    if(isKinect360){
        calibrateKinect360();
    }

    controlFrame.hideCalibrateKinectCam();
}



public void addProCamCalibrationData(){

    // PMatrix3D camPaper = camBoard();
    // PMatrix3D projPaper = projBoard().get();
    // projPaper.invert();
    // projPaper.preApply(camPaper);
    // projPaper.invert();
    // proCamCalibrations.add (projPaper);

    // snapshots
    CalibrationSnapshot snapshot =
        new CalibrationSnapshot(cameraPaperCalibration,
                                projectorPaperCalibration,
                                kinectPaperCalibration);
    snapshots.add(snapshot);

    controlFrame.showCalibrateProCam();
    controlFrame.hideAddProCamCalibration();
    calibrationNumber = calibrationNumber + 1;
    isProjPaperSet = false;
    isCamPaperSet = false;
    isKinectPaperSet = false;

    controlFrame.setProjectorPaperLabel("Please set the calibration.");
    controlFrame.setCameraPaperLabel("Please set the calibration.");
    controlFrame.setKinectPaperLabel("Please set the calibration.");
}

public void calibrateProCam(){

    if(useDefautExtrinsics){
        calibrateWithDefaultFile();
    } else {
        computeManualCalibrations();
    }

    // something need to change, to recalibrate.
    controlFrame.hideCalibrateProCam();

    // Set to projection mode to test.
    noMode();
    projMode(2);
}

private void calibrateWithDefaultFile(){
    try{
        ProjectiveDeviceP projectiveDeviceP = ProjectiveDeviceP.loadProjectorDevice(
            this, Papart.projectorCalib);

        if(!projectiveDeviceP.hasExtrinsics()){
            println("The projector calibration does not have extrinsic calibration: "  + Papart.projectorCalib);
            return;
        }
        println("Calibrating with " + Papart.projectorCalib);
        PMatrix3D projPaper = projectiveDeviceP.getExtrinsics();

        projPaper.print();

        papart.saveCalibration(Papart.cameraProjExtrinsics, projPaper);

    } catch(Exception e){
        println("Could not use the projector calibration: "  + Papart.projectorCalib);
    }

}

private void computeManualCalibrations(){
    PMatrix3D sum = new PMatrix3D(0, 0, 0, 0,
                                  0, 0, 0, 0,
                                  0, 0, 0, 0,
                                  0, 0, 0, 0);

    for(CalibrationSnapshot snapshot : snapshots){
        PMatrix3D extr = computeExtrinsics(snapshot.cameraPaper,
                                           snapshot.projectorPaper);
        addMatrices(sum, extr);
    }
    multMatrix(sum, 1f / (float) snapshots.size());
    papart.saveCalibration(Papart.cameraProjExtrinsics, sum);
    projector.setExtrinsics(sum);
}

private PMatrix3D computeExtrinsics(PMatrix3D camPaper, PMatrix3D projPaper){
    PMatrix3D extr = projPaper.get();
     extr.invert();
     extr.preApply(camPaper);
     extr.invert();
     return extr;
}

public void clearCalibrations(){
    snapshots.clear();
    calibrationNumber = 0;
}

private void calibrateKinectOne(){
    PMatrix3D kinectExtr = kinectDevice.getStereoCalibration().get();
    kinectExtr.invert();

    PMatrix3D boardViewFromDepth = camBoard();

    // camBoard().print();
    // boardViewFromDepth.print();

    planeCalibCam = PlaneCalibration.CreatePlaneCalibrationFrom(boardViewFromDepth,
                                                                new PVector(297, 210));
    planeCalibCam.flipNormal();

    // identity - no external camera for ProCam calibration
    PMatrix3D kinectCameraExtrinsics = new PMatrix3D();
    // Depth -> Color calibration.
    kinectCameraExtrinsics.set(kinectExtr);

    boolean inter = computeScreenPaperIntersection(planeCalibCam, kinectCameraExtrinsics);

    if(!inter){
        println("No intersection");
        return;
    }

    // move the plane up a little.
    planeCalibCam.moveAlongNormal(-7f);

    saveKinectPlaneCalibration(planeCalibCam);
    saveKinectCameraExtrinsics(kinectCameraExtrinsics);
}

private void calibrateKinect360(){
    calibrateKinect360Extr();
    calibrateKinect360Plane();
}

private void calibrateKinect360Extr(){
    // Depth -> color  extrinsics
    PMatrix3D kinectExtr = kinectDevice.getStereoCalibration().get();

    // color -> depth  extrinsics
    kinectExtr.invert();

    // depth -> tracking
    PMatrix3D kinectCameraExtrinsics = computeKinectCamExtrinsics(kinectExtr);

    // // tracking -> depth
    kinectCameraExtrinsics.invert();

    saveKinectCameraExtrinsics(kinectCameraExtrinsics);

}

private void calibrateKinect360Plane(){
    // Depth -> color  extrinsics
    PMatrix3D kinectExtr = kinectDevice.getStereoCalibration().get();

    // color -> depth  extrinsics
    kinectExtr.invert();

    planeCalibCam = computeAveragePlaneCam();
    PlaneCalibration planeCalibKinect = computeAveragePlaneKinect(kinectExtr);
    planeCalibCam.flipNormal();

    PMatrix3D kinectCameraExtrinsics = papart.loadCalibration(Papart.kinectTrackingCalib);

    boolean inter = computeScreenPaperIntersection(planeCalibCam,
                                                   kinectCameraExtrinsics);
    if(!inter){
        println("No intersection");
        kinect360Board().print();
        return;
    }

    // move the plane up a little.
    planeCalibKinect.flipNormal();
    planeCalibKinect.moveAlongNormal(-20f);

    saveKinectPlaneCalibration(planeCalibKinect);
}


private PMatrix3D computeKinectCamExtrinsics(PMatrix3D stereoExtr){

    PMatrix3D sum = new PMatrix3D(0, 0, 0, 0,
                                  0, 0, 0, 0,
                                  0, 0, 0, 0,
                                  0, 0, 0, 0);

    int nbCalib = 0;
    for(CalibrationSnapshot snapshot : snapshots){
        if(snapshot.kinectPaper == null)
            continue;

        // Color -> Paper
        PMatrix3D boardFromDepth = snapshot.kinectPaper.get();

        /// depth -> color -> color -> Paper
        boardFromDepth.preApply(stereoExtr);

        PMatrix3D extr = computeExtrinsics(boardFromDepth, snapshot.cameraPaper);

        addMatrices(sum, extr);
        nbCalib++;
    }

    multMatrix(sum, 1f / (float) nbCalib);
    return sum;
}

private PlaneCalibration computeAveragePlaneKinect(PMatrix3D stereoExtr){
    PVector paperSize = new PVector(297, 210);

    Plane sumKinect = new Plane(new Vec3D(0,0,0),
                                new Vec3D(0,0,0));

    int nbCalib = 0;
    for(CalibrationSnapshot snapshot : snapshots){
        if(snapshot.kinectPaper == null)
            continue;

        //  color -> paper
        PMatrix3D boardFromDepth = snapshot.kinectPaper.get();

        // Depth -> color -> color -> paper
        boardFromDepth.preApply(stereoExtr);

        PlaneCalibration planeCalibKinect =
            PlaneCalibration.CreatePlaneCalibrationFrom(boardFromDepth, paperSize);
        sumPlane(sumKinect, planeCalibKinect.getPlane());
        nbCalib++;
    }

    averagePlane(sumKinect, 1f / nbCalib);

    PlaneCalibration calibration = new PlaneCalibration();
    calibration.setPlane(sumKinect);
    calibration.setHeight(PlaneCalibration.DEFAULT_PLANE_HEIGHT);

    System.out.println("Plane viewed by the kinect");
    println(sumKinect);

    return calibration;
}

private PlaneCalibration computeAveragePlaneCam(){
    PVector paperSize = new PVector(297, 210);

    Plane sumCam = new Plane(new Vec3D(0,0,0),
                             new Vec3D(0,0,0));

    int nbPlanes = 0;
    for(CalibrationSnapshot snapshot : snapshots){

        if(snapshot.cameraPaper == null)
            continue;

        PlaneCalibration cam = PlaneCalibration.CreatePlaneCalibrationFrom(
            snapshot.cameraPaper.get(), paperSize);

        sumPlane(sumCam, cam.getPlane());
        nbPlanes++;
    }

    averagePlane(sumCam, 1f / nbPlanes);

    PlaneCalibration calibration = new PlaneCalibration();
    calibration.setPlane(sumCam);
    calibration.setHeight(PlaneCalibration.DEFAULT_PLANE_HEIGHT);

    System.out.println("Plane viewed by the camera");
    println(sumCam);
    return calibration;
}


void saveKinectCameraExtrinsics(PMatrix3D kinectCameraExtrinsics){
    papart.saveCalibration(Papart.kinectTrackingCalib, kinectCameraExtrinsics);
    papart.setTableLocation(camBoard());
}

void saveKinectPlaneCalibration(PlaneCalibration planeCalib){
    planeProjCalib.setPlane(planeCalib);
    planeProjCalib.setHomography(homographyCalibration);
    planeProjCalib.saveTo(this, Papart.planeAndProjectionCalib);

    println("Calibration OK");
    isCalibrated = true;
}
