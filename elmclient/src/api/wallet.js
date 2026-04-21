import axios from 'axios'
import auth from '../utils/auth'

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || ''

function getCurrentUserId() {
	return auth.getUserInfo()?.id
}

function getAuthHeaders() {
	return {
		Authorization: `Bearer ${auth.getToken()}`
	}
}

function unsupported(message) {
	return Promise.resolve({
		data: {
			success: false,
			data: null,
			message
		}
	})
}

export default {
	// 获取钱包信息
	getWallet() {
		return axios.get(`${API_BASE_URL}/api/wallet`, {
			headers: getAuthHeaders()
		})
	},

	// 充值
	recharge(amount) {
		return axios.post(`${API_BASE_URL}/api/wallet/recharge`, null, {
			params: {
				userId: getCurrentUserId(),
				amount
			},
			headers: getAuthHeaders()
		})
	},

	// 提现
	withdraw() {
		return unsupported('当前后端未提供提现接口')
	},

	// 支付/转账
	pay(toUserId, amount) {
		if (toUserId) {
			return axios.post(`${API_BASE_URL}/api/wallet/transfer`, null, {
				params: {
					fromUserId: getCurrentUserId(),
					toUserId,
					amount
				},
				headers: getAuthHeaders()
			})
		}

		return axios.post(`${API_BASE_URL}/api/wallet/pay`, null, {
			params: {
				userId: getCurrentUserId(),
				amount
			},
			headers: getAuthHeaders()
		})
	},

	// 查询交易流水
	getTransactions() {
		return Promise.resolve({
			data: {
				success: true,
				data: [],
				message: '当前后端未提供交易流水接口'
			}
		})
	},

	// 冻结金额
	freeze() {
		return unsupported('当前后端未提供冻结金额接口')
	},

	// 解冻金额
	unfreeze() {
		return unsupported('当前后端未提供解冻金额接口')
	},

	// 设置透支额度（管理员功能）
	setCreditLimit() {
		return unsupported('当前后端未提供设置透支额度接口')
	},

	repayOverdraft(amount) {
		return axios.post(`${API_BASE_URL}/api/wallet/repayOverdraft`, null, {
			params: {
				userId: getCurrentUserId(),
				amount
			},
			headers: getAuthHeaders()
		})
	}
}

