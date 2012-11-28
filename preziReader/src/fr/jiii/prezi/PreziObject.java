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
import processing.core.PGraphics;
import processing.core.PImage;
import sun.security.krb5.internal.APOptions;

/**
 *
 * @author jiii
 */
public class PreziObject implements Drawable {

    // TODO: create types...
    protected int id;
    protected int parentId;
    protected String label;
    protected float x, y;
    protected float r, s;
    protected String className;
    protected String type;

    static public PreziObject loadObject(Element elem) {

        String type = elem.getAttribute("type");
        String className = elem.getAttribute("class");

        if (type != null && type.equalsIgnoreCase("image")) {
            return new PreziImage(elem);
        }

        ///////////////// TextField /////////////////

        // check if it is 
        // textfield,  type ?
        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node nNode = children.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element e = (Element) nNode;
                if (e.getTagName().equalsIgnoreCase("textfield")) {
                    return new TextField(e);
                }

                if (e.getTagName().equalsIgnoreCase("type")) {

                    String shapeType = e.getChildNodes().item(0).getNodeValue();

                    return new Shape(elem, shapeType);
                }

            }
        }
        
        return null;
    }

    public PreziObject(){}

    public PreziObject(Element elem) {
        loadParameters(elem);

    }
    
    protected void loadParameters(Element elem) {
        parseId(elem.getAttribute("id"));
        this.label = elem.getAttribute("label");
        this.x = Float.parseFloat(elem.getAttribute("x"));
        this.y = Float.parseFloat(elem.getAttribute("y"));
        this.r = Float.parseFloat(elem.getAttribute("r"));
        this.s = Float.parseFloat(elem.getAttribute("s"));
        this.type = elem.getAttribute("type");
        this.className = elem.getAttribute("class");
    }
    
    protected void parseId(String id) {

        String[] parts = id.split("_");
        if (parts.length == 2) {
            this.parentId = Integer.parseInt(parts[0]);
            this.id = Integer.parseInt(parts[1]);
        } else {
            this.id = Integer.parseInt(parts[0]);
        }

    }

    public int getId() {
        return id;
    }

    @Override
    public void drawSelf(PGraphics graphics) {
        
        // 
        graphics.rect(x, y, 200, 200);
        
        
    }
}
