package team.tjusw.elm.po;

import java.math.BigDecimal;

public class OrderDetail {
	private Integer odId;
	private Integer orderId;
	private Integer foodId;
	private Integer quantity;
	private BigDecimal foodPrice;
	
	public Integer getOdId() {
		return odId;
	}
	public void setOdId(Integer odId) {
		this.odId = odId;
	}
	public Integer getOrderId() {
		return orderId;
	}
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	public Integer getFoodId() {
		return foodId;
	}
	public void setFoodId(Integer foodId) {
		this.foodId = foodId;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public BigDecimal getFoodPrice() {
		return foodPrice;
	}
	public void setFoodPrice(BigDecimal foodPrice) {
		this.foodPrice = foodPrice;
	}
}
