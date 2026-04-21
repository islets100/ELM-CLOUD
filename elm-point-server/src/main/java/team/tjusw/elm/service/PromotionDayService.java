package team.tjusw.elm.service;

import java.util.List;

/**
 * 促销日服务接口
 */
public interface PromotionDayService {

    /**
     * 检查今天是否是促销日
     * @return 如果是促销日返回true，否则返回false
     */
    boolean isTodayPromotionDay();

    /**
     * 检查用户是否可以领取红包
     * @param userId 用户ID
     * @return 如果可以领取返回true，否则返回false
     */
    boolean canClaimRedPacket(Long userId);

    /**
     * 用户领取红包
     * @param userId 用户ID
     * @return 领取到的红包金额，如果无法领取则返回0
     */
    Integer claimRedPacket(Long userId);

    /**
     * 获取用户的红包领取记录
     * @param userId 用户ID
     * @return 红包领取记录列表
     */
    List<Object> getRedPacketRecords(Long userId);
}
