/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 *
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package org.cyanspring.tools.utils;

import org.cyanspring.tools.common.Clock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Dennis Chen
 */
public class TimeUtil {
    public final static long millisInDay = 60 * 60 * 24 * 1000;
    public final static long millisInHour = 60 * 60 * 1000;
    public final static long millisInMinute = 60 * 1000;
    public final static long millisInSecond = 1000;
    public final static int HOURS_OF_DAY = 24;

    public static long getTimePass(Date time) {
        Date now = Clock.getInstance().now();
        return now.getTime() - time.getTime();
    }

    public static long getTimePass(long time) {
        return System.currentTimeMillis() - time;
    }

    public static long getTimePass(Date now, Date time) {
        return now.getTime() - time.getTime();
    }

    public static boolean isTimeOut(long time, long period) {
        return TimeUtil.getTimePass(time) > period;
    }

    public static Date parseTime(String format, String time) throws ParseException {
        Calendar today, adjust;
        today = Calendar.getInstance();
        today.setTime(new Date());
        adjust = Calendar.getInstance();
        adjust.setTime(new SimpleDateFormat(format).parse(time));
        adjust.set(Calendar.YEAR, today.get(Calendar.YEAR));
        adjust.set(Calendar.MONTH, today.get(Calendar.MONTH));
        adjust.set(Calendar.DATE, today.get(Calendar.DATE));
        return adjust.getTime();

    }

    public static Date getOnlyDate(Date date) {
        Calendar cal = Calendar.getInstance();
        return getOnlyDate(cal, date);
    }

    public static Date getOnlyDate(Calendar cal, Date date) {
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date convertToday(Calendar cal, Date date, int nHour, int nMin, int nSecond) {
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, nHour);
        cal.set(Calendar.MINUTE, nMin);
        cal.set(Calendar.SECOND, nSecond);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getPreviousDay() {
        Date result = new Date(Clock.getInstance().now().getTime() - 24 * 60 * 60 * 1000);
        return result;
    }

    public static Date getPreviousDay(Date date) {
        Date result = new Date(date.getTime() - 24 * 60 * 60 * 1000);
        return result;
    }

    public static Date getNextDay(Date date) {
        Date result = new Date(date.getTime() + 24 * 60 * 60 * 1000);
        return result;
    }

    public static boolean sameDate(Date d1, Date d2) {
        if (null == d1 || null == d2)
            return false;
        return getOnlyDate(d1).equals(getOnlyDate(d2));
    }

    public static String formatDate(Date dt, String strFmt) {
        SimpleDateFormat sdf = new SimpleDateFormat(strFmt);
        return sdf.format(dt);
    }

    public static Date parseDate(String strValue, String strFmt)
            throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(strFmt);
        return sdf.parse(strValue);
    }

    public static Date subDate(Date dt, int span, TimeUnit unit) {
        return new Date(dt.getTime() - unit.toMillis(span));
    }

    public static Date addDate(Date dt, int span, TimeUnit unit) {
        return new Date(dt.getTime() + unit.toMillis(span));
    }

    public static boolean sameSecond(Date d1, Date d2) {
        return Math.abs(d1.getTime() - d2.getTime()) < 1000;
    }

    // convert whatever day/time to today time
    public static Date convertToday(Date time) {
        Date date = getOnlyDate(time);
        Date start = getOnlyDate(Clock.getInstance().now());
        return new Date(start.getTime() + time.getTime() - date.getTime());
    }

    // convert whatever day/time to dest time
    public static Date convertToday(Date time, Date dest) {
        Date date = getOnlyDate(time);
        Date start = getOnlyDate(dest);
        return new Date(start.getTime() + time.getTime() - date.getTime());
    }

    // convert time in string to dest Date. e.g. 16:40:00
    public static Date convertToday(String time, Date dest) throws Exception {
        String[] times = time.split(":");
        if (times.length != 3) {
            throw new Exception("Invalid time format: " + time);
        }

        int nHour = Integer.parseInt(times[0]);
        int nMin = Integer.parseInt(times[1]);
        int nSecond = Integer.parseInt(times[2]);

        Calendar cal = Calendar.getInstance();

        Date date = TimeUtil.convertToday(cal, dest, nHour, nMin,
                nSecond);

        return date;
    }

    // convert time in string to today's Date. e.g. 16:40:00
    public static Date convertToday(String time) throws Exception {
        Date now = Clock.getInstance().now();
        return convertToday(time, now);

    }

