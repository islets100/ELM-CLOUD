import axios from 'axios'
import auth from '../utils/auth.js'
import walletApi from './wallet.js'
import { normalizeAxiosResult, resolveUserId } from '../utils/apiResult.js'

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || ''

function getCurrentUserId() {
	return resolveUserId(auth.getUserInfo())
}

function getAuthHeaders() {
	return {
		Authorization: `Bearer ${auth.getToken()}`
	}
}

export default {
	// 购买VIP卡
	purchaseVipCard(cardType) {
		return normalizeAxiosResult(axios.post(`${API_BASE_URL}/api/vip/purchase`, null, {
			params: {
				userId: getCurrentUserId(),
				cardType
			},
			headers: getAuthHeaders()
		}))
	},

	// 获取当前有效VIP卡
	getCurrentVipCard() {
		return Promise.resolve({
			data: {
				success: true,
				data: null,
				message: '当前后端未提供VIP详情接口'
			}
		})
	},

	// 获取VIP卡历史记录
	getVipCardHistory() {
		return Promise.resolve({
			data: {
				success: true,
				data: [],
				message: '当前后端未提供VIP历史接口'
			}
		})
	},

	// 管理员设置透支额度
	setCreditLimit() {
		return Promise.resolve({
			data: {
				success: false,
				data: null,
				message: '当前后端未提供VIP透支额度管理接口'
			}
		})
	},

	// 透支还款
	repayOverdraft(amount) {
		return walletApi.repayOverdraft(amount)
	},

	// 获取透支记录
	getOverdraftRecords() {
		return Promise.resolve({
			data: {
				success: true,
				data: [],
				message: '当前后端未提供透支记录接口'
			}
		})
	},

	// 检查VIP状态
	checkVipStatus() {
		return Promise.resolve({
			data: {
				success: true,
				data: false,
				message: '当前后端未提供VIP状态检查接口'
			}
		})
	}
}
