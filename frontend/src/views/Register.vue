<template>
  <div class="login-page">
    <div class="login-box">
      <h2>注册</h2>
      <form @submit.prevent="handleRegister">
        <input v-model="form.userName" placeholder="用户名" required />
        <input v-model="form.password" type="password" placeholder="密码" required />
        <input v-model="form.nickName" placeholder="昵称" />
        <button type="submit" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      <p class="error" v-if="error">{{ error }}</p>
      <p class="link">已有账号？<router-link to="/login">去登录</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'

const router = useRouter()
const form = ref({ userName: '', password: '', nickName: '', email: '', phone: '' })
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  loading.value = true
  error.value = ''
  try {
    await register(form.value)
    router.push('/login')
  } catch (e) {
    error.value = e.message || '注册失败'
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
