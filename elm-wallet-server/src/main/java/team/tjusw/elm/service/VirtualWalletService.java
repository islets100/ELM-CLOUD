package team.tjusw.elm.service;

import java.math.BigDecimal;

public interface VirtualWalletService {
	BigDecimal getBalanceByUserId(String userId);
	int transfer(String fromUserId, String toUserId, BigDecimal amount);
}