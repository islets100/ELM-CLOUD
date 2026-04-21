<template>
	<div class="wrapper">

		<!--header部分-->
		<header>
			<p>编辑送货地址</p>
		</header>

		<!--表单部分-->
		<ul class="form-box">
			<li>
				<div class="title">
					联系人：
				</div>
				<div class="content">
					<input type="text" v-model="deliveryAddress.contactName" placeholder="联系人姓名">
				</div>
			</li>
			<li>
				<div class="title">
					性别：
				</div>
				<div class="content" style="font-size: 3vw;">
					<input type="radio" v-model="deliveryAddress.contactSex" value="1" style="width:6vw;height:3.2vw;">男
					<input type="radio" v-model="deliveryAddress.contactSex" value="0" style="width:6vw;height:3.2vw;">女
				</div>
			</li>
			<li>
				<div class="title">
					电话：
				</div>
				<div class="content">
					<input type="tel" v-model="deliveryAddress.contactTel" placeholder="电话">
				</div>
			</li>
			<li>
				<div class="title">
					收货地址：
				</div>
				<div class="content">
					<input type="text" v-model="deliveryAddress.address" placeholder="收货地址">
				</div>
			</li>
		</ul>

		<div class="button-add">
			<button @click="updateUserAddress">更新</button>
		</div>

		<!--底部菜单部分-->
		<Footer></Footer>

	</div>
</template>

<script>
	import Footer from '../components/Footer.vue';
	import auth from '../utils/auth';

	export default {
		name: 'EditUserAddress',
		data() {
			return {
				businessId: this.$route.query.businessId,
				user: {},
				daId: null,
				deliveryAddress: {
					contactName: '',
					contactSex: 1,
					contactTel: '',
					address: ''
				}
			}
		},
		created() {
			// 获取当前用户
			this.user = auth.getUserInfo();
			if (!this.user) {
				alert('请先登录');
				this.$router.push('/login');
				return;
			}

			// 从路由 params 获取 id
			this.daId = this.$route.params.id;
			if (!this.daId) {
				alert('无法获取要编辑的地址ID');
				return;
			}

			const daIdNumber = Number(this.daId);
			if (isNaN(daIdNumber)) {
				alert('地址ID不合法');
				return;
			}

			// 获取当前要编辑的地址信息
			this.$axios.get('/api/addressById', {
				params: {
					daId: daIdNumber
				}
			}).then(res => {
				if (res.data.success) {
					// 后端返回的 data 是对象
					this.deliveryAddress = res.data.data;
				} else {
					alert('获取地址失败：' + res.data.msg);
				}
			}).catch(err => {
				console.error(err);
				alert('网络错误，无法获取地址信息');
			});
		},
		components: {
			Footer
		},
		methods: {
			updateUserAddress() {
				// 校验
				if (!this.deliveryAddress.contactName) {
					alert('联系人姓名不能为空！');
					return;
				}
				if (!this.deliveryAddress.contactTel) {
					alert('联系人电话不能为空！');
					return;
				}
				if (!this.deliveryAddress.address) {
					alert('联系人地址不能为空！');
					return;
				}

				// 构建表单参数
				const params = this.$qs.stringify({
					daId: Number(this.daId), // ⚠️ 保持 daId 和后端一致
					contactName: this.deliveryAddress.contactName,
					contactSex: Number(this.deliveryAddress.contactSex),
					contactTel: this.deliveryAddress.contactTel,
					address: this.deliveryAddress.address,
					userId: Number(this.user.id)
				});

				// 调用后端接口更新地址
				this.$axios.post('/api/addresses/update', params, {
						headers: {
							'Content-Type': 'application/x-www-form-urlencoded'
						}
					})
					.then(res => {
						if (res.data.success) {
							this.$router.push({
								path: '/userAddress',
								query: {
									businessId: this.businessId
								}
							});
						} else {
							alert('更新地址失败：' + res.data.msg);
						}
					})
					.catch(err => {
						console.error(err);
						alert('网络错误，请稍后重试');
					});
			}
		}
	}
</script>


<style scoped>
	/*************** 总容器 ***************/
	.wrapper {
		width: 100%;
		height: 100%;
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

	/*************** （表单信息） ***************/
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