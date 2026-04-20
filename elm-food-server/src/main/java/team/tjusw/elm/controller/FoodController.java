package team.tjusw.elm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.tjusw.elm.service.FoodService;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.Food;

@RestController
@RequestMapping("/food")
 // Force IDE trigger
public class FoodController {
    
    @Autowired
    private FoodService foodService;

    @GetMapping("/list")
    public CommonResult<List<Food>> listFoodByBusinessId(@RequestParam("businessId") Integer businessId) {
        List<Food> ret = foodService.listFoodByBusinessId(businessId);
        if (ret == null || ret.isEmpty()) {
            return new CommonResult<>(200, "没有该商家的商品", ret);
        } else {
            return new CommonResult<>(200, "成功", ret);
        }
    }

    @PostMapping("/add")
    public CommonResult<Boolean> addFood(Food food) {
        try {
            boolean success = foodService.addFood(food);
            return new CommonResult<>(200, "success", success);
        } catch (Exception e) {
            return new CommonResult<>(500, e.getMessage(), false);
        }
    }

    @GetMapping("/{foodId}")
    public CommonResult<Food> getFoodById(@PathVariable("foodId") Integer foodId) {
        Food ret = foodService.getFoodById(foodId);
        if (ret == null) {
            return new CommonResult<>(404, "不存在该食品", ret);
        } else {
            return new CommonResult<>(200, "成功", ret);
        }
    }
}