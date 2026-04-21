<template>
	<div class="user-management">
		<!-- 顶部导航 -->
		<nav class="top-nav">
			<h1>用户管理</h1>
			<div class="btn-group">
				<button @click="$router.push('/admin/add-user')" class="add-btn">
					新增用户
				</button>
				<button @click="$router.push('/admin/add-business')" class="add-business-btn">
					新增店铺
				</button>
				<button @click="$router.push('/admin/reward-rules')" class="reward-rules-btn">
					奖励规则
				</button>
				<button @click="handleLogout" class="logout-btn">
					退出
				</button>
			</div>
		</nav>

		<!-- 搜索和操作区：修改点1 - 输入框改为文本类型，绑定用户名变量 -->
		<div class="action-bar">
			<div class="search-container">
				<!-- 原：type="number" + v-model="searchUserId" + 占位符"输入用户ID查询" -->
				<input 
					type="text"  
					v-model="searchUsername"  
					placeholder="输入用户名查询"  
					@keyup.enter="getUserDetail"
				>
				<button @click="getUserDetail" class="search-btn">
					查询
				</button>
			</div>
			<button @click="loadAllUsers" class="load-all-btn" :disabled="loading">
				<i class="fas fa-sync-alt" :class="{ 'fa-spin': loading }"></i>
				{{ loading ? '加载中...' : '加载所有用户' }}
			</button>
		</div>

		<!-- 用户详情卡片 -->
		<div class="user-card" v-if="userDetail">
			<div class="card-header">
				<h3>用户信息详情</h3>
				<button @click="userDetail = null" class="close-btn">
					<i class="fas fa-times"></i>
				</button>
			</div>
			<div class="info-grid">
				<div class="info-item">
					<span class="label">用户ID：</span>
					<span class="value">{{ userDetail.id }}</span>
				</div>
				<div class="info-item">
					<span class="label">用户名：</span>
					<span class="value">{{ userDetail.username }}</span>
				</div>
				<div class="info-item">
					<span class="label">权限：</span>
					<div class="roles">
						<span class="role-tag" v-for="auth in userDetail.authorities" :key="auth.name">
							{{ auth.name === 'ADMIN' ? '管理员' : auth.name === 'BUSINESS' ? '商家' : '普通用户' }}
						</span>
					</div>
				</div>
				<div class="info-item">
					<span class="label">创建时间：</span>
					<span class="value">{{ formatDate(userDetail.createTime) }}</span>
				</div>
			</div>
			<div class="card-actions">
				<button @click="prepareUpdatePassword(userDetail)" class="edit-pwd-btn">
					 修改密码
				</button>
			</div>
		</div>

		<!-- 所有用户列表 -->
		<div class="users-list-container" v-if="users.length > 0">
			<div class="list-header">
				<h3>用户列表 <span class="count-badge">{{ users.length }} 位用户</span></h3>
				<div class="filter-controls">
					<select v-model="roleFilter" @change="filterUsers">
						<option value="all">所有角色</option>
						<option value="ADMIN">管理员</option>
						<option value="BUSINESS">商家</option>
						<option value="USER">普通用户</option>
					</select>
				</div>
			</div>

			<div class="table-responsive">
				<table class="users-table">
					<thead>
						<tr>
							<th>用户ID</th>
							<th>用户名</th>
							<th>角色</th>
							<th>创建时间</th>
							<th>操作</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="user in filteredUsers" :key="user.id" class="user-row">
							<td>{{ user.id }}</td>
							<td>{{ user.username }}</td>
							<td>
								<span class="role-tag" :class="{
                        'admin': user.authorities.some(a => a.name === 'ADMIN'),
                        'business': user.authorities.some(a => a.name === 'BUSINESS'),
                        'user': user.authorities.some(a => a.name === 'USER')
                      }">
									{{ user.authorities.some(a => a.name === 'ADMIN') ? '管理员' : 
                     user.authorities.some(a => a.name === 'BUSINESS') ? '商家' : '普通用户' }}
								</span>
							</td>
							<td>{{ formatDate(user.createTime) }}</td>
							<td class="actions">
								<!-- 修改点2 - 点击“查看”时传整个user对象，而非仅ID -->
								<button @click="viewUserDetail(user)" class="view-btn">
									 查看
								</button>
								<button @click="prepareUpdatePassword(user)" class="edit-btn">
									修改密码
								</button>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 无数据提示：修改点3 - 判断条件从searchUserId改为searchUsername -->
		<div class="empty-state" v-if="(!userDetail && searchUsername) || (users.length === 0 && !loading)">
			<div class="empty-icon">
				<i class="fas fa-user-friends"></i>
			</div>
			<p class="empty-text">
				{{ (users.length === 0 && !loading) ? '暂无用户数据' : '未查询到该用户' }}
			</p>
			<!-- 重置按钮判断条件同步改 -->
			<button @click="resetSearch" class="reset-btn" v-if="searchUsername">
				清除查询
			</button>
		</div>

		<!-- 加载中状态 -->
		<div class="loading-state" v-if="loading && users.length === 0">
			<div class="spinner">
				<i class="fas fa-circle-notch fa-spin"></i>
			</div>
			<p>正在加载用户数据...</p>
		</div>

		<!-- 修改密码弹窗 -->
		<div class="modal-backdrop" v-if="showPwdModal">
			<div class="modal">
				<div class="modal-header">
					<h4>修改密码</h4>
					<button @click="showPwdModal = false" class="modal-close">
						<i class="fas fa-times"></i>
					</button>
				</div>
				<div class="modal-body">
					<p class="modal-subtitle">为用户 {{ currentEditUser?.username }} 修改密码</p>
					<input type="password" v-model="newPassword" placeholder="输入新密码" class="password-input">
				</div>
				<div class="modal-footer">
					<button @click="showPwdModal = false" class="btn cancel-btn">取消</button>
					<button @click="updatePassword" class="btn confirm-btn">确认修改</button>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
	import adminApi from '../../api/admin'
	import auth from '../../utils/auth'

	export default {
		name: 'UserManagement',
		data() {
			return {
				// 修改点4 - 删除原searchUserId，新增searchUsername存储用户名
				searchUsername: '',  // 替代原searchUserId，存储用户输入的用户名
				userDetail: null,
				users: [], // 所有用户列表
				filteredUsers: [], // 过滤后的用户列表
				roleFilter: 'all', // 角色筛选条件
				showPwdModal: false, // 修改密码弹窗开关
				newPassword: '', // 新密码
				currentEditUser: null, // 当前编辑的用户
				loading: false // 加载状态
			}
		},
		created() {
			// 权限校验：未登录/非管理员跳登录页
			if (!auth.isLoggedIn() || !auth.isAdmin()) {
				this.$router.push('/admin/login')
			} else {
				// 页面加载时自动加载所有用户
				this.loadAllUsers()
			}
		},
		methods: {
			// 格式化日期（兼容不同格式的时间字符串）
			formatDate(dateString) {
				if (!dateString) return ''
				const date = new Date(dateString)
				// 处理无效日期
				if (isNaN(date.getTime())) return dateString
				return date.toLocaleString()
			},

			// 修改点5 - 核心：用户名查询逻辑（替代原ID查询）
			async getUserDetail() {
				// 1. 校验：用户名非空且去空格
				const username = this.searchUsername.trim()
				if (!username) {
					this.showToast('请输入用户名') // 提示文案改“用户名”
					return
				}

				this.loading = true
				try {
					// 2. 先查用户名是否存在（调用后端新增接口userExistsByUsername）
					const existRes = await adminApi.userExistsByUsername(username)
					if (existRes.data === 1) {
						// 3. 查用户名对应的详情（调用后端新增接口getUserByUsername）
						const detailRes = await adminApi.getUserByUsername(username)
						this.userDetail = detailRes.data
						this.users = [] // 清空列表，专注显示单个用户
					} else {
						this.userDetail = null
						this.showToast('未查询到该用户')
					}
				} catch (err) {
					this.showToast('查询失败：' + (err.response?.data?.message || '网络错误'))
					console.error(err)
				} finally {
					this.loading = false
				}
			},

			// 加载所有用户（逻辑不变）
			async loadAllUsers() {
				this.loading = true
				try {
					// 调用获取所有用户的接口 /api/users
					const response = await adminApi.getAllUsers()
					this.users = response.data || []
					this.filterUsers() // 应用角色过滤
					this.userDetail = null // 清空单个用户详情
					this.searchUsername = '' // 修改点6 - 清空用户名输入框（原清空searchUserId）
				} catch (err) {
					this.showToast('加载用户失败：' + (err.response?.data?.message || '权限不足或网络错误'))
					console.error('加载用户列表失败', err)
					this.users = []
				} finally {
					this.loading = false
				}
			},

			// 根据角色过滤用户（逻辑不变）
			filterUsers() {
				if (this.roleFilter === 'all') {
					this.filteredUsers = [...this.users]
				} else {
					this.filteredUsers = this.users.filter(user =>
						user.authorities && user.authorities.some(auth => auth.name === this.roleFilter)
					)
				}
			},

			// 修改点7 - 查看用户详情：从“传ID查”改为“传用户对象设用户名查”
			viewUserDetail(user) {
				this.searchUsername = user.username // 输入框显示该用户的用户名
				this.getUserDetail() // 调用用户名查询方法
			},

			// 准备修改密码（逻辑不变）
			prepareUpdatePassword(user) {
				this.currentEditUser = user
				this.newPassword = ''
				this.showPwdModal = true
			},

			// 提交修改密码（逻辑不变）
			async updatePassword() {
				if (!this.newPassword) {
					this.showToast('请输入新密码')
					return
				}

				if (!this.currentEditUser) {
					this.showToast('未选择用户')
					return
				}

				try {
					await adminApi.updatePassword({
						username: this.currentEditUser.username,
						password: this.newPassword
					})
					this.showToast('密码修改成功')
					this.showPwdModal = false
					this.newPassword = ''
				} catch (err) {
					this.showToast('修改失败：' + (err.response?.data?.message || '权限不足'))
					console.error(err)
				}
			},

			// 退出登录：跳转到用户登录页面
			handleLogout() {
				if (confirm('确定要退出登录吗？')) {
					auth.removeToken()
					auth.removeUserInfo()
					this.$router.push('/login')
				}
			},

			// 修改点8 - 重置搜索：清空用户名输入框（原清空searchUserId）
			resetSearch() {
				this.searchUsername = '' // 替代原this.searchUserId = ''
				this.userDetail = null
				this.loadAllUsers()
			},

			// 显示提示信息（逻辑不变）
			showToast(message) {
				// 简单实现，实际项目可替换为Element UI等组件库的Toast
				alert(message)
			}
		},
		// 监听角色筛选条件变化，实时过滤（逻辑不变）
		watch: {
			roleFilter() {
				this.filterUsers()
			}
		}
	}
