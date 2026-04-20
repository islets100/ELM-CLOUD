package team.tjusw.elm.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.tjusw.elm.mapper.FoodMapper;
import team.tjusw.elm.po.Food;
import team.tjusw.elm.service.FoodService;
import team.tjusw.elm.feign.BusinessClient;

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
        // Cross-service verification via Feign
        businessClient.getBusinessById(food.getBusinessId());
        
        // Add to database
        int rows = foodMapper.insertFood(food);
        return rows > 0;
    }
}