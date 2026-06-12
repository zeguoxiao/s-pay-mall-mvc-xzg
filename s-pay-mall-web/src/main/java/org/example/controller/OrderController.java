package org.example.controller;

import org.example.common.Constants.Constants;
import org.example.common.response.Response;
import org.example.dao.IOrderDao;
import org.example.domain.po.PayOrder;
import org.example.domain.req.ShopCartReq;
import org.example.domain.res.PayOrderRes;
import org.example.service.impl.OrderServiceImpl;
import org.example.service.weixin.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IOrderDao orderDao;
    @Autowired
    private OrderServiceImpl orderServiceImpl;

    /** 单个商品下单（走支付宝） */
    @PostMapping("/create")
    public Response<Object> createOrder(HttpServletRequest request,
                                        @RequestBody ShopCartReq shopCartReq) {
        Long userId = (Long) request.getAttribute("userId");
        shopCartReq.setUserId(String.valueOf(userId));
        try {
            PayOrderRes res = orderService.createOrder(shopCartReq);
            return Response.<Object>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("下单成功")
                    .data(res)
                    .build();
        } catch (Exception e) {
            return Response.<Object>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("下单失败: " + e.getMessage())
                    .build();
        }
    }
    /** 购物车结算 → 生成订单 */
    @PostMapping("/cart/checkout")
    public Response<Object> cartCheckout(HttpServletRequest request,
                                         @RequestBody Map<Long, Integer> cartItems) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = orderServiceImpl.createCartOrder(userId, cartItems);
        return Response.<Object>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("下单成功")
                .data(result)
                .build();
    }

    /** 查我的订单 */
    @GetMapping("/list")
    public Response<Object> listOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<PayOrder> orders = orderDao.queryByUserId(String.valueOf(userId));
        return Response.<Object>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("success")
                .data(orders)
                .build();
    }
}