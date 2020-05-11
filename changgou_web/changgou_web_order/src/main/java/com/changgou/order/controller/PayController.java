package com.changgou.order.controller;

import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.pay.feign.PayFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/wwxpay")
public class PayController {

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private PayFeign payFeign;

    @RequestMapping("/nativePay")
    public String wxPay(@RequestParam String orderId, Model model){
        //远程调用payFeign,进行微信支付接口调用
        //订单不存在,返回fail
        Order order = orderFeign.findOrderById(orderId).getData();
        if (order == null){
            return "fail";
        }

        //如果是已支付
        if (!"0".equals(order.getPayStatus())){
            return "fail";
        }

        //调用微信接口失败
        Map resultMap = (Map) payFeign.nativePay(orderId, order.getTotalMoney()).getData();
        if (resultMap == null){
            return "fail";   //调用支付接口失败
        }

        //返回前端参数
        resultMap.put("orderId",orderId);
        resultMap.put("payMoney",order.getTotalMoney());
        model.addAllAttributes(resultMap);
        return "weixinpay";
    }

    @RequestMapping("/topaysuccess")
    public String toPaySuccess(Model model,String payMoney){
        model.addAttribute("payMoney",payMoney);
        return "paysuccess";
    }
}
