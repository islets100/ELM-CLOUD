<template>
	<div class="points-mall wrapper">
		<header>
			<button @click="$router.go(-1)" class="back-btn">
				<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
					<path d="M19 12H5M12 19l-7-7 7-7"/>
				</svg>
			</button>
			<span>积分商城</span>
			<div class="header-placeholder"></div>
		</header>

		<!-- 内容区 -->
		<div class="content">
			<!-- 当前积分显示 -->
			<div class="points-banner">
				<div class="banner-bg"></div>
				<div class="banner-content">
					<div class="points-info">
						<div class="points-icon">💎</div>
						<div class="points-text">
							<div class="points-label">我的积分</div>
							<div class="points-value">{{ availablePoints.toLocaleString() }}</div>
						</div>
					</div>
					<button @click="$router.push('/points')" class="view-details-btn">
						<span>明细</span>
						<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
							<path d="M9 18l6-6-6-6"/>
						</svg>
					</button>
				</div>
			</div>

			<!-- 分类标签 -->
			<div class="category-tabs">
				<div class="tab active">🔥 热门</div>
				<div class="tab">🎵 音乐</div>
				<div class="tab">📺 视频</div>
				<div class="tab">🎓 学习</div>
			</div>

			<!-- 商品列表 -->
			<div class="goods-list">
				<div 
					v-for="item in goodsList" 
					:key="item.id" 
					class="goods-card"
					@click="showPurchaseDialog(item)"
				>
					<div class="card-badge" v-if="item.points > 5000">🔥</div>
					<div class="card-image">
						<img 
							:src="item.image" 
							:alt="item.name"
							@error="handleImageError"
							loading="lazy"
						/>
					</div>
					<div class="card-content">
						<div class="goods-name">{{ item.name }}</div>
						<div class="goods-desc">{{ item.description }}</div>
						<div class="card-footer">
							<div class="goods-points">
								<span class="points-icon">💎</span>
								<span class="points-value">{{ item.points.toLocaleString() }}</span>
							</div>
							<div class="exchange-btn">兑换</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 购买确认弹窗 -->
		<div v-if="showDialog" class="dialog-overlay" @click="closeDialog">
			<div class="dialog-box" @click.stop>
				<button @click="closeDialog" class="dialog-close">
					<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<path d="M18 6L6 18M6 6l12 12"/>
					</svg>
				</button>
				
				<div class="dialog-goods">
					<div class="dialog-image">
						<img 
							:src="selectedGoods.image" 
							:alt="selectedGoods.name"
							@error="handleImageError"
						/>
					</div>
					<div class="dialog-info">
						<h3>{{ selectedGoods.name }}</h3>
						<p>{{ selectedGoods.description }}</p>
					</div>
				</div>

				<div class="dialog-points">
					<div class="point-row">
						<span class="label">兑换所需</span>
						<span class="value cost">
							<span class="icon">💎</span>
							{{ selectedGoods.points.toLocaleString() }}
						</span>
					</div>
					<div class="point-row">
						<span class="label">当前积分</span>
						<span class="value">{{ availablePoints.toLocaleString() }}</span>
					</div>
					<div class="divider"></div>
					<div class="point-row">
						<span class="label">剩余积分</span>
						<span class="value" :class="{ 'insufficient': availablePoints < selectedGoods.points }">
							{{ (availablePoints - selectedGoods.points).toLocaleString() }}
						</span>
					</div>
				</div>

				<div class="dialog-actions">
					<button @click="closeDialog" class="btn-cancel">取消</button>
					<button 
						@click="confirmPurchase" 
						class="btn-confirm"
						:disabled="availablePoints < selectedGoods.points || purchasing"
					>
						<span v-if="!purchasing">立即兑换</span>
						<span v-else>兑换中...</span>
					</button>
				</div>
			</div>
		</div>

		<!-- 底部菜单 -->
		<Footer></Footer>
	</div>
</template>

