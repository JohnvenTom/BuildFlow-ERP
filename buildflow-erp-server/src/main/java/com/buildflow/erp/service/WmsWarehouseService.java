package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsWarehouse;

import java.util.List;

/**
 * 仓库档案服务接口
 * 定义仓库档案管理的核心业务方法，包括分页查询、增删改查、全部仓库列表查询等操作。
 */
public interface WmsWarehouseService {

    /**
     * 分页查询仓库列表
     * 支持按仓库名称模糊搜索、仓库类型过滤
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param name      仓库名称（模糊查询），可为null
     * @param type      仓库类型，可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页仓库列表
     */
    R<PageResult<WmsWarehouse>> page(Integer pageNum, Integer pageSize, String name, String type);

    /**
     * 新增仓库
     * 校验仓库名称唯一性后插入数据库
     *
     * @param warehouse 仓库实体对象，需包含name、type等字段
     * @return 操作结果，仓库名称已存在时返回R.fail()
     */
    R<Void> add(WmsWarehouse warehouse);

    /**
     * 编辑仓库信息
     *
     * @param warehouse 仓库实体对象，id字段必填
     * @return 操作结果，仓库不存在时返回R.fail()
     */
    R<Void> update(WmsWarehouse warehouse);

    /**
     * 删除仓库
     * 逻辑删除，由@TableLogic自动处理
     *
     * @param id 仓库ID
     * @return 操作结果，仓库不存在时返回R.fail()
     */
    R<Void> delete(Long id);

    /**
     * 查询全部仓库列表
     * 用于下拉选择场景，返回所有未删除的仓库
     *
     * @return 全部仓库列表
     */
    R<List<WmsWarehouse>> listAll();
}