</script>

<style scoped>
/* 基础布局：简洁留白 */
.user-management {
  padding: 1.5rem;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f9fafb;
  min-height: 100vh;
}

/* 顶部导航：简洁分割 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.top-nav h1 {
  color: #1e293b;
  font-size: 1.6rem;
  font-weight: 600;
  margin: 0;
}

.btn-group {
  display: flex;
  gap: 0.8rem; /* 缩小间距更紧凑 */
}

/* 按钮通用：去冗余阴影，简洁hover */
.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: background-color 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  box-shadow: none; /* 去默认阴影，更干净 */
}

.btn:hover {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08); /* 轻微悬浮阴影 */
}

/* 新增用户按钮：蓝色主题 */
.add-btn {
  background-color: #3b82f6;
  color: white;
}

.add-btn:hover {
  background-color: #2563eb;
}

/* 新增店铺按钮：绿色主题 */
.add-business-btn {
  background-color: #10b981;
  color: white;
}

.add-business-btn:hover {
  background-color: #059669;
}

/* 奖励规则按钮：橙色主题 */
.reward-rules-btn {
  background-color: #f59e0b;
  color: white;
}

.reward-rules-btn:hover {
  background-color: #d97706;
}

/* 退出按钮：红色主题 */
.logout-btn {
  background-color: #ef4444;
  color: white;
}

