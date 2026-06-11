package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.common.Constants.Constants;
import org.example.common.response.Response;
import org.example.service.impl.LoginServiceImpl;
import org.example.service.impl.WeixinLoginServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/login/")
public class LoginController {

    @Autowired
    private LoginServiceImpl loginService;
    @Autowired
    private WeixinLoginServiceImpl weixinLoginService;
    // ========== 微信登录（原来的不动） ==========

    @RequestMapping(value = "weixin_qrcode_ticket", method = RequestMethod.GET)
    public Response<String> weixinQrCodeTicket() {
        try {
            String qrCodeTicket = weixinLoginService.createQrCodeTicket();
            log.info("生成微信扫码登录 ticket:{}", qrCodeTicket);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(qrCodeTicket)
                    .build();
        } catch (Exception e) {
            log.error("生成微信扫码登录 ticket 失败", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "check_login", method = RequestMethod.GET)
    public Response<String> checkLogin(@RequestParam String ticket) {
        try {
            String openidToken = weixinLoginService.checkLogin(ticket);
            log.info("扫码检测登录结果 ticket:{} openidToken:{}", ticket, openidToken);
            if (StringUtils.isNotBlank(openidToken)) {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.SUCCESS.getCode())
                        .info(Constants.ResponseCode.SUCCESS.getInfo())
                        .data(openidToken)
                        .build();
            } else {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.NO_LOGIN.getCode())
                        .info(Constants.ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }
        } catch (Exception e) {
            log.error("扫码检测登录结果失败 ticket:{}", ticket, e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    // ========== 用户注册/登录/登出（新增的） ==========

    /** 注册 */
    @RequestMapping(value = "register", method = RequestMethod.POST)
    public Response<Object> register(@RequestBody Map<String, String> body) {
        try {
            String userName = body.get("userName");
            String password = body.get("password");
            String nickName = body.get("nickName");

            if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
                return Response.<Object>builder()
                        .code(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("用户名和密码不能为空")
                        .build();
            }

            loginService.register(userName, password, nickName);

            return Response.<Object>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("注册成功")
                    .build();
        } catch (Exception e) {
            log.error("注册失败", e);
            return Response.<Object>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    /** 登录 */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Response<Object> login(@RequestBody Map<String, String> body) {
        try {
            String userName = body.get("userName");
            String password = body.get("password");

            if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
                return Response.<Object>builder()
                        .code(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("用户名和密码不能为空")
                        .build();
            }

            String token = loginService.login(userName, password);

            return Response.<Object>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("登录成功")
                    .data(token)
                    .build();
        } catch (Exception e) {
            log.error("登录失败", e);
            return Response.<Object>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    /** 登出 */
    @RequestMapping(value = "logout", method = RequestMethod.POST)
    public Response<Object> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                loginService.logout(authHeader.substring(7));
            }
            return Response.<Object>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("登出成功")
                    .build();
        } catch (Exception e) {
            return Response.<Object>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("登出失败")
                    .build();
        }
    }
    //Redis中 token没有删掉的原因排查log.info消息到控制台
//@RequestMapping(value = "logout", method = RequestMethod.POST)
//public Response<Object> logout(HttpServletRequest request) {
//    try {
//        String authHeader = request.getHeader("Authorization");
//        log.info("Authorization原始值: {}", authHeader);
//        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//            log.info("提取的token: {}", token);
//            loginService.logout(token);
//        }
//        return Response.<Object>builder()
//                .code(Constants.ResponseCode.SUCCESS.getCode())
//                .info("登出成功")
//                .build();
//    } catch (Exception e) {
//        return Response.<Object>builder()
//                .code(Constants.ResponseCode.UN_ERROR.getCode())
//                .info("登出失败")
//                .build();
//    }
//}

    /** 获取当前用户信息 */
    @RequestMapping(value = "userinfo", method = RequestMethod.GET)
    public Response<Object> userInfo(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            return Response.<Object>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("查询成功")
                    .data(data)
                    .build();
        } catch (Exception e) {
            return Response.<Object>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }
}