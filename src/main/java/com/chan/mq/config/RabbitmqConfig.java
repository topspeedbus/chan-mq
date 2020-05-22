package com.chan.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: chenjie23333
 * @create: -
 * @description:
 **/
@Configuration
public class RabbitmqConfig {


    /**
     * 声明交换机，任意类型
     */
    @Bean
    public DirectExchange deadLetterEx() {
        return new DirectExchange("test_dead_ex", true, false);
    }
    /**
     * 声明一个死信队列. x-dead-letter-exchange 对应 死信交换机 x-dead-letter-routing-key 对应
     * 死信队列
     */
    @Bean("deadLetterQueue")
    public Queue deadLetterQueue() {
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange 声明 死信交换机
        args.put("x-dead-letter-exchange", "test_dead_ex");
        // x-dead-letter-routing-key 声明 死信路由键
        args.put("x-dead-letter-routing-key", "test_dead_redirect");
        return QueueBuilder.durable("test_dead_queque").withArguments(args).build();
    }



    /**
     * 定义死信队列转发队列.
     *
     * @return the queue
     */
    @Bean("redirectQueue")
    public Queue redirectQueue() {
        return QueueBuilder.durable("REDIRECT_QUEUE").build();
    }

    /**
     * 死信路由通过 DL_KEY 绑定键绑定到死信队列上.
     *
     * @return the binding
     */
    @Bean
    public Binding deadLetterBinding() {
        return new Binding("test_dead_queque", Binding.DestinationType.QUEUE, "test_dead_ex", "test_dead_routing_key", null);
    }

    /**
     * 死信路由通过 KEY_R 绑定键绑定到死信队列上.
     *
     * @return the binding
     */
    @Bean
    public Binding redirectBinding() {
        return new Binding("REDIRECT_QUEUE", Binding.DestinationType.QUEUE, "test_dead_ex", "test_dead_redirect", null);
    }
}
