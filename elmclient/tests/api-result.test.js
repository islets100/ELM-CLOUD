import test from 'node:test'
import assert from 'node:assert/strict'

import { normalizeCommonResult } from '../src/utils/apiResult.js'

test('normalizeCommonResult normalizes legacy entity fields', () => {
	const payload = {
		code: 200,
		message: 'ok',
		result: [
			{
				businessId: 5,
				businessName: '食满堂·牛肉饭',
				businessExplain: '现炒现做的家常盖饭',
				starPrice: 20
			},
			{
				foodId: 7,
				foodName: '红烧牛肉饭',
				foodExplain: '慢炖牛腩搭配时蔬',
				foodPrice: 22
			}
		]
	}

	const normalized = normalizeCommonResult(payload)

	assert.equal(normalized.success, true)
	assert.equal(normalized.message, 'ok')
	assert.equal(normalized.data[0].id, 5)
	assert.equal(normalized.data[0].startPrice, 20)
	assert.equal(normalized.data[0].businessName, '食满堂·牛肉饭')
	assert.equal(normalized.data[0].businessExplain, '现炒现做的家常盖饭')
	assert.equal(normalized.data[1].id, 7)
	assert.equal(normalized.data[1].foodName, '红烧牛肉饭')
	assert.equal(normalized.data[1].foodExplain, '慢炖牛腩搭配时蔬')
})

test('normalizeCommonResult normalizes current user aliases', () => {
	const payload = {
		code: 200,
		message: 'ok',
		result: {
			userId: 'u1001',
			userName: 'Alice',
			authorities: [
				{ name: 'USER' }
			]
		}
	}

	const normalized = normalizeCommonResult(payload)

	assert.equal(normalized.success, true)
	assert.deepEqual(normalized.data, {
		id: 'u1001',
		userId: 'u1001',
		username: 'Alice',
		userName: 'Alice',
		authorities: [
			{ name: 'USER' }
		]
	})
})
