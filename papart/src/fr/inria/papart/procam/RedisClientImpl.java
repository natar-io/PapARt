/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import static fr.inria.papart.procam.VideoEmitter.REDIS_HOST;
import static fr.inria.papart.procam.VideoEmitter.REDIS_PORT;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole
 */
public class RedisClientImpl implements RedisClient {

    public static final int REDIS_PORT = 6379;
    public static final String REDIS_HOST = "localhost";
    public static final String NO_AUTH = "";
    protected String redisHost = REDIS_HOST;
    protected int redisPort = REDIS_PORT;
    private String redisAuth = NO_AUTH;

    private static final RedisClientImpl mainClient = new RedisClientImpl();

    public static RedisClientImpl getMainConnection() {
        return mainClient;
    }

    public static Jedis createMainConnection() {
        return mainClient.createConnection();
    }

    @Override
    public Jedis createConnection() {
        Jedis jedis = new Jedis(redisHost, redisPort);
        if(this.redisAuth == null ? NO_AUTH != null : !this.redisAuth.equals(NO_AUTH)){
            jedis.auth(redisAuth);
        }
        return jedis;
    }
    public RedisClientImpl() {
    }

    public RedisClientImpl(RedisClient client) {
      this.setRedisHost(client.getRedisHost());
      this.setRedisPort(client.getRedisPort());
    }

    @Override
    public String getRedisHost() {
        return redisHost;
    }

    @Override
    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }
    
    @Override
    public void setRedisAuth(String redisAuth) {
        this.redisAuth = redisAuth;
    }
    

    @Override
    public int getRedisPort() {
        return redisPort;
    }

    @Override
    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

}
