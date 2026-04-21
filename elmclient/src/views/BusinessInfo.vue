<template>
	<div class="wrapper">
		<!-- header部分 -->
		<header>
			<p>商家信息</p>
		</header>
		<!-- 商家logo部分 -->
		<div class="business-logo">
			<img :src="business.businessImg || '../assets/sj01.png'" alt="商家图片">
		</div>
		<!-- 商家信息部分 -->
		<div class="business-info">
			<h1>{{business.businessName}}</h1>
			<p>&#165;{{business.startPrice}}起送 &#165;{{business.deliveryPrice}}配送</p>
			<p>{{business.businessExplain}}</p>
		</div>
		<!-- 食品列表部分 -->
		<ul class="food">

			<!-- 无商品时显示空状态 -->
			<li v-if="foodArr.length === 0" class="empty-food">
				<i class="fa fa-cutlery"></i>
				<span>暂无商品，敬请期待</span>
			</li>

			<!-- 商品列表渲染 -->
			<li v-for="(item,index) in foodArr" :key="item.id">
				<div class="food-left">
					<img :src="item.foodImg || '../assets/default-food.png'" alt="商品图片">
					<div class="food-left-info">
						<h3>{{item.foodName}}</h3>
						<p>{{item.foodExplain}}</p>
						<p>&#165;{{item.foodPrice}}</p>
					</div>
				</div>
				<div class="food-right">
					<div>
						<i class="fa fa-minus-circle" @click="minus(index)" v-show="item.quantity!=0"></i>
					</div>
					<p><span v-show="item.quantity!=0">{{item.quantity}}</span></p>
					<div>
						<i class="fa fa-plus-circle" @click="add(index)"></i>
					</div>
				</div>
			</li>
		</ul>
		<!-- 购物车部分 -->
		<div class="cart">
			<div class="cart-left">
				<div class="cart-left-icon"
					:style="totalQuantity==0?'background-color:#505051;':'background-color:#3190E8;'">
					<i class="fa fa-shopping-cart"></i>
					<div class="cart-left-icon-quantity" v-show="totalQuantity!=0">{{totalQuantity}}</div>
				</div>
				<div class="cart-left-info">
					<p>&#165;{{totalPrice}}</p>
					<p>另需配送费{{business.deliveryPrice}}元</p>
				</div>
			</div>
			<div class="cart-right">
				<!--不够起送费-->
				<div class="cart-right-item" v-show="totalSettle<business.startPrice"
					style="background-color:#535356;cursor: default;">
					&#165;{{business.startPrice}}起送
				</div>
				<!--达到起送费-->
				<div class="cart-right-item" @click="toOrder" v-show="totalSettle>=business.startPrice">
					去结算
				</div>
			</div>
		</div>
	</div>
</template>

