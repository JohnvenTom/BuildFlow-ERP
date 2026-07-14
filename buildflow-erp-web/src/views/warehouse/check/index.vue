<template>
  <div class="check-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="盘点单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入盘点单号" clearable />
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
          <span>盘点单列表</span>
          <el-button type="primary" @click="handleAdd">新增盘点单</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="盘点单号" width="160" show-overflow-tooltip />
        <el-table-column prop="warehouseName" label="盘点仓库" min-width="140" show-overflow-tooltip />
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
            <el-button v-if="row.status === '草稿' || row.status === '已审核'" type="danger" link @click="handleVoid(row)">作废</el-button>
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
      title="新增盘点单"
      width="900px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="盘点仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" placeholder="请选择盘点仓库" style="width: 100%" @change="handleWarehouseChange">
            <el-option
              v-for="item in warehouseOptions"
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
        <el-form-item label="盘点明细">
          <el-table :data="form.details" border style="width: 100%">
            <el-table-column label="商品" min-width="200">
              <template #default="{ row }">
                <el-select v-model="row.productId" placeholder="请选择商品" style="width: 100%" @change="handleProductChange(row)">
                  <el-option
                    v-for="item in productOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="系统数量" width="130">
              <template #default="{ row }">
                <el-input-number v-model="row.systemQuantity" :min="0" disabled style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="实盘数量" width="150">
              <template #default="{ row }">
                <el-input-number v-model="row.actualQuantity" :min="0" :step="1" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="差异数量" width="130">
              <template #default="{ row }">
                <span :class="{ 'diff-negative': getDiff(row) < 0 }">{{ getDiff(row) }}</span>
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
import { checkPage, checkAdd, checkAudit, checkVoid, warehouseList, productList } from '@/api/warehouse'

/** 搜索表单数据 */
const searchForm = reactive({
  orderNo: '',
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

/** 仓库下拉选项 */
const warehouseOptions = ref<any[]>([])
/** 商品下拉选项 */
const productOptions = ref<any[]>([])

/** 盘点明细项类型 */
interface CheckDetailItem {
  productId: number | undefined
  systemQuantity: number
  actualQuantity: number
}

/** 盘点单表单数据 */
const form = reactive({
  warehouseId: undefined as number | undefined,
  remark: '',
  details: [] as CheckDetailItem[]
})

/** 表单校验规则 */
const formRules = reactive<FormRules>({
  warehouseId: [{ required: true, message: '请选择盘点仓库', trigger: 'change' }]
})

/**
 * 获取状态对应的Tag类型
 * @param status 盘点单状态
 * @returns Element Plus Tag类型
 */
function statusTagType(status: string): string {
  if (status === '草稿') return 'info'
  if (status === '已审核') return 'success'
  if (status === '已作废') return 'danger'
  return ''
}

/**
 * 计算盘点差异
 * @param row 明细行数据
 * @returns 实盘数量与系统数量的差值
 */
function getDiff(row: CheckDetailItem): number {
  return (row.actualQuantity || 0) - (row.systemQuantity || 0)
}

/**
 * 加载盘点单列表数据
 * @description 根据搜索条件和分页参数请求盘点单分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await checkPage({
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
  searchForm.status = ''
  handleSearch()
}

/**
 * 重置表单数据
 * @description 将表单字段恢复为初始值
 */
function resetForm() {
  form.warehouseId = undefined
  form.remark = ''
  form.details = []
}

/**
 * 新增盘点单操作
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
  form.details.push({ productId: undefined, systemQuantity: 0, actualQuantity: 0 })
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
 * 仓库变更事件
 * @description 选择仓库后可触发加载该仓库库存（预留扩展点）
 */
function handleWarehouseChange() {
  // 预留：可根据仓库ID加载对应库存数据自动填充明细
}

/**
 * 商品变更事件
 * @param row 当前行明细数据
 * @description 选择商品后自动填充系统数量（从库存数据中读取）
 */
function handleProductChange(row: CheckDetailItem) {
  // 预留：可根据仓库ID+商品ID查询库存自动填充 systemQuantity
  // 实际场景应调用库存查询接口获取当前库存
  row.systemQuantity = 0
}

/**
 * 审核盘点单操作
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
  await checkAudit(row.id, row.auditBy)
  ElMessage.success('审核成功')
  loadData()
}

/**
 * 作废盘点单操作
 * @param row 当前行数据
 * @description 二次确认后调用作废接口
 * @throws 用户取消作废时不执行操作
 */
async function handleVoid(row: any) {
  await ElMessageBox.confirm(`确定要作废盘点单「${row.orderNo}」吗？`, '作废确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await checkVoid(row.id)
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
    ElMessage.warning('请至少添加一条盘点明细')
    return
  }
  const hasEmptyProduct = form.details.some((d) => !d.productId)
  if (hasEmptyProduct) {
    ElMessage.warning('请选择明细中的商品')
    return
  }
  await checkAdd({ ...form })
  ElMessage.success('新增成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadWarehouseOptions()
  loadProductOptions()
  loadData()
})
</script>

<style scoped>
.check-container {
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

.diff-negative {
  color: #f56c6c;
  font-weight: bold;
}
</style>
