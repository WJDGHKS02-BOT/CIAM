import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import tsconfigPaths from 'vite-tsconfig-paths';
import path from 'path';

export default defineConfig(({ mode }) => {
  const isDev = mode === 'development';

  return {
    plugins: [react(), tsconfigPaths()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
        '@components': path.resolve(__dirname, './src/components'),
        '@constants': path.resolve(__dirname, './src/constants'),
        '@pages': path.resolve(__dirname, './src/pages'),
        '@states': path.resolve(__dirname, './src/states'),
        '@styles': path.resolve(__dirname, './src/styles'),
        '@types': path.resolve(__dirname, './src/types'),
        '@utils': path.resolve(__dirname, './src/utils'),
      },
    },
    server: {
      watch: {
        usePolling: true,
      },
    },
    build: {
      emptyOutDir: true,
      sourcemap: true,
      rollupOptions: {
        input: {
          signInProxy: './src/pages/sign-in-proxy/index.tsx',
        },
        output: {
          dir: isDev ? '../src/main/resources/static/dist/dev' : '../src/main/resources/static/dist/prod',
          entryFileNames: '[name]-bundle.js',
        },
      },
    },
  };
});
