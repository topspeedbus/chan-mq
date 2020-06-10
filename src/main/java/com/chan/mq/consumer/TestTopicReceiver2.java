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
        value = @Queue(value = "queue_test2", durable = "true"),
        exchange = @Exchange(name = "test.topic.ex", type = "topic", ignoreDeclarationExceptions = "true"),
        key = "topic.test.*"), ackMode = "MANUAL")
public class TestTopicReceiver2 {
    @RabbitHandler
    public void onTestMsg(@Payload String message, @Headers Map<String, Object> headers, Channel channel) {
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            System.out.println("test2收到消息，开始消费--------------> " + message);
            Object o = headers.get(AmqpHeaders.CORRELATION_ID);
            Object hello = headers.get("hello");
            System.out.println(hello);
            System.out.println(o);
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
