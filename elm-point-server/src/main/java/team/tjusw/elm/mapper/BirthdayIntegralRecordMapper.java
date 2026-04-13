package team.tjusw.elm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import team.tjusw.elm.po.BirthdayIntegralRecord;

import java.time.LocalDate;

/**
 * 生日积分记录Mapper接口
 */
@Mapper
public interface BirthdayIntegralRecordMapper {

    /**
     * 插入生日积分记录
     */
    int insert(BirthdayIntegralRecord record);

    /**
     * 根据用户ID和记录日期查询
     */
    BirthdayIntegralRecord findByUserIdAndRecordDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);

    /**
     * 检查指定用户和日期的记录是否存在
     */
    boolean existsByUserIdAndRecordDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);
}
