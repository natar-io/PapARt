/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.compositor;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;
import static processing.core.PApplet.println;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;

public class AppRunnerDebug extends Thread {

    public boolean ready = false;

    String displayId = "1";
    String xvfb = "xvfb-run";
    String program = "/usr/bin/firefox";
    String windowManager = "openbox";

    public AppRunnerDebug() {
    }
    // -n id display
    // -w delay
    //	    String args = "-s '-ac -screen 0 800x600x16' -n 1 -w 2";

    class TestScript {
        int iExitValue;
        String sCommandString;

        public void runScript(String command) {
            sCommandString = command;
            CommandLine oCmdLine = CommandLine.parse(sCommandString);
            DefaultExecutor oDefaultExecutor = new DefaultExecutor();
            oDefaultExecutor.setExitValue(0);
            try {
                iExitValue = oDefaultExecutor.execute(oCmdLine);
            } catch (ExecuteException e) {
                System.err.println("Execution failed.");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("permission denied.");
                e.printStackTrace();
            }
        }
    }

    void xvfb() throws IOException {

        //	String args = "-s \"-ac -screen 0 "+ resX + "x" + resY + "x16 \" -n "+displayId+" -w 2";
//        String[] cmd = new String[]{
//            "/bin/sh", "/usr/bin/xvfb-run",
//            "-e", "/dev/stdout" ,
//            "-s", "-ac -screen 0 800x600x16",
//            "-n", Integer.toString(displayId),
//            "-w", "5",
//            "firefox"
//        };
        String[] cmd = new String[]{
            "/usr/bin/Xvfb", ":1",
            "-ac","-screen", "0", "800x600x16"
        };
        
//        String[] cmd = new String[]{ 
//            "Xephyr", "-ac", "-br", "-noreset", "-screen",  "1024x768",  ":" + displayId 
//        };
        
        println("Start xvfb");
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();

//        pb.redirectOutput(Redirect.INHERIT);
//        pb.redirectError(Redirect.INHERIT);
        Map<String, String> env = pb.environment();
        env.put("PATH", "/bin:/usr/bin");
        Process p = pb.start();
        println("sleep 6sec");
        try {
            Thread.sleep(6000);
            if (!p.isAlive()) {
                println("Exit value: " + p.exitValue());
            }
            println("Alive ? " + p.isAlive());
        } catch (InterruptedException e) {
        }

//        TestScript testScript = new TestScript();
//        testScript.runScript("xvfb-run -e /dev/stdout -s \"-ac -screen 0 800x600x16\" -n 1 -w 5 firefox");
    }

    void wm() throws IOException {
        println("Start wm: " + windowManager);
        //	Process p2 = Runtime.getRuntime().exec(windowManager, envp);

        String[] cmd = new String[]{windowManager};

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectOutput(Redirect.INHERIT);
        pb.redirectError(Redirect.INHERIT);

        Map<String, String> env = pb.environment();
        env.put("PATH", "/bin:/usr/bin");
        env.put("DISPLAY", ":" + displayId);
        Process p = pb.start();

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
        }

    }

    void event() throws IOException {
        println("Start eventManager.");

        String eventManager = "/usr/bin/processing-java --sketch=/home/realitytech/gordon/repos/papart-calibration/exec/redisKeyReader/ --output=/home/realitytech/gordon/repos/papart-calibration/exec/redisKeyReader/build --force --run";

        String[] cmd = new String[]{
            "/usr/bin/processing-java",
            "--sketch=/home/realitytech/gordon/repos/papart-calibration/exec/redisKeyReader/",
            "--output=/home/realitytech/gordon/repos/papart-calibration/exec/redisKeyReader/build",
            "--force",
            "--run"
        };

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectOutput(Redirect.INHERIT);
        pb.redirectError(Redirect.INHERIT);

        Map<String, String> env = pb.environment();
        env.put("PATH", "/bin:/usr/bin");
        env.put("DISPLAY", ":" + displayId);
        Process p = pb.start();
    }

    public void run() {
        try {
            xvfb();
            wm();
            event();
            ready = true;
        } catch (Exception e) {
            println("Error starting process");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AppRunnerDebug runner = new AppRunnerDebug();
        runner.start();
        while (true) {
            Thread.sleep(1000);
        }
    }
}
