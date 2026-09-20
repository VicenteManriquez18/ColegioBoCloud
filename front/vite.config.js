import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],

  server: {
    proxy: {
      // Todas las peticiones a /api-gw/* se redirigen al API Gateway de AWS
      // El browser ve localhost → sin CORS. Vite hace el reenvío server-side.
      '/api-gw': {
        target: 'https://8brl0s03y3.execute-api.us-east-1.amazonaws.com/prod',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api-gw/, ''),
        secure: true,
      },
    },
  },
});
