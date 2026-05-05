package team.tjusw.elm.mapper;

import java.math.BigDecimal;
import team.tjusw.elm.po.Point;

public interface PointMapper {
	int getBalanceByUserId(String userId);
	int earnPointByOrder(Integer orderId);
	int deductOrder(Integer orderId, int pointAmount);
	BigDecimal getDeductedAmount(Integer orderId, int pointAmount);
}