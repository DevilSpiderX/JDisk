import { createApp } from 'vue';
import App from './App.vue';
import "./assets/fontawesome/css/all.css";
import router from './router/router';
import pinia from './store/pinia';
import "./style.css";
import "./styles/box-shadow.css";

const app = createApp(App);
app.use(router)
    .use(pinia)
    .mount('#app');
