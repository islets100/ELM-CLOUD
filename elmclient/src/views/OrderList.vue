<template>
  <div class="business-orders">
    <header>
      <p>店铺订单管理</p>
    </header>
    
    <!-- 订单状态筛选 -->
    <div class="order-filter">
      <button :class="{ active: status === '' }" @click="status = ''">全部</button>
      <button :class="{ active: status === '0' }" @click="status = '0'">待支付</button>
      <button :class="{ active: status === '1' }" @click="status = '1'">已接单</button>
      <button :class="{ active: status === '2' }" @click="status = '2'">已完成</button>
      <button :class="{ active: status === '3' }" @click="status = '3'">已取消</button>
    </div>
    
    <!-- 订单列表 -->
    <div class="order-list">
      <div v-for="order in orders" :key="order.orderId" class="order-item">
        <div class="order-header">
          <p>订单编号: {{ order.orderId }}</p>
          <p>下单时间: {{ order.createTime }}</p>
          <p class="status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </p>
        </div>
        
        <div class="order-foods">
          <p v-for="food in order.orderFoods" :key="food.foodId">
            {{ food.foodName }} x {{ food.quantity }}
          </p>
        </div>
        
        <div class="order-footer">
          <p>总金额: ¥{{ order.orderTotal }}</p>
          <div class="order-actions">
            <button v-if="order.status === '0'" @click="handleAccept(order.orderId)">接单</button>
            <button v-if="order.status === '1'" @click="handleComplete(order.orderId)">完成</button>
            <button v-if="order.status === '0'" @click="handleCancel(order.orderId)">取消</button>
          </div>
        </div>
      </div>
      
      <div v-if="orders.length === 0" class="no-orders">
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
      <div class="footer-item" @click="$router.push('/businessManage')">
        <i class="fa fa-user"></i>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BusinessOrders',
  data() {
    return {
      orders: [], // 订单列表
      status: '', // 筛选状态（空=全部）
      businessId: 'test_business_123', // 模拟商家ID（后续从接口获取）
      refreshTimer: null // 定时刷新计时器
    };
  },
  created() {
    // 1. 验证商家身份（真实场景保留）
    const user = this.$getSessionStorage('user');
    if (!user || user.userType !== 1) {
      this.$router.push('/index');
      return;
    }

    // 2. 初始化订单数据（真实场景：先获取商家ID，再拉取订单）
    // this.getBusinessId(); // 真实场景：从用户ID获取商家ID
    this.getOrders(); // 拉取订单列表

    // 3. 定时刷新（核心：用户下单后自动同步，30秒一次）
    this.startRefreshTimer();
  },
  beforeUnmount() {
    // 页面销毁时清除定时器，避免内存泄漏
    this.clearRefreshTimer();
  },
  methods: {
    // 真实场景：从用户ID获取商家ID（当前注释，用模拟ID）
    getBusinessId() {
      const userId = this.$getSessionStorage('user').userId;
      this.$axios.post('BusinessController/getBusinessByUserId', this.$qs.stringify({ userId }))
        .then(res => {
          if (res.data.businessId) {
            this.businessId = res.data.businessId;
            this.getOrders(); // 获取商家ID后拉取订单
          }
        })
        .catch(error => {
          console.error('获取商家ID失败:', error);
        });
    },

    // 拉取订单列表（核心：同步用户新下单的关键）
    getOrders() {
      // ===================== 真实接口调用（后续启用）=====================
      /* this.$axios.post('OrdersController/listBusinessOrders', this.$qs.stringify({
        businessId: this.businessId, // 商家唯一ID，确保只拉取当前店铺订单
        status: this.status // 筛选状态
      }))
        .then(res => {
          this.orders = res.data || []; // 接口返回最新订单列表
        })
        .catch(error => {
          console.error('获取订单失败:', error);
          this.orders = [];
        }); */

      // ===================== 模拟订单数据（当前使用，后续删除）=====================
      this.orders = [
        // 新下单的待处理订单（模拟用户刚创建的订单）
        {
          orderId: 'ORD' + Date.now().toString().slice(-6), // 动态生成订单号
          createTime: new Date().toLocaleString('zh-CN', { 
            year: 'numeric', month: '2-digit', day: '2-digit', 
            hour: '2-digit', minute: '2-digit' 
          }), // 实时时间
          status: '0', // 待处理（用户刚下单）
          orderTotal: 68.5, // 订单总价
          orderFoods: [
            { foodId: 'f001', foodName: '珍珠奶茶', quantity: 2 },
            { foodId: 'f003', foodName: '巧克力圣代', quantity: 1 }
          ]
        },
        // 已存在的订单（不同状态）
        {
          orderId: 'ORD24092101',
          createTime: '2024-09-21 10:30',
          status: '1', // 已接单
          orderTotal: 36,
          orderFoods: [
            { foodId: 'f002', foodName: '草莓奶昔', quantity: 1 },
            { foodId: 'f004', foodName: '蓝莓气泡水', quantity: 1 }
          ]
        },
        {
          orderId: 'ORD24092102',
          createTime: '2024-09-21 09:15',
          status: '2', // 已完成
          orderTotal: 52,
          orderFoods: [
            { foodId: 'f001', foodName: '珍珠奶茶', quantity: 3 },
            { foodId: 'f003', foodName: '巧克力圣代', quantity: 1 }
          ]
        },
        {
          orderId: 'ORD24092103',
          createTime: '2024-09-21 08:45',
          status: '3', // 已取消
          orderTotal: 28,
          orderFoods: [
            { foodId: 'f002', foodName: '草莓奶昔', quantity: 1 },
            { foodId: 'f004', foodName: '蓝莓气泡水', quantity: 1 }
          ]
        }
      ];
    },

    // 获取订单状态文本
    getStatusText(status) {
      switch(status) {
        case '0': return '待处理';
        case '1': return '已接单';
        case '2': return '已完成';
        case '3': return '已取消';
        default: return '未知状态';
      }
    },

    // 获取订单状态样式
    getStatusClass(status) {
      switch(status) {
        case '0': return 'pending';
        case '1': return 'accepted';
        case '2': return 'completed';
        case '3': return 'cancelled';
        default: return '';
      }
    },

    // 接单（状态改为1）
    handleAccept(orderId) {
      this.updateOrderStatus(orderId, '1', '接单成功');
    },

    // 完成订单（状态改为2）
    handleComplete(orderId) {
      this.updateOrderStatus(orderId, '2', '订单已完成');
    },

    // 取消订单（状态改为3）
    handleCancel(orderId) {
      this.updateOrderStatus(orderId, '3', '订单已取消');
    },

    // 更新订单状态（核心：同步后端状态）
    updateOrderStatus(orderId, targetStatus, successMsg) {
      // ===================== 真实接口调用（后续启用）=====================
      /* this.$axios.post('OrdersController/updateOrderStatus', this.$qs.stringify({
        orderId: orderId,
        status: targetStatus,
        businessId: this.businessId // 验证商家权限
      }))
        .then(res => {
          if (res.data > 0) {
            alert(successMsg);
            this.getOrders(); // 刷新订单列表
          } else {
            alert('操作失败，请重试');
          }
        })
        .catch(error => {
          console.error('更新订单状态失败:', error);
          alert('网络错误，请重试');
        }); */

      // ===================== 模拟状态更新（当前使用，后续删除）=====================
      const orderIndex = this.orders.findIndex(order => order.orderId === orderId);
      if (orderIndex !== -1) {
        this.orders[orderIndex].status = targetStatus;
        alert(successMsg);
      } else {
        alert('操作失败，订单不存在');
      }
    },

    // 启动定时刷新（30秒一次，确保用户下单后自动同步）
    startRefreshTimer() {
      this.refreshTimer = setInterval(() => {
        console.log('自动刷新订单列表...');
        this.getOrders();
      }, 30000); // 30秒 = 30000毫秒
    },

    // 清除定时刷新
    clearRefreshTimer() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer);
        this.refreshTimer = null;
      }
    }
  },
  watch: {
    // 筛选状态变化时，重新拉取订单
    status() {
      if (this.businessId) {
        this.getOrders();
      }
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