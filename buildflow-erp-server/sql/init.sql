-- ================================================
-- BuildFlow ERP 建材进销存CRM系统 数据库初始化脚本
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- ================================================

CREATE DATABASE IF NOT EXISTS buildflow_erp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE buildflow_erp;

-- ================================================
-- 1. 系统基础表
-- ================================================

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码（MD5加盐加密）',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    login_fail_count INT DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until DATETIME DEFAULT NULL COMMENT '锁定截止时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='用户账号表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色表';

-- 菜单权限表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0为顶级菜单',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    icon VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
    sort INT DEFAULT 0 COMMENT '排序序号',
    permission VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    menu_type TINYINT DEFAULT 1 COMMENT '类型：1-目录 2-菜单 3-按钮',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='菜单权限表';

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

-- 操作日志表
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    module VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    operation VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
    method VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    params TEXT DEFAULT NULL COMMENT '请求参数',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='操作日志表';

-- ================================================
-- 2. CRM模块表
-- ================================================

-- 客户档案表
DROP TABLE IF EXISTS crm_customer;
CREATE TABLE crm_customer (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '客户名称',
    type VARCHAR(20) NOT NULL COMMENT '客户类型：工程项目/装修公司/分销商/零售散户',
    address VARCHAR(300) DEFAULT NULL COMMENT '工地/公司地址',
    contact VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    credit_limit DECIMAL(12,2) DEFAULT 0 COMMENT '信用额度',
    current_debt DECIMAL(12,2) DEFAULT 0 COMMENT '当前未结欠款',
    salesperson_id BIGINT DEFAULT NULL COMMENT '负责业务员ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-作废',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_type (type),
    KEY idx_salesperson (salesperson_id)
) ENGINE=InnoDB COMMENT='客户档案表';

-- 供应商档案表
DROP TABLE IF EXISTS crm_supplier;
CREATE TABLE crm_supplier (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    main_category VARCHAR(50) DEFAULT NULL COMMENT '主营品类',
    contact VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    settlement_cycle VARCHAR(50) DEFAULT NULL COMMENT '结算周期',
    payable_limit DECIMAL(12,2) DEFAULT 0 COMMENT '应付上限',
    cooperation_status TINYINT DEFAULT 1 COMMENT '合作状态：1-合作中 0-已终止',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='供应商档案表';

-- 客户跟进记录表
DROP TABLE IF EXISTS crm_follow_record;
CREATE TABLE crm_follow_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    follow_type VARCHAR(20) NOT NULL COMMENT '跟进类型：电话/上门/报价',
    content TEXT DEFAULT NULL COMMENT '跟进内容',
    next_visit_time DATETIME DEFAULT NULL COMMENT '下次回访时间',
    salesperson_id BIGINT DEFAULT NULL COMMENT '跟进业务员ID',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_customer (customer_id),
    KEY idx_salesperson (salesperson_id)
) ENGINE=InnoDB COMMENT='客户跟进记录表';

-- ================================================
-- 3. 仓储模块表
-- ================================================

-- 商品档案表
DROP TABLE IF EXISTS wms_product;
CREATE TABLE wms_product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category VARCHAR(20) NOT NULL COMMENT '商品分类：瓷砖/管材/防水材料/门窗/砂石',
    spec VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    color_no VARCHAR(50) DEFAULT NULL COMMENT '色号',
    unit VARCHAR(20) DEFAULT NULL COMMENT '计量单位',
    purchase_price DECIMAL(12,2) DEFAULT 0 COMMENT '采购价',
    sale_price DECIMAL(12,2) DEFAULT 0 COMMENT '销售价',
    min_stock INT DEFAULT 0 COMMENT '最低库存预警值',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category (category)
) ENGINE=InnoDB COMMENT='商品档案表';

