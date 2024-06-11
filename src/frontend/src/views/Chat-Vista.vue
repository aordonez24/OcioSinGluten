<template>
  <div>
    <header3/>
    <div class="container mt-5 mb-5"> <!-- Agregamos un margen inferior -->
      <div class="row">
        <!-- Formulario para enviar mensajes -->
        <div class="col-md-4">
          <div class="mb-3">
            <h4>Enviar Mensaje</h4>
            <input v-model="nombre" type="text" class="form-control mb-2" placeholder="Nombre" readonly>
            <input v-model="mensaje" type="text" class="form-control mb-2" placeholder="Mensaje">
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
  min-height: calc(80vh - 200px); /* Ajusta este valor según la altura de tu encabezado y pie de página */
  width: 90%;
  margin: 50px auto 0; /* Eliminamos el margen inferior */
  padding: 30px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
}

/* Ajusta el tamaño del formulario para enviar mensajes */
.mb-3 {
  max-width: 300px;
}

/* Añade margen entre los elementos del formulario */
.mb-2 {
  margin-bottom: 10px;
}

/* Estilos para los mensajes */
.chat-container {
  margin-left: -15px;
  margin-right: -15px;
}

/* Estilos para los mensajes */
.mensaje {

  border: none;
  padding: 0;
  margin-bottom: 10px;
}

.mensaje-contenido {
  border-radius: 10px;
  padding: 10px;
}

/* Estilos para los mensajes enviados */
.mensaje-enviado {
  background-color: #ffa500; /* Verde claro */
}

/* Estilos para los mensajes recibidos */
.mensaje-recibido {
  background-color: #fcf8e3; /* Amarillo claro */
}

/* Estilos para el nombre del remitente */
.mensaje-nombre {
  font-weight: bold;
}

footer-componente {
  margin-top: 20px; /* Ajusta este valor según el espacio que desees */
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
</style>