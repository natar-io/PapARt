/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.compositor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jiii
 */
public class AppRunnerTest {

    public static void main(String[] args) {

        // XServer 
        XDisplay display = new XDisplay(800, 600);
        display.start();

        // sleep 1sec
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(AppRunnerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        // sleep ?
        // Window manager 
        XAppRunner wm = new XAppRunner("openbox", display);
        wm.start();

        // Event manager
        String[] cmd = new String[]{
            "/usr/bin/processing-java",
            "--sketch=/home/realitytech/gordon/repos/papart-calibration/exec/redisKeyReader/",
            "--output=/home/realitytech/gordon/repos/papart-calibration/exec/redisKeyReader/build",
            "--force",
            "--run"
        };
        XAppRunner event = new XAppRunner(cmd, display);
        event.start();
        
        
        XAppRunner firefox = new XAppRunner("firefox", display);
        firefox.start();

        
        // --pid 1234
        //  DISPLAY=:1 xdotool search --name "Mozilla Firefox" windowsize 500 500

        // DISPLAY=:1 xdotool search --pid 21298 --name "Mozilla Firefox" windowsize 500 5

        // ps -p 23798 -o comm=
       // firefox

        
        
        // sleep 1sec
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(AppRunnerTest.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
