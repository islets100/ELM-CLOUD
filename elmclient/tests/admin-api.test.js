import test from 'node:test'
import assert from 'node:assert/strict'
import axios from 'axios'

import auth from '../src/utils/auth.js'
import { createResolvedCallRecorder, stubMethod } from './helpers/stubs.js'

let capturedRequestInterceptor = null
const restoreInterceptorUse = stubMethod(axios.interceptors.request, 'use', handler => {
	capturedRequestInterceptor = handler
	return 1
})

const { default: adminApi } = await import('../src/api/admin.js')
restoreInterceptorUse()

function restoreAdminState() {
	auth.getToken = originalAuthGetToken
	auth.getUserInfo = originalAuthGetUserInfo
	axios.get = originalAxiosGet
	axios.post = originalAxiosPost
}

const originalAuthGetToken = auth.getToken
const originalAuthGetUserInfo = auth.getUserInfo
const originalAxiosGet = axios.get
const originalAxiosPost = axios.post

test('admin api registers request interceptor that injects bearer token', () => {
	try {
		auth.getToken = () => 'admin-token'

		const config = capturedRequestInterceptor({ headers: {} })

		assert.equal(config.headers.Authorization, 'Bearer admin-token')
	} finally {
		restoreAdminState()
	}
})

test('admin login posts credentials to auth endpoint', async () => {
	try {
		const postRecorder = createResolvedCallRecorder()
		axios.post = postRecorder

		const payload = { username: 'admin', password: '123456' }
		await adminApi.login(payload)

		assert.deepEqual(postRecorder.calls[0], ['/api/auth', payload])
	} finally {
		restoreAdminState()
	}
})

test('admin getUserById sends userId as query parameter', async () => {
	try {
		const getRecorder = createResolvedCallRecorder()
		axios.get = getRecorder

		await adminApi.getUserById('u1001')

		assert.deepEqual(getRecorder.calls[0], ['/api/userById', {
			params: {
				userId: 'u1001'
			}
		}])
	} finally {
		restoreAdminState()
	}
})

test('admin addBusiness posts business payload to businesses endpoint', async () => {
	try {
		const postRecorder = createResolvedCallRecorder()
		axios.post = postRecorder

		const payload = { businessName: 'Demo Shop', userId: 'merchant1001' }
		await adminApi.addBusiness(payload)

		assert.deepEqual(postRecorder.calls[0], ['/api/businesses', payload])
	} finally {
		restoreAdminState()
	}
})
