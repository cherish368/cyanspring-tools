package org.cyanspring.tools.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {


    /**
     * 对xml敏感字符过滤
     *
     * @param str
     * @return
     */
    public static String ClearChar(String str) {
        String regEx = "[\"`~!@#$%^&*()=|{}':;'\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        str = str.replaceAll(regEx, "");
        return str;
    }

    /* 转化成字符串 */
    public static String toStr(Object obj) {
        if (obj == null)
            return "";
        else
            return obj.toString();
    }

    /**
     * 去掉所有空格
     */
    public static String formatNum(String num) {
        return num.replaceAll("\\s*", "");
    }

    public static String fomartRq(String str) {
        String ym = "";
        String[] split = str.split("-");
        ym = split[0] + "年" + split[1] + "个月";
        return ym;
    }

    public static String toStr(byte[] bytes) {
        return toStr(bytes, "UTF-8");
    }

    public static String toStr(byte[] bytes, String charestName) {
        String msg = "";
        try {
            msg = new String(bytes, charestName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public static int getInt(String str) {
        if (isEmpty(str))
            return 0;
        int a = Integer.parseInt(str);
        return a;
    }


    public static String[] getArray(String str) {
        if (isEmpty(str))
            return null;
        String[] arr = str.split(",");
        return arr;
    }

    /* 判断数值是否为空 */
    public static boolean isIntEmpty(Integer num) {
        if (num == null || num == 0)
            return true;
        else
            return false;
    }

    /* 判断字符串是否为空 */
    public static boolean isEmpty(String str) {
        if (str == null || str.equals(""))
            return true;
        else
            return false;
    }

    public static boolean isNotEmpty(String str) {
        return isEmpty(str) == true ? false : true;
    }

    public static boolean isEqual(String str1, String str2) {
        if (str1 != null && str1.equals(str2))
            return true;
        else
            return false;
    }

    public static boolean isPositive(Float number) {
        if (number == null || number <= 0)
            return false;
        return true;
    }

    public static Float toFloatTwo(Float number1, Float number2) {
        return toFloatTwo(number1 + number2);
    }

    public static Float toFloatTwo(Float number) {
        return (float) mathRound(number, 2);
    }

    public static Float toFloatOne(Float number) {
        return (float) mathRound(number, 1);
    }

    public static String toFloatTwoPer(Integer number1, Integer number2) {
        Float result = 0f;
        if (number1 != null && number2 != null)
            result = (float) mathRound(number1 * 100 / number2, 2);
        return toStr(result) + "%";
    }

    protected static String padding(String str, int length, char ch,
                                    boolean isLeft) {
        if (str.length() < length) {
            String pad = "";
            for (int i = str.length(); i < length; i++)
                pad += ch;
            if (isLeft)
                str = pad + str;
            else
                str = str + pad;
        }
        return str;
    }

    public static String paddingLeft(String str, int left, String ch) {
        if (isEmpty(ch)) ch = "0";
        return paddingLeft(str, left, ch.charAt(0));
    }

    public static String paddingLeft(String str, int left, char ch) {
        return padding(str, left, ch, true);
    }

    public static String paddingRight(String str, int right, char ch) {
        return padding(str, right, ch, false);
    }

    public static String clearHtmlTag(String inputString) {
        StringBuffer retVal = new StringBuffer();
        if (!isEmpty(inputString)) {
            inputString = inputString.trim();
            for (int i = 0; i < inputString.length(); i++) {
                switch (inputString.charAt(i)) {
                    case '<':
                        retVal.append("");
                        break;
                    case '>':
                        retVal.append("");
                        break;
                    default:
                        retVal.append(inputString.charAt(i));
                        break;
                }
            }
        }
        return retVal.toString();
    }

    /**
     * 清除字符串中含有的危险字符
     *
     * @param inputString
     * @return
     */
    public static String cleanString(String inputString) {
        StringBuffer retVal = new StringBuffer();
        if (!isEmpty(inputString)) {
            inputString = processSqlStr(inputString);
            inputString = inputString.trim();
            for (int i = 0; i < inputString.length(); i++) {
                switch (inputString.charAt(i)) {
                    case '\'':
                        retVal.append("");//&quot;
                        break;
                    case '<':
                        retVal.append("");//&lt;
                        break;
                    case '>':
                        retVal.append("");//&gt;
                        break;
				/*case '*':
					retVal.append("&#42;");
					break;*/
                    default:
                        retVal.append(inputString.charAt(i));
                        break;
                }
            }
        }
        return retVal.toString();
    }

    public static String processSqlStr(String str) {
        if (str != "") {
            String sqlStr = "and |or |exec |insert |select |delete |update |count |chr |mid |master |truncate |char |declare ";
            String[] anySqlStr = sqlStr.split("\\|");
            for (String ss : anySqlStr) {
                if ((!isEmpty(str)) && str.indexOf(ss) >= 0) {
                    ss = clearSpacialStr(ss);
                    str = str.replaceAll(ss, "");
                }
            }
        }
        return str;
    }

    public static String getXmlEscape(String text) {
        if (isEmpty(text))
            return "";
        else {
            text = text.replaceAll("&", "&amp;");
            text = text.replaceAll("<", "&lt;");
            text = text.replaceAll(">", "&gt;");
            text = text.replaceAll(" ", "&nbsp;");
            return text;
        }
    }

    public static String getUnXmlEscape(String text) {
        if (isEmpty(text))
            return "";
        else {
            text = text.replace("&lt;", "<");
            text = text.replace("&gt;", ">");
            text = text.replace("&amp;", "&");
            return text;
        }
    }

    public static String replace(String source, String oldString,
                                 String newString) {
        StringBuffer output = new StringBuffer();
        int lengthOfSource = source.length(); //
        int lengthOfOld = oldString.length(); //
        int posStart = 0; //
        int pos; //
        while ((pos = source.indexOf(oldString, posStart)) >= 0) {
            output.append(source.substring(posStart, pos));
            output.append(newString);
            posStart = pos + lengthOfOld;
        }
        if (posStart < lengthOfSource) {
            output.append(source.substring(posStart));
        }
        return output.toString();
    }

    public static String getLeftStr(String str, int length, String endStr) {// length一共返回的字数
        if (StringUtil.isEmpty(str))
            return "";
        String reStr;
        if (length < getStrLength(str)) {
            reStr = str.substring(0, length - 1) + endStr;
        } else {
            reStr = str;
        }
        return reStr;
    }

    public static String getRight(String str, int length) {// length一共返回的字数
        if (StringUtil.isEmpty(str))
            return "";
        String reStr;
        if (length < getStrLength(str)) {
            reStr = str.substring(str.length() - 6, str.length());
        } else {
            reStr = str;
        }
        return reStr;
    }

    /* 截取左边规定字数字符串 */
    public static String getLeftStr(String str, int length) {
        if (StringUtil.isEmpty(str))
            return "";
        String reStr;
        if (length < str.length()) {
            reStr = str.substring(0, length - 1) + "...";
        } else {
            reStr = str;
        }
        return reStr;
    }

    /* 获得双字节字符串的字节数 */
    public static int getStrLength(String str) {
        byte bt[] = str.getBytes();
        int l = 0; // l 为字符串之实际长度
        for (int i = 0; i < bt.length; i++) {
            if (bt[i] == 63) { // 判断是否为汉字或全脚符号
                l++;
            }
            l++;
        }
        return l;
    }

    public static int getLength(String str) {
        if (isEmpty(str))
            return 0;
        else
            return str.length();
    }

    public static String getHostName(String uri) {
        if (!StringUtil.isEmpty(uri)) {
            if (uri.startsWith("/"))
                uri = uri.substring(1);
            uri = StringUtil.getFirstStr(uri, "/");
            uri = "/" + uri;
        }
        return uri;
    }

    /* 剥去HTML标签 */
    public static String regStripHtml(String text) {
        return regStripHtml(text, "");
    }

    /* 剥去HTML标签 */
    public static String regStripHtml(String text, String rTxt) {// <(\S*?)
        // [^>]*>.*?</\1>|<.*? />
        String rePattern = "<([^>]*)>";// "^(0|[1-9]\\d*)(|\\.?\\d{1,3})$";
        // <\\s*(\\S+)(\\s[^>]*)?>
        Pattern pattern = Pattern.compile(rePattern);
        Matcher matcher = pattern.matcher(text);
        StringBuffer strBuffer = new StringBuffer();
        Boolean result = matcher.find();
        if (result) {
            while (result) {
                matcher.appendReplacement(strBuffer, rTxt);
                result = matcher.find();
            }
        } else
            return text;
        matcher.appendTail(strBuffer);
        return strBuffer.toString();
    }

    /* 是否包含字符串 */
    public static boolean isContants(String str1, String str2) {
        int result = str1.toUpperCase().indexOf(str2.toUpperCase());
        boolean b = false;
        if (result != -1)
            b = true;
        return b;
    }

    /* 获得某个字符串在另个字符串中出现的次数 */
    public static int getStrCount(String strOriginal, String strSymbol) {
        int count = 0;
        for (int i = 0; i < (strOriginal.length() - strSymbol.length() + 1); i++) {
            if (strOriginal.substring(i, i + strSymbol.length() - 1).equals(
                    strSymbol)) {
                count = count + 1;
            }
        }
        return count;
    }

    /* 获得某个字符串在另个字符串第一次出现时前面所有字符 */
    public static String getFirstStr(String strOriginal, String strSymbol) {
        int strPlace = strOriginal.indexOf(strSymbol);
        if (strPlace != -1)
            strOriginal = strOriginal.substring(0, strPlace);
        return strOriginal;
    }

    /* 获得某个字符串在另个字符串第一次出现时后面所有字符 */
    public static String getFirstStrAfter(String strOriginal, String strSymbol) {
        int strPlace = strOriginal.indexOf(strSymbol);
        if (strPlace != -1)
            strOriginal = strOriginal.substring(strPlace + 1);
        return strOriginal;
    }

    /* 获得某个字符串在另个字符串最后一次出现时后面所有字符 */
    public static String getLastStr(String strOriginal, String strSymbol) {
        int strPlace = strOriginal.lastIndexOf(strSymbol) + strSymbol.length();
        strOriginal = strOriginal.substring(strPlace);
        return strOriginal;
    }

    /* 获得某个字符串在另个字符串最后一次出现时前面所有字符 */
    public static String getLastStrBefore(String strOriginal, String strSymbol) {
        int strPlace = strOriginal.lastIndexOf(strSymbol) + strSymbol.length();
        strOriginal = strOriginal.substring(0, strPlace);
        return strOriginal;
    }

    public static String getUrlPath(String strOriginal) {
        return getLastStrBefore(strOriginal, File.separator) + File.separator;//"/"
    }

    public static String getUrlPage(String strOriginal) {
        return getLastStr(strOriginal, File.separator);//"/"
    }

    public static String getFileType(String strOriginal) {
        return getLastStr(strOriginal, ".");
    }

    // 清理url参数
    public static String clearParam(String queryStr, String parName) {
        if (isEmpty(queryStr))
            return "";
        else {
            String[] paras = queryStr.split("&");
            String[] vals;
            String newQueryStr = "";
            for (int i = 0; i < paras.length; i++) {
                vals = paras[i].split("=");
                if (vals.length == 2) {
                    if (vals[0].equals(parName))
                        continue;
                    if (!isEmpty(newQueryStr))
                        newQueryStr += "&";
                    newQueryStr += vals[0] + "=" + vals[1];
                }
            }
            return newQueryStr;
        }
    }

    public static String outHighlightText(String str, String findstr) {
        if (StringUtil.isNotEmpty(str) && StringUtil.isNotEmpty(findstr)) {
            String text1 = "<font class=\"queryTitl\">%s</font>";//.queryTitl{color:red;}
            findstr = clearSpacialStr(findstr);
            str = str.replaceAll(findstr, text1.replace("%s", findstr));
        }
        return str;
    }

    public static String outHighlightText(String str, String findstr, String cssclass) {
        if (StringUtil.isNotEmpty(str) && StringUtil.isNotEmpty(findstr)) {
            String text1 = "<span class=\"" + cssclass + "\">%s</span>";
            findstr = clearSpacialStr(findstr);
            str = str.replaceAll(findstr, text1.replace("%s", findstr));
        }
        return str;
    }

    public static String clearSpacialStr(String str) {
        if (!isEmpty(str)) {
            str = str.replaceAll("\\(", "\\\\\\(").replaceAll("\\)", "\\\\\\)")
                    .replaceAll("\\*", "\\\\\\*").replaceAll("\\+", "\\\\\\+");
        }
        return str;
    }

    public static boolean isMatches(String str, String rePattern) {
        Pattern pattern = Pattern.compile(rePattern);
        Matcher matcher = pattern.matcher(str);
        // 指示匹配是否成功
        return matcher.matches();
    }

    /* 是否是数字 */
    public static boolean isNum(String str) {
        String rePattern = "^-?(\\d*)$";
        // 指示匹配是否成功
        return isMatches(str, rePattern);
    }

    // 转换成长整型
    public static Long toLong(String str) {
        if (isEmpty(str))
            return 0L;
        else if (isNum(str)) {
            return Long.valueOf(str);
        } else
            return 0L;
    }

    public static Integer toInt(String str) {
        if (isEmpty(str))
            return 0;
        else if (isNum(str)) {
            return Integer.valueOf(str);
        } else if (isDouble(str)) {
            Double db = toDouble(str);
            return db.intValue();
        } else
            return 0;
    }

    public static Integer toInt(Object obj) {
        if (obj == null)
            return 0;
        else {
            return toInt(toStr(obj));
        }
    }

    public static Integer toInt(Long lng) {
        if (lng == null)
            return null;
        else
            return lng.intValue();
    }

    public static boolean isDouble(String str) {
        //^[-+]?(/d+(/./d*)?|/./d+)([eE]([-+]?([012]?/d{1,2}|30[0-7])|-3([01]?[4-9]|[012]?[0-3])))?[dD]?$
        //String rePattern = "^[-]?(0|[1-9]\\d*)(|\\.?\\d{1,3})$";
        String rePattern = "^([-])?(?!0\\d)\\d+(\\.\\d{1,})?(E([-])?(?!0\\d)\\d+(\\.\\d{1,}))?$";
        return isMatches(str, rePattern);
    }

    public static boolean isSDouble(String str) {
        String rePattern2 = "^((-?\\d+.?\\d*)[Ee]{1}(-?\\d+))$";
        return isMatches(str, rePattern2);
    }

    public static Double toDouble(String str) {
        if (!isEmpty(str) && (isDouble(str) || isSDouble(str)))
            return Double.valueOf(String.valueOf(str));
        else
            return 0.0;
    }

    public static Float toFloat(Object obj) {
        return toFloat(toStr(obj));
    }

    public static Float toFloat(String str) {
        if (isEmpty(str) || !isDouble(str))
            return Float.valueOf(0);
        else
            return Float.valueOf(str);
    }

    public static String formatFloatNumber(String value) {
        if (!isEmpty(value)) {
            if (isDouble(value) || isSDouble(value)) {
                Double valDb = toDouble(value);
                if (valDb.doubleValue() != 0.00) {
                    DecimalFormat df = new DecimalFormat("########.00");
                    return df.format(valDb.doubleValue());
                }
            } else {
                return "0.00";
            }
        }
        return "";
    }

    public static String formatFloatNumber(Float value) {
        if (value != null) {
            if (value.doubleValue() != 0.00) {
                DecimalFormat df = new DecimalFormat("########.00");
                return df.format(value.doubleValue());
            } else {
                return "0.00";
            }
        }
        return "";
    }

    public static String formatFloatNumber(Double value) {
        if (value != null) {
            if (value.doubleValue() != 0.00) {
                DecimalFormat df = new DecimalFormat("########.00");
                return df.format(value.doubleValue());
            } else {
                return "0.00";
            }
        }
        return "";
    }

    /* 保留多位数字 */
    public static double mathRound(double value, int digits) {
        BigDecimal bg = new BigDecimal(value);
        double f1 = bg.setScale(digits, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1;
    }

    public static Float mathRound(Float value, int digits) {
        BigDecimal bg = new BigDecimal(value);
        Float f1 = bg.setScale(digits, BigDecimal.ROUND_HALF_UP).floatValue();
        return f1;
    }

    /* 保留多位数字返回字符串 */
    public static String mathRoundStr(double value, int digits) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(digits);
        return nf.format(value);
    }

    public static boolean isMobile(String str) {
        boolean isMobile = false;
        String rePattern = "^(((13[0-9])|(14[0-9])|(17[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$";
        isMobile = isMatches(str, rePattern);
        return isMobile;
    }

    public static boolean isDate(String str) {
        boolean isDate = false;
        String rePattern = "^(\\d{4})\\-(\\d{1,2})\\-(\\d{1,2})$";
        isDate = isMatches(str, rePattern);
        if (!isDate) {
            rePattern = "^(\\d{4})\\-(\\d{1,2})\\-(\\d{1,2}) (\\d{1,2}):(\\d{1,2})$";
            isDate = isMatches(str, rePattern);
        }
        // isDate = isMatches(str, rePattern);
        if (!isDate) {
            rePattern = "^(\\d{4})\\-(\\d{1,2})\\-(\\d{1,2}) (\\d{1,2}):(\\d{1,2}):(\\d{1,2})$";
            isDate = isMatches(str, rePattern);
        }
        return isDate;
    }

    public static Date toLongDate(String str) {
        return toDate(str, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date toDate(String str) {
        if (isEmpty(str))
            return null;
        return toDate(str, "yyyy-MM-dd");
    }

    public static Date toDate(String str, String formatStr) {
        boolean isDate = isDate(str);
        if (isDate) {
            DateFormat df = new SimpleDateFormat(formatStr);
            try {
                return df.parse(str);
            } catch (Exception err) {
                System.out.print(str + err);
                Calendar cal = Calendar.getInstance();
                return cal.getTime();
            }
        } else {
            Calendar cal = Calendar.getInstance();
            return cal.getTime();
        }
    }

    public static String getRandom() {
        Random random = new Random();
        int i = random.nextInt(10000);
        String radomid = String.valueOf(i);
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime) + radomid;
        return dateString;
    }

    public static Boolean toBollean(Object bl) {
        String str = toStr(bl).toLowerCase();
        if (isEmpty(str) || str.equals("0") || str.equals("false"))
            return false;
        else
            return true;

    }

    public static String substring(String str, int start, int length) {
        if (str.length() > length) {
            return str.substring(start, start + length);
        } else
            return str;
    }

    public static String substring2(String str, int start, int length) {
        if (str.length() > length) {
            return str.substring(start, start + length) + "...";
        } else
            return str;
    }

    public static String substring(String str, int start) {
        return substring(str, start, str.length() - start);
    }

    public static String trim(String str) {
        return trim(trim(str, " ", 1), " ", -1);
    }

    public static String trimStart(String str, String stripChars) {
        return trim(str, stripChars, -1);
    }

    public static String trimEnd(String str, String stripChars) {
        return trim(str, stripChars, 1);
    }

    public static String trim(String str, String stripChars) {
        return trim(str, stripChars, 0);
    }

    private static String trim(String str, String stripChars, int mode) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        int start = 0;
        int end = length;
        // 扫描字符串头部
        if (mode <= 0) {
            if (stripChars == null) {
                while ((start < end)
                        && (Character.isWhitespace(str.charAt(start)))) {
                    start++;
                }
            } else if (stripChars.length() == 0) {
                return str;
            } else {
                while ((start < end)
                        && (stripChars.indexOf(str.charAt(start)) != -1)) {
                    start++;
                }
            }
        }
        // 扫描字符串尾部
        if (mode >= 0) {
            if (stripChars == null) {
                while ((start < end)
                        && (Character.isWhitespace(str.charAt(end - 1)))) {
                    end--;
                }
            } else if (stripChars.length() == 0) {
                return str;
            } else {
                while ((start < end)
                        && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
                    end--;
                }
            }
        }
        if ((start > 0) || (end < length)) {
            return str.substring(start, end);
        }
        return str;
    }

    // source中是否包括key
    public static boolean isContains(String key, String source) {
        if (isEmpty(key) || isEmpty(source))
            return false;
        else {
            source = source.replaceAll(" ", "");
            if (("," + source + ",").contains("," + key + ","))
                return true;
            else
                return false;
        }
    }

    public static String formatByTag(String[] arr) {
        String filter = "";
        for (int i = 0; i < arr.length; i++) {
            if (filter == "")
                filter += "'" + arr[i].trim() + "'";
            else
                filter += ",'" + arr[i].trim() + "'";
        }
        return filter;
    }

    public static String formatByTag(String str) {
        if (isEmpty(str))
            return "";
        String[] arr = str.split(",");
        return formatByTag(arr);
    }

    public static String getFormatLeft(String name, Integer level,
                                       String formatStr) {
        //level--;// 第一位不处理
        for (int i = 0; i < level; i++) {
            name = formatStr + name;
        }
        return name;
    }

    public static String getFirstLower(String sName) {
        if (!isEmpty(sName)) {
            return sName.substring(0, 1).toLowerCase() + sName.substring(1);
        } else
            return "";
    }

    // 取得最后一层包名
    public static String getPackName(String tableName) {
        String[] array = tableName.split("_");
        return array[0].toLowerCase();
    }

    public static String getServiceName(String tableName) {
        return StringUtil.getEntityName(tableName) + "Service";
    }

    public static String getMapperName(String tableName) {
        return StringUtil.getEntityName(tableName) + "Mapper";
    }

    // 取得实体类名
    public static String getEntityName(String tableName) {
        String[] array = tableName.split("_");
        String newColName = "";
        for (int i = 0; i < array.length; i++) {
            if (StringUtil.isEmpty(array[i]))
                continue;
            if (i != 0)
                newColName += array[i].substring(0, 1).toUpperCase()
                        + array[i].substring(1);
            else
                newColName += array[i];
        }
        return newColName;
    }

    public static String getFiledName(String tableName) {
        if (StringUtil.isEmpty(tableName))
            return "";
        tableName = getEntityName(tableName);
        return tableName.substring(0, 1).toLowerCase() + tableName.substring(1);
    }

    // \U解码
    public static String unescapeUnicode(String str) {
        StringBuffer b = new StringBuffer();
        Matcher m = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(str);
        while (m.find())
            b.append((char) Integer.parseInt(m.group(1), 16));
        return b.toString();
    }

    public static boolean compare(String str, String shortstr) {
        if (isEmpty(str) || isEmpty(shortstr) || str.indexOf(shortstr) < 0)
            return false;
        return true;
    }

    public static String paddingLeftStr(String str, String by) {
        if (!StringUtil.isEmpty(str)) {
            int len = by.length() - 2;
            for (int i = 0; i < len; i++)
                str = "--" + str;
            return str;
        }
        return "";
    }

    public static List<String> getKeys(String val) {
        List<String> list = new ArrayList<String>();
        if (!isEmpty(val)) {
            String[] ids = val.split(",");
            for (String id : ids) {
                list.add(id);
            }
        }
        return list;
    }

    public static String encode(String encoding) {
        try {
            byte[] b = encoding.getBytes("GBK");
            encoding = new String(b, "8859_1");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return encoding;
    }

    public static String encodeUrl(String encoding) {
        if (StringUtil.isNotEmpty(encoding)) {
            try {
                encoding = URLDecoder.decode(URLEncoder.encode(encoding, "ISO8859-1"), "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return encoding;
    }

    public static boolean isMod(Integer src, Integer mod) {
        if (mod != null && src != null) {
            if (src % mod == 0)
                return true;
        }
        return false;
    }

    public static String getColumn(Integer num, Integer len) {
        if (num != null && len != null) {
            return StringUtil.paddingLeft(toStr(num), len, '0');
        }
        return "";
    }

    //0d98315b-ddac-4718-ab7b-301e3cdcf5f5
    public static String getUuid(String UUID) {
        String[] uuid = UUID.split("-");
        StringBuffer sb = new StringBuffer();
        for (String str : uuid) {
            sb.append(str);
        }
        String str = sb.toString();
        str = str.trim().substring(1, 13);
        return str;
    }

    /**
     * 把大写字母转换成首字母大写
     */
    public static String toLowerCase(String str) {
        if (isEmpty(str))
            return null;
        return new StringBuffer().append(str.charAt(0)).append(str.substring(1).toLowerCase()).toString();
    }

    /**
     * 把大写字母转换成小写字母
     */
    public static String toLower(String str) {
        if (isEmpty(str))
            return null;
        return str.toLowerCase().toString();
    }

    /**
     * 字符串换行
     */
    private static StringBuffer sb;

    public static String newLine(String str, int length) {
        if (!isEmpty(str)) {
            sb = new StringBuffer();
            if (str.length() > 10) {
                if (length > 5) {
                    subStr(str, length);
                    return sb.toString();
                } else {
                    subStr(str, 10);
                    return sb.toString();
                }
            } else {
                return str;
            }
        }
        return str;
    }


    public static String subStr(String str, int length) {
        if (!isEmpty(str)) {
            if (str.length() > length) {
                sb.append(str.substring(0, length)).append("<br />");
                subStr(str.substring(length), length);
            } else {
                sb.append(str);
            }
        }
        return sb.toString();
    }


    /**
     * 将数字金额转大写(精确到小数点后两位)
     *
     * @param money
     * @return
     */
    public static String hangeToBig(double money) {
        char[] hunit = {'拾', '佰', '仟'}; // 段内位置表示
        char[] vunit = {'万', '亿'}; // 段名表示
        char[] digit = {'零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'}; // 数字表示
        long midVal = (long) (money * 100); // 转化成整形
        String valStr = String.valueOf(midVal); // 转化成字符串
        String head = valStr.substring(0, valStr.length() - 2); // 取整数部分
        String rail = valStr.substring(valStr.length() - 2); // 取小数部分
        String prefix = ""; // 整数部分转化的结果
        String suffix = ""; // 小数部分转化的结果
        // 处理小数点后面的数
        if (rail.equals("00")) { // 如果小数部分为0
            suffix = "整";
        } else {
            suffix = digit[rail.charAt(0) - '0'] + "角" + digit[rail.charAt(1) - '0'] + "分"; // 否则把角分转化出来
        }
        // 处理小数点前面的数
        char[] chDig = head.toCharArray(); // 把整数部分转化成字符数组
        char zero = '0'; // 标志'0'表示出现过0
        byte zeroSerNum = 0; // 连续出现0的次数
        for (int i = 0; i < chDig.length; i++) { // 循环处理每个数字
            int idx = (chDig.length - i - 1) % 4; // 取段内位置
            int vidx = (chDig.length - i - 1) / 4; // 取段位置
            if (chDig[i] == '0') { // 如果当前字符是0
                zeroSerNum++; // 连续0次数递增
                if (zero == '0') { // 标志
                    zero = digit[0];
                } else if (idx == 0 && vidx > 0 && zeroSerNum < 4) {
                    prefix += vunit[vidx - 1];
                    zero = '0';
                }
                continue;
            }
            zeroSerNum = 0; // 连续0次数清零
            if (zero != '0') { // 如果标志不为0,则加上,例如万,亿什么的
                prefix += zero;
                zero = '0';
            }
            prefix += digit[chDig[i] - '0']; // 转化该数字表示
            if (idx > 0)
                prefix += hunit[idx - 1];
            if (idx == 0 && vidx > 0) {
                prefix += vunit[vidx - 1]; // 段结束位置应该加上段名如万,亿
            }
        }
        if (prefix.length() > 0)
            prefix += '圆'; // 如果整数部分存在,则有圆的字样
        return prefix + suffix; // 返回正确表示
    }

    public static Date getDateForTomr(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
		/*calendar.add(calendar.YEAR, 1);//把日期往后增加一年.整数往后推,负数往前移动
		calendar.add(calendar.DAY_OF_MONTH, 1);//把日期往后增加一个月.整数往后推,负数往前移动
		calendar.add(calendar.WEEK_OF_MONTH, 1);//把日期往后增加一个月.整数往后推,负数往前移动*/
        calendar.add(calendar.DATE, 1);//把日期往后增加一天.整数往后推,负数往前移动
        date = calendar.getTime();   //这个时间就是日期往后推一天的结果
        return date;
    }

    public static String getDateForTomr(String date) {
        String[] split = date.split("-");
        if (split.length >= 3) {
            String dt = "";
            int i = Integer.parseInt(split[2]) + 1;
            if ((i + "").length() < 2)
                dt = split[0] + "-" + split[1] + "-0" + i;
            else
                dt = split[0] + "-" + split[1] + "-" + i;
            return dt;
        }
        return date;
    }

    public static boolean isIgnoreEqual(String str1, String str2) {
        if (str1 != null && str1.equalsIgnoreCase(str2))
            return true;
        else
            return false;
    }

    public static String getTableNameCh(String Num) {
        if (!isEmpty(Num)) {
            if (isEqual("1", Num)) return "检测人员";
            else if (isEqual("2", Num)) return "检测设备";
            else if (isEqual("3", Num)) return "检测线";
            else if (isEqual("4,5,6", Num)) return "机动车";
        }
        return Num;
    }

    public static String getSfxr(String zt) {
        if ("0".equals(zt))
            return "信任";
        if ("1".equals(zt))
            return "不信任";
        return "";
    }
}