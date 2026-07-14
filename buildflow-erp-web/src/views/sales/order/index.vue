<template>
  <div class="sales-order-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="searchForm.customerId" placeholder="请选择客户" clearable>
            <el-option
              v-for="item in customerOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="业务员">
          <el-select v-model="searchForm.salespersonId" placeholder="请选择业务员" clearable>
            <el-option
              v-for="item in salespersonOptions"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="草稿" />
            <el-option label="已审核" value="已审核" />
            <el-option label="已作废" value="已作废" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作区 + 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>销售订单列表</span>
          <el-button type="primary" @click="handleAdd">新增销售订单</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="160" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="salespersonName" label="业务员" width="100" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="总金额" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="commissionAmount" label="提成金额" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.commissionAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">查看详情</el-button>
            <el-button v-if="row.status === '草稿'" type="success" link @click="handleAudit(row)">审核</el-button>
            <el-button v-if="row.status === '草稿'" type="danger" link @click="handleVoid(row)">作废</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增销售订单"
      width="950px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" placeholder="请选择客户" style="width: 100%">
            <el-option
              v-for="item in customerOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="业务员" prop="salespersonId">
          <el-select v-model="form.salespersonId" placeholder="请选择业务员" style="width: 100%">
            <el-option
              v-for="item in salespersonOptions"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提成比例(%)" prop="commissionRate">
          <el-input-number v-model="form.commissionRate" :min="0" :max="100" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>

        <!-- 明细表格 -->
        <el-form-item label="订单明细">
          <el-table :data="form.details" border style="width: 100%">
            <el-table-column label="商品" min-width="180">
              <template #default="{ row }">
                <el-select v-model="row.productId" placeholder="请选择商品" style="width: 100%">
                  <el-option
                    v-for="item in productOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="色号" width="100">
              <template #default="{ row }">
                <el-input v-model="row.colorCode" placeholder="色号" />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.unitPrice" :min="0" :precision="2" :step="1" style="width: 100%" @change="calcDetailAmount(row)" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="110">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" :step="1" style="width: 100%" @change="calcDetailAmount(row)" />
              </template>
            </el-table-column>
            <el-table-column label="折扣(%)" width="110">
              <template #default="{ row }">
                <el-input-number v-model="row.discount" :min="0" :max="100" :precision="0" :step="5" style="width: 100%" @change="calcDetailAmount(row)" />
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120">
              <template #default="{ row }">
                {{ formatAmount(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeDetail($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" link @click="addDetail" style="margin-top: 8px">+ 添加明细</el-button>
        </el-form-item>

        <!-- 总金额和提成 -->
        <el-form-item label="总金额">
          <span class="total-amount">{{ formatAmount(totalAmount) }}</span>
        </el-form-item>
        <el-form-item label="提成金额">
          <span class="commission-amount">{{ formatAmount(commissionAmount) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="销售订单详情"
      width="800px"
      destroy-on-close
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ detailData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="业务员">{{ detailData.salespersonName }}</el-descriptions-item>
        <el-descriptions-item label="提成比例">{{ detailData.commissionRate }}%</el-descriptions-item>
        <el-descriptions-item label="总金额">{{ formatAmount(detailData.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="提成金额">{{ formatAmount(detailData.commissionAmount) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.status)">{{ detailData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>订单明细</el-divider>
      <el-table :data="detailData.details || []" border stripe>
        <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="colorCode" label="色号" width="100" show-overflow-tooltip />
        <el-table-column prop="unitPrice" label="单价" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" show-overflow-tooltip />
        <el-table-column prop="discount" label="折扣(%)" width="90" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { salesOrderPage, salesOrderDetail, salesOrderAdd, salesOrderAudit, salesOrderVoid } from '@/api/sales'
import { customerList } from '@/api/crm'
import { productList } from '@/api/warehouse'
import { userPage } from '@/api/system'

/** 搜索表单数据 */
const searchForm = reactive({
  orderNo: '',
  customerId: undefined as number | undefined,
  salespersonId: undefined as number | undefined,
  status: ''
})

/** 分页参数 */
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

/** 表格数据 */
const tableData = ref<any[]>([])
/** 加载状态 */
const loading = ref(false)

/** 新增弹窗显示状态 */
const dialogVisible = ref(false)
/** 详情弹窗显示状态 */
const detailVisible = ref(false)
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 客户下拉选项 */
const customerOptions = ref<any[]>([])
/** 业务员下拉选项 */
const salespersonOptions = ref<any[]>([])
/** 商品下拉选项 */
const productOptions = ref<any[]>([])

/** 明细行数据类型 */
interface DetailItem {
  productId: number | undefined
  colorCode: string
  unitPrice: number
  quantity: number
  discount: number
  amount: number
}

/** 销售订单表单数据 */
const form = reactive({
  customerId: undefined as number | undefined,
  salespersonId: undefined as number | undefined,
  commissionRate: 0,
  remark: '',
  details: [] as DetailItem[]
})

/** 表单校验规则 */
const formRules = reactive<FormRules>({
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  salespersonId: [{ required: true, message: '请选择业务员', trigger: 'change' }]
})

/** 详情数据 */
const detailData = reactive<any>({
  orderNo: '',
  customerName: '',
  salespersonName: '',
  commissionRate: 0,
  totalAmount: 0,
  commissionAmount: 0,
  status: '',
  createTime: '',
  remark: '',
  details: []
})

/**
 * 计算总金额（自动汇总明细金额）
 * @returns 所有明细金额之和
 */
const totalAmount = computed(() => {
  return form.details.reduce((sum, item) => sum + (item.amount || 0), 0)
})

/**
 * 计算提成金额（总金额 × 提成比例 / 100）
 * @returns 提成金额
 */
const commissionAmount = computed(() => {
  return totalAmount.value * (form.commissionRate || 0) / 100
})

/**
 * 获取状态对应的Tag类型
 * @param status 订单状态
 * @returns Element Plus Tag类型
 */
function statusTagType(status: string): string {
  if (status === '草稿') return 'info'
  if (status === '已审核') return 'success'
  if (status === '已作废') return 'danger'
  return ''
}

/**
 * 格式化金额显示（千分位 + 2位小数）
 * @param value 金额数值
 * @returns 格式化后的金额字符串
 */
function formatAmount(value: number | undefined | null): string {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/**
 * 加载销售订单列表数据
 * @description 根据搜索条件和分页参数请求销售订单分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await salesOrderPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 加载客户下拉选项
 * @description 请求全部客户列表填充下拉选项
 */
async function loadCustomerOptions() {
  const res = await customerList()
  customerOptions.value = res.data || []
}

/**
 * 加载业务员下拉选项
 * @description 请求用户列表填充业务员下拉选项
 */
async function loadSalespersonOptions() {
  const res = await userPage({ pageNum: 1, pageSize: 999 })
  salespersonOptions.value = res.data?.list || []
}

/**
 * 加载商品下拉选项
 * @description 请求全部商品列表填充下拉选项
 */
async function loadProductOptions() {
  const res = await productList()
  productOptions.value = res.data || []
}

/**
 * 搜索操作
 * @description 重置页码后重新加载数据
 */
function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

/**
 * 重置搜索条件
 * @description 清空搜索表单并重新加载数据
 */
function handleReset() {
  searchForm.orderNo = ''
  searchForm.customerId = undefined
  searchForm.salespersonId = undefined
  searchForm.status = ''
  handleSearch()
}

/**
 * 重置表单数据
 * @description 将表单字段恢复为初始值
 */
function resetForm() {
  form.customerId = undefined
  form.salespersonId = undefined
  form.commissionRate = 0
  form.remark = ''
  form.details = []
}

/**
 * 新增销售订单操作
 * @description 重置表单并打开新增弹窗
 */
function handleAdd() {
  resetForm()
  dialogVisible.value = true
}

/**
 * 添加明细行
 * @description 在明细表格末尾新增一行空白明细
 */
function addDetail() {
  form.details.push({
    productId: undefined,
    colorCode: '',
    unitPrice: 0,
    quantity: 1,
    discount: 100,
    amount: 0
  })
}

/**
 * 删除明细行
 * @param index 明细行索引
 * @description 移除指定索引的明细行
 */
function removeDetail(index: number) {
  form.details.splice(index, 1)
}

/**
 * 计算明细行金额（单价 × 数量 × 折扣 / 100）
 * @param row 明细行数据
 * @description 自动计算并更新明细行金额
 */
function calcDetailAmount(row: DetailItem) {
  row.amount = row.unitPrice * row.quantity * (row.discount || 100) / 100
}

/**
 * 查看订单详情
 * @param row 当前行数据
 * @description 请求订单详情并打开详情弹窗
 */
async function handleDetail(row: any) {
  const res = await salesOrderDetail(row.id)
  Object.assign(detailData, res.data || {})
  detailVisible.value = true
}

/**
 * 审核销售订单操作
 * @param row 当前行数据
 * @description 二次确认后调用审核接口
 * @throws 用户取消审核时不执行操作
 */
async function handleAudit(row: any) {
  await ElMessageBox.confirm('审核后订单将生效，是否确认？', '审核确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await salesOrderAudit(row.id)
  ElMessage.success('审核成功')
  loadData()
}

/**
 * 作废销售订单操作
 * @param row 当前行数据
 * @description 二次确认后调用作废接口
 * @throws 用户取消作废时不执行操作
 */
async function handleVoid(row: any) {
  await ElMessageBox.confirm(`确定要作废销售订单「${row.orderNo}」吗？`, '作废确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await salesOrderVoid(row.id)
  ElMessage.success('作废成功')
  loadData()
}

/**
 * 提交表单操作
 * @description 校验表单并检查明细数据后调用新增接口
 */
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (form.details.length === 0) {
    ElMessage.warning('请至少添加一条订单明细')
    return
  }
  const hasEmptyProduct = form.details.some((d) => !d.productId)
  if (hasEmptyProduct) {
    ElMessage.warning('请选择明细中的商品')
    return
  }
  await salesOrderAdd({ ...form })
  ElMessage.success('新增成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadCustomerOptions()
  loadSalespersonOptions()
  loadProductOptions()
  loadData()
})
</script>

<style scoped>
.sales-order-container {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.commission-amount {
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
}
</style>
