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

/**
 *
 * @author jiii
 */
public class Shape extends PreziObject {

    float w, h;
    String shapeType;

    public Shape(Element elem, String type) {

        shapeType = type;
        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node nNode = children.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nNode;
                
                if (e.getTagName().equalsIgnoreCase("size")) {
                    NodeList nl = e.getChildNodes();

                    for (int j = 0; j < nl.getLength(); j++) {
                        Node nNode2 = nl.item(j);
                        if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
                            
                            Element e2 = (Element) nNode2;
                            
                            if (e2.getTagName().equalsIgnoreCase("w")) {
                                w = Float.parseFloat(e2.getChildNodes().item(0).getNodeValue());
                            }
                            if (e2.getTagName().equalsIgnoreCase("h")) {
                                h = Float.parseFloat(e2.getChildNodes().item(0).getNodeValue());
                            }
                        }
                    }

                }
            }
        }

        System.out.println("Shape " + shapeType + " " + w + " " + h);
    }

    @Override
    public void drawSelf(PGraphics graphics) {
    }
}
