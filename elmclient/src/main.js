import {
	createApp
} from 'vue'
import App from './App.vue'
import router from './router'

// 引入 element-plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'  // 引入样式


import 'font-awesome/css/font-awesome.min.css'
import axios from 'axios'
import qs from 'qs'
import { normalizeApiData, normalizeCommonResult } from './utils/apiResult.js'
import { isFallbackResponse, shouldSuppressNativeAlert, showFallbackPrompt } from './utils/fallbackRetry.js'
import {
	getCurDate,
	setSessionStorage,
	getSessionStorage,
	removeSessionStorage,
	setLocalStorage,
	getLocalStorage,
	removeLocalStorage
} from './common.js'

// 设置 axios 的基础 url
axios.defaults.baseURL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:14000/';

const originalAlert = window.alert.bind(window)
window.alert = message => {
	if (shouldSuppressNativeAlert()) {
		return
	}

	originalAlert(message)
}

function isCommonResultPayload(data) {
	return !!(
		data &&
		typeof data === 'object' &&
		('code' in data || 'result' in data || 'success' in data || 'data' in data) &&
		!('id_token' in data)
	)
}

function normalizeAxiosResponse(response) {
	const data = response?.data

	if (isCommonResultPayload(data)) {
		return {
			...response,
			data: normalizeCommonResult(data)
		}
	}

	return {
		...response,
		data: normalizeApiData(data)
	}
}

function maybeShowFallbackPrompt(response) {
	const { data, config } = response || {}

	if (
		isFallbackResponse(data) &&
		config?.__retryable &&
		!config.__skipFallbackPrompt
	) {
		showFallbackPrompt(
			{
				...data.data,
				message: data.message
			},
			config
		)
	}
}

// 请求拦截器：打标记并保留可重放配置
axios.interceptors.request.use(config => {
	config.__requestId = Math.random().toString(36).slice(2)
	config.__retryable = !config.__skipFallbackRetry
	config.__startedAt = Date.now()

	return config
}, error => Promise.reject(error))

// 添加 axios 响应拦截器，统一处理后端 CommonResult 格式，并识别网关 fallback
axios.interceptors.response.use(
	response => {
		const normalizedResponse = normalizeAxiosResponse(response)
		maybeShowFallbackPrompt(normalizedResponse)
		return normalizedResponse
	},
	error => {
		if (error?.response) {
			const normalizedResponse = normalizeAxiosResponse(error.response)
			error.response = normalizedResponse
			maybeShowFallbackPrompt(normalizedResponse)
		}

		console.error('API Error:', error)
		return Promise.reject(error)
	}
)

// 创建 Vue 应用实例
const app = createApp(App)

// 挂载全局属性，Vue 3 不能用 Vue.prototype，改成 app.config.globalProperties
app.config.globalProperties.$axios = axios
app.config.globalProperties.$qs = qs
app.config.globalProperties.$getCurDate = getCurDate
app.config.globalProperties.$setSessionStorage = setSessionStorage
app.config.globalProperties.$getSessionStorage = getSessionStorage
app.config.globalProperties.$removeSessionStorage = removeSessionStorage
app.config.globalProperties.$setLocalStorage = setLocalStorage
app.config.globalProperties.$getLocalStorage = getLocalStorage
app.config.globalProperties.$removeLocalStorage = removeLocalStorage

/*
// 路由全局守卫
router.beforeEach((to, from, next) => {
	// let user = sessionStorage.getItem('user')
	// // 除了登录、注册、首页、商家列表、商家信息之外，都需要判断是否登录
	// if (!(
	//   to.path === '/' ||
	//   to.path === '/index' ||
	//   to.path === '/businessList' ||
	//   to.path === '/businessInfo' ||
	//   to.path === '/login' ||
	//   to.path === '/register'
	// )) {
	//   if (user == null) {
	//     router.push('/login')
	//     location.reload()
	//   }
	// }
	// next()
	const user = sessionStorage.getItem('user')
	const publicPaths = ['/', '/index', '/businessList', '/businessInfo', '/login', '/register']
	// 如果访问受保护页且未登录 → 强制跳到登录，并带上重定向
	if (!publicPaths.includes(to.path) && !user) {
		return next({
			path: '/login',
			query: {
				redirect: to.fullPath
			}
		})
	}
	// 如果已登录去登录页 → 自动跳回首页
	if (user && to.path === '/login') {
		return next('/index')
	}
	next()
})

*/

// 挂载路由
app.use(router)

// 挂载应用到页面
app.mount('#app')

