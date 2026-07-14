import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

/** 静态路由配置 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      // 数据大屏
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据大屏' }
      },
      // CRM管理
      {
        path: 'crm/customer',
        name: 'CrmCustomer',
        component: () => import('@/views/crm/customer/index.vue'),
        meta: { title: '客户档案' }
      },
      {
        path: 'crm/supplier',
        name: 'CrmSupplier',
        component: () => import('@/views/crm/supplier/index.vue'),
        meta: { title: '供应商档案' }
      },
      {
        path: 'crm/follow',
        name: 'CrmFollow',
        component: () => import('@/views/crm/follow/index.vue'),
        meta: { title: '跟进记录' }
      },
      {
        path: 'crm/credit',
        name: 'CrmCredit',
        component: () => import('@/views/crm/credit/index.vue'),
        meta: { title: '信用与逾期' }
      },
      // 采购管理
      {
        path: 'purchase/order',
        name: 'PurchaseOrder',
        component: () => import('@/views/purchase/order/index.vue'),
        meta: { title: '采购订单' }
      },
      {
        path: 'purchase/stock-in',
        name: 'PurchaseStockIn',
        component: () => import('@/views/purchase/stock-in/index.vue'),
        meta: { title: '采购入库' }
      },
      {
        path: 'purchase/return',
        name: 'PurchaseReturn',
        component: () => import('@/views/purchase/return/index.vue'),
        meta: { title: '采购退货' }
      },
      {
        path: 'purchase/statement',
        name: 'PurchaseStatement',
        component: () => import('@/views/purchase/statement/index.vue'),
        meta: { title: '月度对账' }
      },
      // 销售管理
      {
        path: 'sales/order',
        name: 'SalesOrder',
        component: () => import('@/views/sales/order/index.vue'),
        meta: { title: '销售订单' }
      },
      {
        path: 'sales/delivery',
        name: 'SalesDelivery',
        component: () => import('@/views/sales/delivery/index.vue'),
        meta: { title: '工地出库' }
      },
      {
        path: 'sales/return',
        name: 'SalesReturn',
        component: () => import('@/views/sales/return/index.vue'),
        meta: { title: '销售退货' }
      },
      {
        path: 'sales/commission',
        name: 'SalesCommission',
        component: () => import('@/views/sales/commission/index.vue'),
        meta: { title: '提成核算' }
      },
      // 仓储管理
      {
        path: 'warehouse/product',
        name: 'WarehouseProduct',
        component: () => import('@/views/warehouse/product/index.vue'),
        meta: { title: '商品档案' }
      },
      {
        path: 'warehouse/warehouse',
        name: 'WarehouseWarehouse',
        component: () => import('@/views/warehouse/warehouse/index.vue'),
        meta: { title: '仓库管理' }
      },
      {
        path: 'warehouse/inventory',
        name: 'WarehouseInventory',
        component: () => import('@/views/warehouse/inventory/index.vue'),
        meta: { title: '库存查询' }
      },
      {
        path: 'warehouse/transfer',
        name: 'WarehouseTransfer',
        component: () => import('@/views/warehouse/transfer/index.vue'),
        meta: { title: '仓库调拨' }
      },
      {
        path: 'warehouse/check',
        name: 'WarehouseCheck',
        component: () => import('@/views/warehouse/check/index.vue'),
        meta: { title: '库存盘点' }
      },
      {
        path: 'warehouse/alert',
        name: 'WarehouseAlert',
        component: () => import('@/views/warehouse/alert/index.vue'),
        meta: { title: '库存预警' }
      },
      // 财务管理
      {
        path: 'finance/receivable',
        name: 'FinanceReceivable',
        component: () => import('@/views/finance/receivable/index.vue'),
        meta: { title: '客户应收' }
      },
      {
        path: 'finance/payable',
        name: 'FinancePayable',
        component: () => import('@/views/finance/payable/index.vue'),
        meta: { title: '供应商应付' }
      },
      {
        path: 'finance/payment-receive',
        name: 'FinancePaymentReceive',
        component: () => import('@/views/finance/payment-receive/index.vue'),
        meta: { title: '客户回款' }
      },
      {
        path: 'finance/payment-pay',
        name: 'FinancePaymentPay',
        component: () => import('@/views/finance/payment-pay/index.vue'),
        meta: { title: '供应商付款' }
      },
      {
        path: 'finance/overdue',
        name: 'FinanceOverdue',
        component: () => import('@/views/finance/overdue/index.vue'),
        meta: { title: '逾期预警' }
      },
      // 系统管理
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'system/log',
        name: 'SystemLog',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '操作日志' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局路由守卫
 * 检查用户是否已登录（token是否存在），未登录则跳转登录页
 */
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const hasToken = userStore.token

  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    if (to.path === '/login') {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
