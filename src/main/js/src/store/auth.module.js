import AuthService from '../services/auth.service';

const user = JSON.parse(localStorage.getItem('user'));
const initialState = user
    ? {status: {loggedIn: true}, user}
    : {status: {loggedIn: false}, user: null};

export const auth = {
    namespaced: true,
    state: initialState,
    actions: {
        loginPhone({commit}, user) {
            return AuthService.loginPhone(user).then(
                user => {
                    return Promise.resolve(user);
                },
                error => {
                    commit('loginFailure');
                    return Promise.reject(error);
                }
            );
        },
        loginCode({commit}, user) {
            return AuthService.loginCode(user).then(
                user => {
                    if (AuthService.needPassword) {
                        return Promise.reject("Need password");
                    }
                    commit('loginSuccess', user);
                    return Promise.resolve(user);
                },
                error => {
                    commit('loginFailure');
                    return Promise.reject(error);
                }
            );
        },
        loginPassword({commit}, user) {
            return AuthService.loginPassword(user).then(
                user => {
                    commit('loginSuccess', user);
                    return Promise.resolve(user);
                },
                error => {
                    commit('loginFailure');
                    return Promise.reject(error);
                }
            );
        },
        logout({commit}) {
            AuthService.logout().then(
                user => {
                    commit('logout', user);
                    return Promise.resolve(user);
                },
                error => {
                    console.log("Logout failed")
                    return Promise.reject(error);
                }
            );
            commit('logout');
        },
    },
    mutations: {
        loginSuccess(state, user) {
            state.status.loggedIn = true;
            state.user = user;
        },
        loginFailure(state) {
            state.status.loggedIn = false;
            state.user = null;
        },
        logout(state) {
            state.status.loggedIn = false;
            state.user = null;
        },
    }
};
