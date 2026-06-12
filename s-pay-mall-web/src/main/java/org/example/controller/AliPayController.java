package org.example.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants.Constants;
import org.example.common.response.Response;
import org.example.controller.dto.CreatePayRequestDTO;
import org.example.domain.req.ShopCartReq;
import org.example.domain.res.PayOrderRes;
import org.example.service.weixin.IOrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/alipay/")
public class AliPayController {

    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;

    @Resource
    private IOrderService orderService;
    @Resource
    private AlipayClient alipayClient;
    @Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;

    /**
     * http://localhost:8080/api/v1/alipay/create_pay_order
     *
     * {
     *     "userId": "10001",
     *     "productId": "100001"
     * }
     */
    @RequestMapping(value = "create_pay_order", method =  RequestMethod.POST)
    public Response<String> createPayOrder(@RequestBody CreatePayRequestDTO createPayRequestDTO){
        try {
            log.info("商品下单，根据商品ID创建支付单开始 userId:{} productId:{}", createPayRequestDTO.getUserId(), createPayRequestDTO.getUserId());
            String userId = createPayRequestDTO.getUserId();
            String productId = createPayRequestDTO.getProductId();
            // 下单
            PayOrderRes payOrderRes = orderService.createOrder(ShopCartReq.builder()
                    .userId(userId)
                    .productId(productId)
                    .build());

            log.info("商品下单，根据商品ID创建支付单完成 userId:{} productId:{} orderId:{}", userId, productId, payOrderRes.getOrderId());
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(payOrderRes.getPayUrl())
                    .build();
        } catch (Exception e) {
            log.error("商品下单，根据商品ID创建支付单失败 userId:{} productId:{}", createPayRequestDTO.getUserId(), createPayRequestDTO.getUserId(), e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


    @RequestMapping(value = "alipay_notify_url", method = RequestMethod.POST)
    public String payNotify(HttpServletRequest request) throws AlipayApiException {
        log.info("支付回调，消息接收 {}", request.getParameter("trade_status"));

        if (!request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            return "false";
        }

        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }

        String tradeNo = params.get("out_trade_no");
        String gmtPayment = params.get("gmt_payment");
        String alipayTradeNo = params.get("trade_no");

        String sign = params.get("sign");
        String content = AlipaySignature.getSignCheckContentV1(params);
        boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayPublicKey, "UTF-8"); // 验证签名
        // 支付宝验签
        if (!checkSignature) {
            return "false";
        }

        // 验签通过
        log.info("支付回调，交易名称: {}", params.get("subject"));
        log.info("支付回调，交易状态: {}", params.get("trade_status"));
        log.info("支付回调，支付宝交易凭证号: {}", params.get("trade_no"));
        log.info("支付回调，商户订单号: {}", params.get("out_trade_no"));
        log.info("支付回调，交易金额: {}", params.get("total_amount"));
        log.info("支付回调，买家在支付宝唯一id: {}", params.get("buyer_id"));
        log.info("支付回调，买家付款时间: {}", params.get("gmt_payment"));
        log.info("支付回调，买家付款金额: {}", params.get("buyer_pay_amount"));
        log.info("支付回调，支付回调，更新订单 {}", tradeNo);

        // 可以增加 code 10000 与 tradeStatus TRADE_SUCCESS 判断
        orderService.changeOrderPaySuccess(tradeNo);

        return "success";
    }

    /** 购物车结算 → 创建支付宝支付单 */
    @RequestMapping(value = "create_cart_pay_order", method = RequestMethod.POST)
    public Response<String> createCartPayOrder(@RequestBody Map<Long, Integer> cartItems,
                                               HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> orderResult = orderService.createCartOrder(userId, cartItems);
            String orderId = (String) orderResult.get("orderId");
            BigDecimal totalPrice = (BigDecimal) orderResult.get("totalPrice");

            // 创建支付宝支付单
            AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
            payRequest.setNotifyUrl(notifyUrl);
            payRequest.setReturnUrl(returnUrl);

            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderId);
            bizContent.put("total_amount", totalPrice.toString());
            bizContent.put("subject", "购物车订单");
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            payRequest.setBizContent(bizContent.toString());

            String form = alipayClient.pageExecute(payRequest).getBody();

            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("支付单创建成功")
                    .data(form)
                    .build();
        } catch (Exception e) {
            log.error("购物车结算支付失败", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("支付创建失败: " + e.getMessage())
                    .build();
        }
    }

}