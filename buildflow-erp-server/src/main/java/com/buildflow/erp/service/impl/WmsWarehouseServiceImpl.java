package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsWarehouse;
import com.buildflow.erp.mapper.WmsWarehouseMapper;
import com.buildflow.erp.service.WmsWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 仓库档案服务实现类
 * 实现仓库档案管理的核心业务逻辑，包括分页查询、新增、编辑、删除、全部列表查询等操作。
 */
@Service
public class WmsWarehouseServiceImpl implements WmsWarehouseService {

    @Autowired
    private WmsWarehouseMapper wmsWarehouseMapper;

    /**
     * 分页查询仓库列表
     * 支持按仓库名称模糊搜索、仓库类型过滤，按创建时间倒序排列
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param name      仓库名称（模糊查询），可为null
     * @param type      仓库类型，可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页仓库列表
     */
    @Override
    public R<PageResult<WmsWarehouse>> page(Integer pageNum, Integer pageSize, String name, String type) {
        Page<WmsWarehouse> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WmsWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), WmsWarehouse::getName, name)
                .eq(StringUtils.hasText(type), WmsWarehouse::getType, type)
                .orderByDesc(WmsWarehouse::getCreateTime);
        Page<WmsWarehouse> result = wmsWarehouseMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 新增仓库
     * 校验仓库名称唯一性后插入数据库
     *
     * @param warehouse 仓库实体对象，需包含name、type等字段
     * @return 操作结果，仓库名称已存在时返回R.fail()
     */
    @Override
    public R<Void> add(WmsWarehouse warehouse) {
        // 校验仓库名称是否已存在
        LambdaQueryWrapper<WmsWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmsWarehouse::getName, warehouse.getName());
        if (wmsWarehouseMapper.selectCount(wrapper) > 0) {
            return R.fail("仓库名称已存在");
        }
        wmsWarehouseMapper.insert(warehouse);
        return R.ok();
    }

    /**
     * 编辑仓库信息
     *
     * @param warehouse 仓库实体对象，id字段必填
     * @return 操作结果，仓库不存在时返回R.fail()
     */
    @Override
    public R<Void> update(WmsWarehouse warehouse) {
        WmsWarehouse existing = wmsWarehouseMapper.selectById(warehouse.getId());
        if (existing == null) {
            return R.fail("仓库不存在");
        }
        wmsWarehouseMapper.updateById(warehouse);
        return R.ok();
    }

    /**
     * 删除仓库
     * 逻辑删除，由@TableLogic自动处理
     *
     * @param id 仓库ID
     * @return 操作结果，仓库不存在时返回R.fail()
     */
    @Override
    public R<Void> delete(Long id) {
        WmsWarehouse existing = wmsWarehouseMapper.selectById(id);
        if (existing == null) {
            return R.fail("仓库不存在");
        }
        wmsWarehouseMapper.deleteById(id);
        return R.ok();
    }

    /**
     * 查询全部仓库列表
     * 用于下拉选择场景，返回所有未删除的仓库，按创建时间倒序排列
     *
     * @return 全部仓库列表
     */
    @Override
    public R<List<WmsWarehouse>> listAll() {
        LambdaQueryWrapper<WmsWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(WmsWarehouse::getCreateTime);
        List<WmsWarehouse> list = wmsWarehouseMapper.selectList(wrapper);
        return R.ok(list);
    }
}
