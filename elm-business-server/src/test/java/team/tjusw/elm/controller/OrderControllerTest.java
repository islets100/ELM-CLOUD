package team.tjusw.elm.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import team.tjusw.elm.po.Order;
import team.tjusw.elm.po.OrderDetail;
import team.tjusw.elm.po.OrderRequest;
import team.tjusw.elm.service.OrderService;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private Order order;
    private List<OrderDetail> orderDetails;
    private OrderRequest orderRequest;

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

        orderRequest = new OrderRequest();
        orderRequest.setUserId("user1");
        orderRequest.setBusinessId(1);
        orderRequest.setDaId(1);
        orderRequest.setOrderDetails(orderDetails);

        // 初始化 MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void testCreateOrder() throws Exception {
        // 模拟服务方法
        when(orderService.createOrder(anyString(), anyInt(), anyInt(), anyList())).thenReturn(order);

        // 执行测试
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("订单创建成功"))
                .andExpect(jsonPath("$.result.orderId").value(order.getOrderId()));

        // 验证方法调用
        verify(orderService, times(1)).createOrder(anyString(), anyInt(), anyInt(), anyList());
    }

    @Test
    void testListOrdersByUserId() throws Exception {
        // 模拟返回数据
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        when(orderService.listOrdersByUserId("user1")).thenReturn(orders);

        // 执行测试
        mockMvc.perform(get("/orders/user/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取订单列表成功"))
                .andExpect(jsonPath("$.result.length()").value(1));

        // 验证方法调用
        verify(orderService, times(1)).listOrdersByUserId("user1");
    }

    @Test
    void testListOrdersByBusinessId() throws Exception {
        // 模拟返回数据
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        when(orderService.listOrdersByBusinessId(1)).thenReturn(orders);

        // 执行测试
        mockMvc.perform(get("/orders/business/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取商家订单列表成功"))
                .andExpect(jsonPath("$.result.length()").value(1));

        // 验证方法调用
        verify(orderService, times(1)).listOrdersByBusinessId(1);
    }

    @Test
    void testGetOrderById() throws Exception {
        // 模拟返回数据
        when(orderService.getOrderById(1)).thenReturn(order);

        // 执行测试
        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取订单详情成功"))
                .andExpect(jsonPath("$.result.orderId").value(order.getOrderId()));

        // 验证方法调用
        verify(orderService, times(1)).getOrderById(1);
    }

    @Test
    void testListOrderDetailByOrderId() throws Exception {
        // 模拟返回数据
        when(orderService.listOrderDetailByOrderId(1)).thenReturn(orderDetails);

        // 执行测试
        mockMvc.perform(get("/orders/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取订单详情成功"))
                .andExpect(jsonPath("$.result.length()").value(orderDetails.size()));

        // 验证方法调用
        verify(orderService, times(1)).listOrderDetailByOrderId(1);
    }

    @Test
    void testPayOrder() throws Exception {
        // 模拟返回数据
        when(orderService.payOrder(1)).thenReturn(1);

        // 执行测试
        mockMvc.perform(put("/orders/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("支付成功"))
                .andExpect(jsonPath("$.result").value(1));

        // 验证方法调用
        verify(orderService, times(1)).payOrder(1);
    }

    @Test
    void testDeliverOrder() throws Exception {
        // 模拟返回数据
        when(orderService.deliverOrder(1)).thenReturn(1);

        // 执行测试
        mockMvc.perform(put("/orders/1/deliver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("开始配送"))
                .andExpect(jsonPath("$.result").value(1));

        // 验证方法调用
        verify(orderService, times(1)).deliverOrder(1);
    }

    @Test
    void testCheckoutOrder() throws Exception {
        // 模拟返回数据
        when(orderService.checkoutOrder(1)).thenReturn(1);

        // 执行测试
        mockMvc.perform(put("/orders/1/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("订单完成"))
                .andExpect(jsonPath("$.result").value(1));

        // 验证方法调用
        verify(orderService, times(1)).checkoutOrder(1);
    }
}
