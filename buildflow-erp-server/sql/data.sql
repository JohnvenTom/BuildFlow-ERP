-- ================================================
-- BuildFlow ERP 建材进销存CRM系统 测试数据脚本
-- ================================================

USE buildflow_erp;

-- ================================================
-- 1. 角色与菜单数据
-- ================================================

-- 角色
INSERT INTO sys_role (id, role_name, role_code, description) VALUES
(1, '系统管理员', 'ADMIN', '拥有全部模块权限'),
(2, '销售业务员', 'SALES', '客户管理、销售开单、查看自身订单'),
(3, '仓管人员', 'WAREHOUSE', '商品、仓库管理、出入库操作'),
(4, '财务人员', 'FINANCE', '应收应付、回款付款、报表'),
(5, '只读查看员', 'VIEWER', '仅查询数据，无增删改权限');

-- 菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, icon, sort, permission, menu_type) VALUES
(1, 0, '数据大屏', '/dashboard', 'Monitor', 1, NULL, 1),
(2, 0, 'CRM管理', '/crm', 'User', 2, NULL, 1),
(3, 2, '客户档案', '/crm/customer', NULL, 1, 'crm:customer:list', 2),
(4, 2, '供应商档案', '/crm/supplier', NULL, 2, 'crm:supplier:list', 2),
(5, 2, '跟进记录', '/crm/follow', NULL, 3, 'crm:follow:list', 2),
(6, 2, '信用与逾期', '/crm/credit', NULL, 4, 'crm:credit:list', 2),
(10, 0, '采购管理', '/purchase', 'ShoppingCart', 3, NULL, 1),
(11, 10, '采购订单', '/purchase/order', NULL, 1, 'purchase:order:list', 2),
(12, 10, '采购入库', '/purchase/stock-in', NULL, 2, 'purchase:stockin:list', 2),
(13, 10, '采购退货', '/purchase/return', NULL, 3, 'purchase:return:list', 2),
(14, 10, '月度对账', '/purchase/statement', NULL, 4, 'purchase:statement:list', 2),
(20, 0, '销售管理', '/sales', 'Sell', 4, NULL, 1),
(21, 20, '销售订单', '/sales/order', NULL, 1, 'sales:order:list', 2),
(22, 20, '工地出库', '/sales/delivery', NULL, 2, 'sales:delivery:list', 2),
(23, 20, '销售退货', '/sales/return', NULL, 3, 'sales:return:list', 2),
(24, 20, '提成核算', '/sales/commission', NULL, 4, 'sales:commission:list', 2),
(30, 0, '仓储管理', '/warehouse', 'Box', 5, NULL, 1),
(31, 30, '商品档案', '/warehouse/product', NULL, 1, 'wms:product:list', 2),
(32, 30, '仓库管理', '/warehouse/warehouse', NULL, 2, 'wms:warehouse:list', 2),
(33, 30, '库存查询', '/warehouse/inventory', NULL, 3, 'wms:inventory:list', 2),
(34, 30, '仓库调拨', '/warehouse/transfer', NULL, 4, 'wms:transfer:list', 2),
(35, 30, '库存盘点', '/warehouse/check', NULL, 5, 'wms:check:list', 2),
(36, 30, '库存预警', '/warehouse/alert', NULL, 6, 'wms:alert:list', 2),
(40, 0, '财务管理', '/finance', 'Money', 6, NULL, 1),
(41, 40, '客户应收', '/finance/receivable', NULL, 1, 'fin:receivable:list', 2),
(42, 40, '供应商应付', '/finance/payable', NULL, 2, 'fin:payable:list', 2),
(43, 40, '客户回款', '/finance/payment-receive', NULL, 3, 'fin:receive:list', 2),
(44, 40, '供应商付款', '/finance/payment-pay', NULL, 4, 'fin:pay:list', 2),
(45, 40, '逾期预警', '/finance/overdue', NULL, 5, 'fin:overdue:list', 2),
(50, 0, '系统管理', '/system', 'Setting', 7, NULL, 1),
(51, 50, '用户管理', '/system/user', NULL, 1, 'sys:user:list', 2),
(52, 50, '角色管理', '/system/role', NULL, 2, 'sys:role:list', 2),
(53, 50, '操作日志', '/system/log', NULL, 3, 'sys:log:list', 2);

-- 角色菜单关联（管理员拥有全部菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE is_deleted = 0;

-- 销售业务员菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 5), (2, 6), (2, 20), (2, 21), (2, 22), (2, 23), (2, 24);

-- 仓管人员菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 10), (3, 11), (3, 12), (3, 13), (3, 30), (3, 31), (3, 32), (3, 33), (3, 34), (3, 35), (3, 36);

-- 财务人员菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1), (4, 14), (4, 40), (4, 41), (4, 42), (4, 43), (4, 44), (4, 45);

-- 只读查看员菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6), (5, 10), (5, 11), (5, 12), (5, 13), (5, 14),
(5, 20), (5, 21), (5, 22), (5, 23), (5, 24), (5, 30), (5, 31), (5, 32), (5, 33), (5, 34), (5, 35), (5, 36),
(5, 40), (5, 41), (5, 42), (5, 43), (5, 44), (5, 45);

-- ================================================
-- 2. 用户数据（密码均为 123456，BCrypt加密）
-- ================================================

