/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import fr.inria.papart.procam.camera.Camera;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import static processing.core.PConstants.ARGB;
import static processing.core.PConstants.RGB;
import processing.core.PImage;
import processing.data.JSONObject;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole
 */
public class VideoEmitter {

    Jedis redis, redisSend;

    // Arguments
    public static final int REDIS_PORT = 6379;
    public static final String REDIS_HOST = "localhost";

    private String host = REDIS_HOST;
    private int port = REDIS_PORT;
    private String output = "image";

    private PImage imageRef;
    private int colorImageCount;

    public VideoEmitter() {
    }

    public VideoEmitter(String key, String host, int port) {
        this.host = host;
        this.port = port;
        this.output = key;

        connectRedis();
    }

    public void setReference(PImage img) {
        imageRef = img;
        sendParams(img);
    }

    public void sendImage(PImage img, int time) {

        if (imageRef == null || img.width != imageRef.width || img.height != imageRef.height) {
            setReference(img);
        }

        img.loadPixels();
        byte[] imageData;
        try {
            imageData = integersToBytes(img.pixels);
        } catch (IOException ex) {
            Logger.getLogger(VideoEmitter.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Cannot convert image to bytes.");
            return;
        }

        colorImageCount++;

        String name = output;
        byte[] id = name.getBytes();
        JSONObject imageInfo = new JSONObject();
        imageInfo.setLong("timestamp", time);
        imageInfo.setLong("imageCount", colorImageCount);
        redis.set(id, imageData);
        redis.publish(id, imageInfo.toString().getBytes());
    }

    byte[] integersToBytes(int[] values) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (int i = 0; i < values.length; ++i) {
            dos.writeInt(values[i]);
        }

        return baos.toByteArray();
    }

    private void checkConnection() {
    }

    private void connectRedis() {
        try {
            redis = new Jedis(host, port);
            if (redis == null) {
                throw new Exception("Cannot connect to server. ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        // redis.auth("156;2Asatu:AUI?S2T51235AUEAIU");
    }

    private void sendParams(PImage img) {
        redis.set(output + ":width", Integer.toString(img.width));
        redis.set(output + ":height", Integer.toString(img.height));
        redis.set(output + ":channels", Integer.toString(4));
        if (img.format == RGB) {
            redis.set(output + ":pixelformat", Camera.PixelFormat.RGB.toString());
        }

        if (img.format == ARGB) {
            redis.set(output + ":pixelformat", Camera.PixelFormat.ARGB.toString());
        }

    }
}
