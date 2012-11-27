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

/**
 *
 * @author jiii
 */
public class PreziImage implements Drawable {

    private float w, h;
    private String name;
    PImage image;

    public PreziImage(Element elem) {

        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node nNode = children.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element e = (Element) nNode;
                if (e.getTagName().equalsIgnoreCase("source")) {

                    this.w = Float.parseFloat(e.getAttribute("w"));
                    this.h = Float.parseFloat(e.getAttribute("h"));

                    name = e.getChildNodes().item(0).getNodeValue();
                }

//                if (e.getTagName().equalsIgnoreCase("ressource")) {
//                    y = Float.parseFloat(e.getChildNodes().item(0).getNodeValue());
//                }

            }
        }


        if (name.endsWith(".jpe")) {

            String path = PreziLoader.parent.sketchPath
                    + PreziLoader.dataFolder
                    + "/" + name;
            this.image = PreziLoader.parent.loadImage(path, "jpg");

        } else {

            String path = PreziLoader.parent.sketchPath
                    + PreziLoader.dataFolder
                    + "/" + name;
            this.image = PreziLoader.parent.loadImage(path);
        }

        System.out.println("Image loaded " + name);
    }

    @Override
    public void drawSelf(PGraphics graphics) {
        
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
