package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsProduct;

import java.util.List;

/**
 * 商品档案服务接口
 * 定义商品档案管理的核心业务方法，包括分页查询、增删改查、全部商品列表查询等操作。
 */
public interface WmsProductService {

    /**
     * 分页查询商品列表
     * 支持按商品名称模糊搜索、商品分类过滤
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param name      商品名称（模糊查询），可为null
     * @param category  商品分类，可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页商品列表
     */
    R<PageResult<WmsProduct>> page(Integer pageNum, Integer pageSize, String name, String category);

    /**
     * 新增商品
     * 校验商品名称唯一性后插入数据库
     *
     * @param product 商品实体对象，需包含name、category等字段
     * @return 操作结果，商品名称已存在时返回R.fail()
     */
    R<Void> add(WmsProduct product);

    /**
     * 编辑商品信息
     *
     * @param product 商品实体对象，id字段必填
     * @return 操作结果，商品不存在时返回R.fail()
     */
    R<Void> update(WmsProduct product);

    /**
     * 删除商品
     * 逻辑删除，由@TableLogic自动处理
     *
     * @param id 商品ID
     * @return 操作结果，商品不存在时返回R.fail()
     */
    R<Void> delete(Long id);

    /**
     * 查询全部商品列表
     * 用于下拉选择场景，返回所有未删除的商品
     *
     * @return 全部商品列表
     */
    R<List<WmsProduct>> listAll();
}
