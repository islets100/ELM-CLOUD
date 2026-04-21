<template>
	<div class="wrapper">
		<header>
			<p>编辑送货地址</p>
		</header>

		<ul class="form-box">
			<li>
				<div class="title">联系人</div>
				<div class="content">
					<input type="text" v-model="deliveryAddress.contactName" placeholder="联系人姓名">
				</div>
			</li>
			<li>
				<div class="title">性别</div>
				<div class="content" style="font-size: 3vw;">
					<input type="radio" v-model="deliveryAddress.contactSex" :value="1" style="width:6vw;height:3.2vw;">男
					<input type="radio" v-model="deliveryAddress.contactSex" :value="0" style="width:6vw;height:3.2vw;">女
				</div>
			</li>
			<li>
				<div class="title">电话</div>
				<div class="content">
					<input type="tel" v-model="deliveryAddress.contactTel" placeholder="电话">
				</div>
			</li>
			<li>
				<div class="title">送货地址</div>
				<div class="content">
					<input type="text" v-model="deliveryAddress.address" placeholder="送货地址">
				</div>
			</li>
		</ul>

		<div class="button-add">
			<button @click="updateUserAddress">保存</button>
		</div>

		<Footer></Footer>
	</div>
</template>

<script>
import Footer from '../components/Footer.vue'
import auth from '../utils/auth'

export default {
	name: 'EditUserAddress',
	components: {
		Footer
	},
	data() {
		return {
			businessId: this.$route.query.businessId,
			user: {},
			daId: Number(this.$route.params.id),
			deliveryAddress: {
				contactName: '',
				contactSex: 1,
				contactTel: '',
				address: ''
			}
		}
	},
	async created() {
		this.user = auth.getUserInfo()
		if (!this.user?.id) {
			alert('请先登录')
			this.$router.push('/login')
			return
		}

		if (!this.daId) {
			alert('地址 ID 无效')
			return
		}

		try {
			const response = await this.$axios.get(`/api/delivery-addresses/${this.daId}`)
			if (response.data.success) {
				this.deliveryAddress = response.data.data || this.deliveryAddress
			} else {
				alert(response.data.message || '加载地址失败')
			}
		} catch (error) {
			console.error('Failed to load delivery address:', error)
		}
	},
	methods: {
		async updateUserAddress() {
			if (!this.deliveryAddress.contactName || !this.deliveryAddress.contactTel || !this.deliveryAddress.address) {
				alert('请完整填写地址信息')
				return
			}

			try {
				const response = await this.$axios.put(`/api/delivery-addresses/${this.daId}`, {
					...this.deliveryAddress,
					userId: this.user.id
				})

				if (!response.data.success) {
					alert(response.data.message || '更新地址失败')
					return
				}

				const selectedAddress = this.$getLocalStorage(this.user.id)
				if (selectedAddress?.id === this.daId || selectedAddress?.daId === this.daId) {
					this.$setLocalStorage(this.user.id, {
						...this.deliveryAddress,
						userId: this.user.id,
						daId: this.daId,
						id: this.daId
					})
				}

				this.$router.push({
					path: '/userAddress',
					query: {
						businessId: this.businessId
					}
				})
			} catch (error) {
				console.error('Failed to update delivery address:', error)
			}
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

	.wrapper .form-box {
		width: 100%;
		margin-top: 12vw;
	}

	.wrapper .form-box li {
		box-sizing: border-box;
		padding: 4vw 3vw 0vw 3vw;
		display: flex;
	}

	.wrapper .form-box li .title {
		flex: 0 0 18vw;
		font-size: 3vw;
		font-weight: 700;
		color: #666;
	}

	.wrapper .form-box li .content {
		flex: 1;
		display: flex;
		align-items: center;
	}

	.wrapper .form-box li .content input {
		border: none;
		outline: none;
		width: 100%;
		height: 4vw;
		font-size: 3vw;
	}

	.wrapper .button-add {
		box-sizing: border-box;
		padding: 4vw 3vw 0vw 3vw;
	}

	.wrapper .button-add button {
		width: 100%;
		height: 10vw;
		font-size: 3.8vw;
		font-weight: 700;
		border: none;
		outline: none;
		border-radius: 4px;
		color: #fff;
		background-color: #38CA73;
	}
</style>
