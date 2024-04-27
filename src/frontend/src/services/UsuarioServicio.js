import axios from 'axios';

const USUARIO_API_BASE_URL = 'http://localhost:8080/ociosingluten/usuarios';

class UsuarioServicio {
    getUsuarios() {
        return axios.get(USUARIO_API_BASE_URL);
    }

    agregarUsuario(datosUsuario) {
        return axios.post('http://localhost:8080/ociosingluten/usuarios/nuevoUsuario', datosUsuario, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    }

    loginWithParams(email, password) {
        const queryParams = {
            email: email,
            password: password
        };
        return axios.post(`${USUARIO_API_BASE_URL}/login`, null, { params: queryParams });
    }


}

export default new UsuarioServicio();
