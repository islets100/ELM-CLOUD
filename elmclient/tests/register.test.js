import test from 'node:test'
import assert from 'node:assert/strict'

import { validateRegisterUsername } from '../src/utils/register.js'

test('validateRegisterUsername rejects blank usernames', () => {
	assert.deepEqual(validateRegisterUsername('   '), {
		valid: false,
		message: '用户名不能为空',
		value: ''
	})
})

test('validateRegisterUsername rejects unsupported characters', () => {
	assert.deepEqual(validateRegisterUsername('bad name!'), {
		valid: false,
		message: '用户名仅支持字母、数字、下划线和短横线',
		value: 'bad name!'
	})
})

test('validateRegisterUsername accepts trimmed legal usernames', () => {
	assert.deepEqual(validateRegisterUsername('  demo_user-01  '), {
		valid: true,
		message: '',
		value: 'demo_user-01'
	})
})
