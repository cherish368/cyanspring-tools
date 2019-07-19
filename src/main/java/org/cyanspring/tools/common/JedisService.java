package org.cyanspring.tools.common;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyanspring.tools.utils.RedisConfig;
import org.cyanspring.tools.utils.StringUtil;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

public class JedisService {
    private Log logger = LogFactory.getLog(JedisService.class);

    private static Map<String, JedisPool> maps = new HashMap<String, JedisPool>();

    /**
     * 获取连接池.
     *
     * @return 连接池实例
     */
    private static JedisPool getPool(String ip, int port) {
        String key = ip + ":" + port;
        JedisPool pool = null;
        synchronized (JedisService.class) {
            if (!maps.containsKey(key)) {
                RedisConfig redConfig = RedisConfig.getInstance();//redConfig.getAuthor();
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(redConfig.getMaxActive());
                config.setMaxIdle(redConfig.getMaxIdle());
                config.setMaxWaitMillis(redConfig.getMaxWait());
                //config.setTestOnBorrow(true);
                config.setTestOnReturn(true);
                try {
                    pool = new JedisPool(config, ip, port, redConfig.getTimeOut(), redConfig.getAuthor(), redConfig.getDatabase());
                    maps.put(key, pool);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                pool = maps.get(key);
            }
        }
        return pool;
    }

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到时才会装载，从而实现了延迟加载。
     */
    private static class RedisUtilHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static JedisService instance = new JedisService();
    }

    /**
     * 当getInstance方法第一次被调用的时候，它第一次读取
     * RedisUtilHolder.instance，导致RedisUtilHolder类得到初始化；而这个类在装载并被初始化的时候，会初始化它的静
     * 态域，从而创建RedisUtil的实例，由于是静态的域，因此只会在虚拟机装载类的时候初始化一次，并由虚拟机来保证它的线程安全性。
     * 这个模式的优势在于，getInstance方法并没有被同步，并且只是执行一个域的访问，因此延迟初始化并没有增加任何访问成本。
     */
    public static JedisService getInstance() {
        return RedisUtilHolder.instance;
    }

    public Jedis getJedis() {
        RedisConfig redConfig = RedisConfig.getInstance();
        return getJedis(redConfig.getRemateIp(), redConfig.getRematePort());
    }

    /**
     * 获取Redis实例.
     *
     * @return Redis工具类实例
     */
    public Jedis getJedis(String ip, int port) {
        Jedis jedis = null;
        int count = 0;
        do {
            try {
                jedis = getPool(ip, port).getResource();
                if (!StringUtil.isEmpty(RedisConfig.getInstance().getAuthor()))
                    jedis.auth(RedisConfig.getInstance().getAuthor());
            } catch (Exception e) {
                logger.error("get redis master1 failed!", e);
                // 销毁对象
                getPool(ip, port).returnBrokenResource(jedis);
            }
            count++;
        } while (jedis == null && count < 5);//BaseConfig.getRetryNum()
        return jedis;
    }

    /**
     * 释放redis实例到连接池.
     *
     * @param jedis redis实例
     */
    public void closeJedis(Jedis jedis, String ip, int port) {
        if (jedis != null) {
            getPool(ip, port).returnResource(jedis);
        }
    }

    public void closeJedis(Jedis jedis) {
        if (jedis != null) {
            RedisConfig redConfig = RedisConfig.getInstance();
            getPool(redConfig.getRemateIp(), redConfig.getRematePort()).returnResource(jedis);
        }
    }

}
