package fr.inria.papart.apps;

import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import processing.core.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Jeremy Laviole, <laviole@rea.lity.tech>
 */
@SuppressWarnings("serial")
public class CameraTestLettuce extends PApplet {

    int[] incomingPixels;

    @Override
    public void settings() {
        // the application will be rendered in full screen, and using a 3Dengine.
        size(640, 480, P3D);
    }

    @Override
    public void setup() {
        connectRedist();
        noLoop();
        incomingPixels = new int[640 * 480];
//        byte[] id = defaultName.getBytes();
//        // Subscribe tests
//        MyListener l = new MyListener();
////        byte[] id = defaultName.getBytes();
//        redis.subscribe(l, id);
//        new RedisThread().start();
    }

    void connectRedist() {
        RedisClient client = RedisClient.create("redis://localhost");
//        StatefulRedisConnection<String, String> connection = client.connect();
//        RedisStringCommands sync = connection.sync();
//        String value = (String) sync.get("key");
        ByteArrayCodec codec = io.lettuce.core.codec.ByteArrayCodec.INSTANCE;

        StatefulRedisPubSubConnection<byte[], byte[]> pubsub = client.connectPubSub(codec);

        pubsub.addListener(new RedisPubSubListener<byte[], byte[]>() {
            @Override
            public void message(byte[] k, byte[] v) {
                System.out.println("Message received: " + v.length);
                setImage(v);
                redraw();
            }

            @Override
            public void message(byte[] k, byte[] k1, byte[] v) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void subscribed(byte[] k, long l) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void psubscribed(byte[] k, long l) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void unsubscribed(byte[] k, long l) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void punsubscribed(byte[] k, long l) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });

//        RedisPubSubAsyncCommands<byte[], byte[]> async = pubsub.async();
        RedisPubSubCommands<byte[], byte[]> sync = pubsub.sync();
        sync.subscribe(defaultName.getBytes());
//        async.subscribe(defaultName.getBytes());

    }

    @Override
    public void draw() {

        background(255);
        loadPixels();
        System.arraycopy(incomingPixels, 0, this.pixels, 0, 640 * 480);
        updatePixels();
    }

    public void setImage(byte[] message) {
        ByteArrayInputStream bis = new ByteArrayInputStream(message);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object o = in.readObject();
            byte[] px = (byte[]) o;
            byteToInt(px, true, incomingPixels);
        } catch (IOException ex) {
            println("unpack issue " + ex);
        } catch (Exception ex) {
            println("unpack issue2 " + ex);
            ex.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                println("Reading issue");
                // ignore close exception
            }
        }
    }

    public void byteToInt(byte[] incomingImg, boolean RGB, int[] outputImg) {
        assert (incomingImg.length == 3 * outputImg.length);

        // WidthStep to take into account ?!!
        int k = 0;
        for (int j = 0; j < outputImg.length; j++) {
            byte b = incomingImg[k++];
            byte g = incomingImg[k++];
            byte r = incomingImg[k++];
            outputImg[j] = (r & 255) << 16 | (g & 255) << 8 | (b & 255);
        }
    }

    // TODO: add hostname ?
    public static final String OUTPUT_PREFIX = "nectar:";
    public static final String OUTPUT_PREFIX2 = ":camera-server:camera";
    public static final String REDIS_PORT = "6379";

    static String defaultHost = "jiii-mi";
    static String defaultName = OUTPUT_PREFIX + defaultHost + OUTPUT_PREFIX2 + "#0";

    /**
     * @param passedArgs the command line arguments
     */
    static public void main(String[] passedArgs) {

        Options options = new Options();
//         options.addOption("i", "input", true, "Input line in Redis if any.");
        options.addOption("o", "output", true, "Output line in Redis if any, default is:" + defaultName);
        options.addOption("rp", "redisport", true, "Redis port, default is: " + REDIS_PORT);
        options.addOption("rh", "redishost", true, "Redis host, default is: 127.0.0.1");
        options.addOption("h", "host", true, "this computer's name.");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, passedArgs);

            if (cmd.hasOption("o")) {
                String output = cmd.getOptionValue("o");

                System.out.println("Output: " + output);
            } else {
                System.out.println("No output value"); // print the date
                System.out.println("Default output: " + defaultName); // print the date
            }

        } catch (ParseException ex) {
            Logger.getLogger(CameraTestLettuce.class.getName()).log(Level.SEVERE, null, ex);
        }

//        if (passedArgs != null) {
//            PApplet.main(concat(appletArgs, passedArgs));
//        } else {
        String[] appletArgs = new String[]{CameraTestLettuce.class.getName()};
        PApplet.main(appletArgs);
//        }
    }

//        public byte[] intToBytes(int my_int) throws IOException {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutput out = new ObjectOutputStream(bos);
//        out.writeInt(my_int);
//        out.close();
//        byte[] int_bytes = bos.toByteArray();
//        bos.close();
//        return int_bytes;
//    }
//
//    public int bytesToInt(byte[] int_bytes) throws IOException {
//        ByteArrayInputStream bis = new ByteArrayInputStream(int_bytes);
//        ObjectInputStream ois = new ObjectInputStream(bis);
//        int my_int = ois.readInt();
//        ois.close();
//        return my_int;
//    }
}
