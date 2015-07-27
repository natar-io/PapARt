/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.panel;

import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author jiii
 */
public class PGraphicsOpenGLExtension extends PGraphicsOpenGL {

    public static PGraphicsOpenGL mainContext;

    public PGraphicsOpenGLExtension() {
        super();
        this.pgl = mainContext.pgl;

//        inGeo = newInGeometry(this, IMMEDIATE); 
//        tessGeo = newTessGeometry(this, IMMEDIATE);
//        texCache = newTexCache(this);
//
//        initialized = false;
    }

}