    public static long dayTimestamp(long time) {
        return time / millisInDay * millisInDay;
    }

    /**
     * get week in year
     * Sunday is first day
     *
     * @param date String yyyy-MM-dd
     * @return
     */
    public static int getTraditionalWeek(String date) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            cal.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        return week;
    }

    /**
     * get week in year
     * Sunday is first day
     *
     * @param date Date
     * @return
     */
    public static int getTraditionalWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        return week;
    }

    /**
     * get week in year
     * Monday is first day
     *
     * @param date String yyyy-MM-dd
     * @return
     */
    public static int getNormalWeek(String date) {
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int week = localDate.get(weekFields.weekOfYear());
        return week;

    }

    /**
     * get week in year
     * Monday is first day
     *
     * @param date Date
     * @return
     */
    public static int getNormalWeek(Date date) {
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault(); //need think Zone
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        int week = localDate.get(weekFields.weekOfYear());
        return week;

    }

    /**
     * get month in year
     * January  is first month,index is 1
     *
     * @param date Date
     * @return
     */
    public static int getNormalMonth(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault(); //need think Zone
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        int monthValue = localDate.getMonthValue();
        return monthValue;

    }

    /**
     * @param date
     * @param offset 0-表示本周，1-表示下周，-1-表示上周
     * @return
     */
    public static String getFirstDayInWeek(Date date, int offset) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate().plusWeeks(offset).with(DayOfWeek.MONDAY);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTimeFormatter.format(localDate);
    }

    /**
     * @param date
     * @param offset 0本月，1下个月，-1上个月，依次类推
     * @return
     */
    public static Date getFirstDayInMonth(Date date, int offset) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate()
                .plusMonths(offset).with(TemporalAdjusters.firstDayOfMonth());
        instant = localDate.atStartOfDay().atZone(zoneId).toInstant();
        Date result = Date.from(instant);
        return result;
    }

    /**
     * @param date
     * @param offset 0本月，1下个月，-1上个月，依次类推
     * @return
     */
    public static Date getLastDayInMonth(Date date, int offset) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate()
                .plusMonths(offset).with(TemporalAdjusters.lastDayOfMonth());
        instant = localDate.atStartOfDay().atZone(zoneId).toInstant();
        Date result = Date.from(instant);
        return result;
    }


    /**
     * get yyyy-MM-dd date
     *
     * @param date
     * @return
     */
    public static String getShortDate(Date date) {
        if (date == null)
            date = Clock.getInstance().now();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }

    public static Date getKeyTime(Date datetime, String strType) {
        Calendar keyTime = Calendar.getInstance();
        keyTime.setTime(datetime);
        keyTime.set(Calendar.SECOND, 0);
        keyTime.set(Calendar.MILLISECOND, 0);
        switch (strType) {
            case "1M":
                return keyTime.getTime();
            case "3M":
                keyTime.set(Calendar.MINUTE,
                        ((keyTime.get(Calendar.MINUTE) / 3) * 3));
                break;
            case "5M":
                keyTime.set(Calendar.MINUTE,
                        ((keyTime.get(Calendar.MINUTE) / 5) * 5));
                break;
            case "10M":
                keyTime.set(Calendar.MINUTE,
                        ((keyTime.get(Calendar.MINUTE) / 10) * 10));
                break;
            case "15M":
                keyTime.set(Calendar.MINUTE,
                        ((keyTime.get(Calendar.MINUTE) / 15) * 15));
                break;
            case "30M":
                keyTime.set(Calendar.MINUTE,
                        ((keyTime.get(Calendar.MINUTE) / 30) * 30));
                break;
            case "1H":
                keyTime.set(Calendar.MINUTE, 0);
                keyTime.set(Calendar.HOUR_OF_DAY, keyTime.get(Calendar.HOUR_OF_DAY));
                break;
            case "2H":
                keyTime.set(Calendar.MINUTE, 0);
                keyTime.set(Calendar.HOUR_OF_DAY,
                        (keyTime.get(Calendar.HOUR_OF_DAY) / 2) * 2);
                break;
            case "4H":
                keyTime.set(Calendar.MINUTE, 0);
                keyTime.set(Calendar.HOUR_OF_DAY,
                        (keyTime.get(Calendar.HOUR_OF_DAY) / 4) * 4);
                break;
            case "6H":
                keyTime.set(Calendar.MINUTE, 0);
                keyTime.set(Calendar.HOUR_OF_DAY,
                        (keyTime.get(Calendar.HOUR_OF_DAY) / 6) * 6);
                break;
            case "8H":
                keyTime.set(Calendar.MINUTE, 0);
                keyTime.set(Calendar.HOUR_OF_DAY,
                        (keyTime.get(Calendar.HOUR_OF_DAY) / 8) * 8);
                break;
            case "12H":
                keyTime.set(Calendar.MINUTE, 0);
                keyTime.set(Calendar.HOUR_OF_DAY,
                        (keyTime.get(Calendar.HOUR_OF_DAY) / 12) * 12);
                break;
            case "D":
                keyTime.set(Calendar.MINUTE, 0);
                keyTime.set(Calendar.HOUR_OF_DAY, 0);
                break;
            case "W":
                keyTime.set(Calendar.MINUTE, 0);
                keyTime.set(Calendar.HOUR_OF_DAY, 0);
                keyTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case "MTH":
                keyTime.set(Calendar.MINUTE, 0);
                keyTime.set(Calendar.HOUR_OF_DAY, 0);
                keyTime.set(Calendar.DAY_OF_MONTH, 1);
                break;
            default:
                break;
        }
        return keyTime.getTime();
    }

    public static long getInterval(String strType) {
        long interval = 60000L;
        switch (strType) {
            case "1M":
                return interval;
            case "3M":
                return interval * 3;
            case "5M":
                return interval * 5;
            case "10M":
                return interval * 10;
            case "15M":
                return interval * 15;
            case "30M":
                return interval * 30;
            case "1H":
                return interval * 60;
            case "2H":
                return interval * 120;
            case "4H":
                return interval * 240;
            case "6H":
                return interval * 360;
            case "8H":
                return interval * 480;
            case "12H":
                return interval * 720;
            case "D":
                return interval * 1440;
            case "W":
                return interval * 7 * 1440;
        }
        return interval;
    }

    /**
     * 含头含尾
     * 3min/5min-->1min
     * 10min/15min-->5min
     * 30min-->15min
     * 1h-->30min
     * 2h-->1h
     * 4h/6h-->2h
     * 8h-->4h
     * 12h-->6h
     * D-->12h
     * W-->D
     * MTH-->D
     *
     * @param realTime
     * @param strType
     * @param firstKeyTime
     * @return
     */
    public static List<Date> getKeyTimes(Date realTime, String strType, Date firstKeyTime) {
        List<Date> list = new ArrayList<>();
        Date keyTime = getKeyTime(realTime, strType);
        list.add(keyTime);
        int length = 0;
        switch (strType) {
            case "3M":
                length += 2;
                break;
            case "5M":
                length += 4;
                break;
            case "10M":
                length += 5;
                break;
            case "15M":
                length += 10;
                break;
            case "30M":
                length += 15;
                break;
            case "1H":
                length += 30;
                break;
            case "2H":
                length += 60;
                break;
            case "4H":
                length += 120;
                break;
            case "6H":
                length += 240;
                break;
            case "8H":
                length += 240;
                break;
            case "12H":
                length += 360;
                break;
            case "D":
                length += 720;
                break;
            case "W":
                length += 6 * 1440;
                break;
            case "MTH":
                Calendar now = Calendar.getInstance();
                now.setTime(keyTime);
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);
                int actualMaximum = now.getActualMaximum(Calendar.DAY_OF_MONTH);
                length = 1440 * (actualMaximum - 1);
                break;
            default:
                break;
        }
        Date date = TimeUtil.addDate(keyTime, length, TimeUnit.MINUTES);
        if (getTimePass(firstKeyTime, date) < 0) {
            date = firstKeyTime;
        }
        list.add(date);
        return list;

    }

    public static void main(String[] args) {
        DateTimeFormatter localFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
        Date beginDate = Date.from(Instant.from(localFormatter.parse("2019-06-05 23:59")));
        Date data = Date.from(Instant.from(localFormatter.parse("2019-07-11 23:59")));
        String[] keyLineTypeArray = new String[]{"3M", "5M", "10M", "15M", "30M", "1H", "2H",
                "4H", "6H", "8H", "12H", "D", "W", "MTH"};
        for (String keyLineType : keyLineTypeArray) {
            List<Date> keyTimes = getKeyTimes(beginDate, keyLineType, data);
            if (!keyTimes.isEmpty()) {
                for (Date date : keyTimes) {
                    System.out.print(keyTimes.size() + ":" + keyLineType + ":" + localFormatter.format(date.toInstant()) + "\n");
                }
                System.out.println();
            }
        }

    }

}
