<template>
  <div class="purchase-order-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplierId" placeholder="请选择供应商" clearable>
            <el-option
              v-for="item in supplierOptions"
              :key="item.id"
              :label="item.name"
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
          <span>采购订单列表</span>
          <el-button type="primary" @click="handleAdd">新增采购订单</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="160" show-overflow-tooltip />
        <el-table-column prop="supplierName" label="供应商名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="总金额" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
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
      title="新增采购订单"
      width="900px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option
              v-for="item in supplierOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>

        <!-- 明细表格 -->
        <el-form-item label="采购明细">
          <el-table :data="form.details" border style="width: 100%">
            <el-table-column label="商品" min-width="200">
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
            <el-table-column label="色号" width="120">
              <template #default="{ row }">
                <el-input v-model="row.colorCode" placeholder="色号" />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="140">
              <template #default="{ row }">
                <el-input-number v-model="row.unitPrice" :min="0" :precision="2" :step="1" style="width: 100%" @change="calcDetailAmount(row)" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="130">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" :step="1" style="width: 100%" @change="calcDetailAmount(row)" />
              </template>
            </el-table-column>
            <el-table-column label="金额" width="130">
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

        <!-- 总金额 -->
        <el-form-item label="总金额">
          <span class="total-amount">{{ formatAmount(totalAmount) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { purchaseOrderPage, purchaseOrderAdd, purchaseOrderAudit, purchaseOrderVoid } from '@/api/purchase'
import { supplierList } from '@/api/crm'
import { productList } from '@/api/warehouse'

/** 搜索表单数据 */
const searchForm = reactive({
  orderNo: '',
  supplierId: undefined as number | undefined,
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

/** 弹窗显示状态 */
const dialogVisible = ref(false)
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 供应商下拉选项 */
const supplierOptions = ref<any[]>([])
/** 商品下拉选项 */
const productOptions = ref<any[]>([])

/** 明细行数据类型 */
interface DetailItem {
  productId: number | undefined
  colorCode: string
  unitPrice: number
  quantity: number
  amount: number
}

/** 采购订单表单数据 */
const form = reactive({
  supplierId: undefined as number | undefined,
  remark: '',
  details: [] as DetailItem[]
})

/** 表单校验规则 */
const formRules = reactive<FormRules>({
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }]
})

/**
 * 计算总金额（自动汇总明细金额）
 * @returns 所有明细金额之和
 */
const totalAmount = computed(() => {
  return form.details.reduce((sum, item) => sum + (item.amount || 0), 0)
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
 * 加载采购订单列表数据
 * @description 根据搜索条件和分页参数请求采购订单分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await purchaseOrderPage({
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
 * 加载供应商下拉选项
 * @description 请求全部供应商列表填充下拉选项
 */
async function loadSupplierOptions() {
  const res = await supplierList()
  supplierOptions.value = res.data || []
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
  searchForm.supplierId = undefined
  searchForm.status = ''
  handleSearch()
}

/**
 * 重置表单数据
 * @description 将表单字段恢复为初始值
 */
function resetForm() {
  form.supplierId = undefined
  form.remark = ''
  form.details = []
}

/**
 * 新增采购订单操作
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
 * 计算明细行金额（单价 × 数量）
 * @param row 明细行数据
 * @description 自动计算并更新明细行金额
 */
function calcDetailAmount(row: DetailItem) {
  row.amount = row.unitPrice * row.quantity
}

/**
 * 审核采购订单操作
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
  await purchaseOrderAudit(row.id)
  ElMessage.success('审核成功')
  loadData()
}

/**
 * 作废采购订单操作
 * @param row 当前行数据
 * @description 二次确认后调用作废接口
 * @throws 用户取消作废时不执行操作
 */
async function handleVoid(row: any) {
  await ElMessageBox.confirm(`确定要作废采购订单「${row.orderNo}」吗？`, '作废确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await purchaseOrderVoid(row.id)
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
    ElMessage.warning('请至少添加一条采购明细')
    return
  }
  const hasEmptyProduct = form.details.some((d) => !d.productId)
  if (hasEmptyProduct) {
    ElMessage.warning('请选择明细中的商品')
    return
  }
  await purchaseOrderAdd({ ...form })
  ElMessage.success('新增成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadSupplierOptions()
  loadProductOptions()
  loadData()
})
</script>

<style scoped>
.purchase-order-container {
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
</style>
