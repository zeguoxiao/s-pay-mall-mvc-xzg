import request from '../utils/request'

// 登录
export function login(data) {
  return request.post('/api/v1/login/login', data)
}

// 注册
export function register(data) {
  return request.post('/api/v1/login/register', data)
}

// 用户信息
export function getUserInfo() {
  return request.get('/api/v1/login/userinfo')
}

// 退出登录
export function logout() {
  return request.get('/api/v1/login/logout')
}
