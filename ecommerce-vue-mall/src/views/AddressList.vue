<template>
  <div class="address-list-page">
    <div class="page-header-bar">
      <div class="header-left">
        <h3 class="page-title">地址管理</h3>
        <p class="page-subtitle">管理您的收货地址，最多可添加20个地址</p>
      </div>
      <el-button type="primary" @click="showForm('add')" round>
        <span class="btn-plus">+</span>
        新增地址
      </el-button>
    </div>

    <div v-loading="loading">
      <div v-if="addresses.length > 0" class="address-cards">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-card"
          :class="{ 'is-default': addr.isDefault === 1 }"
        >
          <div class="card-top">
            <div class="addr-radio-icon">
              <span v-if="addr.isDefault === 1" class="radio-dot" />
            </div>
            <div class="addr-info">
              <div class="addr-contact">
                <span class="name">{{ addr.receiverName }}</span>
                <span class="phone">{{ addr.receiverPhone }}</span>
                <span v-if="addr.isDefault === 1" class="default-badge">默认</span>
              </div>
              <p class="addr-detail">
                {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
              </p>
            </div>
            <div class="addr-actions">
              <button v-if="addr.isDefault !== 1" class="action-btn set-default-btn" @click="handleSetDefault(addr)" title="设为默认">
                <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
              </button>
              <button class="action-btn edit-btn" @click="showForm('edit', addr)" title="编辑">
                <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
              <button class="action-btn delete-btn" @click="handleDelete(addr)" title="删除">
                <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
              </button>
            </div>
          </div>
        </div>

        <div class="add-address-card" @click="showForm('add')">
          <div class="add-content">
            <el-icon :size="32" color="#c0c4cc"><Plus /></el-icon>
            <span class="add-text">新增地址</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" width="80" height="80" fill="none" stroke="#dcdfe6" stroke-width="1.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
        </div>
        <p class="empty-text">暂无收货地址</p>
        <p class="empty-hint">添加一个收货地址，让购物更便捷</p>
        <el-button type="primary" round size="large" @click="showForm('add')">添加新地址</el-button>
      </div>
    </div>

    <!-- 新增/编辑地址弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formMode === 'add' ? '新增地址' : '编辑地址'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="请输入11位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="form.region"
            :options="regionData"
            :props="{ checkStrictly: false }"
            placeholder="请选择省/市"
            style="width: 100%"
            clearable
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="街道、门牌号等（无需重复填写省市区）" maxlength="100" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/user'
import { regionData } from '@/utils/region'

const loading = ref(false)
const saving = ref(false)
const addresses = ref([])
const dialogVisible = ref(false)
const formMode = ref('add')
const editId = ref(null)
const formRef = ref(null)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detail: '',
  isDefault: false
})

const formRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  region: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

async function fetchAddresses() {
  loading.value = true
  try {
    const res = await getAddressList()
    addresses.value = res.data || []
  } catch {} finally {
    loading.value = false
  }
}

function showForm(mode, addr) {
  formMode.value = mode
  if (mode === 'edit' && addr) {
    editId.value = addr.id
    form.receiverName = addr.receiverName
    form.receiverPhone = addr.receiverPhone
    form.region = [addr.province, addr.city]
    form.detail = addr.detail
    form.isDefault = addr.isDefault === 1
  } else {
    editId.value = null
    form.receiverName = ''
    form.receiverPhone = ''
    form.region = []
    form.detail = ''
    form.isDefault = false
  }
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const [province, city] = form.region || ['', '']
    const data = {
      receiverName: form.receiverName,
      receiverPhone: form.receiverPhone,
      province,
      city,
      district: city,
      detail: form.detail,
      isDefault: form.isDefault ? 1 : 0
    }
    if (formMode.value === 'add') {
      await addAddress(data)
      ElMessage.success('地址添加成功')
    } else {
      data.id = editId.value
      await updateAddress(data)
      ElMessage.success('地址更新成功')
    }
    dialogVisible.value = false
    fetchAddresses()
  } catch {} finally {
    saving.value = false
  }
}

async function handleDelete(addr) {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', { type: 'warning' })
    await deleteAddress(addr.id)
    ElMessage.success('地址已删除')
    fetchAddresses()
  } catch {}
}

async function handleSetDefault(addr) {
  try {
    await setDefaultAddress(addr.id)
    ElMessage.success('已设为默认地址')
    fetchAddresses()
  } catch {}
}

onMounted(() => {
  fetchAddresses()
})
</script>

<style scoped>
:root {
  --primary: #409EFF;
  --primary-light: #ecf5ff;
  --accent: #f56c6c;
  --text-primary: #303133;
  --text-secondary: #909399;
  --bg-white: #ffffff;
  --shadow-sm: 0 2px 12px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 20px rgba(0, 0, 0, 0.1);
  --radius-md: 12px;
  --radius-lg: 16px;
  --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.address-list-page {
  min-height: 300px;
}

.page-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

.btn-plus {
  display: inline-block;
  font-size: 18px;
  font-weight: 600;
  margin-right: 4px;
  line-height: 1;
  vertical-align: -1px;
}

.address-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.address-card {
  background: var(--bg-white);
  border: 2px solid #ebeef5;
  border-radius: var(--radius-md);
  padding: 20px 24px;
  transition: var(--transition);
  cursor: default;
  position: relative;
}

.address-card:hover {
  border-color: #c6d9f0;
  box-shadow: var(--shadow-sm);
}

.address-card.is-default {
  border-color: #fbc4c4;
  background: linear-gradient(135deg, #fff5f5, #fffafa);
}

.card-top {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.addr-radio-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 3px;
  flex-shrink: 0;
  transition: var(--transition);
}

.address-card.is-default .addr-radio-icon {
  border-color: #f56c6c;
  background: #fef0f0;
}

.radio-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #f56c6c;
}

.addr-info {
  flex: 1;
  min-width: 0;
}

.addr-contact {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.addr-contact .name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.addr-contact .phone {
  font-size: 14px;
  color: var(--text-secondary);
}

.default-badge {
  display: inline-block;
  background: linear-gradient(135deg, #ff4444, #f56c6c);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 10px;
  letter-spacing: 0.5px;
}

.addr-detail {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.addr-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
  padding-left: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  transition: var(--transition);
  color: var(--text-secondary);
}

.edit-btn:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.set-default-btn:hover {
  background: #e1f3d8;
  color: #67c23a;
}

.delete-btn:hover {
  background: #fef0f0;
  color: #f56c6c;
}

.add-address-card {
  min-height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: var(--transition);
  background: #fafbfc;
}

.add-address-card:hover {
  border-color: var(--primary);
  background: var(--primary-light);
}

.add-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.add-icon-plus {
  font-size: 32px;
  font-weight: 300;
  color: #c0c4cc;
  line-height: 1;
}

.add-address-card:hover .add-icon-plus {
  color: var(--primary);
}

.add-text {
  font-size: 15px;
  color: var(--text-secondary);
  font-weight: 500;
}

.add-address-card:hover .add-text {
  color: var(--primary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.empty-icon {
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-text {
  font-size: 18px;
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}
</style>