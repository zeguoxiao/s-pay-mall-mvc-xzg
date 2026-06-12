<template>
  <div class="orders-page">
    <h2>我的订单</h2>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="orders.length === 0" class="empty">
      <p>暂无订单</p>
      <router-link to="/" class="go-shop">去逛逛</router-link>
    </div>
    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-id">订单号: {{ order.orderId }}</span>
          <span :class="['status', order.status.toLowerCase()]">{{ statusText(order.status) }}</span>
        </div>
        <div class="order-body">
          <p>商品: {{ order.productName || '购物车订单' }}</p>
          <p class="order-amount">金额: ¥{{ order.totalAmount }}</p>
          <p class="order-time">下单时间: {{ formatTime(order.createTime) }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderList } from '../api/order'

const orders = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getOrderList()
    orders.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

function statusText(status) {
  const map = {
    CREATE: '已创建',
    PAY_WAIT: '待支付',
    PAY_SUCCESS: '已支付',
    DEAL_DONE: '已完成',
    CLOSE: '已关闭',
  }
  return map[status] || status
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.orders-page { max-width: 800px; margin: 30px auto; padding: 0 20px; }
h2 { margin-bottom: 20px; color: #333; }
.loading, .empty { text-align: center; padding: 60px; color: #999; }
.go-shop { color: #ff6700; text-decoration: none; margin-top: 12px; display: inline-block; }
.order-card {
  background: #fff; border-radius: 8px; padding: 16px;
  margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.order-header {
  display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 12px; border-bottom: 1px solid #f0f0f0;
}
.order-id { font-size: 14px; color: #666; }
.status {
  padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: bold;
}
.status.create { background: #e6f7ff; color: #1890ff; }
.status.pay_wait { background: #fff7e6; color: #fa8c16; }
.status.pay_success { background: #f6ffed; color: #52c41a; }
.status.deal_done { background: #f6ffed; color: #52c41a; }
.status.close { background: #fff1f0; color: #ff4d4f; }
.order-body { padding-top: 12px; }
.order-body p { font-size: 14px; color: #333; margin-bottom: 4px; }
.order-amount { color: #ff6700; font-weight: bold; font-size: 16px; }
.order-time { color: #999; font-size: 12px; }
</style>
