package org.example.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.common.Constants.Constants;
import org.example.common.response.Response;
import org.example.service.impl.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 *
 * 流程：
 * 1. 前端请求进来 → 先经过这里
 * 2. 从请求头取 Authorization: Bearer xxx
 * 3. 去 Redis 查这个 Token 存不存在
 * 4. 存在 → 放行，Controller 里能拿到 userId
 * 5. 不存在 → 返回 401
 *
 * 为什么不用 Filter：
 * Filter 是 Servlet 层的，拦截所有请求
 * Interceptor 是 Spring 层的，能访问 Controller 上下文，更灵活
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 从请求头取 Token
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            send401(response, "未登录");
            return false;
        }

        // 2. 提取 Token（去掉 "Bearer " 前缀）
        String token = authHeader.substring(7);

        // 3. 去 Redis 查这个 Token 对应的 userId
        Long userId = tokenService.getUserIdByToken(token);
        if (userId == null) {
            send401(response, "Token 已过期或无效");
            return false;
        }

        // 4. 放行，把 userId 存到 request 里
        // Controller 里用 request.getAttribute("userId") 就能拿到
        request.setAttribute("userId", userId);
        return true;
    }

    // 返回 401 JSON
    private void send401(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        Response<Object> resp = Response.<Object>builder()
                .code(Constants.ResponseCode.NO_LOGIN.getCode())
                .info(message)
                .build();
        response.getWriter().write(JSON.toJSONString(resp));
    }
}