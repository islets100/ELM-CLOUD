<template>
	<div class="user-info-page">
		<nav class="top-nav">
			<button @click="$router.go(-1)" class="back-btn">← 返回</button>
			<h1>修改昵称</h1>
		</nav>

		<form class="info-form" @submit.prevent="submitUpdate">
			<div class="form-item">
				<label>用户名 <span class="required">*</span></label>
				<input type="text" v-model="userForm.username" placeholder="输入新用户名" required maxlength="50">
				<span class="hint">1-50个字符</span>
			</div>

			<button type="submit" class="submit-btn" :disabled="loading">
				<span v-if="loading">保存中...</span>
				<span v-else>保存修改</span>
			</button>
	</form>
	</div>
</template>

<script>
	import auth from '@/utils/auth.js'
	import userApi from '../api/user.js' // 修复：引号不匹配问题（原先是单引号结尾）

	export default {
	name: 'UserInfo',
	data() {
		return {
			loading: false,
			userForm: {
				username: ''
			}
		}
	},
	created() {
		// 验证登录状态
		if (!auth.isLoggedIn()) {
			this.$router.push('/login')
			return
		}

		// 获取当前用户信息
		const user = auth.getUserInfo()
		// 新增：防止user为null时的报错
		if (user) {
			this.userForm.username = user.username || ''
		}
	},
		methods: {
		async submitUpdate() {
			const newUsername = this.userForm.username.trim()

			// 前端验证
			if (!newUsername) {
				alert('用户名不能为空')
				return
			}
			if (newUsername.length > 50) {
				alert('用户名不能超过50个字符')
				return
			}

			this.loading = true
			try {
				const user = auth.getUserInfo()
				// 新增：检查用户信息是否存在
				if (!user || !user.id) {
					alert('用户信息获取失败，请重新登录')
					this.$router.push('/login')
					return
				}

				// 更新用户名
				const response = await userApi.updateUserInfo(
					user.id, // 用户ID
					newUsername, // 新用户名
					user.birthday // 保持原生日不变
				)

				if (response.status === 200) {
					// 更新本地存储的用户信息
					const updatedUser = {
						...user,
						username: newUsername
					}
					auth.setUserInfo(updatedUser)

					alert('昵称修改成功')
					this.$router.push('/user') // 跳回我的页面
				}
			} catch (err) {
				console.error('修改昵称失败', err)
				// 优化：更精准的错误提示
				const errorMsg = err.response?.data || '网络错误，请重试'
				alert(`修改失败：${errorMsg}`)
			} finally {
				this.loading = false
			}
		}
	}
	}
</script>

<style scoped>
	/* 样式参考AddUser.vue的表单样式 */
	.top-nav {
		display: flex;
		align-items: center;
		padding: 1rem;
		border-bottom: 1px solid #eee;
	}

	.back-btn {
		background: none;
		border: none;
		font-size: 1.2rem;
		cursor: pointer;
	}

	.info-form {
		padding: 1.5rem;
	}

	.form-item {
		margin-bottom: 1.5rem;
	}

	.required {
		color: red;
	}

	input {
		width: 100%;
		padding: 0.8rem;
		margin-top: 0.5rem;
		border: 1px solid #ddd;
		border-radius: 4px;
	}

	.submit-btn {
		width: 100%;
		padding: 0.8rem;
		background-color: #0097FF;
		color: white;
		border: none;
		border-radius: 4px;
		cursor: pointer;
	}

	.submit-btn:disabled {
		background-color: #ccc;
		cursor: not-allowed;
	}
</style>