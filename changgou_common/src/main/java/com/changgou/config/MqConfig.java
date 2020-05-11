package com.changgou.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    public static final String AddPointExchange = "AddPointExchange";   //添加积分交换机

    public static final String AddPointQueue = "AddPointQueue";      //添加积分队列

    public static final String FinishAddPointQueue = "FinishAddPointQueue";   //完成积分队列

    public static final String AddPointRouteKey = "AddPointRouteKey";

    public static final String FinishAddPointRouteKey = "FinishAddPointRouteKey";


    //交换机
    @Bean(name = "addpointexchange")
    public Exchange AddPointExchange(){
        return ExchangeBuilder.directExchange(AddPointExchange).durable(true).build();
    }

    //添加积分队列
    @Bean(name = "addpointqueue")
    public Queue AddPointQueue(){
        return QueueBuilder.durable(AddPointQueue).build();
    }

    //完成添加积分队列
    @Bean(name = "finishaddpointqueue")
    public Queue FinishAddPointQueue(){
        return QueueBuilder.durable(FinishAddPointQueue).build();
    }


    //绑定交换机与添加积分队列
    @Bean
    public Binding PointExchangeWithPointQueue(@Qualifier(value = "addpointexchange") Exchange addpointexchange ,@Qualifier(value = "addpointqueue") Queue addpointqueue ){
        return BindingBuilder.bind(addpointqueue).to(addpointexchange).with(AddPointRouteKey).noargs();
    }


    //绑定交换机与完成添加积分队列绑定
    @Bean
    public Binding PointExchangeWithFinishPointQueue(@Qualifier(value = "addpointexchange") Exchange addpointexchange,@Qualifier(value = "finishaddpointqueue") Queue finishaddpointqueue){
        return BindingBuilder.bind(finishaddpointqueue).to(addpointexchange).with(FinishAddPointRouteKey).noargs();
    }


}
