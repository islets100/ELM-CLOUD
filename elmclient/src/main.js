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

// 添加 axios 响应拦截器，统一处理后端 CommonResult 格式
axios.interceptors.response.use(
	response => {
		const data = response.data

		// 如果后端返回的是 CommonResult 格式 {code, message, result}
		// 但不处理登录接口（它直接返回 {id_token}）
		if (data && typeof data === 'object' && 'code' in data && 'result' in data && !('id_token' in data)) {
			// 转换为前端期望的格式 {success, data, message}
			const success = data.code === 200
			return {
				...response,
				data: {
					success: success,
					data: data.result,
					message: data.message
				}
			}
		}

		// 其他情况直接返回原响应（如登录接口返回的 {id_token}）
		return response
	},
	error => {
		// 统一错误处理
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

