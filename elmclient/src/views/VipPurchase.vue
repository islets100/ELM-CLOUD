<template>
	<div class="vip-purchase wrapper">
		<header>
			<i class="fa fa-arrow-left back-btn" @click="$router.back()"></i>
			<span>购买VIP</span>
		</header>

		<div class="content">
			<!-- VIP卡选择 -->
			<div class="card-list">
				<div class="card-item" 
					v-for="card in vipCards" 
					:key="card.type"
					:class="{ 'selected': selectedCard === card.type }"
					@click="selectCard(card.type)">
					<div class="card-badge" :class="card.type.toLowerCase()">
						<i class="fa fa-crown"></i>
					</div>
					<h3>{{ card.name }}</h3>
					<div class="card-price">
						<span class="currency">¥</span>
						<span class="amount">{{ card.price }}</span>
					</div>
					<div class="card-duration">{{ card.duration }}</div>
					<ul class="card-features">
						<li v-for="(feature, index) in card.features" :key="index">
							<i class="fa fa-check"></i>
							<span>{{ feature }}</span>
						</li>
					</ul>
					<div class="select-indicator" v-if="selectedCard === card.type">
						<i class="fa fa-check-circle"></i>
					</div>
				</div>
			</div>

			<!-- 钱包余额提示 -->
			<div class="balance-info">
				<div class="balance-row">
					<span>钱包余额</span>
					<span class="balance-amount">¥{{ walletBalance }}</span>
				</div>
				<div class="balance-tip" v-if="!hasEnoughBalance">
					<i class="fa fa-exclamation-circle"></i>
					余额不足，请先充值
				</div>
			</div>

			<!-- 购买按钮 -->
			<div class="action-section">
				<button class="purchase-btn" 
					:disabled="!selectedCard || !hasEnoughBalance || loading"
					@click="confirmPurchase">
					{{ loading ? '购买中...' : '立即购买' }}
				</button>
				<button class="recharge-btn" v-if="!hasEnoughBalance" @click="$router.push('/wallet/recharge')">
					去充值
				</button>
			</div>
		</div>
	</div>
</template>

<script>
	import vipApi from '../api/vip.js'
	import walletApi from '../api/wallet.js'

	export default {
		name: 'VipPurchase',
		data() {
			return {
				vipCards: [
				{
					type: 'BASIC',
					name: '基础版',
					price: 99,
					duration: '3个月',
					creditLimit: 500,
					bonusIntegral: 100,
					features: [
						'透支额度 ¥500',
						'赠送 100 积分',
						'专属客服',
						'优先配送'
					]
				},
				{
					type: 'PREMIUM',
					name: '高级版',
					price: 299,
					duration: '6个月',
					creditLimit: 2000,
					bonusIntegral: 300,
					features: [
						'透支额度 ¥2000',
						'赠送 300 积分',
						'专属客服',
						'优先配送',
						'专属优惠券',
						'免配送费'
					]
				},
				{
					type: 'PLATINUM',
					name: '白金版',
					price: 999,
					duration: '12个月',
					creditLimit: 5000,
					bonusIntegral: 1000,
					features: [
						'透支额度 ¥5000',
						'赠送 1000 积分',
						'专属客服',
						'优先配送',
						'专属优惠券',
						'免配送费',
						'生日特权',
						'积分翻倍'
					]
				}
			],
				selectedCard: null,
				walletBalance: 0,
				loading: false
			}
		},
		computed: {
			selectedCardPrice() {
				const card = this.vipCards.find(c => c.type === this.selectedCard)
				return card ? card.price : 0
			},
			hasEnoughBalance() {
				return this.walletBalance >= this.selectedCardPrice
			}
		},
		created() {
			this.loadWalletBalance()
		},
		methods: {
			async loadWalletBalance() {
				try {
					const response = await walletApi.getWallet()
					if (response.data.success) {
						this.walletBalance = response.data.data.availableBalance || 0
					}
				} catch (err) {
					console.error('获取钱包余额失败', err)
				}
			},
			selectCard(type) {
				this.selectedCard = type
			},
			async confirmPurchase() {
				if (!this.selectedCard) {
					alert('请选择VIP卡类型')
					return
				}

				if (!this.hasEnoughBalance) {
					alert('余额不足，请先充值')
					return
				}

				const card = this.vipCards.find(c => c.type === this.selectedCard)
				if (!confirm(`确认购买${card.name}VIP（¥${card.price}）吗？`)) {
					return
				}

				this.loading = true
				try {
					const response = await vipApi.purchaseVipCard(this.selectedCard)
					if (response.data.success) {
						alert('购买成功！')
						this.$router.push('/vip/center')
					} else {
						alert('购买失败: ' + response.data.message)
					}
				} catch (err) {
					console.error('购买失败', err)
					alert('购买失败，请重试')
				} finally {
					this.loading = false
				}
			}
		}
	}
