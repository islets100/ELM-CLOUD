import axios from 'axios'
import auth from '../utils/auth'

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || ''

export default {
	// 获取积分统计信息
	getStatistics() {
		return axios.get(`${API_BASE_URL}/api/integral/statistics`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 获取积分明细
	getDetails() {
		return axios.get(`${API_BASE_URL}/api/integral/details`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 获取可用积分
	getAvailable() {
		return axios.get(`${API_BASE_URL}/api/integral/available`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 获取可用积分的详细信息
	getAvailableDetails() {
		return axios.get(`${API_BASE_URL}/api/integral/available-details`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 签到
	signIn() {
		return axios.post(`${API_BASE_URL}/api/integral/sign-in`, null, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 检查今天是否已签到
	checkSignInToday() {
		return axios.get(`${API_BASE_URL}/api/integral/sign-in/check`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 订单评论获取积分（已确认收货后使用）
	comment(orderId, content, hasPicture) {
		return axios.post(`${API_BASE_URL}/api/integral/comment`, null, {
			params: {
				orderId,
				content,
				hasPicture
			},
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 消费积分（积分商城兑换）
	consumeIntegral(amount, channel, businessId, description) {
		return axios.post(`${API_BASE_URL}/api/integral/consume`, null, {
			params: {
				amount,
				channel,
				businessId,
				description
			},
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 测试功能：增加积分
	addIntegralTest(amount) {
		const user = auth.getUserInfo()
		if (!user || !user.id) {
			return Promise.reject(new Error('用户未登录或用户信息不完整'))
		}
		return axios.post(`${API_BASE_URL}/api/integral/add`, null, {
			params: {
				userId: user.id,
				amount: amount,
				channel: 'ACTIVITY',
				description: '活动赠与'
			},
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 检查是否可以领取生日积分
	checkBirthdayIntegral() {
		return axios.get(`${API_BASE_URL}/api/integral/birthday/check`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 领取生日积分
	claimBirthdayIntegral() {
		return axios.post(`${API_BASE_URL}/api/integral/birthday/claim`, null, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 检查今天是否是促销日
	isTodayPromotionDay() {
		return axios.get(`${API_BASE_URL}/api/promotion-day/today`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 检查是否可以领取红包
	checkCanClaimRedPacket() {
		return axios.get(`${API_BASE_URL}/api/promotion-day/red-packet/check`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	// 领取红包
	claimRedPacket() {
		return axios.post(`${API_BASE_URL}/api/promotion-day/red-packet/claim`, null, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 获取红包领取记录
	getRedPacketRecords() {
		return axios.get(`${API_BASE_URL}/api/promotion-day/red-packet/records`, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	}
}
