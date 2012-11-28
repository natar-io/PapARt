/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jiii
 */
package fr.jiii.prezi;

import processing.core.PApplet;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class PreziLoader {

    public static PApplet parent;
    static final String fileName = "/content.xml";
    static final String dataFolder = "/prezi/repo";
    
    public static Prezi loadPrezi(PApplet pa, String file) {

        parent = pa;
        
        try {

            File fXmlFile = new File(file+"/content.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            
            Prezi prezi = new Prezi(doc);
            return prezi;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
