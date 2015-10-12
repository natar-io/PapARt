class CalibrationSnapshot{

    PMatrix3D  cameraPaper = null;
    PMatrix3D  projectorPaper = null;
    PMatrix3D  kinectPaper = null;

    public CalibrationSnapshot(PMatrix3D cameraPaperCalibration,
                               PMatrix3D  projectorPaperCalibration,
                               PMatrix3D  kinectPaperCalibration){
        cameraPaper = cameraPaperCalibration.get();
        projectorPaper = projectorPaperCalibration.get();
        if(kinectPaperCalibration != null)
            kinectPaper = kinectPaperCalibration.get();
    }


}
