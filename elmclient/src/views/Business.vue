<template>
	<div class="business-home">
		<header>
			<p>{{ business.businessName || '商家中心' }}</p>
		</header>

		<!-- 未创建店铺时的引导 -->
		<div v-if="!hasBusiness" class="create-business-guide">
			<div class="guide-content">
				<i class="fa fa-store"></i>
				<h3>您还没有创建店铺</h3>
				<p>请先创建店铺，然后才能添加商品</p>
				<button class="create-business-btn" @click="showCreateBusinessModal = true">
					创建我的店铺
				</button>
			</div>
		</div>

		<!-- 已有店铺时的商品管理 -->
		<div v-else class="food-container">
			<div class="section-title">
				我的商品
				<!-- 如果有多家店铺，提供切换按钮 -->
				<div v-if="businesses && businesses.length > 1" class="business-switcher">
					<span class="label">当前店铺：</span>
					<button
						v-for="biz in businesses"
						:key="biz.id"
						class="biz-tag"
						:class="{ active: biz.id === business.id }"
						@click="switchBusiness(biz)"
					>
						{{ biz.businessName }}
					</button>
				</div>
			</div>

			<!-- 商品列表 -->
			<div class="food-list">
				<div v-for="food in foodList" :key="food.id" class="food-item">
					<img :src="food.foodImg ? food.foodImg : '../assets/default-food.png'" :alt="food.foodName"
						class="food-img">
					<div class="food-info">
						<div class="info-top">
							<h3 class="food-name">{{ food.foodName }}</h3>
							<div class="food-price">¥{{ food.foodPrice }}</div>
						</div>
						<p class="food-desc">{{ food.foodExplain }}</p>
					</div>
					<div class="food-actions">
						<button class="edit-btn" @click="openEditModal(food.id)">编辑</button>
						<button class="delete-btn" @click="handleDelete(food.id)">删除</button>
					</div>
				</div>
			</div>

			<!-- 空状态 -->
			<div v-if="foodList.length === 0" class="empty-state">
				<i class="fa fa-cutlery"></i>
				<p>暂无商品，点击下方添加</p>
			</div>

			<!-- 上架商品按钮 -->
			<button class="add-food-btn" @click="openAddModal">
				<i class="fa fa-plus"></i>
				<span>上架商品</span>
			</button>
		</div>

		<!-- 创建店铺弹窗 -->
		<div class="modal-mask" v-if="showCreateBusinessModal">
			<div class="modal-content">
				<div class="modal-header">
					<h3>创建店铺</h3>
					<button class="close-btn" @click="showCreateBusinessModal = false">&times;</button>
				</div>
				<div class="modal-form">
					<div class="form-item">
						<label class="form-label">店铺名称</label>
						<input type="text" v-model="newBusiness.businessName" placeholder="请输入店铺名称" class="form-input">
					</div>
					<div class="form-item">
						<label class="form-label">店铺地址</label>
						<input type="text" v-model="newBusiness.businessAddress" placeholder="请输入店铺地址"
							class="form-input">
					</div>
					<div class="form-item">
						<label class="form-label">店铺简介</label>
						<textarea v-model="newBusiness.businessExplain" placeholder="请输入店铺简介" rows="3"
							class="form-textarea"></textarea>
					</div>
					<div class="form-item">
						<label class="form-label">起送价（元）</label>
						<input type="number" v-model.number="newBusiness.startPrice" placeholder="请输入起送价" min="0"
							step="0.01" class="form-input">
					</div>
					<div class="form-item">
						<label class="form-label">配送费（元）</label>
						<input type="number" v-model.number="newBusiness.deliveryPrice" placeholder="请输入配送费" min="0"
							step="0.01" class="form-input">
					</div>
					<!-- 新增：店铺分类选择 -->
					<div class="form-item">
						<label class="form-label">店铺分类</label>
						<select v-model="newBusiness.orderTypeId" class="form-select" required>
							<option value="">请选择分类</option>
							<option value="1">dcfl01 - 美食</option>
							<option value="2">dcfl02 - 早餐</option>
							<option value="3">dcfl03 - 跑腿代购</option>
							<option value="4">dcfl04 - 汉堡披萨</option>
							<option value="5">dcfl05 - 甜品饮品</option>
							<option value="6">dcfl06 - 速食简餐</option>
							<option value="7">dcfl07 - 地方小吃</option>
							<option value="8">dcfl08 - 米粉面馆</option>
							<option value="9">dcfl09 - 包子粥铺</option>
							<option value="10">dcfl10 - 炸鸡炸串</option>
						</select>
					</div>
				</div>
				<div class="modal-footer">
					<button class="cancel-btn" @click="showCreateBusinessModal = false">取消</button>
					<button class="confirm-btn" @click="createBusiness">创建店铺</button>
				</div>
			</div>
		</div>

		<!-- 新增商品弹窗 -->
		<div class="modal-mask" v-if="showAddModal">
			<div class="modal-content">
				<div class="modal-header">
					<h3>上架新商品</h3>
					<button class="close-btn" @click="closeAddModal">&times;</button>
				</div>
				<div class="modal-form">
					<div class="form-item">
						<label class="form-label">商品图片</label>
						<div class="img-upload-area">
							<img :src="newFood.previewUrl || '../assets/default-food.png'" alt="商品预览"
								class="preview-img">
							<input type="file" ref="addImgInput" accept="image/*" @change="handleAddImgSelect"
								class="hidden-file-input">
							<button class="upload-img-btn" @click="triggerAddImgInput">选择图片</button>
						</div>
					</div>
					<div class="form-item">
						<label class="form-label">商品名称</label>
						<input type="text" v-model="newFood.foodName" placeholder="请输入商品名称" class="form-input">
					</div>
					<div class="form-item">
						<label class="form-label">商品价格</label>
						<input type="number" v-model.number="newFood.foodPrice" placeholder="请输入商品价格（元）" min="0.01"
							step="0.01" class="form-input">
					</div>
					<div class="form-item">
						<label class="form-label">商品描述</label>
						<textarea v-model="newFood.foodExplain" placeholder="请输入商品描述（可选）" rows="3"
							class="form-textarea"></textarea>
					</div>
				</div>
				<div class="modal-footer">
					<button class="cancel-btn" @click="closeAddModal">取消</button>
					<button class="confirm-btn" @click="submitNewFood">确定上架</button>
				</div>
			</div>
		</div>

		<!-- 编辑商品弹窗 -->
		<div class="modal-mask" v-if="showEditModal">
			<div class="modal-content">
				<div class="modal-header">
					<h3>编辑商品</h3>
					<button class="close-btn" @click="closeEditModal">&times;</button>
				</div>
				<div class="modal-form">
					<div class="form-item">
						<label class="form-label">商品图片</label>
						<div class="img-upload-area">
							<img :src="editFood.previewUrl || editFood.foodImg || '../assets/default-food.png'"
								alt="商品预览" class="preview-img">
							<input type="file" ref="editImgInput" accept="image/*" @change="handleEditImgSelect"
								class="hidden-file-input">
							<button class="upload-img-btn" @click="triggerEditImgInput">更换图片</button>
						</div>
					</div>
					<div class="form-item">
						<label class="form-label">商品名称</label>
						<input type="text" v-model="editFood.foodName" placeholder="请输入商品名称" class="form-input">
					</div>
					<div class="form-item">
						<label class="form-label">商品价格</label>
						<input type="number" v-model.number="editFood.foodPrice" placeholder="请输入商品价格（元）" min="0.01"
							step="0.01" class="form-input">
					</div>
					<div class="form-item">
						<label class="form-label">商品描述</label>
						<textarea v-model="editFood.foodExplain" placeholder="请输入商品描述（可选）" rows="3"
							class="form-textarea"></textarea>
					</div>
				</div>
				<div class="modal-footer">
					<button class="cancel-btn" @click="closeEditModal">取消</button>
					<button class="confirm-btn" @click="submitEditFood">保存修改</button>
				</div>
			</div>
		</div>

		<!-- 店铺信息弹窗 -->
		<div class="modal-mask" v-if="showBusinessInfoModal">
			<div class="modal-content">
				<div class="modal-header">
					<h3>店铺信息</h3>
					<button class="close-btn" @click="showBusinessInfoModal = false">&times;</button>
				</div>
				<div class="modal-form">
					<div class="form-item">
						<label class="form-label">店铺名称</label>
						<input type="text" v-model="business.businessName" class="form-input" readonly>
					</div>
					<div class="form-item">
						<label class="form-label">店铺地址</label>
						<input type="text" v-model="business.businessAddress" class="form-input" readonly>
					</div>
					<div class="form-item">
						<label class="form-label">起送价</label>
						<input type="text" :value="formatPrice(business.startPrice)" class="form-input" readonly>
					</div>
					<div class="form-item">
						<label class="form-label">配送费</label>
						<input type="text" :value="formatPrice(business.deliveryPrice)" class="form-input" readonly>
					</div>
					<div class="form-item" v-if="business.businessExplain">
						<label class="form-label">店铺简介</label>
						<textarea v-model="business.businessExplain" rows="3" class="form-textarea" readonly></textarea>
					</div>
				</div>
				<div class="modal-footer">
					<button class="confirm-btn" @click="showBusinessInfoModal = false">确定</button>
				</div>
			</div>
		</div>

		<!-- 底部导航 -->
		<div class="merchant-footer">
			<div class="footer-item active" @click="$router.push('/business')">
				<i class="fa fa-home"></i>
				<span>首页</span>
			</div>
			<div class="footer-item" @click="goToOrders">
				<i class="fa fa-list-alt"></i>
				<span>订单</span>
			</div>
			<div class="footer-item" @click="goToWallet">
				<i class="fa fa-credit-card"></i>
				<span>钱包</span>
			</div>
			<div class="footer-item" @click="goToManage">
				<i class="fa fa-user"></i>
				<span>我的</span>
			</div>
		</div>
	</div>
