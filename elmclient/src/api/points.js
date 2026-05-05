import axios from 'axios'
import auth from '../utils/auth.js'

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || ''

function getAuthHeaders() {
	return {
		Authorization: `Bearer ${auth.getToken()}`
	}
}

function getCurrentUserId() {
	const user = auth.getUserInfo()
	return user?.id
}

export default {
	getStatistics() {
		return axios.get(`${API_BASE_URL}/api/integral/statistics/${getCurrentUserId()}`, {
			headers: getAuthHeaders()
		})
	},

	getDetails() {
		return axios.get(`${API_BASE_URL}/api/integral/details/${getCurrentUserId()}`, {
			headers: getAuthHeaders()
		})
	},

	getAvailable() {
		return axios.get(`${API_BASE_URL}/api/integral/available/${getCurrentUserId()}`, {
			headers: getAuthHeaders()
		})
	},

	getAvailableDetails() {
		return axios.get(`${API_BASE_URL}/api/integral/available-details/${getCurrentUserId()}`, {
			headers: getAuthHeaders()
		})
	},

	signIn() {
		return axios.post(`${API_BASE_URL}/api/integral/sign-in/${getCurrentUserId()}`, null, {
			headers: getAuthHeaders()
		})
	},

	checkSignInToday() {
		return axios.get(`${API_BASE_URL}/api/integral/sign-in/check/${getCurrentUserId()}`, {
			headers: getAuthHeaders(),
			__skipFallbackPrompt: true,
			__skipFallbackRetry: true
		})
	},

	comment(orderId, content, hasPicture) {
		return axios.post(`${API_BASE_URL}/api/integral/comment`, null, {
			params: {
				userId: getCurrentUserId(),
				orderId,
				content,
				hasPicture
			},
			headers: getAuthHeaders()
		})
	},

	consumeIntegral(amount, channel, businessId, description) {
		return axios.post(`${API_BASE_URL}/api/integral/consume`, null, {
			params: {
				userId: getCurrentUserId(),
				amount,
				channel,
				businessId,
				description
			},
			headers: getAuthHeaders()
		})
	},

	addIntegralTest(amount) {
		return axios.post(`${API_BASE_URL}/api/integral/add`, null, {
			params: {
				userId: getCurrentUserId(),
				amount,
				channel: 'ACTIVITY',
				description: '测试积分增加'
			},
			headers: getAuthHeaders()
		})
	},

	checkBirthdayIntegral() {
		return axios.get(`${API_BASE_URL}/api/integral/birthday/check/${getCurrentUserId()}`, {
			headers: getAuthHeaders()
		})
	},

	claimBirthdayIntegral() {
		return axios.post(`${API_BASE_URL}/api/integral/birthday/claim/${getCurrentUserId()}`, null, {
			headers: getAuthHeaders()
		})
	},

	isTodayPromotionDay() {
		return axios.get(`${API_BASE_URL}/api/promotion-day/today`, {
			headers: getAuthHeaders()
		})
	},

	checkCanClaimRedPacket() {
		return axios.get(`${API_BASE_URL}/api/promotion-day/red-packet/check`, {
			params: {
				userId: getCurrentUserId()
			},
			headers: getAuthHeaders()
		})
	},

	claimRedPacket() {
		return axios.post(`${API_BASE_URL}/api/promotion-day/red-packet/claim`, null, {
			params: {
				userId: getCurrentUserId()
			},
			headers: getAuthHeaders()
		})
	},

	getRedPacketRecords() {
		return axios.get(`${API_BASE_URL}/api/promotion-day/red-packet/records`, {
			params: {
				userId: getCurrentUserId()
			},
			headers: getAuthHeaders()
		})
	}
}
