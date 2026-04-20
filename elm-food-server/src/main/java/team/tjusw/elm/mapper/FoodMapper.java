package team.tjusw.elm.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import team.tjusw.elm.po.Food;

@Mapper
public interface FoodMapper {
    @Select("select * from food where businessId=#{businessId} and valid = 1 order by foodId")
    List<Food> listFoodByBusinessId(Integer businessId);

    @Select("select * from food where foodId=#{foodId}")
    Food getFoodById(Integer foodId);

    @Insert("insert into food (foodName, foodExplain, foodImg, foodPrice, businessId, remarks, valid) " +
            "values (#{foodName}, #{foodExplain}, #{foodImg}, #{foodPrice}, #{businessId}, #{remarks}, #{valid})")
    @Options(useGeneratedKeys = true, keyProperty = "foodId")
    int insertFood(Food food);
}