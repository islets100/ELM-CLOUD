package team.tjusw.elm.service;

import java.math.BigDecimal;

public interface PointService {
	int getBalanceByUserId(String userId);
	int earnPointByOrder(Integer orderId);
	int deductOrder(Integer orderId, int pointAmount);
	BigDecimal getDeductedAmount(Integer orderId, int pointAmount);
}