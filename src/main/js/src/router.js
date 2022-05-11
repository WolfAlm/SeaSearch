import Vue from 'vue'
import Router from 'vue-router'
import App from "./App.vue"

Vue.use(Router)

export default new Router({
    routes: [
        {
            path: '/',
            name: 'login',
            component: App,
            meta: { title: 'SeaSearch: Login' } // <- I would to use this one
        },
    ]
})