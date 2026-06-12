package org.example.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.domain.po.Product;
import java.util.List;

@Mapper
public interface IProductDao {
    Product queryById(Long id);
    List<Product> queryAll();
    int insert(Product product);
    int updateById(Product product);
    int deleteById(Long id);

    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}