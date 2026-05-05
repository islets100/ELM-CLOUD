<template>
  <div class="business-page">
    <header class="page-header">
      <p>{{ currentBusiness ? currentBusiness.businessName : '商家中心' }}</p>
    </header>

    <main class="page-body">
      <section v-if="!businesses.length" class="empty-card">
        <img :src="defaultShopLogo" alt="shop" class="empty-logo">
        <h3>还没有店铺</h3>
        <p>先创建店铺，再管理菜品。</p>

        <div class="form-grid compact">
          <input v-model.trim="businessForm.businessName" type="text" placeholder="店铺名称">
          <input v-model.trim="businessForm.businessAddress" type="text" placeholder="店铺地址">
          <textarea v-model.trim="businessForm.businessExplain" rows="3" placeholder="店铺简介"></textarea>
          <select v-model.number="businessForm.orderTypeId">
            <option :value="null" disabled>选择分类</option>
            <option v-for="item in orderTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
          <input v-model.number="businessForm.startPrice" type="number" min="0" step="0.01" placeholder="起送价">
          <input v-model.number="businessForm.deliveryPrice" type="number" min="0" step="0.01" placeholder="配送费">
          <textarea v-model.trim="businessForm.remarks" rows="2" placeholder="备注（可选）"></textarea>
        </div>

        <button class="primary-btn" :disabled="submittingBusiness" @click="createBusiness">
          {{ submittingBusiness ? '创建中...' : '创建店铺' }}
        </button>
      </section>

      <section v-else class="content-section">
        <div class="toolbar-card">
          <div class="toolbar-top">
            <div>
              <h3>{{ currentBusiness.businessName }}</h3>
              <p>{{ currentBusiness.businessAddress || '未填写地址' }}</p>
            </div>
            <button class="secondary-btn" @click="$router.push('/businessManage')">店铺设置</button>
          </div>

          <div v-if="businesses.length > 1" class="business-switcher">
            <button
              v-for="business in businesses"
              :key="business.id"
              class="switch-item"
              :class="{ active: business.id === currentBusiness.id }"
              @click="selectBusiness(business)"
            >
              {{ business.businessName }}
            </button>
          </div>
        </div>

        <div class="section-head">
          <h3>菜品管理</h3>
          <button class="primary-btn small" @click="openCreateFoodDialog">新增菜品</button>
        </div>

        <div v-if="loadingFoods" class="empty-card small-card">
          <p>正在加载菜品...</p>
        </div>

        <div v-else-if="!foodList.length" class="empty-card small-card">
          <p>当前店铺还没有菜品。</p>
          <button class="primary-btn small" @click="openCreateFoodDialog">立即新增</button>
        </div>

        <div v-else class="food-list">
          <article v-for="food in foodList" :key="food.id" class="food-card">
            <img :src="food.foodImg || defaultFoodImage" :alt="food.foodName" class="food-image">
            <div class="food-main">
              <div class="food-title">
                <h4>{{ food.foodName }}</h4>
                <span>￥{{ formatPrice(food.foodPrice) }}</span>
              </div>
              <p>{{ food.foodExplain || '暂无描述' }}</p>
            </div>
            <div class="food-actions">
              <button class="secondary-btn small" @click="openEditFoodDialog(food)">编辑</button>
              <button class="danger-btn small" @click="deleteFood(food)">删除</button>
            </div>
          </article>
        </div>
      </section>
    </main>

    <div v-if="foodDialogVisible" class="dialog-mask" @click.self="closeFoodDialog">
      <div class="dialog-card">
        <div class="dialog-head">
          <h3>{{ foodDialogMode === 'create' ? '新增菜品' : '编辑菜品' }}</h3>
          <button class="icon-btn" @click="closeFoodDialog">&times;</button>
        </div>

        <div class="dialog-body">
          <div class="image-picker">
            <img :src="foodForm.foodImg || defaultFoodImage" alt="food" class="preview-image">
            <input
              ref="foodImageInput"
              type="file"
              accept="image/*"
              class="hidden-input"
              @change="handleFoodImageChange"
            >
            <button class="secondary-btn small" @click="$refs.foodImageInput.click()">上传图片</button>
          </div>

          <div class="form-grid">
            <input v-model.trim="foodForm.foodName" type="text" placeholder="菜品名称">
            <input v-model.number="foodForm.foodPrice" type="number" min="0.01" step="0.01" placeholder="价格">
            <textarea v-model.trim="foodForm.foodExplain" rows="3" placeholder="菜品描述"></textarea>
            <textarea v-model.trim="foodForm.remarks" rows="2" placeholder="备注（可选）"></textarea>
          </div>
        </div>

        <div class="dialog-actions">
          <button class="secondary-btn" @click="closeFoodDialog">取消</button>
          <button class="primary-btn" :disabled="submittingFood" @click="submitFood">
            {{ submittingFood ? '提交中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <div class="merchant-footer">
      <div class="footer-item active" @click="$router.push('/business')">
        <i class="fa fa-home"></i>
        <span>首页</span>
      </div>
      <div class="footer-item" @click="goToOrders">
        <i class="fa fa-list-alt"></i>
        <span>订单</span>
      </div>
      <div class="footer-item" @click="$router.push('/wallet')">
        <i class="fa fa-credit-card"></i>
        <span>钱包</span>
      </div>
      <div class="footer-item" @click="$router.push('/businessManage')">
        <i class="fa fa-user"></i>
        <span>管理</span>
      </div>
    </div>
  </div>
</template>

<script>
import defaultFoodImage from '../assets/sp01.png'
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

function createBusinessForm() {
  return {
    businessName: '',
    businessAddress: '',
    businessExplain: '',
    orderTypeId: null,
    startPrice: 20,
    deliveryPrice: 5,
    remarks: ''
  }
}

function createFoodForm() {
  return {
    id: null,
    foodName: '',
    foodPrice: '',
    foodExplain: '',
    foodImg: '',
    remarks: ''
  }
}

export default {
  name: 'Business',
  data() {
    return {
      defaultFoodImage,
      defaultShopLogo,
      orderTypeOptions: ORDER_TYPE_OPTIONS,
      businesses: [],
      currentBusiness: null,
      foodList: [],
      loadingFoods: false,
      submittingBusiness: false,
      submittingFood: false,
      businessForm: createBusinessForm(),
      foodDialogVisible: false,
      foodDialogMode: 'create',
      foodForm: createFoodForm()
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
    formatPrice(value) {
      const number = Number(value || 0)
      return number.toFixed(2)
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
          this.currentBusiness = null
          this.foodList = []
          return
        }

        const routeBusinessId = Number(this.$route.query.businessId || 0)
        const targetId = preferredId || routeBusinessId || (this.currentBusiness && this.currentBusiness.id)
        const target = list.find(item => item.id === targetId) || list[0]
        await this.selectBusiness(target)
      } catch (error) {
        console.error('load businesses failed', error)
        alert('加载店铺失败')
      }
    },
    async selectBusiness(business) {
      if (!business) {
        return
      }
      this.currentBusiness = business
      await this.loadFoods()
    },
    async loadFoods() {
      if (!this.currentBusiness) {
        this.foodList = []
        return
      }

      this.loadingFoods = true
      try {
        const response = await this.$axios.get(`/api/foods/business/${this.currentBusiness.id}`, {
          headers: this.buildHeaders()
        })
        this.foodList = Array.isArray(response.data.data) ? response.data.data : []
      } catch (error) {
        console.error('load foods failed', error)
        this.foodList = []
        alert('加载菜品失败')
      } finally {
        this.loadingFoods = false
      }
    },
    async createBusiness() {
      if (!this.businessForm.businessName) {
        alert('请输入店铺名称')
        return
      }
      if (!this.businessForm.businessAddress) {
        alert('请输入店铺地址')
        return
      }
      if (!this.businessForm.orderTypeId) {
        alert('请选择店铺分类')
        return
      }

      const userId = this.getCurrentUserId()
      if (!userId) {
        alert('当前登录信息无效')
        return
      }

      this.submittingBusiness = true
      try {
        const payload = {
          businessName: this.businessForm.businessName,
          businessAddress: this.businessForm.businessAddress,
          businessExplain: this.businessForm.businessExplain,
          orderTypeId: this.businessForm.orderTypeId,
          startPrice: Number(this.businessForm.startPrice || 0),
          deliveryPrice: Number(this.businessForm.deliveryPrice || 0),
          remarks: this.businessForm.remarks,
          userId
        }
        const response = await this.$axios.post('/api/businesses', payload, {
          headers: this.buildHeaders()
        })

        if (!response.data.success) {
          throw new Error(response.data.message || 'create business failed')
        }

        this.businessForm = createBusinessForm()
        await this.loadBusinesses(response.data.data && response.data.data.id)
      } catch (error) {
        console.error('create business failed', error)
        alert('创建店铺失败')
      } finally {
        this.submittingBusiness = false
      }
    },
    openCreateFoodDialog() {
      if (!this.currentBusiness) {
        return
      }
      this.foodDialogMode = 'create'
      this.foodForm = createFoodForm()
      this.foodDialogVisible = true
    },
    openEditFoodDialog(food) {
      this.foodDialogMode = 'edit'
      this.foodForm = {
        id: food.id,
        foodName: food.foodName || '',
        foodPrice: Number(food.foodPrice || 0),
        foodExplain: food.foodExplain || '',
        foodImg: food.foodImg || '',
        remarks: food.remarks || ''
      }
      this.foodDialogVisible = true
    },
    closeFoodDialog() {
      this.foodDialogVisible = false
      this.foodForm = createFoodForm()
      if (this.$refs.foodImageInput) {
        this.$refs.foodImageInput.value = ''
      }
    },
    async handleFoodImageChange(event) {
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

      this.foodForm.foodImg = await this.readImageAsDataUrl(file)
    },
    readImageAsDataUrl(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = event => resolve(event.target.result)
        reader.onerror = reject
        reader.readAsDataURL(file)
      })
    },
    async submitFood() {
      if (!this.currentBusiness) {
        return
      }
      if (!this.foodForm.foodName) {
        alert('请输入菜品名称')
        return
      }
      if (!(Number(this.foodForm.foodPrice) > 0)) {
        alert('请输入正确的菜品价格')
        return
      }

      this.submittingFood = true
      try {
        const payload = {
          foodName: this.foodForm.foodName,
          foodPrice: Number(this.foodForm.foodPrice),
          foodExplain: this.foodForm.foodExplain,
          foodImg: this.foodForm.foodImg,
          remarks: this.foodForm.remarks,
          businessId: this.currentBusiness.id
        }

        const request = this.foodDialogMode === 'create'
          ? this.$axios.post('/api/foods', payload, { headers: this.buildHeaders() })
          : this.$axios.put(`/api/foods/${this.foodForm.id}`, payload, { headers: this.buildHeaders() })

        const response = await request
        if (!response.data.success) {
          throw new Error(response.data.message || 'save food failed')
        }

        this.closeFoodDialog()
        await this.loadFoods()
      } catch (error) {
        console.error('save food failed', error)
        alert('保存菜品失败')
      } finally {
        this.submittingFood = false
      }
    },
    async deleteFood(food) {
      if (!confirm(`确认删除 ${food.foodName} 吗？`)) {
        return
      }

      try {
        const response = await this.$axios.delete(`/api/foods/${food.id}`, {
          headers: this.buildHeaders()
        })
        if (!response.data.success) {
          throw new Error(response.data.message || 'delete food failed')
        }
        await this.loadFoods()
      } catch (error) {
        console.error('delete food failed', error)
        alert('删除菜品失败')
      }
    },
    goToOrders() {
      if (!this.currentBusiness) {
        alert('请先创建店铺')
        return
      }
      this.$router.push({
        path: '/businessOrders',
        query: { businessId: this.currentBusiness.id }
      })
    }
  }
}
</script>

