<template>
	<div class="wrapper">
		<!-- header部分 -->
		<header>
			<p>我的订单</p>
		</header>
		<!-- 订单列表部分 -->
		<h3>未支付订单信息：</h3>
		<ul class="order">
			<li v-for="item in orderArr" v-show="item.orderState==0">
				<div class="order-info">
					<p>
						{{item.business.businessName}}
						<i class="fa fa-caret-down" @click="detailetShow(item)"></i>
					</p>
					<div class="order-info-right">
						<p>&#165;{{item.orderTotal}}</p>
						<div class="order-info-right-icon" @click="goToPayment(item)">去支付</div>
					</div>
				</div>
				<ul class="order-detailet" v-show="item.isShowDetailet">
					<li v-for="odItem in item.list">
						<p>{{odItem.food.foodName}} x {{odItem.quantity}}</p>
						<p>&#165;{{odItem.food.foodPrice * odItem.quantity}}</p>
					</li>

					<li>
						<p>配送费</p>
						<p>&#165;{{item.business.deliveryPrice}}</p>
					</li>
				</ul>
			</li>

		</ul>
		<h3>已支付订单信息：</h3>
		<ul class="order">
			<li v-for="item in orderArr" v-show="item.orderState==1 || item.orderState==2 || item.orderState==3 || item.orderState==4">
				<div class="order-info">
					<p>
						{{item.business.businessName}}
						<span class="order-status">{{getStatusText(item.orderState)}}</span>
						<i class="fa fa-caret-down" @click="detailetShow(item)"></i>
					</p>
					<div class="order-info-right">
						<p>&#165;{{item.orderTotal}}</p>
						<!-- 仅在订单状态为3（已完成）时显示确认收货按钮 -->
						<div v-if="item.orderState==3" class="order-info-right-icon confirm-receipt-btn" @click="confirmReceipt(item)">
							确认收货
						</div>
						<!-- 订单状态为4（已确认收货）时显示已确认收货按钮（不可点击），并可进行评价 -->
						<div v-if="item.orderState==4" class="order-info-right-icon confirmed-receipt-btn">
							已确认收货
						</div>
					</div>
				</div>
				<ul class="order-detailet" v-show="item.isShowDetailet">
					<li v-for="odItem in item.list">
						<p>{{odItem.food.foodName}} x {{odItem.quantity}}</p>
						<p>&#165;{{odItem.food.foodPrice * odItem.quantity}}</p>
					</li>

					<li>
						<p>配送费</p>
						<p>&#165;{{item.business.deliveryPrice}}</p>
					</li>

					<!-- 已确认收货订单：显示评价区域（仅前端展示，不落库） -->
					<li v-if="item.orderState==4 && !item.hasCommented">
						<div class="comment-section">
							<p class="comment-title">本次购物体验如何，来分享一下吧！</p>
							<textarea
								v-model="item.commentContent"
								class="comment-textarea"
								placeholder="分享一下本次下单的体验，15字以上可获得积分~"
								rows="3"
							></textarea>
							<div class="comment-upload">
								<label class="upload-label">
									<input
										type="file"
										accept="image/*"
										multiple
										@change="handleCommentImageChange($event, item)"
									/>
									<span>上传图片（可选）</span>
								</label>
								<div class="comment-preview" v-if="item.commentImages && item.commentImages.length">
									<img
										v-for="(img, idx) in item.commentImages"
										:key="idx"
										:src="img.preview"
										alt="评论图片预览"
									/>
								</div>
							</div>
							<div class="comment-footer">
								<span class="comment-tip">
									15字以上不带图 5 积分，15字以上带图 10 积分，15字以下 0 积分（积分有效期一年）
								</span>
								<button class="comment-submit" @click="submitComment(item)">
									提交评价并领取积分
								</button>
							</div>
						</div>
					</li>
				</ul>
			</li>

		</ul>
		<!-- 底部菜单部分 -->
		<Footer></Footer>

	</div>
</template>

