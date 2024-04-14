import axios from 'axios'

const USUARIO_API_BASE_URL = 'http://localhost:8080/ociosingluten/usuarios'

class UsuarioServicio{
    getUsuarios(){
        return axios.get(USUARIO_API_BASE_URL);
    }
}

export default new UsuarioServicio()