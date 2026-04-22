<template>
	<div class="wrapper">
		<header class="top-bar">
			<div class="location-box">
				<i class="fa fa-map-marker"></i>
				<div class="location-text">
					<label for="location-select">配送地址</label>
					<select id="location-select" v-model="currentLocation" @change="onLocationChange">
						<option v-for="loc in locations" :key="loc" :value="loc">{{ loc }}</option>
					</select>
				</div>
			</div>
			<button class="login-btn" type="button" @click="goUserCenter">
				{{ isLoggedIn ? '我的' : '登录' }}
			</button>
		</header>

		<section class="search-section">
			<div class="search-box" @click="focusCategory">
				<i class="fa fa-search"></i>
				<span>查看分类商家，开始点餐</span>
			</div>
		</section>

		<ul ref="categorySection" class="category-grid">
			<li v-for="item in categories" :key="item.id" :title="item.tip" @click="toBusinessList(item.id)">
				<img :src="item.icon" :alt="item.name">
				<p>{{ item.name }}</p>
			</li>
		</ul>

		<section class="member-card">
			<div class="member-left">
				<img src="../assets/super_member.png" alt="会员中心">
				<div>
					<h3>签到领积分</h3>
					<p>登录后可参与每日签到，钱包和积分页都能联动验证。</p>
				</div>
			</div>
			<button type="button" @click="openPointsPage">去查看</button>
		</section>

		<section class="section-title">
			<h2>推荐商家</h2>
			<p>以下内容直接来自网关转发后的后端接口</p>
		</section>

		<section v-if="loading" class="state-card">
			<i class="fa fa-spinner fa-spin"></i>
			<p>正在加载商家...</p>
		</section>

		<section v-else-if="errorMessage" class="state-card">
			<i class="fa fa-exclamation-circle"></i>
			<p>{{ errorMessage }}</p>
			<button type="button" @click="loadRecommendedBusinesses">重新加载</button>
		</section>

		<ul v-else class="business-list">
			<li v-for="(item, index) in recommendedBusinesses" :key="item.id || item.businessId" @click="toBusiness(item.id || item.businessId)">
				<img :src="getBusinessImage(item, index)" :alt="item.businessName">
				<div class="business-info">
					<div class="business-header">
						<h3>{{ item.businessName }}</h3>
						<span>{{ getCategoryName(item.orderTypeId) }}</span>
					</div>
					<p class="business-price">
						起送 ¥{{ formatPrice(item.startPrice) }}
						<span>配送 ¥{{ formatPrice(item.deliveryPrice) }}</span>
					</p>
					<p class="business-desc">{{ item.businessExplain || '暂无商家介绍' }}</p>
					<p class="business-remark">{{ item.remarks || '支持在线下单' }}</p>
				</div>
			</li>
		</ul>

		<div class="sign-in-modal" v-if="showSignInModal" @click.self="closeSignInModal">
			<div class="sign-in-content">
				<div class="sign-in-header">
					<h3>今日签到</h3>
					<button class="close-btn" type="button" @click="closeSignInModal">×</button>
				</div>
				<div class="sign-in-body">
					<div class="sign-in-icon">积分 +10</div>
					<p class="sign-in-text">检测到你今天还没签到</p>
					<p class="sign-in-tip">现在签到会给当前账号补充 10 积分</p>
					<button class="sign-in-btn" type="button" @click="handleSignIn" :disabled="signingIn">
						{{ signingIn ? '签到中...' : '立即签到' }}
					</button>
				</div>
			</div>
		</div>

		<Footer />
	</div>
</template>

<script>
import Footer from '../components/Footer.vue'
import auth from '../utils/auth'
import pointsApi from '../api/points.js'

