<template>
	<div class="points wrapper">
		<header>我的积分</header>

		<!-- 内容区 -->
		<div class="content">
			<!-- 积分统计卡片 -->
			<div class="statistics-card">
				<div class="stat-item">
					<div class="stat-label">总积分</div>
					<div class="stat-value">{{ statistics.totalIntegral || 0 }}</div>
				</div>
				<div class="stat-item stat-item-with-btn">
					<div class="stat-content">
						<div class="stat-label">可用积分</div>
						<div class="stat-value-row">
							<div class="stat-value available">{{ statistics.availableIntegral || 0 }}</div>
							<button class="detail-btn" @click="goToAvailableDetails">
								<i class="el-icon-document"></i> 详情
							</button>
						</div>
					</div>
				</div>
				<div class="stat-item">
					<div class="stat-label">已过期</div>
					<div class="stat-value expired">{{ statistics.expiredIntegral || 0 }}</div>
				</div>
				<div class="stat-item">
					<div class="stat-label">已使用</div>
					<div class="stat-value">{{ statistics.usedIntegral || 0 }}</div>
				</div>
			</div>

			<!-- 生日积分领取横幅 -->
			<div class="birthday-banner" v-if="canClaimBirthdayIntegral">
				<div class="banner-content">
					<div class="banner-text">
						<h3>生日福利</h3>
						<p>生日快乐！您有生日积分待领取</p>
					</div>
					<button class="claim-btn" @click="claimBirthdayIntegral">立即领取</button>
				</div>
			</div>

			<!-- 促销日红包横幅 -->
			<div class="red-packet-banner" v-if="canClaimRedPacket" @click="showRedPacketModal = true">
				<div class="banner-content">
					<div class="banner-text">
						<h3>🎉 促销日红包</h3>
						<p>点击领取您的专属红包，包含积分和优惠券</p>
					</div>
					<div class="banner-cta">点击领取</div>
				</div>
			</div>

			<!-- 红包弹窗组件 -->
			<RedPacketModal
				:visible="showRedPacketModal"
				@close="showRedPacketModal = false"
				@claimed="onRedPacketClaimed"
			/>

			<!-- 积分明细标题 -->
			<div class="details-title">积分明细</div>

			<!-- 积分明细列表 -->
			<div class="details-list" v-if="details.length > 0">
				<div 
					v-for="item in details" 
					:key="item.id" 
					class="detail-item"
					:class="{ 'expired': isExpired(item) }"
				>
					<div class="detail-left">
						<div class="detail-type">{{ getTypeText(item.type) }}</div>
						<div class="detail-channel">{{ getChannelText(item.channel) }}</div>
						<div class="detail-time">{{ formatTime(item.createTime) }}</div>
						<div class="detail-expire" v-if="item.expireTime && item.type === 'INCREASE'">
							有效期至：{{ formatDate(item.expireTime) }}
						</div>
					</div>
					<div class="detail-right">
						<div 
							class="detail-amount"
							:class="{ 
								'increase': item.type === 'INCREASE', 
								'decrease': item.type === 'DECREASE',
								'expired': isExpired(item)
							}"
						>
							{{ item.type === 'INCREASE' ? '+' : '-' }}{{ item.amount }}
						</div>
						<div class="detail-status" v-if="item.status === 'EXPIRED'">已过期</div>
						<div class="detail-status" v-else-if="item.status === 'USED'">已使用</div>
					</div>
				</div>
			</div>

			<!-- 空状态 -->
			<div class="empty-state" v-else>
				<p>暂无积分记录</p>
			</div>
		</div>

		<!-- 底部菜单 -->
		<Footer></Footer>
	</div>
</template>

