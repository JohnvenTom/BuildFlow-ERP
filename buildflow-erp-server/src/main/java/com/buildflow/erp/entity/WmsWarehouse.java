package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库档案实体
 * 对应数据库表 wms_warehouse，映射仓库基本信息、类型、地址、负责人等字段。
 * 用于库存归属、调拨流转、盘点等业务场景。
 *
 * @TableName wms_warehouse
 */
@Data
@TableName("wms_warehouse")
public class WmsWarehouse {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 仓库名称 */
    private String name;

    /** 仓库类型（如：原料仓、成品仓、次品仓等） */
    private String type;

    /** 仓库地址 */
    private String address;

    /** 负责人ID，关联 sys_user.id */
    private Long managerId;

    /** 管理员名称（非数据库字段，关联查询 sys_user.realName 填充） */
    @TableField(exist = false)
    private String managerName;

    /** 逻辑删除标识（0-未删除 1-已删除） */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间，新增时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，新增和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
