package com.chan.mq.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author: chen
 * @date: 2020/6/13 - 18:01
 * @describe:
 */
@RestController
@Slf4j
@RequestMapping("test/send")
public class SendDelayMsgPlugsController {
    @Resource
    private RabbitTemplate rabbitTemplate;

    final RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
        if (ack) {
            System.out.println("消息发送成功3" + correlationData);
            System.out.println("消息发送成功3" + cause);
            log.debug("消息发送成功3：{}", correlationData);
            log.debug("消息发送成功3：{}", cause);
        } else {
            System.out.println("消息发送失败3" + correlationData);
            System.out.println("消息发送失败3" + cause);
            log.error("消息发送失败3:{}", cause);
        }
    };

    final RabbitTemplate.ReturnCallback returnCallback = (message, replyCode, replyText, exchange, routingKey) -> {

        System.out.println("ReturnCallback3:     " + "消息：" + message);
        System.out.println("ReturnCallback3:     " + "回应码：" + replyCode);
        System.out.println("ReturnCallback3:     " + "回应信息：" + replyText);
        System.out.println("ReturnCallback3:     " + "交换机：" + exchange);
        System.out.println("ReturnCallback3:     " + "路由键：" + routingKey);
        log.error("消息：{}， 应答码：{}， 原因：{}， 交换器：{}， 路由键：{}",
                new String(message.getBody()),
                replyCode,
                replyText,
                exchange,
                routingKey);
    };



    /**
     * 插件版
     * @param message
     * @param ttl
     */
    @GetMapping("v2")
    public void test1(@RequestParam(value = "message") String message,
                      @RequestParam Integer ttl) {

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
//            messageProperties.setExpiration(ttl);
            //消息定位：实现幂等
            messageProperties.setCorrelationId("243987701");
            //持久化三点：交换机，队列，消息。调用：fsync指令非实时，消息无法完全做到不流失。设置消息持久化：注意影响性能
//            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setHeader("hello", "1111111111");

            /**
             * 需要安装插件 rabbitmq_delayed_message_exchange
             */
            messageProperties.setDelay(ttl);
            return msg;
        };

        rabbitTemplate.setReturnCallback(returnCallback);
        rabbitTemplate.setConfirmCallback(confirmCallback);

        for (int i = 0; i < 100000; i++) {
            System.out.println(i);
            rabbitTemplate.convertAndSend("delay_exchange", "delay_key", "计数===" + i +"=======" + message + "------>" + new Date(), messagePostProcessor);
        }
//        rabbitTemplate.convertAndSend("test_dead_ex", "test_dead_routing_key", message + "------>" + new Date(), messagePostProcessor);
//        rabbitTemplate.convertAndSend(RabbitMQConstant.DEFAULT_EXCHANGE, RabbitMQConstant.DEFAULT_KEY,
//                message + "------>" + new Date(), messagePostProcessor);
    }


    @Value("${spring.profiles.active}")
    private String profile;
    @GetMapping("v3")
    public void test2(@RequestParam(value = "message") String message,
                      @RequestParam Integer ttl) {
        System.out.println(profile);
    }
}
