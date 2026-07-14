package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsProduct;
import com.buildflow.erp.mapper.WmsProductMapper;
import com.buildflow.erp.service.WmsProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品档案服务实现类
 * 实现商品档案管理的核心业务逻辑，包括分页查询、新增、编辑、删除、全部列表查询等操作。
 */
@Service
public class WmsProductServiceImpl implements WmsProductService {

    @Autowired
    private WmsProductMapper wmsProductMapper;

    /**
     * 分页查询商品列表
     * 支持按商品名称模糊搜索、商品分类过滤，按创建时间倒序排列
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param name      商品名称（模糊查询），可为null
     * @param category  商品分类，可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页商品列表
     */
    @Override
    public R<PageResult<WmsProduct>> page(Integer pageNum, Integer pageSize, String name, String category) {
        Page<WmsProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), WmsProduct::getName, name)
                .eq(StringUtils.hasText(category), WmsProduct::getCategory, category)
                .orderByDesc(WmsProduct::getCreateTime);
        Page<WmsProduct> result = wmsProductMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 新增商品
     * 校验商品名称唯一性后插入数据库
     *
     * @param product 商品实体对象，需包含name、category等字段
     * @return 操作结果，商品名称已存在时返回R.fail()
     */
    @Override
    public R<Void> add(WmsProduct product) {
        // 校验商品名称是否已存在
        LambdaQueryWrapper<WmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmsProduct::getName, product.getName());
        if (wmsProductMapper.selectCount(wrapper) > 0) {
            return R.fail("商品名称已存在");
        }
        wmsProductMapper.insert(product);
        return R.ok();
    }

    /**
     * 编辑商品信息
     *
     * @param product 商品实体对象，id字段必填
     * @return 操作结果，商品不存在时返回R.fail()
     */
    @Override
    public R<Void> update(WmsProduct product) {
        WmsProduct existing = wmsProductMapper.selectById(product.getId());
        if (existing == null) {
            return R.fail("商品不存在");
        }
        wmsProductMapper.updateById(product);
        return R.ok();
    }

    /**
     * 删除商品
     * 逻辑删除，由@TableLogic自动处理
     *
     * @param id 商品ID
     * @return 操作结果，商品不存在时返回R.fail()
     */
    @Override
    public R<Void> delete(Long id) {
        WmsProduct existing = wmsProductMapper.selectById(id);
        if (existing == null) {
            return R.fail("商品不存在");
        }
        wmsProductMapper.deleteById(id);
        return R.ok();
    }

    /**
     * 查询全部商品列表
     * 用于下拉选择场景，返回所有未删除的商品，按创建时间倒序排列
     *
     * @return 全部商品列表
     */
    @Override
    public R<List<WmsProduct>> listAll() {
        LambdaQueryWrapper<WmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(WmsProduct::getCreateTime);
        List<WmsProduct> list = wmsProductMapper.selectList(wrapper);
        return R.ok(list);
    }
}
