package com.changgou.seckill.service.impl;

import com.changgou.seckill.dao.SeckillMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Override
    @Transactional
    public boolean createSeckillOrder(SeckillOrder seckillOrder) {

        try {
            //查出该商品
            SeckillGoods seckillGoods = seckillMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
            //设置修改条件
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            //减库存
            seckillMapper.updateByPrimaryKey(seckillGoods);
            //插入秒杀订单
            seckillOrderMapper.insertSelective(seckillOrder);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
