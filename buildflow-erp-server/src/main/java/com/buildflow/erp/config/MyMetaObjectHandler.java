package com.buildflow.erp.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>
 * 实现公共字段的自动填充，包括创建时间和更新时间。
 * </p>
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     * <p>
     * 自动填充 createTime 和 updateTime 字段为当前时间。
     * </p>
     *
     * @param metaObject MyBatis-Plus 元对象，用于获取和设置实体字段值
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时自动填充
     * <p>
     * 自动填充 updateTime 字段为当前时间。
     * </p>
     *
     * @param metaObject MyBatis-Plus 元对象，用于获取和设置实体字段值
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
