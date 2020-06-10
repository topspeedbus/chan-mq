package com.chan.mq;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class MqApplication {

    public static void main(String[] args) {
        SpringApplication.run(MqApplication.class, args);
    }

//    @Bean
//    @Scope("prototype")
//    public RabbitTemplate rabbitTemplate() {
//        System.out.println("=================MqApplication create a new rabbitmq==================");
//        return new RabbitTemplate();
//    }
}
