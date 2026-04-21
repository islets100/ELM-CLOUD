<template>
	<div class="points-test wrapper">
		<header>
			<button @click="$router.go(-1)" class="back-btn">← 返回</button>
			<span>积分测试工具</span>
		</header>

		<!-- 内容区 -->
		<div class="content">
			<!-- 警告提示 -->
			<div class="warning-banner">
				⚠️ 这是测试工具，仅用于开发和测试环境
			</div>

			<!-- 当前积分显示 -->
			<div class="current-points">
				<div class="label">当前可用积分</div>
				<div class="value">{{ availablePoints }}</div>
				<button @click="refreshPoints" class="refresh-btn">🔄 刷新</button>
			</div>

			<!-- 快速添加按钮 -->
			<div class="quick-actions">
				<h3>快速添加</h3>
				<div class="button-grid">
					<button @click="quickAdd(100)" class="quick-btn">+100</button>
					<button @click="quickAdd(500)" class="quick-btn">+500</button>
					<button @click="quickAdd(1000)" class="quick-btn">+1000</button>
					<button @click="quickAdd(5000)" class="quick-btn">+5000</button>
					<button @click="quickAdd(10000)" class="quick-btn">+10000</button>
					<button @click="quickAdd(99999)" class="quick-btn special">+99999</button>
				</div>
			</div>

			<!-- 自定义添加 -->
			<div class="custom-add">
				<h3>自定义添加</h3>
				<div class="input-row">
					<input 
						type="number" 
						v-model.number="customAmount" 
						placeholder="输入积分数量"
						min="1"
						max="999999"
					/>
					<button @click="addCustomPoints" class="add-btn" :disabled="loading">
						{{ loading ? '添加中...' : '添加积分' }}
					</button>
				</div>
			</div>

			<!-- 操作历史 -->
			<div class="history">
				<h3>操作历史</h3>
				<div class="history-list" v-if="history.length > 0">
					<div v-for="(item, index) in history" :key="index" class="history-item">
						<span class="time">{{ item.time }}</span>
						<span class="amount">+{{ item.amount }}</span>
						<span class="status" :class="item.success ? 'success' : 'fail'">
							{{ item.success ? '✓ 成功' : '✗ 失败' }}
						</span>
					</div>
				</div>
				<div v-else class="empty">暂无操作记录</div>
			</div>

			<!-- 快捷链接 -->
			<div class="quick-links">
				<h3>快捷链接</h3>
				<button @click="$router.push('/points')" class="link-btn">📊 查看积分明细</button>
				<button @click="$router.push('/points/mall')" class="link-btn">🛍️ 积分商城</button>
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
		name: 'PointsTest',
		data() {
			return {
				availablePoints: 0,
				customAmount: 1000,
				loading: false,
				history: []
			}
		},
		created() {
			this.refreshPoints()
			this.loadHistory()
		},
		components: {
			Footer
		},
		methods: {
			async refreshPoints() {
				try {
					const response = await pointsApi.getAvailable()
					if (response.data.success) {
						this.availablePoints = response.data.data || 0
					}
				} catch (err) {
					console.error('获取可用积分失败', err)
				}
			},
			async quickAdd(amount) {
				await this.addPoints(amount)
			},
			async addCustomPoints() {
				if (!this.customAmount || this.customAmount <= 0) {
					alert('请输入有效的积分数量')
					return
				}
				await this.addPoints(this.customAmount)
			},
			async addPoints(amount) {
				this.loading = true
				const startTime = new Date()
				
				try {
					const response = await pointsApi.addIntegralTest(amount)
					
					if (response.data.success) {
						// 添加成功
						this.addToHistory(amount, true)
						await this.refreshPoints()
						
						// 显示成功提示
						this.showToast(`成功添加 ${amount} 积分！`, 'success')
					} else {
						this.addToHistory(amount, false)
						this.showToast('添加失败：' + (response.data.message || '未知错误'), 'error')
					}
				} catch (err) {
					console.error('添加积分失败', err)
					this.addToHistory(amount, false)
					const errorMsg = err.response?.data?.message || '网络错误，请重试'
					this.showToast('添加失败：' + errorMsg, 'error')
				} finally {
					this.loading = false
				}
			},
			addToHistory(amount, success) {
				const now = new Date()
				const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
				
				this.history.unshift({
					time: timeStr,
					amount: amount,
					success: success
				})
				
				// 只保留最近20条记录
				if (this.history.length > 20) {
					this.history = this.history.slice(0, 20)
				}
				
				// 保存到localStorage
				this.saveHistory()
			},
			saveHistory() {
				try {
					localStorage.setItem('points_test_history', JSON.stringify(this.history))
				} catch (e) {
					console.error('保存历史记录失败', e)
				}
			},
			loadHistory() {
				try {
					const saved = localStorage.getItem('points_test_history')
					if (saved) {
						this.history = JSON.parse(saved)
					}
				} catch (e) {
					console.error('加载历史记录失败', e)
				}
			},
			showToast(message, type) {
				// 简单的提示实现
				alert(message)
			}
		}
	}
