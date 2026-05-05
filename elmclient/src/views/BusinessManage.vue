<template>
  <div class="manage-page">
    <header class="page-header">
      <p>店铺设置</p>
    </header>

    <main class="page-body">
      <section v-if="!businesses.length" class="empty-card">
        <p>还没有店铺，先去创建店铺。</p>
        <button class="primary-btn" @click="$router.push('/business')">去创建</button>
      </section>

      <template v-else>
        <section class="panel-card">
          <div v-if="businesses.length > 1" class="business-switcher">
            <button
              v-for="business in businesses"
              :key="business.id"
              class="switch-item"
              :class="{ active: business.id === currentBusinessId }"
              @click="selectBusiness(business)"
            >
              {{ business.businessName }}
            </button>
          </div>

          <div class="logo-block">
            <img :src="form.businessImg || defaultShopLogo" alt="logo" class="logo-image">
            <input
              ref="logoInput"
              type="file"
              accept="image/*"
              class="hidden-input"
              @change="handleLogoChange"
            >
            <button class="secondary-btn" @click="$refs.logoInput.click()">上传店铺图</button>
          </div>

          <div class="form-grid">
            <input v-model.trim="form.businessName" type="text" placeholder="店铺名称">
            <input v-model.trim="form.businessAddress" type="text" placeholder="店铺地址">
            <textarea v-model.trim="form.businessExplain" rows="3" placeholder="店铺简介"></textarea>

            <select v-model.number="form.orderTypeId">
              <option v-for="item in orderTypeOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>

            <input v-model.number="form.startPrice" type="number" min="0" step="0.01" placeholder="起送价">
            <input v-model.number="form.deliveryPrice" type="number" min="0" step="0.01" placeholder="配送费">
            <textarea v-model.trim="form.remarks" rows="2" placeholder="备注（可选）"></textarea>
          </div>

          <div class="action-stack">
            <button class="primary-btn" :disabled="saving" @click="saveBusiness">
              {{ saving ? '保存中...' : '保存修改' }}
            </button>
            <button class="secondary-btn ghost" @click="logout">退出登录</button>
          </div>
        </section>
      </template>
    </main>

    <div class="merchant-footer">
      <div class="footer-item" @click="$router.push('/business')">
        <i class="fa fa-home"></i>
        <span>首页</span>
      </div>
      <div class="footer-item" @click="$router.push('/businessOrders')">
        <i class="fa fa-list-alt"></i>
        <span>订单</span>
      </div>
      <div class="footer-item" @click="$router.push('/wallet')">
        <i class="fa fa-credit-card"></i>
        <span>钱包</span>
      </div>
      <div class="footer-item active" @click="$router.push('/businessManage')">
        <i class="fa fa-user"></i>
        <span>管理</span>
      </div>
    </div>
  </div>
</template>

<script>
import defaultShopLogo from '../assets/sj01.png'
import auth from '../utils/auth'

const ORDER_TYPE_OPTIONS = [
  { value: 1, label: '美食' },
  { value: 2, label: '早餐' },
  { value: 3, label: '跑腿' },
  { value: 4, label: '汉堡披萨' },
  { value: 5, label: '甜品饮品' },
  { value: 6, label: '速食简餐' },
  { value: 7, label: '地方小吃' },
  { value: 8, label: '米粉面馆' },
  { value: 9, label: '包子粥店' },
  { value: 10, label: '炸鸡炸串' }
]

function createForm() {
  return {
    id: null,
    userId: '',
    businessName: '',
    businessAddress: '',
    businessExplain: '',
    businessImg: '',
    orderTypeId: 1,
    startPrice: 0,
    deliveryPrice: 0,
    remarks: ''
  }
}

