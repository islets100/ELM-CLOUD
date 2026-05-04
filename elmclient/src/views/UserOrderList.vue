<template>
	<div class="wrapper">
		<header>
			<p>我的订单</p>
		</header>

		<h3>待支付订单</h3>
		<ul class="order">
			<li v-for="item in unpaidOrders" :key="item.id">
				<div class="order-info">
					<p>
						{{ item.business.businessName }}
						<i class="fa fa-caret-down" @click="detailetShow(item)"></i>
					</p>
					<div class="order-info-right">
						<p>&#165;{{ item.orderTotal }}</p>
						<div class="order-info-right-icon" @click="goToPayment(item)">去支付</div>
					</div>
				</div>
				<ul class="order-detailet" v-show="item.isShowDetailet">
					<li v-for="odItem in item.list" :key="odItem.id || odItem.foodId">
						<p>{{ odItem.food?.foodName || '商品' }} x {{ odItem.quantity }}</p>
						<p>&#165;{{ (odItem.food?.foodPrice || 0) * odItem.quantity }}</p>
					</li>
					<li>
						<p>配送费</p>
						<p>&#165;{{ item.business.deliveryPrice || 0 }}</p>
					</li>
				</ul>
			</li>
		</ul>

		<h3>已支付订单</h3>
		<ul class="order">
			<li v-for="item in finishedOrders" :key="item.id">
				<div class="order-info">
					<p>
						{{ item.business.businessName }}
						<span class="order-status">{{ getStatusText(item.orderState) }}</span>
						<i class="fa fa-caret-down" @click="detailetShow(item)"></i>
					</p>
					<div class="order-info-right">
						<p>&#165;{{ item.orderTotal }}</p>
						<div v-if="item.orderState === 3" class="order-info-right-icon confirm-receipt-btn" @click="confirmReceipt(item)">
							确认收货
						</div>
						<div v-if="item.orderState === 4" class="order-info-right-icon confirmed-receipt-btn">
							已完成
						</div>
					</div>
				</div>
				<ul class="order-detailet" v-show="item.isShowDetailet">
					<li v-for="odItem in item.list" :key="odItem.id || odItem.foodId">
						<p>{{ odItem.food?.foodName || '商品' }} x {{ odItem.quantity }}</p>
						<p>&#165;{{ (odItem.food?.foodPrice || 0) * odItem.quantity }}</p>
					</li>
					<li>
						<p>配送费</p>
						<p>&#165;{{ item.business.deliveryPrice || 0 }}</p>
					</li>
					<li v-if="item.orderState === 4 && !item.hasCommented">
						<div class="comment-section">
							<p class="comment-title">评价后可获得积分奖励</p>
							<textarea
								v-model="item.commentContent"
								class="comment-textarea"
								placeholder="输入评价内容，字数满 15 可获得积分"
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
									<span>上传图片</span>
								</label>
								<div class="comment-preview" v-if="item.commentImages && item.commentImages.length">
									<img
										v-for="(img, idx) in item.commentImages"
										:key="idx"
										:src="img.preview"
										alt="评价图片预览"
									/>
								</div>
							</div>
							<div class="comment-footer">
								<span class="comment-tip">15 字以上奖励 5 积分，带图奖励 10 积分。</span>
								<button class="comment-submit" @click="submitComment(item)">
									提交评价
								</button>
							</div>
						</div>
					</li>
				</ul>
			</li>
		</ul>

		<Footer></Footer>
	</div>
</template>

<script>
import Footer from '../components/Footer.vue'
import auth from '../utils/auth'
import pointsApi from '../api/points.js'

