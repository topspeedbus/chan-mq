package com.chan.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.amqp.rabbit.core.DeclareExchangeConnectionListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: chenjie23333
 * @create: -
 * @description:
 **/
@Configuration
@EnableRabbit
public class RabbitmqConfig {

    @Value("${spring.rabbitmq.host}")
    private String addresses;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.virtual-host}")
    private String virtualHost;

    @Value("${spring.rabbitmq.port}")
    private Integer port;

    @Bean
    public ConnectionFactory connectionFactory() {
        /**
         * 生产者和消费者使用同一个cachingConnectionFactory阻塞一个，另一个也无法连接broker
         */
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setAddresses(addresses);
        cachingConnectionFactory.setUsername(username);
        cachingConnectionFactory.setPassword(password);
        cachingConnectionFactory.setVirtualHost(virtualHost);
        cachingConnectionFactory.setPort(port);
//        cachingConnectionFactory.setCacheMode();
        // 如果消息要设置成回调，则以下的配置必须要设置成true

//        ConnectionListener connectionListener = new DirectRabbitListenerContainerFactory();

        /**
         * 限制允许连接总数
         */
//        cachingConnectionFactory.setConnectionLimit(20);
        cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        cachingConnectionFactory.setPublisherReturns(true);
//        cachingConnectionFactory.setChannelCacheSize(20);
//        cachingConnectionFactory.setConnectionListeners(null);
        return cachingConnectionFactory;
    }

    @Bean(name = "rabbitTemplate")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(true);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }


    /**
     * 配置rabbit-admin
     * @param connectionFactory
     * @return
     */
//    @Bean
//    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
//    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
//        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
//
//        return rabbitAdmin;
//    }


    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setPrefetchCount(1);
        /**
         * 容器启动就会创建这个数量的线程监听该队列，可设置taskExecutor 设置线程池策略
         * io密集型可多设置
         */
        factory.setConcurrentConsumers(5);
        return factory;
    }

    @Bean
    public DirectRabbitListenerContainerFactory directRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setPrefetchCount(1);
        factory.setConsumersPerQueue(5);
        return factory;
    }

    @Value("${chan.mq.queue2}")
    private String deadLetterQueue;

    @Value("${chan.key2}")
    private String deadLetterRoutingKey;

    @Value("${chan.key1}")
    private String surviveRoutingKey;

    @Value("${chan.x-dead.ex2}")
    private String deadLetterEx;

    @Value("${chan.x-dead.ex1}")
    private String surviveDeadLetterEx;

    /**
     * 声明交换机，任意类型
     */
    @Bean
    public DirectExchange deadLetterEx() {
        return new DirectExchange(deadLetterEx, true, false);
    }

    /**
     * 声明一个死信队列. x-dead-letter-exchange 对应 死信交换机 x-dead-letter-routing-key 对应
     * 死信队列
     */

    @Bean("deadLetterQueue")
    public Queue deadLetterQueue() {
//        Map<String, Object> args = new HashMap<>(2);
//        // x-dead-letter-exchange 声明 死信交换机
//        args.put("x-dead-letter-exchange", "test_dead_ex");
//        // x-dead-letter-routing-key 声明 死信路由键
//        args.put("x-dead-letter-routing-key", "test_dead_redirect");
//        args.put("x-dead-letter-routing-key", "test_dead_redirect");
        /**
         * durable 持久化：该队列创建一次，如果要改动ttl，必须手动删除队列
         */
        return QueueBuilder.durable(deadLetterQueue).autoDelete().deadLetterExchange(surviveDeadLetterEx).deadLetterRoutingKey(surviveRoutingKey).build();
    }

    /**
     * 定义死信队列转发队列.
     *
     * @return the queue
     */
//    @Bean("redirectQueue")
//    public Queue redirectQueue() {
//        return QueueBuilder.durable("REDIRECT_QUEUE").build();
//    }

    /**
     * 死信路由通过 DL_KEY 绑定键绑定到死信队列上.
     *
     * @return the binding
     */
    @Bean
    public Binding deadLetterBinding() {
//        return new Binding("test_dead_queque", Binding.DestinationType.QUEUE, "test_dead_ex", "test_dead_routing_key", null);
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterEx()).with(deadLetterRoutingKey);
    }

//    public CustomExchange customExchange

    /**
     * 死信路由通过 KEY_R 绑定键绑定到死信队列上.
     *
     * @return the binding
     */
//    @Bean
//    public Binding redirectBinding() {
////        return new Binding("REDIRECT_QUEUE", Binding.DestinationType.QUEUE, "test_dead_ex", "test_dead_redirect", null);
//        return BindingBuilder.bind(redirectQueue()).to(deadLetterEx()).with("test_dead_redirect");
//    }


    @Bean
    public CustomExchange delayExchange(){
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange("delay_exchange","x-delayed-message",true, false, args);
    }

    /**
     * 延时队列
     * @return
     */
    @Bean
    public Queue delayQueue(){
        return new Queue("delay_queue",true);
    }

    /**
     * 给延时队列绑定交换机
     * @return
     */
    @Bean
    public Binding myDelayBinding(){
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with("delay_key").noargs();
    }
}
