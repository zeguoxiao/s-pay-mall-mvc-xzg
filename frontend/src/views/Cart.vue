<template>
  <div class="cart-page">
    <h2>购物车</h2>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="cartList.length === 0" class="empty">
      <p>购物车是空的 🛒</p>
      <router-link to="/" class="go-shop">去逛逛</router-link>
    </div>
    <template v-else>
      <div class="cart-list">
        <div v-for="item in cartList" :key="item.productId" class="cart-item">
          <div class="item-info">
            <h3>商品ID: {{ item.productId }}</h3>
            <p>数量: {{ item.quantity }}</p>
          </div>
          <div class="item-actions">
            <button @click="handleUpdate(item.productId, item.quantity - 1)" :disabled="item.quantity <= 1">-</button>
            <span>{{ item.quantity }}</span>
            <button @click="handleUpdate(item.productId, item.quantity + 1)">+</button>
            <button class="delete" @click="handleRemove(item.productId)">删除</button>
          </div>
        </div>
      </div>
      <div class="cart-footer">
        <button class="checkout-btn" @click="handleCheckout" :disabled="checking">
          {{ checking ? '结算中...' : '去结算' }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getCartList, updateCart, removeCart } from '../api/cart'
import { cartCheckout } from '../api/order'

const cartList = ref([])
const loading = ref(true)
const checking = ref(false)

onMounted(() => loadCart())

async function loadCart() {
  loading.value = true
  try {
    const res = await getCartList()
    const cart = res.data || {}
    cartList.value = Object.entries(cart).map(([productId, quantity]) => ({
      productId: Number(productId),
      quantity
    }))
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleUpdate(productId, quantity) {
  try {
    await updateCart(productId, quantity)
    await loadCart()
  } catch (e) {
    alert(e.message)
  }
}

async function handleRemove(productId) {
  if (!confirm('确定删除？')) return
  try {
    await removeCart(productId)
    await loadCart()
  } catch (e) {
    alert(e.message)
  }
}

async function handleCheckout() {
  const cartItems = {}
  cartList.value.forEach(item => {
    cartItems[item.productId] = item.quantity
  })
  checking.value = true
  try {
    await cartCheckout(cartItems)
    alert('下单成功！')
    await loadCart()
  } catch (e) {
    alert(e.message || '下单失败')
  } finally {
    checking.value = false
  }
}
</script>

<style scoped>
.cart-page { max-width: 800px; margin: 30px auto; padding: 0 20px; }
h2 { margin-bottom: 20px; color: #333; }
.loading, .empty { text-align: center; padding: 60px; color: #999; }
.go-shop { color: #ff6700; text-decoration: none; margin-top: 12px; display: inline-block; }
.cart-item {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 16px; border-radius: 8px;
  margin-bottom: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.item-info h3 { font-size: 16px; color: #333; }
.item-info p { color: #666; font-size: 14px; margin-top: 4px; }
.item-actions { display: flex; align-items: center; gap: 8px; }
.item-actions button {
  width: 32px; height: 32px; border: 1px solid #ddd; border-radius: 4px;
  background: #fff; cursor: pointer; font-size: 16px;
}
.item-actions button:disabled { color: #ccc; cursor: not-allowed; }
.item-actions .delete { width: auto; padding: 0 12px; color: #ff4d4f; border-color: #ff4d4f; }
.item-actions span { min-width: 30px; text-align: center; font-size: 16px; }
.cart-footer { text-align: right; margin-top: 20px; }
.checkout-btn {
  padding: 12px 40px; background: #ff6700; color: #fff;
  border: none; border-radius: 4px; font-size: 16px; cursor: pointer;
}
.checkout-btn:disabled { background: #ccc; }
</style>
