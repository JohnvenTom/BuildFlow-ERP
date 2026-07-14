<template>
  <div class="alert-container">
    <el-card shadow="never">
      <!-- Tab切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 低库存预警Tab -->
        <el-tab-pane label="低库存预警" name="lowStock">
          <div class="tab-header">
            <span></span>
            <el-button type="primary" @click="handleExportLowStock">导出</el-button>
          </div>
          <el-table :data="lowStockData" v-loading="lowStockLoading" border stripe>
            <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="warehouseName" label="仓库" min-width="140" show-overflow-tooltip />
            <el-table-column prop="currentStock" label="当前库存" width="120" show-overflow-tooltip />
            <el-table-column prop="minStock" label="预警值" width="100" show-overflow-tooltip />
            <el-table-column prop="diff" label="差距" width="100" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="diff-negative">{{ row.diff }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 呆滞库存预警Tab -->
        <el-tab-pane label="呆滞库存预警" name="dormant">
          <div class="tab-header">
            <span></span>
            <el-button type="primary" @click="handleExportDormant">导出</el-button>
          </div>
          <el-table :data="dormantData" v-loading="dormantLoading" border stripe>
            <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="warehouseName" label="仓库" min-width="140" show-overflow-tooltip />
            <el-table-column prop="quantity" label="库存数量" width="120" show-overflow-tooltip />
            <el-table-column prop="lastOutDate" label="最后出库日期" width="160" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { lowStockAlert, dormantStockAlert } from '@/api/warehouse'

/** 当前激活的Tab */
const activeTab = ref('lowStock')

/** 低库存预警数据 */
const lowStockData = ref<any[]>([])
/** 低库存加载状态 */
const lowStockLoading = ref(false)

/** 呆滞库存预警数据 */
const dormantData = ref<any[]>([])
/** 呆滞库存加载状态 */
const dormantLoading = ref(false)

/**
 * 加载低库存预警数据
 * @description 请求低库存预警接口获取数据
 */
async function loadLowStockData() {
  lowStockLoading.value = true
  try {
    const res = await lowStockAlert()
    lowStockData.value = res.data || []
  } finally {
    lowStockLoading.value = false
  }
}

/**
 * 加载呆滞库存预警数据
 * @description 请求呆滞库存预警接口获取数据
 */
async function loadDormantData() {
  dormantLoading.value = true
  try {
    const res = await dormantStockAlert()
    dormantData.value = res.data || []
  } finally {
    dormantLoading.value = false
  }
}

/**
 * Tab切换事件
 * @param tab 切换后的Tab名称
 * @description 按需加载对应Tab的数据
 */
function handleTabChange(tab: string | number) {
  if (tab === 'lowStock' && lowStockData.value.length === 0) {
    loadLowStockData()
  } else if (tab === 'dormant' && dormantData.value.length === 0) {
    loadDormantData()
  }
}

/**
 * 导出低库存预警数据
 * @description 将低库存预警数据导出为Excel文件（预留接口调用）
 */
function handleExportLowStock() {
  // 预留：调用后端导出接口
  ElMessage.info('导出功能开发中')
}

/**
 * 导出呆滞库存预警数据
 * @description 将呆滞库存预警数据导出为Excel文件（预留接口调用）
 */
function handleExportDormant() {
  // 预留：调用后端导出接口
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  loadLowStockData()
})
</script>

<style scoped>
.alert-container {
  padding: 16px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.diff-negative {
  color: #f56c6c;
  font-weight: bold;
}
</style>