const categories = [
	{ id: 1, name: '美食', tip: '牛肉饭、小炒', icon: require('../assets/dcfl01.png') },
	{ id: 2, name: '早餐', tip: '豆浆包子', icon: require('../assets/dcfl02.png') },
	{ id: 3, name: '跑腿代购', tip: '取件代买', icon: require('../assets/dcfl03.png') },
	{ id: 4, name: '汉堡披萨', tip: '炸鸡汉堡', icon: require('../assets/dcfl04.png') },
	{ id: 5, name: '甜品饮品', tip: '奶茶甜品', icon: require('../assets/dcfl05.png') },
	{ id: 6, name: '简餐便当', tip: '快手便当', icon: require('../assets/dcfl06.png') },
	{ id: 7, name: '地方小吃', tip: '特色风味', icon: require('../assets/dcfl07.png') },
	{ id: 8, name: '面食', tip: '热汤热面', icon: require('../assets/dcfl08.png') },
	{ id: 9, name: '粥铺', tip: '清淡暖胃', icon: require('../assets/dcfl09.png') },
	{ id: 10, name: '炸鸡烤串', tip: '夜宵解馋', icon: require('../assets/dcfl10.png') }
]

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
	name: 'Index',
	components: {
		Footer
	},
	data() {
		return {
			categories,
			currentLocation: '大学城软件实践实验室',
			locations: [
				'大学城软件实践实验室',
				'大学城生活区东门',
				'大学城图书馆西侧',
				'大学城商业街夜市口'
			],
			recommendedBusinesses: [],
			loading: false,
			errorMessage: '',
			showSignInModal: false,
			signingIn: false
		}
	},
	computed: {
		isLoggedIn() {
			return auth.isLoggedIn()
		}
	},
	async mounted() {
		await Promise.all([this.loadRecommendedBusinesses(), this.checkSignInStatus()])
	},
	methods: {
		formatPrice(value) {
			return Number(value || 0).toFixed(2)
		},

		getCategoryName(orderTypeId) {
			return this.categories.find(item => item.id === Number(orderTypeId))?.name || '其他分类'
		},

		getBusinessImage(item, index) {
			if (item.businessImg) {
				return item.businessImg
			}

			const seed = Number(item.orderTypeId || index + 1) - 1
			return shopImages[((seed + index) % shopImages.length + shopImages.length) % shopImages.length]
		},

		focusCategory() {
			this.$refs.categorySection?.scrollIntoView({
				behavior: 'smooth',
				block: 'start'
			})
		},

		onLocationChange() {
			console.log('Location changed:', this.currentLocation)
		},

		goUserCenter() {
			if (this.isLoggedIn) {
				this.$router.push('/user')
				return
			}

			this.$router.push('/login')
		},

		openPointsPage() {
			if (!this.isLoggedIn) {
				this.$router.push('/login')
				return
			}

			this.$router.push('/points')
		},

		toBusinessList(orderTypeId) {
			this.$router.push({
				path: '/businessList',
				query: {
					orderTypeId
				}
			})
		},

		toBusiness(businessId) {
			this.$router.push({
				path: '/businessInfo',
				query: {
					businessId
				}
			})
		},

		async loadRecommendedBusinesses() {
			this.loading = true
			this.errorMessage = ''

			try {
				const responses = await Promise.all(
					this.categories.map(category => this.$axios.get(`/api/businesses/order-type/${category.id}`))
				)

				const businesses = responses.flatMap(response => (response.data.success ? response.data.data || [] : []))
				this.recommendedBusinesses = businesses
					.filter(item => item && (item.id || item.businessId))
					.sort((a, b) => Number(a.orderTypeId || 0) - Number(b.orderTypeId || 0))
			} catch (error) {
				console.error('Failed to load recommended businesses:', error)
				this.errorMessage = '首页商家加载失败，请确认网关和业务服务都已启动'
			} finally {
				this.loading = false
			}
		},

		async checkSignInStatus() {
			if (!auth.isLoggedIn()) {
				return
			}

			try {
				const response = await pointsApi.checkSignInToday()
				if (response.data.success && response.data.data === false) {
					setTimeout(() => {
						this.showSignInModal = true
					}, 500)
				}
			} catch (error) {
				console.error('Failed to check sign-in status:', error)
			}
		},

		async handleSignIn() {
			if (this.signingIn) {
				return
			}

			this.signingIn = true
			try {
				const response = await pointsApi.signIn()
				if (response.data.success) {
					alert('签到成功，已发放 10 积分')
					this.closeSignInModal()
					return
				}

				alert(response.data.message || '签到失败')
			} catch (error) {
				console.error('Failed to sign in:', error)
				alert(error.response?.data?.message || '签到失败，请稍后重试')
			} finally {
				this.signingIn = false
			}
		},

		closeSignInModal() {
			this.showSignInModal = false
		}
	}
}
</script>

