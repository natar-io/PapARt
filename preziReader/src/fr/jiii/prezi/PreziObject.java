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
    
    private int id;
    private int parentId;
    private String label;
    private float x, y;
    private float r, s;
    private String className;
    private String type;
    private TextField textField = null;
    private PreziImage image = null;
    private Shape shape = null;

    public PreziObject(Element elem) {

//        t id="2" type="button" x="603.4" y="711.1" r="0" s="4.0081787109375" class="bracket">
//        this.id = Integer.parseInt(elem.getAttribute("id"));
        parseId(elem.getAttribute("id"));
        this.label = elem.getAttribute("label");
        this.x = Float.parseFloat(elem.getAttribute("x"));
        this.y = Float.parseFloat(elem.getAttribute("y"));
        this.r = Float.parseFloat(elem.getAttribute("r"));
        this.s = Float.parseFloat(elem.getAttribute("s"));
        this.type = elem.getAttribute("type");
        this.className = elem.getAttribute("class");

        System.out.println("Object loaded: " + id + " " + x + " " + y);


        if (type != null && type.equalsIgnoreCase("image")) {

            image = new PreziImage(elem);
//      <source w="599" h="450">88496915.jpe<url>88496915.jpe</url>
//      </source>       
        }




        ///////////////// TextField /////////////////
        
        // check if it is 
        // textfield, .... 
        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node nNode = children.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element e = (Element) nNode;
                if (e.getTagName().equalsIgnoreCase("textfield")) {
                    textField = new TextField(e);
                }

                if (e.getTagName().equalsIgnoreCase("type")) {
                    
                    String shapeType = e.getChildNodes().item(0).getNodeValue();
                    
                    shape = new Shape(elem, shapeType);
                }

            }
        }

    }

    public void parseId(String id) {

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
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
