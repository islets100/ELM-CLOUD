package team.tjusw.elm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
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
	@DisplayName("list user orders")
	void testListOrdersByUserId() throws Exception {
		CommonResult<List<Orders>> result = ordersController.listOrdersByUserId("user1001");

		assertEquals(200, result.getCode());
		assertNotNull(result.getResult());
		assertFalse(result.getResult().isEmpty());
	}

	@Test
	@DisplayName("list business orders")
	void testListOrdersByBusinessId() throws Exception {
		CommonResult<List<Orders>> result = ordersController.listOrdersByBusinessId(10);

		assertEquals(200, result.getCode());
		assertNotNull(result.getResult());
		assertEquals(1, result.getResult().size());
		assertEquals(2, result.getResult().get(0).getList().size());
	}

	@Test
	@DisplayName("get order by id")
	void testGetOrdersById() throws Exception {
		CommonResult<Orders> result = ordersController.getOrdersById(1);

		assertEquals(200, result.getCode());
		assertNotNull(result.getResult());
		assertEquals("user1001", result.getResult().getUserId());
	}

	@Test
	@DisplayName("update order state")
	void testUpdateOrderState() throws Exception {
		CommonResult<Orders> result = ordersController.updateOrderState(1, 2);

		assertEquals(200, result.getCode());
		assertNotNull(result.getResult());
		assertEquals(2, result.getResult().getOrderState());
	}

	@Test
	@DisplayName("pay by wallet with insufficient balance")
	void testPayByVirtualWalletInsufficientBalance() {
		LinkedHashMap<String, Object> business = new LinkedHashMap<>();
		business.put("userId", "merchant1001");

		when(businessClient.getBusinessById(10)).thenReturn((CommonResult) new CommonResult<>(200, "ok", business));
		when(virtualWalletClient.getBalanceByUserId("user1001"))
				.thenReturn(new CommonResult<>(200, "ok", new BigDecimal("1.00")));
		when(pointClient.getBalanceByUserId("user1001")).thenReturn(new CommonResult<>(200, "ok", 999));

		CommonResult<Integer> result = ordersController.payByVirtualWallet(1, 0);

		assertEquals(402, result.getCode());
		assertEquals(1, result.getResult());
	}
}
