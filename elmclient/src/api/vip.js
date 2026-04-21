import axios from 'axios'
import auth from '../utils/auth'

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || ''

export default {
	// 购买VIP卡
	purchaseVipCard(cardType) {
		return axios.post(`${API_BASE_URL}/api/vip/purchase`, {
			cardType: cardType
		}, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 获取当前有效VIP卡
	getCurrentVipCard() {
		return axios.get(`${API_BASE_URL}/api/vip/current`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 获取VIP卡历史记录
	getVipCardHistory() {
		return axios.get(`${API_BASE_URL}/api/vip/history`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 管理员设置透支额度
	setCreditLimit(userId, creditLimit) {
		return axios.post(`${API_BASE_URL}/api/vip/credit-limit`, {
			userId: userId,
			creditLimit: creditLimit
		}, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 透支还款
	repayOverdraft(amount) {
		return axios.post(`${API_BASE_URL}/api/vip/repay`, {
			amount: amount
		}, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 获取透支记录
	getOverdraftRecords() {
		return axios.get(`${API_BASE_URL}/api/vip/overdraft-records`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 检查VIP状态
	checkVipStatus() {
		return axios.get(`${API_BASE_URL}/api/vip/check`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	}
}
