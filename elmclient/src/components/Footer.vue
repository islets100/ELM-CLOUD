<template>
	<div>
		<ul class="footer">
			<li @click="toIndex">
				<i class="fa fa-home"></i>
				<p>首页</p>
			</li>
			<li>
				<i class="fa fa-compass"></i>
				<p>发现</p>
			</li>
			<li @click="toOrderList">
				<i class="fa fa-file-text-o"></i>
				<p>订单</p>
			</li>
			<li @click="toMy">
				<i class="fa fa-user-o"></i>
				<p>我的</p>
			</li>
		</ul>
	</div>
</template>

<script>
	import auth from '../utils/auth.js' // 根据实际路径调整

	export default {
		name: 'Footer',
		methods: {
			toIndex() {
				this.$router.push({
					path: '/index'
				})
			},
			toOrderList() {
			  console.log('点击订单按钮，登录状态:', auth.isLoggedIn())
			  if (auth.isLoggedIn()) {
			    this.$router.push('/userOrderList')
			  } else {
			    this.$router.push('/login')
			  }
			},
			toMy() {
				if (auth.isLoggedIn()) {
					this.$router.push({
						path: '/user'
					})
				} else {
					this.$router.push({
						path: '/login'
					})
				}
			}
		}
	}
</script>

<style>
	.wrapper .footer {
		width: 100%;
		height: 14vw;
		border-top: solid 1px #DDD;
		background-color: #fff;
		position: fixed;
		left: 0;
		bottom: 0;
		display: flex;
		justify-content: space-around;
		align-items: center;
	}

	.wrapper .footer li {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		color: #999;
		user-select: none;
		cursor: pointer;
	}

	.wrapper .footer li p {
		font-size: 2.8vw;
	}

	.wrapper .footer li i {
		font-size: 5vw;
	}
</style>