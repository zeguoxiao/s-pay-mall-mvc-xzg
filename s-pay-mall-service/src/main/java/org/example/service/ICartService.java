package org.example.service;

import java.util.Map;

public interface ICartService {
    void addItem(Long userId, Long productId, int quantity);
    void updateQuantity(Long userId, Long productId, int quantity);
    void removeItem(Long userId, Long productId);
    Map<Long, Integer> getCart(Long userId);

    void clearCart(Long userId);
}