package com.changgou.order.service;

import java.util.Map;

public interface CartService {
    void add(String username, String skuId, Integer num);

    Map<String,Object> cartList(String username);

    void delete(String username, String skuId);
}
