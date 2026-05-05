package team.tjusw.elm.service;

import java.util.List;

import team.tjusw.elm.po.Food;

public interface FoodService {
	List<Food> listFoodByBusinessId(Integer businessId);

	Food getFoodById(Integer foodId);

	boolean addFood(Food food);

	Food updateFood(Integer foodId, Food food);

	boolean deleteFood(Integer foodId);
}
