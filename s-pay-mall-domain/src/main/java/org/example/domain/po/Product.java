package org.example.domain.po;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Product {
    private Long id;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private Date createTime;
    private Date updateTime;
}