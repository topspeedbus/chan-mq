package com.chan.mq.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.annotation.Resource;

/**
 * @author: chen
 * @date: 2020/5/11 - 20:40
 * @describe:
 */
public class TopicProducer {
    @Resource
    RabbitTemplate rabbitTemplate;

    public void sendTopicMsg() {
        String diyMsg = "";
        rabbitTemplate.convertAndSend("test.topic.ex", "topic.test.demo", diyMsg);
    }
}
