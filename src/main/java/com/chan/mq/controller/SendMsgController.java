package com.chan.mq.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;

/**
 * @author: chen
 * @date: 2020/5/11 - 20:42
 * @describe:
 */
@RestController
@Slf4j
@RequestMapping("test/send")
public class SendMsgController {
    @Resource
    RabbitTemplate rabbitTemplate;


    final RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
        if (ack) {
            System.out.println("消息发送成功1111" + correlationData);
            System.out.println("消息发送成功111" + cause);
            log.debug("消息发送成功11：{}", correlationData);
            log.debug("消息发送成功1：{}", cause);
        } else {
            System.out.println("消息发送失败1" + correlationData);
            System.out.println("消息发送失败1" + cause);
            log.error("消息发送失败1:{}", cause);
        }
    };

   final RabbitTemplate.ReturnCallback returnCallback = (message, replyCode, replyText, exchange, routingKey) -> {

       System.out.println("ReturnCallback1:     " + "消息：" + message);
       System.out.println("ReturnCallback1:     " + "回应码：" + replyCode);
       System.out.println("ReturnCallback1:     " + "回应信息：" + replyText);
       System.out.println("ReturnCallback1:     " + "交换机：" + exchange);
       System.out.println("ReturnCallback1:     " + "路由键：" + routingKey);
       log.error("消息：{}， 应答码：{}， 原因：{}， 交换器：{}， 路由键：{}",
               new String(message.getBody()),
               replyCode,
               replyText,
               exchange,
               routingKey);
   };


    @GetMapping("")
    public void test(@RequestParam(value = "message") String message) {
        MessagePostProcessor messagePostProcessor = msg -> {
            MessageProperties messageProperties = msg.getMessageProperties();
            // 设置编码
            messageProperties.setContentEncoding("utf-8");
            // 设置过期时间10*1000毫秒
//            messageProperties.setExpiration("5000");
            //消息定位：实现幂等
            messageProperties.setCorrelationId("243987701");
            //持久化三点：交换机，队列，消息。调用：fsync指令非实时，消息无法完全做到不流失。设置消息持久化：注意影响性能
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setHeader("hello", "1111111111");
            return msg;
        };


        rabbitTemplate.setReturnCallback(returnCallback);
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.convertAndSend("test.topic.ex", "topic.test.demo", message, messagePostProcessor);
    }

}
