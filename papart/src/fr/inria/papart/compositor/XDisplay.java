/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.compositor;

import java.lang.ProcessBuilder.Redirect;
import java.util.Map;

/**
 *
 * @author jiii
 */
public class XDisplay extends AppRunner {

    public static final XDisplay NO_DISPLAY = new XDisplay(0, 0);
    public static int nextDisplayID = 99;

    private final int id;
    private final int width, height;
    private final int depth = 16;
    private final String xephyrName = "/usr/bin/Xephyr";
    private final String xvfbName = "/usr/bin/Xvfb";

    // For the NO_DISPLAY  constant.
    private XDisplay() {
        id = -1;
        width = 0;
        height = 0;
    }

    public XDisplay(int width, int height) {
        super();
        id = nextDisplayID--;
        this.width = width;
        this.height = height;
        System.out.println("Preparing XDisplay: " + id + " .");
    }

    // Xpra test further... set the resolution / screen ?
    @Override
    protected void buildProcess() {
        String[] cmd = new String[]{
            xvfbName, ":" + id,
            "-ac", "-screen", "0 ", width + "x" + height + "x" + depth
        };
//        String[] cmd = new String[]{
//           xephyrName, ":" + id,
//            "-ac", "-screen", width + "x" + height
//        };
//    String[] cmd = new String[]{
//            "xpra", "start", 
//              "--xvfb=Xvfb", 
//              ":" + id +  "-screen", "0 ", width + "x" + height + "x" + depth
//        };
    
// + VGLRUN C?ESTBON
//    XPRA_RANDR=0 xpra start-desktop \
//    --xvfb="Xvfb :99 -screen 0 800x600x16 +extension GLX +extension RANDR +extension RENDER" \
//    --start=xfce4-session

    
//    XPRA_RANDR=0 xpra start \
//    --xvfb="Xvfb :99 -screen 0 800x600x24+32 -fbdir /var/tmp" \
//    --sync-xvfb=50 --bind-tcp=127.0.0.1:10000 \
//    --start=libreoffice
    

        builder = new ProcessBuilder(cmd);

        // DEBUG
        builder.redirectOutput(Redirect.INHERIT);
        builder.redirectError(Redirect.INHERIT);

        Map<String, String> env = builder.environment();
        env.put("PATH", "/bin:/usr/bin");
    }
    
    @Override
    public String toString() {
        return ":" + id;
    }

    public String name() {
        return ":" + id;
    }

    public int getDisplayId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

}
