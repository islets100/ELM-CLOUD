<template>
	<div class="wrapper">
		<header>
			<p>在线支付</p>
		</header>
		<h3>订单信息</h3>
		<div class="order-info">
			<p>
				{{ orders.business.businessName }}
				<i class="fa fa-caret-down" @click="detailetShow"></i>
			</p>
			<p>&#165;{{ orders.orderTotal }}</p>
		</div>
		<ul class="order-detailet" v-show="isShowDetailet && orders.orderDetails && orders.orderDetails.length > 0">
			<li v-for="item in orders.orderDetails" :key="item.id || item.foodId">
				<p>{{ item.food?.foodName || '商品' }} x {{ item.quantity }}</p>
				<p>&#165;{{ (item.food?.foodPrice || 0) * item.quantity }}</p>
			</li>
			<li>
				<p>配送费</p>
				<p>&#165;{{ orders.business.deliveryPrice || 0 }}</p>
			</li>
		</ul>

		<div class="points-deduction">
			<div class="points-deduction-header">
				<div class="points-info">
					<i class="fa fa-gift"></i>
					<span>积分抵扣</span>
					<span class="available-points">(可用积分: {{ availablePoints }})</span>
				</div>
				<div class="points-switch">
					<label class="switch">
						<input type="checkbox" v-model="usePoints" @change="onPointsToggle">
						<span class="slider"></span>
					</label>
				</div>
			</div>
			<div class="points-input" v-show="usePoints">
				<div class="input-group">
					<input
						type="number"
						v-model.number="pointsToUse"
						:max="maxPointsCanUse"
						:min="0"
						@input="onPointsInput"
						placeholder="请输入要使用的积分">
					<button @click="useMaxPoints" class="max-btn">全部使用</button>
				</div>
				<p class="points-tip">100 积分 = 1 元，最多可抵扣 {{ maxDeductionAmount.toFixed(2) }} 元</p>
				<p class="deduction-amount" v-if="pointsToUse > 0">
					抵扣金额: <span class="amount-highlight">-&#165;{{ currentDeductionAmount.toFixed(2) }}</span>
				</p>
			</div>
		</div>

		<ul class="payment-type">
			<li @click="selectPaymentMethod('wallet')">
				<div class="payment-icon wallet-icon">
					<i class="fa fa-credit-card"></i>
					<span>余额支付</span>
				</div>
				<i class="fa fa-check-circle" v-show="paymentMethod === 'wallet'"></i>
			</li>
			<li @click="selectPaymentMethod('alipay')">
				<img src="../assets/alipay.png">
				<i class="fa fa-check-circle" v-show="paymentMethod === 'alipay'"></i>
			</li>
			<li @click="selectPaymentMethod('wechat')">
				<img src="../assets/wechat.png">
				<i class="fa fa-check-circle" v-show="paymentMethod === 'wechat'"></i>
			</li>
		</ul>

		<div class="payment-summary">
			<div class="summary-row">
				<span>订单金额:</span>
				<span>&#165;{{ orders.orderTotal }}</span>
			</div>
			<div class="summary-row" v-if="usePoints && pointsToUse > 0">
				<span>积分抵扣:</span>
				<span class="deduction">-&#165;{{ currentDeductionAmount.toFixed(2) }}</span>
			</div>
			<div class="summary-row total-row">
				<span>实付金额:</span>
				<span class="final-amount">&#165;{{ finalAmount.toFixed(2) }}</span>
			</div>
		</div>

		<div class="payment-button">
			<button @click="confirmPayment">确认支付 &#165;{{ finalAmount.toFixed(2) }}</button>
		</div>
		<Footer></Footer>
	</div>
</template>

<script>
import Footer from '../components/Footer.vue'
import auth from '../utils/auth'

