<template>
  <div class="orders-page">
    <h2>我的订单</h2>
    <div class="tabs">
      <span :class="['tab', filter === 'pending' && 'active']" @click="filter = 'pending'">待支付</span>
      <span :class="['tab', filter === 'all' && 'active']" @click="filter = 'all'">全部订单</span>
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="displayOrders.length === 0" class="empty">
      <p>暂无订单</p>
      <router-link to="/" class="go-shop">去逛逛</router-link>
    </div>
    <template v-else>
      <div class="order-list">
        <label v-for="order in displayOrders" :key="order.id" class="order-card">
          <input type="checkbox" :value="order.id" v-model="selectedIds" class="checkbox" />
          <div class="order-content">
            <div class="order-header">
              <span class="order-id">订单号: {{ order.orderId }}</span>
              <div class="header-right">
                <span :class="['status', order.status.toLowerCase()]">{{ statusText(order.status) }}</span>
                <span class="delete-btn" @click.stop="handleDelete(order)">删除</span>
              </div>
            </div>
            <div class="order-body">
              <p>商品: {{ order.productName || '购物车订单' }}</p>
              <p class="order-amount">金额: ¥{{ order.totalAmount }}</p>
              <p class="order-time">下单时间: {{ formatTime(order.createTime) }}</p>
            </div>
          </div>
        </label>
      </div>
      <div class="pay-bar">
        <label class="select-all">
          <input type="checkbox" :checked="isAllSelected" @change="toggleAll" />
          全选
        </label>
        <div class="pay-right">
          <span class="total">已选 {{ selectedIds.length }} 件，合计：<b>¥{{ totalPrice }}</b></span>
          <button class="delete-btn-bar" :disabled="selectedIds.length === 0" @click="handleBatchDelete">删除</button>
          <button class="pay-btn" :disabled="selectedIds.length === 0" @click="handlePay">去支付</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getOrderList, deleteOrder } from '../api/order'
import request from '../utils/request'

const orders = ref([])
const loading = ref(true)
const selectedIds = ref([])
const filter = ref('pending')

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

const displayOrders = computed(() => {
  if (filter.value === 'pending') {
    return orders.value.filter(o => o.status === 'CREATE' || o.status === 'PAY_WAIT')
  }
  return orders.value
})

const isAllSelected = computed(() => {
  const payable = displayOrders.value.filter(o => o.status === 'CREATE' || o.status === 'PAY_WAIT')
  return payable.length > 0 && payable.every(o => selectedIds.value.includes(o.id))
})

const totalPrice = computed(() => {
  return displayOrders.value
    .filter(o => (o.status === 'CREATE' || o.status === 'PAY_WAIT') && selectedIds.value.includes(o.id))
    .reduce((sum, o) => sum + Number(o.totalAmount || 0), 0).toFixed(2)
})

function toggleAll() {
  const payable = displayOrders.value.filter(o => o.status === 'CREATE' || o.status === 'PAY_WAIT')
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = payable.map(o => o.id)
  }
}

async function handleDelete(order) {
  if (!confirm('确定删除订单 ' + order.orderId + '？')) return
  try {
    await deleteOrder(order.id)
    orders.value = orders.value.filter(o => o.id !== order.id)
    selectedIds.value = selectedIds.value.filter(id => id !== order.id)
  } catch (e) {
    alert('删除失败: ' + e.message)
  }
}

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

async function handleBatchDelete() {
  if (!confirm('确定删除选中的 ' + selectedIds.value.length + ' 个订单？')) return
  try {
    for (const id of selectedIds.value) {
      await deleteOrder(id)
    }
    orders.value = orders.value.filter(o => !selectedIds.value.includes(o.id))
    selectedIds.value = []
  } catch (e) {
    alert('删除失败: ' + e.message)
  }
}

async function handlePay() {
  const payOrders = displayOrders.value.filter(
    o => (o.status === 'CREATE' || o.status === 'PAY_WAIT') && selectedIds.value.includes(o.id)
  )
  if (payOrders.length === 0) return

  // 多个订单逐个支付
  for (const order of payOrders) {
    try {
      const res = await request.post('/api/v1/alipay/create_pay_order', {
        userId: String(order.userId || ''),
        productId: String(order.productId || '')
      })
      if (res.code === '0000' && res.data) {
        window.location.href = res.data
      } else {
        alert('支付创建失败: ' + (res.info || '未知错误'))
      }
    } catch (e) {
      alert('支付创建失败: ' + e.message)
    }
  }
}
</script>

<style scoped>
.orders-page { max-width: 800px; margin: 30px auto; padding: 0 20px; }
h2 { margin-bottom: 16px; color: #333; }
.tabs { display: flex; gap: 16px; margin-bottom: 20px; }
.tab {
  padding: 8px 20px; cursor: pointer; font-size: 14px; color: #666;
  border-bottom: 2px solid transparent;
}
.tab.active { color: #ff6700; border-bottom-color: #ff6700; font-weight: bold; }
.loading, .empty { text-align: center; padding: 60px; color: #999; }
.go-shop { color: #ff6700; text-decoration: none; margin-top: 12px; display: inline-block; }
.order-card {
  display: flex; align-items: flex-start; gap: 12px;
  background: #fff; border-radius: 8px; padding: 16px;
  margin-bottom: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); cursor: pointer;
}
.checkbox { margin-top: 6px; width: 18px; height: 18px; cursor: pointer; }
.order-content { flex: 1; }
.order-header {
  display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 12px; border-bottom: 1px solid #f0f0f0;
}
.order-id { font-size: 14px; color: #666; }
.header-right { display: flex; align-items: center; gap: 12px; }
.status { padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: bold; }
.status.create { background: #e6f7ff; color: #1890ff; }
.status.pay_wait { background: #fff7e6; color: #fa8c16; }
.status.pay_success { background: #f6ffed; color: #52c41a; }
.status.deal_done { background: #f6ffed; color: #52c41a; }
.status.close { background: #fff1f0; color: #ff4d4f; }
.delete-btn { color: #ff4d4f; font-size: 12px; cursor: pointer; }
.delete-btn:hover { text-decoration: underline; }
.order-body { padding-top: 12px; }
.order-body p { font-size: 14px; color: #333; margin-bottom: 4px; }
.order-amount { color: #ff6700; font-weight: bold; font-size: 16px; }
.order-time { color: #999; font-size: 12px; }
.pay-bar {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 16px 20px; border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05); position: sticky; bottom: 0;
}
.select-all { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 14px; }
.select-all input { width: 18px; height: 18px; cursor: pointer; }
.pay-right { display: flex; align-items: center; gap: 20px; }
.total { font-size: 14px; color: #333; }
.total b { color: #ff6700; font-size: 20px; }
.pay-btn {
  padding: 10px 32px; background: #ff6700; color: #fff;
  border: none; border-radius: 4px; font-size: 16px; cursor: pointer;
}
.pay-btn:disabled { background: #ccc; cursor: not-allowed; }
.pay-btn:hover:not(:disabled) { background: #e55d00; }
.delete-btn-bar {
  padding: 10px 24px; background: #fff; color: #ff4d4f;
  border: 1px solid #ff4d4f; border-radius: 4px; font-size: 14px; cursor: pointer;
}
.delete-btn-bar:disabled { color: #ccc; border-color: #ccc; cursor: not-allowed; }
.delete-btn-bar:hover:not(:disabled) { background: #fff1f0; }
</style>
