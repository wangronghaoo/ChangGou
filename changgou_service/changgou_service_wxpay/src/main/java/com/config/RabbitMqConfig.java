package com.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String UpdateOrderQueue = "UpdateOrderQueue";

    @Bean
    public Queue UpdateOrderQueue(){
        return  QueueBuilder.durable(UpdateOrderQueue).build();
    }
}
