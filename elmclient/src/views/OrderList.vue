<template>
  <div class="business-orders">
    <header>
      <p>店铺订单管理</p>
    </header>

    <div class="order-filter">
      <button :class="{ active: orderState === null }" @click="orderState = null">全部</button>
      <button :class="{ active: orderState === 1 }" @click="orderState = 1">待接单</button>
      <button :class="{ active: orderState === 2 }" @click="orderState = 2">配送中</button>
      <button :class="{ active: orderState === 3 }" @click="orderState = 3">已完成</button>
    </div>

    <div class="order-list">
      <div v-if="loading" class="no-orders">正在加载订单...</div>
      <div v-else-if="filteredOrders.length === 0" class="no-orders">暂无订单</div>
      <div v-for="order in filteredOrders" :key="order.id" class="order-item">
        <div class="order-header">
          <p>订单编号: {{ order.id }}</p>
          <p>下单时间: {{ formatDate(order.orderDate) }}</p>
          <p class="status" :class="getStatusClass(order.orderState)">
            {{ getStatusText(order.orderState) }}
          </p>
        </div>

        <div class="order-foods">
          <p v-for="detail in order.orderDetails" :key="detail.id || `${order.id}-${detail.foodId}`">
            {{ detail.foodName }} x {{ detail.quantity }}
          </p>
        </div>

        <div class="order-footer">
          <p>总金额: ¥{{ formatPrice(order.orderTotal) }}</p>
          <div class="order-actions">
            <button v-if="order.orderState === 1" @click="handleAccept(order.id)">接单</button>
            <button v-if="order.orderState === 2" @click="handleComplete(order.id)">完成</button>
          </div>
        </div>
      </div>
    </div>

    <div class="merchant-footer">
      <div class="footer-item" @click="$router.push('/business')">
        <i class="fa fa-home"></i>
        <span>首页</span>
      </div>
      <div class="footer-item active" @click="$router.push('/orderList')">
        <i class="fa fa-list-alt"></i>
        <span>订单</span>
      </div>
      <div class="footer-item" @click="$router.push('/businessManage')">
        <i class="fa fa-user"></i>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script>
import auth from '../utils/auth'

