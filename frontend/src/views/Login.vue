<template>
  <div class="login-page">
    <div class="login-box">
      <h2>登录</h2>
      <form @submit.prevent="handleLogin">
        <input v-model="form.account" placeholder="用户名" required />
        <input v-model="form.password" type="password" placeholder="密码" required />
        <button type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p class="error" v-if="error">{{ error }}</p>
      <p class="link">没有账号？<router-link to="/register">去注册</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { setToken } from '../utils/token'

const router = useRouter()
const form = ref({ account: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    const res = await login(form.value)
    setToken(res.data.token)
    router.push('/')
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { display: flex; justify-content: center; align-items: center; min-height: 80vh; }
.login-box { background: #fff; padding: 40px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); width: 360px; }
h2 { text-align: center; margin-bottom: 24px; color: #333; }
input { width: 100%; padding: 12px; margin-bottom: 16px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
button { width: 100%; padding: 12px; background: #ff6700; color: #fff; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; }
button:disabled { background: #ccc; }
.error { color: red; text-align: center; margin-top: 12px; }
.link { text-align: center; margin-top: 16px; font-size: 14px; }
.link a { color: #ff6700; text-decoration: none; }
</style>
