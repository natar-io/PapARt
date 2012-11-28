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
import processing.core.PFont;
import processing.core.PGraphics;

/**
 *
 * @author jiii
 */
public class Prezi implements Drawable {

    PFont font1, font2, font3;
    int version;
    HashMap<Integer, PreziObject> objects;
    // path 
    // style 
    // sync
    // config
    float x, y, width, height;
    
    public Prezi(Document doc)  {

        objects = new HashMap<Integer, PreziObject>();
        NodeList zuiTable = doc.getElementsByTagName("zui-table");
        NodeList objects = zuiTable.item(0).getChildNodes();

        parseObjects(objects);

        System.out.println("Prezi loaded " + x + " " + y + " " + width + " " + height);
        
    }

    public void initDraw(PGraphics graphics){
        
        font1 = PreziLoader.parent.loadFont("AndaleMono-48.vlw");
        font2 = PreziLoader.parent.loadFont("AndaleMono-48.vlw");
        font3 = PreziLoader.parent.loadFont("AndaleMono-48.vlw");

        graphics.textFont(font1);
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

    @Override
    public void drawSelf(PGraphics graphics) {
        
        for(PreziObject o : objects.values()){
            o.drawSelf(graphics);
        }
        
    }
}
