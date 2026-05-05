<template>
	<div v-if="state.visible" class="fallback-retry-modal">
		<div class="fallback-retry-overlay" @click="dismiss"></div>
		<div class="fallback-retry-card">
			<p class="fallback-retry-title">服务器繁忙</p>
			<p class="fallback-retry-message">{{ displayMessage }}</p>
			<div class="fallback-retry-actions">
				<button class="retry-btn" :disabled="state.retrying" @click="retry">
					{{ state.retrying ? '重试中...' : '点击重试' }}
				</button>
				<button class="dismiss-btn" :disabled="state.retrying" @click="dismiss">关闭</button>
			</div>
		</div>
	</div>
</template>

<script>
import { computed } from 'vue'
import { dismissFallbackPrompt, fallbackRetryState, retryLastFailedRequest } from '../utils/fallbackRetry'

export default {
	name: 'GlobalFallbackRetryPrompt',
	setup() {
		const displayMessage = computed(() => '服务器繁忙，请稍后重试。')

		const retry = async () => {
			await retryLastFailedRequest()
		}

		const dismiss = () => {
			dismissFallbackPrompt()
		}

		return {
			state: fallbackRetryState,
			displayMessage,
			retry,
			dismiss
		}
	}
}
</script>

<style scoped>
.fallback-retry-modal {
	position: fixed;
	inset: 0;
	z-index: 3000;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 4vw;
}

.fallback-retry-overlay {
	position: absolute;
	inset: 0;
	background: rgba(15, 23, 42, 0.4);
	backdrop-filter: blur(2px);
}

.fallback-retry-card {
	position: relative;
	z-index: 3001;
	width: min(88vw, 360px);
	padding: 24px 20px;
	border-radius: 20px;
	background: #fff;
	box-shadow: 0 16px 48px rgba(15, 23, 42, 0.18);
	animation: fallback-retry-slide-in 0.2s ease-out;
}

.fallback-retry-title {
	margin: 0 0 10px;
	font-size: 18px;
	font-weight: 700;
	color: #0f172a;
}

.fallback-retry-message {
	margin: 0;
	font-size: 14px;
	line-height: 1.6;
	color: #475569;
}

.fallback-retry-actions {
	display: flex;
	gap: 12px;
	margin-top: 18px;
}

.retry-btn,
.dismiss-btn {
	flex: 1;
	height: 40px;
	border: none;
	border-radius: 999px;
	font-size: 14px;
	font-weight: 600;
	cursor: pointer;
}

.retry-btn {
	background: #0097ff;
	color: #fff;
}

.dismiss-btn {
	background: #e2e8f0;
	color: #334155;
}

.retry-btn:disabled,
.dismiss-btn:disabled {
	opacity: 0.6;
	cursor: not-allowed;
}

@keyframes fallback-retry-slide-in {
	from {
		opacity: 0;
		transform: translateY(-12px) scale(0.96);
	}
	to {
		opacity: 1;
		transform: translateY(0) scale(1);
	}
}
</style>
