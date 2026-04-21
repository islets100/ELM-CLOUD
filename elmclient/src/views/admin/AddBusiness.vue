<template>
	<div class="add-business">
		<nav class="top-nav">
			<h1>新增店铺</h1>
			<div class="btn-group">
				<button @click="$router.push('/admin/users')" class="back-btn">返回</button>
			</div>
		</nav>

		<!-- 商家选择区域 -->
		<div class="merchant-selection">
			<h3>选择商家 <span class="required">*</span></h3>
			<div class="search-box">
				<input 
					type="text" 
					v-model="searchKeyword" 
					placeholder="搜索商家名称或ID"
					@input="filterMerchants"
				>
				<i class="fa fa-search"></i>
			</div>

			<!-- 商家列表 -->
			<div class="merchant-list">
				<div 
					v-for="merchant in filteredMerchants" 
					:key="merchant.id"
					class="merchant-item"
					:class="{ 'selected': selectedMerchantId === merchant.id }"
					@click="selectMerchant(merchant.id)"
				>
					<div class="merchant-info">
						<p class="merchant-name">{{ merchant.username }} (ID: {{ merchant.id }})</p>
						<p class="merchant-role">角色：店主</p>
					</div>
					<div class="merchant-actions">
						<i class="fa fa-check" v-if="selectedMerchantId === merchant.id"></i>
					</div>
				</div>
			</div>

			<div v-if="filteredMerchants.length === 0 && loaded" class="no-results">
				<i class="fa fa-info-circle"></i>
				<span>未找到匹配的商家</span>
			</div>
		</div>

		<!-- 错误提示 -->
		<div v-if="errorMessage" class="error-message">
			{{ errorMessage }}
		</div>

		<!-- 店铺信息表单 -->
		<div v-if="selectedMerchantId && loaded" class="form-container">
			<h3>为商家ID: {{ selectedMerchantId }} 新增店铺</h3>
			<form @submit.prevent="submitBusinessForm">
				<div class="form-group">
					<label for="businessName">店铺名称 <span class="required">*</span></label>
					<input 
						v-model="businessForm.businessName" 
						type="text" 
						id="businessName" 
						placeholder="输入店铺名称"
						required
					>
				</div>

				<div class="form-group">
					<label for="businessAddress">店铺地址 <span class="required">*</span></label>
					<input 
						v-model="businessForm.businessAddress" 
						type="text" 
						id="businessAddress"
						placeholder="输入店铺地址"
						required
					>
				</div>

				<div class="form-group">
					<label for="businessExplain">店铺说明</label>
					<textarea 
						v-model="businessForm.businessExplain" 
						id="businessExplain"
						placeholder="店铺说明"
					></textarea>
				</div>

				

				<div class="form-group">
					<label for="orderTypeId">店铺分类 <span class="required">*</span></label>
					<select 
						v-model="businessForm.orderTypeId" 
												id="orderTypeId"
												required
											>
												<option value="">请选择分类</option>
												<option value="1">dcfl01 - 餐饮</option>
												<option value="2">dcfl02 - 超市</option>
												<option value="3">dcfl03 - 快餐便当</option>
												<option value="4">dcfl04 - 奶茶饮品</option>
												<option value="5">dcfl05 - 甜品甜点</option>
												<option value="6">dcfl06 - 餐饮美食</option>
												<option value="7">dcfl07 - 地方小吃</option>
												<option value="8">dcfl08 - 火锅烧烤</option>
												<option value="9">dcfl09 - 海鲜水产</option>
												<option value="10">dcfl10 - 面包糕点</option>
											</select>
										</div>

				<div class="form-group">
					<label for="startPrice">起送价（元）</label>
					<input 
						v-model.number="businessForm.startPrice" 
						type="number" 
						id="startPrice" 
						placeholder="输入起送价"
						min="0" 
						step="0.01"
					>
				</div>

				<div class="form-group">
					<label for="deliveryPrice">配送费（元）</label>
					<input 
						v-model.number="businessForm.deliveryPrice" 
						type="number" 
						id="deliveryPrice" 
						placeholder="输入配送费"
						min="0" 
						step="0.01"
					>
				</div>

				<button type="submit" class="submit-btn" :disabled="loading">
					<span v-if="loading">提交中...</span>
					<span v-else>创建店铺</span>
				</button>
			</form>
		</div>
	</div>
