<template>
	<div class="wrapper">
		<!-- header部分 -->
		<header>
			<p>在线支付</p>
		</header>
		<!-- 订单信息部分 -->
		<h3>订单信息：</h3>
		<div class="order-info">
			<p>
				{{orders.business.businessName}}
				<i class="fa fa-caret-down" @click="detailetShow"></i>
			</p>
			<p>&#165;{{orders.orderTotal}}</p>
		</div>
		<!-- 订单明细部分 -->
		<ul class="order-detailet" v-show="isShowDetailet && orders.orderDetails && orders.orderDetails.length > 0">
			<li v-for="item in orders.orderDetails" :key="item.food?.foodName">
				<p>{{ item.food?.foodName }} x {{ item.quantity }}</p>
				<p>&#165;{{ item.food?.foodPrice * item.quantity }}</p>
			</li>
			<!-- 配送费 -->
			<li>
				<p>配送费</p>
				<p>&#165;{{ orders.business.deliveryPrice }}</p>
			</li>
		</ul>

		<!-- 当没有订单明细时显示提示 -->
		<ul v-show="isShowDetailet && (!orders.orderDetails || orders.orderDetails.length === 0)">
			<li>
				<p>没有订单明细</p>
			</li>
		</ul>

		<!-- 积分抵扣部分 -->
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
						placeholder="请输入要使用的积分"
					>
					<button @click="useMaxPoints" class="max-btn">全部使用</button>
				</div>
				<p class="points-tip">100积分 = 1元，最多可抵扣 {{ maxDeductionAmount.toFixed(2) }} 元</p>
				<p class="deduction-amount" v-if="pointsToUse > 0">
					抵扣金额: <span class="amount-highlight">-&#165;{{ currentDeductionAmount.toFixed(2) }}</span>
				</p>
			</div>
		</div>

		<!-- 支付方式部分 -->
		<ul class="payment-type">
			<li @click="selectPaymentMethod('wallet')">
				<div class="payment-icon wallet-icon">
					<i class="fa fa-credit-card"></i>
					<span>钱包支付</span>
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

		<!-- 支付金额汇总 -->
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
		<!-- 底部菜单部分 -->
		<Footer></Footer>
	</div>
</template>

