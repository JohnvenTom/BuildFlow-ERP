<template>
  <div class="overdue-container">
    <!-- 顶部汇总卡片 -->
    <el-row :gutter="16" class="summary-row">
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-item">
            <div class="summary-icon warning">
              <el-icon size="28"><Warning /></el-icon>
            </div>
            <div class="summary-content">
              <div class="summary-label">逾期客户数</div>
              <div class="summary-value">{{ summary.customerCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-item">
            <div class="summary-icon danger">
              <el-icon size="28"><Money /></el-icon>
            </div>
            <div class="summary-content">
              <div class="summary-label">逾期总金额</div>
              <div class="summary-value danger">{{ formatAmount(summary.totalAmount) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-item">
            <div class="summary-icon info">
              <el-icon size="28"><TrendCharts /></el-icon>
            </div>
            <div class="summary-content">
              <div class="summary-label">平均逾期天数</div>
              <div class="summary-value">{{ summary.avgOverdueDays }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 表格区 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>逾期欠款列表</span>
          <el-button type="success" @click="handleExport">导出催款单</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="customerName" label="客户名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="overdueAmount" label="逾期金额" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="overdue-amount">{{ formatAmount(row.overdueAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="overdueDaysTagType(row.overdueDays)">{{ row.overdueDays }}天</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="120" show-overflow-tooltip />
        <el-table-column prop="contactPhone" label="联系电话" width="140" show-overflow-tooltip />
        <el-table-column prop="lastPaymentDate" label="最后回款日" width="120" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Money, TrendCharts } from '@element-plus/icons-vue'
import { overdueList, overdueSummary } from '@/api/finance'

/** 表格数据 */
const tableData = ref<any[]>([])
/** 加载状态 */
const loading = ref(false)

/** 汇总数据 */
const summary = reactive({
  customerCount: 0,
  totalAmount: 0,
  avgOverdueDays: 0
})

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
 * 根据逾期天数获取Tag类型
 * @description 30天以内为warning，30-60天为danger，60天以上为严重danger
 * @param days 逾期天数
 * @returns Element Plus Tag类型
 */
function overdueDaysTagType(days: number): string {
  if (days < 30) return 'warning'
  if (days < 60) return 'danger'
  return 'danger'
}

/**
 * 加载逾期欠款列表数据
 * @description 请求所有逾期客户及其逾期金额、逾期天数
 */
async function loadData() {
  loading.value = true
  try {
    const res = await overdueList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

/**
 * 加载汇总数据
 * @description 请求逾期客户数、逾期总金额、平均逾期天数
 */
async function loadSummary() {
  const res = await overdueSummary()
  summary.customerCount = res.data?.customerCount || 0
  summary.totalAmount = res.data?.totalAmount || 0
  summary.avgOverdueDays = res.data?.avgOverdueDays || 0
}

/**
 * 导出催款单
 * @description 触发后端导出接口下载催款单文件
 */
function handleExport() {
  ElMessage.info('导出催款单功能开发中')
}

onMounted(() => {
  loadSummary()
  loadData()
})
</script>

<style scoped>
.overdue-container {
  padding: 16px;
}

.summary-row {
  margin-bottom: 16px;
}

.summary-card {
  height: 100%;
}

.summary-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.summary-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: #fff;
}

.summary-icon.warning {
  background-color: #e6a23c;
}

.summary-icon.danger {
  background-color: #f56c6c;
}

.summary-icon.info {
  background-color: #909399;
}

.summary-content {
  flex: 1;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.summary-value.danger {
  color: #f56c6c;
}

.table-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overdue-amount {
  color: #f56c6c;
  font-weight: bold;
}
</style>
