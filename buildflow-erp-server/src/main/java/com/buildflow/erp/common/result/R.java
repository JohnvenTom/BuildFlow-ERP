package com.buildflow.erp.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体
 * 封装所有接口返回格式，保证前后端数据交互一致性
 *
 * @param <T> 响应数据泛型
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 响应数据 */
    private T data;

    private R() {}

    /**
     * 成功响应（无数据）
     * @return 无数据的成功响应
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应（带数据）
     * @param data 响应数据
     * @return 带数据的成功响应
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    /**
     * 成功响应（自定义消息）
     * @param msg 提示信息
     * @param data 响应数据
     * @return 带自定义消息的成功响应
     */
    public static <T> R<T> ok(String msg, T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    /**
     * 失败响应
     * @param msg 错误信息
     * @return 失败响应
     */
    public static <T> R<T> fail(String msg) {
        return fail(500, msg);
    }

    /**
     * 失败响应（自定义状态码）
     * @param code 状态码
     * @param msg 错误信息
     * @return 带自定义状态码的失败响应
     */
    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