export default {
	name: 'Payment',
	components: {
		Footer
	},
	data() {
		return {
			orderId: Number(this.$route.query.orderId),
			orders: {
				business: {},
				orderTotal: 0,
				orderDetails: []
			},
			isShowDetailet: false,
			paymentMethod: 'wallet',
			availablePoints: 0,
			usePoints: false,
			pointsToUse: 0
		}
	},
	computed: {
		maxPointsCanUse() {
			const maxByOrder = Math.floor((this.orders.orderTotal || 0) * 100)
			return Math.min(this.availablePoints, maxByOrder)
		},
		maxDeductionAmount() {
			return this.maxPointsCanUse / 100
		},
		currentDeductionAmount() {
			if (!this.usePoints || this.pointsToUse <= 0) {
				return 0
			}
			return this.pointsToUse / 100
		},
		finalAmount() {
			const final = (this.orders.orderTotal || 0) - this.currentDeductionAmount
			return Math.max(final, 0)
		}
	},
	async created() {
		const user = auth.getUserInfo()
		if (!user?.id) {
			alert('请先登录')
			this.$router.push('/login')
			return
		}

		await this.loadOrder()
		await this.loadAvailablePoints()
	},
	mounted() {
		history.pushState(null, null, document.URL)
		window.onpopstate = () => {
			this.$router.push({
				path: '/index'
			})
		}
	},
	unmounted() {
		window.onpopstate = null
	},
	methods: {
		async loadOrder() {
			try {
				const response = await this.$axios.get(`/api/orders/${this.orderId}`)
				if (!response.data.success) {
					return
				}

				this.orders = {
					...response.data.data,
					business: {},
					orderDetails: response.data.data?.orderDetails || []
				}

				await Promise.all([this.loadBusiness(), this.loadOrderFoods()])
			} catch (error) {
				console.error('Failed to load order:', error)
			}
		},

		async loadBusiness() {
			if (!this.orders.businessId) {
				return
			}

			try {
				const response = await this.$axios.get(`/api/businesses/${this.orders.businessId}`)
				if (response.data.success) {
					this.orders.business = response.data.data || {}
				}
			} catch (error) {
				console.error('Failed to load business:', error)
			}
		},

		async loadOrderFoods() {
			const detailList = this.orders.orderDetails || []
			const foodIds = [...new Set(detailList.map(item => item.foodId).filter(Boolean))]
			if (!foodIds.length) {
				return
			}

			try {
				const responses = await Promise.all(foodIds.map(foodId => this.$axios.get(`/api/foods/${foodId}`)))
				const foodMap = {}
				responses.forEach(response => {
					if (response.data.success && response.data.data) {
						foodMap[response.data.data.id] = response.data.data
					}
				})

				this.orders.orderDetails = detailList.map(item => ({
					...item,
					food: foodMap[item.foodId] || null
				}))
			} catch (error) {
				console.error('Failed to load order foods:', error)
			}
		},

		async loadAvailablePoints() {
			const user = auth.getUserInfo()
			try {
				const response = await this.$axios.get(`/api/integral/available/${user.id}`, {
					headers: {
						Authorization: `Bearer ${auth.getToken()}`
					}
				})
				if (response.data.success) {
					this.availablePoints = response.data.data || 0
				}
			} catch (error) {
				console.error('Failed to load available points:', error)
			}
		},

		detailetShow() {
			this.isShowDetailet = !this.isShowDetailet
		},

		selectPaymentMethod(method) {
			this.paymentMethod = method
		},

		onPointsToggle() {
			if (!this.usePoints) {
				this.pointsToUse = 0
			}
		},

		onPointsInput() {
			if (this.pointsToUse < 0) {
				this.pointsToUse = 0
			}
			if (this.pointsToUse > this.maxPointsCanUse) {
				this.pointsToUse = this.maxPointsCanUse
			}
		},

		useMaxPoints() {
			this.pointsToUse = this.maxPointsCanUse
		},

		async confirmPayment() {
			if (this.paymentMethod === 'alipay' || this.paymentMethod === 'wechat') {
				alert('当前只接通了余额支付')
				return
			}

			try {
				const response = await this.$axios.post(`/api/orders/${this.orderId}/pay/virtual-wallet`, null, {
					params: {
						pointAmount: this.usePoints ? this.pointsToUse : 0
					},
					headers: {
						Authorization: `Bearer ${auth.getToken()}`
					}
				})

				if (!response.data.success) {
					alert(response.data.message || '支付失败')
					return
				}

				alert('支付成功')
				this.$router.push({
					path: '/userOrderList'
				})
			} catch (error) {
				console.error('Failed to pay order:', error)
				alert(error.response?.data?.message || '支付失败')
			}
		}
	}
}
</script>

