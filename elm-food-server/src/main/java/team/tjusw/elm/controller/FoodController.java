package team.tjusw.elm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.Food;
import team.tjusw.elm.service.FoodService;

@RestController
@RequestMapping("/foods")
public class FoodController {

	@Autowired
	private FoodService foodService;

	@GetMapping("/business/{businessId}")
	public CommonResult<List<Food>> listFoodByBusinessId(@PathVariable("businessId") Integer businessId) {
		List<Food> ret = foodService.listFoodByBusinessId(businessId);
		return new CommonResult<>(200, "ok", ret);
	}

	@PostMapping
	public CommonResult<Boolean> addFood(@RequestBody Food food) {
		try {
			boolean success = foodService.addFood(food);
			return new CommonResult<>(200, "ok", success);
		} catch (Exception e) {
			return new CommonResult<>(500, e.getMessage(), false);
		}
	}

	@GetMapping("/{foodId}")
	public CommonResult<Food> getFoodById(@PathVariable("foodId") Integer foodId) {
		Food ret = foodService.getFoodById(foodId);
		if (ret == null) {
			return new CommonResult<>(404, "food not found", null);
		}
		return new CommonResult<>(200, "ok", ret);
	}

	@PutMapping("/{foodId}")
	public CommonResult<Food> updateFood(@PathVariable("foodId") Integer foodId, @RequestBody Food food) {
		try {
			Food ret = foodService.updateFood(foodId, food);
			if (ret == null) {
				return new CommonResult<>(404, "food not found", null);
			}
			return new CommonResult<>(200, "updated", ret);
		} catch (Exception e) {
			return new CommonResult<>(500, e.getMessage(), null);
		}
	}

	@DeleteMapping("/{foodId}")
	public CommonResult<Boolean> deleteFood(@PathVariable("foodId") Integer foodId) {
		try {
			boolean success = foodService.deleteFood(foodId);
			if (!success) {
				return new CommonResult<>(404, "food not found", false);
			}
			return new CommonResult<>(200, "deleted", true);
		} catch (Exception e) {
			return new CommonResult<>(500, e.getMessage(), false);
		}
	}
}
