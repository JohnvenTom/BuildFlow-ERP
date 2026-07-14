<template>
  <div class="credit-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.name" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="欠款状态">
          <el-select v-model="searchForm.debtStatus" placeholder="请选择欠款状态" clearable>
            <el-option label="正常" value="正常" />
            <el-option label="逾期" value="逾期" />
            <el-option label="已结清" value="已结清" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>客户信用与逾期欠款</span>
          <el-button type="success" @click="handleExport">导出</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="customerName" label="客户名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="creditLimit" label="信用额度" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.creditLimit) }}</template>
        </el-table-column>
        <el-table-column prop="currentDebt" label="当前欠款" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.currentDebt) }}</template>
        </el-table-column>
        <el-table-column prop="remainingCredit" label="信用剩余额度" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.remainingCredit) }}</template>
        </el-table-column>
        <el-table-column prop="overdueAmount" label="逾期金额" width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'overdue-amount': row.overdueAmount > 0 }">{{ formatAmount(row.overdueAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'overdue-amount': row.overdueDays > 0 }">{{ row.overdueDays }}</span>
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
import { creditPage } from '@/api/crm'

/** 搜索表单数据 */
const searchForm = reactive({
  name: '',
  debtStatus: ''
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
 * 加载客户信用列表数据
 * @description 根据搜索条件和分页参数请求客户信用分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await creditPage({
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
  searchForm.name = ''
  searchForm.debtStatus = ''
  handleSearch()
}

/**
 * 导出操作
 * @description 导出当前查询条件的信用数据为Excel文件
 * @note 当前为前端模拟导出提示，实际需对接后端导出接口
 */
function handleExport() {
  ElMessage.success('导出功能待对接后端导出接口')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.credit-container {
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

.overdue-amount {
  color: #f56c6c;
  font-weight: bold;
}
</style>
