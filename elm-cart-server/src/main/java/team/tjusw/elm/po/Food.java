package team.tjusw.elm.po;

import java.math.BigDecimal;

public class Food {
	private Integer foodId;
	private Integer businessId;
	private String foodName;
	private String foodExplain;
	private String foodImg;
	private BigDecimal foodPrice;
	private Integer foodStatus;
	
	public Integer getFoodId() {
		return foodId;
	}
	public void setFoodId(Integer foodId) {
		this.foodId = foodId;
	}
	public Integer getBusinessId() {
		return businessId;
	}
	public void setBusinessId(Integer businessId) {
		this.businessId = businessId;
	}
	public String getFoodName() {
		return foodName;
	}
	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}
	public String getFoodExplain() {
		return foodExplain;
	}
	public void setFoodExplain(String foodExplain) {
		this.foodExplain = foodExplain;
	}
	public String getFoodImg() {
		return foodImg;
	}
	public void setFoodImg(String foodImg) {
		this.foodImg = foodImg;
	}
	public BigDecimal getFoodPrice() {
		return foodPrice;
	}
	public void setFoodPrice(BigDecimal foodPrice) {
		this.foodPrice = foodPrice;
	}
	public Integer getFoodStatus() {
		return foodStatus;
	}
	public void setFoodStatus(Integer foodStatus) {
		this.foodStatus = foodStatus;
	}
}
