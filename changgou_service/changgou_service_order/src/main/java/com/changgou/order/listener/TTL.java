package com.changgou.order.listener;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.OrderService;
import com.changgou.pay.feign.PayFeign;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Component
public class TTL {

    @Autowired
    public OrderService orderService;


    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayFeign payFeign;


    @Autowired
    private SkuFeign skuFeign;


    @RabbitListener(queues = "queue.ttl")
    public void receiveDeadMessage(String deadOrderId){

        System.out.println("超时订单为:" + deadOrderId);

        //根据id查询该订单
        Order order = orderService.findById(deadOrderId);

        //已经进行支付
        if (order != null && "1".equals(order.getPayStatus())){
            return;
        }

        //可能未支付,但是微信没及时通知
        //调用微信查询订单api
        Map resultMap = payFeign.queryOrder(deadOrderId);
        //已经支付
        if (resultMap != null && "SUCCESS".equals(resultMap.get("trade_state"))){
            //修改订单的状态
            orderService.updateOrderStatus(deadOrderId, String.valueOf(resultMap.get("transaction_id")));
        }

        //未支付,则关闭订单
        if ("NOTPAY".equals(resultMap.get("trade_state"))){
            //关闭订单返回结果
            payFeign.closeOrder(deadOrderId);
            //修改数据库订单状态为4,以及插入日志
            orderService.updateOrder(deadOrderId);
            //库存回退
            //根据订单id查询订单项
            Example example = new Example(OrderItem.class);
            example.createCriteria().andEqualTo("orderId",deadOrderId);
            List<OrderItem> orderItemList = orderItemMapper.selectByExample(example);
            //根据订单项的skuId，进行库存回退
            for (OrderItem orderItem : orderItemList) {
                skuFeign.addNum(orderItem.getSkuId(),orderItem.getNum());
            }
        }

    }
}
