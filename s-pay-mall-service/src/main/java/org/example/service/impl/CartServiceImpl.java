package org.example.service.impl;

import org.example.service.ICartService;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private RedissonClient redissonClient;

    /** 购物车 key 前缀 */
    private static final String CART_PREFIX = "cart:";

    /** 加购物车：已有则数量+1，没有则新增 */
    @Override
    public void addItem(Long userId, Long productId, int quantity) {
        String key = CART_PREFIX + userId;
        RMap<Long, Integer> cart = redissonClient.getMap(key);
        cart.merge(productId, quantity, Integer::sum);
    }

    /** 改数量：直接覆盖 */
    @Override
    public void updateQuantity(Long userId, Long productId, int quantity) {
        String key = CART_PREFIX + userId;
        RMap<Long, Integer> cart = redissonClient.getMap(key);
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }
    }

    /** 删商品 */
    @Override
    public void removeItem(Long userId, Long productId) {
        String key = CART_PREFIX + userId;
        RMap<Long, Integer> cart = redissonClient.getMap(key);
        cart.remove(productId);
    }

    /** 查购物车：返回 {productId: 数量} */
    @Override
    public Map<Long, Integer> getCart(Long userId) {
        String key = CART_PREFIX + userId;
        RMap<Long, Integer> cart = redissonClient.getMap(key);
        return cart;
    }
    // 清空购物车
    @Override
    public void clearCart(Long userId) {
        RBucket<Map<Long, Integer>> bucket = redissonClient.getBucket("cart:" + userId);
        bucket.delete();
    }
}