</script>

<style scoped>
	.points-test {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
		background-color: #f5f5f5;
	}

	/* Header */
	.points-test > header {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 12vw;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #fff;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 4.8vw;
		z-index: 1000;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
	}

	.back-btn {
		position: absolute;
		left: 4vw;
		background: none;
		border: none;
		color: #fff;
		font-size: 4vw;
		cursor: pointer;
		padding: 2vw;
	}

	/* Content */
	.content {
		margin-top: 12vw;
		padding: 4vw;
		padding-bottom: 14vw;
		flex: 1;
		overflow-y: auto;
	}

	/* Warning Banner */
	.warning-banner {
		background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
		color: white;
		padding: 3vw;
		border-radius: 2vw;
		text-align: center;
		font-size: 3.5vw;
		margin-bottom: 4vw;
		box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
	}

	/* Current Points */
	.current-points {
		background: white;
		border-radius: 2vw;
		padding: 4vw;
		margin-bottom: 4vw;
		text-align: center;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
	}

	.current-points .label {
		font-size: 3.5vw;
		color: #666;
		margin-bottom: 2vw;
	}

	.current-points .value {
		font-size: 10vw;
		font-weight: bold;
		color: #667eea;
		margin-bottom: 3vw;
	}

	.refresh-btn {
		background: #f0f0f0;
		border: none;
		padding: 2vw 4vw;
		border-radius: 5vw;
		font-size: 3.2vw;
		cursor: pointer;
		color: #666;
	}

	/* Quick Actions */
	.quick-actions,
	.custom-add,
	.history,
	.quick-links {
		background: white;
		border-radius: 2vw;
		padding: 4vw;
		margin-bottom: 4vw;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
	}

	h3 {
		font-size: 4vw;
		color: #333;
		margin: 0 0 3vw 0;
	}

	.button-grid {
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		gap: 2vw;
	}

	.quick-btn {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: white;
		border: none;
		padding: 3vw;
		border-radius: 2vw;
		font-size: 4vw;
		font-weight: bold;
		cursor: pointer;
		transition: transform 0.2s;
		box-shadow: 0 2px 4px rgba(102, 126, 234, 0.3);
	}

	.quick-btn:active {
		transform: scale(0.95);
	}

	.quick-btn.special {
		background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
		grid-column: span 3;
	}

	/* Custom Add */
	.input-row {
		display: flex;
		gap: 2vw;
	}

	.input-row input {
		flex: 1;
		padding: 3vw;
		border: 1px solid #ddd;
		border-radius: 2vw;
		font-size: 4vw;
	}

	.add-btn {
		background: #0097FF;
		color: white;
		border: none;
		padding: 3vw 6vw;
		border-radius: 2vw;
		font-size: 4vw;
		cursor: pointer;
		white-space: nowrap;
	}

	.add-btn:disabled {
		background: #ccc;
		cursor: not-allowed;
	}

	/* History */
	.history-list {
		max-height: 40vw;
		overflow-y: auto;
	}

	.history-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 2vw 0;
		border-bottom: 1px solid #f0f0f0;
	}

	.history-item:last-child {
		border-bottom: none;
	}

	.history-item .time {
		font-size: 3vw;
		color: #999;
	}

	.history-item .amount {
		font-size: 4vw;
		font-weight: bold;
		color: #667eea;
	}

	.history-item .status {
		font-size: 3vw;
	}

	.history-item .status.success {
		color: #52c41a;
	}

	.history-item .status.fail {
		color: #ff4d4f;
	}

	.empty {
		text-align: center;
		color: #999;
		padding: 4vw;
		font-size: 3.5vw;
	}

	/* Quick Links */
	.link-btn {
		display: block;
		width: 100%;
		background: #f0f0f0;
		border: none;
		padding: 3vw;
		border-radius: 2vw;
		font-size: 3.5vw;
		cursor: pointer;
		margin-bottom: 2vw;
		text-align: left;
	}

	.link-btn:last-child {
		margin-bottom: 0;
	}
</style>
