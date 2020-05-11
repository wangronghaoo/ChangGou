package com.changgou.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.config.RabbitMqConfig;
import com.changgou.seckill.config.RabbitMqTemplate;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillService;
import com.changgou.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl implements SeckillService {


    //商品redis
    private final String SecKillGoodsKey = "SecKillGoodsKey_";

    //商品库存
    private final String SecKillGoodsCount = "SecKillGoodsCount_";

    //商品+用户,防止恶意刷单
    private final String SecKillOrder = "SecKillOrder_";


    @Autowired
    private SeckillOrderMapper seckillOrderMapper;


    @Autowired
    private IdWorker idWorker;


    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private RabbitMqTemplate rabbitMqTemplate;

    @Override
    public List<SeckillGoods> findList(String nowTime) {
        List<SeckillGoods> seckillGoods = redisTemplate.boundHashOps(SecKillGoodsKey + nowTime).values();


        //及时更新剩余数量
        for (SeckillGoods seckillGood : seckillGoods) {
            String stockCount = (String) redisTemplate.boundValueOps(SecKillGoodsCount + seckillGood.getId()).get();
            seckillGood.setStockCount(Integer.parseInt(stockCount));
        }

        return seckillGoods;
    }



    @Override
    public boolean addOrder(String username, String time, Long id) {

        //根据id获取商品对象
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SecKillGoodsKey + time).get(id);

        //获取商品库存
        String o = (String) redisTemplate.boundValueOps(SecKillGoodsCount + id).get();

        Integer stockCount = Integer.valueOf(o);

        //库存为0
        if (seckillGoods == null || stockCount <= 0){
            return false;
        }
        //如果都满足  ,进行redis库存减一
        Long decrement = redisTemplate.boundValueOps(SecKillGoodsCount + id).decrement();
        //扣减之后库存不足
        if (decrement <= 0){
            //删除商品 和 库存
            redisTemplate.boundHashOps(SecKillGoodsKey + time).delete(String.valueOf(id));
            redisTemplate.delete(String.valueOf(id));
        }

        //将用户id和商品id存放到redis中,防止恶意刷单,故意多次点击
        redisTemplate.boundValueOps(SecKillOrder + id).set(username,30,TimeUnit.SECONDS);


        //如果该用户属于购买同一商品,则为购买重复
        SeckillOrder order = seckillOrderMapper.getSeckillOrderByUserAndId(username, id);
        if (order != null){
            //重复下单
            System.out.println("用户重复下单");
            return false;
        }

        //创建秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setSeckillId(id);
        seckillOrder.setUserId(username);
        seckillOrder.setStatus("0");

        //发送消息
        rabbitMqTemplate.sendMessage("", RabbitMqConfig.addSeckillGoodsOrderQueue, JSON.toJSONString(seckillOrder));


        return true;
    }

}
