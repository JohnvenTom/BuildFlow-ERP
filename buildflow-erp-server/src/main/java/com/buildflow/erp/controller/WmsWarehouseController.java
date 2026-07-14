package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsWarehouse;
import com.buildflow.erp.service.WmsWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库档案管理控制器
 * 提供仓库档案的分页查询、新增、编辑、删除及全部仓库列表查询等接口
 */
@RestController
@RequestMapping("/warehouse/warehouse")
public class WmsWarehouseController {

    @Autowired
    private WmsWarehouseService wmsWarehouseService;

    /**
     * 分页查询仓库列表
     * 支持按仓库名称模糊搜索、仓库类型过滤
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param name      仓库名称（模糊查询），可为空
     * @param type      仓库类型，可为空
     * @return 分页结果，包含总记录数和当前页仓库列表
     */
    @PreAuthorize("@ps.hasPermission('wms:warehouse:list')")
    @GetMapping("/page")
    public R<PageResult<WmsWarehouse>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type) {
        return wmsWarehouseService.page(pageNum, pageSize, name, type);
    }

    /**
     * 新增仓库
     * 校验仓库名称唯一性后插入数据库
     *
     * @param warehouse 仓库实体对象，需包含name、type等字段
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('wms:warehouse:add')")
    @PostMapping
    public R<Void> add(@RequestBody WmsWarehouse warehouse) {
        return wmsWarehouseService.add(warehouse);
    }

    /**
     * 编辑仓库信息
     *
     * @param warehouse 仓库实体对象，id字段必填
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('wms:warehouse:edit')")
    @PutMapping
    public R<Void> update(@RequestBody WmsWarehouse warehouse) {
        return wmsWarehouseService.update(warehouse);
    }

    /**
     * 删除仓库
     * 逻辑删除
     *
     * @param id 仓库ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('wms:warehouse:delete')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        return wmsWarehouseService.delete(id);
    }

    /**
     * 查询全部仓库列表
     * 用于下拉选择场景，返回所有未删除的仓库
     *
     * @return 全部仓库列表
     */
    @PreAuthorize("@ps.hasPermission('wms:warehouse:list')")
    @GetMapping("/list")
    public R<List<WmsWarehouse>> listAll() {
        return wmsWarehouseService.listAll();
    }
}
