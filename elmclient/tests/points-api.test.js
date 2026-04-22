import test from 'node:test'
import assert from 'node:assert/strict'
import axios from 'axios'

import auth from '../src/utils/auth.js'
import pointsApi from '../src/api/points.js'
import { createResolvedCallRecorder } from './helpers/stubs.js'

const originalAuthGetToken = auth.getToken
const originalAuthGetUserInfo = auth.getUserInfo
const originalAxiosGet = axios.get
const originalAxiosPost = axios.post

function applyPointsAuthState() {
	auth.getToken = () => 'points-token'
	auth.getUserInfo = () => ({ id: 'u1001' })
}

function restorePointsState() {
	auth.getToken = originalAuthGetToken
	auth.getUserInfo = originalAuthGetUserInfo
	axios.get = originalAxiosGet
	axios.post = originalAxiosPost
}

test('points api getAvailable requests current user balance with auth header', async () => {
	try {
		applyPointsAuthState()
		const getRecorder = createResolvedCallRecorder()
		axios.get = getRecorder

		await pointsApi.getAvailable()

		assert.deepEqual(getRecorder.calls[0], ['/api/integral/available/u1001', {
			headers: {
				Authorization: 'Bearer points-token'
			}
		}])
	} finally {
		restorePointsState()
	}
})

test('points api comment posts order comment with query params and auth header', async () => {
	try {
		applyPointsAuthState()
		const postRecorder = createResolvedCallRecorder()
		axios.post = postRecorder

		await pointsApi.comment(12, 'great', true)

		assert.deepEqual(postRecorder.calls[0], ['/api/integral/comment', null, {
			params: {
				userId: 'u1001',
				orderId: 12,
				content: 'great',
				hasPicture: true
			},
			headers: {
				Authorization: 'Bearer points-token'
			}
		}])
	} finally {
		restorePointsState()
	}
})

test('points api claimRedPacket posts current user id in params', async () => {
	try {
		applyPointsAuthState()
		const postRecorder = createResolvedCallRecorder()
		axios.post = postRecorder

		await pointsApi.claimRedPacket()

		assert.deepEqual(postRecorder.calls[0], ['/api/promotion-day/red-packet/claim', null, {
			params: {
				userId: 'u1001'
			},
			headers: {
				Authorization: 'Bearer points-token'
			}
		}])
	} finally {
		restorePointsState()
	}
})
