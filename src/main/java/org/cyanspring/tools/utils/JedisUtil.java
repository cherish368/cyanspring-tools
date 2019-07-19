package org.cyanspring.tools.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyanspring.tools.common.BaseEntity;
import org.cyanspring.tools.common.JedisService;
import org.cyanspring.tools.common.SOMList;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.SortingParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JedisUtil {//jedisTemplate
    protected final transient Log logger = LogFactory.getLog(getClass());

    /**
     * 通过管道快速查询大数据量
     *
     * @param listKey
     * @param startIndex
     * @param endIndex
     * @return
     */
    public List<Object> getIndexListByPipeline(String listKey, long startIndex, long endIndex) {
        Jedis jedis = JedisService.getInstance().getJedis();
        List<Object> list = getIndexListByPipeline(jedis, listKey, startIndex, endIndex);
        return list;
    }

    public List<Object> getIndexListByPipeline(Jedis jedis, String listKey, long startIndex, long endIndex) {
        Pipeline pipeline = jedis.pipelined();
        for (long i = startIndex; i <= endIndex; i++) {
            pipeline.lindex(listKey, i);
        }
        List<Object> list = pipeline.syncAndReturnAll();
        try {
            pipeline.close();
        } catch (IOException err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return list;
    }

    public String getIndexByPipeline(Jedis jedis, String listKey, long index) {
        String lindex = jedis.lindex(listKey, index);
        return lindex;
    }

    /**
     * 清空所有key
     */
    public String flushAll() {
        String stata = "";
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            stata = jedis.flushAll();
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return stata;
    }

    public String get(String key) {
        String val = "";
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            val = get(jedis, key);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return val;
    }

    public String get(Jedis jedis, String key) {
        return jedis.get(key);
    }

    public void hset(String hName, String key, String value) {
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            hset(jedis, hName, key, value);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
    }

    public void hset(Jedis jedis, String hName, String key, String value) {//为哈希表中的字段赋值
        jedis.hset(hName, key, value);
    }

    public String hget(String hName, String key) {
        Jedis jedis = JedisService.getInstance().getJedis();
        String rv = "";
        try {
            rv = hget(jedis, hName, key);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return rv;
    }

    public String hget(Jedis jedis, String hName, String key) {
        return jedis.hget(hName, key);
    }

    public <T> void hsetJson(String hName, String key, List<T> list) {
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            hsetJson(jedis, hName, key, list);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
    }

    public <T> void hsetJson(Jedis jedis, String hName, String key, List<T> list) {
        if (list != null && !list.isEmpty()) {
            String json = FastJsonUtil.beanToJson(list);
            hset(jedis, hName, key, json);
        }
    }

    public <T> List<T> hgetJson(String hName, String key, TypeReference clazz) {
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            return hgetJson(jedis, hName, key, clazz);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    public <T> List<T> hgetJson(Jedis jedis, String hName, String key, TypeReference<List<T>> clazz) {
        String json = hget(jedis, hName, key);
        if (!StringUtil.isEmpty(json)) {
            return JSON.parseObject(json, clazz);
        }
        return null;
    }

    public void hdel(String hName, String key) {
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            hdel(jedis, hName, key);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
    }

    public void hdel(Jedis jedis, String hName, String key) {
        try {
            jedis.hdel(hName, key);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
    }

    public void put(String key, String value) {
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            put(jedis, key, value);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
    }

    public void put(Jedis jedis, String key, String value) {
        jedis.set(key, value);
    }

    public <T extends BaseEntity> List<T> getList(String key) {
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            byte[] bs = jedis.get(key.getBytes());
            ListTranscoder<T> transcoder = new ListTranscoder<T>();
            returnJedis(jedis);
            return transcoder.deserialize(bs);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    public <T extends BaseEntity> List<T> getList(Jedis jedis, String key) {
        try {
            byte[] bs = jedis.get(key.getBytes());
            ListTranscoder<T> transcoder = new ListTranscoder<T>();
            return transcoder.deserialize(bs);
        } catch (Exception err) {
            logger.error(err);
        } finally {

        }
        return null;
    }

    public <T extends BaseEntity> void setRange(String key, List<T> list) {
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            setRange(jedis, key, list);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
    }

    public <T extends BaseEntity> void setRange(Jedis jedis, String key, List<T> list) {
        try {
            for (T model : list) {
                jedis.rpush(key, model.toString());
            }
        } catch (Exception err) {
            logger.error(err);
        }
    }

    public <T extends BaseEntity> List<T> getRange(String key, Class<T> clazz) {
        Jedis jedis = JedisService.getInstance().getJedis();
        List<T> list = null;
        try {
            list = getRange(jedis, key, clazz);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return list;
    }

    public <T extends BaseEntity> List<T> getRange(Jedis jedis, String key, Class<T> clazz) {
        SOMList<T> somList = new SOMList<T>();
        List<String> list = null;
        try {
            list = jedis.lrange(key, 0, -1);//jedis.incr("");
        } catch (Exception err) {
            logger.error(err);
        }
        return somList.builder(list, clazz);
    }

    public <T extends BaseEntity> void setList(String key, List<T> list) {
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            setList(jedis, key, list);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
    }

    public <T extends BaseEntity> void setList(Jedis jedis, String key, List<T> list) {
        try {
            ListTranscoder<T> transcoder = new ListTranscoder<T>();
            jedis.set(key.getBytes(), transcoder.serialize(list));
        } catch (Exception err) {
            logger.error(err);
        }
    }

    /**
     * 删除keys对应的记录,可以是多个key
     *
     * @param keys
     * @return 删除的记录数
     */
    public long del(String... keys) {
        long count = 0;
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            count = del(jedis, keys);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return count;
    }

    public long del(Jedis jedis, String... keys) {
        long count = jedis.del(keys);
        return count;
    }

    public long hdel(Jedis jedis, String key, String... fields) {
        long count = jedis.hdel(key, fields);
        return count;
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return boolean
     */
    public boolean contains(String key) {
        boolean exis = false;
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            exis = contains(jedis, key);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return exis;
    }

    public boolean contains(Jedis jedis, String key) {
        boolean exis = jedis.exists(key);
        return exis;
    }

    /**
     * 对List,Set,SortSet进行排序,如果集合数据较大应避免使用这个方法
     *
     * @param key
     * @return List<String> 集合的全部记录
     **/
    public List<String> sort(String key) {
        List<String> list = new ArrayList<String>();
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            list = sort(jedis, key);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return list;
    }

    public List<String> sort(Jedis jedis, String key) {
        List<String> list = jedis.sort(key);
        return list;
    }

    /**
     * 对List,Set,SortSet进行排序或limit
     *
     * @param key
     * @param parame 定义排序类型或limit的起止位置.
     * @return List<String> 全部或部分记录
     **/
    public List<String> sort(String key, SortingParams parame) {
        List<String> list = new ArrayList<String>();
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            list = sort(jedis, key, parame);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return list;
    }

    public List<String> sort(Jedis jedis, String key, SortingParams parame) {
        List<String> list = jedis.sort(key, parame);
        return list;
    }

    /**
     * 返回指定key存储的类型
     *
     * @param key
     * @return String string|list|set|zset|hash
     **/
    public String type(String key) {
        String type = "";
        Jedis sjedis = JedisService.getInstance().getJedis();
        try {
            type = type(sjedis, key);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(sjedis);
        }
        return type;
    }

    public String type(Jedis sjedis, String key) {
        String type = sjedis.type(key);
        return type;
    }

    public Long incr(Jedis jedis, String key) {
        Long incr = jedis.incr(key);
        return incr;
    }

    public Long incr(String key) {
        Long incr = 0l;
        Jedis sjedis = JedisService.getInstance().getJedis();
        try {
            incr = incr(sjedis, key);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(sjedis);
        }
        return incr;
    }

    /**
     * 查找所有匹配给定的模式的键
     *
     * @param pattern 的表达式,*表示多个，？表示一个
     */
    public Set<String> keys(String pattern) {
        Set<String> set = new HashSet<String>();
        Jedis jedis = JedisService.getInstance().getJedis();
        try {
            set = keys(jedis, pattern);
        } catch (Exception err) {
            logger.error(err);
        } finally {
            returnJedis(jedis);
        }
        return set;
    }

    public Set<String> keys(Jedis jedis, String pattern) {
        Set<String> set = jedis.keys(pattern);
        return set;
    }

    public void returnJedis(Jedis jedis) {
        JedisService.getInstance().closeJedis(jedis);
    }

    private static JedisUtil cache = null;

    public static JedisUtil getInstance() {
        if (cache == null) {
            cache = new JedisUtil();
        }
        return cache;
    }
}