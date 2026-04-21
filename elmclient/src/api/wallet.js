import axios from 'axios'
import auth from '../utils/auth'

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || ''

export default {
	// 获取钱包信息
	getWallet() {
		return axios.get(`${API_BASE_URL}/api/wallet`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 充值
	recharge(amount) {
		return axios.post(`${API_BASE_URL}/api/wallet/recharge`, {
			amount: amount
		}, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 提现
	withdraw(amount) {
		return axios.post(`${API_BASE_URL}/api/wallet/withdraw`, {
			amount: amount
		}, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 支付（转账）
	pay(toUserId, amount, orderId) {
		return axios.post(`${API_BASE_URL}/api/wallet/pay`, {
			toUserId: toUserId,
			amount: amount,
			orderId: orderId
		}, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 查询交易流水
	getTransactions() {
		return axios.get(`${API_BASE_URL}/api/wallet/transactions`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 冻结金额
	freeze(amount) {
		return axios.post(`${API_BASE_URL}/api/wallet/freeze`, {
			amount: amount
		}, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 解冻金额
	unfreeze(amount) {
		return axios.post(`${API_BASE_URL}/api/wallet/unfreeze`, {
			amount: amount
		}, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 设置透支额度（管理员功能）
	setCreditLimit(userId, creditLimit) {
		return axios.post(`${API_BASE_URL}/api/wallet/credit-limit`, null, {
			params: {
				userId: userId,
				creditLimit: creditLimit
			},
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	}
}

