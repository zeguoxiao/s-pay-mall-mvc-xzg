package org.example.controller;

import org.example.common.Constants.Constants;
import org.example.common.response.Response;
import org.example.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    @Autowired
    private ICartService cartService;

    /** 加购物车 */
    @PostMapping("/add")
    public Response<Object> add(@RequestParam Long productId,
                                @RequestParam(defaultValue = "1") int quantity,
                                @RequestAttribute Long userId) {
        cartService.addItem(userId, productId, quantity);
        return Response.<Object>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("已加入购物车")
                .build();
    }

    /** 改数量 */
    @PostMapping("/update")
    public Response<Object> update(@RequestParam Long productId,
                                   @RequestParam int quantity,
                                   @RequestAttribute Long userId) {
        cartService.updateQuantity(userId, productId, quantity);
        return Response.<Object>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("数量已更新")
                .build();
    }

    /** 删商品 */
    @PostMapping("/remove")
    public Response<Object> remove(@RequestParam Long productId,
                                   @RequestAttribute Long userId) {
        cartService.removeItem(userId, productId);
        return Response.<Object>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("已移除")
                .build();
    }

    /** 查购物车 */
    @GetMapping("/list")
    public Response<Map<Long, Integer>> list(@RequestAttribute Long userId) {
        return Response.<Map<Long, Integer>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("success")
                .data(cartService.getCart(userId))
                .build();

    }


}