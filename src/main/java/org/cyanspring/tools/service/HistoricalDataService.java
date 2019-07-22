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
    private String oneKeyLine = "1M";
    private String mthKeyLine = "MTH";
    private Long indexRange = 150L;
    private String[] keyLineType = new String[]{"3M", "5M", "10M", "15M", "30M", "1H", "2H",
            "4H", "6H", "8H", "12H", "D", "W"};

    public static void main(String[] args) {
        PropertyConfigurator.configure("log4j.properties");
        long start = System.currentTimeMillis();
        HistoricalDataService historicalDataService = new HistoricalDataService();
        //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        HistoricalBase historicalBase = new HistoricalBase();
        historicalBase.setLow(0.00002903);
        historicalDataService.updateByValue("XRPBTC", "2019-07-10 00:00", historicalBase);
        //historicalDataService.exportAllJson("XRPBTC", "2019-07-10 00:00", "2019-07-10 01:00");
        long end = System.currentTimeMillis();
        log.info((end - start) * 100 * 0.01 / 1000 / 60 + " minutes");
    }

    /**
     * K线按时间段导出 1M/3M/5M/10M/15M/.../MTH
     *
     * @param symbol    例如 ETH.BTC
     * @param startTime 例如 2019-06-07 21:14
     * @param endTime   例如 2019-06-07 22:14
     * @return String
     */
    public String exportAllJson(String symbol, String startTime, String endTime) {
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
            //1、export 1min data
            String oneSymbol = String.format(templateSymbol, symbol, oneKeyLine);
            final Date startOneTime = TimeUtil.getKeyTime(startDate, oneKeyLine);
            final Date endOneTime = TimeUtil.getKeyTime(endDate, oneKeyLine);
            long endPosition = getPosition(jedis, oneSymbol, oneKeyLine, startDate, startOneTime);
            long startPosition = getPosition(jedis, oneSymbol, oneKeyLine, endDate, endOneTime);
            //考虑1min可能有数据缺失情况，扩大范围检索
            List<String> oneList = jedis.lrange(oneSymbol, startPosition < indexRange ? 0 : startPosition - indexRange, endPosition + indexRange);
            List<String> result = oneList.stream().filter(item -> {
                HistoricalPrice price = JSON.parseObject(item, HistoricalPrice.class);
                return (price.getKeyTime().getTime() >= startOneTime.getTime() &&
                        price.getKeyTime().getTime() <= endOneTime.getTime());
            }).collect(Collectors.toList());
            results.put(oneSymbol, result);
            //2、export 3min/5min/.../1week data
            //认为数据没有缺失
            for (String keyLine : keyLineType) {
                String realSymbol = String.format(templateSymbol, symbol, keyLine);
                Long length = jedis.llen(realSymbol);
                if (length == null || length == 0) {
                    return Message.SYMBOL_NO_EXIST.getText();
                }
                long rightPosition = getPosition(jedis, realSymbol, keyLine, startDate, null);
                long leftPosition = getPosition(jedis, realSymbol, keyLine, endDate, null);
                List<String> collect = jedis.lrange(realSymbol, leftPosition, rightPosition);
                results.put(realSymbol, collect);
            }
            //3、export 1month data
            String mthSymbol = String.format(templateSymbol, symbol, mthKeyLine);
            Long length = jedis.llen(mthSymbol);
            Date startKeyTime = TimeUtil.getKeyTime(startDate, mthKeyLine);
            Date endKeyTime = TimeUtil.getKeyTime(endDate, mthKeyLine);
            List<String> list = new ArrayList<>();
            //月数据量小，从头开始检索
            for (int i = 0; i < length; i++) {
                String lindex = jedis.lindex(mthSymbol, i);
                if (!StringUtils.isEmpty(lindex)) {
                    HistoricalPrice historicalPrice = JSON.parseObject(lindex, HistoricalPrice.class);
                    long time = historicalPrice.getKeyTime().getTime();
                    if (time < startKeyTime.getTime()) {
                        break;
                    } else if (time >= startKeyTime.getTime()) {
                        list.add(JSON.toJSONString(historicalPrice));
                    } else if (time > endKeyTime.getTime()) {
                        continue;
                    }
                }
            }
            results.put(mthSymbol, list);
            JSONArray jsonObject = JSONArray.fromObject(results);
            String jsonString = jsonObject.toString();
            String fullPath = PropertiesUtil.getAttribute(nameProperties, nameExport);
            JsonFileUtil.createJsonFile(jsonString, fullPath);
            return fullPath;
        } catch (ParseException e) {
            log.error(e.getMessage());
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
        Jedis jedis = null;
        try {
            String fullPath = PropertiesUtil.getAttribute(nameProperties, nameImport);
            File myFile = new File(fullPath);
            if (!myFile.exists()) {
                log.error(Message.FILE_NO_EXIST.getText());
                return Message.FILE_NO_EXIST;
            }
            Map<String, List<HistoricalPrice>> map = JsonFileUtil.readJsonFile(fullPath, HistoricalPrice.class);
            log.info("Map size {}", map.size());
            jedis = JedisService.getInstance().getJedis();
            for (String key : map.keySet()) {
                List<HistoricalPrice> list = map.get(key);
                log.info(key + ":size:" + list.size());
                list.sort((a, b) -> Long.compare(a.getKeyTime().getTime(), b.getKeyTime().getTime()));
                Long length = jedis.llen(key);
                String[] splits = key.split("_");
                long interval = TimeUtil.getInterval(splits[splits.length - 1]);
                long leftPosition = 0;
                long rightPosition = length - 1;
                if (!key.contains(mthKeyLine)) {
                    if (key.contains(oneKeyLine)) {
                        //1、deal 1min data
                        String lIndex = jedis.lindex(key, 0);
                        HistoricalPrice lPrice = JSON.parseObject(lIndex, HistoricalPrice.class);
                        HistoricalPrice lastPrice = list.get(0);
                        HistoricalPrice firstPrice = list.get(list.size() - 1);
                        leftPosition = (lPrice.getKeyTime().getTime() - firstPrice.getKeyTime().getTime()) / interval;
                        leftPosition = leftPosition - indexRange < 0 ? 0 : leftPosition - indexRange;
                        rightPosition = (lPrice.getKeyTime().getTime() - lastPrice.getKeyTime().getTime()) / interval;
                        rightPosition = rightPosition + indexRange > length ? length : rightPosition + indexRange;
                    } else {
                        //2、deal 3min/.../1week data
                        String lIndex = jedis.lindex(key, 0);
                        HistoricalPrice lPrice = JSON.parseObject(lIndex, HistoricalPrice.class);
                        HistoricalPrice lastPrice = list.get(0);
                        HistoricalPrice firstPrice = list.get(list.size() - 1);
                        leftPosition = (lPrice.getKeyTime().getTime() - firstPrice.getKeyTime().getTime()) / interval;
                        rightPosition = (lPrice.getKeyTime().getTime() - lastPrice.getKeyTime().getTime()) / interval + 1;
                    }
                    //3、deal 1month data
                }
                insertOrUpdateModel(list, jedis, key, leftPosition, rightPosition, interval);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            JedisService.getInstance().closeJedis(jedis);
        }
        return Message.OK;
    }

    private void insertOrUpdateModel(List<HistoricalPrice> list, Jedis jedis, String key,
                                     long leftPosition, long rightPosition, long interval) {
        for (HistoricalPrice item : list) {
            Date prevDate = new Date(item.getKeyTime().getTime() - interval);
            String prevKeyTime = simpleDateFormat.format(prevDate);
            inner:
            for (long i = rightPosition; i >= leftPosition; i--) {
                String index = jedis.lindex(key, i);
                if (!StringUtils.isEmpty(index) && index.contains(prevKeyTime)) {
                    String save = jedis.lindex(key, i - 1);
                    HistoricalPrice savePrice = JSON.parseObject(save, HistoricalPrice.class);
                    boolean flag = false;
                    if (item.getKeyTime().getTime() == savePrice.getKeyTime().getTime()) {
                        //exist update
                        HistoricalPrice prevPrice = JSON.parseObject(index, HistoricalPrice.class);
                        double prevClose = prevPrice.getClose();
                        item.setOpen(prevClose);
                        if (PriceUtils.GreaterThan(prevClose, item.getHigh())) {
                            item.setHigh(prevClose);
                        }
                        if (PriceUtils.LessThan(prevClose, item.getLow())) {
                            item.setLow(prevClose);
                        }
                        item.setVolume(1.0); //test set
                        jedis.lset(key, i - 1, JSON.toJSONString(item));
                        log.info(key + " lset:" + (i - 1) + ":" + JSON.toJSONString(item));
                        flag = true;
                    } else {
                        //not exist insert
                        item.setVolume(2.0); //test set
                        jedis.linsert(key, BinaryClient.LIST_POSITION.BEFORE, index, JSON.toJSONString(item));
                        log.info(key + " linsert:" + (i - 1) + ":" + JSON.toJSONString(item));
                        flag = true;
                    }
                    if (flag) {
                        break inner;
                    }
                }
            }
        }
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
        Jedis jedis = null;
        try {
            Date date = TimeUtil.parseDate(stringTime, pattern);
            jedis = JedisService.getInstance().getJedis();
            //1、deal 1min data
            String oneSymbol = String.format(templateSymbol, symbol, oneKeyLine);
            long onePosition = getPosition(jedis, oneSymbol, oneKeyLine, date, null);
            String oneIndex = jedis.lindex(oneSymbol, onePosition);
            if (!oneIndex.contains(stringTime)) {
                //数据量，考虑有缺失数据的情况出现，扩大范围查找
                long left = onePosition < indexRange ? 0 : onePosition - indexRange;
                Long length = jedis.llen(oneSymbol);
                long right = onePosition + indexRange > length ? length : onePosition + indexRange;
                List<String> lrange = jedis.lrange(oneSymbol, left, right);
                long num = -1L;
                if (lrange != null && !lrange.isEmpty()) {
                    inner:
                    for (long j = left; j <= right; j++) {
                        String index = lrange.get((int) j);
                        if (!StringUtils.isEmpty(index) && index.contains(stringTime)) {
                            num = j;
                            onePosition = j;
                            break inner;
                        }
                    }
                }
                if (num == -1L) {
                    return Message.TIME_VALUE_ERROR;
                }
                oneIndex = jedis.lindex(oneSymbol, num);
            }
            HistoricalPrice onePrice = JSON.parseObject(oneIndex, HistoricalPrice.class);
            //对指定的值赋值
            if (base.getHigh() != null) {
                onePrice.setHigh(base.getHigh());
            }
            if (base.getOpen() != null) {
                onePrice.setOpen(base.getOpen());
            }
            if (base.getLow() != null) {
                onePrice.setLow(base.getLow());
            }
            if (base.getClose() != null) {
                onePrice.setClose(base.getClose());
            }
            //根据上一个K线烛台修正数据
            setValueByPrev(jedis, onePrice, onePosition, oneSymbol);
            //2、deal 3min/.../1week data
            for (int i = 0; i < keyLineType.length; i++) {
                String keyLine = keyLineType[i];
                String realSymbol = String.format(templateSymbol, symbol, keyLine);
                String firstIndex = jedis.lindex(realSymbol, 0);
                HistoricalPrice firstPrice = JSON.parseObject(firstIndex, HistoricalPrice.class);

                //数据量大，烛台间隔稳定，通过间隔计算位置查询，认为数据没有丢失
                Date keyTime = TimeUtil.getKeyTime(date, keyLine);
                long interval = TimeUtil.getInterval(keyLine);
                long position = (firstPrice.getKeyTime().getTime() - keyTime.getTime()) / interval;
                String currIndex = jedis.lindex(realSymbol, position);
                HistoricalPrice currPrice = JSON.parseObject(currIndex, HistoricalPrice.class);
                //定位1min起始时间点
                currPrice = setValueByTime(jedis, currPrice, date, keyLine, realSymbol);
                if (currPrice == null) {
                    return Message.ONE_MIN_DATA_NOT_FOUND;
                }
                //根据上一个K线烛台修正数据
                setValueByPrev(jedis, onePrice, position, realSymbol);
            }
            //3、deal 1month data
            String mthSymbol = String.format(templateSymbol, symbol, mthKeyLine);
            Date keyTime = TimeUtil.getKeyTime(date, mthKeyLine);
            String mthFormat = simpleDateFormat.format(keyTime);
            long length = jedis.llen(mthKeyLine);
            String mthIndex = "";
            long mthPosition = 0;
            //数据量小，烛台间隔不稳定，直接从头开始检索
            for (long i = 0; i < length; i++) {
                mthIndex = jedis.lindex(mthSymbol, i);
                if (mthIndex.contains(mthFormat)) {
                    mthPosition = i;
                    break;
                }
            }
            if (StringUtil.isEmpty(mthIndex)) {
                return Message.MONTH_DATA_NOT_FOUND;
            }
            HistoricalPrice mthPrice = JSON.parseObject(mthIndex, HistoricalPrice.class);
            mthPrice = setValueByTime(jedis, mthPrice, date, mthKeyLine, mthSymbol);
            if (mthPrice == null) {
                return Message.ONE_MIN_DATA_NOT_FOUND;
            }
            //根据上一个K线烛台修正数据
            setValueByPrev(jedis, onePrice, mthPosition, mthSymbol);
            return Message.OK;
        } catch (ParseException e) {
            log.error(e.getMessage());
            return Message.START_TIME_ERROR;
        } finally {
            JedisService.getInstance().closeJedis(jedis);
        }
    }

    /**
     * 根据当前时间获取1min上下范围内数据，
     * 找到最高值和最低值处理并更新烛台
     *
     * @param jedis
     * @param price
     * @param date
     * @param keyLine
     * @param realSymbol
     * @return
     */
    private HistoricalPrice setValueByTime(Jedis jedis, HistoricalPrice price, Date date, String keyLine, String realSymbol) {
        List<Date> keyTimes = TimeUtil.getKeyTimes(date, keyLine);
        long left = keyTimes.get(0).getTime();
        long right = keyTimes.get(1).getTime();
        List<String> list = jedis.lrange(realSymbol, left, right);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Stream<HistoricalPrice> historicalPriceStream = list.stream().map(jsonStr -> JSON.parseObject(jsonStr, HistoricalPrice.class));
        double maxHigh = historicalPriceStream.mapToDouble(HistoricalPrice::getHigh).max().getAsDouble();
        double minLow = historicalPriceStream.mapToDouble(HistoricalBase::getLow).min().getAsDouble();
        if (PriceUtils.GreaterThan(maxHigh, price.getHigh())) {
            price.setHigh(maxHigh);
        }
        if (PriceUtils.LessThan(minLow, price.getLow())) {
            price.setLow(minLow);
        }
        return price;
    }


    /**
     * 根据上一个烛台修正当前烛台数据
     *
     * @param jedis
     * @param price
     * @param index
     * @param realSymbol
     */
    private void setValueByPrev(Jedis jedis, HistoricalPrice price, long index, String realSymbol) {
        String prevIndex = jedis.lindex(realSymbol, index + 1);
        if (!StringUtils.isEmpty(prevIndex)) {
            HistoricalPrice prevPrice = JSON.parseObject(prevIndex, HistoricalPrice.class);
            double prevClose = prevPrice.getClose();
            price.setOpen(prevClose);
            if (PriceUtils.GreaterThan(prevClose, price.getHigh())) {
                price.setHigh(prevClose);
            }
            if (PriceUtils.LessThan(prevClose, price.getLow())) {
                price.setLow(prevClose);
            }
        }
        String result = JSON.toJSONString(price);
        jedis.lset(realSymbol, index, result);
        log.info(realSymbol + " lset:" + result);
    }


    /**
     * 计算烛台数据所在位置
     *
     * @param jedis
     * @param realSymbol
     * @param keyLine
     * @param date
     * @return
     */
    private long getPosition(Jedis jedis, String realSymbol, String keyLine, Date date, Date keyTime) {
        String oneIndex = jedis.lindex(realSymbol, 0);
        HistoricalPrice onePrice = JSON.parseObject(oneIndex, HistoricalPrice.class);
        if (keyTime == null)
            keyTime = TimeUtil.getKeyTime(date, keyLine);
        long interval = TimeUtil.getInterval(keyLine);
        long position = (onePrice.getKeyTime().getTime() - keyTime.getTime()) / interval;
        return position;
    }


}
