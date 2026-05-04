<template>
	<div class="wallet-page wrapper">
		<header>我的钱包</header>

		<div class="content">
			<!-- 钱包余额卡片 -->
			<div class="wallet-card">
				<div class="balance-section">
					<p class="label">账户余额</p>
					<p class="balance">¥{{ wallet.balance || '0.00' }}</p>
				</div>
				<div class="info-section">
					<div class="info-item">
						<span class="info-label">可用余额</span>
						<span class="info-value">¥{{ wallet.availableBalance || '0.00' }}</span>
					</div>
					<div class="info-item">
						<span class="info-label">冻结金额</span>
						<span class="info-value">¥{{ wallet.frozenAmount || '0.00' }}</span>
					</div>
					<div class="info-item" v-if="wallet.creditLimit > 0">
						<span class="info-label">透支额度</span>
						<span class="info-value">¥{{ wallet.creditLimit || '0.00' }}</span>
					</div>
					<div class="info-item" v-if="wallet.overdraftAmount > 0">
						<span class="info-label">已透支</span>
						<span class="info-value warning">¥{{ wallet.overdraftAmount || '0.00' }}</span>
					</div>
				</div>
			</div>

			<!-- 操作按钮 -->
			<div class="action-buttons">
				<!-- 充值按钮：仅普通用户显示 -->
				<button v-if="!isBusiness" class="action-btn recharge-btn" @click="$router.push('/wallet/recharge')">
					充值
				</button>
				<!-- 提现按钮：所有用户都显示 -->
				<button class="action-btn withdraw-btn" :class="{ 'full-width': isBusiness }" @click="$router.push('/wallet/withdraw')">
					提现
				</button>
			</div>

			<!-- 钱包流水统计图 -->
			<div class="chart-container" v-if="chartData.length > 0">
				<div class="chart-title">钱包流水统计</div>
				<div ref="chartRef" class="chart"></div>
			</div>
			<div class="chart-empty" v-else>
				<p>暂无流水数据</p>
			</div>

			<!-- VIP入口 -->
			<div class="vip-banner" v-if="wallet.hasVipPrivilege" @click="$router.push('/vip/center')">
				<div class="vip-icon">
					<i class="fa fa-crown"></i>
				</div>
				<div class="vip-text">
					<h4>VIP会员中心</h4>
					<p>查看VIP权益和透支记录</p>
				</div>
				<div class="vip-arrow">
					<i class="fa fa-chevron-right"></i>
				</div>
			</div>
			<div class="vip-banner no-vip" v-else @click="$router.push('/vip/purchase')">
				<div class="vip-icon">
					<i class="fa fa-crown"></i>
				</div>
				<div class="vip-text">
					<h4>开通VIP会员</h4>
					<p>享受透支额度等专属权益</p>
				</div>
				<div class="vip-arrow">
					<i class="fa fa-chevron-right"></i>
				</div>
			</div>

			<!-- 功能菜单 -->
			<ul class="menu">
				<li @click="$router.push('/wallet/transactions')">
					<span>交易流水</span>
					<span class="arrow">></span>
				</li>
				<li @click="$router.push('/vip/center')">
					<span>VIP中心</span>
					<span class="arrow">></span>
				</li>
			</ul>
		</div>

		<!-- 用户端底部导航 -->
		<Footer v-if="!isBusiness"></Footer>
		
		<!-- 商家端底部导航 -->
		<div v-if="isBusiness" class="merchant-footer">
			<div class="footer-item" @click="$router.push('/business')">
				<i class="fa fa-home"></i>
				<span>首页</span>
			</div>
			<div class="footer-item" @click="$router.push('/businessOrders')">
				<i class="fa fa-list-alt"></i>
				<span>订单</span>
			</div>
			<div class="footer-item active" @click="$router.push('/wallet')">
				<i class="fa fa-credit-card"></i>
				<span>钱包</span>
			</div>
			<div class="footer-item" @click="$router.push('/businessManage')">
				<i class="fa fa-user"></i>
				<span>我的</span>
			</div>
		</div>
	</div>
