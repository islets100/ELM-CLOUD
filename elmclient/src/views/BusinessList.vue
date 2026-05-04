<template>
	<div class="wrapper">
		<header class="page-header">
			<button class="header-action" type="button" @click="$router.back()">返回</button>
			<div class="header-title">
				<p>{{ currentCategory.name }}</p>
				<span>{{ currentCategory.description }}</span>
			</div>
			<button class="header-action ghost" type="button" @click="$router.push('/index')">首页</button>
		</header>

		<section class="category-banner">
			<img :src="currentCategory.icon" :alt="currentCategory.name">
			<div>
				<h1>{{ currentCategory.name }}</h1>
				<p>{{ currentCategory.description }}</p>
			</div>
		</section>

		<section v-if="loading" class="state-card">
			<i class="fa fa-spinner fa-spin"></i>
			<p>正在加载商家列表...</p>
		</section>

		<section v-else-if="errorMessage" class="state-card">
			<i class="fa fa-exclamation-circle"></i>
			<p>{{ errorMessage }}</p>
			<button type="button" @click="getBusinessList">重新加载</button>
		</section>

		<ul v-else-if="businessArr.length" class="business-list">
			<li v-for="(item, index) in businessArr" :key="item.id || item.businessId" @click="toBusinessInfo(item.id || item.businessId)">
				<div class="business-image">
					<img :src="getBusinessImage(item, index)" :alt="item.businessName">
					<div v-if="item.quantity > 0" class="quantity-badge">{{ item.quantity }}</div>
				</div>
				<div class="business-content">
					<div class="business-heading">
						<h3>{{ item.businessName }}</h3>
						<span>{{ item.remarks || '精选商家' }}</span>
					</div>
					<p class="business-price">
						起送 ¥{{ formatPrice(item.startPrice) }}
						<span>配送 ¥{{ formatPrice(item.deliveryPrice) }}</span>
					</p>
					<p class="business-desc">{{ item.businessExplain || '暂无商家介绍' }}</p>
				</div>
			</li>
		</ul>

		<section v-else class="state-card">
			<i class="fa fa-cutlery"></i>
			<p>当前分类还没有可用商家</p>
		</section>

		<Footer />
	</div>
</template>

<script>
import Footer from '../components/Footer.vue'
import auth from '../utils/auth'

const categoryMetaMap = {
	1: { name: '美食', description: '盖饭、小炒和家常热餐', icon: require('../assets/dcfl01.png') },
	2: { name: '早餐', description: '豆浆包子和现做早点', icon: require('../assets/dcfl02.png') },
	3: { name: '跑腿代购', description: '文件、药品和超市代买', icon: require('../assets/dcfl03.png') },
	4: { name: '汉堡披萨', description: '汉堡炸鸡和小食拼盘', icon: require('../assets/dcfl04.png') },
	5: { name: '甜品饮品', description: '奶茶甜品和下午茶', icon: require('../assets/dcfl05.png') },
	6: { name: '简餐便当', description: '出餐快，适合赶课赶工', icon: require('../assets/dcfl06.png') },
	7: { name: '地方小吃', description: '特色风味和夜宵小食', icon: require('../assets/dcfl07.png') },
	8: { name: '面食', description: '汤面拌面和暖胃主食', icon: require('../assets/dcfl08.png') },
	9: { name: '粥铺', description: '清淡养胃，早晚都合适', icon: require('../assets/dcfl09.png') },
	10: { name: '炸鸡烤串', description: '追剧夜宵和解馋加餐', icon: require('../assets/dcfl10.png') }
}

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

