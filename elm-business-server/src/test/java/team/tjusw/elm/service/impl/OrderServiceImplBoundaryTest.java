package team.tjusw.elm.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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
public class OrderServiceImplBoundaryTest {

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

        orderDetails = new ArrayList<>();
        OrderDetail detail1 = new OrderDetail();
        detail1.setFoodId(1);
        detail1.setQuantity(2);
        detail1.setFoodPrice(new java.math.BigDecimal(50.0));
        orderDetails.add(detail1);
    }

    @Test
    void testCreateOrder_EmptyOrderDetails() {
        // 测试空订单详情
        List<OrderDetail> emptyOrderDetails = new ArrayList<>();

        // 模拟 orderMapper.saveOrder 方法
        when(orderMapper.saveOrder(any(Order.class))).thenReturn(1);

        // 执行测试方法
        Order result = orderService.createOrder("user1", 1, 1, emptyOrderDetails);

        // 验证结果
        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals(1, result.getBusinessId());
        assertEquals(1, result.getDaId());
        assertEquals(new java.math.BigDecimal(0.0), result.getOrderTotal());
        assertEquals(0, result.getOrderState());

        // 验证方法调用
        verify(orderMapper, times(1)).saveOrder(any(Order.class));
        verify(orderMapper, times(0)).saveOrderDetail(any(OrderDetail.class));
        verify(rabbitMQSender, times(1)).sendOrderStatusChangeMessage(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        // 模拟返回数据
        when(orderMapper.updateOrderStatus(any(Order.class))).thenReturn(0);

        // 执行测试方法
        int result = orderService.updateOrderStatus(1, 1);

        // 验证结果
        assertEquals(0, result);

        // 验证方法调用
        verify(orderMapper, times(1)).updateOrderStatus(any(Order.class));
        verify(orderMapper, times(0)).getOrderById(1);
        verify(rabbitMQSender, times(0)).sendOrderStatusChangeMessage(any(Order.class));
    }

    @Test
    void testPayOrder_OrderNotFound() {
        // 模拟返回数据
        when(orderMapper.updateOrderPayTime(any(Order.class))).thenReturn(0);

        // 执行测试方法
        int result = orderService.payOrder(1);

        // 验证结果
        assertEquals(0, result);

        // 验证方法调用
        verify(orderMapper, times(1)).updateOrderPayTime(any(Order.class));
        verify(orderMapper, times(0)).getOrderById(1);
        verify(rabbitMQSender, times(0)).sendOrderStatusChangeMessage(any(Order.class));
    }

    @Test
    void testDeliverOrder_OrderNotFound() {
        // 模拟返回数据
        when(orderMapper.updateOrderDeliveryTime(any(Order.class))).thenReturn(0);

        // 执行测试方法
        int result = orderService.deliverOrder(1);

        // 验证结果
        assertEquals(0, result);

        // 验证方法调用
        verify(orderMapper, times(1)).updateOrderDeliveryTime(any(Order.class));
        verify(orderMapper, times(0)).getOrderById(1);
        verify(rabbitMQSender, times(0)).sendOrderStatusChangeMessage(any(Order.class));
    }

    @Test
    void testCheckoutOrder_OrderNotFound() {
        // 模拟返回数据
        when(orderMapper.updateOrderCheckoutTime(any(Order.class))).thenReturn(0);

        // 执行测试方法
        int result = orderService.checkoutOrder(1);

        // 验证结果
        assertEquals(0, result);

        // 验证方法调用
        verify(orderMapper, times(1)).updateOrderCheckoutTime(any(Order.class));
        verify(orderMapper, times(0)).getOrderById(1);
        verify(rabbitMQSender, times(0)).sendOrderStatusChangeMessage(any(Order.class));
    }

    @Test
    void testListOrdersByUserId_EmptyList() {
        // 模拟返回空列表
        when(orderMapper.listOrdersByUserId("user1")).thenReturn(new ArrayList<>());

        // 执行测试方法
        List<Order> result = orderService.listOrdersByUserId("user1");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证方法调用
        verify(orderMapper, times(1)).listOrdersByUserId("user1");
    }

    @Test
    void testListOrderDetailByOrderId_EmptyList() {
        // 模拟返回空列表
        when(orderMapper.listOrderDetailByOrderId(1)).thenReturn(new ArrayList<>());

        // 执行测试方法
        List<OrderDetail> result = orderService.listOrderDetailByOrderId(1);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证方法调用
        verify(orderMapper, times(1)).listOrderDetailByOrderId(1);
    }

    @Test
    void testListOrdersByBusinessId_EmptyList() {
        // 模拟返回空列表
        when(orderMapper.listOrdersByBusinessId(1)).thenReturn(new ArrayList<>());

        // 执行测试方法
        List<Order> result = orderService.listOrdersByBusinessId(1);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证方法调用
        verify(orderMapper, times(1)).listOrdersByBusinessId(1);
    }

    @Test
    void testGetOrderById_OrderNotFound() {
        // 模拟返回 null
        when(orderMapper.getOrderById(1)).thenReturn(null);

        // 执行测试方法
        Order result = orderService.getOrderById(1);

        // 验证结果
        assertNull(result);

        // 验证方法调用
        verify(orderMapper, times(1)).getOrderById(1);
    }
}
