package com.chan.mq.consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author: chen
 * @date: 2020/5/11 - 20:49
 * @describe:
 */
@Component
@RabbitListener(bindings = @QueueBinding(
        /**
         * 这里声明的queue如果没有会自动创建
        */
        value = @Queue(value = "queue_test1", durable = "true"),
        exchange = @Exchange(name = "test.topic.ex", type = "topic"),
        key = "topic.test.#"))
public class TestTopicReceiver1 {
    @RabbitHandler
    public void onTestMsg(@Payload String message, @Headers Map<String, Object> headers, Channel channel) {
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            System.out.println("test1收到消息，开始消费--------------> " + message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                channel.basicReject(deliveryTag, false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
