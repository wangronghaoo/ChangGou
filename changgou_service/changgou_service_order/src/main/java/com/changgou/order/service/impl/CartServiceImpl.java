package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SpuFeign spuFeign;
    @Override
    public void add(String username, String skuId, Integer num) {

        //根据用户名查询redis,存在则已经购买
        OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps("Cart_" + username).get(skuId);
        if (orderItem != null) {
            //之前的数量
            Integer beforeNum = orderItem.getNum();
            Integer totalNum = beforeNum + num;
            orderItem.setNum(totalNum);
            //总的价格为 数量 * 单价
            orderItem.setPayMoney(orderItem.getPrice() * orderItem.getNum());
            orderItem.setMoney(orderItem.getPrice() * orderItem.getNum());
        } else {
            //不存在,则初次购买
            orderItem = this.setOrderItem(skuId, num);
        }
        //redis里进行缓存
        redisTemplate.boundHashOps("Cart_" + username).put(skuId, orderItem);

    }

    /**
     * 查询购物车列表
     * @param username
     * @return
     */
    @Override
    public Map<String,Object> cartList(String username) {
        Integer num = 0;
        Integer payMoney = 0;
        List<OrderItem> orderList = redisTemplate.boundHashOps("Cart_" + username).values();
        for (OrderItem orderItem : orderList) {
            num += orderItem.getNum();
            payMoney += orderItem.getPayMoney();
        }
        Map<String,Object> map = new HashMap<>();
        map.put("orderItems",orderList);
        map.put("totalNum",num);
        map.put("totalPrice",payMoney);
        return map;
    }

    @Override
    public void delete(String username, String skuId) {
        //直接删除redis中的数据
        redisTemplate.boundHashOps("Cart_"+username).delete(skuId);
    }

    /**
     * 第一次购买
     * @param skuId
     * @param num
     * @return
     */
    private OrderItem setOrderItem(String skuId, Integer num) {

        //设置购物车信息
        Sku sku = skuFeign.findById(skuId).getData();
        //根据spuId查询spu
        String spuId = sku.getSpuId();
        Spu spu = spuFeign.findSpuById(spuId).getData();


        //spu设置
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setCategoryId1(spu.getCategory1Id());
        orderItem1.setCategoryId2(spu.getCategory2Id());
        orderItem1.setCategoryId3(spu.getCategory3Id());


        //sku设置
        orderItem1.setSkuId(skuId);
        orderItem1.setSpuId(spuId);
        orderItem1.setName(sku.getName());
        orderItem1.setPrice(sku.getPrice());
        orderItem1.setNum(num);
        orderItem1.setMoney(num * orderItem1.getPrice());
        orderItem1.setPayMoney(num * orderItem1.getPrice());
        orderItem1.setImage(sku.getImage());
        orderItem1.setWeight(num * sku.getWeight());

        return orderItem1;

    }
}
