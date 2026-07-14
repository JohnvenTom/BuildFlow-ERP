<template>
  <div class="commission-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            clearable
          />
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
          <span>业务员提成核算报表</span>
          <el-button type="success" @click="handleExport">导出</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe show-summary :summary-method="getSummary">
        <el-table-column prop="salespersonName" label="业务员姓名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="orderCount" label="订单数量" width="120" show-overflow-tooltip />
        <el-table-column prop="totalCommission" label="提成总金额" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.totalCommission) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { commissionReport } from '@/api/sales'

/** 搜索表单数据 */
const searchForm = reactive({
  dateRange: null as [string, string] | null
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
 * 加载提成报表数据
 * @description 根据搜索条件请求业务员提成数据
 */
async function loadData() {
  loading.value = true
  try {
    const params: any = {}
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await commissionReport(params)
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

/**
 * 搜索操作
 * @description 重新加载报表数据
 */
function handleSearch() {
  loadData()
}

/**
 * 重置搜索条件
 * @description 清空搜索表单并重新加载数据
 */
function handleReset() {
  searchForm.dateRange = null
  handleSearch()
}

/**
 * 合计行计算方法
 * @param param 表格列和行数据
 * @returns 合计行各列的显示值
 */
function getSummary({ columns, data }: { columns: any[]; data: any[] }): string[] {
  const sums: string[] = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    const prop = column.property
    if (prop === 'orderCount') {
      const total = data.reduce((sum, row) => sum + (Number(row[prop]) || 0), 0)
      sums[index] = String(total)
    } else if (prop === 'totalCommission') {
      const total = data.reduce((sum, row) => sum + (Number(row[prop]) || 0), 0)
      sums[index] = formatAmount(total)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

/**
 * 导出提成报表
 * @description 将当前报表数据导出为文件
 */
function handleExport() {
  ElMessage.success('导出功能开发中')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.commission-container {
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
</style>
