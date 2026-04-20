package team.tjusw.elm.po;
public class VipCardPO {
    private Integer id;
    private String userId;
    private String cardType;
    private String purchaseDate;
    private String expireDate;
    private Integer status;
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getExpireDate() { return expireDate; }
    public void setExpireDate(String expireDate) { this.expireDate = expireDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}