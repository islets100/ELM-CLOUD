package team.tjusw.elm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import team.tjusw.elm.mapper.OrderDetailetMapper;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.OrderDetailet;
import team.tjusw.elm.po.Orders;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrdersServiceTest {

	@Autowired
	private OrdersService ordersService;

	@Autowired
	private OrderDetailetMapper orderDetailetMapper;

	@MockBean
	private CartClient cartClient;

	@MockBean
	private VirtualWalletClient virtualWalletClient;

	@MockBean
	private PointClient pointClient;

	@MockBean
	private BusinessClient businessClient;

	@Test
	@DisplayName("list orders by user id")
	void testListOrdersByUserId() {
		List<Orders> orders = ordersService.listOrdersByUserId("user1001");

		assertNotNull(orders);
		assertEquals(1, orders.size());
		assertNotNull(orders.get(0).getList());
		assertEquals(2, orders.get(0).getList().size());
	}

	@Test
	@DisplayName("list orders by business id")
	void testListOrdersByBusinessId() {
		List<Orders> orders = ordersService.listOrdersByBusinessId(10);

		assertNotNull(orders);
		assertEquals(1, orders.size());
		assertNotNull(orders.get(0).getList());
		assertEquals(2, orders.get(0).getList().size());
	}

	@Test
	@DisplayName("get order by id")
	void testGetOrdersById() {
		Orders order = ordersService.getOrdersById(1);

		assertNotNull(order);
		assertEquals("user1001", order.getUserId());
		assertNotNull(order.getList());
		assertEquals(2, order.getList().size());
	}

	@Test
	@DisplayName("update order state")
	void testUpdateOrderState() {
		Orders updated = ordersService.updateOrderState(1, 2);
		Orders persisted = ordersService.getOrdersById(1);

		assertNotNull(updated);
		assertEquals(2, updated.getOrderState());
		assertEquals(2, persisted.getOrderState());
	}

	@Test
	@DisplayName("create order")
	void testCreateOrders() {
		LinkedHashMap<String, Object> item1 = new LinkedHashMap<>();
		item1.put("foodId", 201);
		item1.put("quantity", 2);
		LinkedHashMap<String, Object> item2 = new LinkedHashMap<>();
		item2.put("foodId", 202);
		item2.put("quantity", 1);
		CommonResult<List<?>> cartResult = new CommonResult<>(200, "ok", Arrays.asList(item1, item2));

		when(cartClient.listCart("user2001", 20)).thenReturn(cartResult);
		when(cartClient.removeCart("user2001", 20)).thenReturn(new CommonResult<>(200, "ok", 1));

		Orders order = new Orders();
		order.setUserId("user2001");
		order.setBusinessId(20);
		order.setDaId(2);
		order.setOrderTotal(new BigDecimal("35.00"));

		int orderId = ordersService.createOrders(order);
		Orders saved = ordersService.getOrdersById(orderId);
		List<OrderDetailet> details = orderDetailetMapper.listOrderDetailetByOrderId(orderId);

		assertTrue(orderId > 0);
		assertNotNull(saved);
		assertEquals(2, details.size());
		verify(cartClient, times(1)).listCart("user2001", 20);
		verify(cartClient, times(1)).removeCart("user2001", 20);
	}

	@Test
	@DisplayName("pay by wallet success")
	void testPayByVirtualWalletSuccess() {
		LinkedHashMap<String, Object> business = new LinkedHashMap<>();
		business.put("userId", "merchant1001");

		when(businessClient.getBusinessById(10)).thenReturn((CommonResult) new CommonResult<>(200, "ok", business));
		when(virtualWalletClient.getBalanceByUserId("user1001"))
				.thenReturn(new CommonResult<>(200, "ok", new BigDecimal("100.00")));
		when(pointClient.getBalanceByUserId("user1001")).thenReturn(new CommonResult<>(200, "ok", 500));
		when(pointClient.getDeductedAmount(1, 100))
				.thenReturn(new CommonResult<>(200, "ok", new BigDecimal("1.00")));
		when(virtualWalletClient.transfer("user1001", "merchant1001", new BigDecimal("22.50")))
				.thenReturn(new CommonResult<>(200, "ok", 1));
		when(pointClient.earnPointByOrder(1)).thenReturn(new CommonResult<>(200, "ok", 1));
		when(pointClient.deductOrder(1, 100)).thenReturn(new CommonResult<>(200, "ok", 1));

		Integer result = ordersService.payByVirtualWallet("user1001", 1, 100);
		Orders paidOrder = ordersService.getOrdersById(1);

		assertEquals(200, result);
		assertEquals(1, paidOrder.getOrderState());
	}

	@Test
	@DisplayName("pay by wallet with insufficient balance")
	void testPayByVirtualWalletInsufficientBalance() {
		LinkedHashMap<String, Object> business = new LinkedHashMap<>();
		business.put("userId", "merchant1001");

		when(businessClient.getBusinessById(10)).thenReturn((CommonResult) new CommonResult<>(200, "ok", business));
		when(virtualWalletClient.getBalanceByUserId("user1001"))
				.thenReturn(new CommonResult<>(200, "ok", new BigDecimal("10.00")));
		when(pointClient.getBalanceByUserId("user1001")).thenReturn(new CommonResult<>(200, "ok", 500));

		Integer result = ordersService.payByVirtualWallet("user1001", 1, 100);

		assertEquals(1, result);
	}

	@Test
	@DisplayName("pay by wallet should reject already paid order")
	void testPayByVirtualWalletAlreadyPaid() {
		LinkedHashMap<String, Object> business = new LinkedHashMap<>();
		business.put("userId", "merchant1001");

		when(businessClient.getBusinessById(10)).thenReturn((CommonResult) new CommonResult<>(200, "ok", business));
		when(virtualWalletClient.getBalanceByUserId("user1001"))
				.thenReturn(new CommonResult<>(200, "ok", new BigDecimal("100.00")));
		when(pointClient.getBalanceByUserId("user1001")).thenReturn(new CommonResult<>(200, "ok", 500));
		when(pointClient.getDeductedAmount(1, 50))
				.thenReturn(new CommonResult<>(200, "ok", new BigDecimal("0.50")));
		when(virtualWalletClient.transfer("user1001", "merchant1001", new BigDecimal("23.00")))
				.thenReturn(new CommonResult<>(200, "ok", 1));
		when(pointClient.earnPointByOrder(1)).thenReturn(new CommonResult<>(200, "ok", 1));
		when(pointClient.deductOrder(1, 50)).thenReturn(new CommonResult<>(200, "ok", 1));

		Integer firstPayment = ordersService.payByVirtualWallet("user1001", 1, 50);
		Integer secondPayment = ordersService.payByVirtualWallet("user1001", 1, 50);

		assertEquals(200, firstPayment);
		assertEquals(3, secondPayment);
	}
}
