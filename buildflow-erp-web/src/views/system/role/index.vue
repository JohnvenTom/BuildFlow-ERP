<template>
  <div class="role-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.keyword" placeholder="请输入角色名称" clearable />
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
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <el-table :data="filteredTableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="角色名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="code" label="角色编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button type="warning" link @click="handleAssignMenu(row)">分配菜单</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单弹窗 -->
    <el-dialog
      v-model="menuDialogVisible"
      title="分配菜单"
      width="480px"
      destroy-on-close
    >
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedMenuIds"
        :default-expand-all="true"
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="menuSaving" @click="handleMenuSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { roleList, roleAdd, roleUpdate, roleDelete, getRoleMenus, assignRoleMenus, menuTree } from '@/api/system'

/** 搜索表单数据 */
const searchForm = reactive({
  keyword: ''
})

/** 表格数据（角色列表接口返回全量数据） */
const tableData = ref<any[]>([])
/** 加载状态 */
const loading = ref(false)

/** 根据关键词过滤后的表格数据 */
const filteredTableData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  const kw = searchForm.keyword.toLowerCase()
  return tableData.value.filter(
    (item) => item.name?.toLowerCase().includes(kw) || item.code?.toLowerCase().includes(kw)
  )
})

/** 弹窗显示状态 */
const dialogVisible = ref(false)
/** 弹窗标题 */
const dialogTitle = ref('新增角色')
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 表单数据 */
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  code: '',
  description: ''
})

/** 表单校验规则 */
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
})

/** 分配菜单弹窗显示状态 */
const menuDialogVisible = ref(false)
/** 当前分配菜单的角色ID */
const currentRoleId = ref<number>(0)
/** 菜单树数据 */
const menuTreeData = ref<any[]>([])
/** 已勾选的菜单ID列表 */
const checkedMenuIds = ref<number[]>([])
/** 菜单树组件引用 */
const menuTreeRef = ref<any>()
/** 菜单保存中状态 */
const menuSaving = ref(false)

/**
 * 加载角色列表数据
 * @description 请求角色全量列表数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await roleList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

/**
 * 加载菜单树数据
 * @description 请求菜单树形结构数据，用于分配菜单弹窗
 */
async function loadMenuTree() {
  const res = await menuTree()
  menuTreeData.value = res.data || []
}

/**
 * 搜索操作
 * @description 前端过滤关键词匹配，无需重新请求接口
 */
function handleSearch() {
  // 角色列表为全量数据，搜索通过 computed 前端过滤
}

/**
 * 重置搜索条件
 * @description 清空搜索关键词
 */
function handleReset() {
  searchForm.keyword = ''
}

/**
 * 重置表单数据
 * @description 将表单字段恢复为初始值
 */
function resetForm() {
  form.id = undefined
  form.name = ''
  form.code = ''
  form.description = ''
}

/**
 * 新增角色操作
 * @description 重置表单并打开新增弹窗
 */
function handleAdd() {
  resetForm()
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

/**
 * 编辑角色操作
 * @param row 当前行数据
 * @description 填充表单数据并打开编辑弹窗
 */
function handleEdit(row: any) {
  resetForm()
  dialogTitle.value = '编辑角色'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description
  })
  dialogVisible.value = true
}

/**
 * 删除角色操作
 * @param row 当前行数据
 * @description 二次确认后调用删除接口
 * @throws 用户取消删除时不执行操作
 */
async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定要删除角色「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await roleDelete(row.id)
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
    await roleUpdate({ ...form, status: 1 })
    ElMessage.success('编辑成功')
  } else {
    await roleAdd({ ...form, status: 1 })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

/**
 * 分配菜单操作
 * @param row 当前行数据
 * @description 打开分配菜单弹窗，加载菜单树和角色已分配的菜单ID
 */
async function handleAssignMenu(row: any) {
  currentRoleId.value = row.id
  checkedMenuIds.value = []
  menuDialogVisible.value = true
  // 并行加载菜单树和角色已有菜单
  const [, menuRes] = await Promise.all([
    loadMenuTree(),
    getRoleMenus(row.id)
  ])
  checkedMenuIds.value = menuRes.data || []
}

/**
 * 提交分配菜单操作
 * @description 获取当前勾选的菜单ID（含半选父节点），调用分配接口保存
 * @throws 保存失败时提示错误信息
 */
async function handleMenuSubmit() {
  menuSaving.value = true
  try {
    const tree = menuTreeRef.value
    // 获取全选节点和半选节点ID
    const checkedKeys = tree.getCheckedKeys() as number[]
    const halfCheckedKeys = tree.getHalfCheckedKeys() as number[]
    const menuIds = [...checkedKeys, ...halfCheckedKeys]
    await assignRoleMenus(currentRoleId.value, menuIds)
    ElMessage.success('分配菜单成功')
    menuDialogVisible.value = false
  } finally {
    menuSaving.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.role-container {
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
</style>
