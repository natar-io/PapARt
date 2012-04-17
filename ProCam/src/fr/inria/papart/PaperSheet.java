/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

/**
 *
 * @author jeremylaviole
 */
public class PaperSheet {
    
    private String fileName, name;
    private int width;
    private int height;
    
    public PaperSheet(String fileName, String name, int width, int height){
        this.fileName = fileName;
        this.width = width;
        this.height = height;
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }
    
    public String getName(){
        return name;
    }
}
