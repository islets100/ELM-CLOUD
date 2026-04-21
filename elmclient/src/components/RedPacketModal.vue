<template>
  <div class="red-packet-modal" v-if="visible">
    <!-- 遮罩层 -->
    <div class="modal-overlay" @click="closeModal"></div>
    
    <!-- 红包容器 -->
    <div class="red-packet-container">
      <!-- 红包封面 -->
      <div class="red-packet" v-if="showCover">
        <div class="red-packet-header">
          <div class="red-packet-title">促销日红包</div>
          <div class="red-packet-subtitle">点击拆红包</div>
        </div>
        <div class="red-packet-body">
          <div class="red-packet-amount">🎉</div>
        </div>
        <div class="red-packet-footer">
          <button class="open-btn" @click="openRedPacket">拆红包</button>
        </div>
      </div>
      
      <!-- 拆红包动画 -->
      <div class="red-packet-animation" v-if="showAnimation">
        <div class="animation-content">
          <div class="animation-text">拆红包中...</div>
          <div class="animation-spinner"></div>
        </div>
      </div>
      
      <!-- 奖励展示 -->
      <div class="red-packet-reward" v-if="showReward">
        <!-- 成功图标 -->
        <div class="success-icon">✨</div>
        
        <!-- 奖励标题 -->
        <div class="reward-title">恭喜您</div>
        
        <!-- 奖励内容 -->
        <div class="reward-content">
          <!-- 积分奖励 -->
          <div class="reward-item integral">
            <div class="reward-icon">🎁</div>
            <div class="reward-info">
              <div class="reward-label">积分奖励</div>
              <div class="reward-value highlight">+{{ reward.integralAmount }} 积分</div>
            </div>
          </div>
          
          <!-- 优惠券奖励 -->
          <div class="reward-item coupon" v-if="reward.coupon">
            <div class="reward-icon">🎟️</div>
            <div class="reward-info">
              <div class="reward-label">优惠券</div>
              <div class="reward-value">¥{{ reward.coupon.value }}</div>
              <div class="reward-terms">满{{ reward.coupon.minimumOrderAmount }}元可用</div>
              <div class="reward-expire">有效期至：{{ formatDate(reward.coupon.expireDate) }}</div>
            </div>
          </div>
        </div>
        
        <!-- 确认按钮 -->
        <div class="reward-actions">
          <button class="confirm-btn" @click="confirmReward">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RedPacketModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 红包状态
      showCover: true,
      showAnimation: false,
      showReward: false,
      // 奖励信息
      reward: {
        integralAmount: 0,
        coupon: null
      }
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.resetState()
      }
    }
  },
  methods: {
    // 重置状态
    resetState() {
      this.showCover = true
      this.showAnimation = false
      this.showReward = false
      this.reward = {
        integralAmount: 0,
        coupon: null
      }
    },
    
    // 关闭弹窗
    closeModal() {
      this.$emit('close')
    },
    
    // 拆红包
    async openRedPacket() {
      try {
        // 显示拆红包动画
        this.showCover = false
        this.showAnimation = true
        
        // 模拟网络请求延迟（300-500ms）
        await this.delay(400)
        
        // 调用领取红包API
        const response = await this.$axios.post('/api/promotion-day/red-packet/claim')
        
        if (response.data.success) {
          // 隐藏动画，显示奖励
          this.showAnimation = false
          this.showReward = true
          
          // 设置奖励信息
          const record = response.data.data
          this.reward = {
            integralAmount: record.integralAmount,
            coupon: {
              value: 5.0,
              minimumOrderAmount: 50.0,
              expireDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
            }
          }
          
          // 触发领取成功事件
          this.$emit('claimed', record)
        } else {
          // 处理领取失败
          alert(response.data.error || '领取红包失败')
          this.closeModal()
        }
      } catch (err) {
        console.error('领取红包失败', err)
        alert('领取红包失败，请稍后重试')
        this.closeModal()
      }
    },
    
    // 确认领取
    confirmReward() {
      this.closeModal()
    },
    
    // 延迟函数
    delay(ms) {
      return new Promise(resolve => setTimeout(resolve, ms))
    },
    
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
  }
}
</script>

<style scoped>
/* 基础样式 */
.red-packet-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
}

.red-packet-container {
  position: relative;
  z-index: 1001;
  width: 90%;
  max-width: 320px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 红包封面样式 */
.red-packet {
  background: linear-gradient(135deg, #ff4757, #ff3742);
  color: #fff;
  padding: 24px;
  text-align: center;
}

.red-packet-header {
  margin-bottom: 16px;
}

.red-packet-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 8px;
}

.red-packet-subtitle {
  font-size: 14px;
  opacity: 0.9;
}

.red-packet-body {
  margin: 20px 0;
}

.red-packet-amount {
  font-size: 40px;
  margin: 16px 0;
  animation: bounce 1.5s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-8px); }
  60% { transform: translateY(-4px); }
}

.red-packet-footer {
  margin-top: 16px;
}

.open-btn {
  background: #fff;
  color: #ff4757;
  border: none;
  padding: 10px 28px;
  border-radius: 25px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.open-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
}

.open-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* 拆红包动画样式 */
.red-packet-animation {
  background: linear-gradient(135deg, #ff4757, #ff3742);
  color: #fff;
  padding: 48px 24px;
  text-align: center;
}

.animation-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.animation-text {
  font-size: 18px;
  margin-bottom: 16px;
}

.animation-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 奖励展示样式 */
.red-packet-reward {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: #fff;
  padding: 28px 24px;
  text-align: center;
  animation: rewardFadeIn 0.4s ease-out;
}

@keyframes rewardFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.success-icon {
  font-size: 48px;
  margin-bottom: 16px;
  animation: float 2s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.reward-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #fff;
}

.reward-content {
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  padding: 16px;
  backdrop-filter: blur(10px);
}

.reward-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.reward-item:last-child {
  border-bottom: none;
}

.reward-icon {
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

.reward-info {
  flex: 1;
  text-align: left;
}

.reward-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.reward-value {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 4px;
}

.reward-value.highlight {
  color: #fff200;
  text-shadow: 0 2px 8px rgba(255, 242, 0, 0.5);
}

.reward-terms {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 2px;
}

.reward-expire {
  font-size: 12px;
  opacity: 0.7;
}

.reward-actions {
  margin-top: 20px;
}

.confirm-btn {
  background: #fff;
  color: #ff6b6b;
  border: none;
  padding: 12px 32px;
  border-radius: 28px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  width: 100%;
}

.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
  background: #f8f9fa;
}

.confirm-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}
</style>
