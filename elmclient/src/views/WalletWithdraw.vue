<template>
	<div class="withdraw-page">
		<nav class="top-nav">
			<button @click="$router.go(-1)" class="back-btn">← 返回</button>
			<h1>提现</h1>
		</nav>

		<div class="content">
			<div class="wallet-info">
				<p class="label">可提现余额</p>
				<p class="balance">¥{{ wallet.availableBalance || '0.00' }}</p>
			</div>

			<div class="form-section">
				<div class="form-item">
					<label>提现金额 <span class="required">*</span></label>
					<input type="number" v-model.number="amount" placeholder="请输入提现金额" min="0.01" step="0.01" />
					<span class="hint">最低提现0.01元，从余额中扣除此金额</span>
				</div>

				<div class="fee-info" v-if="amount > 0">
					<div class="fee-item total">
						<span>提现金额</span>
						<span class="fee-value">¥{{ amount.toFixed(2) }}</span>
					</div>
					<div class="fee-item deduct">
						<span>- 手续费{{ feeRateText }}</span>
						<span class="fee-value">¥{{ feeAmount.toFixed(2) }}</span>
					</div>
					<div class="fee-item deduct">
						<span>- 奖励金扣除</span>
						<span class="fee-value">¥{{ rewardDeducted.toFixed(2) }}</span>
					</div>
					<div class="divider"></div>
					<div class="fee-item actual">
						<span>实际到账</span>
						<span class="fee-value highlight">¥{{ actualAmount.toFixed(2) }}</span>
					</div>
				</div>

				<button class="submit-btn" @click="submitWithdraw" :disabled="loading || !amount || amount <= 0 || amount > wallet.availableBalance">
					<span v-if="loading">提现中...</span>
					<span v-else>确认提现</span>
				</button>
			</div>
		</div>
	</div>
</template>

<script>
	import walletApi from '../api/wallet.js'
	import axios from 'axios'

	export default {
		name: 'Withdraw',
		data() {
			return {
				wallet: {
					balance: 0,
					availableBalance: 0,
					rewardAmount: 0
				},
				amount: null,
				loading: false,
				withdrawRules: [] // 提现手续费规则列表
			}
		},
		computed: {
			feeAmount() {
				if (!this.amount || this.amount <= 0) return 0
				// 动态计算手续费
				if (this.withdrawRules.length === 0) return 0
				
				// 使用优先级最高的启用规则
				const activeRule = this.withdrawRules.find(rule => rule.enabled)
				if (!activeRule) return 0
				
				let fee = 0
				if (activeRule.feeRate) {
					fee += this.amount * activeRule.feeRate
				}
				if (activeRule.fixedFee) {
					fee += activeRule.fixedFee
				}
				return fee
			},
			feeRateText() {
				if (this.withdrawRules.length === 0) return ''
				const activeRule = this.withdrawRules.find(rule => rule.enabled)
				if (!activeRule) return ''
				
				let text = '（'
				if (activeRule.feeRate) {
					text += (activeRule.feeRate * 100).toFixed(2) + '%'
				}
				if (activeRule.fixedFee) {
					if (activeRule.feeRate) text += ' + '
					text += '¥' + activeRule.fixedFee
				}
				text += '）'
				return text
			},
			rewardDeducted() {
				if (!this.amount || this.amount <= 0) return 0
				if (!this.wallet.rewardAmount || this.wallet.rewardAmount <= 0) return 0
				if (!this.wallet.balance || this.wallet.balance <= 0) return 0
				// 按比例扣除奖励
				const deducted = (this.amount * this.wallet.rewardAmount) / this.wallet.balance
				return Math.min(deducted, this.wallet.rewardAmount)
			},
			actualAmount() {
				if (!this.amount || this.amount <= 0) return 0
				return this.amount - this.feeAmount - this.rewardDeducted
			}
		},
		created() {
			this.loadWallet()
			this.loadWithdrawRules()
		},
		methods: {
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
			async loadWithdrawRules() {
				try {
					const response = await axios.get('/api/reward-rules/type/WITHDRAW')
					if (response.data.success) {
						// 按优先级降序排列，优先使用优先级高的规则
						this.withdrawRules = response.data.data
							.filter(rule => rule.enabled)
							.sort((a, b) => b.priority - a.priority)
					}
				} catch (err) {
					console.error('获取提现规则失败', err)
				}
			},
			async submitWithdraw() {
				if (!this.amount || this.amount <= 0) {
					alert('请输入有效的提现金额')
					return
				}

				if (this.amount > this.wallet.availableBalance) {
					alert('提现金额不能超过可用余额')
					return
				}

				// 构建确认信息
				let confirmMsg = `确认提现吗？\n\n`
				confirmMsg += `提现金额：¥${this.amount.toFixed(2)}\n`
				confirmMsg += `手续费（10%）：¥${this.feeAmount.toFixed(2)}\n`
				if (this.rewardDeducted > 0) {
					confirmMsg += `奖励金扣除：¥${this.rewardDeducted.toFixed(2)}\n`
				}
				confirmMsg += `\n实际到账：¥${this.actualAmount.toFixed(2)}`

				if (!confirm(confirmMsg)) {
					return
				}

				this.loading = true
				try {
					const response = await walletApi.withdraw(this.amount)
					if (response.data.success) {
						const result = response.data.data
						// 显示详细的提现结果
						let successMsg = '提现成功！\n\n'
						successMsg += `提现金额：¥${result.withdrawAmount}\n`
						successMsg += `手续费：¥${result.feeAmount}\n`
						if (result.rewardDeducted > 0) {
							successMsg += `奖励金扣除：¥${result.rewardDeducted}\n`
						}
						successMsg += `实际到账：¥${result.actualAmount}`
						alert(successMsg)
						this.loadWallet()
						this.$router.go(-1)
					} else {
						alert('提现失败: ' + response.data.message)
					}
				} catch (err) {
					console.error('提现失败', err)
					const errorMsg = err.response?.data?.message || '提现失败，请重试'
					alert(errorMsg)
				} finally {
					this.loading = false
				}
			}
		}
	}
