package com.changgou.seckill.controller;


import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createSeckillOrder")
    public Result createSeckillOrder(SeckillOrder seckillOrder) {
        boolean result = seckillOrderService.createSeckillOrder(seckillOrder);

        if (result) {
            return new Result(true, StatusCode.OK, "创造秒杀订单成功", result);
        }

        return new Result(true, StatusCode.OK, "创造秒杀订单失败",result );
    }
}
