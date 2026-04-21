<template>
	<div class="wrapper">
		<!-- 顶部导航 -->
		<header>
			<p>用户登录</p>
		</header>

		<!-- 登录表单卡片 -->
		<div class="login-card">
			<!-- 品牌标识 -->
			<div class="brand-logo">
				<i class="fa fa-cutlery"></i>
				<h2>外卖平台</h2>
			</div>

			<!-- 表单部分 -->
			<ul class="form-box">
				<li>
					<div class="input-group">
						<i class="fa fa-user"></i>
						<input type="text" v-model="username" placeholder="请输入用户名" @input="clearError">
					</div>
				</li>
				<li>
					<div class="input-group">
						<i class="fa fa-lock"></i>
						<input type="password" v-model="password" placeholder="请输入密码" @input="clearError">
					</div>
				</li>
				<li class="remember-row">
					<div class="remember-me">
						<input type="checkbox" v-model="rememberMe" id="remember">
						<label for="remember">记住登录状态</label>
					</div>
					<a href="#" class="forgot-pwd">忘记密码?</a>
				</li>
			</ul>

			<!-- 错误提示 -->
			<p class="error-message" v-if="errorMsg">{{ errorMsg }}</p>

			<!-- 操作按钮 -->
			<div class="action-buttons">
				<button @click="handleLogin" class="login-btn">登录</button>
				<button @click="goToRegister" class="register-btn">注册账号</button>
			</div>
		</div>

		<!-- 底部菜单 -->
		<Footer></Footer>
	</div>
</template>

<script>
	import Footer from '../components/Footer.vue';
	import auth from '../utils/auth';
	import axios from 'axios';

	export default {
		name: 'Login',
		components: {
			Footer
		},
		data() {
			return {
				username: '',
				password: '',
				rememberMe: false,
				errorMsg: '',
				loading: false
			};
		},
		created() {
			// 如果已登录，直接根据权限跳转
			if (auth.isLoggedIn()) {
				this.redirectByAuthority();
			}
		},
		methods: {
			// 清除错误提示
			clearError() {
				this.errorMsg = '';
			},

			// 处理登录逻辑
			async handleLogin() {
				// 表单验证
				if (!this.username.trim()) {
					this.errorMsg = '请输入用户名';
					return;
				}
				if (!this.password.trim()) {
					this.errorMsg = '请输入密码';
					return;
				}

				this.loading = true;
				this.errorMsg = '';

				try {
					// 1. 调用登录接口获取令牌
					const authResponse = await axios.post('/api/auth', {
						username: this.username.trim(),
						password: this.password.trim(),
						rememberMe: this.rememberMe
					});

					const {
						id_token
					} = authResponse.data;
					if (!id_token) {
						throw new Error('登录失败，未获取到令牌');
					}

					// 2. 保存令牌
					auth.setToken(id_token);

					// 3. 设置请求头携带令牌
					axios.defaults.headers.common['Authorization'] = `Bearer ${id_token}`;

					// 4. 获取当前用户信息（包含权限）
					const userResponse = await axios.get('/api/user');

					// 拦截器已将后端格式 {code, message, result} 转换为 {success, data, message}
					if (!userResponse.data || !userResponse.data.success) {
						throw new Error('获取用户信息失败');
					}

					const userInfo = userResponse.data.data;

					if (!userInfo || !userInfo.authorities) {
						throw new Error('获取用户信息失败');
					}

					// 5. 保存用户信息
					auth.setUserInfo(userInfo);

					// 6. 根据权限跳转
					this.redirectByAuthority();

				} catch (error) {
					console.error('登录过程出错:', error);
					this.errorMsg = this.getErrorMessage(error);
					// 清除可能保存的无效令牌
					auth.removeToken();
					auth.removeUserInfo();
				} finally {
					this.loading = false;
				}
			},

			// 根据用户权限跳转对应页面
			redirectByAuthority() {
				const user = auth.getUserInfo();
				if (!user) return;

				// 获取用户权限列表
				const authorities = user.authorities.map(auth => auth.name) || [];

				// 获取登录前的跳转地址
				const redirectPath = this.$route.query.redirect ?
					decodeURIComponent(this.$route.query.redirect) :
					'/index'; // 如果没有 redirect，跳转到首页

				// 权限判断与跳转
				if (authorities.includes('ADMIN')) {
					// 管理员 - 跳转到用户管理页面
					this.$router.push('/admin/users');
				} else if (authorities.includes('BUSINESS')) {
					// 商家 - 跳转到商家首页
					this.$router.push('/business');
				} else if (authorities.includes('USER')) {
					// 普通用户 - 跳转到之前页面或默认首页
					this.$router.push(redirectPath); // 如果有 redirect 跳回原页面，否则默认首页
				} else {
					// 无匹配权限 - 跳转到首页
					this.$router.push('/index');
				}
			},

			// 跳转到注册页面
			goToRegister() {
				this.$router.push('/register');
			},

			// 统一错误信息处理
			getErrorMessage(error) {
				if (error.response) {
					// 服务器返回错误
					switch (error.response.status) {
						case 401:
							return '用户名或密码错误';
						case 403:
							return '账号已被禁用';
						case 500:
							return '服务器错误，请稍后重试';
						default:
							return `登录失败 (${error.response.status})`;
					}
				} else if (error.message) {
					return error.message;
				} else {
					return '登录失败，请检查网络连接';
				}
			}
		}
	};
