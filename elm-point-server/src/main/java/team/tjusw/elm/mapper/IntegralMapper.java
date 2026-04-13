package team.tjusw.elm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import team.tjusw.elm.po.Integral;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分Mapper接口
 */
@Mapper
public interface IntegralMapper {

    /**
     * 插入积分记录
     */
    int insert(Integral integral);

    /**
     * 根据ID更新积分记录
     */
    int updateById(Integral integral);

    /**
     * 根据ID查询积分记录
     */
    Integral selectById(@Param("id") Integer id);

    /**
     * 根据用户ID查询所有积分记录，按创建时间倒序
     */
    List<Integral> selectByUserIdOrderByCreateTimeDesc(@Param("userId") Long userId);

    /**
     * 根据用户ID和状态查询积分记录，按过期时间升序
     */
    List<Integral> selectByUserIdAndStatusOrderByExpireTimeAsc(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 根据用户ID、状态和过期时间查询积分记录
     */
    List<Integral> selectByUserIdAndStatusAndExpireTimeAfter(@Param("userId") Long userId, @Param("status") String status, @Param("expireTime") LocalDateTime expireTime);

    /**
     * 查询过期的积分记录
     */
    List<Integral> selectByStatusAndExpireTimeBefore(@Param("status") String status, @Param("expireTime") LocalDateTime expireTime);

    /**
     * 查询用户可用积分总和
     */
    Integer sumAmountByUserIdAndStatusAndExpireTimeAfter(@Param("userId") Long userId, @Param("status") String status, @Param("expireTime") LocalDateTime expireTime);
}
