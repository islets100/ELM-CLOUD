package team.tjusw.elm.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team.tjusw.elm.mapper.OrderMapper;
import team.tjusw.elm.po.Order;
import team.tjusw.elm.po.OrderDetail;
import team.tjusw.elm.service.RabbitMQSender;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private RabbitMQSender rabbitMQSender;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private List<OrderDetail> orderDetails;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        order = new Order();
        order.setOrderId(1);
        order.setUserId("user1");
        order.setBusinessId(1);
        order.setDaId(1);
        order.setOrderTotal(new java.math.BigDecimal(100.0));
        order.setOrderState(0);
        order.setOrderTime(new Date());

        orderDetails = new ArrayList<>();
        OrderDetail detail1 = new OrderDetail();
        detail1.setFoodId(1);
        detail1.setQuantity(2);
        detail1.setFoodPrice(new java.math.BigDecimal(50.0));
        orderDetails.add(detail1);
    }

    @Test
    void testCreateOrder() {
        // 模拟 orderMapper.saveOrder 方法
        when(orderMapper.saveOrder(any(Order.class))).thenReturn(1);
        // 模拟 orderMapper.saveOrderDetail 方法
        when(orderMapper.saveOrderDetail(any(OrderDetail.class))).thenReturn(1);

        // 执行测试方法
        Order result = orderService.createOrder("user1", 1, 1, orderDetails);

        // 验证结果
        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals(1, result.getBusinessId());
        assertEquals(1, result.getDaId());
        assertEquals(new java.math.BigDecimal(100.0), result.getOrderTotal());
        assertEquals(0, result.getOrderState());
        assertNotNull(result.getOrderTime());

        // 验证方法调用
        verify(orderMapper, times(1)).saveOrder(any(Order.class));
        verify(orderMapper, times(orderDetails.size())).saveOrderDetail(any(OrderDetail.class));
        verify(rabbitMQSender, times(1)).sendOrderStatusChangeMessage(any(Order.class));
    }

    @Test
    void testListOrdersByUserId() {
        // 模拟返回数据
        List<Order> expectedOrders = new ArrayList<>();
        expectedOrders.add(order);
        when(orderMapper.listOrdersByUserId("user1")).thenReturn(expectedOrders);

        // 执行测试方法
        List<Order> result = orderService.listOrdersByUserId("user1");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(order.getOrderId(), result.get(0).getOrderId());

        // 验证方法调用
        verify(orderMapper, times(1)).listOrdersByUserId("user1");
    }

    @Test
    void testGetOrderById() {
        // 模拟返回数据
        when(orderMapper.getOrderById(1)).thenReturn(order);

        // 执行测试方法
        Order result = orderService.getOrderById(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(order.getOrderId(), result.getOrderId());

        // 验证方法调用
        verify(orderMapper, times(1)).getOrderById(1);
    }

    @Test
    void testListOrderDetailByOrderId() {
        // 模拟返回数据
        when(orderMapper.listOrderDetailByOrderId(1)).thenReturn(orderDetails);

        // 执行测试方法
        List<OrderDetail> result = orderService.listOrderDetailByOrderId(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(orderDetails.size(), result.size());

        // 验证方法调用
        verify(orderMapper, times(1)).listOrderDetailByOrderId(1);
    }

    @Test
    void testListOrdersByBusinessId() {
        // 模拟返回数据
        List<Order> expectedOrders = new ArrayList<>();
        expectedOrders.add(order);
        when(orderMapper.listOrdersByBusinessId(1)).thenReturn(expectedOrders);

        // 执行测试方法
        List<Order> result = orderService.listOrdersByBusinessId(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证方法调用
        verify(orderMapper, times(1)).listOrdersByBusinessId(1);
    }

    @Test
    void testUpdateOrderStatus() {
        // 模拟返回数据
        when(orderMapper.updateOrderStatus(any(Order.class))).thenReturn(1);
        when(orderMapper.getOrderById(1)).thenReturn(order);

        // 执行测试方法
        int result = orderService.updateOrderStatus(1, 1);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(orderMapper, times(1)).updateOrderStatus(any(Order.class));
        verify(orderMapper, times(1)).getOrderById(1);
        verify(rabbitMQSender, times(1)).sendOrderStatusChangeMessage(any(Order.class));
    }

    @Test
    void testPayOrder() {
        // 模拟返回数据
        when(orderMapper.updateOrderPayTime(any(Order.class))).thenReturn(1);
        when(orderMapper.getOrderById(1)).thenReturn(order);

        // 执行测试方法
        int result = orderService.payOrder(1);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(orderMapper, times(1)).updateOrderPayTime(any(Order.class));
        verify(orderMapper, times(1)).getOrderById(1);
        verify(rabbitMQSender, times(1)).sendOrderStatusChangeMessage(any(Order.class));
    }

    @Test
    void testDeliverOrder() {
        // 模拟返回数据
        when(orderMapper.updateOrderDeliveryTime(any(Order.class))).thenReturn(1);
        when(orderMapper.getOrderById(1)).thenReturn(order);

        // 执行测试方法
        int result = orderService.deliverOrder(1);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(orderMapper, times(1)).updateOrderDeliveryTime(any(Order.class));
        verify(orderMapper, times(1)).getOrderById(1);
        verify(rabbitMQSender, times(1)).sendOrderStatusChangeMessage(any(Order.class));
    }

    @Test
    void testCheckoutOrder() {
        // 模拟返回数据
        when(orderMapper.updateOrderCheckoutTime(any(Order.class))).thenReturn(1);
        when(orderMapper.getOrderById(1)).thenReturn(order);

        // 执行测试方法
        int result = orderService.checkoutOrder(1);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(orderMapper, times(1)).updateOrderCheckoutTime(any(Order.class));
        verify(orderMapper, times(1)).getOrderById(1);
        verify(rabbitMQSender, times(1)).sendOrderStatusChangeMessage(any(Order.class));
    }
}