<script>
	import Footer from '../components/Footer.vue'
	import pointsApi from '../api/points.js'

	export default {
		name: 'PointsMall',
		data() {
			return {
				availablePoints: 0,
				showDialog: false,
				selectedGoods: {},
				purchasing: false,
				goodsList: [
					{
						id: 1,
						name: 'QQ音乐绿钻',
						description: '畅享千万正版音乐，尊享绿钻特权',
						points: 500,
						image: '/images/mall/qq-music.jpg'
					},
					{
						id: 2,
						name: 'Bilibili大会员',
						description: '专属身份标识，海量番剧免费看',
						points: 800,
						image: '/images/mall/bilibili.jpg'
					},
					{
						id: 3,
						name: '百度网盘SVIP',
						description: '超大空间，极速下载，畅享云端',
						points: 1000,
						image: '/images/mall/baidu-pan.jpg'
					},
					{
						id: 4,
						name: '爱奇艺黄金VIP',
						description: '热播大剧抢先看，广告跳过特权',
						points: 600,
						image: '/images/mall/iqiyi.jpg'
					},
					{
						id: 5,
						name: '腾讯视频VIP',
						description: '独播好剧，超前点播特权',
						points: 650,
						image: '/images/mall/tencent-video.jpg'
					},
					{
						id: 6,
						name: '网易云音乐黑胶VIP',
						description: '无损音质，海量曲库随心听',
						points: 700,
						image: '/images/mall/netease-music.jpg'
					},
					{
						id: 7,
						name: '喜马拉雅VIP',
						description: '海量有声书，知识付费内容畅听',
						points: 550,
						image: '/images/mall/ximalaya.jpg'
					},
					{
						id: 8,
						name: '天津大学毕业证',
						description: '985名校毕业证书，走上人生巅峰从此开始',
						points: 9999,
						image: '/images/mall/tju-diploma.jpg'
					},
					{
						id: 9,
						name: '测试商品',
						description: '仅需1积分，用于测试积分兑换功能',
						points: 1,
						image: '/images/mall/test-product.jpg'
					}
				]
			}
		},
		created() {
			this.loadAvailablePoints()
		},
		components: {
			Footer
		},
		methods: {
			async loadAvailablePoints() {
				try {
					const response = await pointsApi.getAvailable()
					if (response.data.success) {
						this.availablePoints = response.data.data || 0
					}
				} catch (err) {
					console.error('获取可用积分失败', err)
				}
			},
			showPurchaseDialog(goods) {
				this.selectedGoods = goods
				this.showDialog = true
			},
			closeDialog() {
				this.showDialog = false
				this.selectedGoods = {}
			},
			handleImageError(e) {
				// 图片加载失败时使用占位图
				e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9ImciIHgxPSIwJSIgeTE9IjAlIiB4Mj0iMTAwJSIgeTI9IjEwMCUiPjxzdG9wIG9mZnNldD0iMCUiIHN0eWxlPSJzdG9wLWNvbG9yOiM2NjdlZWE7c3RvcC1vcGFjaXR5OjEiIC8+PHN0b3Agb2Zmc2V0PSIxMDAlIiBzdHlsZT0ic3RvcC1jb2xvcjojNzY0YmEyO3N0b3Atb3BhY2l0eToxIiAvPjwvbGluZWFyR3JhZGllbnQ+PC9kZWZzPjxyZWN0IHdpZHRoPSI0MDAiIGhlaWdodD0iNDAwIiBmaWxsPSJ1cmwoI2cpIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iNjAiIGZpbGw9IndoaXRlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSIgb3BhY2l0eT0iMC44Ij7wn5OOPE90ZXh0Pjwvc3ZnPg=='
			},
			async confirmPurchase() {
				if (this.availablePoints < this.selectedGoods.points) {
					alert('积分不足，无法兑换')
					return
				}

				this.purchasing = true
				try {
					// 调用积分消费接口
					const response = await pointsApi.consumeIntegral(
						this.selectedGoods.points,
						'EXCHANGE',
						this.selectedGoods.id,
						`兑换商品：${this.selectedGoods.name}`
					)

					if (response.data.success) {
						alert(`兑换成功！您已成功兑换 ${this.selectedGoods.name}`)
						// 刷新可用积分
						await this.loadAvailablePoints()
						this.closeDialog()
					} else {
						alert('兑换失败：' + (response.data.message || '未知错误'))
					}
				} catch (err) {
					console.error('兑换失败', err)
					const errorMsg = err.response?.data?.message || '网络错误，请重试'
					alert('兑换失败：' + errorMsg)
				} finally {
					this.purchasing = false
				}
			}
		}
	}
