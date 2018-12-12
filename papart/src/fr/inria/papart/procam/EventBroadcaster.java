/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchList;
import fr.inria.papart.multitouch.tracking.TouchPointEventHandler;
import processing.core.PVector;
import processing.data.JSONObject;
import redis.clients.jedis.Jedis;

/**
 *
 * @author ditrop
 */
public class EventBroadcaster {
    
    
    private String prefix = "evt:99:";
    private String prefixPub = "evt:99";
    private Jedis redis;

    public EventBroadcaster(String host, int port){
        connectRedis();
    }
    
    public void connectRedis() {
        redis = new Jedis("127.0.0.1", 6379);
        // redis.auth("156;2Asatu:AUI?S2T51235AUEAIU");
    }

    public void captureMouse() {
        JSONObject ob = new JSONObject();
        ob.setString("name", "captureMouse");
        ob.setBoolean("pressed", true);
        redis.publish(prefixPub, ob.toString());
    }

    public void releaseMouse() {
        JSONObject ob = new JSONObject();
        ob.setString("name", "captureMouse");
        ob.setBoolean("pressed", false);
        redis.publish(prefixPub, ob.toString());
    }

    public void captureKeyboard() {
        JSONObject ob = new JSONObject();
        ob.setString("name", "captureKeyboard");
        ob.setBoolean("pressed", true);
        redis.publish(prefixPub, ob.toString());
    }

    public void releaseKeyboard() {
        JSONObject ob = new JSONObject();
        ob.setString("name", "captureKeyboard");
        ob.setBoolean("pressed", false);
        redis.publish(prefixPub, ob.toString());
    }

    /**
     * Send touch list event to Redis EXPERIMENTAL
     *
     * @param t
     */
    public void sendTouchs(TouchList touchList, PVector drawingSize) {
        for (Touch t : touchList) {
            sendTouch(t, drawingSize);
        }
    }

    /**
     * Send touch event to Redis EXPERIMENTAL
     *
     * @param t
     */
    public void sendTouch(Touch t, PVector drawingSize) {

        boolean creation = false;
        if (t.trackedSource().attachedObject == null) {
            t.trackedSource().attachedObject = new TouchKiller(t);
            creation = true;
            System.out.println("Attaching tracked source to " + t.id + " -- creation event ?");
        }

        JSONObject ob = new JSONObject();
        ob.setString("name", "pointer");
        ob.setString("id", Integer.toString(t.id));
        ob.setFloat("x", t.position.x / drawingSize.x);
        ob.setFloat("y", t.position.y / drawingSize.y);
        ob.setBoolean("pressed", t.pressed);
        ob.setBoolean("creation", creation);
        redis.publish(prefixPub, ob.toString());
    }

    class TouchKiller implements TouchPointEventHandler {

        public Touch touch;

        public TouchKiller(Touch t) {
            this.touch = t;
        }

        @Override
        public void delete() {
            JSONObject ob = new JSONObject();
            ob.setString("name", "pointerDeath");
            ob.setString("id", Integer.toString(touch.id));
            redis.publish(prefixPub, ob.toString());
        }

    }

}
