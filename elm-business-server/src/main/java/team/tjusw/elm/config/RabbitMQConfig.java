package team.tjusw.elm.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // 队列名称
    public static final String ORDER_STATUS_QUEUE = "order.status.queue";
    // 交换机名称
    public static final String ORDER_EXCHANGE = "order.exchange";
    // 路由键
    public static final String ORDER_STATUS_ROUTING_KEY = "order.status.change";

    // 创建队列
    @Bean
    public Queue orderStatusQueue() {
        return new Queue(ORDER_STATUS_QUEUE, true);
    }

    // 创建交换机
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    // 绑定队列和交换机
    @Bean
    public Binding orderStatusBinding() {
        return BindingBuilder.bind(orderStatusQueue()).to(orderExchange()).with(ORDER_STATUS_ROUTING_KEY);
    }
}
