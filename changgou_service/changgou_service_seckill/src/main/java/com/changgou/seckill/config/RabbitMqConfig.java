package com.changgou.seckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {

    public static final String addSeckillGoodsOrderQueue = "addSeckillGoodsOrderQueue";


    //开启队列持久化
    @Bean
    public Queue queue(){
        return new Queue(addSeckillGoodsOrderQueue,true);
    }

}