</template>

<script>
	import adminApi from '../../api/admin'
	import auth from '../../utils/auth'
	import axios from 'axios'

	export default {
		name: 'AddBusiness',
		data() {
			return {
				// 商家相关
				allMerchants: [],        // 所有商家列表
				filteredMerchants: [],    // 过滤后的商家列表
				searchKeyword: '',        // 搜索关键词
				selectedMerchantId: null, // 选中的商家ID
				
				// 店铺表单数据
				businessForm: {
					businessName: '',
					businessAddress: '',
					businessExplain: '',
					orderTypeId: null,       // 店铺分类ID 
					startPrice: 20.00,       // 默认起送价
					deliveryPrice: 5.00      // 默认配送费
				},
				
				// 状态控制
				loading: false,
				loaded: false,
				errorMessage: ''
			};
		},
		created() {
			// 权限校验
			if (!auth.isLoggedIn() || !auth.isAdmin()) {
				this.$router.push('/admin/login')
				return
			}

			const token = auth.getToken()
			if (!token) {
				alert('登录状态失效，请重新登录')
				this.$router.push('/admin/login')
				return
			}

			// 加载所有商家（仅店主角色）
			this.loadAllMerchants()
		},
		methods: {
			// 加载所有商家（仅包含店主角色）
			async loadAllMerchants() {
				try {
					this.loading = true
					const token = auth.getToken()
					
					// 调用用户列表接口
					const response = await axios.get('/api/users', {
						headers: {
							'Authorization': `Bearer ${token}`
						}
					})

					// 过滤出拥有BUSINESS权限的用户（店主）
					this.allMerchants = response.data.filter(user => 
						user.authorities && user.authorities.some(auth => auth.name === 'BUSINESS')
					)
					
					this.filteredMerchants = [...this.allMerchants]
					this.loaded = true
				} catch (error) {
					console.error('加载商家列表失败', error)
					this.errorMessage = '加载商家列表失败，请刷新页面重试'
					this.allMerchants = []
					this.filteredMerchants = []
				} finally {
					this.loading = false
				}
			},

			// 根据关键词过滤商家
			filterMerchants() {
				if (!this.searchKeyword.trim()) {
					this.filteredMerchants = [...this.allMerchants]
					return
				}
				
				const keyword = this.searchKeyword.trim().toLowerCase()
				this.filteredMerchants = this.allMerchants.filter(merchant => 
					merchant.username.toLowerCase().includes(keyword) || 
					String(merchant.id).includes(keyword)
				)
			},

			// 选择商家
			selectMerchant(merchantId) {
				this.selectedMerchantId = merchantId
				this.errorMessage = ''
			},

			// 提交新增店铺表单
			async submitBusinessForm() {
				// 前端校验
				if (!this.selectedMerchantId) {
					this.errorMessage = '请先选择一个商家'
					return
				}

				if (!this.businessForm.businessName.trim()) {
					this.errorMessage = '请输入店铺名称'
					return
				}

				if (!this.businessForm.businessAddress.trim()) {
					this.errorMessage = '请输入店铺地址'
					return
				}

				// 关键更新：验证分类ID为1-10的有效范围
								const validTypes = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
								if (!this.businessForm.orderTypeId || !validTypes.includes(String(this.businessForm.orderTypeId))) {
									this.errorMessage = '请选择有效的店铺分类（1-10）'
									return
								}

				this.loading = true
				try {
					const token = auth.getToken()
					
					// 构造提交数据
					const submitData = {
						...this.businessForm,
						businessOwner: {
							id: Number(this.selectedMerchantId) // 关联选中的商家
						},
						deleted: false,
						createTime: new Date().toISOString(),
						updateTime: new Date().toISOString()
					}

					console.log('提交的店铺数据:', submitData)

					// 调用新增店铺接口
					const response = await axios.post('/api/businesses', submitData, {
						headers: {
							'Authorization': `Bearer ${token}`,
							'Content-Type': 'application/json'
						}
					})

					if (response.data.success) {
						alert('店铺新增成功！')
						this.$router.push('/admin/users')
					} else {
						this.errorMessage = '新增失败：' + (response.data.message || '未知错误')
					}
				} catch (error) {
					console.error('提交店铺信息失败', error)
					this.errorMessage = `新增失败: ${error.response?.data?.message || error.message || '网络错误'}`
				} finally {
					this.loading = false
				}
			}
		}
	}
