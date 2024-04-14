<script>
  import UsuarioServicio from "../services/UsuarioServicio";

  export default {
    name: 'Usuarios-Items',
    data(){
      return {
        usuarios : []
      }
    },
    methods:{
      getUsuarios() {
        UsuarioServicio.getUsuarios().then((response) => {
          this.usuarios = response.data;
        });
      }
    },
    created(){
      this.getUsuarios()
    }
  }
</script>

<template>
  <div class="container">
    <h1 class="text-center"> Lista de usuarios registrados </h1>
    <table class="table table-striped">
      <thead>
        <th>DNI del usuario</th>
        <th>Nombre del usuario</th>
        <th>Apellido del usuario</th>
        <th>Email del usuario</th>
      </thead>
      <tbody>
        <tr v-for = "usuario in usuarios" v-bind:key = "usuario.dni">
            <td> {{usuario.dni}} </td>
            <td> {{usuario.nombre}} </td>
            <td> {{usuario.apellidos}} </td>
            <td> {{usuario.email}} </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
