package com.changgou.order.feign;

import com.changgou.entity.Result;
import com.changgou.order.pojo.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "order")
public interface OrderFeign {

    /**
     * 添加
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/cart/addCart")
    Result addCart(@RequestParam String skuId, @RequestParam Integer num);


    /**
     * 购物车列表
     * @return
     */
    @GetMapping("/cart/list")
    Result<Map> list();


    /**
     * 删除购物车
     * @param skuId
     * @return
     */
    @RequestMapping("/cart/delete")
    Result delete(@RequestParam String skuId);


    /**
     * 提交购物车数据
     * @param order
     * @return
     */
    @PostMapping("/order/saveOrder")
    Result saveOrder(Order order);



    @PostMapping("/order/findByOrderId")
    Result<Order> findOrderById(@RequestParam String orderId);

}
