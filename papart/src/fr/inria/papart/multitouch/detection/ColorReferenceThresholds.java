/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import static processing.core.PApplet.abs;
import processing.core.PGraphics;

/**
 *
 * @author jiii
 */
public class ColorReferenceThresholds {

    public float brightness, saturation;
    public float hue;
    public float redThreshold, blueThreshold;
    public int referenceColor, erosion;

    
    /**
     * Color distance on the HSB scale. The incomingPix is compared with the
     * baseline. The method returns true if each channel validates the condition
     * for the given threshold.
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
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
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

}
