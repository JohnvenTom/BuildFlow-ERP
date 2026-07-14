# BuildFlow ERP 建材进销存 CRM 系统实施计划

## 一、项目概述

基于 PRD 文档，搭建建材行业一体化进销存 CRM 系统，技术栈：SpringBoot + MyBatis-Plus + MySQL8.0 + SpringSecurity（后端），Vue3 + TypeScript + Element Plus + ECharts（前端）。

## 二、当前状态分析

项目目前仅有 `.doc/PDR.md` 需求文档，无任何代码、配置、数据库脚本。需从零搭建完整系统。

## 三、系统架构设计

### 3.1 技术选型

| 层次       | 技术                   | 版本    |
| -------- | -------------------- | ----- |
| 后端框架     | SpringBoot           | 2.7.x |
| ORM      | MyBatis-Plus         | 3.5.x |
| 安全框架     | SpringSecurity + JWT | -     |
| 数据库      | MySQL                | 8.0   |
| 前端框架     | Vue3 + TypeScript    | 3.4.x |
| UI 组件库   | Element Plus         | 2.x   |
| 构建工具     | Vite                 | 5.x   |
| 图表库      | ECharts              | 5.x   |
| HTTP 客户端 | Axios                | 1.x   |
| 路由       | Vue Router           | 4.x   |
| 状态管理     | Pinia                | 2.x   |

### 3.2 后端分层架构

```
com.buildflow.erp
├── common/              # 通用工具、常量、统一响应体
│   ├── result/          # Result<T> 统一响应
│   ├── exception/       # 全局异常处理
│   ├── utils/           # 工具类（MD5、日期、编号生成等）
│   └── constants/       # 系统常量
├── config/              # 配置类（Security、Cors、MyBatis、Swagger）
├── security/            # JWT 过滤器、UserDetailsService 实现
├── entity/              # 数据库实体类
├── mapper/              # MyBatis-Plus Mapper 接口
├── service/             # 业务逻辑层
│   └── impl/            # 业务实现类
├── controller/          # REST 控制器
├── dto/                 # 请求/响应数据传输对象
└── interceptor/         # 操作日志拦截器
```

### 3.3 前端项目结构

```
src/
├── api/                 # 接口请求模块
├── assets/              # 静态资源
├── components/          # 公共组件
├── layouts/             # 布局组件
├── router/              # 路由配置
├── stores/              # Pinia 状态管理
├── utils/               # 工具函数
├── views/               # 页面视图
│   ├── login/           # 登录页
│   ├── dashboard/       # 数据大屏
│   ├── crm/             # CRM 模块
│   ├── purchase/        # 采购模块
│   ├── sales/           # 销售模块
│   ├── warehouse/       # 仓储模块
│   ├── finance/         # 财务模块
│   └── system/          # 系统管理
└── types/               # TypeScript 类型定义
```

## 四、数据库设计（共 25 张核心表）

### 4.1 系统基础表（5 张）

| 表名                  | 说明     | 核心字段                                                                                    |
| ------------------- | ------ | --------------------------------------------------------------------------------------- |
| sys\_user           | 用户账号   | id, username, password, real\_name, phone, role\_id, status, locked\_until, is\_deleted |
| sys\_role           | 角色表    | id, role\_name, role\_code, description, is\_deleted                                    |
| sys\_menu           | 菜单权限表  | id, parent\_id, menu\_name, path, icon, sort, permission                                |
| sys\_role\_menu     | 角色菜单关联 | id, role\_id, menu\_id                                                                  |
| sys\_operation\_log | 操作日志   | id, user\_id, module, operation, method, params, ip, create\_time                       |

### 4.2 CRM 模块表（3 张）

| 表名                  | 说明     | 核心字段                                                                                                          |
| ------------------- | ------ | ------------------------------------------------------------------------------------------------------------- |
| crm\_customer       | 客户档案   | id, name, type, address, contact, phone, credit\_limit, current\_debt, salesperson\_id, status, is\_deleted   |
| crm\_supplier       | 供应商档案  | id, name, main\_category, contact, phone, settlement\_cycle, payable\_limit, cooperation\_status, is\_deleted |
| crm\_follow\_record | 客户跟进记录 | id, customer\_id, follow\_type, content, next\_visit\_time, salesperson\_id, create\_time                     |

### 4.3 采购模块表（3 张）

