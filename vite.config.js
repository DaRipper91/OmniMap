import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            src: '/src',
        },
    },
});
//# sourceMappingURL=vite.config.js.map