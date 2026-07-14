package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsProduct;
import com.buildflow.erp.service.WmsProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品档案管理控制器
 * 提供商品档案的分页查询、新增、编辑、删除及全部商品列表查询等接口
 */
@RestController
@RequestMapping("/warehouse/product")
public class WmsProductController {

    @Autowired
    private WmsProductService wmsProductService;

    /**
     * 分页查询商品列表
     * 支持按商品名称模糊搜索、商品分类过滤
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param name      商品名称（模糊查询），可为空
     * @param category  商品分类，可为空
     * @return 分页结果，包含总记录数和当前页商品列表
     */
    @PreAuthorize("@ps.hasPermission('wms:product:list')")
    @GetMapping("/page")
    public R<PageResult<WmsProduct>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        return wmsProductService.page(pageNum, pageSize, name, category);
    }

    /**
     * 新增商品
     * 校验商品名称唯一性后插入数据库
     *
     * @param product 商品实体对象，需包含name、category等字段
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('wms:product:add')")
    @PostMapping
    public R<Void> add(@RequestBody WmsProduct product) {
        return wmsProductService.add(product);
    }

    /**
     * 编辑商品信息
     *
     * @param product 商品实体对象，id字段必填
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('wms:product:edit')")
    @PutMapping
    public R<Void> update(@RequestBody WmsProduct product) {
        return wmsProductService.update(product);
    }

    /**
     * 删除商品
     * 逻辑删除
     *
     * @param id 商品ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('wms:product:delete')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        return wmsProductService.delete(id);
    }

    /**
     * 查询全部商品列表
     * 用于下拉选择场景，返回所有未删除的商品
     *
     * @return 全部商品列表
     */
    @PreAuthorize("@ps.hasPermission('wms:product:list')")
    @GetMapping("/list")
    public R<List<WmsProduct>> listAll() {
        return wmsProductService.listAll();
    }
}
