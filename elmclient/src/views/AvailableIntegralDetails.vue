<template>
	<div class="available-integral-details wrapper">
		<header>
			<i class="el-icon-arrow-left back-btn" @click="goBack"></i>
			<span>可用积分详情</span>
		</header>

		<!-- 内容区 -->
		<div class="content">
			<!-- 总可用积分 -->
			<div class="total-card">
				<div class="total-label">当前可用积分</div>
				<div class="total-value">{{ totalAvailable }}</div>
			</div>

			<!-- 可用积分列表 -->
			<div class="integral-list" v-if="integralList.length > 0">
				<div 
					v-for="item in integralList" 
					:key="item.id" 
					class="integral-item"
					:class="{ 'expired-item': isExpired(item) }"
				>
					<div class="item-header">
						<div class="item-channel">
							<i class="el-icon-present"></i>
							{{ getChannelText(item.channel) }}
						</div>
						<div class="item-remaining">
							<span v-if="isExpired(item)" class="expired-badge">已过期</span>
							<span v-else>剩余 <span class="remaining-value">{{ getRemainingAmount(item) }}</span> 积分</span>
						</div>
					</div>

					<div class="item-body">
						<div class="info-row">
							<span class="info-label">获得时间：</span>
							<span class="info-value">{{ formatTime(item.createTime) }}</span>
						</div>
						<div class="info-row">
							<span class="info-label">原始积分：</span>
							<span class="info-value">{{ item.amount }} 积分</span>
						</div>
						<div class="info-row">
							<span class="info-label">过期时间：</span>
							<span class="info-value" :class="{ 'expire-soon': isExpireSoon(item), 'expired-text': isExpired(item) }">
								{{ formatDate(item.expireTime) }}
								<span v-if="isExpireSoon(item) && !isExpired(item)" class="expire-tag">即将过期</span>
								<span v-if="isExpired(item)" class="expire-tag expired">已过期</span>
							</span>
						</div>
						<div class="info-row" v-if="item.description">
							<span class="info-label">说明：</span>
							<span class="info-value">{{ item.description }}</span>
						</div>
					</div>

					<!-- 消费记录 -->
					<div class="consumption-section" v-if="hasConsumption(item)">
						<div class="consumption-title">
							<i class="el-icon-tickets"></i>
							消费记录
						</div>
						<div class="consumption-info">
							已消费 {{ getConsumedAmount(item) }} 积分
						</div>
					</div>

					<!-- 进度条 -->
					<div class="progress-bar">
						<div class="progress-fill" :style="{ width: getProgressPercent(item) + '%' }"></div>
					</div>
					<div class="progress-text">
						可用率：{{ getProgressPercent(item) }}%
					</div>
				</div>
			</div>

			<!-- 空状态 -->
			<div class="empty-state" v-else>
				<i class="el-icon-box"></i>
				<p>暂无可用积分</p>
			</div>
		</div>

		<!-- 底部菜单 -->
		<Footer></Footer>
	</div>
</template>

<script>
	import Footer from '../components/Footer.vue'
	import pointsApi from '../api/points.js'

	export default {
		name: 'AvailableIntegralDetails',
		data() {
			return {
				integralList: [],
				totalAvailable: 0
			}
		},
		created() {
			this.loadAvailableDetails()
		},
		components: {
			Footer
		},
		methods: {
			async loadAvailableDetails() {
				try {
					const response = await pointsApi.getAvailableDetails()
					if (response.data.success) {
						this.integralList = response.data.data || []
						this.calculateTotal()
					}
				} catch (err) {
					console.error('获取可用积分详情失败', err)
					this.$message.error('获取可用积分详情失败')
				}
			},
			calculateTotal() {
				this.totalAvailable = this.integralList.reduce((sum, item) => {
					return sum + this.getRemainingAmount(item)
				}, 0)
			},
			getRemainingAmount(item) {
				// 如果有剩余金额字段，使用剩余金额；否则使用原始金额
				if (item.remainingAmount != null) {
					return item.remainingAmount
				}
				return item.amount || 0
			},
			getConsumedAmount(item) {
				// 计算已消费的积分
				const remaining = this.getRemainingAmount(item)
				return (item.amount || 0) - remaining
			},
			hasConsumption(item) {
				// 判断是否有消费记录
				return this.getConsumedAmount(item) > 0
			},
			getProgressPercent(item) {
				// 计算可用率百分比
				const remaining = this.getRemainingAmount(item)
				const original = item.amount || 1
				return Math.round((remaining / original) * 100)
			},
			getChannelText(channel) {
				const channelMap = {
					'CONSUMPTION': '订单消费',
					'ACTIVITY': '活动奖励',
					'COMMENT': '评论奖励',
					'PICTURE': '图片评论',
					'CHECK_IN': '每日签到',
					'PACKET': '红包奖励',
					'VIP_LEVEL': '会员奖励',
					'BIRTHDAY': '生日奖励',
					'OTHER': '其他'
				}
				return channelMap[channel] || channel
			},
			formatTime(timeStr) {
				if (!timeStr) return ''
				const date = new Date(timeStr)
				const year = date.getFullYear()
				const month = String(date.getMonth() + 1).padStart(2, '0')
				const day = String(date.getDate()).padStart(2, '0')
				const hour = String(date.getHours()).padStart(2, '0')
				const minute = String(date.getMinutes()).padStart(2, '0')
				return `${year}-${month}-${day} ${hour}:${minute}`
			},
			formatDate(dateStr) {
				if (!dateStr) return ''
				const date = new Date(dateStr)
				const year = date.getFullYear()
				const month = String(date.getMonth() + 1).padStart(2, '0')
				const day = String(date.getDate()).padStart(2, '0')
				return `${year}-${month}-${day}`
			},
			isExpireSoon(item) {
				// 判断是否即将过期（7天内）
				if (!item.expireTime) return false
				const expireDate = new Date(item.expireTime)
				const now = new Date()
				const diffDays = (expireDate - now) / (1000 * 60 * 60 * 24)
				return diffDays > 0 && diffDays <= 7
			},
			isExpired(item) {
				// 判断是否已过期
				if (item.status === 'EXPIRED') return true
				if (!item.expireTime) return false
				return new Date(item.expireTime) < new Date()
			},
			goBack() {
				this.$router.back()
			}
		}
	}
