import test from 'node:test'
import assert from 'node:assert/strict'
import axios from 'axios'

import auth from '../src/utils/auth.js'
import userApi from '../src/api/user.js'
import { createResolvedCallRecorder } from './helpers/stubs.js'

const originalAuthGetToken = auth.getToken
const originalAxiosGet = axios.get
const originalAxiosPost = axios.post
const originalAxiosPut = axios.put

function applyUserAuthState() {
	auth.getToken = () => 'user-token'
}

function restoreUserState() {
	auth.getToken = originalAuthGetToken
	axios.get = originalAxiosGet
	axios.post = originalAxiosPost
	axios.put = originalAxiosPut
}

test('user api updateUserInfo sends payload and bearer token', async () => {
	try {
		applyUserAuthState()
		const putRecorder = createResolvedCallRecorder()
		axios.put = putRecorder

		await userApi.updateUserInfo('u1001', 'demo', '2026-04-22')

		assert.deepEqual(putRecorder.calls[0], ['/api/users/u1001', {
			username: 'demo',
			birthday: '2026-04-22'
		}, {
			headers: {
				Authorization: 'Bearer user-token'
			}
		}])
	} finally {
		restoreUserState()
	}
})

test('user api updatePassword posts json payload with auth header', async () => {
	try {
		applyUserAuthState()
		const postRecorder = createResolvedCallRecorder()
		axios.post = postRecorder

		const payload = {
			oldPassword: '123456',
			newPassword: '654321'
		}

		await userApi.updatePassword(payload)

		assert.deepEqual(postRecorder.calls[0], ['/api/password', payload, {
			headers: {
				Authorization: 'Bearer user-token',
				'Content-Type': 'application/json'
			}
		}])
	} finally {
		restoreUserState()
	}
})

test('user api checkBirthdayModification requests status with auth header', async () => {
	try {
		applyUserAuthState()
		const getRecorder = createResolvedCallRecorder()
		axios.get = getRecorder

		await userApi.checkBirthdayModification()

		assert.deepEqual(getRecorder.calls[0], ['/api/user/birthday/modification/check', {
			headers: {
				Authorization: 'Bearer user-token'
			}
		}])
	} finally {
		restoreUserState()
	}
})
