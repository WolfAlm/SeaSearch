import {createWebHistory, createRouter} from "vue-router";
import Profile from "./components/Profile.vue";
import Login from "./components/Login.vue";

const routes = [
    {
        path: '/login',
        name: 'login',
        component: Login,
        meta: {title: 'SeaSearch: Login'}
    },
    {
        path: '/profile',
        name: 'profile',
        component: Profile,
        meta: {title: 'SeaSearch: Profile'}
    },
]

const router = createRouter({
    history: createWebHistory(),
    mode: 'history',
    routes,
});

router.beforeEach((to, from, next) => {
    const publicPages = ['/login'];
    const authRequired = !publicPages.includes(to.path);
    const loggedIn = localStorage.getItem('user');
    if (authRequired && !loggedIn) {
        next('/login');
    } else {
        next();
    }
});

export default router;