</script>

<style scoped>
	.available-integral-details {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
		background: #f5f5f5;
	}

	.available-integral-details > header {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 12vw;
		background-color: #0097FF;
		color: #fff;
		display: flex;
		justify-content: center;
		align-items: center;
		font-size: 4.8vw;
		z-index: 1000;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
	}

	.back-btn {
		position: absolute;
		left: 4vw;
		font-size: 5vw;
		cursor: pointer;
	}

	.content {
		margin-top: 12vw;
		padding: 4vw;
		padding-bottom: 14vw;
		flex: 1;
		overflow-y: auto;
	}

	/* 总可用积分卡片 */
	.total-card {
		background-color: #0097FF;
		border-radius: 3vw;
		padding: 6vw;
		margin-bottom: 4vw;
		text-align: center;
		color: white;
		box-shadow: 0 4px 15px rgba(0, 151, 255, 0.3);
	}

	.total-label {
		font-size: 3.5vw;
		opacity: 0.9;
		margin-bottom: 2vw;
	}

	.total-value {
		font-size: 10vw;
		font-weight: bold;
	}

	/* 积分列表 */
	.integral-list {
		display: flex;
		flex-direction: column;
		gap: 3vw;
	}

	.integral-item {
		background: white;
		border-radius: 3vw;
		padding: 4vw;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
		transition: all 0.3s ease;
	}

	.integral-item:hover {
		box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
		transform: translateY(-1px);
	}

	.item-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 3vw;
		padding-bottom: 3vw;
		border-bottom: 1px solid #f0f0f0;
	}

	.item-channel {
		font-size: 4vw;
		font-weight: bold;
		color: #333;
		display: flex;
		align-items: center;
		gap: 1.5vw;
	}

	.item-channel i {
		color: #0097FF;
	}

	.item-remaining {
		font-size: 3.5vw;
		color: #666;
	}

	.remaining-value {
		font-size: 4.5vw;
		font-weight: bold;
		color: #0097FF;
	}

	.item-body {
		margin-bottom: 3vw;
	}

	.info-row {
		display: flex;
		align-items: flex-start;
		margin-bottom: 2vw;
		font-size: 3.5vw;
	}

	.info-label {
		color: #999;
		min-width: 20vw;
	}

	.info-value {
		color: #333;
		flex: 1;
	}

	.info-value.expire-soon {
		color: #ff6b6b;
	}

	.expire-tag {
		display: inline-block;
		background: #ff6b6b;
		color: white;
		padding: 0.5vw 2vw;
		border-radius: 2vw;
		font-size: 2.8vw;
		margin-left: 2vw;
	}

	.expire-tag.expired {
		background: #999;
	}

	.expired-badge {
		background: #999;
		color: white;
		padding: 1vw 2vw;
		border-radius: 2vw;
		font-size: 3vw;
	}

	.expired-item {
		opacity: 0.7;
		background: #f8f8f8;
	}

	.expired-text {
		color: #999;
	}

	/* 消费记录 */
	.consumption-section {
		background: #f8f9fa;
		border-radius: 2vw;
		padding: 3vw;
		margin-bottom: 3vw;
	}

	.consumption-title {
		font-size: 3.5vw;
		color: #666;
		margin-bottom: 2vw;
		display: flex;
		align-items: center;
		gap: 1.5vw;
	}

	.consumption-title i {
		color: #0097FF;
	}

	.consumption-info {
		font-size: 3.2vw;
		color: #333;
	}

	/* 进度条 */
	.progress-bar {
		height: 2vw;
		background: #e9ecef;
		border-radius: 1vw;
		overflow: hidden;
		margin-bottom: 1.5vw;
	}

	.progress-fill {
		height: 100%;
		background-color: #0097FF;
		transition: width 0.3s ease;
	}

	.progress-text {
		text-align: right;
		font-size: 3vw;
		color: #999;
	}

	/* 空状态 */
	.empty-state {
		text-align: center;
		padding: 10vw;
		color: #999;
	}

	.empty-state i {
		font-size: 15vw;
		margin-bottom: 3vw;
		opacity: 0.5;
	}

	.empty-state p {
		font-size: 4vw;
	}
</style>
