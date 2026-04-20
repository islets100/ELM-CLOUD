package team.tjusw.elm.mapper;
import org.apache.ibatis.annotations.*;
import team.tjusw.elm.po.VipCardPO;

@Mapper
public interface VipCardMapper {
    @Insert("INSERT INTO vip_card (user_id, card_type, purchase_date, expire_date, status) VALUES (#{userId}, #{cardType}, #{purchaseDate}, #{expireDate}, #{status})")
    int insert(VipCardPO vipCard);
    
    @Select("SELECT * FROM vip_card WHERE user_id = #{userId} AND status = 1")
    VipCardPO selectActiveByUserId(@Param("userId") String userId);
}