<template>
	<div class="vip-repay wrapper">
		<header>
			<i class="fa fa-arrow-left back-btn" @click="$router.back()"></i>
			<span>透支还款</span>
		</header>

		<div class="content">
			<!-- 透支信息 -->
			<div class="overdraft-info">
				<div class="info-item">
					<span class="label">已透支金额</span>
					<span class="value warning">¥{{ wallet.overdraftAmount || '0.00' }}</span>
				</div>
				<div class="info-item">
					<span class="label">钱包余额</span>
					<span class="value">¥{{ wallet.availableBalance || '0.00' }}</span>
				</div>
				<div class="info-item">
					<span class="label">透支额度</span>
					<span class="value">¥{{ wallet.creditLimit || '0.00' }}</span>
				</div>
			</div>

			<!-- 还款金额输入 -->
			<div class="repay-form">
				<h3>还款金额</h3>
				<div class="amount-input">
					<span class="currency">¥</span>
					<input type="number" 
						v-model="repayAmount" 
						placeholder="请输入还款金额"
						step="0.01"
						min="0.01">
				</div>
				<div class="quick-amounts">
					<button @click="setAmount(100)">100元</button>
					<button @click="setAmount(500)">500元</button>
					<button @click="setAmount(1000)">1000元</button>
					<button @click="setAllAmount">全部还清</button>
				</div>
				<div class="tips">
					<p><i class="fa fa-info-circle"></i> 还款将优先偿还利息，再偿还本金</p>
					<p><i class="fa fa-info-circle"></i> 按透支时间顺序依次还款</p>
				</div>
			</div>

			<!-- 透支记录预览 -->
			<div class="records-preview" v-if="unpaidRecords.length > 0">
				<h3>待还款记录</h3>
				<div class="record-item" v-for="record in unpaidRecords" :key="record.id">
					<div class="record-header">
						<span class="record-time">{{ formatDate(record.overdraftTime) }}</span>
						<span class="record-status" :class="{ 'overdue': record.isOverdue }">
							{{ record.isOverdue ? '已逾期' : '未还清' }}
						</span>
					</div>
					<div class="record-detail">
						<span>剩余本金: ¥{{ record.remainingAmount }}</span>
						<span>未付利息: ¥{{ record.unpaidInterest }}</span>
					</div>
				</div>
			</div>

			<!-- 还款按钮 -->
			<div class="action-section">
				<button class="repay-btn" 
					:disabled="!canRepay || loading"
					@click="confirmRepay">
					{{ loading ? '还款中...' : '确认还款' }}
				</button>
			</div>
		</div>
	</div>
</template>

<script>
	import vipApi from '../api/vip.js'
	import walletApi from '../api/wallet.js'

	export default {
		name: 'VipRepay',
		data() {
			return {
				wallet: {
					overdraftAmount: 0,
					availableBalance: 0,
					creditLimit: 0
				},
				unpaidRecords: [],
				repayAmount: '',
				loading: false
			}
		},
		computed: {
			canRepay() {
				const amount = parseFloat(this.repayAmount)
				return amount > 0 && 
					   amount <= this.wallet.availableBalance && 
					   amount <= this.wallet.overdraftAmount
			}
		},
		created() {
			this.loadData()
		},
		methods: {
			async loadData() {
				await Promise.all([
					this.loadWallet(),
					this.loadOverdraftRecords()
				])
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
						this.unpaidRecords = (response.data.data || []).filter(r => r.status === 0)
					}
				} catch (err) {
					console.error('获取透支记录失败', err)
				}
			},
			setAmount(amount) {
				this.repayAmount = amount.toString()
			},
			setAllAmount() {
				this.repayAmount = this.wallet.overdraftAmount.toString()
			},
			async confirmRepay() {
				const amount = parseFloat(this.repayAmount)
				
				if (!amount || amount <= 0) {
					alert('请输入有效的还款金额')
					return
				}

				if (amount > this.wallet.availableBalance) {
					alert('余额不足')
					return
				}

				if (amount > this.wallet.overdraftAmount) {
					alert('还款金额不能超过已透支金额')
					return
				}

				if (!confirm(`确认还款 ¥${amount.toFixed(2)} 吗？`)) {
					return
				}

				this.loading = true
				try {
					const response = await vipApi.repayOverdraft(amount)
					if (response.data.success) {
						alert('还款成功！')
						this.$router.push('/vip/center')
					} else {
						alert('还款失败: ' + response.data.message)
					}
				} catch (err) {
					console.error('还款失败', err)
					alert('还款失败，请重试')
				} finally {
					this.loading = false
				}
			},
			formatDate(dateStr) {
				if (!dateStr) return '-'
				const date = new Date(dateStr)
				return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
			}
		}
	}
