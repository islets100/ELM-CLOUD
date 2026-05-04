<template>
	<div class="wrapper">
		<header class="page-header">
			<button class="header-action" type="button" @click="$router.back()">返回</button>
			<p>商家详情</p>
			<button class="header-action ghost" type="button" @click="$router.push('/businessList')">分类</button>
		</header>

		<section class="hero-card">
			<img :src="heroImage" :alt="business.businessName || '商家图片'">
			<div class="hero-content">
				<h1>{{ business.businessName || '商家加载中' }}</h1>
				<p class="price-line">
					起送 ¥{{ formatPrice(business.startPrice) }}
					<span>配送 ¥{{ formatPrice(business.deliveryPrice) }}</span>
				</p>
				<p class="explain-line">{{ business.businessExplain || '这家店还没有补充介绍。' }}</p>
				<span class="remark-tag">{{ business.remarks || '支持在线下单' }}</span>
			</div>
		</section>

		<section v-if="loading" class="state-card">
			<i class="fa fa-spinner fa-spin"></i>
			<p>正在加载商品...</p>
		</section>

		<section v-else-if="errorMessage" class="state-card">
			<i class="fa fa-exclamation-circle"></i>
			<p>{{ errorMessage }}</p>
			<button type="button" @click="initData">重新加载</button>
		</section>

		<ul v-else class="food-list">
			<li v-if="foodArr.length === 0" class="state-card inline-state">
				<i class="fa fa-cutlery"></i>
				<p>商家还没有上架商品</p>
			</li>
			<li v-for="(item, index) in foodArr" :key="item.id || item.foodId">
				<div class="food-left">
					<img :src="getFoodImage(item, index)" :alt="item.foodName">
					<div class="food-info">
						<h3>{{ item.foodName }}</h3>
						<p>{{ item.foodExplain || '暂无商品介绍' }}</p>
						<div class="food-bottom">
							<span>¥{{ formatPrice(item.foodPrice) }}</span>
							<em>{{ item.remarks || '现做现卖' }}</em>
						</div>
					</div>
				</div>
				<div class="food-right">
					<button type="button" class="icon-btn minus" @click="minus(index)" :disabled="item.quantity === 0">-</button>
					<strong>{{ item.quantity }}</strong>
					<button type="button" class="icon-btn plus" @click="add(index)">+</button>
				</div>
			</li>
		</ul>

		<div class="cart-bar">
			<div class="cart-summary">
				<div class="cart-icon" :class="{ active: totalQuantity > 0 }">
					<i class="fa fa-shopping-cart"></i>
					<span v-if="totalQuantity > 0">{{ totalQuantity }}</span>
				</div>
				<div>
					<p class="cart-total">¥{{ formatPrice(totalPrice) }}</p>
					<p class="cart-tip">另需配送费 ¥{{ formatPrice(business.deliveryPrice) }}</p>
				</div>
			</div>
			<button
				type="button"
				class="checkout-btn"
				:disabled="totalSettle < Number(business.startPrice || 0)"
				@click="toOrder"
			>
				{{ totalSettle < Number(business.startPrice || 0) ? `还差 ¥${formatDiffPrice}` : '去结算' }}
			</button>
		</div>
	</div>
</template>

<script>
import auth from '../utils/auth'

const shopImages = [
	require('../assets/sj01.png'),
	require('../assets/sj02.png'),
	require('../assets/sj03.png'),
	require('../assets/sj04.png'),
	require('../assets/sj05.png'),
	require('../assets/sj06.png'),
	require('../assets/sj07.png'),
	require('../assets/sj08.png'),
	require('../assets/sj09.png')
]

const foodImages = [
	require('../assets/sp01.png'),
	require('../assets/sp02.png'),
	require('../assets/sp03.png'),
	require('../assets/sp04.png'),
	require('../assets/sp05.png'),
	require('../assets/sp06.png'),
	require('../assets/sp07.png'),
	require('../assets/sp08.png'),
	require('../assets/sp09.png'),
	require('../assets/sp10.png'),
	require('../assets/sp11.png'),
	require('../assets/sp12.png')
]

