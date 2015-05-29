/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.multitouch.metaphors;

import fr.inria.papart.multitouch.OneEuroFilter;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchList;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.PaperTouchScreen;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author jiii
 */
public abstract class RSTTransform {

    protected PVector sceneTranslate;
    protected float sceneScale = 1.0f;
    protected float sceneRotateZ = 0f;
    protected float sceneRotateX = 0f;
    protected float sceneRotateY = 0f;

    protected static int TRANSLATE_X = 0;
    protected static int TRANSLATE_Y = 1;
    protected static int SCALE = 2;
    protected static int ROTATE_X = 3;
    protected static int ROTATE_Y = 4;
    protected static int ROTATE_Z = 5;
    protected static int NbFilters = 6;

    protected OneEuroFilter[] filters;
    public static float filterFreq = 30f;
    public static float filterCut = 0.2f;
    public static float filterBeta = 8.000f;

    public RSTTransform(PVector size) {
        this.sceneTranslate = size.get();
        this.sceneTranslate.mult(0.5f);
        try {
            filters = new OneEuroFilter[NbFilters];
            for (int i = 0; i < filters.length; i++) {
                filters[i] = new OneEuroFilter(filterFreq, filterCut, filterBeta);
            }
        } catch (Exception e) {
            System.out.println("OneEuro Exception. Pay now." + e);
        }

    }

    public void addRotation(float rot) {
        try {
            float rotationFiltered = (float) filters[ROTATE_Z].filter(rot);
            sceneRotateZ += rotationFiltered;
        } catch (Exception ex) {
            Logger.getLogger(RSTTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addRotationY(float rot) {
        try {
            float rotationFiltered = (float) filters[ROTATE_Y].filter(rot);
            sceneRotateY += rotationFiltered;
        } catch (Exception ex) {
            Logger.getLogger(RSTTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addTranslation(PVector translate) {
        try {
            float translationXFiltered = (float) filters[TRANSLATE_X].filter(translate.x);
            float translationYFiltered = (float) filters[TRANSLATE_Y].filter(translate.y);
            sceneTranslate.x += translationXFiltered;
            sceneTranslate.y += translationYFiltered;

        } catch (Exception ex) {
            Logger.getLogger(RSTTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void multScale(float sc) {
        try {
            float scaleFiltered = (float) filters[SCALE].filter(sc);
            sceneScale *= scaleFiltered;
        } catch (Exception ex) {
            Logger.getLogger(RSTTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public abstract void update(TouchList touchList, int currentTime);

    protected void emptyUpdate(){
          addRotation(0);
          addTranslation(new PVector());
          multScale(1);
    }
    
    protected PVector computeTranslate(Touch touch) {
        return touch.speed;
    }

    public void applyTransformationTo(PaperScreen paperScreen) {
        applyTransformationTo(paperScreen.getGraphics());
    }

    public void applyTransformationTo(PGraphicsOpenGL pgl) {
        applyTransformationTo(pgl.modelview);
    }

    public void applyTransformationTo(PMatrix3D matrix) {
        matrix.translate(sceneTranslate.x, sceneTranslate.y, sceneTranslate.z);
        matrix.rotateX(sceneRotateX);
        matrix.rotateY(sceneRotateY);
        matrix.rotateZ(sceneRotateZ);
        matrix.scale(sceneScale);
    }

    public String toString() {
        return "Translation " + sceneTranslate + " scale: " + sceneScale + " rotation, X, Y, Z, "
                + sceneRotateX + " " + sceneRotateY + " " + sceneRotateZ;
    }

}
