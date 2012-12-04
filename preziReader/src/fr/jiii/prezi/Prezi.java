/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.jiii.prezi;

import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

/**
 *
 * @author jiii
 */
public class Prezi {

    int version;
    HashMap<Integer, PreziObject> objects;
    // path 
    // style 
    // sync
    // config
    float x, y, width, height;
    
    public Prezi(Document doc) {

        objects = new HashMap<Integer, PreziObject>();
        NodeList zuiTable = doc.getElementsByTagName("zui-table");
        NodeList objects = zuiTable.item(0).getChildNodes();

        parseObjects(objects);

        System.out.println("Prezi loaded " + x + " " + y + " " + width + " " + height);
        
    }

    private void parseObjects(NodeList objects) {

        for (int i = 0; i < objects.getLength(); i++) {

            Node nNode = objects.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element e = (Element) nNode;
                if (e.getTagName().equalsIgnoreCase("object")) {
                    addObject(new PreziObject(e));
                }
                if (e.getTagName().equalsIgnoreCase("settings")) {
                    parseSettings(e);
                }

            }
        }

    }

    public void addObject(PreziObject pObject) {

        objects.put(pObject.getId(), pObject);

    }

    public void parseSettings(Element sett) {

        NodeList settings = sett.getChildNodes();

        for (int i = 0; i < settings.getLength(); i++) {
            Node nNode = settings.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element e = (Element) nNode;

                if (e.getTagName().equalsIgnoreCase("bounds")) {
                    x = Float.parseFloat(e.getAttribute("x"));
                    y = Float.parseFloat(e.getAttribute("y"));
                    width = Float.parseFloat(e.getAttribute("width"));
                    height = Float.parseFloat(e.getAttribute("height"));
                }

                // autoplay 

                // aspect ratio

            }
        }
    }
}
