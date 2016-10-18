/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.guimodes;

import java.util.HashMap;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class SecondaryMode {

    private  String currentMode;
    private  final HashMap<String, Integer> modes = new HashMap<String, Integer>();
    private  int changeTime;
    private  PApplet parent = null;
    protected String name;
    private  int NB_MODES = 0;
//    private  int minimumModeDuration = 200;
//    private  boolean timeFiltering = false;

    public SecondaryMode(){
    }
    
    public void init(PApplet papplet) {
        parent = papplet;
    }

    public int asInt() {
        return modes.get(currentMode);
    }

    public  void clear() {
        modes.clear();
    }

    public void add(String modeName) {
        modes.put(modeName, NB_MODES++);
    }

    public void add(String modeName, int id) {
        NB_MODES++;
        modes.put(modeName, id);
    }

    public boolean is(String modeName) {
        return modeName.equals(currentMode);
    }

    public  void set(String modeName) {
        Integer modeId = modes.get(modeName);
        assert (modeId != null);
        currentMode = modeName;
    }
    
    public String getCurrent() {
        return currentMode;
    }

    public  int size() {
        return modes.size();
    }

    public  boolean contains(String key) {
        return modes.containsKey(key);
    }

    public  boolean contains(SecondaryMode value) {
        return modes.containsValue(value);
    }
    
    public String toString() {
        return modes.toString();
    }

    public  int lastChangeTime() {
        if (parent == null) {
            System.err.println("Use the init() method to use timer capabilites.");
            return 0;
        }
        return changeTime;
    }
}