<script>
	import auth from '../utils/auth';

	export default {
		name: 'BusinessInfo',
		data() {
			return {
				businessId: null, // 先设为 null
				business: {},
				foodArr: [],
				user: null
			}
		},
		activated() {
			this.initData(); // 页面激活时初始化
		},
		created() {
			this.businessId = this.$route.query.businessId; // 动态获取
			console.log('当前 businessId =', this.businessId); // 调试

			// 从 auth 模块获取用户信息
			this.user = auth.getUserInfo();
			console.log('当前用户 =', this.user); // 调试

			this.initData(); // 初始化数据
		},
		methods: {
			// 初始化数据
			initData() {
				// 从session获取用户
				this.user = auth.getUserInfo();
				console.log('initData 中的用户 =', this.user); // 调试
				// 加载商家信息
				this.getBusinessDetail();
				// 加载商品列表
				this.getLatestFoodList();
			},

			// 获取商家信息
			getBusinessDetail() {
				console.log('请求商家信息，businessId =', this.businessId); // 输出当前商家ID
				this.$axios.get(`/api/businesses/getBusinessById?businessId=${this.businessId}`)
					.then(response => {
						console.log('商家信息接口返回:', response.data); // 输出完整返回数据
						if (response.data.success) {
							this.business = response.data.data;
							console.log('商家数据已赋值:', this.business);
						} else {
							alert('获取商家信息失败: ' + response.data.message);
						}
					})
					.catch(error => {
						console.error('获取商家信息失败:', error.response || error);
					});
			},

			// 获取商品列表
			getLatestFoodList() {
				this.$axios.get(`/api/foods/listFoodByBusinessId?businessId=${this.businessId}`)
					.then(response => {
						console.log('商品列表返回:', response.data); // <-- 看完整返回值
						// 根据实际返回格式改判断
						if (response.data.success && Array.isArray(response.data.data)) {
							this.foodArr = response.data.data.map(food => ({
								...food,
								quantity: 0,
								foodImg: food.foodImg ? food.foodImg : 'default.jpg'
							}));
							if (this.user) this.listCart();
						} else {
							console.warn('接口返回不符合预期', response.data);
						}
					}).catch(error => {
						console.error('获取商品列表失败:', error);
					});
			},


			// 同步购物车
			listCart() {
				console.log('[调试] 当前用户信息 =', this.user); // <-- 调试点1
				console.log('[调试] userId =', this.user?.userId); // <-- 调试点2
				console.log('[调试] businessId =', this.businessId); // <-- 调试点3

				this.$axios.get(`/api/carts?userId=${this.user.id}&businessId=${this.businessId}`)
					.then(response => {
						console.log('[调试] 购物车接口返回 =', response.data); // <-- 调试点4
						if (response.data.success && Array.isArray(response.data.data)) {
							// 同步购物车数量到商品列表
							response.data.data.forEach(cartItem => {
								const foodIndex = this.foodArr.findIndex(item => item.id === cartItem.food.id);
								if (foodIndex !== -1) {
									this.foodArr[foodIndex].quantity = cartItem.quantity;
								}
							});
						}
					}).catch(error => {
						console.error('同步购物车失败:', error);
					});
			},


			// 添加商品到购物车
			add(index) {
				if (!this.user) {
					alert('请先登录');
					this.$router.push('/login');
					return;
				}
				console.log('[调试] add() 当前用户信息 =', this.user); // <-- 调试点
				console.log('[调试] userId =', this.user?.userId);

				// 前端先更新数量
				this.foodArr[index].quantity += 1;
				// 调用保存接口
				this.saveCart(index);
			},

			// 从购物车减少商品
			minus(index) {
				if (!this.user) {
					alert('请先登录');
					this.$router.push('/login');
					return;
				}
				if (this.foodArr[index].quantity > 0) {
					// 前端先更新数量
					this.foodArr[index].quantity -= 1;
					// 调用更新接口
					this.updateCart(index, this.foodArr[index].quantity);
				}
			},

			// 保存购物车
			saveCart(index) {
				this.$axios.post('/api/carts/save', this.$qs.stringify({
					businessId: this.businessId,
					userId: this.user.id,
					foodId: this.foodArr[index].id
				})).then(response => {
					if (!response.data.success) {
						alert('添加购物车失败: ' + response.data.msg);
						// 回滚数量
						this.foodArr[index].quantity -= 1;
					}
				}).catch(error => {
					console.error('添加购物车失败:', error);
					// 回滚数量
					this.foodArr[index].quantity -= 1;
				});
			},

			// 更新购物车
			updateCart(index, num) {
				this.$axios.post('/api/carts/update', this.$qs.stringify({
					businessId: this.businessId,
					userId: this.user.id,
					foodId: this.foodArr[index].id,
					quantity: num
				})).then(response => {
					if (!response.data.success) {
						alert('更新购物车失败: ' + response.data.msg);
						// 回滚数量
						this.foodArr[index].quantity += 1;
					}
				}).catch(error => {
					console.error('更新购物车失败:', error);
					// 回滚数量
					this.foodArr[index].quantity += 1;
				});
			},

			// 删除购物车商品
			removeCart(index) {
				this.$axios.post('/api/carts/remove', this.$qs.stringify({
					businessId: this.businessId,
					userId: this.user.id,
					foodId: this.foodArr[index].id,
				})).then(response => {
					if (response.data.success) {
						this.foodArr[index].quantity = 0;
					} else {
						alert('删除购物车失败: ' + response.data.msg);
					}
				}).catch(error => {
					console.error('删除购物车失败:', error);
				});
			},

			// 去结算
			toOrder() {
				if (!this.user) {
					alert('请先登录');
					this.$router.push('/login');
					return;
				}
				this.$router.push({
					path: '/orders',
					query: {
						businessId: this.business.id
					}
				});
			}
		},

		computed: {
			// 食品总价格
			totalPrice() {
				return this.foodArr.reduce((total, item) => {
					return total + (item.foodPrice * item.quantity);
				}, 0);
			},
			// 食品总数量
			totalQuantity() {
				return this.foodArr.reduce((quantity, item) => {
					return quantity + item.quantity;
				}, 0);
			},
			// 结算总价格（商品总价+配送费）
			totalSettle() {
				return this.totalPrice + (this.business.deliveryPrice || 0);
			}
		}
	}
</script>

<style scoped>
	/* 样式部分保持不变 */
	/****************** 总容器 ******************/
	.wrapper {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
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

	/****************** 商家logo部分 ******************/
	.wrapper .business-logo {
		width: 100%;
		padding-top: 12vw;
	}

	.wrapper .business-logo img {
		width: 100%;
		height: 30vw;
		object-fit: cover;
	}

	/****************** 商家信息部分 ******************/
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

	/****************** 食品列表部分 ******************/
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

	/****************** 购物车部分 ******************/
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