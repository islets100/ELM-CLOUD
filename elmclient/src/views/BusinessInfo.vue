<template>
	<div class="wrapper">
		<header>
			<p>商家详情</p>
		</header>
		<div class="business-logo">
			<img :src="business.businessImg || '../assets/sj01.png'" alt="商家图片">
		</div>
		<div class="business-info">
			<h1>{{ business.businessName }}</h1>
			<p>&#165;{{ business.startPrice }}起送 &#165;{{ business.deliveryPrice }}配送</p>
			<p>{{ business.businessExplain }}</p>
		</div>
		<ul class="food">
			<li v-if="foodArr.length === 0" class="empty-food">
				<i class="fa fa-cutlery"></i>
				<span>暂无商品</span>
			</li>
			<li v-for="(item, index) in foodArr" :key="item.id">
				<div class="food-left">
					<img :src="item.foodImg || '../assets/default-food.png'" alt="商品图片">
					<div class="food-left-info">
						<h3>{{ item.foodName }}</h3>
						<p>{{ item.foodExplain }}</p>
						<p>&#165;{{ item.foodPrice }}</p>
					</div>
				</div>
				<div class="food-right">
					<div>
						<i class="fa fa-minus-circle" @click="minus(index)" v-show="item.quantity !== 0"></i>
					</div>
					<p><span v-show="item.quantity !== 0">{{ item.quantity }}</span></p>
					<div>
						<i class="fa fa-plus-circle" @click="add(index)"></i>
					</div>
				</div>
			</li>
		</ul>
		<div class="cart">
			<div class="cart-left">
				<div class="cart-left-icon" :style="totalQuantity === 0 ? 'background-color:#505051;' : 'background-color:#3190E8;'">
					<i class="fa fa-shopping-cart"></i>
					<div class="cart-left-icon-quantity" v-show="totalQuantity !== 0">{{ totalQuantity }}</div>
				</div>
				<div class="cart-left-info">
					<p>&#165;{{ totalPrice }}</p>
					<p>另需配送费{{ business.deliveryPrice }}元</p>
				</div>
			</div>
			<div class="cart-right">
				<div class="cart-right-item" v-show="totalSettle < business.startPrice" style="background-color:#535356;cursor: default;">
					&#165;{{ business.startPrice }}起送
				</div>
				<div class="cart-right-item" @click="toOrder" v-show="totalSettle >= business.startPrice">
					去结算
				</div>
			</div>
		</div>
	</div>
</template>

<script>
import auth from '../utils/auth'

export default {
	name: 'BusinessInfo',
	data() {
		return {
			businessId: Number(this.$route.query.businessId),
			business: {},
			foodArr: [],
			user: null
		}
	},
	created() {
		this.initData()
	},
	activated() {
		this.initData()
	},
	methods: {
		async initData() {
			this.businessId = Number(this.$route.query.businessId)
			this.user = auth.getUserInfo()

			await Promise.all([this.getBusinessDetail(), this.getFoodList()])
			if (this.user?.id) {
				await this.loadCart()
			}
		},

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
					this.foodArr = []
					return
				}

				this.foodArr = (response.data.data || []).map(food => ({
					...food,
					quantity: 0,
					foodImg: food.foodImg || 'default.jpg'
				}))
			} catch (error) {
				console.error('Failed to load foods:', error)
				this.foodArr = []
			}
		},

		async loadCart() {
			try {
				const response = await this.$axios.get(`/api/carts/user/${this.user.id}/business/${this.businessId}`)
				if (!response.data.success) {
					return
				}

				const quantityMap = {}
				;(response.data.data || []).forEach(cartItem => {
					quantityMap[cartItem.foodId] = cartItem.quantity
				})

				this.foodArr = this.foodArr.map(item => ({
					...item,
					quantity: quantityMap[item.id] || 0
				}))
			} catch (error) {
				console.error('Failed to load cart:', error)
			}
		},

		add(index) {
			if (!this.user?.id) {
				alert('请先登录')
				this.$router.push('/login')
				return
			}

			this.foodArr[index].quantity += 1
			this.saveCart(index)
		},

		minus(index) {
			if (!this.user?.id) {
				alert('请先登录')
				this.$router.push('/login')
				return
			}

			if (this.foodArr[index].quantity <= 0) {
				return
			}

			this.foodArr[index].quantity -= 1
			if (this.foodArr[index].quantity === 0) {
				this.removeCart(index)
				return
			}
			this.updateCart(index, this.foodArr[index].quantity)
		},

		async saveCart(index) {
			try {
				const response = await this.$axios.post('/api/carts', {
					businessId: this.businessId,
					userId: this.user.id,
					foodId: this.foodArr[index].id,
					quantity: 1
				})
				if (!response.data.success) {
					this.foodArr[index].quantity -= 1
				}
			} catch (error) {
				console.error('Failed to save cart item:', error)
				this.foodArr[index].quantity -= 1
			}
		},

		async updateCart(index, quantity) {
			try {
				const response = await this.$axios.put(
					`/api/carts/user/${this.user.id}/business/${this.businessId}/food/${this.foodArr[index].id}`,
					{ quantity }
				)
				if (!response.data.success) {
					this.foodArr[index].quantity += 1
				}
			} catch (error) {
				console.error('Failed to update cart item:', error)
				this.foodArr[index].quantity += 1
			}
		},

		async removeCart(index) {
			try {
				const response = await this.$axios.delete(
					`/api/carts/user/${this.user.id}/business/${this.businessId}/food/${this.foodArr[index].id}`
				)
				if (!response.data.success) {
					this.foodArr[index].quantity = 1
				}
			} catch (error) {
				console.error('Failed to remove cart item:', error)
				this.foodArr[index].quantity = 1
			}
		},

		toOrder() {
			if (!this.user?.id) {
				alert('请先登录')
				this.$router.push('/login')
				return
			}

			this.$router.push({
				path: '/orders',
				query: {
					businessId: this.businessId
				}
			})
		}
	},
	computed: {
		totalPrice() {
			return this.foodArr.reduce((total, item) => total + item.foodPrice * item.quantity, 0)
		},
		totalQuantity() {
			return this.foodArr.reduce((quantity, item) => quantity + item.quantity, 0)
		},
		totalSettle() {
			return this.totalPrice + (this.business.deliveryPrice || 0)
		}
	}
}
</script>

