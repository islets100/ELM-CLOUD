import axios from 'axios'
import auth from '../utils/auth.js'

axios.interceptors.request.use(config => {
	const token = auth.getToken();
	if (token) {
		config.headers.Authorization = `Bearer ${token}`;
	}
	return config;
});

export default {
	login(loginDto) {
		return axios.post('/api/auth', loginDto)
	},
	getCurrentUser() {
		return axios.get('/api/user')
	},
	createUser(userData) {
		return axios.post('/api/users', userData)
	},
	addPerson(personData) {
		return axios.post('/api/persons', personData)
	},
	updatePassword(loginDto) {
		return axios.post('/api/password', loginDto)
	},
	getUserById(userId) {
		return axios.get('/api/userById', { params: { userId } })
	},
	getUserByIdAndPassword(userId, password) {
		return axios.get('/api/userByIdByPass', { params: { userId, password } })
	},
	addBusiness(businessData) {
		return axios.post('/api/businesses', businessData)
	},
	getAllUsers() {
		return axios.get('/api/users')
	},
	userExistsByUsername(userName) {
		return axios.get('/api/userExistsByUsername', { params: { userName } })
	},
	getUserByUsername(userName) {
		return axios.get('/api/userByUsername', { params: { userName } })
	}
}
