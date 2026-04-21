<template>
	<div class="wrapper">
		<!-- header部分 -->
		<header>
			<div class="icon-location-box">
				<div class="icon-location"></div>
			</div>
			<!-- 替换location-text，变成可选择位置的下拉 -->
			<div class="location-text">
				<label>当前位置：</label>
				<select v-model="currentLocation" @change="onLocationChange">
					<option v-for="loc in locations" :key="loc" :value="loc">{{ loc }}</option>
				</select>
			</div>
		</header>

		<!-- search部分 -->
		<!-- 搜索框部分（此块与search-fixed-top块宽度高度一致，用于当search-fixed-top块固定后，挡住下面块不要窜上去） -->
		<div class="search">
			<!-- 当滚动条超过上面的定位块时，search-fixed-top块变成固定在顶部。 -->
			<div class="search-fixed-top" ref="fixedBox">
				<!-- 搜索来源部分中间的白框 -->
				<div class="search-box">
					<i class="fa fa-search"></i>搜索饿了么商家、商品名称
				</div>
			</div>
		</div>

		<!-- 点餐分类部分 -->
		<ul class="foodtype">
			<li @click="toBusinessList(1)">
				<img src="../assets/dcfl01.png">
				<p>美食</p>
			</li>
			<li @click="toBusinessList(2)">
				<img src="../assets/dcfl02.png">
				<p>早餐</p>
			</li>
			<li @click="toBusinessList(3)">
				<img src="../assets/dcfl03.png">
				<p>跑腿代购</p>
			</li>
			<li @click="toBusinessList(4)">
				<img src="../assets/dcfl04.png">
				<p>汉堡披萨</p>
			</li>
			<li @click="toBusinessList(5)">
				<img src="../assets/dcfl05.png">
				<p>甜品饮品</p>
			</li>
			<li @click="toBusinessList(6)">
				<img src="../assets/dcfl06.png">
				<p>速食简餐</p>
			</li>
			<li @click="toBusinessList(7)">
				<img src="../assets/dcfl07.png">
				<p>地方小吃</p>
			</li>
			<li @click="toBusinessList(8)">
				<img src="../assets/dcfl08.png">
				<p>米粉面馆</p>
			</li>
			<li @click="toBusinessList(9)">
				<img src="../assets/dcfl09.png">
				<p>包子粥铺</p>
			</li>
			<li @click="toBusinessList(10)">
				<img src="../assets/dcfl10.png">
				<p>炸鸡炸串</p>
			</li>
		</ul>

		<!-- 横幅广告部分 -->
		<div class="banner">
			<h3>品质套餐</h3>
			<p>搭配齐全吃得好</p>
			<a>立即抢购 &gt;</a>
		</div>

		<!-- 超级会员部分 -->
		<div class="supermember">
			<div class="left">
				<img src="../assets/super_member.png">
				<h3>超级会员</h3>
				<p>&#8226; 每月享超值权益</p>
			</div>
			<div class="right">
				立即开通 &gt;
			</div>
		</div>

		<!-- 推荐商家部分 -->
		<div class="recommend">
			<div class="recommend-line"></div>
			<p>推荐商家</p>
			<div class="recommend-line"></div>
		</div>

		<!-- 推荐方式部分 -->
		<ul class="recommendtype">
			<li>综合排序<i class="fa fa-caret-down"></i></li>
			<li>距离最近</li>
			<li>销量最高</li>
			<li>筛选<i class="fa fa-filter"></i></li>
		</ul>

		<!-- 推荐商家列表部分 -->
		<ul class="business">
			<li @click="toBusiness(10001)">
				<img src="../assets/sj01.png">
				<div class="business-info">
					<div class="business-info-h">
						<h3>万家饺子（软件园E18店）</h3>
						<div class="business-info-like">&#8226;</div>
					</div>
					<div class="business-info-star">
						<div class="business-info-star-left">
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<p>4.9 月售345单</p>
						</div>
						<div class="business-info-star-right">
							蜂鸟专送
						</div>
					</div>
					<div class="business-info-delivery">
						<p>&#165;15起送 | &#165;3配送</p>
						<p>3.22km | 30分钟</p>
					</div>
					<div class="business-info-explain">
						<div>各种饺子</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-incon">新</div>
							<p>饿了么新用户首单立减9元</p>
						</div>
						<div class="business-info-promotion-right">
							<p>2个活动</p>
							<i class="fa fa-caret-down"></i>
						</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-incon">特</div>
							<p>特价商品5元起</p>
						</div>
					</div>
				</div>
			</li>
			<li @click="toBusiness(10002)">
				<img src="../assets/sj02.png">
				<div class="business-info">
					<div class="business-info-h">
						<h3>小锅饭豆腐馆（全运店）</h3>
						<div class="business-info-like">&#8226;</div>
					</div>
					<div class="business-info-star">
						<div class="business-info-star-left">
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<p>4.9 月售345单</p>
						</div>
						<div class="business-info-star-right">
							蜂鸟专送
						</div>
					</div>
					<div class="business-info-delivery">
						<p>&#165;15起送 | &#165;3配送</p>
						<p>3.22km | 30分钟</p>
					</div>
					<div class="business-info-explain">
						<div>各种饺子</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-incon">新</div>
							<p>饿了么新用户首单立减9元</p>
						</div>
						<div class="business-info-promotion-right">
							<p>2个活动</p>
							<i class="fa fa-caret-down"></i>
						</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-incon">特</div>
							<p>特价商品5元起</p>
						</div>
					</div>
				</div>
			</li>
			<li @click="toBusiness(10003)">
				<img src="../assets/sj03.png">
				<div class="business-info">
					<div class="business-info-h">
						<h3>麦当劳麦乐送（全运路店）</h3>
						<div class="business-info-like">&#8226;</div>
					</div>
					<div class="business-info-star">
						<div class="business-info-star-left">
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<p>4.9 月售345单</p>
						</div>
						<div class="business-info-star-right">
							蜂鸟专送
						</div>
					</div>
					<div class="business-info-delivery">
						<p>&#165;15起送 | &#165;3配送</p>
						<p>3.22km | 30分钟</p>
					</div>
					<div class="business-info-explain">
						<div>各种饺子</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-incon">新</div>
							<p>饿了么新用户首单立减9元</p>
						</div>
						<div class="business-info-promotion-right">
							<p>2个活动</p>
							<i class="fa fa-caret-down"></i>
						</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-incon">特</div>
							<p>特价商品5元起</p>
						</div>
					</div>
				</div>
			</li>
			<li @click="toBusiness(10004)">
				<img src="../assets/sj04.png">
				<div class="business-info">
					<div class="business-info-h">
						<h3>米村拌饭（浑南店）</h3>
						<div class="business-info-like">&#8226;</div>
					</div>
					<div class="business-info-star">
						<div class="business-info-star-left">
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<p>4.9 月售345单</p>
						</div>
						<div class="business-info-star-right">
							蜂鸟专送
						</div>
					</div>
					<div class="business-info-delivery">
						<p>&#165;15起送 | &#165;3配送</p>
						<p>3.22km | 30分钟</p>
					</div>
					<div class="business-info-explain">
						<div>各种饺子</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-incon">新</div>
							<p>饿了么新用户首单立减9元</p>
						</div>
						<div class="business-info-promotion-right">
							<p>2个活动</p>
							<i class="fa fa-caret-down"></i>
						</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-incon">特</div>
							<p>特价商品5元起</p>
						</div>
					</div>
				</div>
			</li>
			<li @click="toBusiness(10005)">
				<img src="../assets/sj05.png">
				<div class="business-info">
					<div class="business-info-h">
						<h3>申记串道（中海康城店）</h3>
						<div class="business-info-like">&#8226;</div>
					</div>
					<div class="business-info-star">
						<div class="business-info-star-left">
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<p>4.9 月售345单</p>
						</div>
						<div class="business-info-star-right">
							蜂鸟专送
						</div>
					</div>
					<div class="business-info-delivery">
						<p>&#165;15起送 | &#165;3配送</p>
						<p>3.22km | 30分钟</p>
					</div>
					<div class="business-info-explain">
						<div>各种饺子</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-incon">新</div>
							<p>饿了么新用户首单立减9元</p>
						</div>
						<div class="business-info-promotion-right">
							<p>2个活动</p>
							<i class="fa fa-caret-down"></i>
						</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-incon">特</div>
							<p>特价商品5元起</p>
						</div>
					</div>
				</div>
			</li>
			<li @click="toBusiness(10006)">
				<img src="../assets/sj06.png">
				<div class="business-info">
					<div class="business-info-h">
						<h3>半亩良田排骨米饭</h3>
						<div class="business-info-like">&#8226;</div>
					</div>
					<div class="business-info-star">
						<div class="business-info-star-left">
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<i class="fa fa-star"></i>
							<p>4.9 月售345单</p>
						</div>
						<div class="business-info-star-right">
							蜂鸟专送
						</div>
					</div>
					<div class="business-info-delivery">
						<p>&#165;15起送 | &#165;3配送</p>
						<p>3.22km | 30分钟</p>
					</div>
					<div class="business-info-explain">
						<div>各种饺子</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-icon">新</div>
							<p>饿了么新用户首单立减9元</p>
						</div>
						<div class="business-info-promotion-right">
							<p>2个活动</p>
							<i class="fa fa-caret-down"></i>
						</div>
					</div>
					<div class="business-info-promotion">
						<div class="business-info-promotion-left">
							<div class="business-info-promotion-left-icon" style="background-color: #f1884f;">特</div>
							<p>特价商品5元起</p>
						</div>
					</div>
				</div>
			</li>
		</ul>

		<!-- 签到弹窗 -->
		<div class="sign-in-modal" v-if="showSignInModal" @click.self="closeSignInModal">
			<div class="sign-in-content">
				<div class="sign-in-header">
					<span class="close-btn" @click="closeSignInModal">×</span>
					<h3>每日签到</h3>
				</div>
				<div class="sign-in-body">
					<div class="sign-in-icon">📅</div>
					<p class="sign-in-text">签到即可获得10积分</p>
					<p class="sign-in-tip">积分有效期1年</p>
					<button class="sign-in-btn" @click="handleSignIn" :disabled="signingIn">
						{{ signingIn ? '签到中...' : '立即签到' }}
					</button>
				</div>
			</div>
		</div>

		<!-- 底部菜单部分 -->
		<Footer></Footer>
	</div>