INSERT INTO sys_user (id, username, password, real_name, phone, role_id, status) VALUES
(1, 'admin', '$2b$10$HgsS2PV9ccqHHraSq.z5geprgap6dbnwhMsjdKcfmZG28Is3grAg2', '系统管理员', '13800000001', 1, 1),
(2, 'sales01', '$2b$10$HgsS2PV9ccqHHraSq.z5geprgap6dbnwhMsjdKcfmZG28Is3grAg2', '张三', '13800000002', 2, 1),
(3, 'sales02', '$2b$10$HgsS2PV9ccqHHraSq.z5geprgap6dbnwhMsjdKcfmZG28Is3grAg2', '李四', '13800000003', 2, 1),
(4, 'warehouse01', '$2b$10$HgsS2PV9ccqHHraSq.z5geprgap6dbnwhMsjdKcfmZG28Is3grAg2', '王五', '13800000004', 3, 1),
(5, 'finance01', '$2b$10$HgsS2PV9ccqHHraSq.z5geprgap6dbnwhMsjdKcfmZG28Is3grAg2', '赵六', '13800000005', 4, 1),
(6, 'viewer01', '$2b$10$HgsS2PV9ccqHHraSq.z5geprgap6dbnwhMsjdKcfmZG28Is3grAg2', '孙七', '13800000006', 5, 1);

-- ================================================
-- 3. 仓库数据
-- ================================================

INSERT INTO wms_warehouse (id, name, type, address, manager_id) VALUES
(1, '总部中心仓', '总部中心仓', '建材路1号', 4),
(2, '城东门店仓', '门店仓', '城东建材市场A区', 4),
(3, '万达工地临时仓', '工地临时仓', '万达广场工地', NULL);

-- ================================================
-- 4. 商品数据
-- ================================================

INSERT INTO wms_product (id, name, category, spec, color_no, unit, purchase_price, sale_price, min_stock) VALUES
(1, '金意陶大理石瓷砖', '瓷砖', '800x800mm', 'KJ-001', '片', 45.00, 68.00, 500),
(2, '东鹏抛釉砖', '瓷砖', '600x600mm', 'DP-002', '片', 32.00, 52.00, 300),
(3, '日丰PPR管', '管材', 'DN20', 'RF-003', '米', 8.50, 15.00, 1000),
(4, '伟星PVC排水管', '管材', 'DN110', 'WX-004', '米', 12.00, 22.00, 500),
(5, '东方雨虹防水涂料', '防水材料', '20kg/桶', 'DF-005', '桶', 120.00, 198.00, 100),
(6, '德高防水浆料', '防水材料', '10kg/桶', 'DG-006', '桶', 65.00, 108.00, 150),
(7, '断桥铝门窗', '门窗', '70系列', 'DQ-007', '平方米', 280.00, 450.00, 50),
(8, '中砂', '砂石', '中砂', 'ZS-008', '吨', 80.00, 130.00, 200),
(9, '马可波罗仿古砖', '瓷砖', '600x600mm', 'MK-009', '片', 55.00, 88.00, 200),
(10, '联塑PE给水管', '管材', 'DN32', 'LS-010', '米', 15.00, 28.00, 800);

-- ================================================
-- 5. 库存数据
-- ================================================

INSERT INTO wms_inventory (product_id, warehouse_id, batch_no, color_no, quantity) VALUES
(1, 1, 'B20260101', 'KJ-001', 2000),
(2, 1, 'B20260101', 'DP-002', 1500),
(3, 1, 'B20260201', 'RF-003', 3000),
(4, 1, 'B20260201', 'WX-004', 1200),
(5, 1, 'B20260301', 'DF-005', 200),
(6, 1, 'B20260301', 'DG-006', 300),
(7, 1, 'B20260401', 'DQ-007', 80),
(8, 1, 'B20260401', 'ZS-008', 500),
(9, 2, 'B20260101', 'MK-009', 400),
(10, 2, 'B20260201', 'LS-010', 1500);

-- ================================================
-- 6. 客户数据
-- ================================================

INSERT INTO crm_customer (id, name, type, address, contact, phone, credit_limit, current_debt, salesperson_id, status) VALUES
(1, '万达广场项目部', '工程项目', '万达广场工地', '陈工', '13900000001', 500000.00, 0, 2, 1),
(2, '恒大装饰公司', '装修公司', '恒大大厦3楼', '刘总', '13900000002', 300000.00, 0, 2, 1),
(3, '城南建材分销', '分销商', '城南建材市场B区', '王经理', '13900000003', 200000.00, 0, 3, 1),
(4, '张先生', '零售散户', '阳光小区5栋', '张先生', '13900000004', 50000.00, 0, 3, 1);

-- ================================================
-- 7. 供应商数据
-- ================================================

INSERT INTO crm_supplier (id, name, main_category, contact, phone, settlement_cycle, payable_limit, cooperation_status) VALUES
(1, '金意陶陶瓷厂', '瓷砖', '李厂长', '13700000001', '月结30天', 500000.00, 1),
(2, '日丰管业', '管材', '张经理', '13700000002', '月结45天', 300000.00, 1),
(3, '东方雨虹防水', '防水材料', '赵总', '13700000003', '月结30天', 200000.00, 1),
(4, '城南砂石场', '砂石', '马老板', '13700000004', '现结', 100000.00, 1);

-- ================================================
-- 8. 跟进记录数据
-- ================================================

INSERT INTO crm_follow_record (customer_id, follow_type, content, next_visit_time, salesperson_id) VALUES
(1, '上门', '拜访万达项目部陈工，确认二期用砖需求约5000片', '2026-07-20 10:00:00', 2),
(2, '电话', '恒大刘总确认下月有3个新项目开工，需要防水和管材', '2026-07-25 14:00:00', 2),
(3, '报价', '给城南分销报了PPR管和PVC管批发价', '2026-08-01 09:00:00', 3);
