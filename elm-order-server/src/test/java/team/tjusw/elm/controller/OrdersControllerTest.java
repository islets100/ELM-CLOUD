package team.tjusw.elm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
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
	@DisplayName("create order through controller")
	void testCreateOrders() throws Exception {
		LinkedHashMap<String, Object> item1 = new LinkedHashMap<>();
		item1.put("foodId", 201);
		item1.put("quantity", 2);
		LinkedHashMap<String, Object> item2 = new LinkedHashMap<>();
		item2.put("foodId", 202);
		item2.put("quantity", 1);

		when(cartClient.listCart("user2001", 20)).thenReturn(new CommonResult<>(200, "ok", Arrays.asList(item1, item2)));
		when(cartClient.removeCart("user2001", 20)).thenReturn(new CommonResult<>(200, "ok", 1));

		Orders order = new Orders();
		order.setUserId("user2001");
		order.setBusinessId(20);
		order.setDaId(2);
		order.setOrderTotal(new BigDecimal("35.00"));

		CommonResult<Integer> createResult = ordersController.createOrders(order);
		CommonResult<Orders> savedResult = ordersController.getOrdersById(createResult.getResult());

		assertEquals(201, createResult.getCode());
		assertTrue(createResult.getResult() > 0);
		assertEquals(200, savedResult.getCode());
		assertEquals("user2001", savedResult.getResult().getUserId());
		assertEquals(2, savedResult.getResult().getList().size());
	}

	@Test
	@DisplayName("pay by wallet success")
	void testPayByVirtualWalletSuccess() throws Exception {
		LinkedHashMap<String, Object> business = new LinkedHashMap<>();
		business.put("userId", "merchant1001");

		when(businessClient.getBusinessById(10)).thenReturn((CommonResult) new CommonResult<>(200, "ok", business));
		when(virtualWalletClient.getBalanceByUserId("user1001"))
				.thenReturn(new CommonResult<>(200, "ok", new BigDecimal("100.00")));
		when(pointClient.getBalanceByUserId("user1001")).thenReturn(new CommonResult<>(200, "ok", 500));
		when(pointClient.getDeductedAmount(1, 100))
				.thenReturn(new CommonResult<>(200, "ok", new BigDecimal("1.00")));
		when(virtualWalletClient.freezeFunds(1, "user1001", "merchant1001", new BigDecimal("22.50")))
				.thenReturn(new CommonResult<>(200, "ok", 1));
		when(pointClient.earnPointByOrder(1)).thenReturn(new CommonResult<>(200, "ok", 1));
		when(pointClient.deductOrder(1, 100)).thenReturn(new CommonResult<>(200, "ok", 1));

		CommonResult<Integer> result = ordersController.payByVirtualWallet(1, 100);
		CommonResult<Orders> paidOrder = ordersController.getOrdersById(1);

		assertEquals(200, result.getCode());
		assertEquals(200, result.getResult());
		assertEquals(1, paidOrder.getResult().getOrderState());
	}

	@Test
	@DisplayName("pay by wallet should be idempotent for paid order")
	void testPayByVirtualWalletAlreadyPaid() {
		LinkedHashMap<String, Object> business = new LinkedHashMap<>();
		business.put("userId", "merchant1001");

		when(businessClient.getBusinessById(10)).thenReturn((CommonResult) new CommonResult<>(200, "ok", business));
		when(virtualWalletClient.getBalanceByUserId("user1001"))
				.thenReturn(new CommonResult<>(200, "ok", new BigDecimal("100.00")));
		when(pointClient.getBalanceByUserId("user1001")).thenReturn(new CommonResult<>(200, "ok", 500));
		when(pointClient.getDeductedAmount(1, 50))
				.thenReturn(new CommonResult<>(200, "ok", new BigDecimal("0.50")));
		when(virtualWalletClient.freezeFunds(1, "user1001", "merchant1001", new BigDecimal("23.00")))
				.thenReturn(new CommonResult<>(200, "ok", 1));
		when(pointClient.earnPointByOrder(1)).thenReturn(new CommonResult<>(200, "ok", 1));
		when(pointClient.deductOrder(1, 50)).thenReturn(new CommonResult<>(200, "ok", 1));

		CommonResult<Integer> firstPayment = ordersController.payByVirtualWallet(1, 50);
		CommonResult<Integer> secondPayment = ordersController.payByVirtualWallet(1, 50);

		assertEquals(200, firstPayment.getCode());
		assertEquals(409, secondPayment.getCode());
		assertEquals(3, secondPayment.getResult());
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
