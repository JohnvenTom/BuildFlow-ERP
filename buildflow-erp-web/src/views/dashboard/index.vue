<template>
  <div class="dashboard" v-loading="loading">
    <!-- 顶部核心指标卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="item in statCards" :key="item.label">
        <div class="stat-card__icon" :style="{ background: item.color }">
          <el-icon :size="28"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ item.value }}</div>
          <div class="stat-card__label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <!-- 第二行：客户类型销量 + 商品热销排行 -->
    <div class="chart-row">
      <div class="chart-card">
        <div class="chart-card__title">客户类型销量分布</div>
        <div ref="customerTypeChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="chart-card__title">商品热销排行 TOP10</div>
        <div ref="hotProductChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 第三行：销售趋势 + 仓库库存饼图 + 逾期欠款 -->
    <div class="chart-row chart-row--three">
      <div class="chart-card">
        <div class="chart-card__title">近30日销售额趋势</div>
        <div ref="salesTrendChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="chart-card__title">各仓库当前库存总量</div>
        <div ref="warehouseStockChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="chart-card__title">逾期欠款客户榜单</div>
        <div class="overdue-table-wrapper">
          <el-table :data="dashboardData?.overdueCustomers || []" stripe size="small" height="100%">
            <el-table-column prop="name" label="客户名称" min-width="100" />
            <el-table-column prop="amount" label="逾期金额" min-width="100" align="right">
              <template #default="{ row }">
                <span class="amount-text">{{ formatAmount(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="days" label="逾期天数" min-width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.days > 90 ? 'danger' : row.days > 30 ? 'warning' : 'info'" size="small">
                  {{ row.days }}天
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!dashboardData?.overdueCustomers?.length" description="暂无逾期数据" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { TrendCharts, Goods, Money, Box } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getDashboardData } from '@/api/dashboard'

// 注册ECharts按需组件
echarts.use([
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  CanvasRenderer
])

/** 大屏数据类型定义 */
interface DashboardData {
  todaySales: number
  monthSales: number
  totalReceivable: number
  dormantStockCount: number
  customerTypeSales: { type: string; amount: number }[]
  hotProducts: { name: string; amount: number }[]
  overdueCustomers: { name: string; amount: number; days: number }[]
  salesTrend: { date: string; amount: number }[]
  warehouseStock: { name: string; quantity: number }[]
}

const loading = ref(false)
const dashboardData = ref<DashboardData | null>(null)

// 图表DOM引用
const customerTypeChartRef = ref<HTMLElement>()
const hotProductChartRef = ref<HTMLElement>()
const salesTrendChartRef = ref<HTMLElement>()
const warehouseStockChartRef = ref<HTMLElement>()

// ECharts实例
let customerTypeChart: echarts.ECharts | null = null
let hotProductChart: echarts.ECharts | null = null
let salesTrendChart: echarts.ECharts | null = null
let warehouseStockChart: echarts.ECharts | null = null

/**
 * 格式化金额显示
 * @param val 金额数值
 * @returns 格式化后的金额字符串
 */
const formatAmount = (val: number | string): string => {
  const num = Number(val)
  if (isNaN(num)) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/**
 * 核心指标卡片配置
 */
const statCards = ref([
  { label: '今日销售额', value: '0.00', icon: TrendCharts, color: 'linear-gradient(135deg, #409EFF, #66b1ff)' },
  { label: '本月总销量', value: '0.00', icon: Goods, color: 'linear-gradient(135deg, #67C23A, #85ce61)' },
  { label: '总应收欠款', value: '0.00', icon: Money, color: 'linear-gradient(135deg, #E6A23C, #ebb563)' },
  { label: '呆滞库存数量', value: '0', icon: Box, color: 'linear-gradient(135deg, #F56C6C, #f78989)' }
])

/**
 * 初始化客户类型销量柱状图
 * @param data 客户类型销量数据
 */
const initCustomerTypeChart = (data: { type: string; amount: number }[]) => {
  if (!customerTypeChartRef.value) return
  customerTypeChart = echarts.init(customerTypeChartRef.value)
  customerTypeChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}: {c} 元' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.type),
      axisLabel: { color: '#606266' },
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#606266', formatter: (val: number) => val >= 10000 ? (val / 10000).toFixed(1) + '万' : String(val) },
      splitLine: { lineStyle: { color: '#ebeef5' } }
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.amount),
      barWidth: '40%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#66b1ff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  })
}

/**
 * 初始化商品热销排行横向柱状图
 * @param data 商品热销排行数据
 */
const initHotProductChart = (data: { name: string; amount: number }[]) => {
  if (!hotProductChartRef.value) return
  hotProductChart = echarts.init(hotProductChartRef.value)
  const sorted = [...data].reverse()
  hotProductChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}: {c} 元' },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: { color: '#606266', formatter: (val: number) => val >= 10000 ? (val / 10000).toFixed(1) + '万' : String(val) },
      splitLine: { lineStyle: { color: '#ebeef5' } }
    },
    yAxis: {
      type: 'category',
      data: sorted.map(d => d.name),
      axisLabel: { color: '#606266', width: 80, overflow: 'truncate' },
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    series: [{
      type: 'bar',
      data: sorted.map(d => d.amount),
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#67C23A' },
          { offset: 1, color: '#85ce61' }
        ]),
        borderRadius: [0, 4, 4, 0]
      }
    }]
  })
}

