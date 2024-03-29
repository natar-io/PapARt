/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import static fr.inria.papart.depthcam.analysis.DepthAnalysisImpl.INVALID_DEPTH;
import java.nio.ShortBuffer;
import org.bytedeco.opencv.opencv_core.*;

/**
 *
 * @author realitytech
 */
public class OpenNIDepth implements DepthAnalysis.DepthComputation {

    private ShortBuffer frameData;
    float[] histogram;

    public OpenNIDepth() {
    }

    @Override
    public float findDepth(int offset) {
        int depth = (int) (frameData.get(offset) & 0xFFFF);
        if (depth == 0) {
            return INVALID_DEPTH;
        }
        return depth;
    }

    @Override
    public void updateDepth(IplImage depthImage) {
        frameData = depthImage.getShortBuffer();
    }
}