<style scoped>
	.wrapper {
		width: 100%;
		min-height: 100%;
		padding-bottom: 15vw;
		box-sizing: border-box;
	}

	.wrapper header {
		width: 100%;
		height: 12vw;
		background-color: #0097FF;
		color: #fff;
		font-size: 4.8vw;
		position: fixed;
		left: 0;
		top: 0;
		z-index: 1000;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.wrapper h3 {
		margin-top: 12vw;
		box-sizing: border-box;
		padding: 4vw 4vw 0;
		font-size: 4vw;
		font-weight: 300;
		color: #999;
	}

	.wrapper .order-info {
		box-sizing: border-box;
		padding: 4vw;
		font-size: 4vw;
		color: #666;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.wrapper .order-info p:last-child {
		color: orangered;
	}

	.wrapper .order-detailet {
		width: 100%;
	}

	.wrapper .order-detailet li {
		width: 100%;
		box-sizing: border-box;
		padding: 1vw 4vw;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.wrapper .order-detailet li p {
		font-size: 3vw;
		color: #666;
	}

	.wrapper .payment-type {
		width: 100%;
	}

	.wrapper .payment-type li {
		width: 100%;
		box-sizing: border-box;
		padding: 4vw;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.wrapper .payment-type li img {
		width: 33vw;
		height: 8.9vw;
	}

	.wrapper .payment-type li .payment-icon {
		display: flex;
		align-items: center;
		gap: 2vw;
	}

	.wrapper .payment-type li .wallet-icon {
		background: #fff;
		color: #333;
		padding: 2vw 4vw;
		border-radius: 1.5vw;
		width: 33vw;
		height: 8.9vw;
		box-sizing: border-box;
		justify-content: center;
		border: 1px solid #e0e0e0;
	}

	.wrapper .payment-type li .wallet-icon i {
		font-size: 5vw;
		color: #0097FF;
	}

	.wrapper .payment-type li .wallet-icon span {
		font-size: 4vw;
		font-weight: bold;
	}

	.wrapper .payment-type li .fa-check-circle {
		font-size: 5vw;
		color: #38CA73;
	}

	.wrapper .payment-button {
		width: 100%;
		box-sizing: border-box;
		padding: 4vw;
	}

	.wrapper .payment-button button {
		width: 100%;
		height: 10vw;
		border: none;
		outline: none;
		border-radius: 4px;
		background-color: #38CA73;
		color: #fff;
		font-size: 4vw;
	}

	.wrapper .points-deduction {
		width: 100%;
		background-color: #fff;
		box-sizing: border-box;
		padding: 4vw;
		margin-bottom: 2vw;
		border-top: 1px solid #f0f0f0;
		border-bottom: 1px solid #f0f0f0;
	}

	.wrapper .points-deduction-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2vw;
	}

	.wrapper .points-info {
		display: flex;
		align-items: center;
		gap: 2vw;
		font-size: 4vw;
		color: #333;
	}

	.wrapper .points-info i {
		color: #ff6b6b;
		font-size: 5vw;
	}

	.wrapper .available-points {
		font-size: 3vw;
		color: #999;
	}

	.switch {
		position: relative;
		display: inline-block;
		width: 12vw;
		height: 6.5vw;
	}

	.switch input {
		opacity: 0;
		width: 0;
		height: 0;
	}

	.slider {
		position: absolute;
		cursor: pointer;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: #ccc;
		transition: 0.4s;
		border-radius: 6.5vw;
	}

	.slider:before {
		position: absolute;
		content: "";
		height: 5vw;
		width: 5vw;
		left: 0.75vw;
		bottom: 0.75vw;
		background-color: white;
		transition: 0.4s;
		border-radius: 50%;
	}

	input:checked + .slider {
		background-color: #38CA73;
	}

	input:checked + .slider:before {
		transform: translateX(5.5vw);
	}

	.wrapper .points-input {
		margin-top: 3vw;
		padding-top: 3vw;
		border-top: 1px dashed #e0e0e0;
	}

	.wrapper .input-group {
		display: flex;
		gap: 2vw;
		margin-bottom: 2vw;
	}

	.wrapper .input-group input {
		flex: 1;
		height: 10vw;
		border: 1px solid #ddd;
		border-radius: 4px;
		padding: 0 3vw;
		font-size: 4vw;
		box-sizing: border-box;
	}

	.wrapper .input-group input:focus {
		outline: none;
		border-color: #0097FF;
	}

	.wrapper .max-btn {
		height: 10vw;
		padding: 0 4vw;
		background-color: #0097FF;
		color: #fff;
		border: none;
		border-radius: 4px;
		font-size: 3.5vw;
		cursor: pointer;
		white-space: nowrap;
	}

	.wrapper .points-tip {
		font-size: 3vw;
		color: #999;
		margin-bottom: 2vw;
	}

	.wrapper .deduction-amount {
		font-size: 3.5vw;
		color: #333;
		font-weight: bold;
	}

	.wrapper .amount-highlight {
		color: #ff6b6b;
		font-size: 4vw;
	}

	.wrapper .payment-summary {
		width: 100%;
		background-color: #fff;
		box-sizing: border-box;
		padding: 4vw;
		margin-bottom: 2vw;
		border-top: 1px solid #f0f0f0;
	}

	.wrapper .summary-row {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 2vw 0;
		font-size: 3.5vw;
		color: #666;
	}

	.wrapper .summary-row.total-row {
		border-top: 1px dashed #e0e0e0;
		margin-top: 2vw;
		padding-top: 3vw;
		font-size: 4vw;
		font-weight: bold;
		color: #333;
	}

	.wrapper .deduction {
		color: #ff6b6b;
	}

	.wrapper .final-amount {
		color: #ff6b6b;
		font-size: 5vw;
	}
</style>
