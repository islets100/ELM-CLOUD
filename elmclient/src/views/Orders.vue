<template>
	<div class="wrapper">
		<!-- header部分 -->
		<header>
			<p>确认订单</p>
		</header>
		<!-- 订单信息部分 -->
		<div class="order-info">
			<h5>订单配送至：</h5>
			<div class="order-info-address" @click="toUserAddress">
				<p>{{ deliveryaddress != null ? deliveryaddress.address : '请选择送货地址' }}</p>
				<i class="fa fa-angle-right"></i>
			</div>
			<!-- 显示收货人信息：姓名、性别、电话 -->
			<p v-if="deliveryaddress">
				{{ deliveryaddress.contactName }}{{ formatSex(deliveryaddress.contactSex) }}
				{{ deliveryaddress.contactTel }}
			</p>
			<p v-else>
				{{ user.userName }}{{ formatSex(user.userSex) }} {{ user.id }}
			</p>
		</div>

		<h3>{{ business.businessName || '' }}</h3>

		<!-- 订单明细部分 -->
		<ul class="order-detailed">
			<li v-for="item in cartArr.filter(i => i?.food)" :key="item?.id || 0">
				<div class="order-detailed-left">
					<!-- 修改后代码 -->
					<img 
					:src="item.food?.foodImg || '../assets/default-food.png'" 
					alt="商品图片"
					>
					<p>{{ item.food?.foodName || '商品信息不存在' }} x {{ item.quantity || 0 }}</p>
				</div>
				<p>&#165;{{ (item.food?.foodPrice || 0) * (item.quantity || 0) }}</p>
			</li>
		</ul>

		<div class="order-deliveryfee">
			<p>配送费</p>
			<p>&#165;{{ business.deliveryPrice || 0 }}</p>
		</div>
		<!-- 合计部分 -->
		<div class="total">
			<div class="total-left">
				&#165;{{ totalPrice }}
			</div>
			<div class="total-right" @click="toPayment">
				去支付
			</div>
		</div>
	</div>
</template>

<script>
	import auth from '../utils/auth';

	export default {
		name: 'Orders',
		data() {
			return {
				businessId: this.$route.query.businessId,
				business: {},
				user: null, // 一定用 null 初始化
				cartArr: [],
				deliveryaddress: null
			}
		},
		created() {
			// 获取用户信息
			this.user = auth.getUserInfo();
			console.log('当前用户 =', this.user);

			if (!this.user) {
				alert('请先登录');
				this.$router.push('/login');
				return;
			}

			// 尝试获取本地存储的送货地址
			this.deliveryaddress = this.$getLocalStorage(this.user.id) || null;

			// 获取商家信息
			this.getBusinessDetail();

			// 获取购物车信息
			this.getCart();
		},
		methods: {
			getBusinessDetail() {
				this.$axios.get('/api/businesses/getBusinessById', {
					params: {
						businessId: this.businessId
					}
				}).then(res => {
					console.log('商家信息返回：', res.data);
					if (res.data.success) {
						this.business = res.data.data;
					}
				}).catch(err => console.error(err));
			},

			getCart() {
				this.$axios.get('/api/carts', {
					params: {
						userId: this.user.id,
						businessId: this.businessId
					}
				}).then(res => {
					console.log('购物车返回：', res.data);
					let arr = Array.isArray(res.data) ? res.data : (Array.isArray(res.data.data) ? res.data.data :
						[]);
					this.cartArr = arr.filter(c => c?.food);
				}).catch(err => console.error(err));
			},

			formatSex(value) {
				return value === 1 ? '先生' : '女士';
			},

			toUserAddress() {
				this.$router.push({
					path: '/userAddress',
					query: {
						businessId: this.businessId
					}
				});
			},

			toPayment() {
				if (!this.deliveryaddress) {
					alert('请选择送货地址！');
					return;
				}

				const token = auth.getToken();
				console.log('当前 token =', token);
				if (!token) {
					alert('登录状态失效，请重新登录');
					this.$router.push('/login');
					return;
				}

				const orderParams = {
					business: {
						id: Number(this.businessId)
					},
					deliveryAddress: {
						id: Number(this.deliveryaddress.id)
					}
				};

				console.log('准备创建订单 → 请求参数:', orderParams);

				this.$axios.post('/api/orders', orderParams, {
					headers: {
						'Authorization': `Bearer ${token}`,
						'Content-Type': 'application/json'
					}
				}).then(res => {
					console.log('创建订单返回数据:', res.data);
					const order = res.data.data;
					if (order && order.id) {
						console.log('订单创建成功 → 订单ID:', order.id);
						this.$router.push({
							path: '/payment',
							query: {
								orderId: order.id
							}
						});
					} else {
						console.warn('创建订单失败 → 返回数据不完整:', res.data);
						alert('创建订单失败！');
					}
				}).catch(error => {
					console.error('创建订单请求出错:', error);
					let msg = '创建订单失败，请检查控制台信息';
					if (error.response?.data?.message) {
						msg = `创建订单失败: ${error.response.data.message}`;
					}
					alert(msg);
				});
			}
		},
		computed: {
			totalPrice() {
				let total = 0;
				for (let cartItem of this.cartArr) {
					total += (cartItem?.food?.foodPrice || 0) * (cartItem?.quantity || 0);
				}
				total += this.business?.deliveryPrice || 0;
				return total;
			}
		}
	}
