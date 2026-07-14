<template>
  <div class="follow-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="客户">
          <el-select v-model="searchForm.customerId" placeholder="请选择客户" clearable filterable>
            <el-option
              v-for="item in customerOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进类型">
          <el-select v-model="searchForm.followType" placeholder="请选择跟进类型" clearable>
            <el-option label="电话" value="电话" />
            <el-option label="上门" value="上门" />
            <el-option label="报价" value="报价" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间段">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
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
          <span>客户跟进记录</span>
          <el-button type="primary" @click="handleAdd">新增跟进</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="customerName" label="客户名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="followType" label="跟进类型" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag
              :type="row.followType === '电话' ? '' : row.followType === '上门' ? 'success' : 'warning'"
            >
              {{ row.followType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="跟进内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="nextVisitDate" label="下次回访时间" width="130" show-overflow-tooltip />
        <el-table-column prop="salesperson" label="跟进业务员" width="110" show-overflow-tooltip />
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
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width: 100%">
            <el-option
              v-for="item in customerOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进类型" prop="followType">
          <el-select v-model="form.followType" placeholder="请选择跟进类型" style="width: 100%">
            <el-option label="电话" value="电话" />
            <el-option label="上门" value="上门" />
            <el-option label="报价" value="报价" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入跟进内容"
          />
        </el-form-item>
        <el-form-item label="下次回访时间" prop="nextVisitDate">
          <el-date-picker
            v-model="form.nextVisitDate"
            type="date"
            placeholder="请选择下次回访时间"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
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
import { followPage, followAdd, followUpdate, followDelete, customerList } from '@/api/crm'

/** 搜索表单数据 */
const searchForm = reactive({
  customerId: undefined as number | undefined,
  followType: '',
  dateRange: null as string[] | null
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

/** 客户下拉选项 */
const customerOptions = ref<any[]>([])

/** 弹窗显示状态 */
const dialogVisible = ref(false)
/** 弹窗标题 */
const dialogTitle = ref('新增跟进记录')
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 表单数据 */
const form = reactive({
  id: undefined as number | undefined,
  customerId: undefined as number | undefined,
  followType: '',
  content: '',
  nextVisitDate: ''
})

/** 表单校验规则 */
const formRules = reactive<FormRules>({
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  followType: [{ required: true, message: '请选择跟进类型', trigger: 'change' }]
})

/**
 * 加载跟进记录列表数据
 * @description 根据搜索条件和分页参数请求跟进记录分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      customerId: searchForm.customerId,
      followType: searchForm.followType
    }
    // 处理时间段参数
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await followPage(params)
    tableData.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 加载客户下拉选项
 * @description 请求全部客户列表供下拉选择使用
 */
async function loadCustomerOptions() {
  try {
    const res = await customerList()
    customerOptions.value = res.data || []
  } catch {
    customerOptions.value = []
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
  searchForm.customerId = undefined
  searchForm.followType = ''
  searchForm.dateRange = null
  handleSearch()
}

/**
 * 重置表单数据
 * @description 将表单字段恢复为初始值
 */
function resetForm() {
  form.id = undefined
  form.customerId = undefined
  form.followType = ''
  form.content = ''
  form.nextVisitDate = ''
}

/**
 * 新增跟进记录操作
 * @description 重置表单并打开新增弹窗
 */
function handleAdd() {
  resetForm()
  dialogTitle.value = '新增跟进记录'
  dialogVisible.value = true
}

/**
 * 编辑跟进记录操作
 * @param row 当前行数据
 * @description 填充表单数据并打开编辑弹窗
 */
function handleEdit(row: any) {
  resetForm()
  dialogTitle.value = '编辑跟进记录'
  Object.assign(form, row)
  dialogVisible.value = true
}

/**
 * 删除跟进记录操作
 * @param row 当前行数据
 * @description 二次确认后调用删除接口
 * @throws 用户取消删除时不执行操作
 */
async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定要删除该跟进记录吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await followDelete(row.id)
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
    await followUpdate({ ...form })
    ElMessage.success('编辑成功')
  } else {
    await followAdd({ ...form })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadCustomerOptions()
  loadData()
})
</script>

<style scoped>
.follow-container {
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
