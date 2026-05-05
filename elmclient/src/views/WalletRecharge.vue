<template>
	<div class="recharge-page">
		<nav class="top-nav">
			<button @click="$router.go(-1)" class="back-btn">← 返回</button>
			<h1>充值</h1>
		</nav>

		<div class="content">
			<div class="wallet-info">
				<p class="label">当前余额</p>
				<p class="balance">¥{{ wallet.balance || '0.00' }}</p>
			</div>

			<div class="form-section">
				<div class="form-item">
					<label>充值金额 <span class="required">*</span></label>
					<input type="number" v-model.number="amount" placeholder="请输入充值金额" min="0.01" step="0.01" />
					<span class="hint">最低充值0.01元</span>
				</div>

				<div class="tip-section" v-if="rewardRules.length > 0">
					<div class="tip" v-for="rule in applicableRules" :key="rule.id">
						<span class="tip-icon">🎁</span>
						<span>充值满{{ rule.thresholdAmount }}元，赠送{{ rule.rewardAmount }}元！</span>
					</div>
				</div>

				<button class="submit-btn" @click="submitRecharge" :disabled="loading || !amount || amount <= 0">
					<span v-if="loading">充值中...</span>
					<span v-else>确认充值</span>
				</button>
			</div>
		</div>
	</div>
</template>

<script>
	import walletApi from '../api/wallet.js'
	import axios from 'axios'

	export default {
		name: 'Recharge',
		data() {
			return {
				wallet: {
					balance: 0
				},
				amount: null,
				loading: false,
				rewardRules: [] // 充值奖励规则列表
			}
		},
		computed: {
			// 计算当前金额可以获得的奖励规则
			applicableRules() {
				if (!this.amount) return []
				return this.rewardRules.filter(rule => 
					rule.enabled && this.amount >= rule.thresholdAmount
				).sort((a, b) => b.thresholdAmount - a.thresholdAmount) // 按满额金额降序排列
			}
		},
		created() {
			this.loadWallet()
			this.loadRewardRules()
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
			async loadRewardRules() {
				try {
					const response = await axios.get('/api/reward-rules/type/RECHARGE')
					if (response.data.success) {
						this.rewardRules = response.data.data.filter(rule => rule.enabled)
					}
				} catch (err) {
					console.error('获取奖励规则失败', err)
				}
			},
			async submitRecharge() {
				if (!this.amount || this.amount <= 0) {
					alert('请输入有效的充值金额')
					return
				}

				if (!confirm(`确认充值 ¥${this.amount} 吗？`)) {
					return
				}

				this.loading = true
				try {
					const response = await walletApi.recharge(this.amount)
					if (response.data.success) {
						alert('充值成功！')
						this.loadWallet()
						this.$router.go(-1)
					} else {
						alert('充值失败: ' + response.data.message)
					}
				} catch (err) {
					console.error('充值失败', err)
					const errorMsg = err.response?.data?.message || '充值失败，请重试'
					alert(errorMsg)
				} finally {
					this.loading = false
				}
			}
		}
	}
</script>

<style scoped>
	.recharge-page {
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
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

	.tip-section {
		margin: 4vw 0;
	}

	.tip {
		display: flex;
		align-items: center;
		gap: 2vw;
		padding: 3vw;
		background-color: #fff3cd;
		border-radius: 2vw;
		font-size: 3.5vw;
		color: #856404;
	}

	.tip-icon {
		font-size: 5vw;
	}

	.submit-btn {
		width: 100%;
		height: 10vw;
		background-color: #3190e8;
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
		background-color: #2a7bc7;
	}
</style>

