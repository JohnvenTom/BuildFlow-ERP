<template>
  <div class="payment-receive-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="回款单号">
          <el-input v-model="searchForm.receiveNo" placeholder="请输入回款单号" clearable />
        </el-form-item>
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
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="草稿" />
            <el-option label="已审核" value="已审核" />
            <el-option label="已作废" value="已作废" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>客户回款单列表</span>
          <el-button type="primary" @click="handleAdd">新增回款单</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="receiveNo" label="回款单号" width="160" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="amount" label="回款金额" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="payMethod" label="付款方式" width="100" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '草稿'" type="success" link @click="handleAudit(row)">审核</el-button>
            <el-button v-if="row.status === '草稿'" type="danger" link @click="handleVoid(row)">作废</el-button>
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

    <!-- 新增弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增回款单"
      width="950px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" placeholder="请选择客户" style="width: 100%" filterable @change="handleCustomerChange">
            <el-option
              v-for="item in customerOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="付款方式" prop="payMethod">
          <el-select v-model="form.payMethod" placeholder="请选择付款方式" style="width: 100%">
            <el-option label="现金" value="现金" />
            <el-option label="转账" value="转账" />
            <el-option label="支票" value="支票" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>

        <!-- 核销明细表格 -->
        <el-form-item label="核销明细">
          <el-table :data="form.details" border style="width: 100%">
            <el-table-column label="应收台账" min-width="220">
              <template #default="{ row }">
                <el-select v-model="row.receivableId" placeholder="请选择应收台账" style="width: 100%" filterable>
                  <el-option
                    v-for="item in receivableOptions"
                    :key="item.id"
                    :label="`${item.sourceNo}（剩余：${formatAmount(item.remainingAmount)}）`"
                    :value="item.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="剩余金额" width="130">
              <template #default="{ row }">
                {{ formatAmount(getReceivableRemaining(row.receivableId)) }}
              </template>
            </el-table-column>
            <el-table-column label="本次核销金额" width="160">
              <template #default="{ row }">
                <el-input-number v-model="row.writeOffAmount" :min="0" :precision="2" :step="100" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeDetail($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" link @click="addDetail" style="margin-top: 8px">+ 添加核销明细</el-button>
        </el-form-item>

        <!-- 核销汇总 -->
        <el-form-item label="核销总额">
          <span class="total-amount">{{ formatAmount(totalWriteOffAmount) }}</span>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { paymentReceivePage, paymentReceiveAdd, paymentReceiveAudit, paymentReceiveVoid, receivablePage } from '@/api/finance'
import { customerList } from '@/api/crm'

/** 搜索表单数据 */
const searchForm = reactive({
  receiveNo: '',
  customerId: undefined as number | undefined,
  status: ''
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

/** 新增弹窗显示状态 */
const dialogVisible = ref(false)
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 客户下拉选项 */
const customerOptions = ref<any[]>([])
/** 应收台账下拉选项 */
const receivableOptions = ref<any[]>([])

/** 核销明细行数据类型 */
interface WriteOffDetail {
  receivableId: number | undefined
  writeOffAmount: number
}

/** 回款单表单数据 */
const form = reactive({
  customerId: undefined as number | undefined,
  payMethod: '',
  remark: '',
  details: [] as WriteOffDetail[]
})

/** 表单校验规则 */
const formRules = reactive<FormRules>({
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  payMethod: [{ required: true, message: '请选择付款方式', trigger: 'change' }]
})

/**
 * 计算核销总额（自动汇总明细核销金额）
 * @returns 所有明细核销金额之和
 */
const totalWriteOffAmount = computed(() => {
  return form.details.reduce((sum, item) => sum + (item.writeOffAmount || 0), 0)
})

/**
 * 获取状态对应的Tag类型
 * @param status 回款单状态
 * @returns Element Plus Tag类型
 */
function statusTagType(status: string): string {
  if (status === '草稿') return 'info'
  if (status === '已审核') return 'success'
  if (status === '已作废') return 'danger'
  return ''
}

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
 * 加载回款单列表数据
 * @description 根据搜索条件和分页参数请求回款单分页数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await paymentReceivePage({
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
 * 加载客户下拉选项
 * @description 请求全部客户列表填充下拉选项
 */
async function loadCustomerOptions() {
  const res = await customerList()
  customerOptions.value = res.data || []
}

/**
 * 根据客户ID加载应收台账选项
 * @description 请求指定客户的应收台账（未结清）填充核销明细下拉
 * @param customerId 客户ID
 */
async function loadReceivableOptions(customerId: number) {
  const res = await receivablePage({ customerId, status: '正常', pageNum: 1, pageSize: 999 })
  receivableOptions.value = res.data?.rows || []
}

/**
 * 根据应收台账ID获取剩余金额
 * @param receivableId 应收台账ID
 * @returns 剩余金额，未找到返回0
 */
function getReceivableRemaining(receivableId: number | undefined): number {
  if (!receivableId) return 0
  const item = receivableOptions.value.find((r) => r.id === receivableId)
  return item?.remainingAmount || 0
}

/**
 * 客户选择变更处理
 * @description 切换客户时清空核销明细并重新加载该客户的应收台账
 * @param customerId 选中的客户ID
 */
async function handleCustomerChange(customerId: number | undefined) {
  form.details = []
  receivableOptions.value = []
  if (customerId) {
    await loadReceivableOptions(customerId)
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
  searchForm.receiveNo = ''
  searchForm.customerId = undefined
  searchForm.status = ''
  handleSearch()
}

/**
 * 重置表单数据
 * @description 将表单字段恢复为初始值
 */
function resetForm() {
  form.customerId = undefined
  form.payMethod = ''
  form.remark = ''
  form.details = []
  receivableOptions.value = []
}

/**
 * 新增回款单操作
 * @description 重置表单并打开新增弹窗
 */
function handleAdd() {
  resetForm()
  dialogVisible.value = true
}

/**
 * 添加核销明细行
 * @description 在核销明细表格末尾新增一行空白明细
 */
function addDetail() {
  if (!form.customerId) {
    ElMessage.warning('请先选择客户')
    return
  }
  form.details.push({
    receivableId: undefined,
    writeOffAmount: 0
  })
}

/**
 * 删除核销明细行
 * @param index 明细行索引
 * @description 移除指定索引的核销明细行
 */
function removeDetail(index: number) {
  form.details.splice(index, 1)
}

/**
 * 审核回款单操作
 * @param row 当前行数据
 * @description 二次确认"审核后应收将减少"后调用审核接口
 * @throws 用户取消审核时不执行操作
 */
async function handleAudit(row: any) {
  await ElMessageBox.confirm('审核后应收将减少，是否确认？', '审核确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await paymentReceiveAudit(row.id)
  ElMessage.success('审核成功')
  loadData()
}

/**
 * 作废回款单操作
 * @param row 当前行数据
 * @description 二次确认后调用作废接口
 * @throws 用户取消作废时不执行操作
 */
async function handleVoid(row: any) {
  await ElMessageBox.confirm(`确定要作废回款单「${row.receiveNo}」吗？`, '作废确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await paymentReceiveVoid(row.id)
  ElMessage.success('作废成功')
  loadData()
}

/**
 * 提交表单操作
 * @description 校验表单并检查核销明细数据后调用新增接口
 * @throws 表单校验失败或明细数据异常时终止提交
 */
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (form.details.length === 0) {
    ElMessage.warning('请至少添加一条核销明细')
    return
  }
  const hasEmptyReceivable = form.details.some((d) => !d.receivableId)
  if (hasEmptyReceivable) {
    ElMessage.warning('请选择核销明细中的应收台账')
    return
  }
  const hasZeroAmount = form.details.some((d) => !d.writeOffAmount || d.writeOffAmount <= 0)
  if (hasZeroAmount) {
    ElMessage.warning('核销金额必须大于0')
    return
  }
  // 按后端 FinPaymentReceiveDTO 结构组装：主表 receive + 明细 items
  await paymentReceiveAdd({
    receive: {
      customerId: form.customerId,
      payMethod: form.payMethod,
      remark: form.remark
    },
    items: form.details.map((d) => ({
      receivableId: d.receivableId,
      writeOffAmount: d.writeOffAmount
    }))
  })
  ElMessage.success('新增成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadCustomerOptions()
  loadData()
})
</script>

<style scoped>
.payment-receive-container {
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

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}
</style>
