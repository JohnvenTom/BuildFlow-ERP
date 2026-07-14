package com.buildflow.erp.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 * 封装分页查询返回数据，包含总记录数和当前页数据列表
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页数据列表 */
    private List<T> rows;

    public PageResult() {}

    /**
     * 构造分页结果
     * @param total 总记录数
     * @param rows 当前页数据列表
     */
    public PageResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }
}