</script>

<style scoped>
	.vip-repay {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
		background-color: #f5f5f5;
	}

	.vip-repay>header {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 12vw;
		background-color: #3190e8;
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

	.overdraft-info {
		background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
		margin: 4vw;
		padding: 5vw;
		border-radius: 3vw;
		color: #fff;
		box-shadow: 0 2vw 4vw rgba(0, 0, 0, 0.1);
	}

	.info-item {
		display: flex;
		justify-content: space-between;
		margin-bottom: 3vw;
		font-size: 4vw;
	}

	.info-item:last-child {
		margin-bottom: 0;
	}

	.info-item .label {
		opacity: 0.9;
	}

	.info-item .value {
		font-weight: bold;
		font-size: 4.5vw;
	}

	.info-item .value.warning {
		color: #ffd700;
	}

	.repay-form {
		background-color: #fff;
		margin: 0 4vw 4vw;
		padding: 5vw;
		border-radius: 2vw;
		box-shadow: 0 1vw 2vw rgba(0, 0, 0, 0.05);
	}

	.repay-form h3 {
		font-size: 4.5vw;
		margin-bottom: 3vw;
		color: #333;
	}

	.amount-input {
		display: flex;
		align-items: center;
		border: 2px solid #e0e0e0;
		border-radius: 2vw;
		padding: 3vw;
		margin-bottom: 3vw;
	}

	.amount-input .currency {
		font-size: 5vw;
		font-weight: bold;
		color: #666;
		margin-right: 2vw;
	}

	.amount-input input {
		flex: 1;
		border: none;
		outline: none;
		font-size: 5vw;
		font-weight: bold;
	}

	.quick-amounts {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 2vw;
		margin-bottom: 3vw;
	}

	.quick-amounts button {
		padding: 2.5vw;
		border: 1px solid #3190e8;
		background-color: #fff;
		color: #3190e8;
		border-radius: 1.5vw;
		font-size: 3.5vw;
		cursor: pointer;
	}

	.quick-amounts button:active {
		background-color: #3190e8;
		color: #fff;
	}

	.tips {
		background-color: #f0f8ff;
		padding: 3vw;
		border-radius: 1.5vw;
		font-size: 3vw;
		color: #666;
	}

	.tips p {
		margin: 0 0 1.5vw 0;
		display: flex;
		align-items: center;
		gap: 2vw;
	}

	.tips p:last-child {
		margin-bottom: 0;
	}

	.tips i {
		color: #3190e8;
	}

	.records-preview {
		background-color: #fff;
		margin: 0 4vw 4vw;
		padding: 4vw;
		border-radius: 2vw;
		box-shadow: 0 1vw 2vw rgba(0, 0, 0, 0.05);
	}

	.records-preview h3 {
		font-size: 4.5vw;
		margin-bottom: 3vw;
		color: #333;
	}

	.record-item {
		background-color: #f9f9f9;
		padding: 3vw;
		border-radius: 2vw;
		margin-bottom: 2vw;
		border-left: 3px solid #ff6b6b;
	}

	.record-item:last-child {
		margin-bottom: 0;
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
		background-color: #ffd700;
		color: #fff;
	}

	.record-status.overdue {
		background-color: #ff6b6b;
	}

	.record-detail {
		display: flex;
		justify-content: space-between;
		font-size: 3.5vw;
		color: #666;
	}

	.action-section {
		padding: 0 4vw;
	}

	.repay-btn {
		width: 100%;
		height: 12vw;
		border: none;
		border-radius: 2vw;
		font-size: 4.5vw;
		font-weight: bold;
		background-color: #ff6b6b;
		color: #fff;
		cursor: pointer;
	}

	.repay-btn:disabled {
		background-color: #ccc;
		cursor: not-allowed;
	}
</style>
