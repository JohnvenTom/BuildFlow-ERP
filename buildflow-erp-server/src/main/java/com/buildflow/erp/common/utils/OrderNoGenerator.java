package com.buildflow.erp.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单据编号生成器
 * 生成规则：业务类型前缀 + 年月日 + 4位流水号，如 PO202607140001
 * 注意：此实现为单机版本，使用AtomicInteger保证线程安全
 */
public class OrderNoGenerator {

    /** 流水号计数器，每天重置 */
    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    /** 上次生成编号的日期，用于判断是否需要重置流水号 */
    private static String lastDate = "";

    /** 业务类型前缀常量 */
    public static final String PURCHASE_ORDER = "PO";       // 采购订单
    public static final String STOCK_IN = "SI";             // 入库单
    public static final String PURCHASE_RETURN = "PR";      // 采购退货
    public static final String SALES_ORDER = "SO";          // 销售订单
    public static final String DELIVERY = "DO";             // 出库单
    public static final String SALES_RETURN = "SR";         // 销售退货
    public static final String STOCK_TRANSFER = "ST";       // 调拨单
    public static final String STOCK_CHECK = "SC";          // 盘点单
    public static final String PAYMENT_RECEIVE = "RC";      // 回款单
    public static final String PAYMENT_PAY = "PP";          // 付款单

    /**
     * 生成单据编号
     * @param prefix 业务类型前缀（使用本类常量）
     * @return 唯一单据编号，格式如 PO202607140001
     */
    public static synchronized String generate(String prefix) {
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // 日期变更时重置流水号
        if (!currentDate.equals(lastDate)) {
            lastDate = currentDate;
            SEQUENCE.set(1);
        }

        int seq = SEQUENCE.getAndIncrement();
        return prefix + currentDate + String.format("%04d", seq);
    }
}
