<template>
	<div class="wrapper">
		<header>
			<p>地址管理</p>
		</header>

		<ul class="addresslist">
			<li v-for="item in deliveryAddressArr" :key="item.id">
				<div class="addresslist-left" @click="setDeliveryAddress(item)">
					<h3>{{item.contactName}}{{formatSex(item.contactSex)}} {{item.contactTel}}</h3>
					<p>{{item.address}}</p>
				</div>
				<div class="addresslist-right">
					<i class="fa fa-edit" @click="editUserAddress(item.id)"></i>
					<i class="fa fa-remove" @click="removeUserAddress(item.id)"></i>
				</div>
			</li>

		</ul>

		<div class="addbtn" @click="toAddUserAddress">
			<i class="fa fa-plus-circle"></i>
			<p>新增收货地址</p>
		</div>

		<Footer></Footer>
	</div>
</template>

<script>
	import Footer from '../components/Footer.vue';
	import auth from '../utils/auth';

	export default {
		name: 'UserAddress',
		data() {
			return {
				businessId: this.$route.query.businessId,
				user: {},
				deliveryAddressArr: []
			}
		},
		created() {
			this.user = auth.getUserInfo();
			if (!this.user) {
				alert('请先登录');
				this.$router.push('/login');
				return;
			}
			this.listDeliveryAddressByUserId();
		},
		components: {
			Footer
		},
		methods: {
			formatSex(value) {
				return value == 1 ? '先生' : '女士';
			},
			// 列出用户地址
			listDeliveryAddressByUserId() {
				if (!this.user?.id) return;
				this.$axios.get('/api/addressesByUserId', {
						params: {
							userId: this.user.id
						}
					})
					.then(res => {
						this.deliveryAddressArr = res.data.data || [];
					})
					.catch(err => console.error(err));
			},
			setDeliveryAddress(deliveryAddress) {
				if (!this.user?.id) return;
				this.$setLocalStorage(this.user.id, deliveryAddress);
				this.$router.push({
					path: '/orders',
					query: {
						businessId: this.businessId
					}
				});
			},
			toAddUserAddress() {
				this.$router.push({
					path: '/addUserAddress',
					query: {
						businessId: this.businessId
					}
				});
			},
			editUserAddress(daId) {
				if (!daId) {
					console.error('跳转失败：daId 为空或无效');
					alert('地址ID为空，无法编辑');
					return;
				}

				this.$router.push({
					name: 'EditUserAddress',
					params: {
						id: daId
					}, // ✅ 使用 daId 传递
					query: {
						businessId: this.businessId
					}
				});
			},
			removeUserAddress(daId) {
				if (!confirm('确认要删除此送货地址吗？')) return;
				this.$axios.delete(`/api/addresses/${daId}`)
					.then(res => {
						if (res.data.data > 0) {
							let deliveryAddress = this.$getLocalStorage(this.user.id);
							if (deliveryAddress?.daId === daId) {
								this.$removeLocalStorage(this.user.id);
							}
							this.listDeliveryAddressByUserId();
						} else {
							alert('删除地址失败！');
						}
					})
					.catch(err => console.error(err));
			}
		}
	}
</script>

<style scoped>
	/*************** 总容器 ***************/
	.wrapper {
		width: 100%;
		height: 100%;
		background-color: #F5F5F5;
	}

	/*************** header ***************/
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
		/*保证在最上层*/
		z-index: 1000;
	}

	/*************** addresslist ***************/
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
		/*左边这块区域是可以点击的*/
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

	/*************** 新增地址部分 ***************/
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