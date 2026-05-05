<template>
	<div class="vip-center wrapper">
		<header>VIP中心</header>

		<div class="content">
			<!-- VIP卡信息 -->
			<div class="vip-card" v-if="vipCard">
				<div class="vip-badge" :class="vipCard.cardType.toLowerCase()">
					<i class="fa fa-crown"></i>
					<span>{{ getCardTypeName(vipCard.cardType) }}</span>
				</div>
				<div class="vip-info">
					<div class="info-row">
						<span class="label">购买时间</span>
						<span class="value">{{ formatDate(vipCard.purchaseTime) }}</span>
					</div>
					<div class="info-row">
						<span class="label">到期时间</span>
						<span class="value" :class="{ 'expired': !vipCard.isValid }">
							{{ formatDate(vipCard.expiryTime) }}
						</span>
					</div>
					<div class="info-row">
						<span class="label">状态</span>
						<span class="value" :class="getStatusClass(vipCard.status)">
							{{ getStatusText(vipCard.status, vipCard.isValid) }}
						</span>
					</div>
				</div>
			</div>
			<div class="no-vip" v-else>
				<i class="fa fa-crown"></i>
				<p>您还不是VIP会员</p>
				<button class="purchase-btn" @click="$router.push('/vip/purchase')">
					立即开通
				</button>
			</div>

			<!-- 透支额度信息 -->
			<div class="credit-section" v-if="wallet.creditLimit > 0">
				<h3>透支额度</h3>
				<div class="credit-card">
					<div class="credit-item">
						<span class="label">总额度</span>
						<span class="amount">¥{{ wallet.creditLimit || '0.00' }}</span>
					</div>
					<div class="credit-item">
						<span class="label">已使用</span>
						<span class="amount used">¥{{ wallet.overdraftAmount || '0.00' }}</span>
					</div>
					<div class="credit-item">
						<span class="label">可用额度</span>
						<span class="amount available">¥{{ wallet.availableCreditLimit || '0.00' }}</span>
					</div>
				</div>
				<button class="repay-btn" v-if="wallet.overdraftAmount > 0" @click="$router.push('/vip/repay')">
					立即还款
				</button>
			</div>

			<!-- 透支记录 -->
			<div class="overdraft-section" v-if="overdraftRecords.length > 0">
				<h3>透支记录</h3>
				<div class="record-list">
					<div class="record-item" v-for="record in overdraftRecords" :key="record.id">
						<div class="record-header">
							<span class="record-time">{{ formatDateTime(record.overdraftTime) }}</span>
							<span class="record-status" :class="{ 'overdue': record.isOverdue }">
								{{ record.status === 0 ? (record.isOverdue ? '已逾期' : '未还清') : '已还清' }}
							</span>
						</div>
						<div class="record-body">
							<div class="record-row">
								<span>透支金额</span>
								<span class="highlight">¥{{ record.overdraftAmount }}</span>
							</div>
							<div class="record-row">
								<span>已还金额</span>
								<span>¥{{ record.repaidAmount }}</span>
							</div>
							<div class="record-row">
								<span>剩余本金</span>
								<span class="warning">¥{{ record.remainingAmount }}</span>
							</div>
							<div class="record-row">
								<span>累计利息</span>
								<span>¥{{ record.accumulatedInterest }}</span>
							</div>
							<div class="record-row">
								<span>未付利息</span>
								<span class="warning">¥{{ record.unpaidInterest }}</span>
							</div>
							<div class="record-row">
								<span>还款期限</span>
								<span :class="{ 'overdue': record.isOverdue }">
									{{ formatDate(record.dueDate) }}
								</span>
							</div>
						</div>
					</div>
				</div>
			</div>

			<!-- 功能菜单 -->
			<ul class="menu">
				<li @click="$router.push('/vip/purchase')" v-if="!vipCard || !vipCard.isValid">
					<span>开通/续费VIP</span>
					<span class="arrow">></span>
				</li>
				<li @click="$router.push('/vip/history')">
					<span>VIP历史记录</span>
					<span class="arrow">></span>
				</li>
				<li @click="$router.push('/wallet')">
					<span>返回钱包</span>
					<span class="arrow">></span>
				</li>
			</ul>
		</div>

		<Footer></Footer>
	</div>
</template>

