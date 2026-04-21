<template>
	<div class="wrapper">
		<!-- header部分 -->
		<header>
			<p>商家列表</p>
		</header>
		<!-- 商家列表部分 -->
		<ul class="business">
			<li v-for="item in businessArr" :key="item.id" @click="toBusinessInfo(item.id)">
				<div class="business-img">
					<!-- <img :src="`/images/${item.businessImg}`"> -->
					<img :src="item.businessImg || '../assets/sj01.png'">
					<div class="business-img-quantity" v-show="item.quantity>0">{{item.quantity}}</div>
				</div>
				<div class="business-info">
					<h3>{{item.businessName}}</h3>
					<p>&#165;{{item.startPrice}}起送 | &#165;{{item.deliveryPrice}}配送</p>
					<p>{{item.businessExplain}}</p>
				</div>
			</li>
		</ul>
		<!-- 底部菜单部分 -->
		<Footer></Footer>
	</div>
</template>

<script>
	import Footer from '../components/Footer.vue';

	export default {
		name: 'BusinessList',
		data() {
			return {
				orderTypeId: this.$route.query.orderTypeId,
				businessArr: [],
				user: null
			}
		},
		created() {
			this.user = this.$getSessionStorage('user');
			this.getBusinessList();
		},
		watch: {
			'$route.query.orderTypeId': function(newVal, oldVal) {
				this.loadData(); // 路由参数变化时重新加载数据
			}
		},
		components: {
			Footer
		},
		methods: {
			// 获取商家列表
			getBusinessList() {
				// 后端接口为GET请求，路径对应BusinessController的listBusinessByOrderTypeId方法
				this.$axios.get(`/api/businesses/listBusinessByOrderTypeId`, {
					params: {
						orderTypeId: this.orderTypeId
					}
				}).then(response => {
					// 后端返回格式为HttpResult，数据在data字段中
					if (response.data.success) {
						this.businessArr = response.data.data;
						// 初始化购物车数量字段
						this.businessArr.forEach(item => {
							item.quantity = 0;
						});
						// 已登录用户查询购物车
						if (this.user) {
							this.getCartList();
						}
					} else {
						console.error('获取商家列表失败:', response.data.message);
					}
				}).catch(error => {
					console.error('请求商家列表出错:', error);
				});
			},

			// 获取购物车列表
			getCartList() {
				this.$axios.get(`/api/carts`, {
					params: {
						userId: this.user.id
					}
				}).then(response => {
					if (response.data.success) {
						const cartArr = response.data.data;
						// 计算每个商家的购物车商品总数
						this.businessArr.forEach(business => {
							cartArr.forEach(cart => {
								if (cart.business.id === business.id) {
									business.quantity += cart.quantity;
								}
							});
						});
					} else {
						console.error('获取购物车失败:', response.data.message);
					}
				}).catch(error => {
					console.error('请求购物车出错:', error);
				});
			},

			toBusinessInfo(businessId) {
				this.$router.push({
					path: '/businessInfo',
					query: {
						businessId: businessId
					}
				});
			}
		}
	}
</script>

<style scoped>
	/* 样式保持不变 */
	/****************** 总容器 ******************/
	.wrapper {
		width: 100%;
		height: 100%;
	}

	/****************** header部分 ******************/
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

	/****************** 商家列表部分 ******************/
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