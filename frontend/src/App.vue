<template>
  <div id="app">
    <!-- <Login/> -->
    <router-view></router-view>
  </div>
</template>

<script>
// import Login from './components/Login.vue'

export default {
  name: 'App',
  components: {
    // Login
  }
}
</script>

<style>
body {
margin: 0;
}

#app {
  padding: 0;
  margin: 0;
  min-height: 100vh;
  overflow-y: hidden;
}
</style>
