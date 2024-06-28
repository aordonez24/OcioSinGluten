<template>
  <div>
    <header3/>
    <div class="container1">
      <div class="container mt-5 mb-5">
      <div class="row">
        <!-- Formulario para enviar mensajes -->
        <div class="col-md-4">
          <div class="mb-3">
            <h4>Envie su mensaje</h4>
            <input v-model="nombre" type="text" class="form-control mb-2" placeholder="Nombre" readonly>
            <input v-model="mensaje" type="text" class="form-control mb-2" placeholder="Mensaje" maxlength="100">
            <button @click="enviarMensaje" class="botoncin">Enviar</button>
          </div>
        </div>

        <!-- Chat -->
        <div class="col-md-8">
          <h4>Chat</h4>
          <div id="mensajes" class="container chat-container">
            <ul id="ULMensajes" class="list-group">
              <li v-for="(mensaje, index) in mensajes" :key="index" :class="{'mensaje-enviado': mensaje.enviado, 'mensaje-recibido': !mensaje.enviado}" class="list-group-item mensaje">
                <div class="mensaje-contenido">
                  <span class="mensaje-nombre">{{ mensaje.nombre }}</span>: {{ mensaje.contenido }}
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    </div>
    <footer-componente class="mt-auto"/>
  </div>
</template>

<script>
import { Client } from '@stomp/stompjs';
import Header3 from "@/components/headerIniciadoSesion.vue";
import FooterComponente from "@/components/footer.vue";
import {mapGetters} from "vuex";

export default {
  components: {FooterComponente, Header3},
  data() {
    return {
      stompCliente: null,
      nombre: '',
      mensaje: '',
      mensajes: []
    };
  },
  mounted() {
    this.conectarWS();
    this.nombre = this.username;

  },
  computed: {
    ...mapGetters(['username', 'isAuthenticated'])
  },
  methods: {
    async conectarWS() {
      try {
        this.onWebSocketClose();
        this.stompCliente = new Client({
          webSocketFactory: () => new WebSocket('ws://localhost:8080/websocket')
        });
        this.stompCliente.onConnect = () => {
          this.onConnectSocket();
        };
        this.stompCliente.onWebSocketClose = this.onWebSocketClose;

        await this.stompCliente.activate(); // Intenta activar la conexión WebSocket
      } catch (error) {
        console.error('Error al conectar WebSocket:', error);
      }
    },
    onConnectSocket() {
      this.stompCliente.subscribe('/tema/mensajes', (mensaje) => {
        this.mostrarMensaje(mensaje.body);
      });
    },
    onWebSocketClose() {
      if (this.stompCliente !== null) {
        this.stompCliente.deactivate();
      }
    },
    enviarMensaje() {
      if (this.stompCliente !== null) {
        this.stompCliente.publish({
          destination: '/app/envio',
          body: JSON.stringify({
            nombre: this.nombre,
            contenido: this.mensaje
          })
        });
      }
      this.mensaje = '';
    },
    mostrarMensaje(mensaje) {
      const body = JSON.parse(mensaje);
      this.mensajes.push(body);
    }
  }
};
</script>



<style scoped>
.container {
  min-height: calc(80vh - 200px);
  width: 90%;
  margin: 50px auto 0;
  padding: 30px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
}

.mb-3 {
  max-width: 300px;
}

.mb-2 {
  margin-bottom: 10px;
}

.chat-container {
  margin-left: -15px;
  margin-right: -15px;
}

.mensaje {

  border: none;
  padding: 0;
  margin-bottom: 10px;
}

.mensaje-contenido {
  border-radius: 10px;
  padding: 10px;
}

.mensaje-enviado {
  background-color: #ffa500;
}

.mensaje-recibido {
  background-color: #fcf8e3;
}

.mensaje-nombre {
  font-weight: bold;
}

footer-componente {
  margin-top: 20px;
}


.botoncin {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  color: white;
  background-color: #9DD9D2;
  border: none;
  cursor: pointer;
}

.botoncin:hover {
  background-color: #ffcc74;
}

.container1 {
  background-image: url("@/assets/images/_01d90abf-9b74-4813-b728-42c7b8f918a7.jpg");
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-repeat: no-repeat;
  background-size: cover;
  padding: 20px;
}
</style>