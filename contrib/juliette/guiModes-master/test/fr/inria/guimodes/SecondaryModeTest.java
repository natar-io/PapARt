/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.guimodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class SecondaryModeTest {

    public SecondaryModeTest() {
    }

    /**
     * Test of clear method, of class Mode.
     */
    @Test
    public void testCreation() {

        SecondaryMode mode = new SecondaryMode();
        
        
        assertTrue(mode.size() == 0);
        mode.add("test1");

        assertTrue(mode.size() == 1);
        mode.add("test2");

        assertTrue(mode.size() == 2);
        mode.set("test1");

        mode.set("test1");
        assertTrue(mode.is("test1"));
        assertFalse(mode.is("test2"));

        mode.set("test2");
        assertTrue(mode.is("test2"));
        assertFalse(mode.is("test1"));

        mode.clear();
        assertTrue(mode.size() == 0);
    }

  
}
