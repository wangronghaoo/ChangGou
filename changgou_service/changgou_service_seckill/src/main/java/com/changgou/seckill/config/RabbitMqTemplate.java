package com.changgou.seckill.config;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class RabbitMqTemplate{

    private final String RabbitMqConfirm = "RabbitMqConfirm";

    private final String CorrelationDataKey = "CorrelationDataKey";

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private RabbitTemplate rabbitTemplate;

    public RabbitMqTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                //是否发送成功
                if (b){
                    //删除redis中的消息
                    redisTemplate.delete(CorrelationDataKey);
                    redisTemplate.delete(RabbitMqConfirm+correlationData.getId());
                }else {
                    //重新发送
                    Map<String,String> message = redisTemplate.opsForHash().entries(RabbitMqConfirm + correlationData.getId());
                    String exchange = message.get("exchange");
                    String routingKey = message.get("routingKey");
                    String message1 = message.get("message");

                    rabbitTemplate.convertAndSend(exchange,routingKey, JSON.toJSONString(message1));
                }
            }
        });
    }


    public void sendMessage(String exchange , String routingKey, String message){

        //携带唯一标识发送消息,并存到redis
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        redisTemplate.boundValueOps(CorrelationDataKey).set(correlationData.getId());

        //发送的数据保存到redis中 ,如果发送失败,可以重新发送
        Map<String,String> map = new HashMap<>();
        map.put("exchange",exchange);
        map.put("routingKey",routingKey);
        map.put("message",message);
        redisTemplate.opsForHash().putAll(RabbitMqConfirm+correlationData.getId(),map);

        //携带唯一标识发送消息
        rabbitTemplate. convertAndSend(exchange,routingKey,message,correlationData);

    }



}
