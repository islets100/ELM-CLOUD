package team.tjusw.elm.po;

import java.math.BigDecimal;

public class FrozenFundsPO {
    private Integer id;
    private Integer orderId;
    private String userId;
    private String businessUserId;
    private BigDecimal amount;
    private Integer status;
    private String createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBusinessUserId() { return businessUserId; }
    public void setBusinessUserId(String businessUserId) { this.businessUserId = businessUserId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
