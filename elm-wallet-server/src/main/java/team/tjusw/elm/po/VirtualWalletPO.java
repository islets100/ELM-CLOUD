package team.tjusw.elm.po;
import java.math.BigDecimal;
public class VirtualWalletPO {
    private Integer walletId;
    private String userId;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal usedCreditLimit;
    private String lastInterestTime;
    private String createTime;

    public Integer getWalletId() { return walletId; }
    public void setWalletId(Integer walletId) { this.walletId = walletId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public BigDecimal getUsedCreditLimit() { return usedCreditLimit; }
    public void setUsedCreditLimit(BigDecimal usedCreditLimit) { this.usedCreditLimit = usedCreditLimit; }
    public String getLastInterestTime() { return lastInterestTime; }
    public void setLastInterestTime(String lastInterestTime) { this.lastInterestTime = lastInterestTime; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}