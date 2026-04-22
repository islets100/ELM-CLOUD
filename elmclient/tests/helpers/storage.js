class MemoryStorage {
	constructor() {
		this.clear()
	}

	clear() {
		this.store = new Map()
	}

	getItem(key) {
		return this.store.has(key) ? this.store.get(key) : null
	}

	setItem(key, value) {
		this.store.set(String(key), String(value))
	}

	removeItem(key) {
		this.store.delete(String(key))
	}
}

export function installStorageMocks() {
	globalThis.localStorage = new MemoryStorage()
	globalThis.sessionStorage = new MemoryStorage()
}
