package com.chan.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author: chen
 * @date: 2020/5/7 - 11:51
 * @describe:
 */
@Component
@Slf4j
public class ProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 容器启动后把该配置内置到模板里面
     */
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            System.out.println("消息发送成功" +  correlationData);
            log.debug("消息发送成功：{}", correlationData);
        } else {
            System.out.println("消息发送失败" + cause);
            log.error("消息发送失败:{}", cause);
        }
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("ReturnCallback:     "+"消息："+message);
        System.out.println("ReturnCallback:     "+"回应码："+replyCode);
        System.out.println("ReturnCallback:     "+"回应信息："+replyText);
        System.out.println("ReturnCallback:     "+"交换机："+exchange);
        System.out.println("ReturnCallback:     "+"路由键："+routingKey);
        log.error("消息：{}， msgStr:{}, 应答码：{}， 原因：{}， 交换器：{}， 路由键：{}",
                new String(message.getBody()),
                replyCode,
                replyText,
                exchange,
                routingKey);
    }
}