</template>

<script>
	import auth from '../utils/auth'

	export default {
		name: 'Business',
		data() {
			return {
				hasBusiness: false,
				// 当前选中的店铺
				business: {},
				// 当前商家名下的所有店铺
				businesses: [],
				foodList: [],
				showCreateBusinessModal: false,
				newBusiness: {
					businessName: '',
					businessAddress: '',
					businessExplain: '',
					startPrice: 20,
					deliveryPrice: 5,
					orderTypeId: null // 新增：初始化分类ID
				},
				showAddModal: false,
				newFood: {
					foodName: '',
					foodPrice: 0,
					foodExplain: '',
					imgFile: null,
					previewUrl: ''
				},
				showEditModal: false,
				editFood: {
					id: '',
					foodName: '',
					foodPrice: 0,
					foodExplain: '',
					foodImg: '',
					imgFile: null,
					previewUrl: ''
				},
				showBusinessInfoModal: false,
			}
		},
		created() {
			this.initData();
		},
		methods: {
			// 价格格式化
			formatPrice(price) {
				if (!price) return '0.00';
				return typeof price === 'number' ? price.toFixed(2) : parseFloat(price).toFixed(2);
			},

			// 初始化数据
			async initData() {
				// 先加载当前商家名下的全部店铺
				await this.loadMyBusinesses();
				// 如果至少有一家店，加载当前选中店铺的商品
				if (this.hasBusiness && this.business && this.business.id) {
					await this.getFoodList();
				}
			},

			// 加载当前商家的全部店铺列表
			async loadMyBusinesses() {
				try {
					// 使用后端提供的 /api/businesses/my-businesses 列表接口
					const response = await this.$axios.get('/api/businesses/my-businesses');

					if (response.data.success && Array.isArray(response.data.data)) {
						this.businesses = response.data.data;
						if (this.businesses.length > 0) {
							// 默认选中第一家店铺
							this.business = this.businesses[0];
							this.hasBusiness = true;
						} else {
							this.hasBusiness = false;
						}
					} else {
						this.hasBusiness = false;
						console.log('未找到店铺:', response.data.message);
					}
				} catch (error) {
					console.error('获取店铺列表失败:', error.response?.data || error.message);
					this.hasBusiness = false;
				}
			},

			// 切换当前选中的店铺
			async switchBusiness(biz) {
				if (!biz || !biz.id || (this.business && this.business.id === biz.id)) {
					return;
				}
				this.business = biz;
				await this.getFoodList();
			},

			// 创建店铺（修复接口路径和响应处理）
			async createBusiness() {
				try {
					if (!this.newBusiness.businessName.trim()) {
						alert('请输入店铺名称');
						return;
					}
					// 新增：验证分类选择
					if (this.newBusiness.orderTypeId === null || this.newBusiness.orderTypeId === '') {
						alert('请选择店铺分类');
						return;
					}

					// 后端创建店铺接口：/api/businesses/create-my-business（与后端路径完全匹配）
					const response = await this.$axios.post(
						'/api/businesses/create-my-business',
						this.newBusiness
					);

					// 处理后端HttpResult响应
					if (response.data.success) {
						alert('店铺创建成功！');
						this.showCreateBusinessModal = false;
						// 重新加载店铺列表与当前选中店铺信息
						await this.loadMyBusinesses();
						if (this.hasBusiness && this.business && this.business.id) {
							await this.getFoodList();
						}
					} else {
						// 后端返回的错误信息（如"您已经创建过店铺"）
						alert('创建失败：' + response.data.message);
					}
				} catch (error) {
					// 捕获网络错误或权限不足（403）等异常
					console.error('创建店铺失败:', error);
					const errorMsg = error.response?.data?.message || '网络错误，请重试';
					alert('创建失败：' + errorMsg);
				}
			},

			// 获取商品列表（修复：使用后端支持的参数）
			async getFoodList() {
				try {
					// 后端通过 business 参数筛选商家商品，路径为 /api/foods
					const response = await this.$axios.get('/api/foods', {
						params: {
							business: this.business.id
						} // 传递店铺ID
					});
					if (response.data.success) {
						this.foodList = response.data.data;
					} else {
						console.error('获取商品列表失败:', response.data.message);
					}
				} catch (error) {
					console.error('获取商品列表失败:', error);
				}
			},

			// 打开新增商品弹窗
			openAddModal() {
				if (!this.hasBusiness) {
					alert('请先创建店铺');
					return;
				}
				this.newFood = {
					foodName: '',
					foodPrice: 0,
					foodExplain: '',
					imgFile: null,
					previewUrl: ''
				};
				this.showAddModal = true;
			},

			// 触发图片选择
			triggerAddImgInput() {
				this.$refs.addImgInput.click();
			},

			// 处理新增商品图片选择
			handleAddImgSelect(e) {
				const file = e.target.files[0];
				if (!file) return;

				if (!file.type.startsWith('image/')) {
					alert('请选择图片文件');
					return;
				}
				if (file.size > 2 * 1024 * 1024) {
					alert('图片大小不能超过2MB');
					return;
				}

				const reader = new FileReader();
				reader.onload = (e) => {
					this.newFood.previewUrl = e.target.result;
				};
				reader.readAsDataURL(file);
				this.newFood.imgFile = file;
			},

			// 提交新增商品（修复：接口路径与后端匹配）
			async submitNewFood() {
				try {
					if (!this.newFood.foodName || this.newFood.foodName.trim() === '') {
						alert('请输入商品名称');
						return;
					}
					if (!this.newFood.foodPrice || this.newFood.foodPrice <= 0) {
						alert('请输入正确的商品价格');
						return;
					}

					// 构建商品数据，关联当前店铺ID
					const foodData = {
						foodName: this.newFood.foodName,
						foodPrice: this.newFood.foodPrice,
						foodExplain: this.newFood.foodExplain || '',
						foodImg: this.newFood.previewUrl || '',
						business: {
							id: this.business.id
						} // 关联店铺
					};

					// 后端新增商品接口路径为 /api/foods（POST）
					const response = await this.$axios.post('/api/foods', foodData);
					if (response.data.success) {
						alert('商品上架成功！');
						this.closeAddModal();
						await this.getFoodList(); // 刷新列表
					} else {
						alert('上架失败：' + response.data.message);
					}
				} catch (error) {
					console.error('上架商品失败:', error);
					alert('上架失败，请重试');
				}
			},

			// 关闭新增商品弹窗
			closeAddModal() {
				this.showAddModal = false;
			},

			// 打开编辑商品弹窗
			openEditModal(foodId) {
				const food = this.foodList.find(f => f.id === foodId);
				if (!food) {
					alert('商品不存在');
					return;
				}
				this.editFood = {
					id: food.id,
					foodName: food.foodName,
					foodPrice: food.foodPrice,
					foodExplain: food.foodExplain || '',
					foodImg: food.foodImg,
					imgFile: null,
					previewUrl: food.foodImg
				};
				this.showEditModal = true;
			},

			// 关闭编辑商品弹窗
			closeEditModal() {
				this.showEditModal = false;
			},

			// 触发编辑商品图片选择
			triggerEditImgInput() {
				this.$refs.editImgInput.click();
			},

			// 处理编辑商品图片选择
			handleEditImgSelect(e) {
				const file = e.target.files[0];
				if (!file) return;

				if (!file.type.startsWith('image/')) {
					alert('请选择图片文件');
					return;
				}
				if (file.size > 2 * 1024 * 1024) {
					alert('图片大小不能超过2MB');
					return;
				}

				const reader = new FileReader();
				reader.onload = (e) => {
					this.editFood.previewUrl = e.target.result;
				};
				reader.readAsDataURL(file);
				this.editFood.imgFile = file;
			},

			// 提交编辑商品
			async submitEditFood() {
				try {
					if (!this.editFood.foodName || this.editFood.foodName.trim() === '') {
						alert('请输入商品名称');
						return;
					}
					if (!this.editFood.foodPrice || this.editFood.foodPrice <= 0) {
						alert('请输入正确的商品价格');
						return;
					}

					// 构建更新数据
					const updateData = {
						foodName: this.editFood.foodName.trim(),
						foodPrice: this.editFood.foodPrice,
						foodExplain: this.editFood.foodExplain || '',
						foodImg: this.editFood.previewUrl || this.editFood.foodImg,
						business: {
							id: this.business.id
						} // 保持店铺关联
					};

					// 后端编辑商品接口路径为 /api/foods/{id}（PUT）
					const response = await this.$axios.put(`/api/foods/${this.editFood.id}`, updateData);

					if (response.data.success) {
						alert('商品修改成功！');
						this.closeEditModal();
						await this.getFoodList();
					} else {
						alert('修改失败：' + response.data.message);
					}
				} catch (error) {
					console.error('修改商品失败:', error);
					alert('修改失败，请重试: ' + (error.response?.data?.message || error.message));
				}
			},

			// 删除商品（适配后端软删除接口）
			async handleDelete(foodId) {
				if (!confirm('确定要下架该商品吗？')) {
					return;
				}

				try {
					// 调用后端删除接口：DELETE /api/foods/{id}
					const response = await this.$axios.delete(`/api/foods/${foodId}`);

					// 处理后端HttpResult响应
					if (response.data.success) {
						alert('商品下架成功！');
						await this.getFoodList(); // 刷新商品列表（已过滤deleted=true的商品）
					} else {
						// 后端返回的业务错误（如无权限、商品不存在）
						alert('下架失败：' + response.data.message);
					}
				} catch (error) {
					// 捕获网络错误或服务器异常
					console.error('删除商品请求失败:', error);
					const errorMsg = error.response?.data?.message || '网络错误，请重试';
					alert('下架失败：' + errorMsg);
				}
			},

			// 获取商品列表（确保过滤已删除商品，与后端逻辑一致）
			async getFoodList() {
				try {
					const response = await this.$axios.get('/api/foods', {
						params: {
							business: this.business.id
						}
					});

					if (response.data.success) {
						// 后端已通过查询条件过滤deleted=false的商品，直接使用即可
						this.foodList = response.data.data;
					} else {
						console.error('获取商品列表失败:', response.data.message);
					}
				} catch (error) {
					console.error('获取商品列表失败:', error);
				}
			},

			// 跳转到店铺管理
			goToManage() {
				if (!this.hasBusiness) {
					alert('请先创建店铺');
					return;
				}
				this.$router.push('/businessManage');
			},

			// 跳转到钱包页面
			goToWallet() {
				this.$router.push('/wallet');
			},

			// 跳转到订单页面，带上当前选中的店铺ID，方便订单页直接显示对应店铺
			goToOrders() {
				if (!this.hasBusiness || !this.business || !this.business.id) {
					alert('请先创建并选择店铺');
					return;
				}
				this.$router.push({
					path: '/businessOrders',
					query: {
						businessId: this.business.id
					}
				});
			}
		}
	}
