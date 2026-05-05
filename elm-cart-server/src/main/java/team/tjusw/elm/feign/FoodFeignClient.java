package team.tjusw.elm.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.Food;

@FeignClient(name = "elm-food-server")
public interface FoodFeignClient {
    
    @GetMapping("/foods/{foodId}")
    CommonResult<Food> getFoodById(@PathVariable("foodId") Integer foodId);
}