</script>

<style scoped>
	.add-business {
		padding: 1.5rem;
		max-width: 1200px;
		margin: 0 auto;
		background-color: #f9f9f9;
		min-height: 100vh;
	}

	.top-nav {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-bottom: 1rem;
		border-bottom: 1px solid #eee;
		margin-bottom: 1.5rem;
	}

	.top-nav h1 {
		color: #333;
		font-size: 1.5rem;
		margin: 0;
	}

	.back-btn {
		padding: 0.5rem 1rem;
		background: #0097ff;
		color: #fff;
		border: none;
		border-radius: 4px;
		cursor: pointer;
		transition: background 0.2s;
	}

	.back-btn:hover {
		background: #0078cc;
	}

	/* 商家选择区域样式 */
	.merchant-selection {
		background: #fff;
		padding: 1.5rem;
		border-radius: 8px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
		margin-bottom: 1.5rem;
	}

	.merchant-selection h3 {
		color: #333;
		margin-top: 0;
		margin-bottom: 1rem;
		font-size: 1.1rem;
	}

	.search-box {
		position: relative;
		margin-bottom: 1rem;
	}

	.search-box input {
		width: 100%;
		padding: 0.8rem 0.8rem 0.8rem 2.5rem;
		border: 1px solid #e5e7eb;
		border-radius: 4px;
		font-size: 0.9rem;
		box-sizing: border-box;
	}

	.search-box i {
		position: absolute;
		left: 0.8rem;
		top: 50%;
		transform: translateY(-50%);
		color: #999;
	}

	.merchant-list {
		max-height: 300px;
		overflow-y: auto;
		border: 1px solid #e5e7eb;
		border-radius: 4px;
	}

	.merchant-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 0.8rem 1rem;
		border-bottom: 1px solid #f5f5f5;
		cursor: pointer;
		transition: background 0.2s;
	}

	.merchant-item:last-child {
		border-bottom: none;
	}

	.merchant-item:hover {
		background-color: #f9f9f9;
	}

	.merchant-item.selected {
		background-color: #e6f7ff;
		border-left: 3px solid #1890ff;
	}

	.merchant-name {
		font-weight: 500;
		margin: 0;
		color: #333;
	}

	.merchant-role {
		font-size: 0.8rem;
		color: #666;
		margin: 0.2rem 0 0 0;
	}

	.merchant-actions i {
		color: #1890ff;
		font-size: 1.2rem;
	}

	.no-results {
		text-align: center;
		padding: 2rem;
		color: #999;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
	}

	/* 表单样式 */
	.form-container {
		background: #fff;
		padding: 1.5rem;
		border-radius: 8px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
	}

	.form-container h3 {
		color: #333;
		margin-top: 0;
		margin-bottom: 1.5rem;
		font-size: 1.1rem;
	}

	.form-group {
		margin-bottom: 1.2rem;
	}

	.form-group label {
		display: block;
		margin-bottom: 0.5rem;
		color: #666;
		font-size: 0.9rem;
	}

	.form-group input,
	.form-group textarea,
	.form-group select {
		width: 100%;
		padding: 0.8rem;
		border: 1px solid #e5e7eb;
		border-radius: 4px;
		font-size: 0.9rem;
		box-sizing: border-box;
	}

	.form-group textarea {
		min-height: 100px;
		resize: vertical;
	}

	.required {
		color: #ff4d4f;
	}

	.submit-btn {
		width: 100%;
		padding: 0.8rem;
		background: #38ca73;
		color: #fff;
		border: none;
		border-radius: 4px;
		font-size: 1rem;
		cursor: pointer;
		transition: background 0.2s;
	}

	.submit-btn:disabled {
		background: #99d8b4;
		cursor: not-allowed;
	}

	.submit-btn:hover:not(:disabled) {
		background: #2db366;
	}

	.error-message {
		color: #dc3545;
		padding: 0.8rem;
		background-color: #f8d7da;
		border: 1px solid #f5c6cb;
		border-radius: 4px;
		margin-bottom: 1.5rem;
	}
</style>