import { isFallbackResponse } from '../utils/fallbackRetry'

export default {
	name: 'BusinessInfo',
	data() {
		return {
			businessId: Number(this.$route.query.businessId),
			business: {},
			foodArr: [],
			user: null,
			loading: false,
			errorMessage: ''
		}
	},
	computed: {
		heroImage() {
			if (this.business.businessImg) {
				return this.business.businessImg
			}

			const baseIndex = Number(this.business.orderTypeId || this.businessId || 1) - 1
			return shopImages[((baseIndex % shopImages.length) + shopImages.length) % shopImages.length]
		},
		totalPrice() {
			return this.foodArr.reduce((total, item) => total + Number(item.foodPrice || 0) * Number(item.quantity || 0), 0)
		},
		totalQuantity() {
			return this.foodArr.reduce((quantity, item) => quantity + Number(item.quantity || 0), 0)
		},
		totalSettle() {
			return this.totalPrice + Number(this.business.deliveryPrice || 0)
		},
		formatDiffPrice() {
			const diff = Number(this.business.startPrice || 0) - this.totalSettle
			return Math.max(diff, 0).toFixed(2)
		}
	},
	created() {
		this.initData()
	},
	activated() {
		this.initData()
	},
	watch: {
		'$route.query.businessId'(newVal) {
			this.businessId = Number(newVal)
			this.initData()
		}
	},
	methods: {
		formatPrice(value) {
			return Number(value || 0).toFixed(2)
		},

		getFoodImage(item, index) {
			if (item.foodImg) {
				return item.foodImg
			}

			const seed = Number(item.id || item.foodId || index)
			return foodImages[seed % foodImages.length]
		},

		async initData() {
			this.businessId = Number(this.$route.query.businessId)
			this.user = auth.getUserInfo()
			this.loading = true
			this.errorMessage = ''

			try {
				await Promise.all([this.getBusinessDetail(), this.getFoodList()])
				if (this.user?.id && this.foodArr.length) {
					await this.loadCart()
				}
			} catch (error) {
				console.error('Failed to initialize business data:', error)
				this.errorMessage = '商家详情加载失败，请稍后重试'
			} finally {
				this.loading = false
			}
		},

		async getBusinessDetail() {
			const response = await this.$axios.get(`/api/businesses/${this.businessId}`)
			if (!response.data.success) {
				throw new Error(response.data.message || 'business load failed')
			}
			this.business = response.data.data || {}
		},

		async getFoodList() {
			const response = await this.$axios.get(`/api/foods/business/${this.businessId}`)
			if (!response.data.success) {
				throw new Error(response.data.message || 'food load failed')
			}

			this.foodArr = (response.data.data || []).map(food => ({
				...food,
				quantity: 0
			}))
		},

		async loadCart() {
			try {
				const response = await this.$axios.get(`/api/carts/user/${this.user.id}/business/${this.businessId}`, {
						__skipFallbackPrompt: true,
						__skipFallbackRetry: true
					})
				if (!response.data.success) {
					return
				}

				const quantityMap = {}
				;(response.data.data || []).forEach(cartItem => {
					quantityMap[cartItem.foodId] = Number(cartItem.quantity || 0)
				})

				this.foodArr = this.foodArr.map(item => ({
					...item,
					quantity: quantityMap[item.id || item.foodId] || 0
				}))
			} catch (error) {
				console.error('Failed to load cart:', error)
			}
		},

		ensureLogin() {
			if (this.user?.id) {
				return true
			}

			alert('请先登录后再加入购物车')
			this.$router.push({
				path: '/login',
				query: {
					redirect: `/businessInfo?businessId=${this.businessId}`
				}
			})
			return false
		},

		async add(index) {
			if (!this.ensureLogin()) {
				return
			}

			this.foodArr[index].quantity += 1
			try {
				const response = await this.$axios.post('/api/carts', {
					businessId: this.businessId,
					userId: this.user.id,
					foodId: this.foodArr[index].id || this.foodArr[index].foodId,
					quantity: 1
				})
				if (!response.data.success) {
					this.foodArr[index].quantity -= 1
					if (!isFallbackResponse(response.data)) {
							alert(response.data.message || '加入购物车失败')
						}
				}
			} catch (error) {
				console.error('Failed to save cart item:', error)
				this.foodArr[index].quantity -= 1
				alert(error.response?.data?.message || '加入购物车失败')
			}
		},

		async minus(index) {
			if (!this.ensureLogin()) {
				return
			}

			if (this.foodArr[index].quantity <= 0) {
				return
			}

			this.foodArr[index].quantity -= 1
			const foodId = this.foodArr[index].id || this.foodArr[index].foodId

			try {
				if (this.foodArr[index].quantity === 0) {
					const response = await this.$axios.delete(`/api/carts/user/${this.user.id}/business/${this.businessId}/food/${foodId}`)
					if (!response.data.success) {
						this.foodArr[index].quantity = 1
						alert(response.data.message || '更新购物车失败')
					}
					return
				}

				const response = await this.$axios.put(
					`/api/carts/user/${this.user.id}/business/${this.businessId}/food/${foodId}`,
					{ quantity: this.foodArr[index].quantity }
				)
				if (!response.data.success) {
					this.foodArr[index].quantity += 1
					alert(response.data.message || '更新购物车失败')
				}
			} catch (error) {
				console.error('Failed to update cart item:', error)
				this.foodArr[index].quantity += 1
				alert(error.response?.data?.message || '更新购物车失败')
			}
		},

		toOrder() {
			if (!this.ensureLogin()) {
				return
			}

			if (this.totalQuantity === 0) {
				alert('请先选择商品')
				return
			}

			if (this.totalSettle < Number(this.business.startPrice || 0)) {
				alert(`还差 ¥${this.formatDiffPrice} 才能起送`)
				return
			}

			this.$router.push({
				path: '/orders',
				query: {
					businessId: this.businessId
				}
			})
		}
	}
}
</script>

