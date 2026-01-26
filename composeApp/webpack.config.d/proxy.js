// Ensure devServer exists
config.devServer = config.devServer || {};

// MUST be an array, not an object
config.devServer.proxy = [
    {
        context: ['/api'],
        target: 'https://static.yueban.site',
        secure: false,
        changeOrigin: true,
        // Optional log to see if proxy is working in browser console
        logLevel: 'debug'
    }
];