<style scoped>
.business-page {
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

.content-section,
.empty-card,
.toolbar-card,
.food-card,
.dialog-card {
  background: #fff;
  border-radius: 4vw;
}

.empty-card {
  padding: 8vw 5vw;
  text-align: center;
  box-shadow: 0 2vw 6vw rgba(15, 23, 42, 0.08);
}

.empty-card.small-card {
  padding: 6vw 4vw;
}

.empty-logo {
  width: 24vw;
  height: 24vw;
  object-fit: cover;
  border-radius: 50%;
  margin-bottom: 4vw;
}

.toolbar-card {
  padding: 4vw;
  box-shadow: 0 2vw 6vw rgba(15, 23, 42, 0.08);
}

.toolbar-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 3vw;
}

.toolbar-top h3,
.section-head h3,
.food-title h4 {
  margin: 0;
}

.toolbar-top p {
  margin: 1vw 0 0;
  color: #64748b;
  font-size: 3.1vw;
}

.business-switcher {
  display: flex;
  flex-wrap: wrap;
  gap: 2vw;
  margin-top: 4vw;
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

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 5vw 0 3vw;
}

.food-list {
  display: flex;
  flex-direction: column;
  gap: 3vw;
}

.food-card {
  display: flex;
  gap: 3vw;
  padding: 3vw;
  box-shadow: 0 2vw 6vw rgba(15, 23, 42, 0.08);
}