.logout-btn:hover {
  background-color: #dc2626;
}

/* 加载所有用户按钮：同新增店铺色调保持一致 */
.load-all-btn {
  background-color: #10b981;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  transition: background-color 0.2s ease;
}

.load-all-btn:hover:not(:disabled) {
  background-color: #059669;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.load-all-btn:disabled {
  background-color: #a7f3d0;
  cursor: not-allowed;
}

/* 操作栏：紧凑排版 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.search-container {
  display: flex;
  gap: 0.6rem;
  flex: 1;
  max-width: 400px; /* 缩小搜索框宽度更协调 */
}

.search-container input {
  flex: 1;
  padding: 0.6rem 0.9rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.2s ease;
}

.search-container input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1); /* 简洁焦点反馈 */
}

.search-btn {
  background-color: #3b82f6;
  color: white;
  padding: 0.6rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  transition: background-color 0.2s ease;
  box-shadow: none;
}

.search-btn:hover {
  background-color: #2563eb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

/* 用户详情卡片：轻量阴影 */
.user-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 1.2rem;
  margin-bottom: 1.5rem;
  transition: box-shadow 0.2s ease;
}

.user-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08); /* hover时轻微强化 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
}

.card-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 1.1rem;
}

.close-btn {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  font-size: 1.1rem;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: #ef4444;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
  margin-bottom: 1.2rem;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.label {
  color: #64748b;
  font-size: 0.85rem;
  margin-bottom: 0.2rem;
  font-weight: 500;
}

.value {
  color: #1e293b;
  font-size: 0.95rem;
}

.roles {
  display: flex;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.role-tag {
  display: inline-block;
  padding: 0.25rem 0.6rem;
  border-radius: 16px;
  font-size: 0.8rem;
  font-weight: 500;
}

.role-tag.admin {
  background-color: #dbeafe;
  color: #1e40af;
}

.role-tag.business {
  background-color: #fee2e2;
  color: #991b1b;
}

.role-tag.user {
  background-color: #dcfce7;
  color: #166534;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
}

.edit-pwd-btn {
  background-color: #3b82f6;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  transition: background-color 0.2s ease;
  box-shadow: none;
}

.edit-pwd-btn:hover {
  background-color: #2563eb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

/* 用户列表容器：简洁包裹 */
.users-list-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
  overflow: hidden;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.2rem;
  border-bottom: 1px solid #e5e7eb;
}

