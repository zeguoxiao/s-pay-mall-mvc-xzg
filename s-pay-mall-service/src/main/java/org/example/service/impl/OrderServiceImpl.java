package org.example.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.common.Constants.Constants;
import org.example.dao.IOrderDao;
import org.example.dao.IProductDao;
import org.example.domain.po.OrderItem;
import org.example.dao.IOrderItemDao;
import org.example.domain.po.PayOrder;
import org.example.domain.po.Product;
import org.example.domain.req.ShopCartReq;
import org.example.domain.res.PayOrderRes;
import org.example.domain.vo.ProductVO;
import org.example.service.ICartService;
import org.example.service.rpc.ProductRPC;
import org.example.service.weixin.IOrderService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;

    @Resource
    private IOrderDao orderDao;
    @Resource
    private ProductRPC productRPC;
    @Resource
    private IProductDao productDao;
    @Resource
    private AlipayClient alipayClient;
    @Resource
    private IOrderItemDao orderItemDao;

    @Autowired
    private ICartService cartService;
    @Autowired
    private RedissonClient redissonClient;

    @Resource
    private EventBus eventBus;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception {
        // 1. 查询当前用户是否存在未支付订单或掉单订单
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setUserId(shopCartReq.getUserId());
        payOrderReq.setProductId(shopCartReq.getProductId());

        //PayOrder unpaidOrder = orderDao.queryUnPayOrder(payOrderReq);
        PayOrder unpaidOrder = getUnpaidOrderFromCacheOrDb(shopCartReq.getUserId(), shopCartReq.getProductId());

        if (null != unpaidOrder && Constants.OrderStatusEnum.PAY_WAIT.getCode().equals(unpaidOrder.getStatus())) {
            log.info("创建订单-存在，已存在未支付订单。userId:{} productId:{} orderId:{}", shopCartReq.getUserId(), shopCartReq.getProductId(), unpaidOrder.getOrderId());
            return PayOrderRes.builder()
                    .orderId(unpaidOrder.getOrderId())
                    .payUrl(unpaidOrder.getPayUrl())
                    .build();
        } else if (null != unpaidOrder && Constants.OrderStatusEnum.CREATE.getCode().equals(unpaidOrder.getStatus())) {
            log.info("创建订单-存在，存在未创建支付单订单，创建支付单开始 userId:{} productId:{} orderId:{}", shopCartReq.getUserId(), shopCartReq.getProductId(), unpaidOrder.getOrderId());
            PayOrder payOrder = doPrepayOrder(unpaidOrder.getProductId(), unpaidOrder.getProductName(), unpaidOrder.getOrderId(), unpaidOrder.getTotalAmount());
            return PayOrderRes.builder()
                    .orderId(payOrder.getOrderId())
                    .payUrl(payOrder.getPayUrl())
                    .build();
        }

        // 2. 查询商品 & 创建订单
        ProductVO productVO = productRPC.queryProductByProductId(shopCartReq.getProductId());
        String orderId = RandomStringUtils.randomNumeric(16);
        orderDao.insert(PayOrder.builder()
                .userId(shopCartReq.getUserId())
                .productId(shopCartReq.getProductId())
                .productName(productVO.getProductName())
                .orderId(orderId)
                .totalAmount(productVO.getPrice())
                .orderTime(new Date())
                .status(Constants.OrderStatusEnum.CREATE.getCode())
                .build());

        // 3. 创建支付单
        PayOrder payOrder = doPrepayOrder(productVO.getProductId(), productVO.getProductName(), orderId, productVO.getPrice());

        return PayOrderRes.builder()
                .orderId(orderId)
                .payUrl(payOrder.getPayUrl())
                .build();
    }

    @Override
    public void changeOrderPaySuccess(String orderId) {
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setOrderId(orderId);
        payOrderReq.setStatus(Constants.OrderStatusEnum.PAY_SUCCESS.getCode());
        orderDao.changeOrderPaySuccess(payOrderReq);
        // 支付成功后减库存
        PayOrder order = orderDao.queryByOrderNo(orderId);
        if (order != null) {
            List<OrderItem> items = orderItemDao.queryByOrderNo(orderId);
            if (items != null && !items.isEmpty()) {
                for (OrderItem item : items) {
                    productDao.deductStock(item.getProductId(), item.getQuantity());
                }
            } else if (order.getProductId() != null) {
                productDao.deductStock(Long.parseLong(order.getProductId()),1);
            }
        }

        eventBus.post(JSON.toJSONString(payOrderReq));
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderDao.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return orderDao.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return orderDao.changeOrderClose(orderId);
    }

    private PayOrder doPrepayOrder(String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", totalAmount.toString());
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();

        PayOrder payOrder = new PayOrder();
        payOrder.setOrderId(orderId);
        payOrder.setPayUrl(form);
        payOrder.setStatus(Constants.OrderStatusEnum.PAY_WAIT.getCode());

        orderDao.updateOrderPayInfo(payOrder);

        return payOrder;
    }/**
     * 先查Redis缓存，没有再查数据库
     * 这就是"缓存穿透"的基本防护：查到了就缓存，查不到也缓存一个空值
     */
    private PayOrder getUnpaidOrderFromCacheOrDb(String userId, String productId) {
        String cacheKey = "order:unpay:" + userId + ":" + productId;

        try {
            RBucket<String> bucket = redissonClient.getBucket(cacheKey);
            String cached = bucket.get();
            if (cached != null) {
                log.info("Redis缓存命中, userId:{} productId:{}", userId, productId);
                return objectMapper.readValue(cached, PayOrder.class);
            }
        } catch (Exception e) {
            log.warn("Redis查询异常，降级查数据库", e);
        }

        PayOrder query = new PayOrder();
        query.setUserId(userId);
        query.setProductId(productId);
        PayOrder order = orderDao.queryUnPayOrder(query);

        if (order != null) {
            try {
                RBucket<String> bucket = redissonClient.getBucket(cacheKey);
                bucket.set(objectMapper.writeValueAsString(order), 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis写入异常", e);
            }
        }

        return order;
    }
    /**
     * 购物车下单：遍历购物车 → 扣库存 → 生成订单
     */
    public Map<String, Object> createCartOrder(Long userId, Map<Long, Integer> cartItems) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

//            // 扣库存（防超卖）模拟的
//            int rows = productRPC.deductStock(productId, quantity);
            int rows = productDao.deductStock(productId, quantity);
            if (rows == 0) {
                throw new RuntimeException("商品库存不足，productId: " + productId);
            }

//            // 查商品信息 模拟的
//            ProductVO productVO = productRPC.queryProductByProductId(productId);
            Product product = productDao.queryById(productId);

//            // 构建订单明细
//            OrderItem item = new OrderItem();
//            item.setProductId(productId);
//            item.setProductName(productVO.getProductName());
//            item.setPrice(productVO.getPrice());
//            item.setQuantity(quantity);
//            items.add(item);
//
//            // 累加总价
//            totalPrice = totalPrice.add(productVO.getPrice().multiply(new BigDecimal(quantity)));
            OrderItem item = new OrderItem();
            item.setProductId(productId);
            item.setProductName(product.getProductName());
            item.setPrice(product.getPrice());
            item.setQuantity(quantity);
            items.add(item);

            totalPrice = totalPrice.add(product.getPrice().multiply(new BigDecimal(quantity)));
        }

        // 创建订单
        String orderId = RandomStringUtils.randomNumeric(16);
        PayOrder order = PayOrder.builder()
                .userId(String.valueOf(userId))
                .orderId(orderId)
                .productId(null)  // 购物车下单，多件商品，没有单一 productId
                .productName("购物车订单")
                .totalAmount(totalPrice)
                .orderTime(new Date())
                .status(Constants.OrderStatusEnum.CREATE.getCode())
                .build();
        orderDao.insert(order);

        // 保存订单明细
        for (OrderItem item : items) {
            item.setOrderId(Long.valueOf(orderId));  // String 转 Long
        }
        orderItemDao.insertBatch(items);// 循环外面，一次插入全部

        log.info("购物车下单成功: {}, 总价: {}", orderId, totalPrice);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("totalPrice", totalPrice);
        cartService.clearCart(userId);
        return result;
    }

}