-- 仓库档案表
DROP TABLE IF EXISTS wms_warehouse;
CREATE TABLE wms_warehouse (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '仓库名称',
    type VARCHAR(20) NOT NULL COMMENT '仓库类型：总部中心仓/门店仓/工地临时仓',
    address VARCHAR(300) DEFAULT NULL COMMENT '仓库地址',
    manager_id BIGINT DEFAULT NULL COMMENT '仓库管理员ID',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='仓库档案表';

-- 库存明细表
DROP TABLE IF EXISTS wms_inventory;
CREATE TABLE wms_inventory (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    batch_no VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    color_no VARCHAR(50) DEFAULT NULL COMMENT '色号',
    quantity INT DEFAULT 0 COMMENT '库存数量',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_warehouse_batch (product_id, warehouse_id, batch_no, color_no),
    KEY idx_product (product_id),
    KEY idx_warehouse (warehouse_id)
) ENGINE=InnoDB COMMENT='库存明细表';

-- 调拨单主表
DROP TABLE IF EXISTS wms_stock_transfer;
CREATE TABLE wms_stock_transfer (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    transfer_no VARCHAR(30) NOT NULL COMMENT '调拨单编号',
    from_warehouse_id BIGINT NOT NULL COMMENT '调出仓库ID',
    to_warehouse_id BIGINT NOT NULL COMMENT '调入仓库ID',
    status VARCHAR(2) DEFAULT '0' COMMENT '单据状态：0-草稿 1-已审核 2-已作废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_transfer_no (transfer_no)
) ENGINE=InnoDB COMMENT='仓库调拨单';

-- 调拨单明细表
DROP TABLE IF EXISTS wms_stock_transfer_item;
CREATE TABLE wms_stock_transfer_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    transfer_id BIGINT NOT NULL COMMENT '调拨单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '调拨数量',
    PRIMARY KEY (id),
    KEY idx_transfer (transfer_id)
) ENGINE=InnoDB COMMENT='调拨单明细表';

-- 盘点单主表
DROP TABLE IF EXISTS wms_stock_check;
CREATE TABLE wms_stock_check (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    check_no VARCHAR(30) NOT NULL COMMENT '盘点单编号',
    warehouse_id BIGINT NOT NULL COMMENT '盘点仓库ID',
    status VARCHAR(2) DEFAULT '0' COMMENT '单据状态：0-草稿 1-已审核 2-已作废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_check_no (check_no)
) ENGINE=InnoDB COMMENT='库存盘点单';

-- 盘点单明细表
DROP TABLE IF EXISTS wms_stock_check_item;
CREATE TABLE wms_stock_check_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    check_id BIGINT NOT NULL COMMENT '盘点单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    system_qty INT DEFAULT 0 COMMENT '系统数量',
    actual_qty INT DEFAULT 0 COMMENT '实盘数量',
    diff_qty INT DEFAULT 0 COMMENT '差异数量（盘盈为正，盘亏为负）',
    PRIMARY KEY (id),
    KEY idx_check (check_id)
) ENGINE=InnoDB COMMENT='盘点单明细表';

-- ================================================
-- 4. 采购模块表
-- ================================================

-- 采购订单主表
DROP TABLE IF EXISTS purchase_order;
CREATE TABLE purchase_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(30) NOT NULL COMMENT '采购订单编号',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '订单总金额',
    status VARCHAR(2) DEFAULT '0' COMMENT '单据状态：0-草稿 1-已审核 2-已作废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_supplier (supplier_id)
) ENGINE=InnoDB COMMENT='采购订单';

-- 采购订单明细表
DROP TABLE IF EXISTS purchase_order_item;
CREATE TABLE purchase_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '采购订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    color_no VARCHAR(50) DEFAULT NULL COMMENT '色号',
    unit_price DECIMAL(12,2) DEFAULT 0 COMMENT '采购单价',
    quantity INT DEFAULT 0 COMMENT '采购数量',
    amount DECIMAL(12,2) DEFAULT 0 COMMENT '明细金额',
    PRIMARY KEY (id),
    KEY idx_order (order_id)
) ENGINE=InnoDB COMMENT='采购订单明细表';

-- 采购入库单主表
DROP TABLE IF EXISTS purchase_stock_in;
CREATE TABLE purchase_stock_in (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    stock_in_no VARCHAR(30) NOT NULL COMMENT '入库单编号',
    order_id BIGINT DEFAULT NULL COMMENT '关联采购订单ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    warehouse_id BIGINT NOT NULL COMMENT '入库仓库ID',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '入库总金额',
    status VARCHAR(2) DEFAULT '0' COMMENT '单据状态：0-草稿 1-已审核 2-已作废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_in_no (stock_in_no),
    KEY idx_supplier (supplier_id),
    KEY idx_warehouse (warehouse_id)
) ENGINE=InnoDB COMMENT='采购入库单';

-- 采购入库单明细表
DROP TABLE IF EXISTS purchase_stock_in_item;
CREATE TABLE purchase_stock_in_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    stock_in_id BIGINT NOT NULL COMMENT '入库单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    color_no VARCHAR(50) DEFAULT NULL COMMENT '色号',
    batch_no VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    quantity INT DEFAULT 0 COMMENT '入库数量',
    unit_price DECIMAL(12,2) DEFAULT 0 COMMENT '采购单价',
    PRIMARY KEY (id),
    KEY idx_stock_in (stock_in_id)
) ENGINE=InnoDB COMMENT='采购入库单明细表';

