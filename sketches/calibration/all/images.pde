PMatrix3D currentCamBoard(){
    return board.getTransfoMat(camera).get();
}

PMatrix3D currentKinect360Board(){
    assert(isKinect360Activated);
    return board.getTransfoMat(cameraKinect).get();
}

PMatrix3D currentProjBoard(){
    IplImage projImage = projectorImage();
    if(projImage == null)
        return null;


    DetectedMarker[] markers = DetectedMarker.detect(this.projectorTracker, projImage);
    board.updateLocation(projectorAsCamera, projImage, markers);

    return board.getTransfoMat(projectorAsCamera);
}


IplImage grayImage = null;

IplImage projectorImage(){

    projectorView.setCorners(corners);
    IplImage projImage = projectorView.getIplViewOf(camera);

    if(board.useGrayscaleImages()){
        projImage =  greyProjectorImage(projImage);
    }

    return projImage;
}


IplImage greyProjectorImage(IplImage projImage){
    if(grayImage == null){
        grayImage = IplImage.create(projector.getWidth(),
                                    projector.getHeight(),
                                    IPL_DEPTH_8U, 1);
    }
    cvCvtColor(projImage, grayImage, CV_BGR2GRAY);

    // if(test){
    //     cvSaveImage( sketchPath() + "/data/projImage.jpg", grayImage);
    //     cvSaveImage( sketchPath() + "/data/camImage.jpg", camera.getIplImage());
    // }
    return grayImage;
}
