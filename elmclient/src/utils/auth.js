import {
	getLocalStorage,
	setLocalStorage,
	removeLocalStorage
} from '../common.js'
import { normalizeUserInfoShape } from './apiResult.js'

const TOKEN_KEY = 'token' // 通用令牌 key
const USER_INFO_KEY = 'user_info' // 通用用户信息 key

export default {
	// ===================== Token 操作 =====================
	setToken(token) {
		// Token 是字符串，直接存储，不使用 JSON 序列化
		localStorage.setItem(TOKEN_KEY, token)
	},

	getToken() {
		// Token 是字符串，直接获取，不使用 JSON 解析
		return localStorage.getItem(TOKEN_KEY)
	},

	removeToken() {
		removeLocalStorage(TOKEN_KEY)
	},

	// ===================== 用户信息操作 =====================
	setUserInfo(info) {
		setLocalStorage(USER_INFO_KEY, normalizeUserInfoShape(info))
	},

	getUserInfo() {
		return normalizeUserInfoShape(getLocalStorage(USER_INFO_KEY))
	},

	removeUserInfo() {
		removeLocalStorage(USER_INFO_KEY)
	},

	// ===================== 登录状态判断 =====================
	isLoggedIn() {
		return !!this.getToken()
	},

	isAdmin() {
		const user = this.getUserInfo()
		return user?.authorities?.some(auth => auth.name === 'ADMIN')
	},

	isBusiness() {
		const user = this.getUserInfo()
		return user?.authorities?.some(auth => auth.name === 'BUSINESS')
	},

	isUser() {
		const user = this.getUserInfo()
		return user?.authorities?.some(auth => auth.name === 'USER')
	},

	// ===================== 退出登录 =====================
	logout() {
		this.removeToken()
		this.removeUserInfo()
	}
}
