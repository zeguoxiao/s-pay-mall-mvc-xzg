import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/token'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/cart', name: 'Cart', component: () => import('../views/Cart.vue'), meta: { requireAuth: true } },
  { path: '/orders', name: 'Orders', component: () => import('../views/Orders.vue'), meta: { requireAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫：需要登录的页面，没 token 就跳登录页
router.beforeEach((to, from, next) => {
  if (to.meta.requireAuth && !getToken()) {
    next('/login')
  } else {
    next()
  }
})

export default router
