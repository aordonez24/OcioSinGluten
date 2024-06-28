import { createStore } from 'vuex';

const store = createStore({
    state: {
        token: localStorage.getItem('token') || null,
        username: localStorage.getItem('username') || null
    },
    mutations: {
        setToken(state, token) {
            state.token = token;
            localStorage.setItem('token', token);
        },
        setUsername(state, username) {
            state.username = username;
            localStorage.setItem('username', username);
        },
        clearAuthData(state) {
            state.token = null;
            state.username = null;
            localStorage.removeItem('token');
            localStorage.removeItem('username');
        }
    },
    actions: {
        login({ commit }, authData) {
            commit('setToken', authData.token);
            commit('setUsername', authData.username);
        },
        logout({ commit }) {
            commit('clearAuthData');
        }
    },
    getters: {
        isAuthenticated(state) {
            return state.token !== null;
        },
        username(state) {
            return state.username;
        }
    }
});

export default store;
