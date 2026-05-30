# s-pay-mall-mvc-xzg

基于 Spring Boot + MyBatis 的支付商城 MVC 项目，学习小傅哥 [s-pay-mall-mvc](https://github.com/fuzhengwei/s-pay-mall-mvc) 课程后的实践版本。

## 📌 项目简介

本项目是一个支付商城的后端服务，主要实现：

- **微信扫码登录** — 对接微信公众号，生成二维码扫码登录
- **支付宝沙箱支付** — 集成支付宝 SDK，支持沙箱环境测试支付流程
- **订单管理** — 创建订单、查询订单、关闭订单
- **定时任务** — 未支付订单通知、超时订单自动关闭
- **支付回调监听** — 监听支付成功事件，更新订单状态

## 🛠 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.12 | 基础框架 |
| MyBatis | 2.1.4 | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| Redis (Redisson) | 3.26.0 | 缓存/分布式锁 |
| 支付宝 SDK | 4.38.157 | 支付对接 |
| Retrofit2 | 2.9.0 | HTTP 客户端 |
| Lombok | 1.18.38 | 简化代码 |

## 📁 项目结构

```
s-pay-mall-mvc-xzg
├── s-pay-mall-common       # 公共模块（常量、异常、工具类）
├── s-pay-mall-dao           # 数据访问层（MyBatis Mapper）
├── s-pay-mall-domain        # 领域模型（PO/VO/DTO/Req/Res）
├── s-pay-mall-service       # 业务逻辑层
│   └── weixin               # 微信相关服务
└── s-pay-mall-web           # Web 启动模块
    ├── config               # 配置类
    ├── controller           # 接口控制器
    ├── job                  # 定时任务
    └── listener             # 事件监听器
```

## 🚀 快速开始

### 环境要求

- JDK 8+
- MySQL 8.x
- Redis
- Maven 3.x

### 1. 初始化数据库

```sql
CREATE DATABASE s-pay-mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

> 具体表结构参考原项目或后续补充的 SQL 脚本。

### 2. 修改配置

编辑 `s-pay-mall-web/src/main/resources/application-dev.yml`，修改数据库和微信/支付宝配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/s-pay-mall?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码

weixin:
  config:
    app-id: 你的微信AppID
    app-secret: 你的微信AppSecret
```

### 3. 编译运行

```bash
# 编译
mvn clean compile

# 启动
cd s-pay-mall-web
mvn spring-boot:run
```

启动后访问 http://localhost:8080

### 4. 接口测试

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/login/weixin_qrcode_ticket` | GET | 生成微信扫码登录二维码 |
| `/api/v1/login/check_login` | GET | 检测扫码登录状态 |
| `/api/v1/alipay/*` | POST | 支付宝支付相关接口 |

## 📝 学习笔记

这个项目是跟着小傅哥的课程一步步做的，主要收获：

- Spring Boot 多模块项目结构搭建
- 微信公众号开发流程（扫码登录、模板消息）
- 支付宝沙箱环境对接
- 定时任务 + 事件监听器的使用
- MyBatis 多数据源配置

## ⚠️ 注意事项

- 支付宝使用的是**沙箱环境**，不是真实支付
- 微信配置需要在[微信公众平台](https://mp.weixin.qq.com/)申请测试号
- 请勿将真实的密钥和私钥提交到 GitHub

## 📄 License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
