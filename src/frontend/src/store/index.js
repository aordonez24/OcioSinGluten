import { createStore } from 'vuex';

const store = createStore({
    state: {
        token: localStorage.getItem('token') || null, // Obtener el token del almacenamiento local
        username: localStorage.getItem('username') || null // Obtener el username del almacenamiento local
    },
    mutations: {
        setToken(state, token) {
            state.token = token;
            localStorage.setItem('token', token); // Guardar el token en el almacenamiento local
        },
        setUsername(state, username) {
            state.username = username;
            localStorage.setItem('username', username); // Guardar el username en el almacenamiento local
        },
        clearAuthData(state) {
            state.token = null;
            state.username = null;
            localStorage.removeItem('token'); // Eliminar el token del almacenamiento local al cerrar sesión
            localStorage.removeItem('username'); // Eliminar el username del almacenamiento local al cerrar sesión
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
