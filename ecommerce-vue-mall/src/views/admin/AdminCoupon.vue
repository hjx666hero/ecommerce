<template>
  <div class="admin-coupon-page">
    <div class="page-header">
      <h2>优惠券管理</h2>
      <el-button type="primary" @click="showForm('add')" round>
        <el-icon style="margin-right:4px"><Plus /></el-icon>新增优惠券
      </el-button>
    </div>

    <div v-loading="loading" class="content">
      <el-table :data="coupons" stripe style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="优惠券名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeColor(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="面值" width="100">
          <template #default="{ row }">
            <span v-if="row.type === 2">{{ (row.discountValue * 10).toFixed(1) }}折</span>
            <span v-else>¥{{ row.discountValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="使用门槛" width="100">
          <template #default="{ row }">
            <span v-if="row.type === 3">无门槛</span>
            <span v-else>满¥{{ row.minAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已领/总量" width="100">
          <template #default="{ row }">
            <span :class="{ 'exhausted': row.receiveCount >= row.totalCount }">
              {{ row.receiveCount }} / {{ row.totalCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="每人限领" width="80" prop="perLimit" />
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <span class="date-text">{{ formatDate(row.startTime) }} ~ {{ formatDate(row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showForm('edit', row)">编辑</el-button>
            <el-button size="small" type="success" link @click="showDistribute(row)">发放</el-button>
            <el-popconfirm title="确定删除该优惠券？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="formMode === 'add' ? '新增优惠券' : '编辑优惠券'" width="540px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="form.name" placeholder="如：618满减券" maxlength="50" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">满减券</el-radio>
            <el-radio :value="2">折扣券</el-radio>
            <el-radio :value="3">无门槛券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠面值" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="0.01" :precision="2" :step="10" style="width: 200px" />
          <span class="form-hint" v-if="form.type === 2">（如 0.85 表示 8.5折）</span>
          <span class="form-hint" v-else>（单位：元）</span>
        </el-form-item>
        <el-form-item label="使用门槛" prop="minAmount" v-if="form.type !== 3">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" :step="50" style="width: 200px" />
          <span class="form-hint">满多少元可用</span>
        </el-form-item>
        <el-form-item label="发放总量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" :max="999999" style="width: 200px" />
        </el-form-item>
        <el-form-item label="每人限领" prop="perLimit">
          <el-input-number v-model="form.perLimit" :min="1" :max="10" style="width: 200px" />
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 发放优惠券弹窗 -->
    <el-dialog v-model="distributeVisible" title="发放优惠券" width="500px" destroy-on-close>
      <el-form :model="distributeForm" ref="distributeFormRef" label-width="100px">
        <el-form-item label="优惠券">
          <el-tag type="primary">{{ distributeCoupon?.name }}</el-tag>
        </el-form-item>
        <el-form-item label="发放方式">
          <el-radio-group v-model="distributeForm.mode">
            <el-radio value="all">全量发放（所有用户）</el-radio>
            <el-radio value="specific">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用户ID" v-if="distributeForm.mode === 'specific'">
          <el-input
            v-model="distributeForm.userIdsText"
            type="textarea"
            :rows="3"
            placeholder="请输入用户ID，多个用逗号分隔，如：1,2,3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="distributeVisible = false">取消</el-button>
        <el-button type="primary" :loading="distributing" @click="handleDistribute">确认发放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminCouponList, saveAdminCoupon, deleteAdminCoupon, batchDistributeCoupon } from '@/api/admin'
import { formatDate } from '@/utils'

const loading = ref(false)
const saving = ref(false)
const distributing = ref(false)
const coupons = ref([])
const dialogVisible = ref(false)
const distributeVisible = ref(false)
const formMode = ref('add')
const formRef = ref(null)
const distributeCoupon = ref(null)

const form = reactive({
  name: '',
  type: 1,
  discountValue: 10,
  minAmount: 100,
  totalCount: 100,
  perLimit: 1,
  dateRange: [],
  enabled: true
})

const distributeForm = reactive({
  mode: 'all',
  userIdsText: ''
})

const rules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入优惠面值', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入发放总量', trigger: 'blur' }],
  perLimit: [{ required: true, message: '请输入每人限领数', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

function typeLabel(type) {
  const map = { 1: '满减券', 2: '折扣券', 3: '无门槛券' }
  return map[type] || '未知'
}

function typeColor(type) {
  const map = { 1: 'danger', 2: 'warning', 3: 'success' }
  return map[type] || 'info'
}

async function fetchCoupons() {
  loading.value = true
  try {
    const res = await getAdminCouponList({ page: 1, size: 100 })
    coupons.value = res.data?.records || res.data || []
  } catch {} finally {
    loading.value = false
  }
}

function showForm(mode, row) {
  formMode.value = mode
  if (mode === 'edit' && row) {
    form.id = row.id
    form.name = row.name
    form.type = row.type
    form.discountValue = row.discountValue
    form.minAmount = row.minAmount || 0
    form.totalCount = row.totalCount
    form.perLimit = row.perLimit
    form.dateRange = [row.startTime, row.endTime]
    form.enabled = row.status === 1
  } else {
    Object.assign(form, {
      id: null, name: '', type: 1, discountValue: 10, minAmount: 100,
      totalCount: 100, perLimit: 1, dateRange: [], enabled: true
    })
  }
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const data = {
      id: form.id,
      name: form.name,
      type: form.type,
      discountValue: form.discountValue,
      minAmount: form.type === 3 ? 0 : form.minAmount,
      totalCount: form.totalCount,
      receiveCount: 0,
      perLimit: form.perLimit,
      startTime: form.dateRange[0],
      endTime: form.dateRange[1],
      status: form.enabled ? 1 : 0
    }
    await saveAdminCoupon(data)
    ElMessage.success(formMode.value === 'add' ? '创建成功' : '更新成功')
    dialogVisible.value = false
    fetchCoupons()
  } catch {} finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  try {
    await saveAdminCoupon({ ...row, status: row.status === 1 ? 0 : 1 })
    ElMessage.success('状态已更新')
    fetchCoupons()
  } catch {}
}

async function handleDelete(id) {
  try {
    await deleteAdminCoupon(id)
    ElMessage.success('删除成功')
    fetchCoupons()
  } catch {}
}

function showDistribute(row) {
  distributeCoupon.value = row
  distributeForm.mode = 'all'
  distributeForm.userIdsText = ''
  distributeVisible.value = true
}

async function handleDistribute() {
  distributing.value = true
  try {
    let userIds = null
    if (distributeForm.mode === 'specific') {
      userIds = distributeForm.userIdsText.split(',')
        .map(s => Number(s.trim()))
        .filter(n => !isNaN(n) && n > 0)
      if (userIds.length === 0) {
        ElMessage.warning('请输入有效的用户ID')
        distributing.value = false
        return
      }
    }
    const res = await batchDistributeCoupon(distributeCoupon.value.id, userIds)
    ElMessage.success(`成功发放 ${res.data} 张优惠券`)
    distributeVisible.value = false
    fetchCoupons()
  } catch {} finally {
    distributing.value = false
  }
}

onMounted(() => {
  fetchCoupons()
})
</script>

<style scoped>
.admin-coupon-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 22px; font-weight: 700; margin: 0; }
.form-hint { color: #909399; font-size: 12px; margin-left: 8px; }
.exhausted { color: #f56c6c; font-weight: 700; }
.date-text { font-size: 12px; color: #606266; }
</style>
