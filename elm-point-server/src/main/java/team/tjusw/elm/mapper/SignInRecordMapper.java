package team.tjusw.elm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import team.tjusw.elm.po.SignInRecord;

import java.time.LocalDate;

/**
 * 签到记录Mapper接口
 */
@Mapper
public interface SignInRecordMapper {

    /**
     * 插入签到记录
     */
    int insert(SignInRecord signInRecord);

    /**
     * 根据用户ID查询签到记录，按签到日期倒序
     */
    SignInRecord findTopByUserIdOrderBySignDateDesc(@Param("userId") Long userId);

    /**
     * 查询用户今天的签到记录
     */
    SignInRecord findTodaySignIn(@Param("userId") Long userId, @Param("signDate") LocalDate signDate);
}
