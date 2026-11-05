<script setup>
import { computed, onMounted, ref } from 'vue'
import { MessagesSquare, Search, Users } from 'lucide-vue-next'
import { api } from '@/api'
import { useAvisos } from '@/stores/avisos'
import { coincide } from '@/utils'
import EstadoVacio from '@/components/EstadoVacio.vue'
import ListaUsuarios from '@/components/ListaUsuarios.vue'

const avisos = useAvisos()
const usuarios = ref(null)
const busqueda = ref('')

const filtrados = computed(() =>
  (usuarios.value ?? []).filter((u) => coincide(busqueda.value, u.username, u.nombre)),
)

async function alternarSeguir(usuario) {
  const siguiendo = usuario.siguiendo
  usuario.siguiendo = !siguiendo
  usuario.num_seguidores += siguiendo ? -1 : 1
  try {
    await api(`/usuarios/${usuario.username}/seguir`, { method: siguiendo ? 'DELETE' : 'PUT' })
  } catch (e) {
    usuario.siguiendo = siguiendo
    usuario.num_seguidores += siguiendo ? 1 : -1
    avisos.error(e)
  }
}

onMounted(async () => {
  usuarios.value = await api('/usuarios').catch(() => [])
})
</script>

<template>
  <div class="contenedor">
    <div class="cabecera-pagina">
      <div>
        <h1>Comunidad</h1>
        <p>Sigue a otras personas celíacas para no perderte sus recomendaciones.</p>
      </div>
      <RouterLink to="/chat" class="btn btn-oscuro"><MessagesSquare :size="18" /> Chat de la comunidad</RouterLink>
    </div>

    <label class="buscador" style="margin-bottom: 1.25rem; display: block">
      <span class="solo-lectores">Buscar personas</span>
      <Search :size="18" />
      <input v-model="busqueda" type="search" placeholder="Buscar por nombre o usuario…" />
    </label>

    <div v-if="usuarios === null" class="rejilla">
      <div v-for="n in 6" :key="n" class="esqueleto" style="height: 82px"></div>
    </div>
    <ListaUsuarios v-else-if="filtrados.length" :usuarios="filtrados" seguir @alternar-seguir="alternarSeguir" />
    <EstadoVacio v-else :icono="Users" titulo="No se ha encontrado a nadie" texto="Prueba con otro nombre." />
  </div>
</template>