-- 采购退货单主表
DROP TABLE IF EXISTS purchase_return;
CREATE TABLE purchase_return (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    return_no VARCHAR(30) NOT NULL COMMENT '退货单编号',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    stock_in_id BIGINT DEFAULT NULL COMMENT '关联入库单ID',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '退货总金额',
    status VARCHAR(2) DEFAULT '0' COMMENT '单据状态：0-草稿 1-已审核 2-已作废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_return_no (return_no),
    KEY idx_supplier (supplier_id)
) ENGINE=InnoDB COMMENT='采购退货单';

-- 采购退货单明细表
DROP TABLE IF EXISTS purchase_return_item;
CREATE TABLE purchase_return_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    return_id BIGINT NOT NULL COMMENT '退货单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT DEFAULT 0 COMMENT '退货数量',
    unit_price DECIMAL(12,2) DEFAULT 0 COMMENT '退货单价',
    PRIMARY KEY (id),
    KEY idx_return (return_id)
) ENGINE=InnoDB COMMENT='采购退货单明细表';

-- ================================================
-- 5. 销售模块表
-- ================================================

-- 销售订单主表
DROP TABLE IF EXISTS sales_order;
CREATE TABLE sales_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(30) NOT NULL COMMENT '销售订单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    salesperson_id BIGINT NOT NULL COMMENT '业务员ID',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '订单总金额',
    commission_rate DECIMAL(5,2) DEFAULT 0 COMMENT '提成比例（%）',
    commission_amount DECIMAL(12,2) DEFAULT 0 COMMENT '提成金额',
    status VARCHAR(2) DEFAULT '0' COMMENT '单据状态：0-草稿 1-已审核 2-已作废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_customer (customer_id),
    KEY idx_salesperson (salesperson_id)
) ENGINE=InnoDB COMMENT='销售订单';

-- 销售订单明细表
DROP TABLE IF EXISTS sales_order_item;
CREATE TABLE sales_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '销售订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    color_no VARCHAR(50) DEFAULT NULL COMMENT '色号',
    unit_price DECIMAL(12,2) DEFAULT 0 COMMENT '销售单价',
    quantity INT DEFAULT 0 COMMENT '销售数量',
    discount DECIMAL(5,2) DEFAULT 100 COMMENT '折扣（%）',
    amount DECIMAL(12,2) DEFAULT 0 COMMENT '明细金额',
    delivered_qty INT DEFAULT 0 COMMENT '已出库数量',
    PRIMARY KEY (id),
    KEY idx_order (order_id)
) ENGINE=InnoDB COMMENT='销售订单明细表';

-- 工地分次出库单主表
DROP TABLE IF EXISTS delivery_order;
CREATE TABLE delivery_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    delivery_no VARCHAR(30) NOT NULL COMMENT '出库单编号',
    order_id BIGINT NOT NULL COMMENT '关联销售订单ID',
    warehouse_id BIGINT NOT NULL COMMENT '出库仓库ID',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '出库总金额',
    status VARCHAR(2) DEFAULT '0' COMMENT '单据状态：0-草稿 1-已审核 2-已作废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_delivery_no (delivery_no),
    KEY idx_order (order_id),
    KEY idx_warehouse (warehouse_id)
) ENGINE=InnoDB COMMENT='工地分次出库单';

-- 出库单明细表
DROP TABLE IF EXISTS delivery_order_item;
CREATE TABLE delivery_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    delivery_id BIGINT NOT NULL COMMENT '出库单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    color_no VARCHAR(50) DEFAULT NULL COMMENT '色号',
    quantity INT DEFAULT 0 COMMENT '出库数量',
    unit_price DECIMAL(12,2) DEFAULT 0 COMMENT '出库单价',
    PRIMARY KEY (id),
    KEY idx_delivery (delivery_id)
) ENGINE=InnoDB COMMENT='出库单明细表';

-- 销售退货单主表
DROP TABLE IF EXISTS sales_return;
CREATE TABLE sales_return (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    return_no VARCHAR(30) NOT NULL COMMENT '退货单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    delivery_id BIGINT DEFAULT NULL COMMENT '关联出库单ID',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '退货总金额',
    status VARCHAR(2) DEFAULT '0' COMMENT '单据状态：0-草稿 1-已审核 2-已作废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_return_no (return_no),
    KEY idx_customer (customer_id)
) ENGINE=InnoDB COMMENT='销售退货单';