</script>

<style scoped>
	/* 样式保持不变 */
	.create-business-guide {
		display: flex;
		justify-content: center;
		align-items: center;
		min-height: 60vh;
		padding: 20vw 0;
	}

	.guide-content {
		text-align: center;
		background: white;
		padding: 8vw;
		border-radius: 3vw;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
	}

	.guide-content i {
		font-size: 20vw;
		color: #0097FF;
		margin-bottom: 4vw;
	}

	.guide-content h3 {
		font-size: 4.5vw;
		color: #333;
		margin-bottom: 3vw;
	}

	.guide-content p {
		font-size: 3.5vw;
		color: #666;
		margin-bottom: 6vw;
	}

	.create-business-btn {
		background-color: #0097FF;
		color: white;
		border: none;
		border-radius: 6vw;
		padding: 3vw 8vw;
		font-size: 3.8vw;
		font-weight: bold;
		cursor: pointer;
	}

	.hidden-file-input {
		display: none;
	}

	.business-home {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: 15vw;
		box-sizing: border-box;
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
	}

	header {
		width: 100%;
		height: 12vw;
		background-color: #0097FF;
		color: #fff;
		font-size: 4.8vw;
		position: fixed;
		top: 0;
		left: 0;
		z-index: 100;
		display: flex;
		justify-content: center;
		align-items: center;
		margin: 0;
	}

	.food-container {
		width: 100%;
		padding: 15vw 0 8vw;
		box-sizing: border-box;
	}

	.section-title {
		font-size: 4vw;
		font-weight: bold;
		color: #333;
		padding: 3vw;
		background-color: white;
		border-bottom: 1px solid #eee;
		margin: 0;
	}

	.business-switcher {
		margin-top: 2vw;
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 2vw;
		font-size: 3vw;
	}

	.business-switcher .label {
		color: #666;
	}

	.biz-tag {
		padding: 1vw 3vw;
		border-radius: 20px;
		border: 1px solid #0097FF;
		background-color: #fff;
		color: #0097FF;
		font-size: 3vw;
		cursor: pointer;
	}

	.biz-tag.active {
		background-color: #0097FF;
		color: #fff;
	}

	.food-list {
		width: 100%;
		background-color: white;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.food-item {
		display: flex;
		align-items: center;
		padding: 3vw;
		border-bottom: 1px solid #f5f5f5;
		box-sizing: border-box;
	}

	.food-img {
		width: 18vw;
		height: 18vw;
		border-radius: 2vw;
		object-fit: cover;
		margin-right: 3vw;
		background-color: #fafafa;
	}

	.food-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
	}

	.info-top {
		display: flex;
		justify-content: space-between;
		align-items: baseline;
		margin-bottom: 1vw;
	}

	.food-name {
		font-size: 3.8vw;
		font-weight: 500;
		color: #333;
		margin: 0;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.food-price {
		font-size: 3.5vw;
		font-weight: bold;
		color: #ff3333;
		margin: 0;
	}

	.food-desc {
		font-size: 3vw;
		color: #999;
		margin: 0;
		display: -webkit-box;
		-webkit-line-clamp: 1;
		-webkit-box-orient: vertical;
		overflow: hidden;
		line-height: 1.4;
	}

	.food-actions {
		display: flex;
		flex-direction: column;
		gap: 1.5vw;
	}

	.edit-btn,
	.delete-btn {
		width: 18vw;
		height: 7vw;
		border: none;
		border-radius: 2vw;
		font-size: 3vw;
		cursor: pointer;
		transition: background-color 0.2s;
	}

	.edit-btn {
		background-color: #0097FF;
		color: white;
	}

	.delete-btn {
		background-color: #ff3333;
		color: white;
	}

	.empty-state {
		text-align: center;
		padding: 15vw 0;
		color: #999;
		background-color: white;
		margin: 0;
	}

	.empty-state i {
		font-size: 15vw;
		margin-bottom: 5vw;
		opacity: 0.5;
	}

	.add-food-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		margin: 3vw auto;
		width: 94%;
		height: 12vw;
		background-color: #0097FF;
		color: white;
		border: none;
		border-radius: 6vw;
		font-size: 4vw;
		font-weight: bold;
		padding: 0 5vw;
		cursor: pointer;
	}

	.modal-mask {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		z-index: 200;
		display: flex;
		justify-content: center;
		align-items: center;
		padding: 5vw;
		box-sizing: border-box;
	}

	.modal-content {
		width: 100%;
		max-width: 80vw;
		background-color: white;
		border-radius: 3vw;
		overflow: hidden;
		box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
	}

	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 3vw;
		border-bottom: 1px solid #eee;
	}

	.modal-header h3 {
		font-size: 3.8vw;
		color: #333;
		margin: 0;
	}

	.close-btn {
		background: none;
		border: none;
		font-size: 4.5vw;
		color: #999;
		cursor: pointer;
	}

	.modal-form {
		padding: 3vw;
	}

	.form-item {
		margin-bottom: 4vw;
	}

	.form-label {
		display: block;
		font-size: 3.5vw;
		color: #666;
		margin-bottom: 2vw;
	}

	.img-upload-area {
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.preview-img {
		width: 30vw;
		height: 30vw;
		object-fit: cover;
		border-radius: 2vw;
		border: 1px dashed #ddd;
		margin-bottom: 2vw;
		background-color: #fafafa;
	}

	.upload-img-btn {
		width: 40vw;
		height: 8vw;
		background-color: #f0f0f0;
		color: #666;
		border: none;
		border-radius: 4vw;
		font-size: 3vw;
		cursor: pointer;
	}

	.form-input,
	.form-textarea {
		width: 100%;
		padding: 2.5vw;
		border: 1px solid #ddd;
		border-radius: 2vw;
		font-size: 3.2vw;
		color: #333;
		box-sizing: border-box;
		outline: none;
	}

	.form-textarea {
		resize: none;
		line-height: 1.5;
	}

	.modal-footer {
		display: flex;
		justify-content: space-between;
		padding: 3vw;
		border-top: 1px solid #eee;
		gap: 3vw;
	}

	.cancel-btn,
	.confirm-btn {
		flex: 1;
		height: 10vw;
		border: none;
		border-radius: 2vw;
		font-size: 3.5vw;
		font-weight: 500;
		cursor: pointer;
	}

	.cancel-btn {
		background-color: #f0f0f0;
		color: #666;
	}

	.confirm-btn {
		background-color: #0097FF;
		color: white;
	}

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

	.footer-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		font-size: 3vw;
		color: #666;
		cursor: pointer;
	}

	.footer-item i {
		font-size: 5vw;
		margin-bottom: 1vw;
	}

	.footer-item.active {
		color: #0097FF;
	}
</style>