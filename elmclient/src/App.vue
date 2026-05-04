<template>
	<div id="app">
		<router-view />
		<GlobalFallbackRetryPrompt />
	</div>
</template>

<script>
import GlobalFallbackRetryPrompt from './components/GlobalFallbackRetryPrompt.vue'

export default {
	name: 'App',
	components: {
		GlobalFallbackRetryPrompt
	}
}
</script>

<!--这里是共通样式，适用于所有组件，所以不要加scoped-->
<style>
	html,
	body,
	div,
	span,
	h1,
	h2,
	h3,
	h4,
	h5,
	h6,
	ul,
	ol,
	li,
	p {
		margin: 0;
		padding: 0;
	}

	html,
	body,
	#app {
		width: 100%;
		height: 100%;
		font-family: "微软雅黑";
	}

	ul,
	ol {
		list-style: none;
	}

	a {
		text-decoration: none;
	}
	
/* 原有样式不改动，追加以下内容 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  background: #f5f7fa;
  color: #333;
}
button, input, select {
  font-family: inherit;
  outline: none;
}	
</style>