export default {
  name: 'BusinessManage',
  data() {
    return {
      defaultShopLogo,
      orderTypeOptions: ORDER_TYPE_OPTIONS,
      businesses: [],
      currentBusinessId: null,
      form: createForm(),
      saving: false
    }
  },
  created() {
    this.loadBusinesses()
  },
  methods: {
    buildHeaders() {
      const token = auth.getToken()
      return token ? { Authorization: `Bearer ${token}` } : {}
    },
    getCurrentUserId() {
      const user = auth.getUserInfo() || {}
      return user.userId || user.id || ''
    },
    async loadBusinesses(preferredId = null) {
      const userId = this.getCurrentUserId()
      if (!userId) {
        return
      }

      try {
        const response = await this.$axios.get(`/api/businesses/user/${userId}`, {
          headers: this.buildHeaders()
        })
        const list = Array.isArray(response.data.data) ? response.data.data : []
        this.businesses = list
        if (!list.length) {
          this.form = createForm()
          return
        }

        const target = list.find(item => item.id === preferredId) || list.find(item => item.id === this.currentBusinessId) || list[0]
        this.selectBusiness(target)
      } catch (error) {
        console.error('load businesses failed', error)
        alert('加载店铺失败')
      }
    },
    selectBusiness(business) {
      if (!business) {
        return
      }
      this.currentBusinessId = business.id
      this.form = {
        id: business.id,
        userId: business.userId || this.getCurrentUserId(),
        businessName: business.businessName || '',
        businessAddress: business.businessAddress || '',
        businessExplain: business.businessExplain || '',
        businessImg: business.businessImg || '',
        orderTypeId: business.orderTypeId || 1,
        startPrice: Number(business.startPrice || 0),
        deliveryPrice: Number(business.deliveryPrice || 0),
        remarks: business.remarks || ''
      }
    },
    async handleLogoChange(event) {
      const file = event.target.files && event.target.files[0]
      if (!file) {
        return
      }
      if (!file.type.startsWith('image/')) {
        alert('请选择图片文件')
        return
      }
      if (file.size > 2 * 1024 * 1024) {
        alert('图片不能超过 2MB')
        return
      }

      this.form.businessImg = await this.readImageAsDataUrl(file)
    },
    readImageAsDataUrl(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = event => resolve(event.target.result)
        reader.onerror = reject
        reader.readAsDataURL(file)
      })
    },
    async saveBusiness() {
      if (!this.form.id) {
        return
      }
      if (!this.form.businessName) {
        alert('请输入店铺名称')
        return
      }
      if (!this.form.businessAddress) {
        alert('请输入店铺地址')
        return
      }

      this.saving = true
      try {
        const payload = {
          businessName: this.form.businessName,
          businessAddress: this.form.businessAddress,
          businessExplain: this.form.businessExplain,
          businessImg: this.form.businessImg,
          orderTypeId: this.form.orderTypeId,
          startPrice: Number(this.form.startPrice || 0),
          deliveryPrice: Number(this.form.deliveryPrice || 0),
          remarks: this.form.remarks,
          userId: this.form.userId || this.getCurrentUserId()
        }

        const response = await this.$axios.put(`/api/businesses/${this.form.id}`, payload, {
          headers: this.buildHeaders()
        })
        if (!response.data.success) {
          throw new Error(response.data.message || 'save business failed')
        }

        await this.loadBusinesses(this.form.id)
      } catch (error) {
        console.error('save business failed', error)
        alert('保存店铺失败')
      } finally {
        this.saving = false
      }
    },
    logout() {
      auth.logout()
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.manage-page {
  min-height: 100vh;
  background: #f3f5f7;
  padding-bottom: 18vw;
}

.page-header {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 20;
  width: 100%;
  height: 12vw;
  background: #0097ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 4.6vw;
}

.page-body {
  padding: 15vw 3vw 0;
}

.panel-card,
.empty-card {
  background: #fff;
  border-radius: 4vw;
  box-shadow: 0 2vw 6vw rgba(15, 23, 42, 0.08);
}

.panel-card {
  padding: 4vw;
}

.empty-card {
  padding: 8vw 5vw;
  text-align: center;
}

.business-switcher {
  display: flex;
  flex-wrap: wrap;
  gap: 2vw;
  margin-bottom: 4vw;
}

.switch-item {
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  color: #475569;
  padding: 1.8vw 3vw;
  border-radius: 999px;
  font-size: 3.1vw;
}

.switch-item.active {
  border-color: #0097ff;
  background: #e0f2fe;
  color: #0097ff;
}

.logo-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3vw;
  margin-bottom: 5vw;
}

.logo-image {
  width: 28vw;
  height: 28vw;
  object-fit: cover;
  border-radius: 50%;
  background: #f8fafc;
}

.hidden-input {
  display: none;
}

.form-grid {
  display: grid;
  gap: 3vw;
}

.form-grid input,
.form-grid select,
.form-grid textarea {
  width: 100%;
  border: 1px solid #dbeafe;
  border-radius: 3vw;
  padding: 3vw;
  box-sizing: border-box;
  font-size: 3.4vw;
  background: #f8fbff;
}

.action-stack {
  display: grid;
  gap: 3vw;
  margin-top: 5vw;
}

.primary-btn,
.secondary-btn {
  min-height: 10vw;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-size: 3.4vw;
  font-weight: 600;
}

.primary-btn {
  background: #0097ff;
  color: #fff;
}

.secondary-btn {
  background: #e2e8f0;
  color: #334155;
}

.secondary-btn.ghost {
  background: #f8fafc;
}

.merchant-footer {
  position: fixed;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 15vw;
  background: #fff;
  display: flex;
  border-top: 1px solid #e2e8f0;
  z-index: 20;
}

.footer-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 3vw;
}

.footer-item i {
  font-size: 4.8vw;
  margin-bottom: 1vw;
}

.footer-item.active {
  color: #0097ff;
}
</style>
