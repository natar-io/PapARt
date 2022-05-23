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

import java.util.HashMap;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;

/**
 *
 * @author Jérémy Laviole - laviole@rea.lity.tech
 */
public class MarkerList extends HashMap<Integer, MarkerSvg> {

    /**
     * Size in mm.
     */
    private final PVector sheetSize = new PVector();

    public float getSheetHeight() {
        return sheetSize.y;
    }

    /**
     * Set sheet size in mm.
     *
     * @param size
     */
    public void setSheetSize(PVector size) {
        setSheetSize(size.x, size.y);
    }

    /**
     * Set sheet size in mm.
     *
     * @param x
     * @param y
     */
    public void setSheetSize(float x, float y) {
        this.sheetSize.x = x;
        this.sheetSize.y = y;
    }


    public JSONArray toJSON() {
      JSONArray output = new JSONArray();
      for (Integer key : this.keySet()) {

          JSONObject element = new JSONObject();
          element.setInt("id", key);
          element.setJSONObject("marker", this.get(key).toJSON());
          output.append(element);
      }
      return output;
    }

    public static MarkerList createFromJSON(JSONArray input) {
      MarkerList list = new MarkerList();
      for(int i= 0; i < input.size(); i++){
          
          JSONObject arr = input.getJSONObject(i);
          int id = arr.getInt("id");
          JSONObject markerJson = arr.getJSONObject("marker");
          MarkerSvg marker = MarkerSvg.createFromJSON(markerJson);
          
          list.put(id, marker);
      }
      return list;
    }

}
