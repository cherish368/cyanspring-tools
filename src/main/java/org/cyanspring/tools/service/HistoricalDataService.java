package org.cyanspring.tools.service;

import com.alibaba.fastjson.JSON;
import net.sf.json.JSONArray;
import org.apache.log4j.PropertyConfigurator;
import org.cyanspring.tools.common.JedisService;
import org.cyanspring.tools.common.Message;
import org.cyanspring.tools.model.HistoricalBase;
import org.cyanspring.tools.model.HistoricalPrice;
import org.cyanspring.tools.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.io.File;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HistoricalDataService {
    private static final Logger log = LoggerFactory.getLogger(HistoricalDataService.class);
    private static final String pattern = "yyyy-MM-dd HH:mm";
    private static final String nameProperties = "redis";
    private static final String nameExport = "export.path";
    private static final String nameImport = "import.path";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    private String templateSymbol = "H_INDEX_%s_%s";
    private String[] keyLineTypeArray = new String[]{"1M", "3M", "5M", "10M", "15M", "30M", "1H", "2H",
            "4H", "6H", "8H", "12H", "D", "W", "MTH"};
    private long leftIndex = 0L;
    private long rightIndex = 0L;

    public static void main(String[] args) {
        PropertyConfigurator.configure(System.getProperty("user.dir") + "/log4j.properties");
        long start = System.currentTimeMillis();
        HistoricalDataService historicalDataService = new HistoricalDataService();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        //historicalDataService.insertOrUpdateByJson();
        historicalDataService.exportAllJson("BTCUSDT", "2017-11-13 00:00:00", "2019-06-07 22:14");
        long end = System.currentTimeMillis();
        log.info((end - start) * 100 * 0.01 / 1000 / 60 + " minutes");
    }

    /**
     * K线按时间段导出 1M/3M/5M/10M/15M/.../MTH
     *
     * @param tempSymbol 例如 ETH.BTC
     * @param startTime  例如 2019-06-07 21:14
     * @param endTime    例如 2019-06-07 22:14
     * @return String
     */
    public String exportAllJson(String tempSymbol, String startTime, String endTime) {
        Jedis jedis = null;
        try {
            Date startDate = TimeUtil.parseDate(startTime, pattern);
            Date endDate = TimeUtil.parseDate(endTime, pattern);
            long start = startDate.getTime();
            long end = endDate.getTime();
            if (start - end > 0) {
                return Message.START_END_TIME_ERROR.getText();
            }
            jedis = JedisService.getInstance().getJedis();
            log.info("jedis DB {}", jedis.getDB());
            Map<String, Object> results = new LinkedHashMap<>();
            for (String keyLine : keyLineTypeArray) {
                String realSymbol = String.format(templateSymbol, tempSymbol, keyLine);
                Long llen = jedis.llen(realSymbol);
                if (llen == null || llen == 0) {
                    return Message.SYMBOL_NO_EXIST.getText();
                }
                //计算时间段内的时间
                Date keyStart = TimeUtil.getKeyTime(startDate, keyLine);
                Date keyEnd = TimeUtil.getKeyTime(endDate, keyLine);
                List<HistoricalPrice> list = new ArrayList<>();
                flag:
                for (int i = 0; i < llen; i++) {
                    String lindex = jedis.lindex(realSymbol, i);
                    if (!StringUtils.isEmpty(lindex)) {
                        HistoricalPrice historicalPrice = JSON.parseObject(lindex, HistoricalPrice.class);
                        long time = historicalPrice.getKeyTime().getTime();
                        if (time < keyStart.getTime()) {
                            continue flag;
                        } else if (time >= keyStart.getTime()) {
                            list.add(historicalPrice);
                        } else if (time > keyEnd.getTime()) {
                            break flag;
                        }
                    }
                }
                List<String> collect = list.stream().sorted((a, b) -> Long.compare(a.getKeyTime().getTime(), b.getKeyTime().getTime()))
                        .map(s -> JSON.toJSONString(s)).collect(Collectors.toList());
                results.put(realSymbol, collect);
            }
            JSONArray jsonObject = JSONArray.fromObject(results);
            String jsonString = jsonObject.toString();
            String fullPath = PropertiesUtil.getAttribute(nameProperties, nameExport);
            JsonFileUtil.createJsonFile(jsonString, fullPath);
            return fullPath;
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            JedisService.getInstance().closeJedis(jedis);
        }
        return null;
    }


    /**
     * 通过Json文件导入Redis数据修复缺失数据
     *
     * @return message
     */
    public Message insertOrUpdateByJson() {
        String fullPath = PropertiesUtil.getAttribute(nameProperties, nameImport);
        File myFile = new File(fullPath);
        if (!myFile.exists()) {
            log.error(Message.FILE_NO_EXIST.getText());
            return Message.FILE_NO_EXIST;
        }
        Map<String, List<HistoricalPrice>> map = JsonFileUtil.readJsonFile(fullPath, HistoricalPrice.class);
        log.info("Map size {}", map.size());
        Jedis jedis = JedisService.getInstance().getJedis();
        //是否需要支持事务 TODO
        for (String realSymbol : map.keySet()) {
            //保证按时间正序
            List<HistoricalPrice> list = map.get(realSymbol);
            System.out.println("size:" + list.size());
            if (list != null && !list.isEmpty()) {
                rightIndex = jedis.llen(realSymbol);
                if (rightIndex == 0) {
                    return Message.SYMBOL_NO_EXIST;
                }
                leftIndex = 0;
                String[] symbols = realSymbol.split("_");
                for (int i = 0; i < list.size(); i++) {
                    //查询第一个最小时间烛台对应的上一个烛台，找到插入或更新位置
                    long interval = TimeUtil.getInterval(list.get(i).getKeyTime(), symbols[symbols.length - 1]);
                    Date prevDate = new Date(list.get(i).getKeyTime().getTime() - interval);
                    String prevKeyTime = simpleDateFormat.format(prevDate);
                    long total = 0;
                    if (i == 0) {
                        total = list.size();
                    }
                    Message message = insertOrUpdateModel(list.get(i), jedis, realSymbol, prevKeyTime, total);
                    if (Message.OK != message) {
                        log.error(message.getText());
                        return message;
                    }
                }
            }
        }
        JedisService.getInstance().closeJedis(jedis);
        return Message.OK;
    }

    private Message insertOrUpdateModel(HistoricalPrice item, Jedis jedis, String realSymbol, String prevKeyTime, long size) {
        for (long i = leftIndex; i < rightIndex; i++) {
            String index = jedis.lindex(realSymbol, i);
            if (index.contains(prevKeyTime)) {
                String save = jedis.lindex(realSymbol, i - 1);
                if (!StringUtils.isEmpty(save)) {
                    HistoricalPrice savePrice = JSON.parseObject(save, HistoricalPrice.class);
                    if (item.getKeyTime().getTime() == savePrice.getKeyTime().getTime()) {
                        //存在更新
                        HistoricalPrice prevPrice = JSON.parseObject(index, HistoricalPrice.class);
                        double prevClose = prevPrice.getClose();
                        item.setOpen(prevClose);
                        if (PriceUtils.GreaterThan(prevClose, item.getHigh())) {
                            item.setHigh(prevClose);
                        }
                        if (PriceUtils.LessThan(prevClose, item.getLow())) {
                            item.setLow(prevClose);
                        }
                        jedis.lset(realSymbol, i - 1, JSON.toJSONString(item));
                        log.info(realSymbol + " lset:" + JSON.toJSONString(item));
                    } else {
                        //不存在插入
                        jedis.linsert(realSymbol, BinaryClient.LIST_POSITION.BEFORE, index, JSON.toJSONString(item));
                        log.info(realSymbol + " linsert:" + JSON.toJSONString(item));
                    }
                    if (size != 0) {
                        leftIndex = i - 2 * size;
                        if (leftIndex < 0) {
                            leftIndex = 0;
                        }
                        rightIndex = i + 2 * size;
                        long length = jedis.llen(realSymbol);
                        if (rightIndex > length) {
                            rightIndex = length;
                        }
                    }
                    return Message.OK;
                }
                //针对某个keyLine检索完毕还没找到上一个烛台直接报错，不再继续检索下一个keyLine
                //弊端：无法完成对第一个烛台的K线的修正
                if (i == rightIndex - 1) {
                    log.error(Message.TIME_VALUE_ERROR.getText());
                    return Message.TIME_VALUE_ERROR;
                }
            }
        }
        log.error(Message.FAILED.getText());
        return Message.FAILED;
    }


    /**
     * 给定时间通过分析进行修复K线数据
     *
     * @param symbol     例如 ETH.BTC
     * @param stringTime 例如 2019-06-15 15:15
     * @return message
     */
    public Message updateByValue(String symbol, String stringTime, HistoricalBase base) {

        if (base.getHigh() == null && base.getOpen() == null
                && base.getLow() == null && base.getClose() == null) {
            return Message.SELECT_VALUE_UPDATE;
        }
        try {
            Date date = TimeUtil.parseDate(stringTime, pattern);
            Jedis jedis = JedisService.getInstance().getJedis();
            //是否需要支持事务 TODO
            for (String keyLine : keyLineTypeArray) {
                String realSymbol = String.format(templateSymbol, symbol, keyLine);
                long length = jedis.llen(realSymbol);
                if (length == 0) {
                    return Message.SYMBOL_NO_EXIST;
                }
                Date keyTime = TimeUtil.getKeyTime(date, keyLine);
                String format = simpleDateFormat.format(keyTime);
                log.info("format:" + format);
                flag:
                for (long i = 0; i < length; i++) {
                    String index = jedis.lindex(realSymbol, i);
                    log.info(index);
                    if (!StringUtils.isEmpty(index) && index.contains(format)) {
                        HistoricalPrice currPrice = JSON.parseObject(index, HistoricalPrice.class);
                        //对指定的值赋值
                        if (base.getHigh() != null) {
                            currPrice.setHigh(base.getHigh());
                        }
                        if (base.getOpen() != null) {
                            currPrice.setOpen(base.getOpen());
                        }
                        if (base.getLow() != null) {
                            currPrice.setLow(base.getLow());
                        }
                        if (base.getClose() != null) {
                            currPrice.setClose(base.getClose());
                        }
                        //根据上一个K线烛台修正数据
                        String prevIndex = jedis.lindex(realSymbol, i + 1);
                        HistoricalPrice prevPrice = JSON.parseObject(prevIndex, HistoricalPrice.class);
                        double prevClose = prevPrice.getClose();
                        currPrice.setOpen(prevClose);
                        if (PriceUtils.GreaterThan(prevClose, currPrice.getHigh())) {
                            currPrice.setHigh(prevClose);
                        }
                        if (PriceUtils.LessThan(prevClose, currPrice.getLow())) {
                            currPrice.setLow(prevClose);
                        }
                        jedis.lset(realSymbol, i, JSON.toJSONString(currPrice));
                        log.info(realSymbol + " lset:" + JSON.toJSONString(currPrice));
                        break flag;
                    }
                    //针对某个keyLine检索完毕还没找到直接报错，不在继续检索下一个keyLine
                    //弊端：无法完成对第一个烛台的K线的修正
                    if (i == length - 1) {
                        log.error(Message.TIME_VALUE_ERROR.getText());
                        return Message.TIME_VALUE_ERROR;
                    }
                }
            }
            return Message.OK;
        } catch (ParseException e) {
            log.error(e.getMessage());
            return Message.START_TIME_ERROR;
        }
    }

    public Message updateByValue2(String symbol, String stringTime, HistoricalBase base) {
        if (base.getHigh() == null && base.getOpen() == null
                && base.getLow() == null && base.getClose() == null) {
            return Message.SELECT_VALUE_UPDATE;
        }
        try {
            Date date = TimeUtil.parseDate(stringTime, pattern);
            Jedis jedis = JedisService.getInstance().getJedis();
            String oneIndex = null;
            HistoricalPrice onePrice = null;
            for (int i = 0; i < keyLineTypeArray.length; i++) {
                String keyLine = keyLineTypeArray[i];
                String realSymbol = String.format(templateSymbol, symbol, keyLine);
                String firstIndex = jedis.lindex(realSymbol, 0);
                HistoricalPrice firstPrice = JSON.parseObject(firstIndex, HistoricalPrice.class);
                if (i == 0) {
                    oneIndex = firstIndex;
                    onePrice = JSON.parseObject(firstIndex, HistoricalPrice.class);
                }
                //cal index position
                Date keyTime = TimeUtil.getKeyTime(date, keyLine);
                long interval = TimeUtil.getInterval(keyTime, keyLine);
                long position = (firstPrice.getKeyTime().getTime() - keyTime.getTime()) / interval;
                long swing = TimeUtil.getTimeSwing(keyLine);
                long left = position < swing ? 0 : position - swing;
                long right = position + swing;
                List<String> lrange = jedis.lrange(realSymbol, left, right);
                long num = -1L;
                if (lrange != null && !lrange.isEmpty()) {
                    inner:
                    for (long j = left; j <= right; j++) {
                        String index = lrange.get((int) j);
                        if (index.contains(simpleDateFormat.format(keyTime))) {
                            num = j;
                            break inner;
                        }
                    }
                }
                if (num == -1L) {
                    return Message.TIME_VALUE_ERROR;
                }
                String currIndex = jedis.lindex(realSymbol, num);
                HistoricalPrice currPrice = JSON.parseObject(currIndex, HistoricalPrice.class);
                //对指定的值赋值
                if (i == 0) {
                    if (base.getHigh() != null) {
                        currPrice.setHigh(base.getHigh());
                    }
                    if (base.getOpen() != null) {
                        currPrice.setOpen(base.getOpen());
                    }
                    if (base.getLow() != null) {
                        currPrice.setLow(base.getLow());
                    }
                    if (base.getClose() != null) {
                        currPrice.setClose(base.getClose());
                    }
                } else {
                    List<Date> keyTimes = TimeUtil.getKeyTimes(date, keyLine);
                    if (keyTimes != null && keyTimes.size() == 2) {
                        long oneInterval = TimeUtil.getInterval(null, "1M");
                        long beginPosition = (onePrice.getKeyTime().getTime() - keyTimes.get(0).getTime()) / oneInterval;
                        long endPosition = (onePrice.getKeyTime().getTime() - keyTimes.get(1).getTime()) / oneInterval;
                        List<String> list = jedis.lrange(realSymbol, beginPosition, endPosition);
                        Stream<HistoricalPrice> tempStream = list.stream().map(s -> JSON.parseObject(s, HistoricalPrice.class));

                        double max = tempStream.mapToDouble(HistoricalPrice::getHigh).max().getAsDouble();
                        double min = tempStream.mapToDouble(HistoricalPrice::getLow).max().getAsDouble();
                        if (PriceUtils.GreaterThan(max, currPrice.getHigh())) {
                            currPrice.setHigh(max);
                        }
                        if (PriceUtils.LessThan(min, currPrice.getLow())) {
                            currPrice.setLow(min);
                        }
                    }

                }
                //根据上一个K线烛台修正数据
                String prevIndex = jedis.lindex(realSymbol, num + 1);
                if (!StringUtils.isEmpty(prevIndex)) {
                    HistoricalPrice prevPrice = JSON.parseObject(prevIndex, HistoricalPrice.class);
                    double prevClose = prevPrice.getClose();
                    currPrice.setOpen(prevClose);
                    if (PriceUtils.GreaterThan(prevClose, currPrice.getHigh())) {
                        currPrice.setHigh(prevClose);
                    }
                    if (PriceUtils.LessThan(prevClose, currPrice.getLow())) {
                        currPrice.setLow(prevClose);
                    }
                }
                jedis.lset(realSymbol, num, JSON.toJSONString(currPrice));
                log.info(realSymbol + " lset:" + JSON.toJSONString(currPrice));
            }
            return Message.OK;
        } catch (ParseException e) {
            log.error(e.getMessage());
            return Message.START_TIME_ERROR;
        }
    }


}
