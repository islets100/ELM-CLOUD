export function stubMethod(target, methodName, implementation) {
	const original = target[methodName]
	target[methodName] = implementation

	return () => {
		target[methodName] = original
	}
}

export function createResolvedCallRecorder(result = { data: { success: true } }) {
	const calls = []

	const recorder = (...args) => {
		calls.push(args)
		return Promise.resolve(result)
	}

	recorder.calls = calls
	return recorder
}
