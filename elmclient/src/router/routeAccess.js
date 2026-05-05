export function getAuthorityNames(user) {
	if (!Array.isArray(user?.authorities)) {
		return []
	}

	return user.authorities.map(authority => authority?.name).filter(Boolean)
}

export function resolvePostLoginPath(user, redirectPath = '/index') {
	const authorities = getAuthorityNames(user)

	if (authorities.includes('ADMIN')) {
		return '/admin/users'
	}

	if (authorities.includes('BUSINESS')) {
		return '/business'
	}

	if (authorities.includes('USER')) {
		return redirectPath || '/index'
	}

	return '/index'
}

export function resolveRouteRedirect(to, authState) {
	const user = authState.getUserInfo?.()
	const meta = to?.meta || {}
	const redirectTarget = to?.fullPath || to?.path || '/index'

	if (meta.isPublic) {
		if (authState.isAdmin?.() && to?.path === '/admin/login') {
			return '/admin/users'
		}

		return null
	}

	if (meta.isAdmin && (!user || !authState.isAdmin?.())) {
		return {
			path: '/admin/login',
			query: {
				redirect: redirectTarget
			}
		}
	}

	if (meta.isBusiness && (!user || !authState.isBusiness?.())) {
		return {
			path: '/login',
			query: {
				redirect: redirectTarget
			}
		}
	}

	if (meta.requiresAuth && !user) {
		return {
			path: '/login',
			query: {
				redirect: redirectTarget
			}
		}
	}

	return null
}
