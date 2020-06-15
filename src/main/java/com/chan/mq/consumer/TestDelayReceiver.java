package com.chan.mq.consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: chen
 * @date: 2020/6/13 - 17:20
 * @describe:
 */
@Component
public class TestDelayReceiver {
    private static volatile int I = 0;
    @RabbitListener(queues = "delay_queue", ackMode = "MANUAL")

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "delay_queue"),
//            exchange = @Exchange(value = "delay_exchange"),
//            key = "delay_key"),
//            ackMode = "MANUAL")
    @RabbitHandler
    public void onTestMsg(@Payload String message, @Headers Map<String, Object> headers, Channel channel) {
            ++ I;
        System.out.println("last===:" + I);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            System.out.println("现在时间2222： " + new Date());
            System.out.println("test2222收到消息，开始消费--------------> " + message);
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
