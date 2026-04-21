<template>
  <div class="business-orders">
    <header>
      <p>店铺订单管理</p>
    </header>
    
    <!-- 店铺切换 + 订单状态筛选 -->
    <div class="order-filter">
      <!-- 店铺选择区域 -->
      <div class="filter-section">
        <div class="section-title">店铺</div>
        <div class="business-switcher" v-if="businesses && businesses.length > 0">
          <button
            v-for="biz in businesses"
            :key="biz.id"
            class="biz-tag"
            :class="{ active: biz.id === businessId }"
            @click="switchBusiness(biz)"
          >
            {{ biz.businessName }}
          </button>
        </div>
      </div>

      <!-- 订单状态区域 -->
      <div class="filter-section">
        <div class="section-title">订单状态</div>
        <div class="status-filter">
          <button :class="{ active: selectedStatus === null }" @click="selectedStatus = null">全部</button>
          <button :class="{ active: selectedStatus === 1 }" @click="selectedStatus = 1">未处理</button>
          <button :class="{ active: selectedStatus === 2 }" @click="selectedStatus = 2">已接单</button>
          <button :class="{ active: selectedStatus === 3 }" @click="selectedStatus = 3">已完成</button>
        </div>
      </div>
    </div>
    
    <!-- 订单列表 -->
    <div class="order-list">
      <div v-for="order in filteredOrders" :key="order.id" class="order-item">
        <div class="order-header">
          <div class="order-info">
            <p class="order-id">订单编号: {{ order.id }}</p>
            <p class="order-time">下单时间: {{ formatDateTime(order.orderDate) }}</p>
          </div>
          <p class="status" :class="getStatusClass(order.orderState)">
            {{ getStatusText(order.orderState) }}
          </p>
        </div>
        
        <div class="order-foods">
          <p v-for="detail in order.orderDetails" :key="detail.id" class="food-item">
            {{ detail.food.foodName }} x {{ detail.quantity }}
          </p>
        </div>
        
        <div class="order-footer">
          <p class="order-total">总金额: ¥{{ order.orderTotal }}</p>
          <div class="order-actions">
            <button 
              v-if="order.orderState === 1" 
              class="btn-accept" 
              @click="handleAccept(order.id)">
              接单
            </button>
            <button 
              v-if="order.orderState === 2" 
              class="btn-complete" 
              @click="handleComplete(order.id)">
              完成
            </button>
          </div>
        </div>
      </div>
      
      <div v-if="filteredOrders.length === 0" class="no-orders">
        暂无订单
      </div>
    </div>
    
    <!-- 底部导航 -->
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
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script>
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

