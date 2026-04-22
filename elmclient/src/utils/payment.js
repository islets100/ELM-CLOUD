function toSafeNumber(value) {
	const parsedValue = Number(value)
	return Number.isFinite(parsedValue) ? parsedValue : 0
}

export function calculateMaxPointsCanUse(orderTotal, availablePoints) {
	const safeOrderTotal = Math.max(toSafeNumber(orderTotal), 0)
	const safeAvailablePoints = Math.max(Math.floor(toSafeNumber(availablePoints)), 0)
	const maxByOrder = Math.floor(safeOrderTotal * 100)

	return Math.min(safeAvailablePoints, maxByOrder)
}

export function normalizePointsToUse(pointsToUse, maxPointsCanUse) {
	const safePointsToUse = Math.max(Math.floor(toSafeNumber(pointsToUse)), 0)
	const safeMaxPointsCanUse = Math.max(Math.floor(toSafeNumber(maxPointsCanUse)), 0)

	return Math.min(safePointsToUse, safeMaxPointsCanUse)
}

export function calculateDeductionAmount(usePoints, pointsToUse) {
	if (!usePoints) {
		return 0
	}

	return normalizePointsToUse(pointsToUse, Number.MAX_SAFE_INTEGER) / 100
}

export function calculateFinalAmount(orderTotal, deductionAmount) {
	const finalAmount = Math.max(toSafeNumber(orderTotal), 0) - Math.max(toSafeNumber(deductionAmount), 0)

	return Math.max(finalAmount, 0)
}
