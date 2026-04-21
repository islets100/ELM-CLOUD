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

import AdminLogin from '../views/admin/AdminLogin.vue'
import UserManagement from '../views/admin/UserManagement.vue'
import AddUser from '../views/admin/AddUser.vue'
import RewardRuleManagement from '../views/admin/RewardRuleManagement.vue'

import auth from '../utils/auth'

const routes = [
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
	{
		path: '/editAddress/:id',
		name: 'EditUserAddress',
		component: EditUserAddress
	},
	{
		path: '/businessRegister',
		name: 'BusinessRegister',
		component: BusinessRegister,
		meta: {
			isBusiness: true
		}
	},
	{
		path: '/businessOrders',
		name: 'BusinessOrders',
		component: BusinessOrders,
		meta: {
			isBusiness: true
		}
	},
	{
		path: '/business',
		name: 'Business',
		component: Business,
		meta: {
			isBusiness: true
		}
	},
	{
		path: '/businessManage',
		name: 'BusinessManage',
		component: BusinessManage,
		meta: {
			isBusiness: true
		}
	},
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
		path: '/admin/add-user',
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
			isAdmin: true
		}
	},
	{
		path: '/admin/reward-rules',
		name: 'RewardRuleManagement',
		component: RewardRuleManagement,
		meta: {
			requiresAuth: true,
			isAdmin: true
		}
	},
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
			requiresAuth: true
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
	{
		path: '/wallet',
		name: 'Wallet',
		component: () => import('../views/Wallet.vue'),
		meta: {
			requiresAuth: true
		}
	},
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

const router = createRouter({
	history: createWebHistory(process.env.BASE_URL),
	routes
})

const originalPush = router.push
router.push = function push(location) {
	return originalPush.call(this, location).catch(err => err)
}

router.beforeEach((to, from, next) => {
	const user = auth.getUserInfo()

	if (to.meta.isPublic) {
		if (auth.isAdmin() && to.path === '/admin/login') {
			return next('/admin/users')
		}
		return next()
	}

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
