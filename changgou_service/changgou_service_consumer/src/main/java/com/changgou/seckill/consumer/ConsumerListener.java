package com.changgou.seckill.consumer;


import com.alibaba.fastjson.JSON;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ConsumerListener {

    @Autowired
    private SeckillOrderService orderService;


    @RabbitListener(queues = "addSeckillGoodsOrderQueue")
    public void receiveMessage(Message message, Channel channel){

        //有可能因为过多的消息堆积从而导致消费者宕机的情况
        //设置预抓取总数,设置处理消息总数
        try {
            channel.basicQos(300);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将消息装为秒杀订单
        SeckillOrder seckillOrder = JSON.parseObject(message.getBody(), SeckillOrder.class);

        boolean result = orderService.createSeckillOrder(seckillOrder);

        //如果执行成功,则返回成功通知
        if (result){
            try {
                //第一个参数:消息的唯一表示
                //第二个参数:false 只对当前的消息进行应答  true 对消费者接收的所有消息进行应答
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            try {
                //第一个boolean true:所有的消费者都会拒绝这个消息 false: 只拒绝当前消息
                //第二个 true : 进入到死信队列 , false : 返回到原来的队列中
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
