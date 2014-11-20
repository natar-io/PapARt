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
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class ModeTest {

    public ModeTest() {
    }

    /**
     * Test of clear method, of class Mode.
     */
    @Test
    public void testCreation() {

        assertTrue(Mode.size() == 0);
        Mode.add("test1");

        assertTrue(Mode.size() == 1);
        Mode.add("test2");

        assertTrue(Mode.size() == 2);
        Mode.set("test1");

        Mode.set("test1");
        assertTrue(Mode.is("test1"));
        assertFalse(Mode.is("test2"));

        Mode.set("test2");
        assertTrue(Mode.is("test2"));
        assertFalse(Mode.is("test1"));

        Mode.clear();
        assertTrue(Mode.size() == 0);
    }

}
