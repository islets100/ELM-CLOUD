import axios from 'axios'
import auth from '../utils/auth.js'

export default {
	updateUserInfo(userId, username, birthday) {
		return axios.put(`/api/users/${userId}`, {
			username: username,
			birthday: birthday
		}, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	},

	updatePassword(loginDto) {
		return axios.post('/api/password', loginDto, {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`,
				'Content-Type': 'application/json'
			}
		})
	},

	// 检查用户是否可以修改生日
	checkBirthdayModification() {
		return axios.get('/api/user/birthday/modification/check', {
			headers: {
				'Authorization': `Bearer ${auth.getToken()}`
			}
		})
	}

	// 可以添加其他用户相关接口
	// getUserDetail(userId) { ... }
}
