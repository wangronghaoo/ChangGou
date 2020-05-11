package com.changgou.pay.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "pay")
public interface PayFeign {

    @RequestMapping("/wxpay/nativePay")
    Result nativePay(@RequestParam String orderId, @RequestParam Integer payMoney);


    @GetMapping("/wxpay/queryOrder")
    Map queryOrder(@RequestParam String orderId);


    @RequestMapping("/wxpay/closeOrder")
    Map closeOrder(@RequestParam String out_trad_no);
}
