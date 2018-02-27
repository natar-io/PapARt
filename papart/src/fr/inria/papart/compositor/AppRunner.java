/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.compositor;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;

/**
 *
 * @author jiii
 */
public class AppRunner extends Thread {

    protected boolean useCommandArguments = false;
    protected final String program;
    protected final String[] programCmd;

    protected ProcessBuilder builder;
    protected Process process;
    protected boolean alive = true;
    protected int PID;
    protected Map<String, String> env;

    protected AppRunner() {
        program = "";
        programCmd = new String[0];
    }

    public AppRunner(String program) {
        this.program = program;
        programCmd = new String[0];
    }

    public AppRunner(String[] programCmd) {
        program = "";
        this.programCmd = programCmd;
        useCommandArguments = true;
    }

    public void autoExit(PApplet applet) {
        applet.registerMethod("dispose", this);
    }

    public void dispose() {
        alive = false;
        System.out.println("Closing: " + this.program + ".");
        process.destroy();
        System.out.println(this.program + " closed.");

        // TODO: timeouts etc... ?
    }

    @Override
    public void run() {
        buildProcess();

        try {
            // DEBUG
            System.out.println("Starting process: " + program);

            process = builder.start();
            PID = (int) getPidOfProcess(process);

            System.out.println("PID of " + program + " : " + PID + ".");

            while (alive) {
                sleep(1000);
            }
        } catch (IOException ex) {
            Logger.getLogger(XDisplay.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(XDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void buildProcess() {
        if (useCommandArguments) {
            builder = new ProcessBuilder(programCmd);
        } else {
            builder = new ProcessBuilder(program);
        }

        // DEBUG
        builder.redirectOutput(Redirect.INHERIT);
        builder.redirectError(Redirect.INHERIT);

        env = builder.environment();
        env.put("PATH", "/bin:/usr/bin");

    }

    public void close() {
        alive = false;
    }

    public static synchronized long getPidOfProcess(Process p) {
        long pid = -1;

        try {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getLong(p);
                f.setAccessible(false);
            }
        } catch (Exception e) {
            pid = -1;
        }
        return pid;
    }

    public String getProgram() {
        return program;
    }

    public ProcessBuilder getBuilder() {
        return builder;
    }

    public Process getProcess() {
        return process;
    }
}
