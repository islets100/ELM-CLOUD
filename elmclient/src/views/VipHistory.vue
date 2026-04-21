<template>
	<div class="vip-history wrapper">
		<header>
			<i class="fa fa-arrow-left back-btn" @click="$router.back()"></i>
			<span>VIP历史记录</span>
		</header>

		<div class="content">
			<div class="history-list" v-if="vipCards.length > 0">
				<div class="history-item" v-for="card in vipCards" :key="card.id">
					<div class="card-badge" :class="card.cardType.toLowerCase()">
						<i class="fa fa-crown"></i>
					</div>
					<div class="card-info">
						<h3>{{ getCardTypeName(card.cardType) }}</h3>
						<div class="info-row">
							<span class="label">购买价格</span>
							<span class="value">¥{{ card.price }}</span>
						</div>
						<div class="info-row">
							<span class="label">购买时间</span>
							<span class="value">{{ formatDateTime(card.purchaseTime) }}</span>
						</div>
						<div class="info-row">
							<span class="label">到期时间</span>
							<span class="value">{{ formatDateTime(card.expiryTime) }}</span>
						</div>
						<div class="info-row">
							<span class="label">状态</span>
							<span class="value" :class="getStatusClass(card.status, card.isValid)">
								{{ getStatusText(card.status, card.isValid) }}
							</span>
						</div>
					</div>
				</div>
			</div>
			<div class="empty-state" v-else>
				<i class="fa fa-inbox"></i>
				<p>暂无VIP购买记录</p>
				<button class="purchase-btn" @click="$router.push('/vip/purchase')">
					立即开通VIP
				</button>
			</div>
		</div>
	</div>
</template>

<script>
	import vipApi from '../api/vip.js'

	export default {
		name: 'VipHistory',
		data() {
			return {
				vipCards: [],
				loading: false
			}
		},
		created() {
			this.loadHistory()
		},
		methods: {
			async loadHistory() {
				this.loading = true
				try {
					const response = await vipApi.getVipCardHistory()
					if (response.data.success) {
						this.vipCards = response.data.data || []
					} else {
						alert('获取历史记录失败: ' + response.data.message)
					}
				} catch (err) {
					console.error('获取历史记录失败', err)
					alert('获取历史记录失败，请重试')
				} finally {
					this.loading = false
				}
			},
			getCardTypeName(type) {
				const names = {
					'BASIC': '基础版',
					'PREMIUM': '高级版',
					'PLATINUM': '白金版'
				}
				return names[type] || type
			},
			getStatusText(status, isValid) {
				if (status === 1) return '已过期'
				if (status === 2) return '已冻结'
				return isValid ? '使用中' : '已过期'
			},
			getStatusClass(status, isValid) {
				if (status === 0 && isValid) return 'active'
				return 'expired'
			},
			formatDateTime(dateStr) {
				if (!dateStr) return '-'
				const date = new Date(dateStr)
				return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
			}
		}
	}
</script>

<style scoped>
	.vip-history {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
		background-color: #f5f5f5;
	}

	.vip-history>header {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 12vw;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #fff;
		text-align: center;
		line-height: 12vw;
		font-size: 4.5vw;
		font-weight: bold;
		z-index: 100;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.back-btn {
		position: absolute;
		left: 4vw;
		font-size: 5vw;
		cursor: pointer;
	}

	.content {
		flex: 1;
		padding-top: 12vw;
		padding-bottom: 4vw;
	}

	.history-list {
		padding: 4vw;
		display: flex;
		flex-direction: column;
		gap: 4vw;
	}

	.history-item {
		background-color: #fff;
		border-radius: 3vw;
		padding: 5vw;
		box-shadow: 0 2vw 4vw rgba(0, 0, 0, 0.1);
		display: flex;
		gap: 4vw;
	}

	.card-badge {
		flex-shrink: 0;
		width: 20vw;
		height: 20vw;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 2vw;
		background-color: #f5f5f5;
	}

	.card-badge i {
		font-size: 10vw;
	}

	.card-badge.basic i {
		color: #ffd700;
	}

	.card-badge.premium i {
		color: #ff69b4;
	}

	.card-badge.platinum i {
		color: #00ffff;
	}

	.card-info {
		flex: 1;
	}

	.card-info h3 {
		font-size: 5vw;
		margin-bottom: 2vw;
		color: #333;
	}

	.info-row {
		display: flex;
		justify-content: space-between;
		margin-bottom: 1.5vw;
		font-size: 3.5vw;
	}

	.info-row:last-child {
		margin-bottom: 0;
	}

	.info-row .label {
		color: #999;
	}

	.info-row .value {
		color: #333;
		font-weight: 500;
	}

	.info-row .value.active {
		color: #52c41a;
	}

	.info-row .value.expired {
		color: #ff6b6b;
	}

	.empty-state {
		text-align: center;
		padding: 20vw 4vw;
	}

	.empty-state i {
		font-size: 20vw;
		color: #ddd;
		margin-bottom: 4vw;
	}

	.empty-state p {
		font-size: 4vw;
		color: #999;
		margin-bottom: 6vw;
	}

	.purchase-btn {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #fff;
		border: none;
		padding: 3vw 8vw;
		border-radius: 5vw;
		font-size: 4vw;
		font-weight: bold;
		cursor: pointer;
	}
</style>