export default {
	name: 'BusinessList',
	components: {
		Footer
	},
	data() {
		return {
			orderTypeId: Number(this.$route.query.orderTypeId) || 1,
			businessArr: [],
			user: null,
			loading: false,
			errorMessage: ''
		}
	},
	computed: {
		currentCategory() {
			return categoryMetaMap[this.orderTypeId] || {
				name: '商家列表',
				description: '按分类查看可下单商家',
				icon: shopImages[0]
			}
		}
	},
	created() {
		this.user = auth.getUserInfo()
		this.getBusinessList()
	},
	watch: {
		'$route.query.orderTypeId'(newVal) {
			this.orderTypeId = Number(newVal) || 1
			this.getBusinessList()
		}
	},
	methods: {
		formatPrice(value) {
			return Number(value || 0).toFixed(2)
		},

		getBusinessImage(item, index) {
			if (item.businessImg) {
				return item.businessImg
			}

			const baseIndex = Number(item.orderTypeId || this.orderTypeId || index + 1) - 1
			return shopImages[(baseIndex + index) % shopImages.length]
		},

		async getBusinessList() {
			this.loading = true
			this.errorMessage = ''
			this.businessArr = []

			try {
				const response = await this.$axios.get(`/api/businesses/order-type/${this.orderTypeId}`)
				if (!response.data.success) {
					this.errorMessage = response.data.message || '商家列表加载失败'
					return
				}

				this.businessArr = (response.data.data || []).map(item => ({
					...item,
					quantity: 0
				}))

				if (this.user?.id && this.businessArr.length) {
					await this.getCartList()
				}
			} catch (error) {
				console.error('Failed to load businesses:', error)
				this.errorMessage = '商家列表加载失败，请稍后重试'
			} finally {
				this.loading = false
			}
		},

		async getCartList() {
			try {
				const response = await this.$axios.get(`/api/carts/user/${this.user.id}`, {
						__skipFallbackPrompt: true,
						__skipFallbackRetry: true
					})
				if (!response.data.success) {
					return
				}

				const quantityMap = {}
				;(response.data.data || []).forEach(cart => {
					const businessId = cart.businessId || cart.id
					quantityMap[businessId] = (quantityMap[businessId] || 0) + Number(cart.quantity || 0)
				})

				this.businessArr = this.businessArr.map(business => ({
					...business,
					quantity: quantityMap[business.id || business.businessId] || 0
				}))
			} catch (error) {
				console.error('Failed to load cart summary:', error)
			}
		},

		toBusinessInfo(businessId) {
			this.$router.push({
				path: '/businessInfo',
				query: {
					businessId
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
	padding-bottom: 16vw;
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

.header-title {
	text-align: center;
}

.header-title p {
	margin: 0;
	font-size: 4.4vw;
	font-weight: 700;
}

.header-title span {
	font-size: 2.8vw;
	opacity: 0.9;
}

.category-banner {
	margin: 4vw;
	padding: 4vw;
	border-radius: 4vw;
	background: #fff;
	box-shadow: 0 1.5vw 4vw rgba(15, 58, 109, 0.08);
	display: flex;
	align-items: center;
	gap: 4vw;
}

.category-banner img {
	width: 16vw;
	height: 16vw;
	object-fit: contain;
}

.category-banner h1 {
	margin: 0 0 1vw;
	font-size: 5vw;
	color: #213547;
}

.category-banner p {
	margin: 0;
	font-size: 3.2vw;
	color: #607080;
	line-height: 1.5;
}

.business-list {
	margin: 0;
	padding: 0 4vw;
	list-style: none;
}

.business-list li {
	margin-bottom: 3vw;
	padding: 3.5vw;
	border-radius: 4vw;
	background-color: #fff;
	box-shadow: 0 1.5vw 4vw rgba(15, 58, 109, 0.06);
	display: flex;
	align-items: center;
	gap: 3vw;
	cursor: pointer;
}

.business-image {
	position: relative;
	flex-shrink: 0;
}

.business-image img {
	width: 20vw;
	height: 20vw;
	border-radius: 3vw;
	object-fit: cover;
	background-color: #eef4f9;
}

.quantity-badge {
	min-width: 5vw;
	height: 5vw;
	padding: 0 1vw;
	border-radius: 999px;
	background-color: #ff5a5f;
	color: #fff;
	font-size: 3vw;
	position: absolute;
	right: -1vw;
	top: -1vw;
	display: flex;
	align-items: center;
	justify-content: center;
}

.business-content {
	flex: 1;
	min-width: 0;
}

.business-heading {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	gap: 2vw;
}

.business-heading h3 {
	margin: 0;
	font-size: 4vw;
	color: #213547;
}

.business-heading span {
	flex-shrink: 0;
	padding: 0.8vw 1.8vw;
	border-radius: 999px;
	background-color: #eef7ff;
	color: #0097ff;
	font-size: 2.6vw;
}

.business-price {
	margin: 1.6vw 0 1vw;
	font-size: 3vw;
	color: #ff7a45;
}

.business-price span {
	margin-left: 2vw;
	color: #607080;
}

.business-desc {
	margin: 0;
	font-size: 3vw;
	line-height: 1.5;
	color: #6b7a89;
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
</style>