-- 销售退货单明细表
DROP TABLE IF EXISTS sales_return_item;
CREATE TABLE sales_return_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    return_id BIGINT NOT NULL COMMENT '退货单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT DEFAULT 0 COMMENT '退货数量',
    unit_price DECIMAL(12,2) DEFAULT 0 COMMENT '退货单价',
    PRIMARY KEY (id),
    KEY idx_return (return_id)
) ENGINE=InnoDB COMMENT='销售退货单明细表';

-- ================================================
-- 6. 财务模块表
-- ================================================

-- 客户应收台账
DROP TABLE IF EXISTS fin_receivable;
CREATE TABLE fin_receivable (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    source_type VARCHAR(20) NOT NULL COMMENT '来源类型：DELIVERY-出库 SALES_RETURN-销售退货',
    source_id BIGINT NOT NULL COMMENT '来源单据ID',
    amount DECIMAL(12,2) DEFAULT 0 COMMENT '应收金额',
    paid_amount DECIMAL(12,2) DEFAULT 0 COMMENT '已收金额',
    remain_amount DECIMAL(12,2) DEFAULT 0 COMMENT '剩余金额',
    due_date DATE DEFAULT NULL COMMENT '到期日',
    status VARCHAR(2) DEFAULT '0' COMMENT '状态：0-正常 1-逾期 2-已结清',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_customer (customer_id),
    KEY idx_status (status)
) ENGINE=InnoDB COMMENT='客户应收台账';

-- 供应商应付台账
DROP TABLE IF EXISTS fin_payable;
CREATE TABLE fin_payable (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    source_type VARCHAR(20) NOT NULL COMMENT '来源类型：STOCK_IN-入库 PURCHASE_RETURN-采购退货',
    source_id BIGINT NOT NULL COMMENT '来源单据ID',
    amount DECIMAL(12,2) DEFAULT 0 COMMENT '应付金额',
    paid_amount DECIMAL(12,2) DEFAULT 0 COMMENT '已付金额',
    remain_amount DECIMAL(12,2) DEFAULT 0 COMMENT '剩余金额',
    due_date DATE DEFAULT NULL COMMENT '到期日',
    status VARCHAR(2) DEFAULT '0' COMMENT '状态：0-正常 1-逾期 2-已结清',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_supplier (supplier_id),
    KEY idx_status (status)
) ENGINE=InnoDB COMMENT='供应商应付台账';

-- 客户回款单主表
DROP TABLE IF EXISTS fin_payment_receive;
CREATE TABLE fin_payment_receive (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    receive_no VARCHAR(30) NOT NULL COMMENT '回款单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    amount DECIMAL(12,2) DEFAULT 0 COMMENT '回款金额',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '付款方式：现金/转账/支票',
    status VARCHAR(2) DEFAULT '0' COMMENT '单据状态：0-草稿 1-已审核 2-已作废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_receive_no (receive_no),
    KEY idx_customer (customer_id)
) ENGINE=InnoDB COMMENT='客户回款单';

-- 回款核销明细表
DROP TABLE IF EXISTS fin_payment_receive_item;
CREATE TABLE fin_payment_receive_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    receive_id BIGINT NOT NULL COMMENT '回款单ID',
    receivable_id BIGINT NOT NULL COMMENT '应收台账ID',
    amount DECIMAL(12,2) DEFAULT 0 COMMENT '核销金额',
    PRIMARY KEY (id),
    KEY idx_receive (receive_id)
) ENGINE=InnoDB COMMENT='回款核销明细表';

-- 供应商付款单主表
DROP TABLE IF EXISTS fin_payment_pay;
CREATE TABLE fin_payment_pay (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    pay_no VARCHAR(30) NOT NULL COMMENT '付款单编号',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    amount DECIMAL(12,2) DEFAULT 0 COMMENT '付款金额',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '付款方式：现金/转账/支票',
    status VARCHAR(2) DEFAULT '0' COMMENT '单据状态：0-草稿 1-已审核 2-已作废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pay_no (pay_no),
    KEY idx_supplier (supplier_id)
) ENGINE=InnoDB COMMENT='供应商付款单';

-- 付款核销明细表
DROP TABLE IF EXISTS fin_payment_pay_item;
CREATE TABLE fin_payment_pay_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    pay_id BIGINT NOT NULL COMMENT '付款单ID',
    payable_id BIGINT NOT NULL COMMENT '应付台账ID',
    amount DECIMAL(12,2) DEFAULT 0 COMMENT '核销金额',
    PRIMARY KEY (id),
    KEY idx_pay (pay_id)
) ENGINE=InnoDB COMMENT='付款核销明细表';
