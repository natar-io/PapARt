/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.procam.Papart;
import static processing.core.PApplet.abs;
import processing.core.PGraphics;

/**
 *
 * @author jiii
 */
public class ColorReferenceThresholds {

    public float brightness, saturation;
    public float hue;
    public float redThreshold, blueThreshold, greenThreshold;
    public float LThreshold, AThreshold, BThreshold;
    public float averageL, averageA, averageB;
    public int referenceColor, erosion;

    /**
     * Color distance on the HSB scale. The incomingPix is compared with the
     * baseline. The method returns true if each channel validates the condition
     * for the given threshold.
     *
     * @param g
     * @return
     */
    public boolean colorFinderHSB(PGraphics g, int incomingPix) {
        float h1 = g.hue(referenceColor);
        float h2 = g.hue(incomingPix);

        return abs(h1 - h2) < hue
                && // Avoid desaturated pixels
                abs(g.saturation(incomingPix) - g.saturation(referenceColor)) < saturation
                && // avoid pixels not bright enough
                abs(g.brightness(incomingPix) - g.brightness(referenceColor)) < brightness;
    }

    public void loadParameter(String data) {
        try {
            String[] pair = data.split(":");
            if (pair[0].startsWith("hue")) {
                hue = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("sat")) {
                saturation = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("intens")) {
                this.brightness = Float.parseFloat(pair[1]);
            }

            if (pair[0].startsWith("erosion")) {
                this.erosion = Integer.parseInt(pair[1]);
            }
            if (pair[0].startsWith("col")) {
                this.referenceColor = Integer.parseInt(pair[1]);
            }

            if (pair[0].startsWith("red")) {
                this.redThreshold = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("blue")) {
                this.blueThreshold = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("green")) {
                this.greenThreshold = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("l")) {
                this.LThreshold = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("A")) {
                this.AThreshold = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("B")) {
                this.BThreshold = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("valL")) {
                this.averageL = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("valA")) {
                this.averageA = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("valB")) {
                this.averageB = Float.parseFloat(pair[1]);
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
    }

    public static ColorReferenceThresholds[] loadDefaultThresholds(int numberOfRefs) {
        ColorReferenceThresholds[] references = new ColorReferenceThresholds[numberOfRefs];

        // Load all the colors. 
        for (int fileId = 0; fileId < numberOfRefs; fileId++) {
            String fileName = Papart.colorThresholds + fileId + ".txt";
            String[] list = Papart.getPapart().getApplet().loadStrings(fileName);

            references[fileId] = new ColorReferenceThresholds();

            for (String data : list) {
                references[fileId].loadParameter(data);
            }
        }
        return references;
    }

    public void setThresholds(float hue, float saturation, float brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public void setRedThreshold(float red) {
        this.redThreshold = red;
    }

    public void setBlueThreshold(float blue) {
        this.blueThreshold = blue;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getHue() {
        return hue;
    }

    public float getRedThreshold() {
        return redThreshold;
    }

    public int getErosion() {
        return erosion;
    }

    public void setErosion(int erosion) {
        this.erosion = erosion;
    }

    public int getReferenceColor() {
        return referenceColor;
    }

    public void setReferenceColor(int referenceColor) {
        this.referenceColor = referenceColor;
    }

    public float getLThreshold() {
        return LThreshold;
    }

    public void setLThreshold(float LThreshold) {
        this.LThreshold = LThreshold;
    }

    public float getAThreshold() {
        return AThreshold;
    }

    public void setAThreshold(float AThreshold) {
        this.AThreshold = AThreshold;
    }

    public float getBThreshold() {
        return BThreshold;
    }

    public void setBThreshold(float BThreshold) {
        this.BThreshold = BThreshold;
    }

}
