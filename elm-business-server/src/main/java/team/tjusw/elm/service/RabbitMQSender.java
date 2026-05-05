package team.tjusw.elm.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import team.tjusw.elm.config.RabbitMQConfig;
import team.tjusw.elm.po.Order;

@Service
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 发送订单状态变更消息
    public void sendOrderStatusChangeMessage(Order order) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_STATUS_ROUTING_KEY,
                order
        );
    }
}
