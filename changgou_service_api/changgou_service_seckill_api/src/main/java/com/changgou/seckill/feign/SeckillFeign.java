package com.changgou.seckill.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "secKill")
public interface SeckillFeign {


    @RequestMapping("/secKill/List")
    Result findList(@RequestParam String nowTime);

    @RequestMapping("/secKill/addOrder")
    Result addOrder(@RequestParam String time,@RequestParam Long id);
}
