import test from 'node:test'
import assert from 'node:assert/strict'

import {
	getAuthorityNames,
	resolvePostLoginPath,
	resolveRouteRedirect
} from '../src/router/routeAccess.js'

function createAuthState(overrides = {}) {
	return {
		getUserInfo: () => null,
		isAdmin: () => false,
		isBusiness: () => false,
		...overrides
	}
}

test('getAuthorityNames extracts valid authority names', () => {
	assert.deepEqual(getAuthorityNames({
		authorities: [{ name: 'ADMIN' }, { name: 'USER' }, {}]
	}), ['ADMIN', 'USER'])
})

test('resolvePostLoginPath routes users by role', () => {
	assert.equal(resolvePostLoginPath({
		authorities: [{ name: 'ADMIN' }]
	}, '/orders'), '/admin/users')

	assert.equal(resolvePostLoginPath({
		authorities: [{ name: 'BUSINESS' }]
	}, '/orders'), '/business')

	assert.equal(resolvePostLoginPath({
		authorities: [{ name: 'USER' }]
	}, '/orders'), '/orders')
})

test('resolveRouteRedirect sends anonymous user to login for protected routes', () => {
	const redirect = resolveRouteRedirect({
		path: '/wallet',
		fullPath: '/wallet',
		meta: { requiresAuth: true }
	}, createAuthState())

	assert.deepEqual(redirect, {
		path: '/login',
		query: { redirect: '/wallet' }
	})
})

test('resolveRouteRedirect sends non-admin user to admin login for admin routes', () => {
	const user = { userId: 'u1001', authorities: [{ name: 'USER' }] }
	const redirect = resolveRouteRedirect({
		path: '/admin/users',
		fullPath: '/admin/users',
		meta: { isAdmin: true, requiresAuth: true }
	}, createAuthState({
		getUserInfo: () => user
	}))

	assert.deepEqual(redirect, {
		path: '/admin/login',
		query: { redirect: '/admin/users' }
	})
})

test('resolveRouteRedirect allows business route for business user', () => {
	const user = { userId: 'merchant1001', authorities: [{ name: 'BUSINESS' }] }
	const redirect = resolveRouteRedirect({
		path: '/business',
		fullPath: '/business',
		meta: { isBusiness: true }
	}, createAuthState({
		getUserInfo: () => user,
		isBusiness: () => true
	}))

	assert.equal(redirect, null)
})

test('resolveRouteRedirect keeps admin away from admin login page', () => {
	const redirect = resolveRouteRedirect({
		path: '/admin/login',
		fullPath: '/admin/login',
		meta: { isPublic: true }
	}, createAuthState({
		getUserInfo: () => ({ userId: 'admin' }),
		isAdmin: () => true
	}))

	assert.equal(redirect, '/admin/users')
})
