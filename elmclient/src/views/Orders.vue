<template>
	<div class="wrapper">
		<header>
			<p>确认订单</p>
		</header>
		<div class="order-info">
			<h5>订单配送信息</h5>
			<div class="order-info-address" @click="toUserAddress">
				<p>{{ deliveryaddress != null ? deliveryaddress.address : '请选择送货地址' }}</p>
				<i class="fa fa-angle-right"></i>
			</div>
			<p v-if="deliveryaddress">
				{{ deliveryaddress.contactName }}{{ formatSex(deliveryaddress.contactSex) }}
				{{ deliveryaddress.contactTel }}
			</p>
			<p v-else>
				{{ user.userName }}{{ formatSex(user.userSex) }} {{ user.id }}
			</p>
		</div>

		<h3>{{ business.businessName || '' }}</h3>

		<ul class="order-detailed">
			<li v-for="item in cartArr.filter(i => i?.food)" :key="item?.id || 0">
				<div class="order-detailed-left">
					<img :src="item.food?.foodImg || '../assets/default-food.png'" alt="商品图片">
					<p>{{ item.food?.foodName || '商品' }} x {{ item.quantity || 0 }}</p>
				</div>
				<p>&#165;{{ (item.food?.foodPrice || 0) * (item.quantity || 0) }}</p>
			</li>
		</ul>

		<div class="order-deliveryfee">
			<p>配送费</p>
			<p>&#165;{{ business.deliveryPrice || 0 }}</p>
		</div>
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
import auth from '../utils/auth'

export default {
	name: 'Orders',
	data() {
		return {
			businessId: Number(this.$route.query.businessId),
			business: {},
			user: null,
			cartArr: [],
			deliveryaddress: null,
			foodMap: {}
		}
	},
	async created() {
		this.user = auth.getUserInfo()
		if (!this.user?.id) {
			alert('请先登录')
			this.$router.push('/login')
			return
		}

		this.deliveryaddress = this.$getLocalStorage(this.user.id) || null
		await Promise.all([this.getBusinessDetail(), this.getFoodList()])
		await this.getCart()
	},
	methods: {
		async getBusinessDetail() {
			try {
				const response = await this.$axios.get(`/api/businesses/${this.businessId}`)
				if (response.data.success) {
					this.business = response.data.data || {}
				}
			} catch (error) {
				console.error('Failed to load business detail:', error)
			}
		},

		async getFoodList() {
			try {
				const response = await this.$axios.get(`/api/foods/business/${this.businessId}`)
				if (!response.data.success) {
					this.foodMap = {}
					return
				}

				this.foodMap = (response.data.data || []).reduce((map, food) => {
					map[food.id || food.foodId] = food
					return map
				}, {})
			} catch (error) {
				console.error('Failed to load foods:', error)
				this.foodMap = {}
			}
		},

		async getCart() {
			try {
				const response = await this.$axios.get(`/api/carts/user/${this.user.id}/business/${this.businessId}`)
				if (!response.data.success) {
					this.cartArr = []
					return
				}

				this.cartArr = (response.data.data || []).map(item => ({
					...item,
					food: this.foodMap[item.foodId] || null
				}))
			} catch (error) {
				console.error('Failed to load cart:', error)
				this.cartArr = []
			}
		},

		formatSex(value) {
			return value === 1 ? '先生' : '女士'
		},

		toUserAddress() {
			this.$router.push({
				path: '/userAddress',
				query: {
					businessId: this.businessId
				}
			})
		},

		async toPayment() {
			if (!this.deliveryaddress?.daId) {
				alert('请选择送货地址')
				return
			}

			const token = auth.getToken()
			if (!token) {
				alert('登录状态已失效，请重新登录')
				this.$router.push('/login')
				return
			}

			try {
				const response = await this.$axios.post('/api/orders', {
					userId: this.user.id,
					businessId: this.businessId,
					daId: this.deliveryaddress.daId,
					orderTotal: this.totalPrice
				}, {
					headers: {
						Authorization: `Bearer ${token}`,
						'Content-Type': 'application/json'
					}
				})

				const orderId = response.data.data
				if (!response.data.success || !orderId) {
					alert(response.data.message || '创建订单失败')
					return
				}

				this.$router.push({
					path: '/payment',
					query: {
						orderId
					}
				})
			} catch (error) {
				console.error('Failed to create order:', error)
				alert(error.response?.data?.message || '创建订单失败')
			}
		}
	},
	computed: {
		totalPrice() {
			let total = 0
			for (const cartItem of this.cartArr) {
				total += (cartItem?.food?.foodPrice || 0) * (cartItem?.quantity || 0)
			}
			total += this.business?.deliveryPrice || 0
			return total
		}
	}
}
</script>

<style scoped>
	.wrapper {
		width: 100%;
		height: 100%;
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

	.wrapper .order-info {
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