/**
 * 初始化近30日销售额趋势折线图
 * @param data 销售趋势数据
 */
const initSalesTrendChart = (data: { date: string; amount: number }[]) => {
  if (!salesTrendChartRef.value) return
  salesTrendChart = echarts.init(salesTrendChartRef.value)
  salesTrendChart.setOption({
    tooltip: { trigger: 'axis', formatter: (params: any) => `${params[0].axisValue}<br/>销售额：${formatAmount(params[0].value)} 元` },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date.substring(5)),
      axisLabel: { color: '#606266', rotate: 30, fontSize: 11 },
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#606266', formatter: (val: number) => val >= 10000 ? (val / 10000).toFixed(1) + '万' : String(val) },
      splitLine: { lineStyle: { color: '#ebeef5' } }
    },
    series: [{
      type: 'line',
      data: data.map(d => d.amount),
      smooth: true,
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: { color: '#409EFF', width: 2 },
      itemStyle: { color: '#409EFF' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.3)' },
          { offset: 1, color: 'rgba(64,158,255,0.05)' }
        ])
      }
    }]
  })
}

/**
 * 初始化各仓库库存总量饼图
 * @param data 仓库库存数据
 */
const initWarehouseStockChart = (data: { name: string; quantity: number }[]) => {
  if (!warehouseStockChartRef.value) return
  warehouseStockChart = echarts.init(warehouseStockChartRef.value)
  const colorPalette = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#b37feb', '#36cfc9', '#ff85c0']
  warehouseStockChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#606266' }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: data.map((d, i) => ({
        name: d.name,
        value: d.quantity,
        itemStyle: { color: colorPalette[i % colorPalette.length] }
      }))
    }]
  })
}

/**
 * 加载大屏数据并初始化图表
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getDashboardData()
    dashboardData.value = res.data as DashboardData
    // 更新指标卡片数值
    statCards.value[0].value = formatAmount(dashboardData.value.todaySales)
    statCards.value[1].value = formatAmount(dashboardData.value.monthSales)
    statCards.value[2].value = formatAmount(dashboardData.value.totalReceivable)
    statCards.value[3].value = String(dashboardData.value.dormantStockCount ?? 0)
    // 初始化图表
    await nextTick()
    initCustomerTypeChart(dashboardData.value.customerTypeSales || [])
    initHotProductChart(dashboardData.value.hotProducts || [])
    initSalesTrendChart(dashboardData.value.salesTrend || [])
    initWarehouseStockChart(dashboardData.value.warehouseStock || [])
  } catch (e) {
    console.error('加载大屏数据失败', e)
  } finally {
    loading.value = false
  }
}

/**
 * 窗口resize时重绘图表
 */
const handleResize = () => {
  customerTypeChart?.resize()
  hotProductChart?.resize()
  salesTrendChart?.resize()
  warehouseStockChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  customerTypeChart?.dispose()
  hotProductChart?.dispose()
  salesTrendChart?.dispose()
  warehouseStockChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}

/* 核心指标卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }

  &__icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__value {
    font-size: 24px;
    font-weight: 700;
    color: #303133;
    line-height: 1.3;
    word-break: break-all;
  }

  &__label {
    font-size: 13px;
    color: #909399;
    margin-top: 4px;
  }
}

/* 图表行 */
.chart-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;

  &--three {
    grid-template-columns: repeat(3, 1fr);
  }
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
    padding-left: 10px;
    border-left: 3px solid #409EFF;
  }
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* 逾期欠款表格 */
.overdue-table-wrapper {
  height: 300px;
  overflow: auto;
}

.amount-text {
  color: #F56C6C;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-row--three {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr;
  }

  .chart-row,
  .chart-row--three {
    grid-template-columns: 1fr;
  }
}
</style>