</template>

<script>
	//导入共通组件
	import Footer from '../components/Footer.vue';
	import auth from '../utils/auth';
	import pointsApi from '../api/points.js';

	export default {
		name: 'Index',
		data() {
			return {
				currentLocation: '沈阳市规划大厦',
				locations: [
					'沈阳市规划大厦',
					'天津大学北洋园校区',
					'南开大学津南校区',
					'其他'
				],
				showSignInModal: false,
				signingIn: false
			};
		},
		mounted() {
			// 1. 检查签到状态
			this.checkSignInStatus();

			// 2. 初始化滚动条监听
			document.onscroll = () => {
				// 获取滚动条位置
				let s1 = document.documentElement.scrollTop;
				let s2 = document.body.scrollTop;
				let scroll = s1 == 0 ? s2 : s1;
				// 获取视口宽度
				let width = document.documentElement.clientWidth;

				// 获取顶部固定块
				let search = this.$refs.fixedBox;
				if (!search) return; // 加一行判空的，避免 null*

				// 判断滚动条超过视口宽度的12%时，搜索块变固定定位
				if (scroll > width * 0.12) {
					search.style.position = 'fixed';
					search.style.left = '0';
					search.style.top = '0';
				} else {
					search.style.position = 'static';
				}
			}
		},
		beforeUnmount() {
			//当切换到其他组件时，就不需要document滚动条事件，所以将此事件去掉
			document.onscroll = null;
		},
		components: {
			Footer
		},
		methods: {
			toBusinessList(orderTypeId) {
				const user = auth.getUserInfo(); // 统一获取用户信息
				console.log('点击美食，user =', user);
				if (!user) {
					// 未登录跳登录页
					this.$router.push('/login');
				} else {
					// 已登录跳商家列表
					this.$router.push({
						path: '/businessList',
						query: {
							orderTypeId
						}
					});
				}
			},

			toBusiness(businessId) {
				const user = auth.getUserInfo();
				if (!user) {
					this.$router.push('/login');
				} else {
					this.$router.push({
						path: '/businessInfo',
						query: {
							businessId
						}
					});
				}
			},

			onLocationChange() {
				console.log('当前定位已切换为：', this.currentLocation);
			},
			async checkSignInStatus() {
				try {
					const user = auth.getUserInfo();
					if (!user) {
						console.log('[sign-in] 未检测到用户信息，跳过签到检查');
						return; // 未登录不显示签到弹窗
					}
					console.log('[sign-in] 检测到用户，开始请求签到状态', user?.id);
					
					const response = await pointsApi.checkSignInToday();
					console.log('[sign-in] 接口响应', response?.data);
					if (response.data.success) {
						const isSignedIn = response.data.data;
						// 如果今天未签到，显示签到弹窗
						if (!isSignedIn) {
							// 延迟1秒显示，让页面先加载完成
							setTimeout(() => {
								this.showSignInModal = true;
							}, 1000);
						}
					}
				} catch (err) {
					console.error('检查签到状态失败', err);
				}
			},
			async handleSignIn() {
				if (this.signingIn) return;
				
				this.signingIn = true;
				try {
					const response = await pointsApi.signIn();
					if (response.data.success) {
						alert('签到成功！获得10积分，有效期1年');
						this.closeSignInModal();
					} else {
						alert(response.data.message || '签到失败');
					}
				} catch (err) {
					console.error('签到失败', err);
					alert(err.response?.data?.message || '签到失败，请稍后重试');
				} finally {
					this.signingIn = false;
				}
			},
			closeSignInModal() {
				this.showSignInModal = false;
			}
		}



	}
