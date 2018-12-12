/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.tracking;

import tech.lity.rea.tracking.MarkerBoard.MarkerType;
import java.util.ArrayList;
import java.util.HashMap;
import tech.lity.rea.tracking.MarkerBoard;
import tech.lity.rea.tracking.MarkerBoardInvalid;
import tech.lity.rea.tracking.MarkerBoardSvg;

/**
 *
 * @author Jérémy Laviole - laviole@rea.lity.tech
 */
public class MarkerBoardFactory {

    private static final HashMap<String, MarkerBoard> allBoards = new HashMap<>();
public static final int DEFAULT_WIDTH = 100, DEFAULT_HEIGHT = 100;
    
    public static MarkerBoard create(String fileName) {
            return MarkerBoardFactory.create(fileName, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    // Todo: error Handling...
    public static MarkerBoard create(String fileName, float width, float height) {

        if (allBoards.containsKey(fileName)) {
            return allBoards.get(fileName);
        }

        MarkerBoard output = MarkerBoardInvalid.board;

        MarkerType type = getType(fileName);
        try {
//            if (type == MarkerType.ARTOOLKITPLUS) {
//                output = new MarkerBoardARToolKitPlus(fileName, width, height);
//            }
            if (type == MarkerType.JAVACV_FINDER) {
                output = new MarkerBoardJavaCV(fileName, width, height);
            }

            if (type == MarkerType.SVG) {
                output = new MarkerBoardSvg(fileName, width, height);
            }
            if(output == MarkerBoardInvalid.board){
                throw new Exception("Impossible to load the markerboard :" + fileName);
            }
            allBoards.put(fileName, output);
        } catch (Exception e) {
            System.err.println("Error loading the markerboard: " + e);
        }

        return output;
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
