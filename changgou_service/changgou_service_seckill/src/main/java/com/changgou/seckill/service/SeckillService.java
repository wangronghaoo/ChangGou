package com.changgou.seckill.service;

import com.changgou.seckill.pojo.SeckillGoods;

import java.util.List;

public interface SeckillService {
    List<SeckillGoods> findList(String nowTime);

    boolean addOrder(String username, String time, Long id);
}
