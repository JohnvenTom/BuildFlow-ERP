package com.buildflow.erp.common.constants;

/**
 * 系统常量
 * 定义系统级别的通用常量，避免硬编码
 */
public class Constants {

    /** 单据状态：草稿 */
    public static final String ORDER_STATUS_DRAFT = "0";

    /** 单据状态：已审核 */
    public static final String ORDER_STATUS_AUDITED = "1";

    /** 单据状态：已作废 */
    public static final String ORDER_STATUS_VOID = "2";

    /** 逻辑删除：已删除 */
    public static final Integer IS_DELETED = 1;

    /** 逻辑删除：未删除 */
    public static final Integer NOT_DELETED = 0;

    /** 账号状态：正常 */
    public static final Integer USER_STATUS_NORMAL = 1;

    /** 账号状态：禁用 */
    public static final Integer USER_STATUS_DISABLED = 0;

    /** 密码错误最大次数 */
    public static final int PASSWORD_MAX_RETRY = 5;

    /** 账号锁定时间（分钟） */
    public static final int ACCOUNT_LOCK_MINUTES = 10;

    /** 默认分页页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大分页大小 */
    public static final int MAX_PAGE_SIZE = 100;

    /** 往来款状态：正常 */
    public static final String FIN_STATUS_NORMAL = "0";

    /** 往来款状态：逾期 */
    public static final String FIN_STATUS_OVERDUE = "1";

    /** 往来款状态：已结清 */
    public static final String FIN_STATUS_SETTLED = "2";

    private Constants() {}
}
