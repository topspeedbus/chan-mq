package com.chan.mq.controller;

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
        MessagePostProcessor messagePostProcessor = msg -> {
            MessageProperties messageProperties = msg.getMessageProperties();
            // 设置编码
            messageProperties.setContentEncoding("utf-8");
            // 设置过期时间10*1000毫秒
            messageProperties.setExpiration("5000");
            return msg;
        };
        rabbitTemplate.convertAndSend("test_dead_ex", "test_dead_routing_key", message + "------>" + new Date(), messagePostProcessor);
    }
}