| 表名                        | 说明     | 核心字段                                                                                           |
| ------------------------- | ------ | ---------------------------------------------------------------------------------------------- |
| purchase\_order           | 采购订单   | id, order\_no, supplier\_id, total\_amount, status, remark, create\_by, audit\_by, is\_deleted |
| purchase\_order\_item     | 采购订单明细 | id, order\_id, product\_id, color\_no, unit\_price, quantity, amount                           |
| purchase\_stock\_in       | 采购入库单  | id, stock\_in\_no, order\_id, warehouse\_id, status, total\_amount, is\_deleted                |
| purchase\_stock\_in\_item | 入库明细   | id, stock\_in\_id, product\_id, color\_no, batch\_no, quantity, unit\_price                    |
| purchase\_return          | 采购退货单  | id, return\_no, supplier\_id, stock\_in\_id, total\_amount, status, is\_deleted                |
| purchase\_return\_item    | 退货明细   | id, return\_id, product\_id, quantity, unit\_price                                             |

### 4.4 销售模块表（5 张）

| 表名                    | 说明      | 核心字段                                                                                                 |
| --------------------- | ------- | ---------------------------------------------------------------------------------------------------- |
| sales\_order          | 销售订单    | id, order\_no, customer\_id, salesperson\_id, total\_amount, commission\_amount, status, is\_deleted |
| sales\_order\_item    | 销售订单明细  | id, order\_id, product\_id, color\_no, unit\_price, quantity, discount, amount                       |
| delivery\_order       | 工地分次出库单 | id, delivery\_no, order\_id, warehouse\_id, status, total\_amount, is\_deleted                       |
| delivery\_order\_item | 出库明细    | id, delivery\_id, product\_id, color\_no, quantity, unit\_price                                      |
| sales\_return         | 销售退货单   | id, return\_no, customer\_id, delivery\_id, total\_amount, status, is\_deleted                       |
| sales\_return\_item   | 退货明细    | id, return\_id, product\_id, quantity, unit\_price                                                   |

### 4.5 仓储模块表（4 张）

| 表名                         | 说明   | 核心字段                                                                                             |
| -------------------------- | ---- | ------------------------------------------------------------------------------------------------ |
| wms\_product               | 商品档案 | id, name, category, spec, color\_no, unit, purchase\_price, sale\_price, min\_stock, is\_deleted |
| wms\_warehouse             | 仓库档案 | id, name, type, address, manager\_id, is\_deleted                                                |
| wms\_inventory             | 库存明细 | id, product\_id, warehouse\_id, batch\_no, color\_no, quantity                                   |
| wms\_stock\_transfer       | 调拨单  | id, transfer\_no, from\_warehouse\_id, to\_warehouse\_id, status, is\_deleted                    |
| wms\_stock\_transfer\_item | 调拨明细 | id, transfer\_id, product\_id, quantity                                                          |
| wms\_stock\_check          | 盘点单  | id, check\_no, warehouse\_id, status, is\_deleted                                                |
| wms\_stock\_check\_item    | 盘点明细 | id, check\_id, product\_id, system\_qty, actual\_qty, diff\_qty                                  |

### 4.6 财务模块表（4 张）

| 表名                          | 说明      | 核心字段                                                                                                |
| --------------------------- | ------- | --------------------------------------------------------------------------------------------------- |
| fin\_receivable             | 客户应收台账  | id, customer\_id, source\_type, source\_id, amount, paid\_amount, remain\_amount, due\_date, status |
| fin\_payable                | 供应商应付台账 | id, supplier\_id, source\_type, source\_id, amount, paid\_amount, remain\_amount, due\_date, status |
| fin\_payment\_receive       | 客户回款单   | id, receive\_no, customer\_id, amount, pay\_method, status, is\_deleted                             |
| fin\_payment\_receive\_item | 回款核销明细  | id, receive\_id, receivable\_id, amount                                                             |
| fin\_payment\_pay           | 供应商付款单  | id, pay\_no, supplier\_id, amount, pay\_method, status, is\_deleted                                 |
| fin\_payment\_pay\_item     | 付款核销明细  | id, pay\_id, payable\_id, amount                                                                    |

## 五、实施步骤（按顺序执行）

### 阶段一：项目初始化与基础设施（后端）

**步骤 1：创建 SpringBoot 后端项目**

