package team.tjusw.elm.service;
import java.math.BigDecimal;
import team.tjusw.elm.po.VirtualWalletPO;

public interface VirtualWalletService {
    VirtualWalletPO getVirtualWallet(String userId);
    BigDecimal getBalance(String userId);
    boolean recharge(String userId, BigDecimal amount);
    boolean pay(String userId, BigDecimal amount);
    int transfer(String fromUserId, String toUserId, BigDecimal amount);
    int freezeFunds(Integer orderId, String userId, String businessUserId, BigDecimal amount);
    int releaseFrozenFunds(Integer orderId);
    boolean repayOverdraft(String userId, BigDecimal amount);
}
