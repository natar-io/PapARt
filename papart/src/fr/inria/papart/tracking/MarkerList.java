/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.tracking;

import java.util.HashMap;
import processing.core.PVector;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class MarkerList extends HashMap<Integer, MarkerSvg> {

    private final PVector sheetSize = new PVector();

    public float getSheetHeight() {
        return sheetSize.y;
    }

    public void setSheetSize(PVector size) {
        setSheetSize(size.x, size.y);
    }

    public void setSheetSize(float x, float y) {
        this.sheetSize.x = x;
        this.sheetSize.y = y;
    }

}
