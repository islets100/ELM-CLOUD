<template>
	<div>
		<ul class="footer">
			<li @click="toIndex">
				<i class="fa fa-home"></i>
				<p>首页</p>
			</li>
			<li @click="toDiscover">
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
import auth from '../utils/auth.js'

export default {
	name: 'Footer',
	methods: {
		toIndex() {
			this.$router.push({
				path: '/index'
			})
		},
		toDiscover() {
			this.$router.push({
				path: '/index'
			})
		},
		toOrderList() {
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
.footer {
	width: 100%;
	width: 100vw;
	height: 14vw;
	margin: 0;
	padding: 0;
	list-style: none;
	border-top: solid 1px #ddd;
	background-color: #fff;
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	display: flex;
	align-items: center;
	justify-content: space-between;
	box-shadow: 0 -1vw 3vw rgba(15, 58, 109, 0.08);
	z-index: 3000;
}

.footer li {
	flex: 1;
	width: 25%;
	min-width: 0;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	color: #999;
	user-select: none;
	cursor: pointer;
}

.footer li p {
	margin: 0.8vw 0 0;
	font-size: 2.8vw;
}

.footer li i {
	font-size: 5vw;
}
</style>