.food-image,
.preview-image {
  width: 22vw;
  height: 22vw;
  object-fit: cover;
  border-radius: 3vw;
  background: #f8fafc;
}

.food-main {
  flex: 1;
}

.food-title {
  display: flex;
  justify-content: space-between;
  gap: 2vw;
  margin-bottom: 2vw;
  font-size: 3.5vw;
}

.food-title span {
  color: #ef4444;
  font-weight: 700;
}

.food-main p {
  margin: 0;
  color: #64748b;
  font-size: 3vw;
  line-height: 1.6;
}

.food-actions {
  display: flex;
  flex-direction: column;
  gap: 2vw;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4vw;
  z-index: 30;
}

.dialog-card {
  width: 100%;
  max-width: 88vw;
  overflow: hidden;
}

.dialog-head,
.dialog-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4vw;
}

.dialog-body {
  padding: 0 4vw 4vw;
}

.dialog-head {
  border-bottom: 1px solid #e2e8f0;
}

.dialog-actions {
  gap: 3vw;
  border-top: 1px solid #e2e8f0;
}

.image-picker {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3vw;
  margin-bottom: 4vw;
}

.hidden-input {
  display: none;
}

.form-grid {
  display: grid;
  gap: 3vw;
}

.form-grid.compact {
  margin: 5vw 0;
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

.primary-btn,
.secondary-btn,
.danger-btn,
.icon-btn {
  border: none;
  border-radius: 999px;
  cursor: pointer;
}

.primary-btn,
.secondary-btn,
.danger-btn {
  min-height: 10vw;
  padding: 0 5vw;
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

.danger-btn {
  background: #fee2e2;
  color: #dc2626;
}

.primary-btn.small,
.secondary-btn.small,
.danger-btn.small {
  min-height: 8vw;
  padding: 0 3vw;
  font-size: 3vw;
}

.icon-btn {
  width: 8vw;
  height: 8vw;
  background: transparent;
  font-size: 6vw;
  color: #64748b;
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
