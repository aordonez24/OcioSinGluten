import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
        user: null // Estado inicial del usuario
    },
    mutations: {
        setUser(state, user) {
            state.user = user;
        }
    },
    actions: {
        login({ commit }, user) {
            // Lógica de inicio de sesión aquí...
            commit('setUser', user); // Establecer el usuario después del inicio de sesión
        },
        logout({ commit }) {
            // Lógica de cierre de sesión aquí...
            commit('setUser', null); // Limpiar el usuario al cerrar sesión
        }
    },
    modules: {}
});
