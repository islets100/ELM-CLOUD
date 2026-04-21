<template>
	<div class="wrapper">
		<header>
			<p>送货地址</p>
		</header>

		<ul class="addresslist">
			<li v-for="item in deliveryAddressArr" :key="item.id">
				<div class="addresslist-left" @click="setDeliveryAddress(item)">
					<h3>{{ item.contactName }}{{ formatSex(item.contactSex) }} {{ item.contactTel }}</h3>
					<p>{{ item.address }}</p>
				</div>
				<div class="addresslist-right">
					<i class="fa fa-edit" @click="editUserAddress(item.id)"></i>
					<i class="fa fa-remove" @click="removeUserAddress(item.id)"></i>
				</div>
			</li>
		</ul>

		<div class="addbtn" @click="toAddUserAddress">
			<i class="fa fa-plus-circle"></i>
			<p>新增送货地址</p>
		</div>

		<Footer></Footer>
	</div>
</template>

<script>
import Footer from '../components/Footer.vue'
import auth from '../utils/auth'

export default {
	name: 'UserAddress',
	components: {
		Footer
	},
	data() {
		return {
			businessId: this.$route.query.businessId,
			user: {},
			deliveryAddressArr: []
		}
	},
	created() {
		this.user = auth.getUserInfo()
		if (!this.user?.id) {
			alert('请先登录')
			this.$router.push('/login')
			return
		}
		this.listDeliveryAddressByUserId()
	},
	methods: {
		formatSex(value) {
			return value === 1 ? '先生' : '女士'
		},

		async listDeliveryAddressByUserId() {
			try {
				const response = await this.$axios.get(`/api/delivery-addresses/user/${this.user.id}`)
				this.deliveryAddressArr = response.data.success ? (response.data.data || []) : []
			} catch (error) {
				console.error('Failed to load delivery addresses:', error)
				this.deliveryAddressArr = []
			}
		},

		setDeliveryAddress(deliveryAddress) {
			this.$setLocalStorage(this.user.id, deliveryAddress)
			this.$router.push({
				path: '/orders',
				query: {
					businessId: this.businessId
				}
			})
		},

		toAddUserAddress() {
			this.$router.push({
				path: '/addUserAddress',
				query: {
					businessId: this.businessId
				}
			})
		},

		editUserAddress(daId) {
			this.$router.push({
				name: 'EditUserAddress',
				params: {
					id: daId
				},
				query: {
					businessId: this.businessId
				}
			})
		},

		async removeUserAddress(daId) {
			if (!confirm('确认删除该送货地址吗？')) {
				return
			}

			try {
				const response = await this.$axios.delete(`/api/delivery-addresses/${daId}`)
				if (!response.data.success || response.data.data <= 0) {
					alert(response.data.message || '删除地址失败')
					return
				}

				const selectedAddress = this.$getLocalStorage(this.user.id)
				if (selectedAddress?.id === daId || selectedAddress?.daId === daId) {
					this.$removeLocalStorage(this.user.id)
				}
				await this.listDeliveryAddressByUserId()
			} catch (error) {
				console.error('Failed to remove delivery address:', error)
			}
		}
	}
}
</script>

<style scoped>
	.wrapper {
		width: 100%;
		height: 100%;
		background-color: #F5F5F5;
	}

	.wrapper header {
		width: 100%;
		height: 12vw;
		background-color: #0097FF;
		display: flex;
		justify-content: space-around;
		align-items: center;
		color: #fff;
		font-size: 4.8vw;
		position: fixed;
		left: 0;
		top: 0;
		z-index: 1000;
	}

	.wrapper .addresslist {
		width: 100%;
		margin-top: 12vw;
		background-color: #fff;
	}

	.wrapper .addresslist li {
		width: 100%;
		box-sizing: border-box;
		border-bottom: solid 1px #DDD;
		padding: 3vw;
		display: flex;
	}

	.wrapper .addresslist li .addresslist-left {
		flex: 5;
		user-select: none;
		cursor: pointer;
	}

	.wrapper .addresslist li .addresslist-left h3 {
		font-size: 4.6vw;
		font-weight: 300;
		color: #666;
	}

	.wrapper .addresslist li .addresslist-left p {
		font-size: 4vw;
		color: #666;
	}

	.wrapper .addresslist li .addresslist-right {
		flex: 1;
		font-size: 5.6vw;
		color: #999;
		cursor: pointer;
		display: flex;
		justify-content: space-around;
		align-items: center;
	}

	.wrapper .addbtn {
		width: 100%;
		height: 14vw;
		border-top: solid 1px #DDD;
		border-bottom: solid 1px #DDD;
		background-color: #fff;
		margin-top: 4vw;
		display: flex;
		justify-content: center;
		align-items: center;
		font-size: 4.5vw;
		color: #0097FF;
		user-select: none;
		cursor: pointer;
	}

	.wrapper .addbtn p {
		margin-left: 2vw;
	}
</style>
