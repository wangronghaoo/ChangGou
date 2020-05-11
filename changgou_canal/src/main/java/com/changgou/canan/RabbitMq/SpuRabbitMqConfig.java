package com.changgou.canan.RabbitMq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

public class SpuRabbitMqConfig {

    //使用路由模式,商品的上架与下架
    public static final String SPU_UPANDDOWN_EXCHANGE = "spu_upAnddown_exchange";

    //商品上下级
    public static final String SPU_UP_QUEUE = "spu_up_queue";
    public static final String SPU_DOWN_QUEUE = "spu_down_queue";

    //静态页面生成
    public static final String CREATE_PAGE_EXCHANGE = "create_page_exchange";
    public static final String CREATE_PAGE_QUEUE = "create_page_queue";


    //声明交换机
    @Bean("spu_upanddown_exchange")
    public Exchange spu_upanddown_exchange(){
        return ExchangeBuilder.directExchange(SPU_UPANDDOWN_EXCHANGE).durable(true).build();
    }


    //声明上架队列
    @Bean("spu_up_queue")
    public Queue spu_up_queue(){
        return QueueBuilder.durable(SPU_UP_QUEUE).build();
    }
    //声明下架队列
    @Bean("spu_down_queue")
    public Queue spu_down_queue(){
        return QueueBuilder.durable(SPU_DOWN_QUEUE).build();
    }

    //声明交换机
    @Bean("create_page_exchange")
    public Exchange create_page_exchange(){
        return ExchangeBuilder.directExchange(CREATE_PAGE_EXCHANGE).durable(true).build();
    }
    //生成静态页面队列
    @Bean("create_page_queue")
    public Queue create_page_queue(){
        return QueueBuilder.durable(CREATE_PAGE_QUEUE).build();
    }

    //交换机与上架队列绑定 : 更加名称注入  ,指定路由key
    @Bean
    public Binding binding1(@Qualifier("spu_upanddown_exchange") Exchange spu_upanddown_exchange ,
                           @Qualifier("spu_up_queue") Queue spu_up_queue){

        return BindingBuilder.bind(spu_up_queue).to(spu_upanddown_exchange).with("spu_up_routekey").noargs();
    }

    //交换机与下架队列绑定
    @Bean
    public Binding binding2(@Qualifier("spu_upanddown_exchange") Exchange spu_upanddown_exchange ,
                           @Qualifier("spu_down_queue") Queue spu_down_queue){
        return BindingBuilder.bind(spu_down_queue).to(spu_upanddown_exchange).with("spu_down_routekey").noargs();

    }


    //交换机与静态生成页面绑定
    @Bean
    public Binding binding3(@Qualifier("create_page_exchange") Exchange create_page_exchange ,
                            @Qualifier("create_page_queue") Queue create_page_queue){
        return BindingBuilder.bind(create_page_queue).to(create_page_exchange).with("create_page_routekey").noargs();

    }
}
