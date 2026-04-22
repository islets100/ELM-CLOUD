import test from 'node:test'
import assert from 'node:assert/strict'

import auth from '../src/utils/auth.js'
import { installStorageMocks } from './helpers/storage.js'

test.beforeEach(() => {
	installStorageMocks()
})

test('auth stores and reads token', () => {
	auth.setToken('jwt-token')

	assert.equal(auth.getToken(), 'jwt-token')
	assert.equal(auth.isLoggedIn(), true)
})

test('auth stores and reads user info and role flags', () => {
	auth.setUserInfo({
		userId: 'u1001',
		authorities: [
			{ name: 'USER' },
			{ name: 'BUSINESS' }
		]
	})

	assert.deepEqual(auth.getUserInfo(), {
		id: 'u1001',
		userId: 'u1001',
		authorities: [
			{ name: 'USER' },
			{ name: 'BUSINESS' }
		]
	})
	assert.equal(auth.isUser(), true)
	assert.equal(auth.isBusiness(), true)
	assert.equal(auth.isAdmin(), false)
})

test('auth normalizes id and userId aliases', () => {
	auth.setUserInfo({
		id: 'merchant1001',
		authorities: [{ name: 'BUSINESS' }]
	})

	assert.deepEqual(auth.getUserInfo(), {
		id: 'merchant1001',
		userId: 'merchant1001',
		authorities: [{ name: 'BUSINESS' }]
	})
})

test('auth normalizes legacy userName field', () => {
	auth.setUserInfo({
		userId: 'u1001',
		userName: 'Alice',
		authorities: [{ name: 'USER' }]
	})

	assert.deepEqual(auth.getUserInfo(), {
		id: 'u1001',
		userId: 'u1001',
		username: 'Alice',
		userName: 'Alice',
		authorities: [{ name: 'USER' }]
	})
})

test('logout clears token and user info', () => {
	auth.setToken('jwt-token')
	auth.setUserInfo({
		userId: 'admin',
		authorities: [{ name: 'ADMIN' }]
	})

	auth.logout()

	assert.equal(auth.getToken(), null)
	assert.equal(auth.getUserInfo(), null)
	assert.equal(auth.isLoggedIn(), false)
})
