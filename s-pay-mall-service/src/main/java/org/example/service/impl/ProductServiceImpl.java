package org.example.service.impl;

import org.example.dao.IProductDao;
import org.example.domain.po.Product;
import org.example.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private IProductDao productDao;

    @Override
    public Product getById(Long id) {
        return productDao.queryById(id);
    }

    @Override
    public List<Product> getAll() {
        return productDao.queryAll();
    }

    @Override
    public Product create(Product product) {
        productDao.insert(product);
        return product;
    }

    @Override
    public Product update(Product product) {
        productDao.updateById(product);
        return productDao.queryById(product.getId());
    }

    @Override
    public boolean delete(Long id) {
        return productDao.deleteById(id) > 0;
    }
}