package team.tjusw.elm.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import team.tjusw.elm.mapper.OrderMapper;
import team.tjusw.elm.po.Order;
import team.tjusw.elm.po.OrderDetail;
import team.tjusw.elm.service.OrderService;
import team.tjusw.elm.service.RabbitMQSender;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Override
    @Transactional
    public Order createOrder(String userId, Integer businessId, Integer daId, List<OrderDetail> orderDetails) {
        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setBusinessId(businessId);
        order.setDaId(daId);
        
        // 计算订单总金额
        double total = 0;
        for (OrderDetail detail : orderDetails) {
            total += detail.getFoodPrice().doubleValue() * detail.getQuantity();
        }
        order.setOrderTotal(new java.math.BigDecimal(total));
        
        order.setOrderState(0); // 0: 待支付
        order.setOrderTime(new Date());
        
        // 保存订单
        orderMapper.saveOrder(order);
        
        // 保存订单详情
        for (OrderDetail detail : orderDetails) {
            detail.setOrderId(order.getOrderId());
            orderMapper.saveOrderDetail(detail);
        }
        
        // 发送订单创建消息
        rabbitMQSender.sendOrderStatusChangeMessage(order);
        
        return order;
    }

    @Override
    public List<Order> listOrdersByUserId(String userId) {
        return orderMapper.listOrdersByUserId(userId);
    }

    @Override
    public Order getOrderById(Integer orderId) {
        return orderMapper.getOrderById(orderId);
    }

    @Override
    public List<OrderDetail> listOrderDetailByOrderId(Integer orderId) {
        return orderMapper.listOrderDetailByOrderId(orderId);
    }

    @Override
    public List<Order> listOrdersByBusinessId(Integer businessId) {
        return orderMapper.listOrdersByBusinessId(businessId);
    }

    @Override
    public int updateOrderStatus(Integer orderId, Integer orderState) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderState(orderState);
        int result = orderMapper.updateOrderStatus(order);
        
        // 发送订单状态变更消息
        if (result > 0) {
            order = orderMapper.getOrderById(orderId);
            rabbitMQSender.sendOrderStatusChangeMessage(order);
        }
        
        return result;
    }

    @Override
    public int payOrder(Integer orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderState(1); // 1: 已支付
        order.setPayTime(new Date());
        int result = orderMapper.updateOrderPayTime(order);
        
        // 发送订单状态变更消息
        if (result > 0) {
            order = orderMapper.getOrderById(orderId);
            rabbitMQSender.sendOrderStatusChangeMessage(order);
        }
        
        return result;
    }

    @Override
    public int deliverOrder(Integer orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderState(2); // 2: 配送中
        order.setDeliveryTime(new Date());
        int result = orderMapper.updateOrderDeliveryTime(order);
        
        // 发送订单状态变更消息
        if (result > 0) {
            order = orderMapper.getOrderById(orderId);
            rabbitMQSender.sendOrderStatusChangeMessage(order);
        }
        
        return result;
    }

    @Override
    public int checkoutOrder(Integer orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderState(4); // 4: 已确认收货
        order.setCheckoutTime(new Date());
        int result = orderMapper.updateOrderCheckoutTime(order);
        
        // 发送订单状态变更消息
        if (result > 0) {
            order = orderMapper.getOrderById(orderId);
            rabbitMQSender.sendOrderStatusChangeMessage(order);
        }
        
        return result;
    }
}