* 文件：`buildflow-erp-server/`

* 使用 Spring Initializr 生成项目骨架

* pom.xml 添加依赖：spring-boot-starter-web, mybatis-plus-boot-starter, mysql-connector, spring-boot-starter-security, jjwt, lombok, swagger

* application.yml 配置数据库连接、端口 8080、MyBatis-Plus 配置

**步骤 2：搭建通用基础模块**

* `common/result/R.java` — 统一响应体 {code, msg, data}

* `common/result/PageResult.java` — 分页响应 {total, rows}

* `common/exception/GlobalExceptionHandler.java` — 全局异常捕获

* `common/exception/BusinessException.java` — 业务异常

* `common/utils/MD5Util.java` — 密码加密工具

* `common/utils/OrderNoGenerator.java` — 单据编号生成（业务类型+年月日+流水号）

* `common/constants/Constants.java` — 系统常量

**步骤 3：配置 SpringSecurity + JWT**

* `config/SecurityConfig.java` — Security 配置，放行登录接口

* `security/JwtTokenUtil.java` — JWT 生成与验证

* `security/JwtAuthenticationFilter.java` — JWT 过滤器

* `security/UserDetailsServiceImpl.java` — 用户认证实现

* 登录接口返回 token，后续请求 Header 携带 token

**步骤 4：创建数据库与全部表**

* `sql/init.sql` — 建库、建表语句（25+ 张表）

* `sql/data.sql` — 初始化测试数据（管理员账号、数据字典、示例商品/客户/供应商）

### 阶段二：系统管理模块（后端 + 前端）

**步骤 5：后端 - 用户角色权限接口**

* `entity/SysUser.java`, `entity/SysRole.java`, `entity/SysMenu.java`, `entity/SysRoleMenu.java`, `entity/SysOperationLog.java`

* 对应 Mapper/Service/Controller

* 接口：登录/登出、用户 CRUD、角色 CRUD、菜单树、操作日志查询

* 操作日志 AOP 拦截器自动记录

**步骤 6：前端 - 项目初始化与登录**

* 创建 Vue3 + TS + Vite 项目

* 安装依赖：element-plus, axios, vue-router, pinia, echarts

* 封装 Axios 请求（统一 token、错误处理、基础 URL）

* 登录页面 + token 管理

* 主布局（侧边栏菜单 + 顶部导航 + 内容区）

* 动态菜单（根据角色权限渲染）

### 阶段三：CRM 客户供应商模块

**步骤 7：后端 - CRM 接口**

* 5 个实体类 + Mapper + Service + Controller

* 客户档案 CRUD + 信用额度校验

* 供应商档案 CRUD

* 跟进记录 CRUD

* 信用与逾期欠款聚合查询

**步骤 8：前端 - CRM 页面**

* `views/crm/customer/index.vue` — 客户档案管理页

* `views/crm/supplier/index.vue` — 供应商档案管理页

* `views/crm/follow/index.vue` — 跟进记录台账页

* `views/crm/credit/index.vue` — 信用与逾期欠款查询页

### 阶段四：WMS 仓储模块

**步骤 9：后端 - 仓储接口**

* 商品档案、仓库档案 CRUD

* 库存查询（按仓库/商品/批次/色号多维度）

* 调拨单 CRUD + 事务（扣减调出仓 + 增加调入仓）

* 盘点单 CRUD + 盈亏计算

**步骤 10：前端 - 仓储页面**

* `views/warehouse/product/index.vue` — 商品档案页

* `views/warehouse/warehouse/index.vue` — 仓库管理页

* `views/warehouse/inventory/index.vue` — 库存查询页

* `views/warehouse/transfer/index.vue` — 调拨单页

* `views/warehouse/check/index.vue` — 盘点单页

* `views/warehouse/alert/index.vue` — 库存预警报表页

### 阶段五：采购管理模块

**步骤 11：后端 - 采购接口**

* 采购订单 CRUD + 审核

* 入库单 CRUD + 事务（自动增加库存 + 生成应付）

* 退货单 CRUD + 事务（扣减库存 + 减少应付）

* 月度对账单聚合查询

**步骤 12：前端 - 采购页面**

* `views/purchase/order/index.vue` — 采购订单页

* `views/purchase/stock-in/index.vue` — 入库单页

* `views/purchase/return/index.vue` — 退货单页

