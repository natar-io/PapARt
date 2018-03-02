/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.compositor;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import static java.lang.Thread.sleep;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jiii
 */
public class XAppRunner extends AppRunner {

    private final XDisplay targetDisplay;

    public XAppRunner(String program, XDisplay target) {
        super(program);
        this.targetDisplay = target;
    }
    public XAppRunner(String[] program, XDisplay target) {
        super(program);
        this.targetDisplay = target;
           
    }
    
    @Override
    protected void buildProcess() {
        super.buildProcess();

        if (this.targetDisplay != XDisplay.NO_DISPLAY) {
            env.put("DISPLAY", targetDisplay.name());
        }
    }
}
