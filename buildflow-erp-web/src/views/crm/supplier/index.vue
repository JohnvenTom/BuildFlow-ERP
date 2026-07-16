<template>
  <div class="supplier-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.name" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="主营品类">
          <el-select v-model="searchForm.category" placeholder="请选择主营品类" clearable>
            <el-option label="瓷砖" value="瓷砖" />
            <el-option label="管材" value="管材" />
            <el-option label="防水材料" value="防水材料" />
            <el-option label="门窗" value="门窗" />
            <el-option label="砂石" value="砂石" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作状态">
          <el-select v-model="searchForm.cooperationStatus" placeholder="请选择合作状态" clearable>
            <el-option label="合作中" :value="0" />
            <el-option label="已暂停" :value="1" />
            <el-option label="已终止" :value="2" />
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
          <span>供应商档案列表</span>
          <el-button type="primary" @click="handleAdd">新增供应商</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="供应商名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="mainCategory" label="主营品类" width="110" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系人" width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="130" show-overflow-tooltip />
        <el-table-column prop="settlementCycle" label="结算周期" width="110" show-overflow-tooltip />
        <el-table-column prop="payableLimit" label="应付上限" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.payableLimit) }}</template>
        </el-table-column>
        <el-table-column prop="cooperationStatus" label="合作状态" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="cooperationStatusType(row.cooperationStatus)">
              {{ cooperationStatusText(row.cooperationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
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
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="主营品类" prop="mainCategory">
          <el-select v-model="form.mainCategory" placeholder="请选择主营品类" style="width: 100%">
            <el-option label="瓷砖" value="瓷砖" />
            <el-option label="管材" value="管材" />
            <el-option label="防水材料" value="防水材料" />
            <el-option label="门窗" value="门窗" />
            <el-option label="砂石" value="砂石" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="结算周期" prop="settlementCycle">
          <el-input v-model="form.settlementCycle" placeholder="请输入结算周期，如：月结30天" />
        </el-form-item>
        <el-form-item label="应付上限" prop="payableLimit">
          <el-input-number v-model="form.payableLimit" :min="0" :precision="2" :step="10000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="合作状态" prop="cooperationStatus">
          <el-select v-model="form.cooperationStatus" placeholder="请选择合作状态" style="width: 100%">
            <el-option label="合作中" :value="0" />
            <el-option label="已暂停" :value="1" />
            <el-option label="已终止" :value="2" />
          </el-select>
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
import { supplierPage, supplierAdd, supplierUpdate, supplierDelete } from '@/api/crm'

/** 搜索表单数据 */
const searchForm = reactive({
  name: '',
  category: '',
  cooperationStatus: '' as number | ''
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
const dialogTitle = ref('新增供应商')
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 表单数据 */
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  mainCategory: '',
  contact: '',
  phone: '',
  settlementCycle: '',
  payableLimit: 0,
  cooperationStatus: 0 as number
})

/** 表单校验规则 */
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  mainCategory: [{ required: true, message: '请选择主营品类', trigger: 'change' }]
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
 * 合作状态码转中文文本
 * @param status 状态码（0-合作中 1-已暂停 2-已终止）
 * @returns 中文状态文本
 */
function cooperationStatusText(status: number | undefined | null): string {
  const map: Record<number, string> = { 0: '合作中', 1: '已暂停', 2: '已终止' }
  return map[Number(status)] ?? '未知'
}

/**
 * 合作状态码转标签类型
 * @param status 状态码（0-合作中 1-已暂停 2-已终止）
 * @returns Element Plus 标签类型
 */
function cooperationStatusType(status: number | undefined | null): 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<number, 'success' | 'warning' | 'danger'> = { 0: 'success', 1: 'warning', 2: 'danger' }
  return map[Number(status)] ?? 'info'
}

/**
 * 加载供应商列表数据
 * @description 根据搜索条件和分页参数请求供应商分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await supplierPage({
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
  searchForm.name = ''
  searchForm.category = ''
  searchForm.cooperationStatus = ''
  handleSearch()
}

/**
 * 重置表单数据
 * @description 将表单字段恢复为初始值
 */
function resetForm() {
  form.id = undefined
  form.name = ''
  form.mainCategory = ''
  form.contact = ''
  form.phone = ''
  form.settlementCycle = ''
  form.payableLimit = 0
  form.cooperationStatus = 0
}

/**
 * 新增供应商操作
 * @description 重置表单并打开新增弹窗
 */
function handleAdd() {
  resetForm()
  dialogTitle.value = '新增供应商'
  dialogVisible.value = true
}

/**
 * 编辑供应商操作
 * @param row 当前行数据
 * @description 填充表单数据并打开编辑弹窗
 */
function handleEdit(row: any) {
  resetForm()
  dialogTitle.value = '编辑供应商'
  Object.assign(form, row)
  dialogVisible.value = true
}

/**
 * 删除供应商操作
 * @param row 当前行数据
 * @description 二次确认后调用删除接口
 * @throws 用户取消删除时不执行操作
 */
async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定要删除供应商「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await supplierDelete(row.id)
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
    await supplierUpdate({ ...form })
    ElMessage.success('编辑成功')
  } else {
    await supplierAdd({ ...form })
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
.supplier-container {
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