</script>


<style scoped>
	/****************** 总容器 ******************/
	.wrapper {
		width: 100%;
		height: 100%;
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
	.wrapper .order-info {
		/* 注意这里，不设置高，靠内容撑开。因为地址有可能折行 */
		width: 100%;
		margin-top: 12vw;
		background-color: #0097EF;
		box-sizing: border-box;
		padding: 2vw;
		color: #fff;
	}

	.wrapper .order-info h5 {
		font-size: 3vw;
		font-weight: 300;
	}

	.wrapper .order-info .order-info-address {
		width: 100%;
		display: flex;
		justify-content: space-between;
		align-items: center;
		font-weight: 700;
		user-select: none;
		cursor: pointer;
		margin: 1vw 0;
	}

	.wrapper .order-info .order-info-address p {
		width: 90%;
		font-size: 5vw;
	}

	.wrapper .order-info .order-info-address i {
		font-size: 6vw;
	}

	.wrapper .order-info p {
		font-size: 3vw;
	}

	.wrapper h3 {
		box-sizing: border-box;
		padding: 3vw;
		font-size: 4vw;
		color: #666;
		border-bottom: solid 1px #DDD;
	}

	/****************** 订单明细部分 ******************/
	.wrapper .order-detailed {
		width: 100%;
	}

	.wrapper .order-detailed li {
		width: 100%;
		height: 16vw;
		box-sizing: border-box;
		padding: 3vw;
		color: #666;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.wrapper .order-detailed li .order-detailed-left {
		display: flex;
		align-items: center;
	}

	.wrapper .order-detailed li .order-detailed-left img {
		width: 10vw;
		height: 10vw;
	}

	.wrapper .order-detailed li .order-detailed-left p {
		font-size: 3.5vw;
		margin-left: 3vw;
	}

	.wrapper .order-detailed li p {
		font-size: 3.5vw;
	}

	.wrapper .order-deliveryfee {
		width: 100%;
		height: 16vw;
		box-sizing: border-box;
		padding: 3vw;
		color: #666;
		display: flex;
		justify-content: space-between;
		align-items: center;
		font-size: 3.5vw;
	}

	/****************** 订单合计部分 ******************/
	.wrapper .total {
		width: 100%;
		height: 14vw;
		position: fixed;
		left: 0;
		bottom: 0;
		display: flex;
	}

	.wrapper .total .total-left {
		flex: 2;
		background-color: #505051;
		color: #fff;
		font-size: 4.5vw;
		font-weight: 700;
		user-select: none;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.wrapper .total .total-right {
		flex: 1;
		background-color: #38CA73;
		color: #fff;
		font-size: 4.5vw;
		font-weight: 700;
		user-select: none;
		cursor: pointer;
		display: flex;
		justify-content: center;
		align-items: center;
	}
</style>