<style scoped>
.wrapper {
	width: 100%;
	min-height: 100vh;
	padding-bottom: 16vw;
	background:
		radial-gradient(circle at top right, rgba(0, 151, 255, 0.14), transparent 32vw),
		linear-gradient(180deg, #eaf6ff 0, #f6f7fb 22vw, #f5f5f5 100%);
}

.top-bar {
	width: 100%;
	padding: 3vw;
	box-sizing: border-box;
	background-color: #0097ff;
	color: #fff;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.location-box {
	display: flex;
	align-items: center;
	gap: 2vw;
}

.location-box i {
	font-size: 5vw;
}

.location-text {
	display: flex;
	flex-direction: column;
}

.location-text label {
	font-size: 2.8vw;
	opacity: 0.86;
	margin-bottom: 0.6vw;
}

.location-text select {
	border: none;
	background: transparent;
	color: #fff;
	font-size: 4.2vw;
	font-weight: 700;
	padding: 0;
}

.location-text select:focus {
	outline: none;
}

.location-text option {
	color: #213547;
}

.login-btn {
	border: 1px solid rgba(255, 255, 255, 0.32);
	background: rgba(255, 255, 255, 0.12);
	color: #fff;
	border-radius: 999px;
	padding: 1.6vw 4vw;
	font-size: 3.2vw;
}

.search-section {
	padding: 3vw 4vw 1vw;
}

.search-box {
	height: 11vw;
	border-radius: 999px;
	background-color: #fff;
	box-shadow: 0 1.5vw 4vw rgba(15, 58, 109, 0.08);
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 2vw;
	color: #7b8a97;
	font-size: 3.4vw;
}

.search-box i {
	color: #0097ff;
}

.category-grid {
	margin: 0;
	padding: 2vw 1.5vw 0;
	list-style: none;
	display: flex;
	flex-wrap: wrap;
	justify-content: space-around;
	align-content: center;
}

.category-grid li {
	width: 18vw;
	height: 20vw;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	cursor: pointer;
}

.category-grid li img {
	width: 12vw;
	height: 10.3vw;
	object-fit: contain;
}

.category-grid li p {
	margin: 1.6vw 0 0;
	font-size: 3.2vw;
	color: #49586a;
	line-height: 1.2;
	text-align: center;
}

.member-card {
	margin: 0 4vw 4vw;
	padding: 3.5vw 4vw;
	border-radius: 4vw;
	background: linear-gradient(135deg, #fff1d1, #ffe4a4);
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 3vw;
}

.member-left {
	display: flex;
	align-items: center;
	gap: 3vw;
}

.member-left img {
	width: 10vw;
	height: 10vw;
}

.member-left h3 {
	margin: 0 0 1vw;
	font-size: 4vw;
	color: #6f4e00;
}

.member-left p {
	margin: 0;
	font-size: 3vw;
	line-height: 1.5;
	color: #8c6711;
}

.member-card button {
	border: none;
	background-color: rgba(111, 78, 0, 0.12);
	color: #6f4e00;
	border-radius: 999px;
	padding: 1.8vw 3vw;
	font-size: 3vw;
}

.section-title {
	padding: 0 4vw;
	margin-bottom: 2vw;
}

.section-title h2 {
	margin: 0 0 1vw;
	font-size: 5vw;
	color: #213547;
}

.section-title p {
	margin: 0;
	font-size: 3vw;
	color: #6b7a89;
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

.business-list li img {
	width: 20vw;
	height: 20vw;
	border-radius: 3vw;
	object-fit: cover;
	background-color: #eef4f9;
}

.business-info {
	flex: 1;
	min-width: 0;
}

.business-header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 2vw;
}

.business-header h3 {
	margin: 0;
	font-size: 4vw;
	color: #213547;
}

.business-header span {
	flex-shrink: 0;
	padding: 0.8vw 1.8vw;
	border-radius: 999px;
	background-color: #eef7ff;
	color: #0097ff;
	font-size: 2.6vw;
}

.business-price {
	margin: 1.4vw 0 1vw;
	font-size: 3vw;
	color: #ff7a45;
}

.business-price span {
	margin-left: 2vw;
	color: #607080;
}

.business-desc,
.business-remark {
	margin: 0;
	font-size: 3vw;
	line-height: 1.5;
	color: #6b7a89;
}

.business-remark {
	margin-top: 1vw;
	color: #0097ff;
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

.sign-in-modal {
	position: fixed;
	inset: 0;
	background-color: rgba(15, 23, 42, 0.45);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 2000;
}

.sign-in-content {
	width: 80%;
	max-width: 420px;
	border-radius: 4vw;
	background-color: #fff;
	overflow: hidden;
	box-shadow: 0 2vw 6vw rgba(15, 58, 109, 0.18);
}

.sign-in-header {
	padding: 4vw;
	background-color: #0097ff;
	color: #fff;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.sign-in-header h3 {
	margin: 0;
	font-size: 4.5vw;
}

.close-btn {
	border: none;
	background: transparent;
	color: #fff;
	font-size: 7vw;
	line-height: 1;
}

.sign-in-body {
	padding: 6vw;
	text-align: center;
}

.sign-in-icon {
	margin-bottom: 3vw;
	font-size: 6vw;
	font-weight: 700;
	color: #0097ff;
}

.sign-in-text {
	margin: 0 0 2vw;
	font-size: 4.5vw;
	color: #213547;
}

.sign-in-tip {
	margin: 0 0 5vw;
	font-size: 3.2vw;
	color: #6b7a89;
}

.sign-in-btn {
	width: 100%;
	height: 11vw;
	border: none;
	border-radius: 999px;
	background-color: #0097ff;
	color: #fff;
	font-size: 4vw;
	font-weight: 700;
}

.sign-in-btn:disabled {
	background-color: #a9c7df;
}
</style>