</script>

<style scoped>
	.points-mall {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
		background: linear-gradient(180deg, #f8f9ff 0%, #f5f5f5 100%);
	}

	/* Header */
	.points-mall > header {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 14vw;
		background-color: #0097FF;
		color: #fff;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 4vw;
		font-size: 4.8vw;
		font-weight: 600;
		z-index: 1000;
		box-shadow: 0 2px 12px rgba(0, 151, 255, 0.3);
	}

	.back-btn {
		background: rgba(255, 255, 255, 0.2);
		border: none;
		color: #fff;
		cursor: pointer;
		padding: 2vw;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		width: 9vw;
		height: 9vw;
		transition: all 0.3s;
	}

	.back-btn:active {
		background: rgba(255, 255, 255, 0.3);
		transform: scale(0.95);
	}

	.header-placeholder {
		width: 9vw;
	}

	/* Content */
	.content {
		margin-top: 14vw;
		padding-bottom: 16vw;
		flex: 1;
		overflow-y: auto;
	}

	/* Points Banner */
	.points-banner {
		position: relative;
		margin: 4vw;
		margin-top: 0;
		border-radius: 4vw;
		overflow: hidden;
		box-shadow: 0 4px 20px rgba(102, 126, 234, 0.25);
	}

	.banner-bg {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: #0097FF;
		opacity: 1;
	}

	.banner-content {
		position: relative;
		padding: 5vw;
		display: flex;
		justify-content: space-between;
		align-items: center;
		color: white;
	}

	.points-info {
		display: flex;
		align-items: center;
		gap: 3vw;
	}

	.points-icon {
		font-size: 10vw;
		filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
	}

	.points-text {
		display: flex;
		flex-direction: column;
	}

	.points-label {
		font-size: 3.2vw;
		opacity: 0.9;
		margin-bottom: 1vw;
		font-weight: 500;
	}

	.points-value {
		font-size: 7vw;
		font-weight: bold;
		letter-spacing: 0.5px;
	}

	.view-details-btn {
		background: rgba(255, 255, 255, 0.25);
		backdrop-filter: blur(10px);
		border: 1px solid rgba(255, 255, 255, 0.3);
		color: white;
		padding: 2.5vw 4vw;
		border-radius: 6vw;
		font-size: 3.2vw;
		cursor: pointer;
		display: flex;
		align-items: center;
		gap: 1vw;
		transition: all 0.3s;
		font-weight: 500;
	}

	.view-details-btn:active {
		background: rgba(255, 255, 255, 0.35);
		transform: scale(0.95);
	}

	/* Category Tabs */
	.category-tabs {
		display: flex;
		gap: 2vw;
		padding: 3vw 4vw;
		overflow-x: auto;
		-webkit-overflow-scrolling: touch;
	}

	.category-tabs::-webkit-scrollbar {
		display: none;
	}

	.tab {
		flex-shrink: 0;
		padding: 2vw 4vw;
		background: white;
		border-radius: 5vw;
		font-size: 3.5vw;
		cursor: pointer;
		transition: all 0.3s;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
	}

	.tab.active {
		background-color: #0097FF;
		color: white;
		box-shadow: 0 4px 12px rgba(0, 151, 255, 0.3);
	}

	/* Goods List */
	.goods-list {
		padding: 0 4vw 4vw;
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 3vw;
	}

	.goods-card {
		position: relative;
		background: white;
		border-radius: 3vw;
		overflow: hidden;
		cursor: pointer;
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
	}

	.goods-card:active {
		transform: scale(0.97);
	}

	.card-badge {
		position: absolute;
		top: 2vw;
		right: 2vw;
		background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
		color: white;
		padding: 1vw 2.5vw;
		border-radius: 3vw;
		font-size: 3vw;
		z-index: 1;
		box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
	}

	.card-image {
		width: 100%;
		height: 35vw;
		background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		overflow: hidden;
	}

	.card-image img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.card-content {
		padding: 3vw;
	}

	.goods-name {
		font-size: 3.8vw;
		font-weight: 600;
		color: #333;
		margin-bottom: 1.5vw;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.goods-desc {
		font-size: 2.8vw;
		color: #999;
		margin-bottom: 3vw;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		-webkit-box-orient: vertical;
		line-height: 1.4;
		height: 7.8vw;
	}

	.card-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.goods-points {
		display: flex;
		align-items: baseline;
		gap: 1vw;
	}

	.goods-points .points-icon {
		font-size: 3.5vw;
	}

	.goods-points .points-value {
		font-size: 4.2vw;
		font-weight: bold;
		color: #0097FF;
	}

	.exchange-btn {
		background-color: #0097FF;
		color: white;
		padding: 1.5vw 3.5vw;
		border-radius: 4vw;
		font-size: 3vw;
		font-weight: 500;
		box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
	}

	/* Dialog */
	.dialog-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.6);
		backdrop-filter: blur(4px);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 2000;
		animation: fadeIn 0.3s ease;
	}

	@keyframes fadeIn {
		from {
			opacity: 0;
		}
		to {
			opacity: 1;
		}
	}

	.dialog-box {
		background: white;
		border-radius: 5vw;
		width: 85vw;
		max-width: 500px;
		overflow: hidden;
		position: relative;
		animation: slideUp 0.3s ease;
		box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
	}

	@keyframes slideUp {
		from {
			transform: translateY(50px);
			opacity: 0;
		}
		to {
			transform: translateY(0);
			opacity: 1;
		}
	}

	.dialog-close {
		position: absolute;
		top: 3vw;
		right: 3vw;
		background: rgba(0, 0, 0, 0.05);
		border: none;
		border-radius: 50%;
		width: 8vw;
		height: 8vw;
		display: flex;
		align-items: center;
		justify-content: center;
		cursor: pointer;
		z-index: 1;
		transition: all 0.3s;
	}

	.dialog-close:active {
		background: rgba(0, 0, 0, 0.1);
		transform: scale(0.9);
	}

	.dialog-goods {
		padding: 5vw;
		padding-top: 6vw;
	}

	.dialog-image {
		width: 100%;
		height: 50vw;
		border-radius: 3vw;
		overflow: hidden;
		margin-bottom: 4vw;
		background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
		box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
	}

	.dialog-image img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.dialog-info h3 {
		font-size: 5vw;
		font-weight: 600;
		color: #333;
		margin: 0 0 2vw 0;
	}

	.dialog-info p {
		font-size: 3.5vw;
		color: #666;
		margin: 0;
		line-height: 1.5;
	}

	.dialog-points {
		background: #f8f9ff;
		padding: 4vw;
		margin: 0 5vw 5vw;
		border-radius: 3vw;
	}

	.point-row {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 3vw;
	}

	.point-row:last-child {
		margin-bottom: 0;
	}

	.point-row .label {
		font-size: 3.5vw;
		color: #666;
	}

	.point-row .value {
		font-size: 4.5vw;
		font-weight: 600;
		color: #333;
	}

	.point-row .value.cost {
		color: #0097FF;
		display: flex;
		align-items: center;
		gap: 1vw;
	}

	.point-row .value.cost .icon {
		font-size: 4vw;
	}

	.point-row .value.insufficient {
		color: #ff6b6b;
	}

	.divider {
		height: 1px;
		background: #e0e0e0;
		margin: 3vw 0;
	}

	.dialog-actions {
		display: flex;
		gap: 3vw;
		padding: 0 5vw 5vw;
	}

	.btn-cancel,
	.btn-confirm {
		flex: 1;
		padding: 3.5vw;
		border-radius: 3vw;
		font-size: 4vw;
		font-weight: 600;
		cursor: pointer;
		border: none;
		transition: all 0.3s;
	}

	.btn-cancel {
		background: #f0f0f0;
		color: #666;
	}

	.btn-cancel:active {
		background: #e0e0e0;
		transform: scale(0.98);
	}

	.btn-confirm {
		background-color: #0097FF;
		color: white;
		box-shadow: 0 4px 16px rgba(0, 151, 255, 0.4);
	}

	.btn-confirm:active {
		transform: scale(0.98);
		box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
	}

	.btn-confirm:disabled {
		background: #ccc;
		box-shadow: none;
		cursor: not-allowed;
		opacity: 0.6;
	}
</style>
