package team.tjusw.elm.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import team.tjusw.elm.feign.OrderClient;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.Integral;
import team.tjusw.elm.po.OrderInfo;
import team.tjusw.elm.service.IntegralService;
import team.tjusw.elm.service.PointService;

@Service
public class PointServiceImpl implements PointService {

	@Autowired
	private IntegralService integralService;

	@Autowired
	private OrderClient orderClient;

	@Override
	public int getBalanceByUserId(String userId) {
		return integralService.getAvailableIntegral(userId);
	}

	@Override
	public int earnPointByOrder(Integer orderId) {
		OrderInfo order = getOrderInfo(orderId);
		int points = integralService.calculateIntegralByOrderAmount(order.getOrderTotal().doubleValue());
		if (points <= 0) {
			return 0;
		}
		integralService.addIntegral(order.getUserId(), points, Integral.CHANNEL_CONSUMPTION, Long.valueOf(orderId),
				"订单消费获得积分");
		return points;
	}

	@Override
	public int deductOrder(Integer orderId, int pointAmount) {
		if (pointAmount <= 0) {
			return 1;
		}
		OrderInfo order = getOrderInfo(orderId);
		integralService.consumeIntegral(order.getUserId(), pointAmount, Integral.CHANNEL_ORDER_DEDUCTION,
				Long.valueOf(orderId), "订单积分抵扣");
		return 1;
	}

	@Override
	public BigDecimal getDeductedAmount(Integer orderId, int pointAmount) {
		if (pointAmount <= 0) {
			return BigDecimal.ZERO;
		}
		OrderInfo order = getOrderInfo(orderId);
		int maxUsablePoints = order.getOrderTotal().multiply(BigDecimal.valueOf(100)).intValue();
		int actualPoints = Math.min(pointAmount, maxUsablePoints);
		return BigDecimal.valueOf(actualPoints).divide(BigDecimal.valueOf(100));
	}

	private OrderInfo getOrderInfo(Integer orderId) {
		CommonResult<OrderInfo> response = orderClient.getOrderById(orderId);
		if (response == null || response.getCode() != 200 || response.getResult() == null) {
			throw new IllegalArgumentException("Order does not exist");
		}
		return response.getResult();
	}
}
