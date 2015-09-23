PMatrix3D camBoard(){
    return board.getTransfoMat(camera).get();
}

PMatrix3D kinect360Board(){
    assert(isKinect360Activated);
    return board.getTransfoMat(cameraKinect).get();
}

PMatrix3D projBoard(){
    IplImage projImage = projectorImage();
    if(projImage == null)
        return null;
    board.updatePosition(projectorAsCamera, projImage);
    return board.getTransfoMat(projectorAsCamera);
}

IplImage grayImage = null;

IplImage projectorImage(){

    projectorView.setCorners(corners);
    IplImage projImage = projectorView.getIplViewOf(camera);

    if(board.useARToolkit()){
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
