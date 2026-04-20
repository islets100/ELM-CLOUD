package team.tjusw.elm.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import team.tjusw.elm.config.RabbitMQConfig;
import team.tjusw.elm.po.Order;

@Service
public class RabbitMQListener {

    // 监听订单状态变更消息
    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_QUEUE)
    public void receiveOrderStatusChangeMessage(Order order) {
        System.out.println("Received order status update: " + order.getOrderId());
        System.out.println("Order state: " + order.getOrderState());
        // 这里可以添加处理订单状态变更的业务逻辑
        // 例如：更新购物车状态、发送通知等
    }
}