</template>

<script>
	import Footer from '../components/Footer.vue'
	import walletApi from '../api/wallet.js'
	import auth from '../utils/auth.js'
	import * as echarts from 'echarts'

	export default {
		name: 'Wallet',
		components: {
			Footer
		},
		data() {
			return {
				wallet: {
					balance: 0,
					frozenAmount: 0,
					creditLimit: 0,
					overdraftAmount: 0,
					availableBalance: 0
				},
				loading: false,
				transactions: [],
				chartData: [],
				chartInstance: null,
				currentUserId: null,
				isBusiness: false // 是否为商家用户
			}
		},
		created() {
			// 获取当前用户ID和角色
			const user = auth.getUserInfo()
			if (user && user.id) {
				this.currentUserId = user.id
			}
			// 判断是否为商家用户
			this.isBusiness = auth.isBusiness()
			this.loadWallet()
			this.loadTransactions()
		},
		mounted() {
			this.$nextTick(() => {
				if (this.chartData.length > 0) {
					this.initChart()
				}
			})
		},
		beforeUnmount() {
			if (this.chartInstance) {
				this.chartInstance.dispose()
				this.chartInstance = null
			}
			window.removeEventListener('resize', this.handleResize)
		},
		watch: {
			chartData() {
				this.$nextTick(() => {
					if (this.chartData.length > 0) {
						this.initChart()
					}
				})
			}
		},
		methods: {
			async loadWallet() {
				this.loading = true
				try {
					const response = await walletApi.getWallet()
					if (response.data.success) {
												const raw = response.data.data || {}
							this.wallet = {
								balance: raw.balance || 0,
								availableBalance: raw.balance || 0,
								frozenAmount: 0,
								creditLimit: raw.creditLimit || 0,
								overdraftAmount: raw.usedCreditLimit || 0,
								hasVipPrivilege: (raw.creditLimit || 0) > 0
							}
					} else {
						alert('获取钱包信息失败: ' + response.data.message)
					}
				} catch (err) {
					console.error('获取钱包信息失败', err)
					alert('获取钱包信息失败，请重试')
				} finally {
					this.loading = false
				}
			},
			async loadTransactions() {
				try {
					const response = await walletApi.getTransactions()
					if (response.data.success) {
						this.transactions = response.data.data || []
						this.processTransactionData()
					} else {
						console.error('获取交易流水失败: ' + response.data.message)
					}
				} catch (err) {
					console.error('获取交易流水失败', err)
				}
			},
			processTransactionData() {
				if (!this.currentUserId || this.transactions.length === 0) {
					this.chartData = []
					return
				}

				// 按日期分组，计算每日的净变化
				const dailyData = {}

				this.transactions.forEach(tx => {
					const date = new Date(tx.transactionTime)
					const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
					
					if (!dailyData[dateStr]) {
						dailyData[dateStr] = 0
					}

					// 计算当前用户的收支
					let amount = 0
					if (tx.transactionType === 'RECHARGE') {
						// 充值是收入
						amount = parseFloat(tx.amount) || 0
					} else if (tx.transactionType === 'WITHDRAW') {
						// 提现是支出
						amount = -(parseFloat(tx.amount) || 0)
					} else if (tx.transactionType === 'PAYMENT' || tx.transactionType === 'PAYMENT_FROZEN') {
						// 支付：如果fromWalletUserId是当前用户，是支出；如果toWalletUserId是当前用户，是收入
						if (tx.fromWalletUserId === this.currentUserId) {
							amount = -(parseFloat(tx.amount) || 0)
						} else if (tx.toWalletUserId === this.currentUserId) {
							amount = parseFloat(tx.amount) || 0
						}
					}

					dailyData[dateStr] += amount
				})

				// 转换为数组并按日期排序
				const sortedDates = Object.keys(dailyData).sort()
				this.chartData = sortedDates.map(date => ({
					date,
					amount: dailyData[date]
				}))
			},
			initChart() {
				if (!this.$refs.chartRef) {
					return
				}

				// 如果已有图表实例，先销毁
				if (this.chartInstance) {
					this.chartInstance.dispose()
					this.chartInstance = null
				}

				// 创建图表实例
				this.chartInstance = echarts.init(this.$refs.chartRef)

				// 准备数据
				const dates = this.chartData.map(item => {
					const date = new Date(item.date)
					return `${date.getMonth() + 1}/${date.getDate()}`
				})
				
				// 计算累计余额变化（从最早日期开始累加）
				// 注意：这里显示的是余额变化趋势，不是绝对余额
				let cumulativeBalance = 0
				const balances = this.chartData.map(item => {
					cumulativeBalance += item.amount
					return parseFloat(cumulativeBalance.toFixed(2))
				})

				// 保存原始日期数据供tooltip使用
				const chartData = this.chartData

				// 配置图表
				const option = {
					title: {
						text: '每日余额变化趋势',
						left: 'center',
						textStyle: {
							fontSize: 14,
							color: '#333'
						}
					},
					tooltip: {
						trigger: 'axis',
						formatter: function(params) {
							const param = params[0]
							const index = param.dataIndex
							const originalDate = chartData[index].date
							const dailyAmount = chartData[index].amount
							const amountText = dailyAmount >= 0 ? `+¥${dailyAmount.toFixed(2)}` : `-¥${Math.abs(dailyAmount).toFixed(2)}`
							return `${originalDate}<br/>当日变化: ${amountText}<br/>累计变化: ¥${param.value}`
						}
					},
					grid: {
						left: '10%',
						right: '10%',
						bottom: '15%',
						top: '20%',
						containLabel: true
					},
					xAxis: {
						type: 'category',
						data: dates,
						axisLabel: {
							rotate: 45,
							fontSize: 10
						}
					},
					yAxis: {
						type: 'value',
						axisLabel: {
							formatter: function(value) {
								return '¥' + value
							},
							fontSize: 10
						}
					},
					series: [{
						name: '余额变化',
						type: 'line',
						data: balances,
						smooth: true,
						itemStyle: {
							color: '#667eea'
						},
						areaStyle: {
							color: {
								type: 'linear',
								x: 0,
								y: 0,
								x2: 0,
								y2: 1,
								colorStops: [{
									offset: 0,
									color: 'rgba(102, 126, 234, 0.3)'
								}, {
									offset: 1,
									color: 'rgba(102, 126, 234, 0.1)'
								}]
							}
						},
						lineStyle: {
							width: 2
						}
					}]
				}

				// 设置配置项
				this.chartInstance.setOption(option)

				// 响应式调整（只添加一次）
				if (!this._resizeHandlerAdded) {
					window.addEventListener('resize', this.handleResize)
					this._resizeHandlerAdded = true
				}
			},
			handleResize() {
				if (this.chartInstance) {
					this.chartInstance.resize()
				}
			}
		}
	}
