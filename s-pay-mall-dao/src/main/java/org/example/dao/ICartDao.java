package org.example.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.domain.po.Product;
import java.util.List;

@Mapper
public interface ICartDao {
    Product queryById(Long id);
}