package team.tjusw.elm.po;

import java.math.BigDecimal;
import java.util.Date;

public class Point {
	private Integer pointId;
	private String userId;
	private Integer orderId;
	private Integer pointAmount;
	private Date createTime;
	private Integer pointType;
	
	public Integer getPointId() {
		return pointId;
	}
	
	public void setPointId(Integer pointId) {
		this.pointId = pointId;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Integer getOrderId() {
		return orderId;
	}
	
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	
	public Integer getPointAmount() {
		return pointAmount;
	}
	
	public void setPointAmount(Integer pointAmount) {
		this.pointAmount = pointAmount;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public Integer getPointType() {
		return pointType;
	}
	
	public void setPointType(Integer pointType) {
		this.pointType = pointType;
	}
}