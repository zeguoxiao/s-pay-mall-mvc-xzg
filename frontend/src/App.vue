<template>
  <div class="app">
    <nav class="navbar">
      <div class="nav-left">
        <router-link to="/" class="logo">🛒 商城</router-link>
      </div>
      <div class="nav-right">
        <template v-if="isLogin">
          <router-link to="/cart" class="nav-link">购物车</router-link>
          <router-link to="/orders" class="nav-link">我的订单</router-link>
          <span class="nav-link" @click="handleLogout">退出</span>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link">注册</router-link>
        </template>
      </div>
    </nav>
    <router-view />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getToken, removeToken } from './utils/token'
import { logout } from './api/auth'

const router = useRouter()
const isLogin = computed(() => !!getToken())

async function handleLogout() {
  try { await logout() } catch (e) {}
  removeToken()
  router.push('/login')
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, sans-serif; background: #f5f5f5; }
.navbar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 20px; height: 60px; background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.logo { font-size: 20px; font-weight: bold; text-decoration: none; color: #333; }
.nav-right { display: flex; gap: 20px; }
.nav-link {
  text-decoration: none; color: #666; cursor: pointer; font-size: 14px;
}
.nav-link:hover { color: #ff6700; }
.router-link-active { color: #ff6700; font-weight: bold; }
</style>
