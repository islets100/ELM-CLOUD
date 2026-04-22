export const USERNAME_PATTERN = /^[a-zA-Z0-9_-]+$/

export function validateRegisterUsername(value) {
	const username = typeof value === 'string' ? value.trim() : ''

	if (!username) {
		return {
			valid: false,
			message: '用户名不能为空',
			value: ''
		}
	}

	if (!USERNAME_PATTERN.test(username)) {
		return {
			valid: false,
			message: '用户名仅支持字母、数字、下划线和短横线',
			value: username
		}
	}

	return {
		valid: true,
		message: '',
		value: username
	}
}