<script>
	import Footer from '../components/Footer.vue';
	import auth from '../utils/auth';
	import pointsApi from '../api/points.js';

	export default {
		name: 'OrderList',
		data() {
			return {
				orderArr: [],
				user: {}
			}
		},
		created() {
			this.user = auth.getUserInfo();
			if (!this.user) {
				alert('请先登录');
				this.$router.push('/login');
				return;
			}

			this.loadOrders();
		},
		methods: {
			detailetShow(orders) {
				orders.isShowDetailet = !orders.isShowDetailet;
			},
			goToPayment(item) {
				console.log('传递的 item:', item); // 打印完整的 item 对象，检查里面的 orderId
				if (item && item.id) {
					console.log('传递的 orderId:', item.id); // 打印传递的 orderId
					this.$router.push({
						path: '/payment',
						query: {
							orderId: item.id
						}
					});
				} else {
					console.error('无效的 orderId');
					alert('无效的订单ID');
				}
			},
			getStatusText(orderState) {
				switch(orderState) {
					case 1: return '待接单';
					case 2: return '已接单';
					case 3: return '已完成';
					case 4: return '已确认收货';
					default: return '未知状态';
				}
			},
			confirmReceipt(item) {
				if (!item || !item.id) {
					alert('无效的订单ID');
					return;
				}

				// 确认收货
				if (!confirm('确认收货后，商家将收到货款，是否确认收货？')) {
					return;
				}

				this.$axios.post(`/api/orders/${item.id}/confirm`)
					.then(response => {
						if (response.data.success) {
							alert('确认收货成功！');
							// 更新订单状态
							item.orderState = 4;
							// 重新加载订单列表以刷新显示
							this.loadOrders();
						} else {
							alert(response.data.message || '确认收货失败，请重试');
						}
					})
					.catch(error => {
						console.error('确认收货出错:', error);
						alert(error.response?.data?.message || '确认收货出错，请稍后再试');
					});
			},
			loadOrders() {
				// 请求用户订单
				this.$axios.get('/api/orders', {
					params: {
						userId: this.user.id
					}
				}).then(response => {
					console.log('订单接口返回:', response.data); // 调试用
					if (response.data.success) {
						let result = response.data.data;
						this.orderArr = result.map(order => {
							console.log('每个订单的 orderId:', order.id); // 确保每个订单的 ID 有值
							order.isShowDetailet = false;
							order.list = order.orderDetails;
							// 评价相关临时字段，仅前端使用
							order.commentContent = '';
							order.commentImages = [];
							order.hasCommented = false; // 本次会话是否已评价
							return order;
						});
					} else {
						this.orderArr = [];
					}
				}).catch(error => {
					console.error(error);
					this.orderArr = [];
				});
			},
			// 处理评价图片选择（仅前端展示，不上传到后端）
			handleCommentImageChange(e, item) {
				const files = Array.from(e.target.files || []);
				if (!files.length) {
					item.commentImages = [];
					return;
				}
				// 仅做预览，数量适当限制（例如最多 3 张）
				const limitedFiles = files.slice(0, 3);
				item.commentImages = [];
				limitedFiles.forEach(file => {
					const reader = new FileReader();
					reader.onload = (evt) => {
						item.commentImages.push({
							file,
							preview: evt.target.result
						});
					};
					reader.readAsDataURL(file);
				});
			},
			// 提交评价并领取积分
			async submitComment(item) {
				if (!item || !item.id) {
					alert('无效的订单信息');
					return;
				}
				if (item.hasCommented) {
					alert('本订单已完成评价，无法重复领取积分');
					return;
				}

				const content = (item.commentContent || '').trim();
				const hasPicture = item.commentImages && item.commentImages.length > 0;

				if (!content) {
					if (!confirm('未填写任何内容，将无法获得积分，是否继续提交？')) {
						return;
					}
				}

				try {
					const resp = await pointsApi.comment(item.id, content, hasPicture);
					if (resp.data && resp.data.success) {
						const got = resp.data.data || 0;
						if (got > 0) {
							alert(`评价成功，本次获得 ${got} 积分，积分将在“我的积分”中展示，有效期一年。`);
						} else {
							alert('评价已提交，但未满足积分规则（15字以上才可获得积分）。');
						}
						// 标记为已评价，本次会话不再显示评价区
						item.hasCommented = true;
						item.commentContent = '';
						item.commentImages = [];
					} else {
						alert(resp.data.message || '提交评价失败，请稍后再试');
					}
				} catch (err) {
					console.error('提交评价失败', err);
					alert(err.response?.data?.message || '提交评价失败，请稍后再试');
				}
			}

		},
		components: {
			Footer
		}
	}
