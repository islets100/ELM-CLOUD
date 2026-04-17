package team.tjusw.elm.po;
import java.math.BigDecimal;
public class OverdraftRecordPO {
    private Integer id;
    private Integer walletId;
    private BigDecimal amount;
    private BigDecimal repaidAmount;
    private BigDecimal interest;
    private String createTime;
    private Integer status;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getWalletId() { return walletId; }
    public void setWalletId(Integer walletId) { this.walletId = walletId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getRepaidAmount() { return repaidAmount; }
    public void setRepaidAmount(BigDecimal repaidAmount) { this.repaidAmount = repaidAmount; }
    public BigDecimal getInterest() { return interest; }
    public void setInterest(BigDecimal interest) { this.interest = interest; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}