<style scoped>
	.wrapper {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
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

	.wrapper .business-logo {
		width: 100%;
		padding-top: 12vw;
	}

	.wrapper .business-logo img {
		width: 100%;
		height: 30vw;
		object-fit: cover;
	}

	.wrapper .business-info {
		width: 100%;
		padding: 3vw;
		background-color: white;
		border-bottom: 1px solid #f5f5f5;
	}

	.wrapper .business-info h1 {
		font-size: 4.5vw;
		color: #333;
		margin: 0 0 2vw;
	}

	.wrapper .business-info p {
		font-size: 3.2vw;
		color: #666;
		margin: 1vw 0;
		line-height: 1.5;
	}

	.wrapper .food {
		width: 100%;
		list-style: none;
		padding: 0;
		margin: 0 0 14vw 0;
	}

	.wrapper .food .empty-food {
		width: 100%;
		height: 40vw;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		background-color: white;
		margin-top: 2vw;
	}

	.wrapper .food .empty-food i {
		font-size: 12vw;
		color: #ddd;
		margin-bottom: 2vw;
	}

	.wrapper .food .empty-food span {
		font-size: 3.5vw;
		color: #999;
	}

	.wrapper .food li {
		width: 100%;
		box-sizing: border-box;
		padding: 2.5vw;
		user-select: none;
		display: flex;
		justify-content: space-between;
		align-items: center;
		background-color: white;
		border-bottom: 1px solid #f5f5f5;
	}

	.wrapper .food li .food-left {
		display: flex;
		align-items: center;
	}

	.wrapper .food li .food-left img {
		width: 20vw;
		height: 20vw;
		border-radius: 2vw;
		object-fit: cover;
	}

	.wrapper .food li .food-left .food-left-info {
		margin-left: 3vw;
	}

	.wrapper .food li .food-left .food-left-info h3 {
		font-size: 3.8vw;
		color: #555;
		margin: 0 0 1vw;
	}

	.wrapper .food li .food-left .food-left-info p {
		font-size: 3vw;
		color: #888;
		margin: 0;
		line-height: 1.4;
	}

	.wrapper .food li .food-right {
		width: 16vw;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.wrapper .food li .food-right .fa-minus-circle {
		font-size: 5.5vw;
		color: #999;
		cursor: pointer;
	}

	.wrapper .food li .food-right p {
		font-size: 3.6vw;
		color: #333;
		margin: 0;
	}

	.wrapper .food li .food-right .fa-plus-circle {
		font-size: 5.5vw;
		color: #0097EF;
		cursor: pointer;
	}

	.wrapper .cart {
		width: 100%;
		height: 14vw;
		position: fixed;
		left: 0;
		bottom: 0;
		display: flex;
	}

	.wrapper .cart .cart-left {
		flex: 2;
		background-color: #505051;
		display: flex;
		align-items: center;
		padding-left: 2vw;
	}

	.wrapper .cart .cart-left .cart-left-icon {
		width: 16vw;
		height: 16vw;
		background-color: #3190E8;
		border-radius: 50%;
		position: relative;
		top: -4vw;
		display: flex;
		justify-content: center;
		align-items: center;
		color: white;
		font-size: 6vw;
	}

	.wrapper .cart .cart-left .cart-left-icon .cart-left-icon-quantity {
		width: 5vw;
		height: 5vw;
		background-color: #FF5A5F;
		border-radius: 50%;
		position: absolute;
		right: 1vw;
		top: 1vw;
		font-size: 3vw;
		color: white;
		text-align: center;
		line-height: 5vw;
	}

	.wrapper .cart .cart-left .cart-left-info {
		margin-left: 2vw;
		color: white;
	}

	.wrapper .cart .cart-left .cart-left-info p:first-child {
		font-size: 4vw;
		margin: 0 0 1vw 0;
	}

	.wrapper .cart .cart-left .cart-left-info p:last-child {
		font-size: 2.8vw;
		margin: 0;
		color: #ccc;
	}

	.wrapper .cart .cart-right {
		flex: 1;
	}

	.wrapper .cart .cart-right .cart-right-item {
		width: 100%;
		height: 100%;
		background-color: #0097FF;
		color: white;
		font-size: 4vw;
		display: flex;
		justify-content: center;
		align-items: center;
	}
</style>
