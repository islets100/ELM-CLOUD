<template>
	<div class="birthday-setting-page">
		<nav class="top-nav">
			<button @click="$router.go(-1)" class="back-btn">← 返回</button>
			<h1>设置生日</h1>
		</nav>

		<form class="birthday-form" @submit.prevent="submitBirthday">
		<div class="form-item">
			<label>生日 <span class="required">*</span></label>
			<input type="date" v-model="userForm.birthday" placeholder="选择生日" required :max="maxDate" :disabled="!canModifyBirthday">
			<span class="hint">选择您的出生日期，设置生日后可前往我的积分页面获得生日月积分奖励</span>
			<span class="modification-hint" v-if="!canModifyBirthday">
				每年只能修改一次生日，您上次修改时间为：{{ lastModificationDate }}
			</span>
		</div>

		<button type="submit" class="submit-btn" :disabled="loading || !canModifyBirthday">
			<span v-if="loading">保存中...</span>
			<span v-else-if="!canModifyBirthday">每年只能修改一次生日</span>
			<span v-else>保存生日</span>
		</button>
	</form>
	</div>
</template>

<script>
	import auth from '@/utils/auth.js'
	import userApi from '../api/user.js'

	export default {
	name: 'BirthdaySetting',
	data() {
		return {
			loading: false,
			maxDate: '',
			userForm: {
				birthday: ''
			},
			canModifyBirthday: true,
			lastModificationDate: null
		}
	},
	created() {
		// 验证登录状态
		if (!auth.isLoggedIn()) {
			this.$router.push('/login')
			return
		}

		// 设置最大日期为今天
		const today = new Date()
		this.maxDate = today.toISOString().split('T')[0]

		// 获取当前用户信息
		const user = auth.getUserInfo()
		// 防止user为null时的报错
		if (user) {
			this.userForm.birthday = user.birthday || ''
		}

		// 检查是否可以修改生日
		this.checkBirthdayModification()
	},
		methods: {
			// 检查是否可以修改生日
			async checkBirthdayModification() {
				try {
					const response = await userApi.checkBirthdayModification()
					if (response.data) {
						this.canModifyBirthday = response.data.canModify
						this.lastModificationDate = response.data.lastModificationDate
					}
				} catch (err) {
					console.error('检查生日修改资格失败', err)
					// 如果检查失败，默认允许修改
					this.canModifyBirthday = true
				}
			},

			async submitBirthday() {
				// 检查是否可以修改生日
				if (!this.canModifyBirthday) {
					alert('每年只能修改一次生日')
					return
				}

				const birthday = this.userForm.birthday

				// 前端验证
				if (!birthday) {
					alert('请选择生日')
					return
				}

				// 生日验证：不能设置未来日期（通过input的max属性已经限制）
				const birthDate = new Date(birthday)
				const today = new Date()
				if (birthDate > today) {
					alert('生日不能超过今天')
					return
				}
				// 限制年龄在18-100岁之间
				const minDate = new Date()
				minDate.setFullYear(today.getFullYear() - 100)
				if (birthDate < minDate) {
					alert('年龄不能超过100岁')
					return
				}

				this.loading = true
				try {
					const user = auth.getUserInfo()
					// 检查用户信息是否存在
					if (!user || !user.id) {
						alert('用户信息获取失败，请重新登录')
						this.$router.push('/login')
						return
					}

					// 更新生日
					const response = await userApi.updateUserInfo(
						user.id, // 用户ID
						user.username, // 保持原用户名不变
						this.userForm.birthday // 生日
					)

					if (response.status === 200) {
						// 更新本地存储的用户信息
						const updatedUser = {
							...user,
							birthday: this.userForm.birthday
						}
						auth.setUserInfo(updatedUser)

						alert('生日设置成功')
						this.$router.push('/user') // 跳回我的页面
					}
				} catch (err) {
					console.error('设置生日失败', err)
					// 优化：更精准的错误提示
					const errorMsg = err.response?.data || '网络错误，请重试'
					alert(`设置失败：${errorMsg}`)
				} finally {
					this.loading = false
				}
			}
	}
	}
</script>

<style scoped>
	/* 样式参考UserInfo.vue的表单样式 */
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

	.birthday-form {
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

	.hint {
		display: block;
		font-size: 0.8rem;
		color: #666;
		margin-top: 0.5rem;
	}

	.modification-hint {
		display: block;
		font-size: 0.8rem;
		color: #ff6b6b;
		margin-top: 0.5rem;
	}
</style>