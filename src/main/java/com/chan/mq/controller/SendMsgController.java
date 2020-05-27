package com.chan.mq.controller;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author: chen
 * @date: 2020/5/11 - 20:42
 * @describe:
 */
@RestController
@RequestMapping("test/send")
public class SendMsgController {
    @Resource
    RabbitTemplate rabbitTemplate;

    @GetMapping("")
    public void test(@RequestParam(value = "message") String message) {
        rabbitTemplate.convertAndSend("test.topic.ex", "topic.test.demo", message);
    }

    @GetMapping("v1")
    public void test1(@RequestParam(value = "message") String message) {

        /**
         * cap:consistency, availability, partition tolerance
         * 为什么要放弃P： p就相当于基础服务的运行情况，如果不保证P 意味着p随时崩掉
         * 如果没有基础服务，那c a 也无法存在
         *
         *
         * base: basically available soft state eventual consistency
         */
        MessagePostProcessor messagePostProcessor = msg -> {
            MessageProperties messageProperties = msg.getMessageProperties();
            // 设置编码
            messageProperties.setContentEncoding("utf-8");
            // 设置过期时间10*1000毫秒
            messageProperties.setExpiration("5000");
            //消息定位：实现幂等
            messageProperties.setCorrelationId("");
            messageProperties.getCorrelationId();
            //持久化三点：交换机，队列，消息。调用：fsync指令非实时，消息无法完全做到不流失。设置消息持久化：注意影响性能
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return msg;
        };        rabbitTemplate.convertAndSend("test_dead_ex", "test_dead_routing_key", message + "------>" + new Date(), messagePostProcessor);
    }
}
