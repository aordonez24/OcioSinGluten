import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/api'

// Notificaciones de la persona con sesión. Se consultan al entrar y cada minuto mientras la
// pestaña está visible: es suficiente para avisos que no son urgentes y no necesita otro WebSocket.
export const useNotificaciones = defineStore('notificaciones', () => {
  const lista = ref([])
  const noLeidas = ref(0)
  let temporizador = null

  async function cargar() {
    try {
      const datos = await api('/notificaciones')
      lista.value = datos.notificaciones
      noLeidas.value = datos.no_leidas
    } catch {
      // Sin conexión o sesión caducada: se reintenta en la siguiente vuelta.
    }
  }

  async function marcarLeidas() {
    if (!noLeidas.value) return
    noLeidas.value = 0
    await api('/notificaciones/leidas', { method: 'POST' }).catch(() => {})
  }

  function empezar() {
    parar()
    cargar()
    temporizador = setInterval(() => document.visibilityState === 'visible' && cargar(), 60_000)
  }

  function parar() {
    clearInterval(temporizador)
    lista.value = []
    noLeidas.value = 0
  }

  return { lista, noLeidas, cargar, marcarLeidas, empezar, parar }
})