<script>
	import Footer from '../components/Footer.vue';
	import auth from '../utils/auth';

	export default {
		name: 'Payment',
		data() {
			return {
				orderId: this.$route.query.orderId,
				orders: {
					business: {},
					orderTotal: 0
				},
				isShowDetailet: false,
				paymentMethod: 'wallet', // 默认选择钱包支付
				// 积分相关
				availablePoints: 0, // 可用积分
				usePoints: false, // 是否使用积分
				pointsToUse: 0 // 要使用的积分数量
			}
		},
		computed: {
			// 最多可以使用的积分（不能超过订单金额的100倍，也不能超过可用积分）
			maxPointsCanUse() {
				const maxByOrder = Math.floor(this.orders.orderTotal * 100);
				return Math.min(this.availablePoints, maxByOrder);
			},
			// 最大可抵扣金额
			maxDeductionAmount() {
				return this.maxPointsCanUse / 100;
			},
			// 当前抵扣金额
			currentDeductionAmount() {
				if (!this.usePoints || this.pointsToUse <= 0) {
					return 0;
				}
				return this.pointsToUse / 100;
			},
			// 最终支付金额
			finalAmount() {
				const deduction = this.usePoints ? this.currentDeductionAmount : 0;
				const final = this.orders.orderTotal - deduction;
				return Math.max(final, 0); // 确保不为负数
			}
		},
		created() {
			// 使用 GET 请求获取订单详情
			this.$axios.get(`/api/orders/${this.orderId}`)
				.then(response => {
					this.orders = response.data.data; // 注意 HttpResult 格式
					console.log('订单详情：', this.orders);
					console.log('订单明细:', this.orders.orderDetails);
				})
				.catch(error => {
					console.error(error);
				});

			// 获取用户可用积分
			this.loadAvailablePoints();
		},
		mounted() {
			//这里的代码实现：一旦路由到在线支付组件，就不能回到订单确认组件。
			//先将当前路由添加到history对象中
			history.pushState(null, null, document.URL);
			//popstate事件能够监听history对象的变化
			window.onpopstate = () => {
				this.$router.push({
					path: '/index'
				});
			}
		},
		destroyed() {
			window.onpopstate = null;
		},
		methods: {
			// 加载可用积分
			loadAvailablePoints() {
				const token = auth.getToken();
				this.$axios.get('/api/integral/available', {
					headers: {
						'Authorization': `Bearer ${token}`
					}
				})
				.then(response => {
					if (response.data.success) {
						this.availablePoints = response.data.data || 0;
						console.log('可用积分：', this.availablePoints);
					}
				})
				.catch(error => {
					console.error('获取积分失败:', error);
				});
			},
			detailetShow() {
				this.isShowDetailet = !this.isShowDetailet;
			},
			selectPaymentMethod(method) {
				this.paymentMethod = method;
			},
			// 积分开关切换
			onPointsToggle() {
				if (!this.usePoints) {
					this.pointsToUse = 0;
				}
			},
			// 积分输入变化
			onPointsInput() {
				// 限制输入范围
				if (this.pointsToUse < 0) {
					this.pointsToUse = 0;
				}
				if (this.pointsToUse > this.maxPointsCanUse) {
					this.pointsToUse = this.maxPointsCanUse;
				}
			},
			// 使用最大积分
			useMaxPoints() {
				this.pointsToUse = this.maxPointsCanUse;
			},
			async confirmPayment() {
				// 如果选择第三方支付，提示用户
				if (this.paymentMethod === 'alipay' || this.paymentMethod === 'wechat') {
					const paymentName = this.paymentMethod === 'alipay' ? '支付宝' : '微信';
					alert(`${paymentName}支付功能暂未接入，请使用钱包支付`);
					return;
				}

				try {
					const token = auth.getToken();

					// 如果使用积分，先进行积分抵扣
					if (this.usePoints && this.pointsToUse > 0) {
						console.log('使用积分抵扣:', this.pointsToUse);
						
						// 调用积分抵扣接口
						const deductResponse = await this.$axios.post('/api/integral/deduct-order', null, {
							params: {
								orderId: this.orderId,
								orderAmount: this.orders.orderTotal,
								integralAmount: this.pointsToUse
							},
							headers: {
								'Authorization': `Bearer ${token}`
							}
						});

						if (!deductResponse.data.success) {
							alert('积分抵扣失败: ' + (deductResponse.data.message || '未知错误'));
							return;
						}

						console.log('积分抵扣成功');
					}

					// 钱包支付（传递实际支付金额）
					const payParams = {
						orderId: this.orderId,
						paymentMethod: this.paymentMethod
					};
					
					// 如果使用了积分抵扣，传递实际支付金额
					if (this.usePoints && this.pointsToUse > 0) {
						payParams.actualAmount = this.finalAmount;
					}
					
					const payResponse = await this.$axios.post('/api/orders/payOk', null, {
						params: payParams,
						headers: {
							'Authorization': `Bearer ${token}`
						}
					});

					if (payResponse.data.success) {
						const message = this.usePoints && this.pointsToUse > 0 
							? `支付成功！使用了 ${this.pointsToUse} 积分，抵扣 ¥${this.currentDeductionAmount.toFixed(2)}` 
							: '支付成功！';
						alert(message);
						this.$router.push({
							path: '/userOrderList'
						});
					} else {
						alert(payResponse.data.message || '支付失败，请重试');
					}
				} catch (error) {
					console.error('支付出错:', error);
					alert('支付出错: ' + (error.response?.data?.message || error.message || '请稍后再试'));
				}
			}
		},
		components: {
			Footer
		}
	}
</script>

<style scoped>
	/****************** 总容器 ******************/
	.wrapper {
		width: 100%;
		min-height: 100%;
		padding-bottom: 15vw;
		box-sizing: border-box;
	}

	/****************** header部分 ******************/
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

	/****************** 订单信息部分 ******************/
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

	/****************** 订单明细部分 ******************/
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

	/****************** 支付方式部分 ******************/
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

	/****************** 积分抵扣部分 ******************/
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

	/* 开关样式 */
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

	/* 积分输入部分 */
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

	/****************** 支付金额汇总 ******************/
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