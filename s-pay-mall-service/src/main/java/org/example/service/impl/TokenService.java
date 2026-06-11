package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 服务 - 用 Redisson 操作 Redis
 *
 * Redis 里存的结构：
 * key = "login:token:eyJhbGciOi..."（Token 字符串）
 * value = userId
 * 过期时间 = 24小时
 *
 * 为什么用 Redisson 而不是原生 Redis：
 * 1. RBucket 自带过期时间设置，一行搞定
 * 2. 支持分布式锁，后面防超卖会用到
 * 3. API 更简洁，不用自己拼命令
 */
@Slf4j
@Service
public class TokenService {

    @Autowired
    private RedissonClient redissonClient;

    private static final String TOKEN_PREFIX = "login:token:";
    private static final long TOKEN_EXPIRE_HOURS = 24;

    // 保存 Token 到 Redis
    public void saveToken(String token, Long userId) {
        String key = TOKEN_PREFIX + token;
        RBucket<Long> bucket = redissonClient.getBucket(key);
        bucket.set(userId, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        log.info("Token 已保存, userId: {}", userId);
    }

    // 根据 Token 取 userId，取不到说明没登录或过期了
    public Long getUserIdByToken(String token) {
        String key = TOKEN_PREFIX + token;
        RBucket<Long> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    // 删除 Token（用户登出）
    public void removeToken(String token) {
        String key = TOKEN_PREFIX + token;
        redissonClient.getBucket(key).delete();
    }
// 删除 Token（用户登出）排查可删除的Token
//public void removeToken(String token) {
//    String key = TOKEN_PREFIX + token;
//    log.info("尝试删除Token: key={}", key);
//    boolean result = redissonClient.getBucket(key).delete();
//    log.info("Token删除结果: {}", result);
//}
}