export default {
  name: 'BusinessOrders',
  data() {
    return {
      orders: [], // 当前选中店铺的订单列表
      selectedStatus: null, // 筛选状态（null=全部，1=未处理，2=已接单、3=已完成）
      businessId: null, // 当前选中店铺ID
      businesses: [], // 当前商家名下的全部店铺
      stompClient: null, // WebSocket 客户端
      wsConnected: false // WebSocket 连接状态
    };
  },
  computed: {
    // 根据选择的状态筛选订单
    filteredOrders() {
      if (this.selectedStatus === null) {
        // 只显示未处理、已接单、已完成的订单（排除未支付和已取消）
        return this.orders.filter(order => 
          order.orderState === 1 || order.orderState === 2 || order.orderState === 3
        );
      } else {
        return this.orders.filter(order => order.orderState === this.selectedStatus);
      }
    }
  },
  created() {
  // 1. 加载当前商家名下的全部店铺，并根据路由参数/默认规则选中店铺
  this.loadMyBusinesses();
  },
  beforeUnmount() {
    // 页面销毁时断开 WebSocket 连接
    this.disconnectWebSocket();
  },
  watch: {
    // 筛选状态变化时，不需要重新请求，因为已经加载了所有订单
    selectedStatus() {
      // 筛选逻辑在 computed 中处理
    },
    // 监听路由中的 businessId 变化（例如从商品页跳转时带过来）
    '$route.query.businessId': {
      handler(newVal) {
        if (!newVal || !this.businesses || this.businesses.length === 0) return;
        const targetId = Number(newVal);
        const target = this.businesses.find(b => b.id === targetId);
        if (target && target.id !== this.businessId) {
          this.switchBusiness(target);
        }
      },
      immediate: false
    }
  },
  methods: {
  // 加载当前商家名下的全部店铺，并根据路由参数/默认规则选中店铺
  async loadMyBusinesses() {
    try {
      const response = await this.$axios.get('/api/businesses/my-businesses', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.data.success && Array.isArray(response.data.data) && response.data.data.length > 0) {
        this.businesses = response.data.data;
        // 优先根据路由参数 businessId 选中店铺，其次默认第一家
        const routeBizId = this.$route.query.businessId ? Number(this.$route.query.businessId) : null;
        let target = null;
        if (routeBizId) {
          target = this.businesses.find(b => b.id === routeBizId) || null;
        }
        if (!target) {
          target = this.businesses[0];
        }
        this.switchBusiness(target);
      } else {
        alert('您还没有创建店铺，请先创建店铺');
        this.$router.push('/businessManage');
      }
    } catch (error) {
      console.error('获取店铺列表失败:', error);
      alert('获取商家信息失败: ' + (error.response?.data?.message || error.message));
    }
  },

  // 切换当前查看订单的店铺
  async switchBusiness(biz) {
    if (!biz || !biz.id || biz.id === this.businessId) {
      return;
    }
    this.businessId = biz.id;
    // 每次切换店铺时，重新加载该店铺订单，并重新建立 WebSocket 订阅
    this.orders = [];
    await this.getOrders();
    this.disconnectWebSocket();
    this.connectWebSocket();
  },

    // 获取订单列表
    async getOrders() {
      if (!this.businessId) {
        return;
      }
      
      try {
        const response = await this.$axios.get('/api/orders/business', {
          params: {
            businessId: this.businessId
          },
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        if (response.data.success) {
          this.orders = response.data.data || [];
          // 按订单时间倒序排列（最新的在前）
          this.orders.sort((a, b) => {
            const dateA = new Date(a.orderDate);
            const dateB = new Date(b.orderDate);
            return dateB - dateA;
          });
        } else {
          console.error('获取订单失败:', response.data.message);
          this.orders = [];
        }
      } catch (error) {
        console.error('获取订单失败:', error);
        this.orders = [];
      }
    },

    // 连接 WebSocket
    connectWebSocket() {
      if (!this.businessId || this.wsConnected) {
        return;
      }

      try {
        const socket = new SockJS('http://localhost:8080/ws');
        this.stompClient = Stomp.over(socket);
        
        // 禁用调试日志
        this.stompClient.debug = () => {};

        this.stompClient.connect({}, () => {
          this.wsConnected = true;
          console.log('WebSocket 连接成功');

          // 订阅新订单通知
          this.stompClient.subscribe(`/topic/business/${this.businessId}/orders`, (message) => {
            try {
              const newOrder = JSON.parse(message.body);
              console.log('收到新订单:', newOrder);
              // 将新订单添加到列表顶部
              this.orders.unshift(newOrder);
              // 显示通知
              this.$message?.success?.('收到新订单！') || alert('收到新订单！');
            } catch (error) {
              console.error('解析新订单消息失败:', error);
            }
          });

          // 订阅订单状态更新通知
          this.stompClient.subscribe(`/topic/business/${this.businessId}/orders/status`, (message) => {
            try {
              const update = JSON.parse(message.body);
              console.log('收到订单状态更新:', update);
              // 更新订单状态
              const orderIndex = this.orders.findIndex(o => o.id === update.orderId);
              if (orderIndex !== -1) {
                this.orders[orderIndex].orderState = update.status;
                // 显示通知
                this.$message?.success?.('订单状态已更新') || alert('订单状态已更新');
              }
            } catch (error) {
              console.error('解析状态更新消息失败:', error);
            }
          });
        }, (error) => {
          console.error('WebSocket 连接失败:', error);
          this.wsConnected = false;
          // 连接失败时，使用定时刷新作为备选方案
          this.startFallbackRefresh();
        });
      } catch (error) {
        console.error('WebSocket 初始化失败:', error);
        // 连接失败时，使用定时刷新作为备选方案
        this.startFallbackRefresh();
      }
    },

    // 断开 WebSocket 连接
    disconnectWebSocket() {
      if (this.stompClient && this.wsConnected) {
        this.stompClient.disconnect();
        this.wsConnected = false;
        console.log('WebSocket 连接已断开');
      }
    },

    // 备选方案：定时刷新（当 WebSocket 连接失败时使用）
    startFallbackRefresh() {
      // 每30秒刷新一次订单列表
      setInterval(() => {
        if (this.businessId) {
          this.getOrders();
        }
      }, 30000);
    },

    // 接单（状态从1变为2）
    async handleAccept(orderId) {
      if (!confirm('确定要接单吗？')) {
        return;
      }

      try {
        const response = await this.$axios.post(
          `/api/orders/${orderId}/status`,
          null,
          {
            params: { status: 2 },
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          }
        );

        if (response.data.success) {
          alert('接单成功！');
          // 更新本地订单状态（WebSocket 也会推送更新，但这里立即更新以提升体验）
          const orderIndex = this.orders.findIndex(o => o.id === orderId);
          if (orderIndex !== -1) {
            this.orders[orderIndex].orderState = 2;
          }
        } else {
          alert('接单失败: ' + (response.data.message || '未知错误'));
        }
      } catch (error) {
        console.error('接单失败:', error);
        alert('接单失败: ' + (error.response?.data?.message || error.message));
      }
    },

    // 完成订单（状态从2变为3）
    async handleComplete(orderId) {
      if (!confirm('确定订单已完成吗？')) {
        return;
      }

      try {
        const response = await this.$axios.post(
          `/api/orders/${orderId}/status`,
          null,
          {
            params: { status: 3 },
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          }
        );

        if (response.data.success) {
          alert('订单已完成！');
          // 更新本地订单状态
          const orderIndex = this.orders.findIndex(o => o.id === orderId);
          if (orderIndex !== -1) {
            this.orders[orderIndex].orderState = 3;
          }
        } else {
          alert('完成订单失败: ' + (response.data.message || '未知错误'));
        }
      } catch (error) {
        console.error('完成订单失败:', error);
        alert('完成订单失败: ' + (error.response?.data?.message || error.message));
      }
    },

    // 获取订单状态文本
    getStatusText(status) {
      switch(status) {
        case 0: return '未支付';
        case 1: return '未处理';
        case 2: return '已接单';
        case 3: return '已完成';
        default: return '未知状态';
      }
    },

    // 获取订单状态样式类
    getStatusClass(status) {
      switch(status) {
        case 0: return 'status-unpaid';
        case 1: return 'status-pending';
        case 2: return 'status-accepted';
        case 3: return 'status-completed';
        default: return '';
      }
    },

    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return '';
      const date = new Date(dateTime);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    }
  }
};
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

/* 筛选栏样式 */
.order-filter {
  display: flex;
  flex-direction: column;
  gap: 1.6vw;
  padding: 2.4vw 3.5vw 1.8vw;
  background-color: #ffffff;
  position: fixed;
  top: 12vw;
  left: 0;
  width: 100%;
  box-sizing: border-box;
  z-index: 99;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

/* 每一块筛选区域 */
.filter-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2vw;
}

.section-title {
  font-size: 3.4vw;
  color: #333;
  font-weight: 600;
  white-space: nowrap;
}

/* 店铺标签区域 */
.business-switcher {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 2vw;
}

.biz-tag {
  padding: 1.4vw 3vw;
  border-radius: 999px;
  border: 1px solid #ddd;
  background-color: #f8f8f8;
  font-size: 3.2vw;
  color: #666;
  cursor: pointer;
  max-width: 40vw;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.biz-tag.active {
  border-color: #0097FF;
  background-color: #e6f3ff;
  color: #0097FF;
  font-weight: 600;
}

/* 状态筛选按钮 */
.status-filter {
  flex: 1;
  display: flex;
  justify-content: space-between;
  gap: 1.5vw;
}

.status-filter button {
  flex: 1;
  padding: 1.6vw 0;
  border-radius: 999px;
  border: 1px solid #ddd;
  background-color: #f8f8f8;
  font-size: 3.1vw;
  color: #666;
  cursor: pointer;
}

.status-filter button.active {
  border-color: #0097FF;
  background-color: #0097FF;
  color: #fff;
  font-weight: 600;
}

/* 订单列表样式 */
.order-list {
  /* header(12vw) + filter区大约 14~16vw，适当多留一些空隙避免遮挡 */
  padding-top: 30vw;
  padding-bottom: 5vw;
}

/* 单个订单项 */
.order-item {
  background-color: white;
  margin: 2vw;
  border-radius: 3vw;
  padding: 3vw;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  position: relative;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2vw;
}

.order-info {
  flex: 1;
}

.order-id {
  font-size: 3.2vw;
  color: #333;
  margin: 0 0 1vw 0;
  font-weight: bold;
}

.order-time {
  font-size: 2.8vw;
  color: #999;
  margin: 0;
}

/* 订单状态样式（右上角） */
.status {
  font-size: 3.2vw;
  font-weight: bold;
  padding: 1vw 2vw;
  border-radius: 2vw;
  white-space: nowrap;
}

.status.status-unpaid {
  color: #999;
  background-color: #f0f0f0;
}

.status.status-pending {
  color: #ff9900;
  background-color: #fff4e6;
}

.status.status-accepted {
  color: #0097FF;
  background-color: #e6f3ff;
}

.status.status-completed {
  color: #00cc66;
  background-color: #e6f9f0;
}

/* 订单商品列表 */
.order-foods {
  border-top: 1px dashed #eee;
  border-bottom: 1px dashed #eee;
  padding: 2vw 0;
  margin-bottom: 2vw;
}

.food-item {
  font-size: 3.2vw;
  color: #666;
  margin: 1vw 0;
}

/* 订单底部（金额+操作按钮） */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-total {
  font-size: 3.6vw;
  color: #333;
  font-weight: bold;
  margin: 0;
}

.order-actions {
  display: flex;
  gap: 2vw;
}

.order-actions button {
  padding: 2vw 4vw;
  border: none;
  border-radius: 2vw;
  font-size: 3.2vw;
  cursor: pointer;
  transition: opacity 0.2s;
  font-weight: bold;
}

.order-actions button:active {
  opacity: 0.7;
}

.btn-accept {
  background-color: #0097FF;
  color: white;
}

.btn-complete {
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
