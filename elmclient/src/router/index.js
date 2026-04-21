import {
	createRouter,
	createWebHistory
} from 'vue-router'
import Index from '../views/Index.vue'
import BusinessList from '../views/BusinessList.vue'
import BusinessInfo from '../views/BusinessInfo.vue'
import Login from '../views/Login.vue'
import Orders from '../views/Orders.vue'
import UserAddress from '../views/UserAddress.vue'
import Payment from '../views/Payment.vue'
import OrderList from '../views/OrderList.vue'
import AddUserAddress from '../views/AddUserAddress.vue'
import EditUserAddress from '../views/EditUserAddress.vue'
import Register from '../views/Register.vue'

import BusinessOrders from '../views/BusinessOrders.vue'
import Business from '../views/Business.vue'
import BusinessRegister from '../views/BusinessRegister.vue'
import BusinessManage from '../views/BusinessManage.vue'

// admin目录下
import AdminLogin from '../views/admin/AdminLogin.vue'
import UserManagement from '../views/admin/UserManagement.vue'
import AddUser from '../views/admin/AddUser.vue'
import RewardRuleManagement from '../views/admin/RewardRuleManagement.vue'

import auth from '../utils/auth'

const routes = [
	// 首页和公共页面
	{
		path: '/',
		name: 'Home',
		component: Index
	},
	{
		path: '/index',
		name: 'Index',
		component: Index
	},
	{
		path: '/businessList',
		name: 'BusinessList',
		component: BusinessList
	},
	{
		path: '/businessInfo',
		name: 'BusinessInfo',
		component: BusinessInfo
	},
	{
		path: '/login',
		name: 'Login',
		component: Login,
		meta: {
			isPublic: true
		}
	},
	{
		path: '/register',
		name: 'Register',
		component: Register,
		meta: {
			isPublic: true
		}
	},
	{
		path: '/orders',
		name: 'Orders',
		component: Orders
	},
	{
		path: '/userAddress',
		name: 'UserAddress',
		component: UserAddress
	},
	{
		path: '/payment',
		name: 'Payment',
		component: Payment
	},
	{
		path: '/orderList',
		name: 'OrderList',
		component: OrderList
	},
	{
		path: '/addUserAddress',
		name: 'AddUserAddress',
		component: AddUserAddress
	},
	// 编辑用户地址保留动态参数
	{
		path: '/editAddress/:id',
		name: 'EditUserAddress',
		component: EditUserAddress
	},

	// 商家路由
	{
		path: '/businessRegister',
		name: 'BusinessRegister',
		component: BusinessRegister
	},
	{
		path: '/businessOrders',
		name: 'BusinessOrders',
		component: BusinessOrders
	},
	{
		path: '/business',
		name: 'Business',
		component: Business
	},
	{
		path: '/businessManage',
		name: 'BusinessManage',
		component: BusinessManage,
		meta: {
			isBusiness: true
		}
	},

	// admin路由
	{
		path: '/admin/login',
		name: 'AdminLogin',
		component: AdminLogin,
		meta: {
			isPublic: true
		}
	},
	{
		path: '/admin/users',
		name: 'UserManagement',
		component: UserManagement,
		meta: {
			requiresAuth: true,
			isAdmin: true
		}
	},
	{
		path: '/admin/add-user', // 使用第二份的新路径
		name: 'AddUser',
		component: AddUser,
		meta: {
			requiresAuth: true,
			isAdmin: true
		}
	},
	{
		path: '/admin/add-business',
		name: 'AddBusiness',
		component: () => import('../views/admin/AddBusiness.vue'),
		meta: {
			requiresAuth: true,
			isAdmin: true // 只有管理员才能访问
		}
	},
	{
		path: '/admin/reward-rules',
		name: 'RewardRuleManagement',
		component: RewardRuleManagement,
		meta: {
			requiresAuth: true,
			isAdmin: true // 只有管理员才能访问
		}
	},

	// 普通用户路由
	{
		path: '/user/info',
		name: 'UserInfo',
		component: () => import('../views/UserInfo.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
	  path: '/user/password',
		  name: 'PasswordUpdate',
		  component: () => import('@/views/PasswordUpdate.vue'),
		  meta: {
		    requiresAuth: true // 需要登录才能访问
		  }
		},
	{
		path: '/user/birthday',
		name: 'BirthdaySetting',
		component: () => import('../views/BirthdaySetting.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/user',
		name: 'UserHome',
		component: () => import('../views/UserHome.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/userOrderList',
		name: 'UserOrderList',
		component: () => import('../views/UserOrderList.vue'),
		meta: {
			requiresAuth: true
		}
	},
	// 钱包相关路由
	{
		path: '/wallet',
		name: 'Wallet',
		component: () => import('../views/Wallet.vue'),
		meta: {
			requiresAuth: true
		}
	},
	// 积分相关路由
	{
		path: '/points',
		name: 'Points',
		component: () => import('../views/Points.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/points/mall',
		name: 'PointsMall',
		component: () => import('../views/PointsMall.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/points/test',
		name: 'PointsTest',
		component: () => import('../views/PointsTest.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/availableIntegralDetails',
		name: 'AvailableIntegralDetails',
		component: () => import('../views/AvailableIntegralDetails.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/wallet/recharge',
		name: 'WalletRecharge',
		component: () => import('../views/WalletRecharge.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/wallet/withdraw',
		name: 'WalletWithdraw',
		component: () => import('../views/WalletWithdraw.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/wallet/transactions',
		name: 'WalletTransactions',
		component: () => import('../views/WalletTransactions.vue'),
		meta: {
			requiresAuth: true
		}
	},
	// VIP相关路由
	{
		path: '/vip/center',
		name: 'VipCenter',
		component: () => import('../views/VipCenter.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/vip/purchase',
		name: 'VipPurchase',
		component: () => import('../views/VipPurchase.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/vip/repay',
		name: 'VipRepay',
		component: () => import('../views/VipRepay.vue'),
		meta: {
			requiresAuth: true
		}
	},
	{
		path: '/vip/history',
		name: 'VipHistory',
		component: () => import('../views/VipHistory.vue'),
		meta: {
			requiresAuth: true
		}
	}
]

// 创建路由
const router = createRouter({
	history: createWebHistory(process.env.BASE_URL),
	routes
})

// 防止重复点击报错
const originalPush = router.push
router.push = function push(location) {
	return originalPush.call(this, location).catch(err => err)
}

// 路由守卫
router.beforeEach((to, from, next) => {
	const user = auth.getUserInfo()
	console.log('路由守卫触发 → to:', to.fullPath, ', from:', from.fullPath)
	console.log('当前用户信息:', user)

	// 公共路由直接放行
	if (to.meta.isPublic) {
		if (auth.isAdmin() && to.path === '/admin/login') {
			return next('/admin/users')
		}
		return next()
	}

	// 管理员页面权限控制
	if (to.meta.isAdmin) {
		if (!user || !auth.isAdmin()) {
			return next({
				path: '/admin/login',
				query: {
					redirect: to.fullPath
				}
			})
		}
	}

	// 商家页面权限控制
	if (to.meta.isBusiness) {
		if (!user || !auth.isBusiness()) {
			return next({
				path: '/login',
				query: {
					redirect: to.fullPath
				}
			})
		}
	}

	// “我的”页面需要登录
	if (to.meta.requiresAuth) {
		if (!user) {
			return next({
				path: '/login',
				query: {
					redirect: to.fullPath
				}
			})
		}
	}

	next()
})

export default router