<script>
	import Footer from '../components/Footer.vue'
	import RedPacketModal from '../components/RedPacketModal.vue'
	import pointsApi from '../api/points.js'

	export default {
		name: 'Points',
		data() {
			return {
				statistics: {
					totalIntegral: 0,
					availableIntegral: 0,
					expiredIntegral: 0,
					usedIntegral: 0
				},
				details: [],
				loading: false,
				canClaimBirthdayIntegral: false,
				// 红包相关状态
				canClaimRedPacket: false,
				showRedPacketModal: false
			}
		},
		created() {
			this.loadStatistics()
			this.loadDetails()
			this.checkBirthdayIntegral()
			// 检查是否可以领取红包
			this.checkCanClaimRedPacket()
		},
		components: {
			Footer,
			RedPacketModal
		},
		methods: {
			async loadStatistics() {
				try {
					const response = await pointsApi.getStatistics()
					if (response.data.success) {
						this.statistics = response.data.data || {}
					}
				} catch (err) {
					console.error('获取积分统计失败', err)
				}
			},
			async loadDetails() {
				try {
					const response = await pointsApi.getDetails()
					if (response.data.success) {
						this.details = response.data.data || []
					}
				} catch (err) {
					console.error('获取积分明细失败', err)
				}
			},
			getTypeText(type) {
				return type === 'INCREASE' ? '获得积分' : '消费积分'
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
					'EXCHANGE': '积分兑换',
					'ORDER_DEDUCTION': '订单抵扣',
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
			isExpired(item) {
				if (item.status === 'EXPIRED') {
					return true
				}
				if (item.expireTime) {
					return new Date(item.expireTime) < new Date()
				}
				return false
			},
			// 跳转到可用积分详情页面
			goToAvailableDetails() {
				this.$router.push('/availableIntegralDetails')
			},
			// 检查是否可以领取生日积分
			async checkBirthdayIntegral() {
				try {
					const response = await pointsApi.checkBirthdayIntegral()
					if (response.data.success) {
						this.canClaimBirthdayIntegral = response.data.data
					}
				} catch (err) {
					console.error('检查生日积分领取资格失败', err)
				}
			},
			// 领取生日积分
			async claimBirthdayIntegral() {
				try {
					const response = await pointsApi.claimBirthdayIntegral()
					if (response.data.success) {
						// 更新积分统计和明细
						this.loadStatistics()
						this.loadDetails()
						// 更新生日积分领取状态
						this.canClaimBirthdayIntegral = false
						// 显示成功提示
						alert(`成功领取 ${response.data.data} 积分`)
					} else {
						alert(response.data.error || '领取积分失败')
					}
				} catch (err) {
					console.error('领取生日积分失败', err)
					alert('领取积分失败，请稍后重试')
				}
			},

			// 检查是否可以领取红包
			async checkCanClaimRedPacket() {
				try {
					const response = await pointsApi.checkCanClaimRedPacket()
					if (response.data.success) {
						this.canClaimRedPacket = response.data.data
					}
				} catch (err) {
					console.error('检查红包领取资格失败', err)
				}
			},

			// 处理红包领取成功
			async onRedPacketClaimed(record) {
				try {
					// 更新积分统计和明细
					await this.loadStatistics()
					await this.loadDetails()
					// 更新红包领取状态
					this.canClaimRedPacket = false
					// 显示成功提示
					alert(`成功领取 ${record.integralAmount} 积分和一张优惠券`)
				} catch (err) {
					console.error('更新积分信息失败', err)
				}
			}
		}
	}
</script>

<style scoped>
	.points {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
	}

	.points > header {
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
	}

	.content {
		margin-top: 12vw;
		padding-bottom: 14vw;
		flex: 1;
		padding: 4vw;
		overflow-y: auto;
	}

	/* 统计卡片 */
	.statistics-card {
		background: #fff;
		border-radius: 2vw;
		padding: 4vw;
		margin-bottom: 4vw;
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 3vw;
		box-shadow: 0 1vw 2vw rgba(0, 0, 0, 0.05);
	}

	.stat-item {
		text-align: center;
	}

	.stat-item-with-btn {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
	}

	.stat-content {
		width: 100%;
		text-align: center;
	}

	.stat-value-row {
		position: relative;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-top: 2vw;
	}

	.stat-value-row .stat-value {
		position: absolute;
		left: 50%;
		transform: translateX(-50%);
	}

	.stat-value-row .detail-btn {
		margin-left: auto;
		margin-right: 2vw;
	}

	.detail-btn {
		background-color: #0097FF;
		color: white;
		border: none;
		padding: 1vw 2.5vw;
		border-radius: 3vw;
		font-size: 2.5vw;
		cursor: pointer;
		display: flex;
		align-items: center;
		gap: 0.8vw;
		transition: all 0.3s ease;
		box-shadow: 0 2px 8px rgba(0, 151, 255, 0.3);
		white-space: nowrap;
		flex-shrink: 0;
	}

	.detail-btn:hover {
		transform: translateY(-1px);
		box-shadow: 0 4px 12px rgba(0, 151, 255, 0.4);
		background-color: #0086e6;
	}

	.detail-btn:active {
		transform: translateY(0);
	}

	.stat-label {
		font-size: 3.2vw;
		color: #666;
		margin-bottom: 2vw;
	}

	.stat-value {
		font-size: 5vw;
		font-weight: bold;
		color: #333;
	}

	.stat-value.available {
		color: #0097FF;
	}

	.stat-value.expired {
		color: #999;
		text-decoration: line-through;
	}

	/* 明细标题 */
	.details-title {
		font-size: 4.5vw;
		font-weight: bold;
		color: #333;
		margin-bottom: 3vw;
	}

	/* 明细列表 */
	.details-list {
		background: #fff;
		border-radius: 2vw;
		overflow: hidden;
	}

	.detail-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 4vw;
		border-bottom: 1px solid #f0f0f0;
	}

	.detail-item:last-child {
		border-bottom: none;
	}

	.detail-item.expired {
		background-color: #f5f5f5;
		opacity: 0.6;
	}

	.detail-left {
		flex: 1;
	}

	.detail-type {
		font-size: 4vw;
		font-weight: bold;
		color: #333;
		margin-bottom: 1vw;
	}

	.detail-channel {
		font-size: 3.2vw;
		color: #666;
		margin-bottom: 1vw;
	}

	.detail-time {
		font-size: 3vw;
		color: #999;
		margin-bottom: 1vw;
	}

	.detail-expire {
		font-size: 2.8vw;
		color: #ff6b6b;
	}

	.detail-item.expired .detail-expire {
		color: #999;
	}

	.detail-right {
		text-align: right;
	}

	.detail-amount {
		font-size: 5vw;
		font-weight: bold;
		margin-bottom: 1vw;
	}

	.detail-amount.increase {
		color: #0097FF;
	}

	.detail-amount.decrease {
		color: #ff6b6b;
	}

	.detail-amount.expired {
		color: #999;
		text-decoration: line-through;
	}

	.detail-status {
		font-size: 2.8vw;
		color: #999;
	}

	/* 空状态 */
	.empty-state {
		text-align: center;
		padding: 10vw;
		color: #999;
		font-size: 3.5vw;
	}

	/* 生日积分领取横幅 */
	.birthday-banner {
		background: linear-gradient(135deg, #ff6b6b, #ee5a24);
		border-radius: 2vw;
		padding: 4vw;
		margin-bottom: 4vw;
		box-shadow: 0 2vw 4vw rgba(255, 107, 107, 0.3);
		animation: bounce 2s infinite;
	}

	@keyframes bounce {
		0%, 20%, 50%, 80%, 100% {
			transform: translateY(0);
		}
		40% {
			transform: translateY(-3vw);
		}
		60% {
			transform: translateY(-1.5vw);
		}
	}

	.banner-content {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.banner-text {
		flex: 1;
		color: #fff;
	}

	.banner-text h3 {
		font-size: 4.5vw;
		font-weight: bold;
		margin-bottom: 1vw;
	}

	.banner-text p {
		font-size: 3.2vw;
		opacity: 0.9;
	}

	.claim-btn {
		background: #fff;
		color: #ff6b6b;
		border: none;
		padding: 2.5vw 5vw;
		border-radius: 3vw;
		font-size: 3.5vw;
		font-weight: bold;
		cursor: pointer;
		transition: all 0.3s ease;
		box-shadow: 0 1vw 2vw rgba(0, 0, 0, 0.1);
	}

	.claim-btn:hover {
		background: #f8f9fa;
		transform: translateY(-0.5vw);
		box-shadow: 0 1.5vw 3vw rgba(0, 0, 0, 0.15);
	}

	.claim-btn:active {
		transform: translateY(0);
		box-shadow: 0 1vw 2vw rgba(0, 0, 0, 0.1);
	}

	/* 促销日红包横幅 */
	.red-packet-banner {
		background: linear-gradient(135deg, #ff4757, #ff3742);
		border-radius: 2vw;
		padding: 4vw;
		margin-bottom: 4vw;
		box-shadow: 0 2vw 4vw rgba(255, 71, 87, 0.3);
		animation: bounce 2s infinite;
		cursor: pointer;
		transition: all 0.3s ease;
	}

	.red-packet-banner:hover {
		transform: translateY(-1vw);
		box-shadow: 0 3vw 6vw rgba(255, 71, 87, 0.4);
	}

	.red-packet-banner .banner-content {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.red-packet-banner .banner-text {
		flex: 1;
		color: #fff;
	}

	.red-packet-banner .banner-text h3 {
		font-size: 4.5vw;
		font-weight: bold;
		margin-bottom: 1vw;
	}

	.red-packet-banner .banner-text p {
		font-size: 3.2vw;
		opacity: 0.9;
	}

	.red-packet-banner .banner-cta {
		background: #fff;
		color: #ff4757;
		padding: 2.5vw 5vw;
		border-radius: 3vw;
		font-size: 3.5vw;
		font-weight: bold;
		box-shadow: 0 1vw 2vw rgba(0, 0, 0, 0.1);
	}
</style>