.list-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.count-badge {
  background-color: #e0f2fe;
  color: #0284c7;
  font-size: 0.75rem;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-weight: 500;
}

.filter-controls {
  display: flex;
  gap: 0.8rem;
}

.filter-controls select {
  padding: 0.4rem 0.7rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background-color: white;
  font-size: 0.85rem;
  color: #1e293b;
}

.table-responsive {
  overflow-x: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 0.8rem 1.2rem;
  text-align: left;
}

.users-table th {
  background-color: #f1f5f9;
  color: #64748b;
  font-weight: 600;
  font-size: 0.85rem;
  white-space: nowrap;
}

.users-table tbody tr {
  border-bottom: 1px solid #e2e8f0;
  transition: background-color 0.2s ease;
}

.users-table tbody tr:hover {
  background-color: #f8fafc;
}

.actions {
  display: flex;
  gap: 0.4rem;
}

.view-btn {
  background-color: #e0f2fe;
  color: #0284c7;
  border: none;
  padding: 0.35rem 0.7rem;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.8rem;
  transition: background-color 0.2s ease;
  box-shadow: none;
}

.view-btn:hover {
  background-color: #bae6fd;
}

.edit-btn {
  background-color: #fef3c7;
  color: #d97706;
  border: none;
  padding: 0.35rem 0.7rem;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.8rem;
  transition: background-color 0.2s ease;
  box-shadow: none;
}

.edit-btn:hover {
  background-color: #fde68a;
}

/* 空状态：简洁提示 */
.empty-state {
  text-align: center;
  padding: 3rem 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
}

.empty-icon {
  font-size: 2.5rem;
  color: #94a3b8;
  margin-bottom: 0.8rem;
}

.empty-text {
  color: #64748b;
  font-size: 1rem;
  margin-bottom: 1.2rem;
}

.reset-btn {
  background-color: #f1f5f9;
  color: #334155;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  font-size: 0.85rem;
}

.reset-btn:hover {
  background-color: #e2e8f0;
}

.loading-state {
  text-align: center;
  padding: 3rem 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
}

.spinner {
  font-size: 2rem;
  color: #3b82f6;
  margin-bottom: 0.8rem;
}

.loading-state p {
  color: #64748b;
  font-size: 1rem;
}

/* 弹窗：简洁悬浮 */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 1rem;
}

.modal {
  background-color: white;
  border-radius: 8px;
  width: 100%;
  max-width: 400px; /* 缩小弹窗宽度更紧凑 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.2rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h4 {
  margin: 0;
  color: #1e293b;
  font-size: 1rem;
}

.modal-close {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  font-size: 1.1rem;
  transition: color 0.2s ease;
}

.modal-close:hover {
  color: #ef4444;
}

.modal-body {
  padding: 1.2rem;
}

.modal-subtitle {
  color: #64748b;
  font-size: 0.85rem;
  margin-bottom: 0.8rem;
}

.password-input {
  width: 100%;
  padding: 0.7rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.password-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
  padding: 1rem 1.2rem;
  border-top: 1px solid #e5e7eb;
}

.cancel-btn {
  background-color: #f1f5f9;
  color: #334155;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  font-size: 0.85rem;
}

.cancel-btn:hover {
  background-color: #e2e8f0;
}

.confirm-btn {
  background-color: #3b82f6;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  font-size: 0.85rem;
}

.confirm-btn:hover {
  background-color: #2563eb;
}

/* 响应式：小屏更紧凑 */
@media (max-width: 768px) {
  .user-management {
    padding: 1rem;
  }

  .top-nav {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.8rem;
  }

  .btn-group {
    width: 100%;
    flex-wrap: wrap;
  }

  .btn-group .btn {
    flex: 1;
    min-width: 100px;
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-container {
    max-width: 100%;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.8rem;
  }

  .filter-controls {
    width: 100%;
  }

  .filter-controls select {
    width: 100%;
  }

  .users-table th,
  .users-table td {
    padding: 0.6rem 0.9rem;
    font-size: 0.8rem;
  }
}
</style>