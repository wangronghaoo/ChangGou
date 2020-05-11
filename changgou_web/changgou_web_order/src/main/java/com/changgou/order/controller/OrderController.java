package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.user.feign.AddressFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@Controller
@RequestMapping("/worder")
public class OrderController {


    /**
     * 跳转页面
     * @return
     */
    @RequestMapping("/order")
    public String order(){
        return "order";
    }


    @Autowired
    private AddressFeign addressFeign;


    /**
     * 寻找地址联系人
     * @return
     */
    @GetMapping("/findAddress")
    @ResponseBody
    public Result findAddress(){
        Result<Map> address = addressFeign.findAddress();
        return address;
    }

    @Autowired
    private OrderFeign orderFeign;


    /**
     * 提交订单数据
     * @param map
     * @return
     */
    @PostMapping("/saveOrder")
    @ResponseBody
    public Result saveOrder(@RequestParam Map map){
        Order order = new Order();
        order.setReceiverAddress((String) map.get("receiverAddress"));
        order.setReceiverMobile((String) map.get("receiverMobile"));
        order.setReceiverContact((String) map.get("receiverContact"));
        order.setPayType((String) map.get("payType"));
        Result result = orderFeign.saveOrder(order);
        return result;
    }


    /**
     * 跳转支付页面
     */
    @RequestMapping("/toPayPage")
    public String toPayPage(String orderId,Model model){
        Order order = orderFeign.findOrderById(orderId).getData();
        model.addAttribute("orderId",order.getId());
        model.addAttribute("payMoney",order.getTotalMoney());
        return "pay";
    }







}
