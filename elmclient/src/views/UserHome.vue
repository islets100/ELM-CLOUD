<template>
	<div class="user-home wrapper">
		<header>我的</header>

		<!-- 内容区，避开 header 和 footer -->
		<div class="content">
			<div class="avatar-nick">
				<img :src="avatarUrl" @click="changeAvatar" />
				<input ref="avatarFile" type="file" accept="image/*" hidden @change="uploadAvatar" />
				<p class="user-name">{{ username || '未登录用户' }}</p>
			</div>

			<ul class="menu">
				<li @click="$router.push('/wallet')">我的钱包</li>
				<li @click="$router.push('/points')">我的积分</li>
				<li @click="$router.push('/points/mall')">积分商城</li>
				<li @click="$router.push('/user/info')">修改昵称</li>
				<li @click="$router.push('/user/birthday')">设置生日</li>
				<li @click="$router.push('/user/password')">修改密码</li>
				<li @click="$router.push('/user/favorite')">我的收藏</li>
				<li @click="$router.push('/user/remark')">我的评论</li>
				<li @click="$router.push('/user/chat')">我的私信</li>
				<li @click="$router.push('/user/like')">商家点赞</li>
			</ul>

			<button class="logout-btn" @click="logout">退出登录</button>
		</div>

		<!-- 底部菜单 -->
		<Footer></Footer>
	</div>
</template>

<script>
	import auth from '@/utils/auth.js';
	import Footer from '../components/Footer.vue';

	export default {
		name: 'UserHome',
		data() {
			return {
				username: '', // 改成和后端字段一致
				avatarUrl: require('@/assets/default-avatar.png') // 默认头像
			};
		},
		created() {
			const user = auth.getUserInfo();
			if (!user) {
				return;
			}

			this.username = user.username || user.userName || user.userId || user.id || '';
			this.avatarUrl = user.userImg || this.avatarUrl;
		},
		components: {
			Footer
		},
		methods: {
			changeAvatar() {
				this.$refs.avatarFile.click();
			},
			uploadAvatar(e) {
				const file = e.target.files[0];
				if (!file) return;

				const reader = new FileReader();
				reader.onload = (e) => {
					this.avatarUrl = e.target.result;
					const user = auth.getUserInfo();
					auth.setUserInfo({
						...user,
						userImg: e.target.result
					});
					alert('头像已在本地更新（无后端时不会保存到服务器）');
				};
				reader.readAsDataURL(file);
			},
			logout() {
				auth.logout();
				this.$router.push('/login');
			}
		}
	};
</script>

<style scoped>
	.user-home {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
	}

	/* header 固定在顶部 */
	.user-home>header {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 12vw;
		background-color: #0097FF;
		color: #fff;
		display: flex;
		justify-content: center;
		align-items: center;
		font-size: 4.8vw;
		z-index: 1000;
	}

	/* 内容区，避开 header 和 footer */
	.user-home>.content {
		margin-top: 12vw;
		padding-bottom: 14vw;
		flex: 1;
		padding: 0 4vw;
		overflow-y: auto;
	}

	/* 头像和昵称 */
	.avatar-nick {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 5vw 0;
		background: #fff;
		margin-bottom: 2vw;
	}

	.avatar-nick img {
		width: 20vw;
		height: 20vw;
		border-radius: 50%;
		cursor: pointer;
		object-fit: cover;
	}

	.user-name {
		margin-top: 2vw;
		font-size: 4vw;
		color: #333;
		font-weight: bold;
	}

	/* 菜单列表 */
	.menu {
		margin: 0;
		padding: 0;
		list-style: none;
		max-height: 50vh;
		overflow-y: auto;
	}

	.menu li {
		background: #fff;
		margin-bottom: 1vw;
		padding: 3vw;
		font-size: 3.5vw;
		cursor: pointer;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.menu li::after {
		content: '→';
		color: #ccc;
		font-size: 4vw;
	}

	/* 退出登录按钮 */
	.logout-btn {
		width: 90%;
		margin: 3vw auto;
		padding: 3vw 0;
		background: #ff4d4f;
		color: #fff;
		border: none;
		border-radius: 1vw;
		font-size: 3.5vw;
		font-weight: bold;
		cursor: pointer;
		display: block;
	}
</style>
