<template>
	<div class="transactions-page">
		<nav class="top-nav">
			<button @click="$router.go(-1)" class="back-btn">← 返回</button>
			<h1>交易流水</h1>
		</nav>

		<div class="content">
			<div v-if="loading" class="loading">加载中...</div>
			<div v-else-if="transactions.length === 0" class="empty">
				<p>暂无交易记录</p>
			</div>
			<div v-else class="transaction-list">
				<div v-for="tx in transactions" :key="tx.id" class="transaction-item">
					<div class="transaction-header">
						<span class="type" :class="getTypeClass(tx.transactionType)">
							{{ getTypeName(tx.transactionType) }}
						</span>
						<span class="amount" :class="getAmountClass(tx)">
							{{ getAmountPrefix(tx) }}¥{{ tx.amount.toFixed(2) }}
						</span>
					</div>
					<div class="transaction-info">
						<p class="time">{{ formatTime(tx.transactionTime) }}</p>
						<p v-if="tx.remark" class="remark">{{ tx.remark }}</p>
						<p v-if="tx.orderId" class="order-id">订单号: {{ tx.orderId }}</p>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
	import walletApi from '../api/wallet.js'

	export default {
		name: 'WalletTransactions',
		data() {
			return {
				transactions: [],
				loading: false
			}
		},
		created() {
			this.loadTransactions()
		},
		methods: {
			async loadTransactions() {
				this.loading = true
				try {
					const response = await walletApi.getTransactions()
					if (response.data.success) {
						this.transactions = response.data.data || []
					} else {
						alert('获取交易流水失败: ' + response.data.message)
					}
				} catch (err) {
					console.error('获取交易流水失败', err)
					alert('获取交易流水失败，请重试')
				} finally {
					this.loading = false
				}
			},
			getTypeName(type) {
				const typeMap = {
					'RECHARGE': '充值',
					'WITHDRAW': '提现',
					'PAYMENT': '支付',
					'VIP_PURCHASE': 'VIP购买',
					'OVERDRAFT_REPAYMENT': '透支还款',
					'PAYMENT_FROZEN': '支付（冻结）'
				}
				return typeMap[type] || type
			},
			getTypeClass(type) {
				return {
					'recharge': type === 'RECHARGE',
					'withdraw': type === 'WITHDRAW',
					'payment': type === 'PAYMENT' || type === 'PAYMENT_FROZEN',
					'vip-purchase': type === 'VIP_PURCHASE',
					'repayment': type === 'OVERDRAFT_REPAYMENT'
				}
			},
			getAmountPrefix(tx) {
				if (tx.transactionType === 'RECHARGE') {
					return '+'
				} else if (tx.transactionType === 'WITHDRAW' || tx.transactionType === 'VIP_PURCHASE') {
					return '-'
				} else if (tx.transactionType === 'OVERDRAFT_REPAYMENT') {
					return '-'
				} else if (tx.transactionType === 'PAYMENT' || tx.transactionType === 'PAYMENT_FROZEN') {
					// 判断是收入还是支出（根据当前用户ID）
					// 这里简化处理，可以根据实际情况调整
					return tx.toWalletUserId ? '+' : '-'
				}
				return ''
			},
			getAmountClass(tx) {
				const prefix = this.getAmountPrefix(tx)
				return {
					'income': prefix === '+',
					'expense': prefix === '-'
				}
			},
			formatTime(timeStr) {
				if (!timeStr) return ''
				const date = new Date(timeStr)
				const year = date.getFullYear()
				const month = String(date.getMonth() + 1).padStart(2, '0')
				const day = String(date.getDate()).padStart(2, '0')
				const hours = String(date.getHours()).padStart(2, '0')
				const minutes = String(date.getMinutes()).padStart(2, '0')
				return `${year}-${month}-${day} ${hours}:${minutes}`
			}
		}
	}
</script>

<style scoped>
	.transactions-page {
		min-height: 100vh;
		background-color: #f5f5f5;
	}

	.top-nav {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 12vw;
		background-color: #3190e8;
		color: #fff;
		display: flex;
		align-items: center;
		z-index: 100;
	}

	.back-btn {
		background: none;
		border: none;
		color: #fff;
		font-size: 5vw;
		padding: 0 4vw;
		cursor: pointer;
	}

	.top-nav h1 {
		flex: 1;
		text-align: center;
		font-size: 4.5vw;
		font-weight: bold;
		margin-right: 10vw;
	}

	.content {
		padding-top: 12vw;
		padding-bottom: 4vw;
	}

	.loading,
	.empty {
		text-align: center;
		padding: 10vw;
		color: #999;
		font-size: 4vw;
	}

	.transaction-list {
		padding: 0 4vw;
	}

	.transaction-item {
		background-color: #fff;
		border-radius: 2vw;
		padding: 4vw;
		margin-bottom: 3vw;
		box-shadow: 0 1vw 2vw rgba(0, 0, 0, 0.05);
	}

	.transaction-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2vw;
	}

	.type {
		font-size: 4vw;
		font-weight: bold;
		padding: 1vw 2vw;
		border-radius: 1vw;
	}

	.type.recharge {
		background-color: #e8f5e9;
		color: #2e7d32;
	}

	.type.withdraw {
		background-color: #ffebee;
		color: #c62828;
	}

	.type.payment {
		background-color: #e3f2fd;
		color: #1565c0;
	}

	.type.vip-purchase {
		background-color: #f3e5f5;
		color: #7b1fa2;
	}

	.type.repayment {
		background-color: #fff3e0;
		color: #e65100;
	}

	.amount {
		font-size: 5vw;
		font-weight: bold;
	}

	.amount.income {
		color: #2e7d32;
	}

	.amount.expense {
		color: #c62828;
	}

	.transaction-info {
		font-size: 3.5vw;
		color: #666;
	}

	.transaction-info p {
		margin: 1vw 0;
	}

	.time {
		color: #999;
	}

	.remark {
		color: #666;
	}

	.order-id {
		color: #999;
		font-size: 3vw;
	}
</style>

