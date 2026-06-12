<template>
  <div class="home">
    <div class="banner">
      <h1>欢迎来到商城</h1>
      <p>精选好物，品质生活</p>
    </div>
    <div class="section">
      <h2>商品列表</h2>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="products.length === 0" class="empty">暂无商品</div>
      <div v-else class="product-grid">
        <div v-for="p in products" :key="p.id" class="product-card">
          <div class="product-img">📦</div>
          <div class="product-info">
            <h3>{{ p.productName }}</h3>
            <p class="price">¥{{ p.price }}</p>
            <p class="stock">库存: {{ p.stock }}</p>
            <button @click="handleAddCart(p)" :disabled="p.stock <= 0">
              {{ p.stock <= 0 ? '已售罄' : '加入购物车' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProductList } from '../api/product'
import { addCart } from '../api/cart'
import { getToken } from '../utils/token'
import { useRouter } from 'vue-router'

const router = useRouter()
const products = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getProductList()
    products.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

async function handleAddCart(product) {
  if (!getToken()) {
    router.push('/login')
    return
  }
  try {
    await addCart(product.id, 1)
    alert('已加入购物车')
    product.stock--
  } catch (e) {
    alert(e.message || '加入失败')
  }
}
</script>

<style scoped>
.banner {
  background: linear-gradient(135deg, #ff6700, #ff9a44);
  color: #fff; padding: 60px 20px; text-align: center;
}
.banner h1 { font-size: 32px; margin-bottom: 8px; }
.banner p { font-size: 16px; opacity: 0.9; }
.section { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
.section h2 { margin-bottom: 20px; color: #333; }
.loading, .empty { text-align: center; padding: 40px; color: #999; }
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; }
.product-card {
  background: #fff; border-radius: 8px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1); transition: transform 0.2s;
}
.product-card:hover { transform: translateY(-4px); }
.product-img { height: 160px; background: #f0f0f0; display: flex; align-items: center; justify-content: center; font-size: 48px; }
.product-info { padding: 16px; }
.product-info h3 { font-size: 16px; color: #333; margin-bottom: 8px; }
.price { color: #ff6700; font-size: 20px; font-weight: bold; margin-bottom: 4px; }
.stock { color: #999; font-size: 12px; margin-bottom: 12px; }
button {
  width: 100%; padding: 8px; background: #ff6700; color: #fff;
  border: none; border-radius: 4px; cursor: pointer; font-size: 14px;
}
button:disabled { background: #ccc; cursor: not-allowed; }
</style>