</script>

<style scoped>
	.withdraw-page {
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
	}

	.wallet-info {
		background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
		padding: 6vw;
		text-align: center;
		color: #fff;
	}

	.wallet-info .label {
		font-size: 3.5vw;
		opacity: 0.9;
		margin-bottom: 2vw;
	}

	.wallet-info .balance {
		font-size: 8vw;
		font-weight: bold;
	}

	.form-section {
		background-color: #fff;
		margin: 4vw;
		padding: 4vw;
		border-radius: 2vw;
	}

	.form-item {
		margin-bottom: 4vw;
	}

	.form-item label {
		display: block;
		font-size: 4vw;
		margin-bottom: 2vw;
		color: #333;
	}

	.required {
		color: #ff6b6b;
	}

	.form-item input {
		width: 100%;
		height: 10vw;
		border: 1px solid #ddd;
		border-radius: 2vw;
		padding: 0 3vw;
		font-size: 4vw;
		box-sizing: border-box;
	}

	.form-item input:focus {
		outline: none;
		border-color: #3190e8;
	}

	.hint {
		display: block;
		font-size: 3vw;
		color: #999;
		margin-top: 1vw;
	}

	.fee-info {
		background-color: #f8f9fa;
		padding: 4vw;
		border-radius: 2vw;
		margin: 4vw 0;
	}

	.fee-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2vw;
		font-size: 4vw;
		color: #333;
	}

	.fee-item:last-child {
		margin-bottom: 0;
	}

	.fee-item.total {
		font-weight: bold;
		color: #000;
	}

	.fee-item.deduct {
		color: #ff6b6b;
		font-size: 3.5vw;
	}

	.fee-item.actual {
		font-weight: bold;
		font-size: 4.5vw;
		padding-top: 2vw;
	}

	.divider {
		height: 1px;
		background-color: #ddd;
		margin: 3vw 0;
	}

	.fee-value {
		font-weight: bold;
	}

	.fee-value.highlight {
		color: #3190e8;
		font-size: 5vw;
	}

	.submit-btn {
		width: 100%;
		height: 10vw;
		background-color: #ff6b6b;
		color: #fff;
		border: none;
		border-radius: 2vw;
		font-size: 4.5vw;
		font-weight: bold;
		cursor: pointer;
		margin-top: 4vw;
	}

	.submit-btn:disabled {
		background-color: #ccc;
		cursor: not-allowed;
	}

	.submit-btn:active:not(:disabled) {
		background-color: #ee5a5a;
	}
</style>

