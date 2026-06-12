package org.example.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.domain.po.OrderItem;
import java.util.List;

@Mapper
public interface IOrderItemDao {
    int insertBatch(List<OrderItem> items);
    List<OrderItem> queryByOrderId(Long orderId);
}