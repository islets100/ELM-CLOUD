<template>
	<div class="wrapper">
		<header>
			<p>商家列表</p>
		</header>
		<ul class="business">
			<li v-for="item in businessArr" :key="item.id" @click="toBusinessInfo(item.id)">
				<div class="business-img">
					<img :src="item.businessImg || '../assets/sj01.png'">
					<div class="business-img-quantity" v-show="item.quantity > 0">{{ item.quantity }}</div>
				</div>
				<div class="business-info">
					<h3>{{ item.businessName }}</h3>
					<p>&#165;{{ item.startPrice }}起送 | &#165;{{ item.deliveryPrice }}配送</p>
					<p>{{ item.businessExplain }}</p>
				</div>
			</li>
		</ul>
		<Footer></Footer>
	</div>
</template>

<script>
import Footer from '../components/Footer.vue'
import auth from '../utils/auth'

export default {
	name: 'BusinessList',
	components: {
		Footer
	},
	data() {
		return {
			orderTypeId: Number(this.$route.query.orderTypeId),
			businessArr: [],
			user: null
		}
	},
	created() {
		this.user = auth.getUserInfo()
		this.getBusinessList()
	},
	watch: {
		'$route.query.orderTypeId'(newVal) {
			this.orderTypeId = Number(newVal)
			this.getBusinessList()
		}
	},
	methods: {
		async getBusinessList() {
			try {
				const response = await this.$axios.get(`/api/businesses/order-type/${this.orderTypeId}`)
				if (!response.data.success) {
					this.businessArr = []
					return
				}

				this.businessArr = (response.data.data || []).map(item => ({
					...item,
					quantity: 0
				}))

				if (this.user?.id) {
					await this.getCartList()
				}
			} catch (error) {
				console.error('Failed to load businesses:', error)
				this.businessArr = []
			}
		},

		async getCartList() {
			try {
				const response = await this.$axios.get(`/api/carts/user/${this.user.id}`)
				if (!response.data.success) {
					return
				}

				const quantityMap = {}
				;(response.data.data || []).forEach(cart => {
					quantityMap[cart.businessId] = (quantityMap[cart.businessId] || 0) + (cart.quantity || 0)
				})

				this.businessArr = this.businessArr.map(business => ({
					...business,
					quantity: quantityMap[business.id] || 0
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

	.wrapper .business {
		width: 100%;
		margin-top: 12vw;
		margin-bottom: 14vw;
	}

	.wrapper .business li {
		width: 100%;
		box-sizing: border-box;
		padding: 2.5vw;
		border-bottom: solid 1px #DDD;
		user-select: none;
		cursor: pointer;
		display: flex;
		align-items: center;
	}

	.wrapper .business li .business-img {
		position: relative;
	}

	.wrapper .business li .business-img img {
		width: 20vw;
		height: 20vw;
	}

	.wrapper .business li .business-img .business-img-quantity {
		width: 5vw;
		height: 5vw;
		background-color: red;
		color: #fff;
		font-size: 3.6vw;
		border-radius: 2.5vw;
		display: flex;
		justify-content: center;
		align-items: center;
		position: absolute;
		right: -1.5vw;
		top: -1.5vw;
	}

	.wrapper .business li .business-info {
		margin-left: 3vw;
	}

	.wrapper .business li .business-info h3 {
		font-size: 3.8vw;
		color: #555;
	}

	.wrapper .business li .business-info p {
		font-size: 3vw;
		color: #888;
		margin-top: 2vw;
	}
</style>
