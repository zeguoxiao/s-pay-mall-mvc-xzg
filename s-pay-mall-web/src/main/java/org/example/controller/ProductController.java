package org.example.controller;

import org.example.common.Constants.Constants;
import org.example.common.response.Response;
import org.example.domain.po.Product;
import org.example.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    @Autowired
    private IProductService productService;

    /** 查询所有商品 */
    @GetMapping("/list")
    public Response<List<Product>> list() {
        return Response.<List<Product>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("success")
                .data(productService.getAll())
                .build();
    }

    /** 根据ID查询商品 */
    @GetMapping("/detail")
    public Response<Product> detail(@RequestParam Long id) {
        return Response.<Product>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("success")
                .data(productService.getById(id))
                .build();
    }

    /** 添加商品 */
    @PostMapping("/add")
    public Response<Product> add(@RequestBody Product product) {
        return Response.<Product>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("添加成功")
                .data(productService.create(product))
                .build();
    }

    /** 修改商品 */
    @PostMapping("/update")
    public Response<Product> update(@RequestBody Product product) {
        return Response.<Product>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("修改成功")
                .data(productService.update(product))
                .build();
    }

    /** 删除商品 */
    @PostMapping("/delete")
    public Response<Object> delete(@RequestParam Long id) {
        productService.delete(id);
        return Response.<Object>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }
}