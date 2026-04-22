import test from 'node:test'
import assert from 'node:assert/strict'

import {
	calculateDeductionAmount,
	calculateFinalAmount,
	calculateMaxPointsCanUse,
	normalizePointsToUse
} from '../src/utils/payment.js'

test('calculateMaxPointsCanUse limits points by order total and available balance', () => {
	assert.equal(calculateMaxPointsCanUse(23.5, 5000), 2350)
	assert.equal(calculateMaxPointsCanUse(23.5, 800), 800)
})

test('normalizePointsToUse clamps values into supported range', () => {
	assert.equal(normalizePointsToUse(-10, 500), 0)
	assert.equal(normalizePointsToUse(888, 500), 500)
	assert.equal(normalizePointsToUse(123.9, 500), 123)
})

test('calculateDeductionAmount only deducts when points are enabled', () => {
	assert.equal(calculateDeductionAmount(false, 500), 0)
	assert.equal(calculateDeductionAmount(true, 500), 5)
})

test('calculateFinalAmount never returns a negative payable amount', () => {
	assert.equal(calculateFinalAmount(23.5, 3), 20.5)
	assert.equal(calculateFinalAmount(10, 20), 0)
})
