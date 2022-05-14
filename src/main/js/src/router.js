import {createWebHistory, createRouter} from "vue-router";
import App from "./App.vue"

const routes = [
    {
        path: '/',
        name: 'login',
        component: App,
        meta: {title: 'SeaSearch: Login'}
    },
    {
        path: '/profile',
        name: 'profile',
        component: App,
        meta: {title: 'SeaSearch: Profile'}
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;