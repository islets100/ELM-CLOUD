import test from 'node:test'
import assert from 'node:assert/strict'

import {
	attachFoodsToOrderDetails,
	buildFoodMap,
	collectFoodIds,
	normalizeOrderForDisplay
} from '../src/utils/paymentOrder.js'

test('normalizeOrderForDisplay resets business info and keeps detail array', () => {
	assert.deepEqual(normalizeOrderForDisplay({
		id: 12,
		business: {
			id: 9,
			businessName: 'Old Shop'
		},
		orderDetails: [{ foodId: 1, quantity: 2 }]
	}), {
		id: 12,
		business: {},
		orderDetails: [{ foodId: 1, quantity: 2 }]
	})

	assert.deepEqual(normalizeOrderForDisplay(null), {
		business: {},
		orderDetails: []
	})
})

test('collectFoodIds deduplicates valid ids and skips empty values', () => {
	assert.deepEqual(collectFoodIds([
		{ foodId: 3 },
		{ foodId: 3 },
		{ foodId: 8 },
		{ foodId: 0 },
		{}
	]), [3, 8])
})

test('buildFoodMap only keeps successful food payloads', () => {
	assert.deepEqual(buildFoodMap([
		{ data: { success: true, data: { id: 3, foodName: 'Rice' } } },
		{ data: { success: false, data: { id: 8, foodName: 'Soup' } } },
		{ data: { success: true, data: null } }
	]), {
		3: { id: 3, foodName: 'Rice' }
	})
})

test('attachFoodsToOrderDetails attaches food objects and falls back to null', () => {
	assert.deepEqual(attachFoodsToOrderDetails([
		{ id: 1, foodId: 3, quantity: 2 },
		{ id: 2, foodId: 8, quantity: 1 }
	], {
		3: { id: 3, foodName: 'Rice', foodPrice: 12 }
	}), [
		{
			id: 1,
			foodId: 3,
			quantity: 2,
			food: { id: 3, foodName: 'Rice', foodPrice: 12 }
		},
		{
			id: 2,
			foodId: 8,
			quantity: 1,
			food: null
		}
	])
})