export default {
  name: 'OrderList',
  data() {
    return {
      orders: [],
      businessId: null,
      foodMap: {},
      orderState: null,
      loading: false,
      refreshTimer: null
    }
  },
  computed: {
    filteredOrders() {
      if (this.orderState === null) {
        return this.orders
      }
      return this.orders.filter(order => order.orderState === this.orderState)
    }
  },
  created() {
    const user = auth.getUserInfo()
    if (!user || auth.isBusiness()) {
      this.$router.push('/index')
      return
    }
    this.loadBusinessAndOrders()
    this.startRefreshTimer()
  },
  beforeUnmount() {
    this.clearRefreshTimer()
  },
  methods: {
    formatPrice(value) {
      return Number(value || 0).toFixed(2)
    },
    formatDate(value) {
      if (!value) return ''
      return String(value).replace('T', ' ').slice(0, 19)
    },
    buildHeaders() {
      const token = auth.getToken()
      return token ? { Authorization: `Bearer ${token}` } : {}
    },
    getStatusText(orderState) {
      if (orderState === 1) return '待接单'
      if (orderState === 2) return '配送中'
      if (orderState === 3) return '已完成'
      return '未支付'
    },
    getStatusClass(orderState) {
      if (orderState === 1) return 'pending'
      if (orderState === 2) return 'accepted'
      if (orderState === 3) return 'completed'
      return ''
    },
    async loadBusinessAndOrders() {
      const user = auth.getUserInfo()
      const userId = user.userId || user.id
      if (!userId) return

      this.loading = true
      try {
        const businessResponse = await this.$axios.get(`/api/businesses/user/${userId}`, {
          headers: this.buildHeaders()
        })
        const businesses = Array.isArray(businessResponse.data.data) ? businessResponse.data.data : []
        if (!businesses.length) {
          this.orders = []
          return
        }
        const business = businesses[0]
        this.businessId = business.id

        const [foodResponse, orderResponse] = await Promise.all([
          this.$axios.get(`/api/foods/business/${this.businessId}`, { headers: this.buildHeaders() }),
          this.$axios.get(`/api/orders/business/${this.businessId}`, { headers: this.buildHeaders() })
        ])

        const foods = Array.isArray(foodResponse.data.data) ? foodResponse.data.data : []
        this.foodMap = foods.reduce((map, food) => {
          map[food.id] = food
          return map
        }, {})

        const orders = Array.isArray(orderResponse.data.data) ? orderResponse.data.data : []
        this.orders = orders.map(order => this.decorateOrder(order))
      } catch (error) {
        console.error('加载订单失败:', error)
        this.orders = []
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
    async updateOrderState(orderId, targetState, successMsg) {
      try {
        const response = await this.$axios.put(`/api/orders/${orderId}/state`, null, {
          params: { orderState: targetState },
          headers: this.buildHeaders()
        })
        if (!response.data.success) {
          throw new Error(response.data.message || 'update failed')
        }
        const updatedOrder = this.decorateOrder(response.data.data)
        this.orders = this.orders.map(item => (item.id === orderId ? updatedOrder : item))
        alert(successMsg)
      } catch (error) {
        console.error('更新订单状态失败:', error)
        alert('操作失败，请重试')
      }
    },
    handleAccept(orderId) {
      this.updateOrderState(orderId, 2, '接单成功')
    },
    handleComplete(orderId) {
      this.updateOrderState(orderId, 3, '订单已完成')
    },
    startRefreshTimer() {
      this.refreshTimer = setInterval(() => {
        if (this.businessId) {
          this.loadBusinessAndOrders()
        }
      }, 30000)
    },
    clearRefreshTimer() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    }
  },
  watch: {
    orderState() {
      if (this.businessId) {
        this.loadBusinessAndOrders()
      }
    }
  }
}
</script>

<style scoped>
.business-orders {
  width: 100%;
  min-height: 100vh;
  padding-bottom: 15vw;
  box-sizing: border-box;
  background-color: #f5f5f5;
}

/* 头部样式 */
header {
  width: 100%;
  height: 12vw;
  background-color: #0097FF;
  color: #fff;
  font-size: 4.8vw;
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  margin: 0;
}

/* 筛选栏样式（修复定位遮挡问题） */
.order-filter {
  display: flex;
  padding: 2vw;
  border-bottom: 1px solid #eee;
  background-color: white;
  position: fixed;
  top: 12vw; /* 接在header下方 */
  left: 0;
  width: 100%;
  box-sizing: border-box;
  z-index: 99;
}

.order-filter button {
  flex: 1;
  padding: 2vw 0;
  border: none;
  background: none;
  font-size: 3.2vw;
  color: #666;
}

.order-filter button.active {
  color: #0097FF;
  font-weight: bold;
  border-bottom: 2px solid #0097FF;
}

/* 订单列表样式（修复顶部遮挡） */
.order-list {
  padding-top: 24vw; /* header(12vw) + filter(12vw) = 24vw */
  padding-bottom: 5vw;
}

/* 单个订单项 */
.order-item {
  background-color: white;
  margin: 2vw;
  border-radius: 3vw;
  padding: 3vw;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  margin-bottom: 2vw;
  font-size: 3vw;
  color: #333;
}

.order-header p {
  margin: 0.5vw 0;
}

/* 订单状态样式 */
.status {
  font-weight: bold;
}

.status.pending {
  color: #ff9900;
}

.status.accepted {
  color: #0097FF;
}

.status.completed {
  color: #00cc66;
}

.status.cancelled {
  color: #999;
}

/* 订单商品列表 */
.order-foods {
  border-top: 1px dashed #eee;
  border-bottom: 1px dashed #eee;
  padding: 2vw 0;
  margin-bottom: 2vw;
}

.order-foods p {
  font-size: 3.2vw;
  color: #666;
  margin: 1vw 0;
}

/* 订单底部（金额+操作按钮） */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 3.2vw;
  color: #333;
}

.order-actions button {
  margin-left: 2vw;
  padding: 1.5vw 3vw;
  border: none;
  border-radius: 2vw;
  font-size: 3vw;
  cursor: pointer;
  transition: background-color 0.2s;
}

.order-actions button:first-child {
  background-color: #0097FF;
  color: white;
}

.order-actions button:last-child {
  background-color: #ff3333;
  color: white;
}

/* 无订单状态 */
.no-orders {
  text-align: center;
  padding: 20vw 0;
  color: #999;
  font-size: 4vw;
}

/* 底部导航 */
.merchant-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 15vw;
  background-color: white;
  display: flex;
  border-top: 1px solid #eee;
  z-index: 98;
}

.footer-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 3vw;
  color: #666;
  cursor: pointer;
}

.footer-item i {
  font-size: 5vw;
  margin-bottom: 1vw;
}

.footer-item.active {
  color: #0097FF;
}
</style>