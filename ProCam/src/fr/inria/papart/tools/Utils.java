/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.tools;

import java.io.File;
import java.net.URL;
import toxi.geom.Matrix4x4;

/**
 *
 * @author jiii
 */
public class Utils {

    static public String getSketchbookFolder() {
        URL main = Matrix4x4.class.getResource("Matrix4x4.class");
        String tmp = main.getPath();

        tmp = tmp.substring(0, tmp.indexOf('!'));
        tmp = tmp.replace("file:", "");
//        tmp = tmp.replace("file:/", "");  TODO: OS check ?

        File f = new File(tmp);
        if (!f.exists()) {
            System.err.println("Error in loading the Sketchbook folder.");
        }
        f = f.getParentFile().getParentFile().getParentFile().getParentFile(); // go to where the config is stored

        return f.getAbsolutePath();
    }
    
    static public String getLibraryFolder(String libname){
        return getSketchbookFolder() + "/libraries/"+libname ;
    }
    
    static public String getPapartFolder(){
        return getSketchbookFolder() + "/libraries/ProCam";
    }
}
