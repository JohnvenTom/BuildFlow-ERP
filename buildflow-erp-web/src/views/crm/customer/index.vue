<template>
  <div class="customer-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.name" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="客户类型">
          <el-select v-model="searchForm.type" placeholder="请选择客户类型" clearable>
            <el-option label="工程项目" value="工程项目" />
            <el-option label="装修公司" value="装修公司" />
            <el-option label="分销商" value="分销商" />
            <el-option label="零售散户" value="零售散户" />
          </el-select>
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

    <!-- 操作区 + 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>客户档案列表</span>
          <el-button type="primary" @click="handleAdd">新增客户</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="客户名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="type" label="客户类型" width="110" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系人" width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="130" show-overflow-tooltip />
        <el-table-column prop="creditLimit" label="信用额度" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.creditLimit) }}</template>
        </el-table-column>
        <el-table-column prop="currentDebt" label="当前欠款" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.currentDebt) }}</template>
        </el-table-column>
        <el-table-column prop="salesperson" label="负责业务员" width="110" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="row.status === '正常' ? 'success' : row.status === '逾期' ? 'danger' : 'info'">
              {{ row.status }}
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
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="客户类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择客户类型" style="width: 100%">
            <el-option label="工程项目" value="工程项目" />
            <el-option label="装修公司" value="装修公司" />
            <el-option label="分销商" value="分销商" />
            <el-option label="零售散户" value="零售散户" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="信用额度" prop="creditLimit">
          <el-input-number v-model="form.creditLimit" :min="0" :precision="2" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责业务员" prop="salespersonId">
          <el-select v-model="form.salespersonId" placeholder="请选择负责业务员" style="width: 100%">
            <el-option
              v-for="item in salespersonOptions"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            />
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
import { customerPage, customerAdd, customerUpdate, customerDelete } from '@/api/crm'

/** 搜索表单数据 */
const searchForm = reactive({
  name: '',
  type: '',
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

/** 弹窗显示状态 */
const dialogVisible = ref(false)
/** 弹窗标题 */
const dialogTitle = ref('新增客户')
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 表单数据 */
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  type: '',
  address: '',
  contact: '',
  phone: '',
  creditLimit: 0,
  salespersonId: undefined as number | undefined
})

/** 表单校验规则 */
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择客户类型', trigger: 'change' }]
})

/** 业务员下拉选项 */
const salespersonOptions = ref<any[]>([])

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
 * 加载客户列表数据
 * @description 根据搜索条件和分页参数请求客户分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await customerPage({
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
  searchForm.type = ''
  searchForm.debtStatus = ''
  handleSearch()
}

/**
 * 重置表单数据
 * @description 将表单字段恢复为初始值
 */
function resetForm() {
  form.id = undefined
  form.name = ''
  form.type = ''
  form.address = ''
  form.contact = ''
  form.phone = ''
  form.creditLimit = 0
  form.salespersonId = undefined
}

/**
 * 新增客户操作
 * @description 重置表单并打开新增弹窗
 */
function handleAdd() {
  resetForm()
  dialogTitle.value = '新增客户'
  dialogVisible.value = true
}

/**
 * 编辑客户操作
 * @param row 当前行数据
 * @description 填充表单数据并打开编辑弹窗
 */
function handleEdit(row: any) {
  resetForm()
  dialogTitle.value = '编辑客户'
  Object.assign(form, row)
  dialogVisible.value = true
}

/**
 * 删除客户操作
 * @param row 当前行数据
 * @description 二次确认后调用删除接口
 * @throws 用户取消删除时不执行操作
 */
async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定要删除客户「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await customerDelete(row.id)
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
    await customerUpdate({ ...form })
    ElMessage.success('编辑成功')
  } else {
    await customerAdd({ ...form })
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
.customer-container {
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