* `views/purchase/statement/index.vue` — 月度对账单页

### 阶段六：销售与工地出库模块

**步骤 13：后端 - 销售接口**

* 销售订单 CRUD + 信用校验 + 提成计算

* 分次出库单 CRUD + 事务（扣减库存 + 生成应收）

* 销售退货单 CRUD + 事务（返还库存 + 减少应收）

* 业务员提成报表

**步骤 14：前端 - 销售页面**

* `views/sales/order/index.vue` — 销售订单页

* `views/sales/delivery/index.vue` — 工地分次出库页

* `views/sales/return/index.vue` — 销售退货页

* `views/sales/commission/index.vue` — 业务员提成报表页

### 阶段七：财务对账模块

**步骤 15：后端 - 财务接口**

* 应收台账查询（按客户汇总，自动由出库单生成）

* 应付台账查询（按供应商汇总，自动由入库单生成）

* 回款单 CRUD + 事务（减少应收）

* 付款单 CRUD + 事务（减少应付）

* 逾期欠款预警查询

**步骤 16：前端 - 财务页面**

* `views/finance/receivable/index.vue` — 客户应收台账页

* `views/finance/payable/index.vue` — 供应商应付台账页

* `views/finance/payment-receive/index.vue` — 回款单页

* `views/finance/payment-pay/index.vue` — 付款单页

* `views/finance/overdue/index.vue` — 逾期预警页

### 阶段八：数据可视化大屏

**步骤 17：后端 - 大屏聚合接口**

* `controller/DashboardController.java` — 单个接口返回所有大屏数据

* 今日销售额、本月销量、总应收欠款、呆滞库存数量

* 客户类型销量分布、商品热销 TOP10、逾期客户榜单

* 近 30 日销售趋势、仓库库存分布

**步骤 18：前端 - 大屏页面**

* `views/dashboard/index.vue` — 经营大屏

* 使用 ECharts 渲染柱状图、折线图、饼图、数字指标卡

### 阶段九：收尾与完善

**步骤 19：通用功能完善**

* 所有列表 Excel 导出（Apache POI）

* 单据 PDF 预览与打印

* 数据字典接口与前端下拉绑定

**步骤 20：测试数据与验证**

* 完善初始化 SQL 脚本

* 全流程联调：采购→入库→销售→出库→财务核销

* 验证事务一致性、权限控制、业务规则

## 六、关键业务规则实现要点

1. **事务一致性**：入库/出库/退货操作必须 `@Transactional`，库存变更与应收应付生成在同一事务中
2. **单据编号**：`OrderNoGenerator` 使用 Redis 或数据库序列保证流水号唯一，格式如 `PO202607140001`
3. **信用校验**：销售订单保存前查询客户当前欠款 + 本次金额 ≤ 信用额度，超出抛出 BusinessException
4. **逻辑删除**：所有表含 `is_deleted` 字段，删除操作更新为 1 而非物理删除
5. **操作日志**：AOP 切面拦截所有增删改 Controller 方法，自动记录操作人、时间、内容

## 七、假设与决策

| 决策项   | 选择               | 原因                          |
| ----- | ---------------- | --------------------------- |
| 后端版本  | SpringBoot 2.7.x | 稳定、生态成熟，与 MyBatis-Plus 兼容性好 |
| 前端版本  | Vue3 + TS        | PRD 要求，TS 提升代码可维护性          |
| 单表/多表 | 每个业务实体独立表        | 清晰、符合 PRD 数据模型              |
| 编号生成  | 数据库序列方案          | 单机部署无需 Redis，简单可靠           |
| 导出方案  | Apache POI       | 成熟的 Excel 导出方案              |
| 前后端通信 | REST + JSON      | 标准方案，前后端分离                  |

## 八、验证步骤

1. 数据库初始化后所有表创建成功，测试数据可查询
2. 登录接口返回 JWT，未登录访问业务接口返回 401
3. 不同角色登录看到不同菜单
4. 采购入库 → 库存增加 → 应付生成（事务一致）
5. 销售出库 → 库存扣减 → 应收生成（事务一致）
6. 超信用额度订单无法保存
7. 调拨单审核后两个仓库库存同时变更
8. 盘点单审核后库存自动修正
9. 回款/付款后应收应付自动核销
10. 大屏数据与业务数据一致

