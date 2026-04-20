package team.tjusw.elm.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import team.tjusw.elm.feign.BusinessClient;
import team.tjusw.elm.feign.CartClient;
import team.tjusw.elm.feign.PointClient;
import team.tjusw.elm.feign.VirtualWalletClient;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.Orders;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrdersControllerTest {

    @Autowired
    private OrdersController ordersController;

    @MockBean
    private CartClient cartClient;

    @MockBean
    private VirtualWalletClient virtualWalletClient;

    @MockBean
    private PointClient pointClient;

    @MockBean
    private BusinessClient businessClient;

    @Test
    @DisplayName("测试查询用户订单列表接口")
    void testListOrdersByUserId() throws Exception {
        CommonResult<List<Orders>> result = ordersController.listOrdersByUserId("user1001");

        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertFalse(result.getResult().isEmpty());
    }

    @Test
    @DisplayName("测试查询单个订单接口")
    void testGetOrdersById() throws Exception {
        CommonResult<Orders> result = ordersController.getOrdersById(1);

        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals("user1001", result.getResult().getUserId());
    }

    @Test
    @DisplayName("测试支付接口在余额不足时返回402")
    void testPayByVirtualWalletInsufficientBalance() {
        java.util.LinkedHashMap<String, Object> business = new java.util.LinkedHashMap<>();
        business.put("userId", "merchant1001");

        when(businessClient.getBusinessById(10)).thenReturn((CommonResult) new CommonResult<>(200, "ok", business));
        when(virtualWalletClient.getBalanceByUserId("user1001")).thenReturn(new CommonResult<>(200, "ok", new BigDecimal("1.00")));
        when(pointClient.getBalanceByUserId("user1001")).thenReturn(new CommonResult<>(200, "ok", 999));

        CommonResult<Integer> result = ordersController.payByVirtualWallet(1, 0);

        assertEquals(402, result.getCode());
        assertEquals(1, result.getResult());
    }
}
