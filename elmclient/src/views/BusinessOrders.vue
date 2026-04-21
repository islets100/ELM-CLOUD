<template>
  <div class="orders-page">
    <header class="page-header">
      <p>商家订单</p>
    </header>

    <main class="page-body">
      <section v-if="!businesses.length" class="empty-card">
        <p>还没有店铺，暂时没有可管理的订单。</p>
        <button class="primary-btn" @click="$router.push('/business')">去创建店铺</button>
      </section>

      <template v-else>
        <section class="filter-card">
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

          <div class="status-switcher">
            <button
              v-for="item in statusOptions"
              :key="item.value === null ? 'all' : item.value"
              class="switch-item"
              :class="{ active: selectedStatus === item.value }"
              @click="selectedStatus = item.value"
            >
              {{ item.label }}
            </button>
          </div>
        </section>

        <section v-if="loading" class="empty-card">
          <p>正在加载订单...</p>
        </section>

        <section v-else-if="!filteredOrders.length" class="empty-card">
          <p>当前筛选条件下没有订单。</p>
        </section>

        <section v-else class="order-list">
          <article v-for="order in filteredOrders" :key="order.id" class="order-card">
            <div class="order-head">
              <div>
                <h3>订单 #{{ order.id }}</h3>
                <p>{{ formatDate(order.orderDate) }}</p>
              </div>
              <span class="status-badge" :class="statusClass(order.orderState)">
                {{ statusText(order.orderState) }}
              </span>
            </div>

            <div class="order-foods">
              <div v-for="detail in order.orderDetails" :key="detail.odId || `${order.id}-${detail.foodId}`" class="food-row">
                <span>{{ detail.foodName }}</span>
                <span>x{{ detail.quantity }}</span>
              </div>
            </div>

            <div class="order-foot">
              <strong>合计 ￥{{ formatPrice(order.orderTotal) }}</strong>
              <div class="action-group">
                <button
                  v-if="order.orderState === 1"
                  class="primary-btn small"
                  @click="updateOrderState(order, 2)"
                >
                  接单
                </button>
                <button
                  v-if="order.orderState === 2"
                  class="danger-btn small"
                  @click="updateOrderState(order, 3)"
                >
                  完成
                </button>
              </div>
            </div>
          </article>
        </section>
      </template>
    </main>

    <div class="merchant-footer">
      <div class="footer-item" @click="$router.push('/business')">
        <i class="fa fa-home"></i>
        <span>首页</span>
      </div>
      <div class="footer-item active" @click="$router.push('/businessOrders')">
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
import auth from '../utils/auth'

const STATUS_OPTIONS = [
  { value: null, label: '全部' },
  { value: 1, label: '待接单' },
  { value: 2, label: '配送中' },
  { value: 3, label: '已完成' }
]

