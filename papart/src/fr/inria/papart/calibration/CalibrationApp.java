package fr.inria.papart.calibration;

import fr.inria.papart.apps.*;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;

public class CalibrationApp extends PaperScreen {

    @Override
    public void settings() {
        setDrawingSize(297, 210);
        loadMarkerBoard(Papart.markerFolder + Papart.calibrationFileName, 297, 210);
        setDrawAroundPaper();
    }

    @Override
    public void setup() {
        // No filtering
        setDrawingFilter(0);
    }

    @Override
    public void drawAroundPaper() {
    }
}
