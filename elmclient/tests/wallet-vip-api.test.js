import test from 'node:test'
import assert from 'node:assert/strict'
import axios from 'axios'

import auth from '../src/utils/auth.js'
import walletApi from '../src/api/wallet.js'
import vipApi from '../src/api/vip.js'
import { createResolvedCallRecorder } from './helpers/stubs.js'

const originalAuthGetToken = auth.getToken
const originalAuthGetUserInfo = auth.getUserInfo
const originalAxiosGet = axios.get
const originalAxiosPost = axios.post
const originalRepayOverdraft = walletApi.repayOverdraft

function applyWalletAuthState() {
	auth.getToken = () => 'wallet-token'
	auth.getUserInfo = () => ({ userId: 'u2001' })
}

function restoreWalletState() {
	auth.getToken = originalAuthGetToken
	auth.getUserInfo = originalAuthGetUserInfo
	axios.get = originalAxiosGet
	axios.post = originalAxiosPost
	walletApi.repayOverdraft = originalRepayOverdraft
}

test('wallet api pay with receiver uses transfer endpoint', async () => {
	try {
		applyWalletAuthState()
		const postRecorder = createResolvedCallRecorder()
		axios.post = postRecorder

		await walletApi.pay('merchant1001', 30)

		assert.deepEqual(postRecorder.calls[0], ['/api/wallet/transfer', null, {
			params: {
				fromUserId: 'u2001',
				toUserId: 'merchant1001',
				amount: 30
			},
			headers: {
				Authorization: 'Bearer wallet-token'
			}
		}])
	} finally {
		restoreWalletState()
	}
})

test('wallet api pay without receiver uses direct pay endpoint', async () => {
	try {
		applyWalletAuthState()
		const postRecorder = createResolvedCallRecorder()
		axios.post = postRecorder

		await walletApi.pay('', 18.5)

		assert.deepEqual(postRecorder.calls[0], ['/api/wallet/pay', null, {
			params: {
				userId: 'u2001',
				amount: 18.5
			},
			headers: {
				Authorization: 'Bearer wallet-token'
			}
		}])
	} finally {
		restoreWalletState()
	}
})

test('wallet api getWallet normalizes legacy common result payload', async () => {
	try {
		applyWalletAuthState()
		const getRecorder = createResolvedCallRecorder({
			data: {
				code: 200,
				message: 'success',
				result: {
					balance: 88,
					availableBalance: 66
				}
			}
		})
		axios.get = getRecorder

		const response = await walletApi.getWallet()

		assert.deepEqual(getRecorder.calls[0], ['/api/wallet', {
			headers: {
				Authorization: 'Bearer wallet-token'
			}
		}])
		assert.equal(response.data.success, true)
		assert.deepEqual(response.data.data, {
			balance: 88,
			availableBalance: 66,
			frozenAmount: 0
		})
		assert.equal(response.data.message, 'success')
	} finally {
		restoreWalletState()
	}
})

test('wallet api unsupported withdraw returns stable fallback payload', async () => {
	try {
		const response = await walletApi.withdraw()

		assert.equal(response.data.success, false)
		assert.equal(response.data.data, null)
		assert.match(response.data.message, /提现接口|鐜版帴鍙/)
	} finally {
		restoreWalletState()
	}
})

test('vip purchase posts card type with current user and auth header', async () => {
	try {
		applyWalletAuthState()
		const postRecorder = createResolvedCallRecorder()
		axios.post = postRecorder

		await vipApi.purchaseVipCard('MONTH')

		assert.deepEqual(postRecorder.calls[0], ['/api/vip/purchase', null, {
			params: {
				userId: 'u2001',
				cardType: 'MONTH'
			},
			headers: {
				Authorization: 'Bearer wallet-token'
			}
		}])
	} finally {
		restoreWalletState()
	}
})

test('vip purchase normalizes legacy common result payload', async () => {
	try {
		applyWalletAuthState()
		const postRecorder = createResolvedCallRecorder({
			data: {
				code: 200,
				message: 'success',
				result: true
			}
		})
		axios.post = postRecorder

		const response = await vipApi.purchaseVipCard('MONTH')

		assert.equal(response.data.success, true)
		assert.equal(response.data.data, true)
		assert.equal(response.data.message, 'success')
	} finally {
		restoreWalletState()
	}
})

test('vip repayOverdraft delegates to wallet api', async () => {
	try {
		const expectedResponse = { data: { success: true, data: 1 } }
		walletApi.repayOverdraft = amount => Promise.resolve({ ...expectedResponse, amount })

		const response = await vipApi.repayOverdraft(88)

		assert.deepEqual(response, {
			data: {
				success: true,
				data: 1
			},
			amount: 88
		})
	} finally {
		restoreWalletState()
	}
})
