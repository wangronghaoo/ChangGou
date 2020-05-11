package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UpdateOrderStatusListener {


    @Autowired
    private OrderService orderService;


    @RabbitListener(queues="UpdateOrderQueue")
    public void receiveMessage(String message){
        System.out.println("修改订单状态的数据" + message);
        Map map = JSON.parseObject(message, Map.class);
        if (map == null){
            return;
        }

        String orderId = (String) map.get("orderId");
        String transactionId = (String) map.get("transactionId");
        orderService.updateOrderStatus(orderId,transactionId);
    }
}
