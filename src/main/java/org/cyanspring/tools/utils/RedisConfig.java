package org.cyanspring.tools.utils;


import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RedisConfig {
    // Redis服务器IP
    protected String remateIp = "127.0.0.1";
    // Redis的端口号
    protected int rematePort = 6379;
    protected int database = 0;
    // 访问密码
    protected String author = "smartbit1234";
    // 可用连接实例的最大数目，默认值为8；
    // 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    protected int maxActive = 1024;
    // 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    protected int maxIdle = 200;
    // 等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    protected int maxWait = 10000;
    protected int timeOut = 10000;
    // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    protected boolean onBorrow = true;

    protected static RedisConfig config;

    public static RedisConfig getInstance() {// RedisConfig
        if (config == null) {
            Properties prop = new Properties();
            try {
                InputStream in = RedisConfig.class.getResourceAsStream("/redis.properties");
                prop.load(in);
                config = builder(prop);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    protected static RedisConfig builder(Properties prop) {
        RedisConfig config = new RedisConfig();
        config.setRemateIp(prop.getProperty("redis.hostName"));
        config.setRematePort(StringUtil.toInt(prop.getProperty("redis.port")));
        config.setAuthor(prop.getProperty("redis.password"));
        config.setDatabase(StringUtil.toInt(prop.getProperty("redis.database")));
        config.setMaxActive(StringUtil.toInt(prop.getProperty("redis.maxActive")));
        config.setMaxIdle(StringUtil.toInt(prop.getProperty("redis.maxIdle")));
        config.setMaxWait(StringUtil.toInt(prop.getProperty("redis.maxWait")));
        config.setTimeOut(StringUtil.toInt(prop.getProperty("redis.timeout")));
        config.setOnBorrow(StringUtil.toBollean(prop.getProperty("redis.testOnBorrow")));
        return config;
    }

    public JedisConnectionFactory getJedisConnectionFactory() {
        RedisConfig config = RedisConfig.getInstance();
        JedisConnectionFactory factory = new JedisConnectionFactory();
        //优先采用用户页面配置
        factory.setHostName(config.getRemateIp());
        factory.setDatabase(config.getDatabase());
        factory.setPort(config.getRematePort());
        factory.setPassword(config.getAuthor());
        return factory;
    }

    public String getRemateIp() {
        return remateIp;
    }

    public void setRemateIp(String remateIp) {
        this.remateIp = remateIp;
    }

    public int getRematePort() {
        return rematePort;
    }

    public void setRematePort(int rematePort) {
        this.rematePort = rematePort;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public boolean isOnBorrow() {
        return onBorrow;
    }

    public void setOnBorrow(boolean onBorrow) {
        this.onBorrow = onBorrow;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }
}