</script>

<style scoped>
	/* 基础样式 */
	.wrapper {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
		position: relative;
		padding-bottom: 14vw;
		/* 预留底部菜单高度 */
		box-sizing: border-box;
	}

	/* 顶部导航 */
	header {
		width: 100%;
		height: 12vw;
		background-color: #0097FF;
		color: #fff;
		font-size: 4.8vw;
		display: flex;
		justify-content: center;
		align-items: center;
		position: fixed;
		top: 0;
		left: 0;
		z-index: 100;
	}

	/* 登录卡片 */
	.login-card {
		width: 90%;
		margin: 20vw auto 0;
		background-color: #fff;
		border-radius: 10px;
		padding: 6vw 5vw;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
	}

	/* 品牌标识 */
	.brand-logo {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 6vw;
	}

	.brand-logo i {
		font-size: 15vw;
		color: #0097FF;
		margin-bottom: 2vw;
	}

	.brand-logo h2 {
		font-size: 5vw;
		color: #333;
		margin: 0;
	}

	/* 表单样式 */
	.form-box {
		list-style: none;
		padding: 0;
		margin: 0 0 4vw 0;
	}

	.form-box li {
		margin-bottom: 4vw;
	}

	.input-group {
		display: flex;
		align-items: center;
		border: 1px solid #ddd;
		border-radius: 6px;
		padding: 3vw;
	}

	.input-group i {
		font-size: 4vw;
		color: #999;
		margin-right: 3vw;
	}

	.input-group input {
		flex: 1;
		border: none;
		outline: none;
		font-size: 3.5vw;
		color: #333;
		padding: 1vw 0;
	}

	.input-group input::placeholder {
		color: #ccc;
	}

	/* 记住密码行 */
	.remember-row {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-top: 2vw;
	}

	.remember-me {
		display: flex;
		align-items: center;
	}

	.remember-me input {
		width: 4vw;
		height: 4vw;
		margin-right: 2vw;
	}

	.remember-me label {
		font-size: 3vw;
		color: #666;
	}

	.forgot-pwd {
		font-size: 3vw;
		color: #0097FF;
		text-decoration: none;
	}

	/* 错误提示 */
	.error-message {
		color: #ff4d4f;
		font-size: 3vw;
		text-align: center;
		margin: 0 0 4vw 0;
		min-height: 4vw;
	}

	/* 操作按钮 */
	.action-buttons {
		display: flex;
		flex-direction: column;
		gap: 3vw;
	}

	.login-btn,
	.register-btn {
		width: 100%;
		height: 12vw;
		border-radius: 6px;
		border: none;
		font-size: 4vw;
		font-weight: bold;
		outline: none;
		cursor: pointer;
	}

	.login-btn {
		background-color: #0097FF;
		color: white;
	}

	.login-btn:disabled {
		background-color: #99ccff;
		cursor: not-allowed;
	}

	.register-btn {
		background-color: #f5f5f5;
		color: #666;
	}

	/* 底部菜单占位 */
	::v-deep .footer {
		position: fixed;
	}
</style>
