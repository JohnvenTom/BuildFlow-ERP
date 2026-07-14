package com.buildflow.erp.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志脱敏工具类
 * 对手机号、密码、身份证、姓名、金额等敏感信息进行脱敏处理，
 * 用于操作日志、接口日志等场景，防止敏感数据明文落库。
 */
public class LogMaskUtil {

    /** 手机号正则：11位数字，以1开头 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

    /** 身份证正则：15位或18位（最后一位可能是X） */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{15}$|^\\d{17}[0-9Xx]$");

    /** JSON中password字段正则（匹配 "password":"value" 形式） */
    private static final Pattern JSON_PASSWORD_PATTERN =
            Pattern.compile("(\"(?:password|passwd|pwd)\"\\s*:\\s*)\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);

    /** JSON中phone字段正则（匹配 "phone":"value" 形式） */
    private static final Pattern JSON_PHONE_PATTERN =
            Pattern.compile("(\"(?:phone|mobile|telephone)\"\\s*:\\s*)\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);

    /** JSON中idCard字段正则（匹配 "idCard":"value" 形式） */
    private static final Pattern JSON_ID_CARD_PATTERN =
            Pattern.compile("(\"(?:idCard|idcard|idNo|id_no|cardNo)\"\\s*:\\s*)\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);

    private LogMaskUtil() {
    }

    /**
     * 手机号脱敏：保留前3位和后4位，中间用4个*代替
     * 例如：13812340001 → 138****0001
     *
     * @param phone 原始手机号
     * @return 脱敏后的手机号；入参为空或不合规时原样返回
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 密码脱敏：统一替换为6个*
     *
     * @param value 原始密码
     * @return 固定返回 ******；入参为空时原样返回
     */
    public static String maskPassword(String value) {
        if (value == null) {
            return null;
        }
        return "******";
    }

    /**
     * 身份证脱敏：保留前3位和后4位，中间用*代替
     * 例如：110101199001011234 → 110***********1234
     *
     * @param idCard 原始身份证号
     * @return 脱敏后的身份证号；入参为空或不合规时原样返回
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return idCard;
        }
        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return idCard;
        }
        int len = idCard.length();
        String prefix = idCard.substring(0, 3);
        String suffix = idCard.substring(len - 4);
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < len - 7; i++) {
            mask.append("*");
        }
        return prefix + mask + suffix;
    }

    /**
     * 姓名脱敏：
     * 两字姓名保留首字，末字用*代替，如：张三 → 张*
     * 三字及以上保留首尾，中间用*代替，如：张三丰 → 张*丰，诸葛亮 → 诸*亮
     *
     * @param name 原始姓名
     * @return 脱敏后的姓名；入参为空或单字时原样返回
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        int len = name.length();
        if (len == 1) {
            return name;
        }
        if (len == 2) {
            return name.charAt(0) + "*";
        }
        // 三字及以上：保留首尾，中间用*代替
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < len - 2; i++) {
            mask.append("*");
        }
        return name.charAt(0) + mask.toString() + name.charAt(len - 1);
    }

    /**
     * 金额脱敏：统一替换为3个*
     * 适用于财务敏感场景，避免金额明文落库
     *
     * @param amount 原始金额（字符串形式）
     * @return 固定返回 ***；入参为空时原样返回
     */
    public static String maskAmount(String amount) {
        if (amount == null) {
            return null;
        }
        return "***";
    }

    /**
     * JSON字符串脱敏：自动识别 password/phone/idCard 字段并脱敏
     * 处理规则：
     *   - password/passwd/pwd 字段值 → ***（密码脱敏）
     *   - phone/mobile/telephone 字段值 → 138****0001（手机号脱敏）
     *   - idCard/idNo/cardNo 字段值 → 110***********1234（身份证脱敏）
     *
     * @param json 原始JSON字符串
     * @return 脱敏后的JSON字符串；入参为空时原样返回
     */
    public static String maskJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        String result = json;
        // 密码字段脱敏
        result = replaceByPattern(result, JSON_PASSWORD_PATTERN, "******");
        // 手机号字段脱敏（逐个匹配，按手机号规则脱敏）
        result = maskJsonField(result, JSON_PHONE_PATTERN);
        // 身份证字段脱敏（逐个匹配，按身份证规则脱敏）
        result = maskJsonField(result, JSON_ID_CARD_PATTERN);
        return result;
    }

    /**
     * 按正则匹配JSON字段，将字段值统一替换为固定脱敏值
     *
     * @param json    原始JSON字符串
     * @param pattern 字段匹配正则（第1组为键名部分，第2组为原值）
     * @param maskVal 脱敏后的固定值
     * @return 脱敏后的JSON字符串
     */
    private static String replaceByPattern(String json, Pattern pattern, String maskVal) {
        Matcher matcher = pattern.matcher(json);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            // 保留键名部分，值替换为脱敏值
            matcher.appendReplacement(sb, matcher.group(1) + "\"" + maskVal + "\"");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 按正则匹配JSON字段，对每个字段值按类型规则逐个脱敏
     * 用于手机号、身份证等需要保留部分明文的字段
     *
     * @param json    原始JSON字符串
     * @param pattern 字段匹配正则（第1组为键名部分，第2组为原值）
     * @return 脱敏后的JSON字符串
     */
    private static String maskJsonField(String json, Pattern pattern) {
        Matcher matcher = pattern.matcher(json);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String keyPart = matcher.group(1);
            String value = matcher.group(2);
            String masked;
            if (pattern == JSON_PHONE_PATTERN) {
                masked = maskPhone(value);
            } else {
                masked = maskIdCard(value);
            }
            matcher.appendReplacement(sb, keyPart + "\"" + masked + "\"");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
