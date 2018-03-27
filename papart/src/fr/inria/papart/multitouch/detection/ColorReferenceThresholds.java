/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.procam.Papart;
import static fr.inria.papart.utils.MathUtils.absd;
import static fr.inria.papart.utils.MathUtils.constrain;
import java.awt.Color;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.split;
import processing.core.PGraphics;
import tech.lity.rea.colorconverter.ColorConverter;

/**
 *
 * @author jiii
 */
public class ColorReferenceThresholds {

    private static final ColorConverter converter = new ColorConverter();

    public float brightness, saturation;
    public float hue;
    public float redThreshold, blueThreshold, greenThreshold;
    public float LThreshold, AThreshold, BThreshold;
    public float averageL, averageA, averageB;
    public int referenceColor, erosion;
    public int id;

    public ColorReferenceThresholds(){
        
    }
    public ColorReferenceThresholds(int id){
        this.id = id;
    }
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

    private int red(int v) {
        return (v >> 16) & 0xFF;
    }

    private int green(int v) {
        return (v >> 8) & 0xFF;
    }

    private int blue(int v) {
        return v & 0xFF;
    }
    int cacheHsbKey = 0;
    float cacheHsbValue[] = new float[3];

    private float hue(int v) {
        if (v != cacheHsbKey) {
            Color.RGBtoHSB((v >> 16) & 0xff, (v >> 8) & 0xff,
                    v & 0xff, cacheHsbValue);
            cacheHsbKey = v;
        }
        return cacheHsbValue[0] * 255f;
    }

    private float saturation(int v) {
        if (v != cacheHsbKey) {
            Color.RGBtoHSB((v >> 16) & 0xff, (v >> 8) & 0xff,
                    v & 0xff, cacheHsbValue);
            cacheHsbKey = v;
        }
        return cacheHsbValue[1] * 255;
    }

    private float brightness(int v) {
        if (v != cacheHsbKey) {
            Color.RGBtoHSB((v >> 16) & 0xff, (v >> 8) & 0xff,
                    v & 0xff, cacheHsbValue);
            cacheHsbKey = v;
        }
        return cacheHsbValue[2] * 255;
    }

    private int color(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    public static String[] INVALID_COLOR = new String[]{""};

    public String[] createReference(int[] colorData) {

        double averageL = 0;
        double averageA = 0;
        double averageB = 0;

        float averageHue = 0;
        float averageSat = 0;
        float averageIntens = 0;

        float averageR = 0;
        float averageG = 0;
        float averageBlue = 0;

        double stdevL = 0;
        double stdevA = 0;
        double stdevB = 0;

        float stdevHue = 0;
        float stdevSat = 0;
        float stdevIntens = 0;

        float stdevR = 0;
        float stdevG = 0;
        float stdevBlue = 0;

        // todo: IF Red > 180, hue gets shifted ?
        for (int i = 0; i < colorData.length; i++) {
            int c = colorData[i];

            averageHue += this.hue(c);
            averageSat += this.saturation(c);
            averageIntens += this.brightness(c);
            averageR += this.red(c);
            averageG += this.green(c);
            averageBlue += this.blue(c);

            double[] lab = converter.RGBtoLAB((int) this.red(c), (int) this.green(c), (int) this.blue(c));
            averageL += constrain(lab[0], 0.0, 100.0);
            averageA += constrain(lab[1], -128, 128);
            averageB += constrain(lab[2], -128, 128);
        }

        averageHue /= colorData.length;
        averageSat /= colorData.length;
        averageIntens /= colorData.length;
        averageR /= colorData.length;
        averageG /= colorData.length;
        averageBlue /= colorData.length;

        averageL /= colorData.length;
        averageA /= colorData.length;
        averageB /= colorData.length;

        // potentially problematic hue for red
        // Solution, we shift it by 255 for low values.
        if (averageR > 180) {
            averageHue = 0;
            for (int i = 0; i < colorData.length; i++) {
                int c = colorData[i];

                float h = this.hue(c);
                if (h < 30) {
                    h = h + 255;
                }
                averageHue += h;
            }
            averageHue /= colorData.length;
//                if (averageHue > 255) {
//                    averageHue = averageHue - 255;
//                }
        }

//            int averageCol = color(averageR, averageG, averageBlue);
        int[] averageColTmp = converter.LABtoRGB(averageL, averageA, averageB);

        int averageCol = color(
                (int) constrain(averageColTmp[0], 0, 255),
                (int) constrain(averageColTmp[1], 0, 255),
                (int) constrain(averageColTmp[2], 0, 255));

//            color(averageR, averageG, averageBlue);
        for (int i = 0; i < colorData.length; i++) {
            int c = colorData[i];

            // high red intensity with a redish rue.
            if (averageR > 180 && this.hue(c) < 30) {
                stdevHue += abs(this.hue(c) + 255 - averageHue);
            } else {
                stdevHue += abs(this.hue(c) - averageHue);
            }
            stdevSat += abs(this.saturation(c) - averageSat);
            stdevIntens += abs(this.brightness(c) - averageIntens);
            stdevR += abs(this.red(c) - averageR);
            stdevG += abs(this.green(c) - averageG);
            stdevBlue += abs(this.blue(c) - averageBlue);

            double[] lab = converter.RGBtoLAB((int) this.red(c), (int) this.green(c), (int) this.blue(c));
            stdevL += absd(constrain(lab[0], 0.0, 100.0) - averageL);
            stdevA += absd(constrain(lab[1], -128, 128) - averageA);
            stdevB += absd(constrain(lab[2], -128, 128) - averageB);
        }

        stdevHue /= colorData.length;
        stdevSat /= colorData.length;
        stdevIntens /= colorData.length;
        stdevR /= colorData.length;
        stdevG /= colorData.length;
        stdevBlue /= colorData.length;
        stdevL /= colorData.length;
        stdevA /= colorData.length;
        stdevB /= colorData.length;

        // Check the stdev... when too high the value is not stored.
        if (stdevHue > 40 || stdevSat > 40 || stdevIntens > 50) {
//            System.out.println("Could not determine color");
            return INVALID_COLOR;
        }

        // Good dev, make it larger
        if (stdevHue < 3) {
            stdevHue = 4;
        }
        // Good dev, make it larger
        if (stdevSat < 5) {
            stdevSat = 10;
        }
        // Good dev, make it larger
        if (stdevIntens < 5) {
            stdevIntens = 10;
        }

        String words = "hue:" + Float.toString(stdevHue * 3) + " "
                + "sat:" + Float.toString(stdevSat * 3) + " "
                + "intens:" + Float.toString(stdevIntens * 3) + " "
                + "erosion:" + Integer.toString(1) + " "
                + "red:" + Float.toString(stdevR * 3) + " "
                + "blue:" + Float.toString(stdevBlue * 3) + " "
                + "green:" + Float.toString(stdevG * 3) + " "
                + "l:" + Double.toString(stdevL) + " "
                + "A:" + Double.toString(stdevA) + " "
                + "B:" + Double.toString(stdevB) + " "
                + "valL:" + Double.toString(averageL) + " "
                + "valA:" + Double.toString(averageA) + " "
                + "valB:" + Double.toString(averageB) + " ";

        words = words + "id:" + Integer.toString(id) + " ";
        words = words + "col:" + Integer.toString(averageCol);

        String[] list = split(words, ' ');
        return list;
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
