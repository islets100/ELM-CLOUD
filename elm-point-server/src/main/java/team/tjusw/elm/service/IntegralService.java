package team.tjusw.elm.service;

import team.tjusw.elm.po.Integral;

import java.time.LocalDate;
import java.util.List;

/**
 * 积分服务接口
 */
public interface IntegralService {

    /**
     * 增加积分
     * @param userId 用户ID
     * @param amount 积分数量
     * @param earnChannel 获得途径
     * @param businessId 关联业务ID
     * @param description 业务描述
     * @return 积分记录
     */
    Integral addIntegral(String userId, Integer amount, String earnChannel,
                        Long businessId, String description);

    /**
     * 消费积分
     * @param userId 用户ID
     * @param amount 积分数量
     * @param spendChannel 消费途径
     * @param businessId 关联业务ID
     * @param description 业务描述
     * @return 积分记录列表
     */
    List<Integral> consumeIntegral(String userId, Integer amount, String spendChannel,
                                  Long businessId, String description);

    /**
     * 查询用户积分明细
     * @param userId 用户ID
     * @return 积分记录列表
     */
    List<Integral> getIntegralDetails(String userId);

    /**
     * 查询用户当前可用积分
     * @param userId 用户ID
     * @return 可用积分数量
     */
    Integer getAvailableIntegral(String userId);

    /**
     * 处理积分过期
     */
    void handleIntegralExpire();

    /**
     * 根据订单金额计算应获得的积分
     * @param orderAmount 订单金额
     * @return 应获得的积分数量
     */
    Integer calculateIntegralByOrderAmount(Double orderAmount);

    /**
     * 根据评论内容和是否带图计算应获得的积分
     * @param commentContent 评论内容
     * @param hasPicture 是否带图
     * @return 应获得的积分数量
     */
    Integer calculateIntegralByComment(String commentContent, boolean hasPicture);

    /**
     * 计算签到应获得的积分
     * @param consecutiveDays 连续签到天数
     * @return 应获得的积分数量
     */
    Integer calculateIntegralByCheckIn(Integer consecutiveDays);

    /**
     * 计算会员开通或续费应获得的积分
     * @param vipLevel VIP等级
     * @param months 开通/续费月数
     * @return 应获得的积分数量
     */
    Integer calculateIntegralByVip(String vipLevel, Integer months);

    /**
     * 计算活动奖励应获得的积分
     * @param activityType 活动类型
     * @return 应获得的积分数量
     */
    Integer calculateIntegralByActivity(String activityType);

    /**
     * 计算生日奖励应获得的积分
     * @param birthday 用户生日
     * @param currentDate 当前日期
     * @return 应获得的积分数量
     */
    Integer calculateIntegralByBirthday(LocalDate birthday, LocalDate currentDate);

    /**
     * 根据订单金额和积分数量计算抵扣后的金额
     * @param orderAmount 订单金额
     * @param integralAmount 积分数量
     * @return 抵扣后的金额
     */
    Double calculateDeductedAmount(Double orderAmount, Integer integralAmount);

    /**
     * 用户签到
     * @param userId 用户ID
     * @return 签到获得的积分数量，如果今天已签到返回0
     */
    Integer signIn(String userId);

    /**
     * 检查用户今天是否已签到
     * @param userId 用户ID
     * @return 是否已签到
     */
    boolean isSignedInToday(String userId);

    /**
     * 处理用户生日积分发放
     * @param userId 用户ID
     * @return 获得的积分总数
     */
    Integer handleBirthdayIntegral(String userId);

    /**
     * 检查用户今天是否已获得生日月度积分
     * @param userId 用户ID
     * @return 是否已获得
     */
    boolean hasEarnedBirthdayMonthlyIntegralToday(String userId);

    /**
     * 检查用户今天是否已获得生日当天积分
     * @param userId 用户ID
     * @return 是否已获得
     */
    boolean hasEarnedBirthdayIntegralToday(String userId);

    /**
     * 判断用户是否可以领取生日积分
     * @param userId 用户ID
     * @return 是否可以领取
     */
    boolean canClaimBirthdayIntegral(String userId);

    /**
     * 用户手动领取生日积分
     * @param userId 用户ID
     * @return 领取的积分数量
     */
    Integer claimBirthdayIntegral(String userId);

    /**
     * 获取用户积分统计信息（总数、可用、过期等）
     * @param userId 用户ID
     * @return 积分统计信息
     */
    IntegralStatistics getIntegralStatistics(String userId);

    /**
     * 获取用户可用积分的详细信息列表
     * @param userId 用户ID
     * @return 可用积分详细信息列表
     */
    List<Integral> getAvailableIntegralDetails(String userId);

    /**
     * 积分统计信息类
     */
    class IntegralStatistics {
        private Integer totalIntegral;
        private Integer availableIntegral;
        private Integer expiredIntegral;
        private Integer usedIntegral;

        public IntegralStatistics() {
        }

        public IntegralStatistics(Integer totalIntegral, Integer availableIntegral,
                                 Integer expiredIntegral, Integer usedIntegral) {
            this.totalIntegral = totalIntegral;
            this.availableIntegral = availableIntegral;
            this.expiredIntegral = expiredIntegral;
            this.usedIntegral = usedIntegral;
        }

        public Integer getTotalIntegral() {
            return totalIntegral;
        }

        public void setTotalIntegral(Integer totalIntegral) {
            this.totalIntegral = totalIntegral;
        }

        public Integer getAvailableIntegral() {
            return availableIntegral;
        }

        public void setAvailableIntegral(Integer availableIntegral) {
            this.availableIntegral = availableIntegral;
        }

        public Integer getExpiredIntegral() {
            return expiredIntegral;
        }

        public void setExpiredIntegral(Integer expiredIntegral) {
            this.expiredIntegral = expiredIntegral;
        }

        public Integer getUsedIntegral() {
            return usedIntegral;
        }

        public void setUsedIntegral(Integer usedIntegral) {
            this.usedIntegral = usedIntegral;
        }
    }
}