</script>

<style scoped>
	.wallet-page {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
	}

	.wallet-page>header {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 12vw;
		background-color: #3190e8;
		color: #fff;
		text-align: center;
		line-height: 12vw;
		font-size: 4.5vw;
		font-weight: bold;
		z-index: 100;
	}

	.content {
		flex: 1;
		padding-top: 12vw;
		padding-bottom: 15vw;
		background-color: #f5f5f5;
	}

	.wallet-card {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		margin: 4vw;
		padding: 6vw;
		border-radius: 3vw;
		color: #fff;
		box-shadow: 0 2vw 4vw rgba(0, 0, 0, 0.1);
	}

	.balance-section {
		text-align: center;
		margin-bottom: 4vw;
	}

	.balance-section .label {
		font-size: 3.5vw;
		opacity: 0.9;
		margin-bottom: 2vw;
	}

	.balance-section .balance {
		font-size: 8vw;
		font-weight: bold;
	}

	.info-section {
		border-top: 1px solid rgba(255, 255, 255, 0.3);
		padding-top: 4vw;
	}

	.info-item {
		display: flex;
		justify-content: space-between;
		margin-bottom: 2vw;
		font-size: 3.5vw;
	}

	.info-item:last-child {
		margin-bottom: 0;
	}

	.info-label {
		opacity: 0.9;
	}

	.info-value {
		font-weight: bold;
	}

	.info-value.warning {
		color: #ffd700;
	}

	.action-buttons {
		display: flex;
		gap: 3vw;
		padding: 0 4vw;
		margin-bottom: 4vw;
	}

	.action-btn {
		flex: 1;
		height: 10vw;
		border: none;
		border-radius: 2vw;
		font-size: 4vw;
		font-weight: bold;
		color: #fff;
		cursor: pointer;
	}

	.action-btn.full-width {
		flex: none;
		width: 100%;
	}

	.recharge-btn {
		background-color: #3190e8;
	}

	.recharge-btn:active {
		background-color: #2a7bc7;
	}

	.withdraw-btn {
		background-color: #ff6b6b;
	}

	.withdraw-btn:active {
		background-color: #ee5a5a;
	}

	.menu {
		background-color: #fff;
		margin: 0 4vw;
		border-radius: 2vw;
		overflow: hidden;
	}

	.menu li {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 4vw;
		border-bottom: 1px solid #f0f0f0;
		font-size: 4vw;
		cursor: pointer;
	}

	.menu li:last-child {
		border-bottom: none;
	}

	.menu li:active {
		background-color: #f5f5f5;
	}

	.arrow {
		color: #999;
		font-size: 4vw;
	}

	.vip-banner {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		margin: 4vw;
		padding: 4vw;
		border-radius: 2vw;
		display: flex;
		align-items: center;
		cursor: pointer;
		box-shadow: 0 2vw 4vw rgba(102, 126, 234, 0.3);
	}

	.vip-banner.no-vip {
		background: linear-gradient(135deg, #ffd700 0%, #ff8c00 100%);
		box-shadow: 0 2vw 4vw rgba(255, 215, 0, 0.3);
	}

	.vip-icon {
		width: 15vw;
		height: 15vw;
		background-color: rgba(255, 255, 255, 0.2);
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 3vw;
	}

	.vip-icon i {
		font-size: 8vw;
		color: #fff;
	}

	.vip-text {
		flex: 1;
	}

	.vip-text h4 {
		font-size: 4.5vw;
		color: #fff;
		margin: 0 0 1vw 0;
		font-weight: bold;
	}

	.vip-text p {
		font-size: 3vw;
		color: rgba(255, 255, 255, 0.9);
		margin: 0;
	}

	.vip-arrow {
		color: #fff;
		font-size: 5vw;
	}

	.chart-container {
		background-color: #fff;
		margin: 4vw;
		padding: 4vw;
		border-radius: 2vw;
		box-shadow: 0 1vw 2vw rgba(0, 0, 0, 0.05);
	}

	.chart-title {
		font-size: 4.5vw;
		font-weight: bold;
		color: #333;
		margin-bottom: 3vw;
		text-align: center;
	}

	.chart {
		width: 100%;
		height: 50vw;
		min-height: 300px;
	}

	.chart-empty {
		background-color: #fff;
		margin: 4vw;
		padding: 8vw;
		border-radius: 2vw;
		text-align: center;
		color: #999;
		font-size: 3.5vw;
	}

	/* 商家底部导航样式 */
	.merchant-footer {
		position: fixed;
		bottom: 0;
		left: 0;
		width: 100%;
		height: 15vw;
		background-color: white;
		display: flex;
		border-top: 1px solid #eee;
		z-index: 99;
	}

	.merchant-footer .footer-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		font-size: 3vw;
		color: #666;
		cursor: pointer;
	}

	.merchant-footer .footer-item i {
		font-size: 5vw;
		margin-bottom: 1vw;
	}

	.merchant-footer .footer-item.active {
		color: #0097FF;
	}
</style>

