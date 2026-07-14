<template>
  <div class="receivable-container">
    <!-- 汇总区域 -->
    <el-card shadow="never" class="summary-card">
      <el-row :gutter="16">
        <el-col :span="8">
          <div class="summary-item">
            <div class="summary-label">应收总额</div>
            <div class="summary-value">{{ formatAmount(summary.totalAmount) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="summary-item">
            <div class="summary-label">已收总额</div>
            <div class="summary-value received">{{ formatAmount(summary.paidAmount) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="summary-item">
            <div class="summary-label">剩余总额</div>
            <div class="summary-value remaining">{{ formatAmount(summary.remainingAmount) }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="客户">
          <el-select v-model="searchForm.customerId" placeholder="请选择客户" clearable filterable>
            <el-option
              v-for="item in customerOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="正常" />
            <el-option label="逾期" value="逾期" />
            <el-option label="已结清" value="已结清" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>客户应收台账</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="customerName" label="客户名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sourceType" label="来源类型" width="120" show-overflow-tooltip />
        <el-table-column prop="sourceNo" label="来源单号" width="160" show-overflow-tooltip />
        <el-table-column prop="receivableAmount" label="应收金额" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.receivableAmount) }}</template>
        </el-table-column>
        <el-table-column prop="receivedAmount" label="已收金额" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.receivedAmount) }}</template>
        </el-table-column>
        <el-table-column prop="remainingAmount" label="剩余金额" width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="remaining-text">{{ formatAmount(row.remainingAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="到期日" width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { receivablePage, receivableSummary } from '@/api/finance'
import { customerList } from '@/api/crm'

/** 搜索表单数据 */
const searchForm = reactive({
  customerId: undefined as number | undefined,
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

/** 汇总数据 */
const summary = reactive({
  totalAmount: 0,
  paidAmount: 0,
  remainingAmount: 0
})

/** 客户下拉选项 */
const customerOptions = ref<any[]>([])

/**
 * 获取状态对应的Tag类型
 * @param status 应收状态
 * @returns Element Plus Tag类型
 */
function statusTagType(status: string): string {
  if (status === '正常') return 'success'
  if (status === '逾期') return 'danger'
  if (status === '已结清') return 'info'
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
 * 加载应收台账列表数据
 * @description 根据搜索条件和分页参数请求应收台账分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await receivablePage({
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
 * 加载汇总数据
 * @description 请求应收台账的汇总金额信息
 */
async function loadSummary() {
  const res = await receivableSummary()
  summary.totalAmount = res.data?.totalAmount || 0
  summary.paidAmount = res.data?.paidAmount || 0
  summary.remainingAmount = res.data?.remainingAmount || 0
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
  searchForm.customerId = undefined
  searchForm.status = ''
  handleSearch()
}

/**
 * 导出应收台账
 * @description 触发后端导出接口下载应收台账文件
 */
function handleExport() {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  loadCustomerOptions()
  loadSummary()
  loadData()
})
</script>

<style scoped>
.receivable-container {
  padding: 16px;
}

.summary-card {
  margin-bottom: 16px;
}

.summary-item {
  text-align: center;
  padding: 8px 0;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.summary-value.received {
  color: #67c23a;
}

.summary-value.remaining {
  color: #f56c6c;
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

.remaining-text {
  color: #f56c6c;
  font-weight: 500;
}
</style>
