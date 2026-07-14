<template>
  <div class="purchase-statement-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
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
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.month"
            type="month"
            placeholder="请选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
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
          <span>月度采购对账单</span>
          <el-button type="success" @click="handleExport">导出</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe show-summary :summary-method="getSummary">
        <el-table-column prop="supplierName" label="供应商" min-width="160" show-overflow-tooltip />
        <el-table-column prop="purchaseAmount" label="采购总金额" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.purchaseAmount) }}</template>
        </el-table-column>
        <el-table-column prop="returnAmount" label="退货总金额" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.returnAmount) }}</template>
        </el-table-column>
        <el-table-column prop="netAmount" label="应付净额" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="net-amount">{{ formatAmount(row.netAmount) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { purchaseStatement } from '@/api/purchase'
import { supplierList } from '@/api/crm'

/** 搜索表单数据 */
const searchForm = reactive({
  supplierId: undefined as number | undefined,
  month: ''
})

/** 表格数据 */
const tableData = ref<any[]>([])
/** 加载状态 */
const loading = ref(false)

/** 供应商下拉选项 */
const supplierOptions = ref<any[]>([])

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
 * 加载对账单数据
 * @description 根据搜索条件请求月度采购对账数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await purchaseStatement({ ...searchForm })
    tableData.value = res.data || []
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
 * 搜索操作
 * @description 重新加载对账数据
 */
function handleSearch() {
  loadData()
}

/**
 * 重置搜索条件
 * @description 清空搜索表单并重新加载数据
 */
function handleReset() {
  searchForm.supplierId = undefined
  searchForm.month = ''
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
    if (prop === 'purchaseAmount' || prop === 'returnAmount' || prop === 'netAmount') {
      const total = data.reduce((sum, row) => sum + (Number(row[prop]) || 0), 0)
      sums[index] = formatAmount(total)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

/**
 * 导出对账单
 * @description 将当前对账数据导出为文件
 */
function handleExport() {
  ElMessage.success('导出功能开发中')
}

onMounted(() => {
  loadSupplierOptions()
  loadData()
})
</script>

<style scoped>
.purchase-statement-container {
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

.net-amount {
  font-weight: bold;
  color: #f56c6c;
}
</style>
