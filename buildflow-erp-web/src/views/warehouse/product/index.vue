<template>
  <div class="product-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.category" placeholder="请选择商品分类" clearable>
            <el-option label="瓷砖" value="瓷砖" />
            <el-option label="管材" value="管材" />
            <el-option label="防水材料" value="防水材料" />
            <el-option label="门窗" value="门窗" />
            <el-option label="砂石" value="砂石" />
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
          <span>商品档案列表</span>
          <el-button type="primary" @click="handleAdd">新增商品</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="商品名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="110" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" width="120" show-overflow-tooltip />
        <el-table-column prop="colorCode" label="色号" width="100" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="80" show-overflow-tooltip />
        <el-table-column prop="purchasePrice" label="采购价" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.purchasePrice) }}</template>
        </el-table-column>
        <el-table-column prop="salePrice" label="销售价" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.salePrice) }}</template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存预警值" width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择商品分类" style="width: 100%">
            <el-option label="瓷砖" value="瓷砖" />
            <el-option label="管材" value="管材" />
            <el-option label="防水材料" value="防水材料" />
            <el-option label="门窗" value="门窗" />
            <el-option label="砂石" value="砂石" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="form.spec" placeholder="请输入规格" />
        </el-form-item>
        <el-form-item label="色号" prop="colorCode">
          <el-input v-model="form.colorCode" placeholder="请输入色号" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="采购价" prop="purchasePrice">
          <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="销售价" prop="salePrice">
          <el-input-number v-model="form.salePrice" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最低库存预警值" prop="minStock">
          <el-input-number v-model="form.minStock" :min="0" :precision="0" :step="1" style="width: 100%" />
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
import { productPage, productAdd, productUpdate, productDelete } from '@/api/warehouse'

/** 搜索表单数据 */
const searchForm = reactive({
  name: '',
  category: ''
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
/** 弹窗标题 */
const dialogTitle = ref('新增商品')
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 表单数据 */
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  category: '',
  spec: '',
  colorCode: '',
  unit: '',
  purchasePrice: 0,
  salePrice: 0,
  minStock: 0
})

/** 表单校验规则 */
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }]
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
 * 加载商品列表数据
 * @description 根据搜索条件和分页参数请求商品分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await productPage({
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
  searchForm.category = ''
  handleSearch()
}

/**
 * 重置表单数据
 * @description 将表单字段恢复为初始值
 */
function resetForm() {
  form.id = undefined
  form.name = ''
  form.category = ''
  form.spec = ''
  form.colorCode = ''
  form.unit = ''
  form.purchasePrice = 0
  form.salePrice = 0
  form.minStock = 0
}

/**
 * 新增商品操作
 * @description 重置表单并打开新增弹窗
 */
function handleAdd() {
  resetForm()
  dialogTitle.value = '新增商品'
  dialogVisible.value = true
}

/**
 * 编辑商品操作
 * @param row 当前行数据
 * @description 填充表单数据并打开编辑弹窗
 */
function handleEdit(row: any) {
  resetForm()
  dialogTitle.value = '编辑商品'
  Object.assign(form, row)
  dialogVisible.value = true
}

/**
 * 删除商品操作
 * @param row 当前行数据
 * @description 二次确认后调用删除接口
 * @throws 用户取消删除时不执行操作
 */
async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定要删除商品「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await productDelete(row.id)
  ElMessage.success('删除成功')
  loadData()
}

/**
 * 提交表单操作
 * @description 校验表单后调用新增或编辑接口
 */
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (form.id) {
    await productUpdate({ ...form })
    ElMessage.success('编辑成功')
  } else {
    await productAdd({ ...form })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.product-container {
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
