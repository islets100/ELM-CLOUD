package team.tjusw.elm.service.impl;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import team.tjusw.elm.service.VirtualWalletService;

@Service
public class VirtualWalletServiceImpl implements VirtualWalletService {
	
	@Override
	public BigDecimal getBalanceByUserId(String userId) {
		// 模拟实现，返回默认值
		return new BigDecimal(1000);
	}
	
	@Override
	public int transfer(String fromUserId, String toUserId, BigDecimal amount) {
		// 模拟实现，返回成功
		return 1;
	}
}