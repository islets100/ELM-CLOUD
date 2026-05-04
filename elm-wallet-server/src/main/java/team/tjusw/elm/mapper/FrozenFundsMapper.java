package team.tjusw.elm.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import team.tjusw.elm.po.FrozenFundsPO;

@Mapper
public interface FrozenFundsMapper {

    @Insert("INSERT INTO frozen_funds (orderId, userId, businessUserId, amount, status, createTime) " +
            "VALUES (#{orderId}, #{userId}, #{businessUserId}, #{amount}, #{status}, #{createTime})")
    int insert(FrozenFundsPO frozenFunds);

    @Select("SELECT * FROM frozen_funds WHERE orderId = #{orderId}")
    FrozenFundsPO selectByOrderId(@Param("orderId") Integer orderId);

    @Update("UPDATE frozen_funds SET status = #{status} WHERE orderId = #{orderId}")
    int updateStatus(@Param("orderId") Integer orderId, @Param("status") Integer status);
}
