/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.tracking;

import fr.inria.papart.tracking.MarkerBoard.MarkerType;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class MarkerBoardFactory {
        

    // Todo: error Handling...
    public static MarkerBoard create(String fileName, float width, float height){

        MarkerType type = getType(fileName);
        
        if (type == MarkerType.ARTOOLKITPLUS) {
            return new MarkerBoardARToolKitPlus(fileName, width, height);
        }
        if (type == MarkerType.JAVACV_FINDER) {
            return new MarkerBoardJavaCV(fileName, width, height);
        }
        
        if (type == MarkerType.SVG) {
            return new MarkerBoardSvg(fileName, width, height);
        }
        
        return MarkerBoardInvalid.board;
    }
    
    private static MarkerType getType(String name) {
        if (name.endsWith("cfg")) {
            return MarkerType.ARTOOLKITPLUS;
        }
        if (name.endsWith("svg")) {
            return MarkerType.SVG;
        }
        if (name.endsWith("png") || name.endsWith("jpg") || name.endsWith("bmp")) {
            return MarkerType.JAVACV_FINDER;
        }
        return MarkerType.INVALID;
    }

    
}