<style scoped>
.wrapper {
	width: 100%;
	min-height: 100vh;
	padding-bottom: 18vw;
	background: linear-gradient(180deg, #eef7ff 0, #f7f7f7 22vw, #f5f5f5 100%);
}

.page-header {
	width: 100%;
	height: 14vw;
	padding: 0 3vw;
	box-sizing: border-box;
	background-color: #0097ff;
	color: #fff;
	position: sticky;
	top: 0;
	z-index: 1000;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.page-header p {
	margin: 0;
	font-size: 4.6vw;
	font-weight: 700;
}

.header-action {
	border: none;
	background: rgba(255, 255, 255, 0.18);
	color: #fff;
	border-radius: 999px;
	padding: 1.4vw 3vw;
	font-size: 3.2vw;
}

.header-action.ghost {
	background: transparent;
	border: 1px solid rgba(255, 255, 255, 0.35);
}

.hero-card {
	margin: 4vw;
	border-radius: 4vw;
	overflow: hidden;
	background-color: #fff;
	box-shadow: 0 1.5vw 4vw rgba(15, 58, 109, 0.08);
}

.hero-card img {
	width: 100%;
	height: 36vw;
	object-fit: cover;
	background-color: #eef4f9;
}

.hero-content {
	padding: 4vw;
}

.hero-content h1 {
	margin: 0 0 1.6vw;
	font-size: 5vw;
	color: #213547;
}

.price-line {
	margin: 0 0 1.4vw;
	font-size: 3.2vw;
	color: #ff7a45;
}

.price-line span {
	margin-left: 2vw;
	color: #607080;
}

.explain-line {
	margin: 0 0 2vw;
	font-size: 3.2vw;
	line-height: 1.6;
	color: #667788;
}

.remark-tag {
	display: inline-flex;
	align-items: center;
	padding: 1vw 2vw;
	border-radius: 999px;
	background-color: #eef7ff;
	color: #0097ff;
	font-size: 2.8vw;
}

.food-list {
	margin: 0;
	padding: 0 4vw;
	list-style: none;
}

.food-list li {
	margin-bottom: 3vw;
	padding: 3.5vw;
	border-radius: 4vw;
	background-color: #fff;
	box-shadow: 0 1.5vw 4vw rgba(15, 58, 109, 0.06);
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 3vw;
}

.food-left {
	display: flex;
	align-items: center;
	gap: 3vw;
	flex: 1;
	min-width: 0;
}

.food-left img {
	width: 18vw;
	height: 18vw;
	border-radius: 3vw;
	object-fit: cover;
	background-color: #eef4f9;
}

.food-info {
	flex: 1;
	min-width: 0;
}

.food-info h3 {
	margin: 0 0 1vw;
	font-size: 3.8vw;
	color: #213547;
}

.food-info p {
	margin: 0 0 1.6vw;
	font-size: 3vw;
	line-height: 1.5;
	color: #6b7a89;
}

.food-bottom {
	display: flex;
	align-items: center;
	gap: 2vw;
	flex-wrap: wrap;
}

.food-bottom span {
	font-size: 3.6vw;
	font-weight: 700;
	color: #ff6b35;
}

.food-bottom em {
	font-style: normal;
	font-size: 2.8vw;
	color: #0097ff;
	background-color: #eef7ff;
	border-radius: 999px;
	padding: 0.6vw 1.6vw;
}

.food-right {
	display: flex;
	align-items: center;
	gap: 2vw;
}

.food-right strong {
	min-width: 4vw;
	text-align: center;
	font-size: 3.6vw;
	color: #213547;
}

.icon-btn {
	width: 7vw;
	height: 7vw;
	border: none;
	border-radius: 50%;
	font-size: 4.5vw;
	line-height: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}

.icon-btn.plus {
	background-color: #0097ff;
	color: #fff;
}

.icon-btn.minus {
	background-color: #eef1f4;
	color: #607080;
}

.icon-btn:disabled {
	opacity: 0.5;
}

.cart-bar {
	width: 100%;
	height: 15vw;
	padding: 0 3vw;
	box-sizing: border-box;
	background-color: #1f2d3d;
	position: fixed;
	left: 0;
	bottom: 0;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.cart-summary {
	display: flex;
	align-items: center;
	gap: 3vw;
	color: #fff;
}

.cart-icon {
	width: 13vw;
	height: 13vw;
	border-radius: 50%;
	background-color: #4a5560;
	position: relative;
	top: -2vw;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 5vw;
}

.cart-icon.active {
	background-color: #0097ff;
}

.cart-icon span {
	min-width: 5vw;
	height: 5vw;
	padding: 0 1vw;
	border-radius: 999px;
	background-color: #ff5a5f;
	font-size: 2.8vw;
	position: absolute;
	right: -0.8vw;
	top: -0.6vw;
	display: flex;
	align-items: center;
	justify-content: center;
}

.cart-total {
	margin: 0 0 0.8vw;
	font-size: 4.2vw;
	font-weight: 700;
}

.cart-tip {
	margin: 0;
	font-size: 2.8vw;
	color: rgba(255, 255, 255, 0.72);
}

.checkout-btn {
	min-width: 28vw;
	height: 10vw;
	border: none;
	border-radius: 999px;
	background: linear-gradient(135deg, #1ac97a, #29b765);
	color: #fff;
	font-size: 3.8vw;
	font-weight: 700;
}

.checkout-btn:disabled {
	background: #4a5560;
	color: rgba(255, 255, 255, 0.7);
}

.state-card {
	margin: 4vw;
	padding: 12vw 6vw;
	border-radius: 4vw;
	background-color: #fff;
	box-shadow: 0 1.5vw 4vw rgba(15, 58, 109, 0.06);
	text-align: center;
	color: #7b8a97;
}

.state-card i {
	font-size: 12vw;
	color: #c8d3dd;
}

.state-card p {
	margin: 3vw 0;
	font-size: 3.6vw;
}

.state-card button {
	border: none;
	background-color: #0097ff;
	color: #fff;
	border-radius: 999px;
	padding: 2vw 5vw;
	font-size: 3.2vw;
}

.inline-state {
	display: block;
}
</style>
