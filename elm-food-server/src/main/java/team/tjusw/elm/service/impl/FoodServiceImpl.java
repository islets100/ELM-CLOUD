package team.tjusw.elm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import team.tjusw.elm.feign.BusinessClient;
import team.tjusw.elm.mapper.FoodMapper;
import team.tjusw.elm.po.Food;
import team.tjusw.elm.service.FoodService;

@Service
public class FoodServiceImpl implements FoodService {

	@Autowired
	private FoodMapper foodMapper;

	@Autowired
	private BusinessClient businessClient;

	@Override
	public List<Food> listFoodByBusinessId(Integer businessId) {
		return foodMapper.listFoodByBusinessId(businessId);
	}

	@Override
	public Food getFoodById(Integer foodId) {
		return foodMapper.getFoodById(foodId);
	}

	@Override
	public boolean addFood(Food food) {
		businessClient.getBusinessById(food.getBusinessId());
		if (food.getValid() == null) {
			food.setValid(1);
		}
		return foodMapper.insertFood(food) > 0;
	}

	@Override
	public Food updateFood(Integer foodId, Food food) {
		Food existing = foodMapper.getFoodById(foodId);
		if (existing == null || (existing.getValid() != null && existing.getValid() == 0)) {
			return null;
		}
		if (food.getBusinessId() == null) {
			food.setBusinessId(existing.getBusinessId());
		}
		businessClient.getBusinessById(food.getBusinessId());
		food.setFoodId(foodId);
		food.setValid(existing.getValid());
		if (foodMapper.updateFood(food) <= 0) {
			return null;
		}
		return foodMapper.getFoodById(foodId);
	}

	@Override
	public boolean deleteFood(Integer foodId) {
		return foodMapper.deleteFood(foodId) > 0;
	}
}
