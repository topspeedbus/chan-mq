package com.chan.mq.consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
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
//@Component
/**
 * 放在类级别，可指定多个 @RabbitHandler,根据传入参数的不同，重载出多个方法
 */
@RabbitListener(bindings = @QueueBinding(
        /**
         * 这里声明的queue如果没有会自动创建
        */
        value = @Queue(value = "queue_test1", durable = "true"),
        exchange = @Exchange(name = "test.topic.ex", type = "topic"),
        key = "topic.test.#"), ackMode = "MANUAL")
public class TestTopicReceiver1 {
    @RabbitHandler
    public void onTestMsg(@Payload String message, @Headers Map<String, Object> headers, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) Long tag) {
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            System.out.println("test1收到消息，开始消费--------------> " + message);
            System.out.println("tag: " + tag);

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
