/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam.camera;

import processing.core.PApplet;
import static processing.core.PConstants.RGB;
import processing.core.PImage;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole
 */
public class VideoReceiver {

    public static final int REDIS_PORT = 6379;
    public static final String REDIS_HOST = "localhost";
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;

    Jedis redisSub, redisGet;
    String format;
//    int[] incomingPixels;
    private PImage receivedPx;

    int widthStep = 0;
    int channels = 3;
    private String input;

    static private String host = REDIS_HOST;
    static private int port = REDIS_PORT;

    public VideoReceiver(PApplet applet, String input, Jedis redis) {
        redisGet = redis;
        int w = DEFAULT_WIDTH, h = DEFAULT_HEIGHT;
        widthStep = w * 3;
        this.input = input;
        try {
            w = Integer.parseInt(redisGet.get(input + ":width"));
            h = Integer.parseInt(redisGet.get(input + ":height"));
            widthStep = w;
            format = redisGet.get(input + ":pixelformat");
            if (format.equals("RGB") || format.equals("BGR")) {
                channels = 3;
            }
            if (format.equals("RGBA") || format.equals("ARGB")) {
                channels = 4;
            }
            String wi = redisGet.get(input + ":widthStep");
            if (wi != null) {
                widthStep = Integer.parseInt(wi);
            }
        } catch (Exception e) {
            System.err.println(e);
            System.err.println("Cannot get image size, using 640x480.");
            e.printStackTrace();;
        }
        receivedPx = applet.createImage(w, h, RGB);
    }

    public void start(Jedis pubsub) {
        new RedisThread(pubsub).start();
    }

    public PImage getOnce() throws Exception {
        updateImage();
        return receivedPx;
    }

    public PImage getReceivedPx() {
        return receivedPx;
    }

    class RedisThread extends Thread {

        private final Jedis redisSub;

        public RedisThread(Jedis conn) {
            this.redisSub = conn;
        }

        public void run() {
            byte[] id = input.getBytes();
            // Subscribe tests
            MyListener l = new MyListener();
//        byte[] id = defaultName.getBytes();

            redisSub.subscribe(l, id);
        }
    }

    private void updateImage() throws Exception {
        setImage(redisGet.get(input.getBytes()));
    }

    public void setImage(byte[] message) throws Exception {

        receivedPx.loadPixels();
        int[] px = receivedPx.pixels;

        if (message == null || message.length != px.length * channels) {
            if (message == null) {
                throw new Exception("Cannot get image.");
            } else {
                throw new Exception("Cannot get the image: or size mismatch, " + "m: " + message.length + " px: " + px.length * channels);
            }
        }
        byte[] incomingImg = message;

        int w = widthStep;
        byte[] lineArray = new byte[w];
        int k = 0;

        int skip = 0;
        int sk = 0;
        if (this.widthStep != receivedPx.width) {
            skip = widthStep - (receivedPx.width * 3);
        }
//        System.out.println("Widthstep "  + widthStep + " w " + receivedPx.width + " Skip: " + skip);

        if (format != null && format.equals("BGR")) {
            for (int i = 0; i < message.length / 3; i++) {

                if (k >= message.length - 3) {
                    break;
                }

                byte b = incomingImg[k++];
                byte g = incomingImg[k++];
                byte r = incomingImg[k++];
                px[i] = (r & 255) << 16 | (g & 255) << 8 | (b & 255);

                sk += 3;
                if (sk == receivedPx.width * 3) {
                    k += skip;
                    sk = 0;
                }
            }

        } else {

            if (format.equals("RGB")) {
                for (int i = 0; i < message.length / 3; i++) {
                    if (k >= message.length - 3) {
                        break;
                    }
                    byte r = incomingImg[k++];
                    byte g = incomingImg[k++];
                    byte b = incomingImg[k++];
                    px[i] = (r & 255) << 16 | (g & 255) << 8 | (b & 255);

                    sk += 3;
                    if (sk == receivedPx.width * 3) {
                        k += skip;
                        sk = 0;
                    }
                }
            } else {

                // TODO: really handle ARGB and RGBA ?
                if (format.equals("ARGB") || format.equals("RGBA")) {
                    for (int i = 0; i < message.length / 4; i++) {
                        if (k >= message.length - 4) {
                            break;
                        }
                        byte a = incomingImg[k++];
                        byte r = incomingImg[k++];
                        byte g = incomingImg[k++];
                        byte b = incomingImg[k++];

                        px[i] = (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);

                        sk += 4;
                        if (sk == receivedPx.width * 4) {
                            k += skip;
                            sk = 0;
                        }
                    }
                }
            }
        }

        receivedPx.updatePixels();
    }

    class MyListener extends BinaryJedisPubSub {

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            try {
                updateImage();
            } catch (Exception e) {
                System.err.println("Exception: " + e);
            }
        }

        @Override
        public void onSubscribe(byte[] channel, int subscribedChannels) {
        }

        @Override
        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        }

        @Override
        public void onPSubscribe(byte[] pattern, int subscribedChannels) {
        }

        @Override
        public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
        }

        @Override
        public void onPMessage(byte[] pattern, byte[] channel, byte[] message) {
        }
    }

}
