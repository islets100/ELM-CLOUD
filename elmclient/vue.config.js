module.exports = {
	devServer: {
		port: 8081
	},
	configureWebpack: {
		resolve: {
			fallback: {
				"net": false,
				"tls": false,
				"fs": false
			}
		}
	}
}