export default {
  name: 'BusinessOrders',
  data() {
    return {
      businesses: [],
      currentBusinessId: null,
      orders: [],
      foodMap: {},
      selectedStatus: null,
      statusOptions: STATUS_OPTIONS,
      loading: false
    }
  },
  computed: {
    filteredOrders() {
      if (this.selectedStatus === null) {
        return this.orders
      }
      return this.orders.filter(order => order.orderState === this.selectedStatus)
    }
  },
  watch: {
    '$route.query.businessId'(value) {
      const targetId = Number(value || 0)
      if (!targetId || targetId === this.currentBusinessId) {
        return
      }
      const target = this.businesses.find(item => item.id === targetId)
      if (target) {
        this.selectBusiness(target)
      }
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
      return Number(value || 0).toFixed(2)
    },
    formatDate(value) {
      if (!value) {
        return ''
      }
      return String(value).replace('T', ' ').slice(0, 19)
    },
    statusText(state) {
      if (state === 1) return '待接单'
      if (state === 2) return '配送中'
      if (state === 3) return '已完成'
      return '未支付'
    },
    statusClass(state) {
      if (state === 1) return 'pending'
      if (state === 2) return 'delivering'
      if (state === 3) return 'done'
      return 'unpaid'
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
          this.orders = []
          return
        }

        const routeBusinessId = Number(this.$route.query.businessId || 0)
        const targetId = preferredId || routeBusinessId || this.currentBusinessId
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
      this.currentBusinessId = business.id
      this.loading = true

      try {
        const [foodResponse, orderResponse] = await Promise.all([
          this.$axios.get(`/api/foods/business/${business.id}`, { headers: this.buildHeaders() }),
          this.$axios.get(`/api/orders/business/${business.id}`, { headers: this.buildHeaders() })
        ])

        const foods = Array.isArray(foodResponse.data.data) ? foodResponse.data.data : []
        this.foodMap = foods.reduce((accumulator, food) => {
          accumulator[food.id] = food
          return accumulator
        }, {})

        const orders = Array.isArray(orderResponse.data.data) ? orderResponse.data.data : []
        this.orders = orders.map(order => this.decorateOrder(order))
      } catch (error) {
        console.error('load business orders failed', error)
        this.orders = []
        this.foodMap = {}
        alert('加载订单失败')
      } finally {
        this.loading = false
      }
    },
    decorateOrder(order) {
      const details = Array.isArray(order.orderDetails) ? order.orderDetails : []
      return {
        ...order,
        orderDetails: details.map(detail => {
          const food = this.foodMap[detail.foodId] || {}
          return {
            ...detail,
            foodName: food.foodName || `菜品 #${detail.foodId}`
          }
        })
      }
    },
    async updateOrderState(order, targetState) {
      const actionText = targetState === 2 ? '接单' : '完成订单'
      if (!confirm(`确认${actionText}吗？`)) {
        return
      }

      try {
        const response = await this.$axios.put(`/api/orders/${order.id}/state`, null, {
          params: { orderState: targetState },
          headers: this.buildHeaders()
        })
        if (!response.data.success) {
          throw new Error(response.data.message || 'update order state failed')
        }

        const updatedOrder = this.decorateOrder(response.data.data)
        this.orders = this.orders.map(item => (item.id === order.id ? updatedOrder : item))
      } catch (error) {
        console.error('update order state failed', error)
        alert('更新订单状态失败')
      }
    }
  }
}
</script>

<style scoped>
.orders-page {
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

.filter-card,
.order-card,
.empty-card {
  background: #fff;
  border-radius: 4vw;
  box-shadow: 0 2vw 6vw rgba(15, 23, 42, 0.08);
}

.filter-card {
  padding: 4vw;
  margin-bottom: 3vw;
}

.business-switcher,
.status-switcher {
  display: flex;
  flex-wrap: wrap;
  gap: 2vw;
}

.status-switcher {
  margin-top: 3vw;
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

.empty-card {
  padding: 8vw 5vw;
  text-align: center;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 3vw;
}

.order-card {
  padding: 4vw;
}

.order-head,
.order-foot,
.food-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-head h3 {
  margin: 0;
  font-size: 3.8vw;
}

.order-head p {
  margin: 1vw 0 0;
  color: #64748b;
  font-size: 3vw;
}

.status-badge {
  padding: 1.6vw 3vw;
  border-radius: 999px;
  font-size: 3vw;
  font-weight: 600;
}

.status-badge.unpaid {
  background: #e2e8f0;
  color: #64748b;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.delivering {
  background: #dbeafe;
  color: #2563eb;
}

.status-badge.done {
  background: #dcfce7;
  color: #16a34a;
}

.order-foods {
  margin: 4vw 0;
  padding: 3vw 0;
  border-top: 1px dashed #e2e8f0;
  border-bottom: 1px dashed #e2e8f0;
}

.food-row + .food-row {
  margin-top: 2vw;
}

.food-row {
  color: #475569;
  font-size: 3.2vw;
}

.action-group {
  display: flex;
  gap: 2vw;
}

.primary-btn,
.danger-btn {
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-size: 3.3vw;
  font-weight: 600;
  min-height: 10vw;
  padding: 0 5vw;
}

.primary-btn {
  background: #0097ff;
  color: #fff;
}

.danger-btn {
  background: #fee2e2;
  color: #dc2626;
}

.primary-btn.small,
.danger-btn.small {
  min-height: 8vw;
  padding: 0 3vw;
  font-size: 3vw;
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
