package org.example.service.weixin;

import org.example.domain.po.PayOrder;
import org.example.domain.req.ShopCartReq;
import org.example.domain.res.PayOrderRes;

import java.util.List;
import java.util.Map;

public interface IOrderService {
    PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception;

    void changeOrderPaySuccess(String orderId);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();



    boolean changeOrderClose(String orderId);

    Map<String, Object> createCartOrder(Long userId, Map<Long, Integer> cartItems);
}