</script>

<style scoped>
	.vip-purchase {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
		background-color: #f5f5f5;
	}

	.vip-purchase>header {
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

	.card-list {
		padding: 4vw;
		display: flex;
		flex-direction: column;
		gap: 4vw;
	}

	.card-item {
		background: #fff;
		border-radius: 3vw;
		padding: 5vw;
		box-shadow: 0 2vw 4vw rgba(0, 0, 0, 0.1);
		cursor: pointer;
		transition: all 0.3s;
		position: relative;
		border: 2px solid transparent;
	}

	.card-item.selected {
		border-color: #667eea;
		box-shadow: 0 2vw 6vw rgba(102, 126, 234, 0.3);
	}

	.card-badge {
		text-align: center;
		margin-bottom: 3vw;
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

	.card-item h3 {
		text-align: center;
		font-size: 5vw;
		margin-bottom: 2vw;
		color: #333;
	}

	.card-price {
		text-align: center;
		margin-bottom: 2vw;
	}

	.card-price .currency {
		font-size: 4vw;
		color: #ff6b6b;
	}

	.card-price .amount {
		font-size: 8vw;
		font-weight: bold;
		color: #ff6b6b;
	}

	.card-duration {
		text-align: center;
		font-size: 3.5vw;
		color: #999;
		margin-bottom: 3vw;
	}

	.card-features {
		list-style: none;
		padding: 0;
		margin: 0;
	}

	.card-features li {
		display: flex;
		align-items: center;
		margin-bottom: 2vw;
		font-size: 3.5vw;
		color: #666;
	}

	.card-features li:last-child {
		margin-bottom: 0;
	}

	.card-features li i {
		color: #667eea;
		margin-right: 2vw;
		font-size: 4vw;
	}

	.select-indicator {
		position: absolute;
		top: 3vw;
		right: 3vw;
		color: #667eea;
		font-size: 6vw;
	}

	.balance-info {
		background-color: #fff;
		margin: 0 4vw 4vw;
		padding: 4vw;
		border-radius: 2vw;
		box-shadow: 0 1vw 2vw rgba(0, 0, 0, 0.05);
	}

	.balance-row {
		display: flex;
		justify-content: space-between;
		align-items: center;
		font-size: 4vw;
	}

	.balance-amount {
		font-size: 5vw;
		font-weight: bold;
		color: #667eea;
	}

	.balance-tip {
		margin-top: 2vw;
		padding: 2vw;
		background-color: #fff3cd;
		border-radius: 1vw;
		font-size: 3.5vw;
		color: #856404;
		display: flex;
		align-items: center;
		gap: 2vw;
	}

	.action-section {
		padding: 0 4vw;
		display: flex;
		flex-direction: column;
		gap: 3vw;
	}

	.purchase-btn,
	.recharge-btn {
		width: 100%;
		height: 12vw;
		border: none;
		border-radius: 2vw;
		font-size: 4.5vw;
		font-weight: bold;
		cursor: pointer;
	}

	.purchase-btn {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #fff;
	}

	.purchase-btn:disabled {
		background: #ccc;
		cursor: not-allowed;
	}

	.recharge-btn {
		background-color: #3190e8;
		color: #fff;
	}
</style>
