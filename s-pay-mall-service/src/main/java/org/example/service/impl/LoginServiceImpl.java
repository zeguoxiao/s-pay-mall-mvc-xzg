package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.common.utils.JwtUtil;
import org.example.dao.IUserDao;
import org.example.domain.po.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.IOException;

/**
 * 登录服务
 *
 * 核心流程：
 * register() → 检查用户名 → BCrypt加密密码 → 存数据库
 * login()    → 查数据库 → BCrypt验证密码 → 生成JWT → 存Redis → 返回Token
 * logout()   → 从Redis删掉Token → 前端再用这个Token请求就会被拦截器拦住
 *
 * BCrypt 是什么：
 * 同一个密码 "123456"，每次加密结果都不一样（加了随机盐）
 * 但验证时能正确匹配，所以安全性比 MD5 高很多
 */
@Slf4j
@Service
public class LoginServiceImpl {

    @Autowired
    private IUserDao userDao;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 用户注册
    public User register(String userName, String password, String nickName) {
        // 1. 检查用户名是否已存在
        User existUser = userDao.queryByUserName(userName);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在: " + userName);
        }

        // 2. 创建用户，密码用 BCrypt 加密后存库
        User user = new User();
        user.setUserName(userName);
        user.setPassword(encoder.encode(password));
        user.setNickName(StringUtils.isNotBlank(nickName) ? nickName : userName);
        user.setRole("USER");

        // 3. 保存到数据库
        userDao.insert(user);
        log.info("用户注册成功: {}", userName);
        return user;
    }

    // 用户登录
    public String login(String userName, String password) {
        // 1. 根据用户名查数据库
        User user = userDao.queryByUserName(userName);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. BCrypt 验证密码（比对明文和加密后的值）
        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 3. 生成 JWT Token（把 userId 和 userName 加密进去）
        String token = jwtUtil.generateToken(user.getId(), user.getUserName());

        // 4. Token 存到 Redis（24小时后自动过期）
        tokenService.saveToken(token, user.getId());

        log.info("用户登录成功: {}", userName);
        return token;
    }

    // 用户登出：从 Redis 删掉 Token
    public void logout(String token) {
        tokenService.removeToken(token);
        log.info("用户已登出");
    }



}