</script>


<style scoped>
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

	/****************** 历史订单列表部分 ******************/
	.wrapper h3 {
		margin-top: 12vw;
		box-sizing: border-box;
		padding: 4vw;
		font-size: 4vw;
		font-weight: 300;
		color: #999;
	}

	.wrapper .order {
		width: 100%;
	}

	.wrapper .order li {
		width: 100%;
	}

	.wrapper .order li .order-info {
		box-sizing: border-box;
		padding: 2vw 4vw;
		font-size: 4vw;
		color: #666;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.wrapper .order li .order-info .order-info-right {
		display: flex;
	}

	.wrapper .order li .order-info .order-info-right .order-info-right-icon {
		background-color: #f90;
		color: #fff;
		border-radius: 3px;
		margin-left: 2vw;
		user-select: none;
		cursor: pointer;
		padding: 1vw 2vw;
		font-size: 3.5vw;
	}

	.wrapper .order li .order-info .order-info-right .confirm-receipt-btn {
		background-color: #0097FF;
		color: #fff;
	}

	.wrapper .order li .order-info .order-info-right .confirmed-receipt-btn {
		background-color: #333;
		color: #fff;
		cursor: default;
	}

	.wrapper .order li .order-detailet {
		width: 100%;
	}

	.wrapper .order li .order-detailet li {
		width: 100%;
		box-sizing: border-box;
		padding: 1vw 4vw;
		color: #666;
		font-size: 3vw;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	/* 评论区域样式 */
	.comment-section {
		width: 100%;
		box-sizing: border-box;
		padding: 2vw 4vw 3vw;
		background-color: #f9f9f9;
		border-radius: 1vw;
		margin-top: 1vw;
	}

	.comment-title {
		font-size: 3.4vw;
		color: #333;
		margin-bottom: 2vw;
	}

	.comment-textarea {
		width: 100%;
		min-height: 16vw;
		border: 1px solid #ddd;
		border-radius: 1vw;
		padding: 2vw;
		font-size: 3.2vw;
		box-sizing: border-box;
		resize: none;
	}

	.comment-upload {
		margin-top: 2vw;
		display: flex;
		flex-direction: column;
	}

	.upload-label {
		display: inline-block;
		padding: 1.5vw 3vw;
		border-radius: 1vw;
		background-color: #fff;
		border: 1px dashed #ccc;
		color: #666;
		font-size: 3vw;
		cursor: pointer;
	}

	.upload-label input {
		display: none;
	}

	.comment-preview {
		margin-top: 2vw;
		display: flex;
		gap: 2vw;
	}

	.comment-preview img {
		width: 16vw;
		height: 16vw;
		object-fit: cover;
		border-radius: 1vw;
		border: 1px solid #eee;
	}

	.comment-footer {
		margin-top: 2vw;
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		gap: 1.5vw;
	}

	.comment-tip {
		font-size: 2.8vw;
		color: #999;
	}

	.comment-submit {
		align-self: flex-end;
		padding: 1.5vw 3vw;
		border-radius: 3px;
		background-color: #0097FF;
		color: #fff;
		border: none;
		font-size: 3.2vw;
		cursor: pointer;
	}

	/****************** 订单状态样式 ******************/
	.wrapper .order li .order-info p .order-status {
		margin-left: 2vw;
		padding: 0.5vw 2vw;
		background-color: #0097FF;
		color: #fff;
		border-radius: 3px;
		font-size: 3vw;
		font-weight: normal;
	}
</style>