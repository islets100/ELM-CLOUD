export function resolveUserId(user) {
	if (!user || typeof user !== 'object') {
		return null
	}

	return user.id || user.userId || null
}

function looksLikeUserEntity(user) {
	if (!user || typeof user !== 'object') {
		return false
	}

	return user.userId != null ||
		user.username != null ||
		user.userName != null ||
		user.authorities != null ||
		user.userSex != null ||
		user.userImg != null
}

function resolveUserName(user) {
	if (!user || typeof user !== 'object') {
		return null
	}

	const candidates = [user.username, user.userName]
	for (const candidate of candidates) {
		if (typeof candidate === 'string' && candidate.trim()) {
			return candidate.trim()
		}
	}

	return null
}

function normalizeUserAliases(user) {
	if (!looksLikeUserEntity(user)) {
		return user
	}

	const normalized = {
		...user
	}
	const normalizedId = resolveUserId(normalized)
	const normalizedName = resolveUserName(normalized)

	if (normalizedId != null) {
		normalized.id = normalizedId
		normalized.userId = normalized.userId || normalizedId
	}

	if (normalizedName != null) {
		normalized.username = normalizedName
		normalized.userName = normalizedName
	}

	return normalized
}

export function normalizeEntityShape(value) {
	if (Array.isArray(value)) {
		return value.map(normalizeEntityShape)
	}

	if (!value || typeof value !== 'object') {
		return value
	}

	const normalized = {}
	Object.keys(value).forEach(key => {
		normalized[key] = normalizeEntityShape(value[key])
	})

	if (normalized.startPrice == null && normalized.starPrice != null) {
		normalized.startPrice = normalized.starPrice
	}

	if (normalized.orderDetails == null && normalized.list != null) {
		normalized.orderDetails = normalized.list
	}

	if (normalized.id == null) {
		const entityIdKeys = ['orderId', 'businessId', 'foodId', 'daId', 'cartId', 'walletId']
		for (const key of entityIdKeys) {
			if (normalized[key] != null) {
				normalized.id = normalized[key]
				break
			}
		}

		if (normalized.id == null && normalized.userId != null && normalized.authorities) {
			normalized.id = normalized.userId
		}
	}

	return normalizeUserAliases(normalized)
}

export function normalizeApiData(value) {
	if (Array.isArray(value)) {
		return value.map(normalizeApiData)
	}

	if (!value || typeof value !== 'object') {
		return value
	}

	const normalized = {}
	Object.keys(value).forEach(key => {
		normalized[key] = normalizeApiData(value[key])
	})

	const looksLikeWallet =
		normalized.walletId != null ||
		normalized.balance != null ||
		normalized.creditLimit != null ||
		normalized.usedCreditLimit != null

	if (looksLikeWallet) {
		if (normalized.availableBalance == null && normalized.balance != null) {
			normalized.availableBalance = normalized.balance
		}

		if (normalized.frozenAmount == null) {
			normalized.frozenAmount = 0
		}

		if (normalized.overdraftAmount == null && normalized.usedCreditLimit != null) {
			normalized.overdraftAmount = normalized.usedCreditLimit
		}

		if (normalized.availableCreditLimit == null && normalized.creditLimit != null) {
			const creditLimit = Number(normalized.creditLimit) || 0
			const usedCreditLimit = Number(normalized.usedCreditLimit) || 0
			normalized.availableCreditLimit = Math.max(creditLimit - usedCreditLimit, 0)
		}

		if (normalized.hasVipPrivilege == null && normalized.creditLimit != null) {
			normalized.hasVipPrivilege = (Number(normalized.creditLimit) || 0) > 0
		}
	}

	return normalizeEntityShape(normalized)
}

export function normalizeUserInfoShape(user) {
	if (!user || typeof user !== 'object') {
		return user
	}

	return normalizeUserAliases(user)
}

export function normalizeCommonResult(payload) {
	if (!payload || typeof payload !== 'object') {
		return {
			success: false,
			data: null,
			message: ''
		}
	}

	const hasSuccessShape = Object.prototype.hasOwnProperty.call(payload, 'success') ||
		Object.prototype.hasOwnProperty.call(payload, 'data')

	if (hasSuccessShape) {
		const normalizedData = normalizeApiData(
			Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : (payload.result ?? null)
		)

		return {
			...payload,
			success: typeof payload.success === 'boolean' ? payload.success : false,
			data: normalizedData,
			message: payload.message || ''
		}
	}

	const code = Number(payload.code)
	return {
		...payload,
		success: Number.isFinite(code) ? code >= 200 && code < 300 : false,
		data: normalizeApiData(payload.result ?? null),
		message: payload.message || ''
	}
}

export async function normalizeAxiosResult(requestPromise) {
	const response = await requestPromise

	return {
		...response,
		data: normalizeCommonResult(response?.data)
	}
}