<script>
	import Footer from '../components/Footer.vue'
	import vipApi from '../api/vip.js'
	import walletApi from '../api/wallet.js'

	export default {
		name: 'VipCenter',
		components: {
			Footer
		},
		data() {
			return {
				vipCard: null,
				wallet: {
					creditLimit: 0,
					overdraftAmount: 0,
					availableCreditLimit: 0
				},
				overdraftRecords: [],
				loading: false
			}
		},
		created() {
			this.loadData()
		},
		methods: {
			async loadData() {
				this.loading = true
				try {
					await Promise.all([
						this.loadVipCard(),
						this.loadWallet(),
						this.loadOverdraftRecords()
					])
				} finally {
					this.loading = false
				}
			},
			async loadVipCard() {
				try {
					const response = await vipApi.getCurrentVipCard()
					if (response.data.success) {
						this.vipCard = response.data.data
					}
				} catch (err) {
					console.log('暂无VIP卡')
				}
			},
			async loadWallet() {
				try {
					const response = await walletApi.getWallet()
					if (response.data.success) {
						this.wallet = response.data.data
					}
				} catch (err) {
					console.error('获取钱包信息失败', err)
				}
			},
			async loadOverdraftRecords() {
				try {
					const response = await vipApi.getOverdraftRecords()
					if (response.data.success) {
						this.overdraftRecords = response.data.data || []
					}
				} catch (err) {
					console.error('获取透支记录失败', err)
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
				return isValid ? '正常' : '已过期'
			},
			getStatusClass(status) {
				if (status === 0) return 'active'
				return 'expired'
			},
			formatDate(dateStr) {
				if (!dateStr) return '-'
				const date = new Date(dateStr)
				return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
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
	.vip-center {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
		background-color: #f5f5f5;
	}

	.vip-center>header {
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
	}

	.content {
		flex: 1;
		padding-top: 12vw;
		padding-bottom: 15vw;
	}

	.vip-card {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		margin: 4vw;
		padding: 6vw;
		border-radius: 3vw;
		color: #fff;
		box-shadow: 0 2vw 4vw rgba(0, 0, 0, 0.1);
	}

	.vip-badge {
		text-align: center;
		margin-bottom: 4vw;
	}

	.vip-badge i {
		font-size: 12vw;
		display: block;
		margin-bottom: 2vw;
	}

	.vip-badge span {
		font-size: 5vw;
		font-weight: bold;
	}

	.vip-badge.basic {
		color: #ffd700;
	}

	.vip-badge.premium {
		color: #ff69b4;
	}

	.vip-badge.platinum {
		color: #00ffff;
	}

	.vip-info {
		border-top: 1px solid rgba(255, 255, 255, 0.3);
		padding-top: 4vw;
	}

	.info-row {
		display: flex;
		justify-content: space-between;
		margin-bottom: 2vw;
		font-size: 3.5vw;
	}

	.info-row:last-child {
		margin-bottom: 0;
	}

	.info-row .label {
		opacity: 0.9;
	}

	.info-row .value {
		font-weight: bold;
	}

	.info-row .value.expired {
		color: #ffd700;
	}

	.info-row .value.active {
		color: #90EE90;
	}

	.no-vip {
		background-color: #fff;
		margin: 4vw;
		padding: 8vw;
		border-radius: 3vw;
		text-align: center;
		box-shadow: 0 2vw 4vw rgba(0, 0, 0, 0.1);
	}

	.no-vip i {
		font-size: 15vw;
		color: #ddd;
		margin-bottom: 3vw;
	}

	.no-vip p {
		font-size: 4vw;
		color: #999;
		margin-bottom: 4vw;
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

	.credit-section,
	.overdraft-section {
		background-color: #fff;
		margin: 4vw;
		padding: 4vw;
		border-radius: 2vw;
		box-shadow: 0 1vw 2vw rgba(0, 0, 0, 0.05);
	}

	.credit-section h3,
	.overdraft-section h3 {
		font-size: 4.5vw;
		margin-bottom: 3vw;
		color: #333;
	}

	.credit-card {
		background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
		padding: 4vw;
		border-radius: 2vw;
		color: #fff;
		margin-bottom: 3vw;
	}

	.credit-item {
		display: flex;
		justify-content: space-between;
		margin-bottom: 2vw;
		font-size: 3.5vw;
	}

	.credit-item:last-child {
		margin-bottom: 0;
	}

	.credit-item .amount {
		font-size: 4vw;
		font-weight: bold;
	}

	.credit-item .amount.used {
		color: #ffd700;
	}

	.credit-item .amount.available {
		color: #90EE90;
	}

	.repay-btn {
		width: 100%;
		background-color: #ff6b6b;
		color: #fff;
		border: none;
		padding: 3vw;
		border-radius: 2vw;
		font-size: 4vw;
		font-weight: bold;
		cursor: pointer;
	}

	.record-list {
		display: flex;
		flex-direction: column;
		gap: 3vw;
	}

	.record-item {
		background-color: #f9f9f9;
		padding: 3vw;
		border-radius: 2vw;
		border-left: 3px solid #667eea;
	}

	.record-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2vw;
		padding-bottom: 2vw;
		border-bottom: 1px solid #eee;
	}

	.record-time {
		font-size: 3vw;
		color: #666;
	}

	.record-status {
		font-size: 3vw;
		padding: 1vw 2vw;
		border-radius: 1vw;
		background-color: #90EE90;
		color: #fff;
	}

	.record-status.overdue {
		background-color: #ff6b6b;
	}

	.record-body {
		font-size: 3.5vw;
	}

	.record-row {
		display: flex;
		justify-content: space-between;
		margin-bottom: 1.5vw;
	}

	.record-row:last-child {
		margin-bottom: 0;
	}

	.record-row .highlight {
		color: #667eea;
		font-weight: bold;
	}

	.record-row .warning {
		color: #ff6b6b;
		font-weight: bold;
	}

	.record-row .overdue {
		color: #ff6b6b;
	}

	.menu {
		background-color: #fff;
		margin: 0 4vw;
		border-radius: 2vw;
		overflow: hidden;
	}

	.menu li {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 4vw;
		border-bottom: 1px solid #f0f0f0;
		font-size: 4vw;
		cursor: pointer;
	}

	.menu li:last-child {
		border-bottom: none;
	}

	.menu li:active {
		background-color: #f5f5f5;
	}

	.arrow {
		color: #999;
		font-size: 4vw;
	}
</style>
