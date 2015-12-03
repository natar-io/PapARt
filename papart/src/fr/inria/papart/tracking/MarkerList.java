/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.tracking;

import java.util.HashMap;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class MarkerList extends HashMap<Integer, MarkerSvg>{
    
    private float sheetHeight = 0;

    public float getSheetHeight() {
        return sheetHeight;
    }

    public void setSheetHeight(float sheetHeight) {
        this.sheetHeight = sheetHeight;
    }
    
    
}
