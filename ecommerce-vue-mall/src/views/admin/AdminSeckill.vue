<template>
  <div class="admin-seckill-page">
    <div class="page-header">
      <h2>秒杀管理</h2>
      <el-button type="danger" @click="showForm('add')" round>
        <el-icon style="margin-right:4px"><Plus /></el-icon>新增秒杀活动
      </el-button>
    </div>

    <div v-loading="loading" class="content">
      <el-table :data="activities" stripe style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="活动名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="spuName" label="商品名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="秒杀价" width="120">
          <template #default="{ row }">
            <span class="seckill-price">¥{{ row.seckillPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="80" prop="stock" />
        <el-table-column label="限购" width="70" prop="limitPerUser">
          <template #default="{ row }">每人{{ row.limitPerUser }}件</template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">
            <span class="date-text">{{ formatDate(row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">
            <span class="date-text">{{ formatDate(row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showForm('edit', row)">编辑</el-button>
            <el-popconfirm title="确定删除该活动？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="formMode === 'add' ? '新增秒杀活动' : '编辑秒杀活动'" width="560px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="如：iPhone限时秒杀" maxlength="50" />
        </el-form-item>
        <el-form-item label="商品SPU ID" prop="spuId">
          <el-input-number v-model="form.spuId" :min="1" placeholder="选择秒杀商品" style="width: 200px" />
          <span class="form-hint">商品对应的SPU ID</span>
        </el-form-item>
        <el-form-item label="SKU ID" prop="skuId">
          <el-input-number v-model="form.skuId" :min="1" placeholder="秒杀规格SKU ID" style="width: 200px" />
          <span class="form-hint">秒杀的商品规格SKU ID</span>
        </el-form-item>
        <el-form-item label="秒杀价" prop="seckillPrice">
          <el-input-number v-model="form.seckillPrice" :min="0.01" :precision="2" style="width: 200px" />
          <span class="form-hint">（单位：元）</span>
        </el-form-item>
        <el-form-item label="秒杀库存" prop="stock">
          <el-input-number v-model="form.stock" :min="1" :max="99999" style="width: 200px" />
        </el-form-item>
        <el-form-item label="每人限购" prop="limitPerUser">
          <el-input-number v-model="form.limitPerUser" :min="1" :max="10" style="width: 200px" />
        </el-form-item>
        <el-form-item label="活动时间" prop="dateRange">
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
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminSeckillList, saveAdminSeckill, deleteAdminSeckill } from '@/api/admin'
import { formatDate } from '@/utils'

const loading = ref(false)
const saving = ref(false)
const activities = ref([])
const dialogVisible = ref(false)
const formMode = ref('add')
const formRef = ref(null)

const form = reactive({
  name: '',
  spuId: 1,
  skuId: 1,
  seckillPrice: 0.01,
  stock: 100,
  limitPerUser: 1,
  dateRange: [],
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  spuId: [{ required: true, message: '请输入商品SPU ID', trigger: 'blur' }],
  skuId: [{ required: true, message: '请输入SKU ID', trigger: 'blur' }],
  seckillPrice: [{ required: true, message: '请输入秒杀价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入秒杀库存', trigger: 'blur' }],
  limitPerUser: [{ required: true, message: '请输入限购数量', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

async function fetchActivities() {
  loading.value = true
  try {
    const res = await getAdminSeckillList({ page: 1, size: 100 })
    activities.value = res.data?.records || res.data || []
  } catch {} finally {
    loading.value = false
  }
}

function showForm(mode, row) {
  formMode.value = mode
  if (mode === 'edit' && row) {
    Object.assign(form, {
      id: row.id,
      name: row.name,
      spuId: row.spuId,
      skuId: row.skuId,
      seckillPrice: row.seckillPrice,
      stock: row.stock,
      limitPerUser: row.limitPerUser,
      dateRange: [row.startTime, row.endTime],
      status: row.status
    })
  } else {
    Object.assign(form, {
      id: null, name: '', spuId: 1, skuId: 1,
      seckillPrice: 0.01, stock: 100, limitPerUser: 1,
      dateRange: [], status: 1
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
      spuId: form.spuId,
      skuId: form.skuId,
      seckillPrice: form.seckillPrice,
      stock: form.stock,
      limitPerUser: form.limitPerUser,
      startTime: form.dateRange[0],
      endTime: form.dateRange[1],
      status: form.status
    }
    await saveAdminSeckill(data)
    ElMessage.success(formMode.value === 'add' ? '创建成功' : '更新成功')
    dialogVisible.value = false
    fetchActivities()
  } catch {} finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteAdminSeckill(id)
    ElMessage.success('删除成功')
    fetchActivities()
  } catch {}
}

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.admin-seckill-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 22px; font-weight: 700; margin: 0; }
.form-hint { color: #909399; font-size: 12px; margin-left: 8px; }
.seckill-price { color: #f56c6c; font-weight: 700; font-size: 15px; }
.date-text { font-size: 12px; color: #606266; }
</style>