export default {
	name: 'OrderList',
	components: {
		Footer
	},
	data() {
		return {
			orderArr: [],
			user: {}
		}
	},
	async created() {
		this.user = auth.getUserInfo()
		if (!this.user?.id) {
			alert('请先登录')
			this.$router.push('/login')
			return
		}

		await this.loadOrders()
	},
	computed: {
		unpaidOrders() {
			return this.orderArr.filter(item => item.orderState === 0)
		},
		finishedOrders() {
			return this.orderArr.filter(item => item.orderState !== 0)
		}
	},
	methods: {
		detailetShow(order) {
			order.isShowDetailet = !order.isShowDetailet
		},

		goToPayment(item) {
			if (!item?.id) {
				return
			}
			this.$router.push({
				path: '/payment',
				query: {
					orderId: item.id
				}
			})
		},

		getStatusText(orderState) {
			switch (orderState) {
				case 1:
					return '已支付'
				case 2:
					return '配送中'
				case 3:
					return '待收货'
				case 4:
					return '已完成'
				default:
					return '处理中'
			}
		},

		async confirmReceipt(item) {
			if (!item?.id) return

			try {
				const response = await this.$axios.put(`/api/orders/${item.id}/confirm-receipt`)
				if (response.data.success) {
					item.orderState = 4
					alert('确认收货成功')
				} else {
					alert(response.data.message || '确认收货失败')
				}
			} catch (error) {
				console.error('确认收货失败:', error)
				alert(error.response?.data?.message || '确认收货失败，请重试')
			}
		},

		async loadOrders() {
			try {
				const response = await this.$axios.get(`/api/orders/user/${this.user.id}`)
				if (!response.data.success) {
					this.orderArr = []
					return
				}

				const orders = response.data.data || []
				const businessIds = [...new Set(orders.map(order => order.businessId).filter(Boolean))]
				const foodIds = [
					...new Set(
						orders.flatMap(order => (order.orderDetails || []).map(detail => detail.foodId).filter(Boolean))
					)
				]

				const [businessResponses, foodResponses] = await Promise.all([
					Promise.all(businessIds.map(id => this.$axios.get(`/api/businesses/${id}`))),
					Promise.all(foodIds.map(id => this.$axios.get(`/api/foods/${id}`)))
				])

				const businessMap = {}
				businessResponses.forEach(item => {
					if (item.data.success && item.data.data) {
						businessMap[item.data.data.id] = item.data.data
					}
				})

				const foodMap = {}
				foodResponses.forEach(item => {
					if (item.data.success && item.data.data) {
						foodMap[item.data.data.id] = item.data.data
					}
				})

				this.orderArr = orders.map(order => {
					const details = (order.orderDetails || []).map(detail => ({
						...detail,
						food: foodMap[detail.foodId] || null
					}))

					return {
						...order,
						business: businessMap[order.businessId] || {},
						orderDetails: details,
						list: details,
						isShowDetailet: false,
						commentContent: '',
						commentImages: [],
						hasCommented: false
					}
				})
			} catch (error) {
				console.error('Failed to load orders:', error)
				this.orderArr = []
			}
		},

		handleCommentImageChange(event, item) {
			const files = Array.from(event.target.files || [])
			if (!files.length) {
				item.commentImages = []
				return
			}

			const limitedFiles = files.slice(0, 3)
			item.commentImages = []
			limitedFiles.forEach(file => {
				const reader = new FileReader()
				reader.onload = loadEvent => {
					item.commentImages.push({
						file,
						preview: loadEvent.target.result
					})
				}
				reader.readAsDataURL(file)
			})
		},

		async submitComment(item) {
			if (!item?.id) {
				return
			}
			if (item.hasCommented) {
				alert('该订单已经评价过了')
				return
			}

			const content = (item.commentContent || '').trim()
			const hasPicture = Array.isArray(item.commentImages) && item.commentImages.length > 0

			if (!content && !confirm('未填写评价内容将无法获得积分，确认继续提交吗？')) {
				return
			}

			try {
				const response = await pointsApi.comment(item.id, content, hasPicture)
				if (!response.data.success) {
					alert(response.data.message || '评价失败')
					return
				}

				item.hasCommented = true
				item.commentContent = ''
				item.commentImages = []
				const points = response.data.data || 0
				alert(points > 0 ? `评价成功，获得 ${points} 积分` : '评价成功')
			} catch (error) {
				console.error('Failed to submit comment:', error)
				alert(error.response?.data?.message || '评价失败')
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
