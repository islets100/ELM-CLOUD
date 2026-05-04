import axios from 'axios'
import { reactive } from 'vue'

export function isFallbackResponse(payload) {
	return !!(
		payload &&
		typeof payload === 'object' &&
		payload.success === false &&
		payload.data &&
		typeof payload.data === 'object' &&
		payload.data.fallback === true
	)
}

function stableSerialize(value) {
	if (value == null) {
		return ''
	}

	if (typeof value === 'string') {
		return value
	}

	try {
		return JSON.stringify(value, Object.keys(value).sort())
	} catch (error) {
		return ''
	}
}

function cloneReplayConfig(config = {}) {
	const headers = config.headers ? { ...config.headers } : {}

	return {
		url: config.url,
		method: config.method,
		params: config.params,
		data: config.data,
		headers,
		baseURL: config.baseURL,
		timeout: config.timeout,
		__skipFallbackPrompt: true,
		__isFallbackRetry: true
	}
}

function buildFingerprint(payload = {}, config = {}) {
	const method = String(config.method || payload.method || 'get').toUpperCase()
	const url = config.url || payload.path || ''
	const service = payload.service || 'unknown'
	const params = stableSerialize(config.params)
	const data = stableSerialize(config.data)
	return [service, method, url, params, data].join('::')
}

export const fallbackRetryState = reactive({
	visible: false,
	message: '',
	service: '',
	retrying: false,
	fingerprint: '',
	requestConfig: null,
	lastShownAt: 0
})

export function showFallbackPrompt(payload = {}, requestConfig = {}) {
	if (!payload.retryable) {
		return
	}

	const fingerprint = buildFingerprint(payload, requestConfig)
	const now = Date.now()
	const samePrompt = fallbackRetryState.visible && fallbackRetryState.fingerprint === fingerprint

	fallbackRetryState.visible = true
	fallbackRetryState.message = payload.message || '服务器繁忙，请稍后重试。'
	fallbackRetryState.service = payload.service || ''
	fallbackRetryState.requestConfig = cloneReplayConfig(requestConfig)
	fallbackRetryState.fingerprint = fingerprint
	fallbackRetryState.lastShownAt = now

	if (!samePrompt) {
		fallbackRetryState.retrying = false
	}
}

export function shouldSuppressNativeAlert() {
	return fallbackRetryState.visible && Date.now() - fallbackRetryState.lastShownAt < 1500
}

export function dismissFallbackPrompt() {
	fallbackRetryState.visible = false
	fallbackRetryState.retrying = false
	fallbackRetryState.message = ''
	fallbackRetryState.service = ''
	fallbackRetryState.fingerprint = ''
	fallbackRetryState.requestConfig = null
}

export async function retryLastFailedRequest() {
	if (fallbackRetryState.retrying || !fallbackRetryState.requestConfig) {
		return false
	}

	fallbackRetryState.retrying = true
	try {
		const response = await axios(fallbackRetryState.requestConfig)
		if (response?.data?.success === false && response?.data?.data?.fallback === true) {
			return false
		}
		dismissFallbackPrompt()
		return true
	} catch (error) {
		return false
	} finally {
		fallbackRetryState.retrying = false
	}
}
