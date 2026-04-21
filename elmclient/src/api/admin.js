import axios from 'axios'
import auth from '../utils/auth'

axios.interceptors.request.use(config => {
	const token = auth.getToken();
	if (token) {
		config.headers.Authorization = `Bearer ${token}`;
	}
	return config;
});

// 管理员专属接口（严格对接你提供的后端接口）
export default {
	// 管理员登录（对接/api/auth）
	login(loginDto) {
		return axios.post('/api/auth', loginDto)
	},
	// 获取当前登录用户（对接/api/user）
	getCurrentUser() {
		return axios.get('/api/user')
	},
	// 新增用户（仅登录账号，对接/api/users）
	createUser(userData) {
		return axios.post('/api/users', userData)
	},
	// 新增自然人（店主/顾客，对接/api/persons）
	addPerson(personData) {
		return axios.post('/api/persons', personData)
	},
	// 修改密码（对接/api/password）
	updatePassword(loginDto) {
		return axios.post('/api/password', loginDto)
	},
	// 按ID查用户是否存在（对接/api/userById）
	getUserById(userId) {
		return axios.get('/api/userById', {
			params: {
				userId
			}
		})
	},
	// 按ID+密码查用户（对接/api/userByIdByPass）
	getUserByIdAndPassword(userId, password) {
		return axios.get('/api/userByIdByPass', {
			params: {
				userId,
				password
			}
		})
	},
	// 新增店铺接口
	addBusiness(businessData) {
		return axios.post('/api/businesses', businessData)
	},
	//新增查询所有用户列表
	getAllUsers() {
		return axios.get('/api/users')
	}
}