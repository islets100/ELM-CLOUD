package team.tjusw.elm.po;

import java.math.BigDecimal;
import java.util.Date;

public class VirtualWallet {
	private Integer walletId;
	private String userId;
	private BigDecimal balance;
	private Date createTime;
	private Date updateTime;
	
	public Integer getWalletId() {
		return walletId;
	}
	
	public void setWalletId(Integer walletId) {
		this.walletId = walletId;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public BigDecimal getBalance() {
		return balance;
	}
	
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public Date getUpdateTime() {
		return updateTime;
	}
	
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}