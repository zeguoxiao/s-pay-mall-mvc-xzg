package org.example.service;

import org.example.domain.po.Product;
import java.util.List;

public interface IProductService {
    Product getById(Long id);
    List<Product> getAll();
    Product create(Product product);
    Product update(Product product);
    boolean delete(Long id);
}