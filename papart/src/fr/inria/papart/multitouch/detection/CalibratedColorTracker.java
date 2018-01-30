/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.utils.MathUtils;
import java.util.ArrayList;
import processing.core.PConstants;

/**
 * [experimental] Similar to the ColorTracker but for all the calibrated colors.
 *
 * @author Jérémy Laviole
 */
public class CalibratedColorTracker extends ColorTracker {

    int numberOfRefs = 5;
    private final ColorReferenceThresholds references[];

    public CalibratedColorTracker(PaperScreen paperScreen, float scale) {
        super(paperScreen, scale);

        references = new ColorReferenceThresholds[numberOfRefs];

        // Load all the colors. 
        for (int fileId = 0; fileId < numberOfRefs; fileId++) {
            String fileName = Papart.colorThresholds + fileId + ".txt";
            String[] list = Papart.getPapart().getApplet().loadStrings(fileName);

            references[fileId] = new ColorReferenceThresholds();

            for (String data : list) {
                references[fileId].loadParameter(data);
            }
        }
    }
    
    public int getReferenceColor(int id){
        return references[id].getReferenceColor();
    }

    @Override
    public ArrayList<TrackedElement> findColor(int time) {

        int currentImageTime = paperScreen.getCameraTracking().getTimeStamp();

        // once per image
        if (lastImageTime == currentImageTime) {
            // return the last known points. 
            return trackedElements;
        }
        lastImageTime = currentImageTime;

        // Get the image
        capturedImage = trackedView.getViewOf(paperScreen.getCameraTracking());
        capturedImage.loadPixels();

        // Reset the colorFoundArray
        touchDetectionColor.resetInputArray();

        // Default to RGB 255 for now, for color distances. 
        paperScreen.getGraphics().colorMode(PConstants.RGB, 255);

        // Tag each pixels
        for (int x = 0; x < capturedImage.width; x++) {
            for (int y = 0; y < capturedImage.height; y++) {
                int offset = x + y * capturedImage.width;
                int c = capturedImage.pixels[offset];

                // for each 
                for (byte id = 0; id < numberOfRefs; id++) {

                    reference = references[id];
                    
                    boolean good = MathUtils.colorFinderHSB(paperScreen.getGraphics(),
                                c, reference.referenceColor, reference.hue, reference.saturation, reference.brightness);
                  
                    // HSB only for now.
                    if (good) {
//                    if (references[id].colorFinderHSB(paperScreen.getGraphics(), c)) {
                        colorFoundArray[offset] = id;
                    }

                }
            }
        }

        int erosion = 1;
        // EROSION by color ?!
        ArrayList<TrackedElement> newElements
                = touchDetectionColor.compute(time, erosion, this.scale);

        TouchPointTracker.trackPoints(trackedElements, newElements, time);
//        for(TrackedElement te : trackedElements){
//            te.filter(time);
//        }

        return trackedElements;
    }
}
