package com.buildflow.erp.common.exception;

/**
 * 全局错误码枚举
 * 5位错误码：前2位模块编号，后3位业务编号
 * 模块编号：10-系统, 11-CRM, 12-采购, 13-销售, 14-仓储, 15-财务, 20-认证, 21-权限
 */
public enum ErrorCode {

    // ========== 系统级（10xxx） ==========
    SUCCESS(20000, "操作成功"),
    SYSTEM_ERROR(10000, "系统内部错误"),
    PARAM_INVALID(10001, "参数校验失败"),
    PARAM_MISSING(10002, "必要参数缺失"),
    DATA_NOT_FOUND(10003, "数据不存在"),
    DATA_DUPLICATE(10004, "数据已存在"),
    OPERATION_FAILED(10005, "操作失败"),

    // ========== 认证模块（20xxx） ==========
    LOGIN_FAILED(20001, "用户名或密码错误"),
    ACCOUNT_LOCKED(20002, "账号已被锁定"),
    ACCOUNT_DISABLED(20003, "账号已被禁用"),
    TOKEN_EXPIRED(20004, "登录已过期，请重新登录"),
    TOKEN_INVALID(20005, "无效的认证令牌"),
    SIGN_ERROR(20006, "接口签名校验失败"),
    SIGN_EXPIRED(20007, "请求已过期"),
    NONCE_DUPLICATE(20008, "请勿重复请求"),

    // ========== 权限模块（21xxx） ==========
    NO_PERMISSION(21001, "没有操作权限"),
    ROLE_NOT_FOUND(21002, "角色不存在"),

    // ========== CRM模块（11xxx） ==========
    CUSTOMER_NOT_FOUND(11001, "客户不存在"),
    CUSTOMER_HAS_ORDER(11002, "客户存在业务单据，无法删除"),
    CREDIT_LIMIT_EXCEEDED(11003, "订单金额超出客户信用额度"),
    SUPPLIER_NOT_FOUND(11004, "供应商不存在"),

    // ========== 采购模块（12xxx） ==========
    PURCHASE_ORDER_NOT_FOUND(12001, "采购订单不存在"),
    PURCHASE_ORDER_AUDITED(12002, "采购订单已审核，无法修改"),
    STOCK_IN_FAILED(12003, "采购入库失败"),

    // ========== 销售模块（13xxx） ==========
    SALES_ORDER_NOT_FOUND(13001, "销售订单不存在"),
    STOCK_INSUFFICIENT(13002, "库存不足，无法出库"),
    DELIVERY_EXCEED_ORDER(13003, "出库数量超出订单未发货数量"),

    // ========== 仓储模块（14xxx） ==========
    PRODUCT_NOT_FOUND(14001, "商品不存在"),
    WAREHOUSE_NOT_FOUND(14002, "仓库不存在"),
    STOCK_TRANSFER_FAILED(14003, "调拨失败"),
    STOCK_CHECK_FAILED(14004, "盘点审核失败"),

    // ========== 财务模块（15xxx） ==========
    RECEIVABLE_NOT_FOUND(15001, "应收记录不存在"),
    PAYABLE_NOT_FOUND(15002, "应付记录不存在"),
    PAYMENT_EXCEED_DEBT(15003, "回款金额超出欠款金额"),

    // ========== 限流（30xxx） ==========
    RATE_LIMIT_EXCEEDED(30001, "请求过于频繁，请稍后再试"),
    IDEMPOTENT_REPEAT(30002, "请勿重复提交");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
