<template>
  <div class="inventory-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable>
            <el-option
              v-for="item in warehouseOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="searchForm.batchNo" placeholder="请输入批次号" clearable />
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
        <span>库存查询列表</span>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="warehouseName" label="仓库" min-width="140" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="批次号" width="140" show-overflow-tooltip />
        <el-table-column prop="colorNo" label="色号" width="100" show-overflow-tooltip />
        <el-table-column prop="quantity" label="库存数量" width="120" show-overflow-tooltip />
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
import { inventoryPage, warehouseList } from '@/api/warehouse'

/** 搜索表单数据 */
const searchForm = reactive({
  warehouseId: undefined as number | undefined,
  productName: '',
  batchNo: ''
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
/** 仓库下拉选项 */
const warehouseOptions = ref<any[]>([])

/**
 * 加载仓库下拉选项
 * @description 请求全部仓库列表填充下拉选项
 */
async function loadWarehouseOptions() {
  const res = await warehouseList()
  warehouseOptions.value = res.data || []
}

/**
 * 加载库存列表数据
 * @description 根据搜索条件和分页参数请求库存分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await inventoryPage({
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
  searchForm.warehouseId = undefined
  searchForm.productName = ''
  searchForm.batchNo = ''
  handleSearch()
}

onMounted(() => {
  loadWarehouseOptions()
  loadData()
})
</script>

<style scoped>
.inventory-container {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