</script>

<style scoped>
	/****************** 总容器 ******************/
	.wrapper {
		width: 100%;
		height: 100%;
	}

	/****************** header 部分 ******************/
	.wrapper header {
		width: 100%;
		height: 12vw;
		background-color: #0097FF;
		display: flex;
		align-items: center;
	}

	.wrapper header .icon-location-box {
		width: 3.5vw;
		height: 3.5vw;
		margin: 0 1vw 0 3vw;
	}

	.wrapper header .location-text {
		font-size: 4.5vw;
		font-weight: 700;
		color: #fff;
	}

	.wrapper header .location-text .fa-caret-down {
		margin-left: 1vw;
	}

	/****************** search 部分 ******************/
	.wrapper .search {
		width: 100%;
		height: 13vw;
	}

	.wrapper .search .search-fixed-top {
		width: 100%;
		height: 13vw;
		background-color: #0097FF;
		display: flex;
		justify-content: center;
		align-items: center;
		z-index: 10;
		/* 增加 z-index */
	}

	.wrapper .search .search-fixed-top .search-box {
		width: 90%;
		height: 9vw;
		background-color: #fff;
		border-radius: 2px;
		display: flex;
		justify-content: center;
		align-items: center;
		font-size: 3.5vw;
		color: #AEAEAE;
		font-family: "宋体";
		/* 此样式是让文本选中状态无效 */
		user-select: none;
	}

	.wrapper .search .search-fixed-top .search-box .fa-search {
		margin-right: 1vw;
	}

	/****************** 点餐分类部分 ******************/
	.wrapper .foodtype {
		width: 100%;
		height: 48vw;
		display: flex;
		flex-wrap: wrap;
		justify-content: space-around;
		/* 要使用 align-content。10 个子元素将自动换行为两行，而且两行作为一个整体垂直居中 */
		align-content: center;
	}

	.wrapper .foodtype li {
		/* 一共 10 个子元素，通过计算，子元素宽度在 16.7 ~ 20 之间，才能保证换两行 */
		width: 18vw;
		height: 20vw;
		display: flex;
		/* 弹性盒子主轴方向设为 column，然后仍然是垂直水平方向居中 */
		flex-direction: column;
		justify-content: center;
		align-items: center;
		user-select: none;
		cursor: pointer;
	}

	.wrapper .foodtype li img {
		width: 12vw;
		/* 视频讲解时高度设置为 12vw，实际上设置为 10.3vw 更佳 */
		height: 10.3vw;
	}

	.wrapper .foodtype li p {
		font-size: 3.2vw;
		color: #666;
	}

	/****************** 横幅广告部分 ******************/
	.wrapper .banner {
		width: 95%;
		margin: 0 auto;
		height: 29vw;
		/* 此三个样式组合，可以保证背景图片充满整个容器 */
		background-image: url(../assets/index_banner.png);
		background-repeat: no-repeat;
		background-size: cover;
		box-sizing: border-box;
		padding: 2vw 6vw;
	}

	.wrapper .banner h3 {
		font-size: 4.2vw;
		margin-bottom: 1.2vw;
	}

	.wrapper .banner p {
		font-size: 3.4vw;
		color: #666;
		margin-bottom: 2.4vw;
	}

	.wrapper .banner a {
		font-size: 3vw;
		color: #C79060;
	}

	/****************** 超级会员部分 ******************/
	.wrapper .supermember {
		width: 95%;
		margin: 0 auto;
		height: 11.5vw;
		background-color: #FEEDC1;
		margin-top: 1.3vw;
		border-radius: 2px;
		color: #644F1B;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.wrapper .supermember .left {
		display: flex;
		align-items: center;
		margin-left: 4vw;
		user-select: none;
	}

	.wrapper .supermember .left img {
		width: 6vw;
		height: 6vw;
		margin-right: 2vw;
	}

	.wrapper .supermember .left h3 {
		font-size: 4vw;
		margin-right: 2vw;
	}

	.wrapper .supermember .left p {
		font-size: 3vw;
	}

	.wrapper .supermember .right {
		font-size: 3vw;
		margin-right: 4vw;
		cursor: pointer;
	}

	/****************** 推荐商家部分 ******************/
	.wrapper .recommend {
		width: 100%;
		height: 14vw;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.wrapper .recommend .recommend-line {
		width: 6vw;
		height: 0.2vw;
		background-color: #888;
	}

	.wrapper .recommend p {
		font-size: 4vw;
		margin: 0 4vw;
	}

	/****************** 推荐方式部分 ******************/
	.wrapper .recommendtype {
		width: 100%;
		height: 5vw;
		margin-bottom: 5vw;
		display: flex;
		justify-content: space-around;
		align-items: center;
	}

	.wrapper .recommendtype li {
		font-size: 3.5vw;
		color: #555;
	}

	/****************** 推荐商家列表部分 ******************/
	.wrapper .business {
		width: 100%;
		margin-bottom: 14vw;
	}

	.wrapper .business li {
		width: 100%;
		box-sizing: border-box;
		padding: 2.5vw;
		user-select: none;
		border-bottom: solid 1px #DDD;
		display: flex;
	}

	.wrapper .business li img {
		width: 18vw;
		height: 18vw;
	}

	.wrapper .business li .business-info {
		width: 100%;
		box-sizing: border-box;
		padding-left: 3vw;
	}

	.wrapper .business li .business-info .business-info-h {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2vw;
	}

	.wrapper .business li .business-info .business-info-h h3 {
		font-size: 4vw;
		color: #333;
	}

	.wrapper .business li .business-info .business-info-h .business-info-like {
		width: 1.6vw;
		height: 3.4vw;
		background-color: #666;
		color: #fff;
		font-size: 4vw;
		margin-right: 4vw;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.wrapper .business li .business-info .business-info-star {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2vw;
		font-size: 3.1vw;
	}


	.wrapper .business li .business-info .business-info-star .business-info-star-left {
		display: flex;
		align-items: center;
	}

	.wrapper .business li .business-info .business-info-star .business-info-star-left .fa-star {
		color: #FEC80E;
		margin-right: 0.5vw;
	}

	.wrapper .business li .business-info .business-info-star .business-info-star-left p {
		color: #666;
		margin-left: 1vw;
	}

	.wrapper .business li .business-info .business-info-star .business-info-star-right {
		background-color: #0097FF;
		color: #fff;
		font-size: 2.4vw;
		border-radius: 2px;
		padding: 0 0.6vw;
	}

	.wrapper .business li .business-info .business-info-delivery {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2vw;
		color: #666;
		font-size: 3.1vw;
	}

	.wrapper .business li .business-info .business-info-explain {
		display: flex;
		align-items: center;
		margin-bottom: 3vw;
	}

	.wrapper .business li .business-info .business-info-explain div {
		border: solid 1px #DDD;
		font-size: 2.8vw;
		color: #666;
		border-radius: 3px;
		padding: 0 0.1vw;
	}

	.wrapper .business li .business-info .business-info-promotion {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1.8vw;
	}

	.wrapper .business li .business-info .business-info-promotion .business-info-promotion-left {
		display: flex;
		align-items: center;
	}

	.wrapper .business li .business-info .business-info-promotion .business-info-promotion-left .business-info-promotion-left-icon {
		width: 4vw;
		height: 4vw;
		background-color: #70BC46;
		border-radius: 3px;
		font-size: 3vw;
		color: #fff;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.wrapper .business li .business-info .business-info-promotion .business-info-promotion-left p {
		color: #666;
		font-size: 3vw;
		margin-left: 2vw;
	}

	.wrapper .business li .business-info .business-info-promotion .business-info-promotion-right {
		display: flex;
		align-items: center;
		font-size: 2.5vw;
		color: #999;
	}

	.wrapper .business li .business-info .business-info-promotion .business-info-promotion-right p {
		margin-right: 2vw;
	}

	.location-bar {
		padding: 10px;
		background-color: #f8f8f8;
		display: flex;
		align-items: center;
		font-size: 10px;
		font-weight: bold;
	}

	select {
		margin-left: 10px;
		padding: 5px;
		font-size: 10px;
		border: 3px solid white;
		border-radius: 4px;
		background-color: #f8f8f8;
		color: #333;
		outline: none;
		font-weight: bold;
	}

	/* 签到弹窗样式 */
	.sign-in-modal {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		display: flex;
		justify-content: center;
		align-items: center;
		z-index: 2000;
	}

	.sign-in-content {
		background: #fff;
		border-radius: 3vw;
		width: 80%;
		max-width: 400px;
		overflow: hidden;
	}

	.sign-in-header {
		background: #0097FF;
		color: #fff;
		padding: 4vw;
		display: flex;
		justify-content: space-between;
		align-items: center;
		position: relative;
	}

	.sign-in-header h3 {
		font-size: 4.5vw;
		margin: 0;
		flex: 1;
		text-align: center;
	}

	.close-btn {
		position: absolute;
		right: 4vw;
		font-size: 6vw;
		cursor: pointer;
		line-height: 1;
	}

	.sign-in-body {
		padding: 6vw;
		text-align: center;
	}

	.sign-in-icon {
		font-size: 12vw;
		margin-bottom: 3vw;
	}

	.sign-in-text {
		font-size: 4.5vw;
		color: #333;
		font-weight: bold;
		margin-bottom: 2vw;
	}

	.sign-in-tip {
		font-size: 3.2vw;
		color: #999;
		margin-bottom: 5vw;
	}

	.sign-in-btn {
		width: 100%;
		padding: 3vw 0;
		background: #0097FF;
		color: #fff;
		border: none;
		border-radius: 2vw;
		font-size: 4vw;
		font-weight: bold;
		cursor: pointer;
	}

	.sign-in-btn:disabled {
		background: #ccc;
		cursor: not-allowed;
	}
</style>