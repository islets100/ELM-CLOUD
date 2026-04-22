function isObject(value) {
	return value !== null && typeof value === 'object'
}

export function normalizeOrderForDisplay(order) {
	const safeOrder = isObject(order) ? order : {}

	return {
		...safeOrder,
		business: {},
		orderDetails: Array.isArray(safeOrder.orderDetails) ? safeOrder.orderDetails : []
	}
}

export function collectFoodIds(orderDetails) {
	if (!Array.isArray(orderDetails)) {
		return []
	}

	return [...new Set(orderDetails.map(item => item?.foodId).filter(Boolean))]
}

export function buildFoodMap(foodResponses) {
	if (!Array.isArray(foodResponses)) {
		return {}
	}

	return foodResponses.reduce((foodMap, response) => {
		const food = response?.data?.data
		if (response?.data?.success && food?.id) {
			foodMap[food.id] = food
		}

		return foodMap
	}, {})
}

export function attachFoodsToOrderDetails(orderDetails, foodMap) {
	const safeOrderDetails = Array.isArray(orderDetails) ? orderDetails : []
	const safeFoodMap = isObject(foodMap) ? foodMap : {}

	return safeOrderDetails.map(item => ({
		...item,
		food: safeFoodMap[item?.foodId] || null
	}))
}
