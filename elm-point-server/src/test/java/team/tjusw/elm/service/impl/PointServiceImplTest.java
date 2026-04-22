package team.tjusw.elm.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team.tjusw.elm.feign.OrderClient;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.OrderInfo;
import team.tjusw.elm.service.IntegralService;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

	@Mock
	private IntegralService integralService;

	@Mock
	private OrderClient orderClient;

	@InjectMocks
	private PointServiceImpl pointService;

	@Test
	void getBalanceByUserIdShouldDelegateForStringUserId() {
		when(integralService.getAvailableIntegral("TestForEnterWallet")).thenReturn(12);

		assertEquals(12, pointService.getBalanceByUserId("TestForEnterWallet"));
		verify(integralService).getAvailableIntegral(eq("TestForEnterWallet"));
	}

	@Test
	void earnPointByOrderShouldSupportStringUserId() {
		OrderInfo order = new OrderInfo();
		order.setUserId("TestForEnterWallet");
		order.setOrderTotal(BigDecimal.valueOf(28.5));
		when(orderClient.getOrderById(1001)).thenReturn(new CommonResult<>(200, "ok", order));
		when(integralService.calculateIntegralByOrderAmount(28.5)).thenReturn(28);

		assertEquals(28, pointService.earnPointByOrder(1001));
		verify(integralService).addIntegral(eq("TestForEnterWallet"), eq(28), eq("CONSUMPTION"), eq(1001L), any());
	}

	@Test
	void deductOrderShouldSupportStringUserId() {
		OrderInfo order = new OrderInfo();
		order.setUserId("TestForEnterWallet");
		order.setOrderTotal(BigDecimal.valueOf(28.5));
		when(orderClient.getOrderById(1001)).thenReturn(new CommonResult<>(200, "ok", order));

		assertEquals(1, pointService.deductOrder(1001, 200));
		verify(integralService).consumeIntegral(eq("TestForEnterWallet"), eq(200), eq("ORDER_DEDUCTION"), eq(1001L), any());
	}

	@Test
	void getBalanceByUserIdShouldDelegateForLegacyNumericTextUserId() {
		when(integralService.getAvailableIntegral("1001")).thenReturn(88);

		assertEquals(88, pointService.getBalanceByUserId("1001"));
		verify(integralService).getAvailableIntegral(eq("1001"));
	}
}
