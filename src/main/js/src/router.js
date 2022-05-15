import {createWebHistory, createRouter} from "vue-router";
import ProfileList from "./components/ProfileList.vue";
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
        path: '/profiles',
        name: 'profiles',
        component: ProfileList,
        meta: {title: 'SeaSearch: Profiles'}
    },
    {
        path: '/profile/:id',
        name: 'Stats',
        component: Profile,
        meta: {title: 'SeaSearch'},
        props: true
    }
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
        console.log('hi')
    } else {
        next();
    }
});

export default router;