package com.changgou.canan.RabbitMq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;

public class AdRabbitMqConfig {

    /**
     * 使用简单工作方式
     * 没有交换机
     * 一个队列
     * 广告更新队列
     */
    public static final String AD_UPDATE_QUEUE = "ad_update_queue";



    //声明队列
    @Bean
    public Queue AD_UPDATE_QUEUE(){
        return QueueBuilder.durable(AD_UPDATE_QUEUE).build();
    }
}
