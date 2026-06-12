import axios from 'axios'
import { getToken, removeToken } from './token'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
})

// 请求拦截器：自动带 token
request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (res) => {
    if (res.data.code === '0000') {
      return res.data
    }
    return Promise.reject(new Error(res.data.info || '请求失败'))
  },
  (err) => {
    if (err.response?.status === 401 || err.response?.status === 403) {
      removeToken()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default request
