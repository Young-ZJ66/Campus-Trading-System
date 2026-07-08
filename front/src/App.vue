<template>
  <router-view v-slot="{ Component }">
    <keep-alive include="Market">
      <component :is="Component" />
    </keep-alive>
  </router-view>

  <LoginDialog />
  <RegisterDialog />
</template>

<script setup>
import LoginDialog from './components/LoginDialog.vue'
import RegisterDialog from './components/RegisterDialog.vue'
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif;
}
</style>