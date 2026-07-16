<template>
  <div class="stock-in-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="入库单号">
          <el-input v-model="searchForm.stockInNo" placeholder="请输入入库单号" clearable />
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
          <span>采购入库单列表</span>
          <el-button type="primary" @click="handleAdd">新增入库单</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="stockInNo" label="入库单号" width="160" show-overflow-tooltip />
        <el-table-column prop="supplierName" label="供应商" min-width="140" show-overflow-tooltip />
        <el-table-column prop="warehouseName" label="入库仓库" min-width="140" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="总金额" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
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
      title="新增采购入库单"
      width="950px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
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
        <el-form-item label="入库仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" placeholder="请选择入库仓库" style="width: 100%">
            <el-option
              v-for="item in warehouseOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联采购订单" prop="purchaseOrderId">
          <el-select v-model="form.purchaseOrderId" placeholder="请选择关联采购订单（可选）" clearable style="width: 100%">
            <el-option
              v-for="item in purchaseOrderOptions"
              :key="item.id"
              :label="item.orderNo"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>

        <!-- 明细表格 -->
        <el-form-item label="入库明细">
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
            <el-table-column label="色号" width="110">
              <template #default="{ row }">
                <el-input v-model="row.colorCode" placeholder="色号" />
              </template>
            </el-table-column>
            <el-table-column label="批次号" width="120">
              <template #default="{ row }">
                <el-input v-model="row.batchNo" placeholder="批次号" />
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
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeDetail($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" link @click="addDetail" style="margin-top: 8px">+ 添加明细</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { stockInPage, stockInAdd, stockInAudit, stockInVoid } from '@/api/purchase'
import { purchaseOrderPage } from '@/api/purchase'
import { supplierList } from '@/api/crm'
import { warehouseList, productList } from '@/api/warehouse'

/** 搜索表单数据 */
const searchForm = reactive({
  stockInNo: '',
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
/** 仓库下拉选项 */
const warehouseOptions = ref<any[]>([])
/** 商品下拉选项 */
const productOptions = ref<any[]>([])
/** 采购订单下拉选项 */
const purchaseOrderOptions = ref<any[]>([])

/** 明细行数据类型 */
interface DetailItem {
  productId: number | undefined
  colorCode: string
  batchNo: string
  unitPrice: number
  quantity: number
  amount: number
}

/** 入库单表单数据 */
const form = reactive({
  supplierId: undefined as number | undefined,
  warehouseId: undefined as number | undefined,
  purchaseOrderId: undefined as number | undefined,
  remark: '',
  details: [] as DetailItem[]
})

/** 表单校验规则 */
const formRules = reactive<FormRules>({
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择入库仓库', trigger: 'change' }]
})

/**
 * 获取状态对应的Tag类型
 * @param status 入库单状态
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
 * 加载入库单列表数据
 * @description 根据搜索条件和分页参数请求入库单分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await stockInPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res.data?.rows || []
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
 * 加载仓库下拉选项
 * @description 请求全部仓库列表填充下拉选项
 */
async function loadWarehouseOptions() {
  const res = await warehouseList()
  warehouseOptions.value = res.data || []
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
 * 加载采购订单下拉选项
 * @description 请求已审核的采购订单列表填充下拉选项
 */
async function loadPurchaseOrderOptions() {
  const res = await purchaseOrderPage({ pageNum: 1, pageSize: 999, status: '已审核' })
  purchaseOrderOptions.value = res.data?.rows || []
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
  searchForm.stockInNo = ''
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
  form.warehouseId = undefined
  form.purchaseOrderId = undefined
  form.remark = ''
  form.details = []
}

/**
 * 新增入库单操作
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
    batchNo: '',
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
 * 审核入库单操作
 * @param row 当前行数据
 * @description 二次确认后调用审核接口，提示库存将变更
 * @throws 用户取消审核时不执行操作
 */
async function handleAudit(row: any) {
  await ElMessageBox.confirm('审核后库存将变更，是否确认？', '审核确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await stockInAudit(row.id)
  ElMessage.success('审核成功')
  loadData()
}

/**
 * 作废入库单操作
 * @param row 当前行数据
 * @description 二次确认后调用作废接口
 * @throws 用户取消作废时不执行操作
 */
async function handleVoid(row: any) {
  await ElMessageBox.confirm(`确定要作废入库单「${row.stockInNo}」吗？`, '作废确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await stockInVoid(row.id)
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
    ElMessage.warning('请至少添加一条入库明细')
    return
  }
  const hasEmptyProduct = form.details.some((d) => !d.productId)
  if (hasEmptyProduct) {
    ElMessage.warning('请选择明细中的商品')
    return
  }
  // 按后端 PurchaseStockInDTO 结构组装：主表 stockIn + 明细 items
  await stockInAdd({
    stockIn: {
      supplierId: form.supplierId,
      warehouseId: form.warehouseId,
      purchaseOrderId: form.purchaseOrderId,
      remark: form.remark
    },
    items: form.details.map((d) => ({
      productId: d.productId,
      colorCode: d.colorCode,
      batchNo: d.batchNo,
      unitPrice: d.unitPrice,
      quantity: d.quantity,
      amount: d.amount
    }))
  })
  ElMessage.success('新增成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadSupplierOptions()
  loadWarehouseOptions()
  loadProductOptions()
  loadPurchaseOrderOptions()
  loadData()
})
</script>

<style scoped>
.stock-in-container {
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
</style>
