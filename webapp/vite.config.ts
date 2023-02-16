import { vitePluginForArco } from '@arco-plugins/vite-vue';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import path from 'path';
import AutoImport from 'unplugin-auto-import/vite';
import { ArcoResolver } from 'unplugin-vue-components/resolvers';
import Components from 'unplugin-vue-components/vite';
import { defineConfig } from 'vite';
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig({
    base: "./",
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "src")
        }
    },
    plugins: [
        vue(),
        vueJsx(),
        AutoImport({
            resolvers: [ArcoResolver()],
        }),
        Components({
            resolvers: [
                ArcoResolver({
                    sideEffect: true
                })
            ],
        }),
        VitePWA({
            manifest: {
                "name": "JDisk",
                "short_name": "JDisk",
                "start_url": "./",
                "display": "standalone",
                "background_color": "#ffffff",
                "theme_color": "#ffffff",
                "lang": "en",
                "scope": "./",
                icons: [
                    {
                        "src": "favicon.png",
                        "sizes": "1408x1408",
                        "type": "image/png"
                    }
                ]
            },
            workbox: {
                cacheId: "JDisk-cache",
                navigateFallback: null,
                globPatterns: ["**\/*.{js,css,html,ttf,png,jpg}"],
                maximumFileSizeToCacheInBytes: 8000000
            }
        }),
        vitePluginForArco({
            style: "css"
        })
    ],
    server: {
        host: "0.0.0.0",
        proxy: {
            "/api/": { target: `http://localhost:8080` },
            "/s/": { target: `http://localhost:8080` },
            "/direct/link/": { target: `http://localhost:8080` },
            "/dl/": { target: `http://localhost:8080` },
        }
    }
})
