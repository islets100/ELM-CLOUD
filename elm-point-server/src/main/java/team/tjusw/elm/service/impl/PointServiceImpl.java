package team.tjusw.elm.service.impl;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import team.tjusw.elm.service.PointService;

@Service
public class PointServiceImpl implements PointService {
	
	// @Autowired
	// private PointMapper pointMapper;
	
	@Override
	public int getBalanceByUserId(String userId) {
		// return pointMapper.getBalanceByUserId(userId);
		return 0;
	}
	
	@Override
	public int earnPointByOrder(Integer orderId) {
		// return pointMapper.earnPointByOrder(orderId);
		return 0;
	}
	
	@Override
	public int deductOrder(Integer orderId, int pointAmount) {
		// return pointMapper.deductOrder(orderId, pointAmount);
		return 0;
	}
	
	@Override
	public BigDecimal getDeductedAmount(Integer orderId, int pointAmount) {
		// return pointMapper.getDeductedAmount(orderId, pointAmount);
		return BigDecimal.ZERO;
	}
}