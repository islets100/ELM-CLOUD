package team.tjusw.elm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.tjusw.elm.mapper.BirthdayIntegralRecordMapper;
import team.tjusw.elm.mapper.IntegralMapper;
import team.tjusw.elm.mapper.SignInRecordMapper;
import team.tjusw.elm.po.BirthdayIntegralRecord;
import team.tjusw.elm.po.Integral;
import team.tjusw.elm.po.SignInRecord;
import team.tjusw.elm.service.IntegralService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 积分服务实现类
 */
@Service
@Transactional
public class IntegralServiceImpl implements IntegralService {

    private static final Logger logger = LoggerFactory.getLogger(IntegralServiceImpl.class);

    @Autowired
    private IntegralMapper integralMapper;

    @Autowired
    private SignInRecordMapper signInRecordMapper;

    @Autowired
    private BirthdayIntegralRecordMapper birthdayIntegralRecordMapper;

    // 积分有效期：默认30天
    private static final int INTEGRAL_EXPIRE_DAYS = 30;
    // 消费积分有效期：1年
    private static final int CONSUMPTION_EXPIRE_DAYS = 365;

    // 签到积分：固定10积分，有效期1年
    private static final int SIGN_IN_INTEGRAL = 10;
    private static final int SIGN_IN_EXPIRE_DAYS = 365;

    // 生日积分有效期：1年
    private static final int BIRTHDAY_INTEGRAL_EXPIRE_DAYS = 365;

    // 积分兑换比例：1元=1积分
    private static final double INTEGRAL_EXCHANGE_RATE = 1.0;

    // 积分抵扣比例：100积分=1元
    private static final double INTEGRAL_DEDUCTION_RATE = 100.0;

    // 评论积分规则
    private static final int COMMENT_MIN_LENGTH = 15;
    private static final int COMMENT_INTEGRAL = 5;
    private static final int COMMENT_WITH_PIC_INTEGRAL = 10;

    // 签到积分规则
    private static final int DAILY_CHECK_IN_INTEGRAL = 1;
    private static final int CONSECUTIVE_7_DAYS_INTEGRAL = 10;
    private static final int CONSECUTIVE_30_DAYS_INTEGRAL = 50;

    // VIP积分规则
    private static final int VIP_BASIC_INTEGRAL_PER_MONTH = 50;
    private static final int VIP_GOLD_BONUS = 20;
    private static final int VIP_PLATINUM_BONUS = 50;
    private static final int VIP_DIAMOND_BONUS = 100;

    // 活动积分规则
    private static final int NEW_USER_ACTIVITY_INTEGRAL = 100;
    private static final int HOLIDAY_ACTIVITY_INTEGRAL = 20;

    // 生日积分规则
    private static final int BIRTHDAY_ACTIVITY_INTEGRAL_DAY = 100;
    private static final int BIRTHDAY_ACTIVITY_INTEGRAL_MONTH = 10;

    @Override
    public Integral addIntegral(Long userId, Integer amount, String earnChannel,
                               Long businessId, String description) {
        // 根据渠道决定有效期
        int expireDays = INTEGRAL_EXPIRE_DAYS;
        switch (earnChannel) {
            case Integral.CHANNEL_CHECK_IN:
                expireDays = SIGN_IN_EXPIRE_DAYS;
                break;
            case Integral.CHANNEL_BIRTHDAY:
                expireDays = BIRTHDAY_INTEGRAL_EXPIRE_DAYS;
                break;
            case Integral.CHANNEL_CONSUMPTION:
                expireDays = CONSUMPTION_EXPIRE_DAYS;
                break;
            default:
                break;
        }

        // 创建积分记录
        Integral integral = new Integral(userId, amount, Integral.TYPE_INCREASE, Integral.STATUS_AVAILABLE,
                                       earnChannel, LocalDateTime.now().plusDays(expireDays),
                                       businessId, description);

        integralMapper.insert(integral);
        logger.info("用户 {} 通过 {} 获得积分 {}，有效期至 {}",
                   userId, earnChannel, amount, integral.getExpireTime());

        return integral;
    }

    @Override
    public List<Integral> consumeIntegral(Long userId, Integer amount, String spendChannel,
                                          Long businessId, String description) {
        // 查询用户可用积分
        Integer availableIntegral = getAvailableIntegral(userId);
        if (availableIntegral < amount) {
            throw new IllegalArgumentException("积分不足");
        }

        // 获取可用积分记录（按过期时间升序，优先使用临近过期的积分）
        List<Integral> availableIntegrals = integralMapper.selectByUserIdAndStatusOrderByExpireTimeAsc(
                userId, Integral.STATUS_AVAILABLE);

        Integer remainingAmount = amount;

        // 逐个消耗积分，直到满足所需积分数量
        for (Integral integral : availableIntegrals) {
            if (remainingAmount <= 0) {
                break;
            }

            // 获取当前积分的实际可用金额（考虑之前可能已经部分消费）
            Integer integralAmount = integral.getAmount();
            Integer currentAvailable = (integral.getRemainingAmount() != null && integral.getRemainingAmount() > 0)
                ? integral.getRemainingAmount()
                : integralAmount;

            // 如果当前可用金额为0，跳过
            if (currentAvailable <= 0) {
                continue;
            }

            Integer consumeFromThis = Math.min(currentAvailable, remainingAmount);

            // 计算新的剩余金额
            Integer newRemaining = currentAvailable - consumeFromThis;

            // 更新原积分记录的状态和剩余金额
            if (newRemaining <= 0) {
                // 完全消耗：标记为已使用
                integral.setStatus(Integral.STATUS_USED);
                integral.setRemainingAmount(0);
            } else {
                // 部分消耗：保持可用状态，更新剩余金额
                integral.setRemainingAmount(newRemaining);
            }
            integralMapper.updateById(integral);

            remainingAmount -= consumeFromThis;
        }

        // 创建一条总的消费记录（负数记录）
        // 消费记录的过期时间设置为当前时间加100年（实际上消费记录不会过期）
        LocalDateTime expireTime = LocalDateTime.now().plusYears(100);
        Integral consumedIntegral = new Integral(userId, amount, Integral.TYPE_DECREASE,
                                               Integral.STATUS_USED, spendChannel,
                                               expireTime, businessId, description);
        integralMapper.insert(consumedIntegral);

        logger.info("用户 {} 消费积分 {}，通过 {}，剩余需消费 {}", userId, amount, spendChannel, remainingAmount);

        // 返回单条消费记录
        List<Integral> result = new ArrayList<>();
        result.add(consumedIntegral);
        return result;
    }

    @Override
    public List<Integral> getIntegralDetails(Long userId) {
        return integralMapper.selectByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public Integer getAvailableIntegral(Long userId) {
        // 获取所有可用且未过期的积分记录
        List<Integral> availableIntegrals = integralMapper.selectByUserIdAndStatusAndExpireTimeAfter(
                userId, Integral.STATUS_AVAILABLE, LocalDateTime.now());

        int total = 0;
        for (Integral integral : availableIntegrals) {
            // 优先使用剩余金额字段，如果没有设置则使用原始金额
            Integer availableAmount;
            if (integral.getRemainingAmount() != null) {
                // 已设置剩余金额（可能是部分消费后的结果）
                availableAmount = integral.getRemainingAmount();
            } else {
                // 未设置剩余金额（说明从未被消费过）
                availableAmount = integral.getAmount();
            }

            // 只累加大于0的金额
            if (availableAmount > 0) {
                total += availableAmount;
            }
        }

        logger.debug("用户 {} 当前可用积分：{}", userId, total);
        return total;
    }

    @Override
    public void handleIntegralExpire() {
        // 查询所有已过期但状态仍为可用的积分记录
        List<Integral> expiredIntegrals = integralMapper.selectByStatusAndExpireTimeBefore(
                Integral.STATUS_AVAILABLE, LocalDateTime.now());

        // 更新这些积分记录的状态为已过期
        for (Integral integral : expiredIntegrals) {
            integral.setStatus(Integral.STATUS_EXPIRED);
            integralMapper.updateById(integral);
        }
    }

    @Override
    public Integer calculateIntegralByOrderAmount(Double orderAmount) {
        // 按照订单金额计算积分，取整数
        return (int) Math.floor(orderAmount * INTEGRAL_EXCHANGE_RATE);
    }

    @Override
    public Integer calculateIntegralByComment(String commentContent, boolean hasPicture) {
        // 根据评论内容长度和是否带图计算积分
        if (commentContent == null || commentContent.length() < COMMENT_MIN_LENGTH) {
            return 0;
        }

        if (hasPicture) {
            return COMMENT_WITH_PIC_INTEGRAL;
        } else {
            return COMMENT_INTEGRAL;
        }
    }

    @Override
    public Integer calculateIntegralByCheckIn(Integer consecutiveDays) {
        // 根据连续签到天数计算积分
        if (consecutiveDays == null || consecutiveDays <= 0) {
            return DAILY_CHECK_IN_INTEGRAL;
        }

        if (consecutiveDays % 30 == 0) {
            return CONSECUTIVE_30_DAYS_INTEGRAL;
        } else if (consecutiveDays % 7 == 0) {
            return CONSECUTIVE_7_DAYS_INTEGRAL;
        } else {
            return DAILY_CHECK_IN_INTEGRAL;
        }
    }

    @Override
    public Integer calculateIntegralByVip(String vipLevel, Integer months) {
        // 根据VIP等级和开通/续费月数计算积分
        if (vipLevel == null || months == null || months <= 0) {
            return 0;
        }

        int baseIntegral = VIP_BASIC_INTEGRAL_PER_MONTH * months;
        int bonusIntegral = 0;

        switch (vipLevel.toUpperCase()) {
            case "GOLD":
                bonusIntegral = VIP_GOLD_BONUS * months;
                break;
            case "PLATINUM":
                bonusIntegral = VIP_PLATINUM_BONUS * months;
                break;
            case "DIAMOND":
                bonusIntegral = VIP_DIAMOND_BONUS * months;
                break;
            default:
                break;
        }

        return baseIntegral + bonusIntegral;
    }

    @Override
    public Integer calculateIntegralByBirthday(LocalDate birthday, LocalDate currentDate) {
        // 根据生日计算积分
        if (birthday == null || currentDate == null) {
            return 0;
        }

        int day = birthday.getDayOfMonth();
        int month = birthday.getMonthValue();

        int currentDay = currentDate.getDayOfMonth();
        int currentMonth = currentDate.getMonthValue();

        if (currentDay == day) {
            return BIRTHDAY_ACTIVITY_INTEGRAL_DAY;
        } else if (currentMonth == month) {
            return BIRTHDAY_ACTIVITY_INTEGRAL_MONTH;
        } else {
            return 0;
        }
    }

    @Override
    public Integer calculateIntegralByActivity(String activityType) {
        // 根据活动类型计算积分
        if (activityType == null) {
            return 0;
        }

        switch (activityType.toUpperCase()) {
            case "NEW_USER":
                return NEW_USER_ACTIVITY_INTEGRAL;
            case "HOLIDAY":
                return HOLIDAY_ACTIVITY_INTEGRAL;
            default:
                return 0;
        }
    }

    @Override
    public Double calculateDeductedAmount(Double orderAmount, Integer integralAmount) {
        // 根据订单金额和积分数量计算抵扣后的金额
        if (orderAmount == null || orderAmount <= 0) {
            return orderAmount;
        }

        if (integralAmount == null || integralAmount <= 0) {
            return orderAmount;
        }

        // 计算可抵扣的金额（积分数量 / 抵扣比例）
        Double deductibleAmount = integralAmount / INTEGRAL_DEDUCTION_RATE;

        // 抵扣后的金额不能小于0
        return Math.max(orderAmount - deductibleAmount, 0.0);
    }

    @Override
    public Integer signIn(Long userId) {
        LocalDate today = LocalDate.now();

        // 检查今天是否已签到
        if (isSignedInToday(userId)) {
            return 0; // 今天已签到，返回0
        }

        // 查询最近的签到记录，计算连续签到天数
        SignInRecord lastSignIn = signInRecordMapper.findTopByUserIdOrderBySignDateDesc(userId);
        int consecutiveDays = 1;

        if (lastSignIn != null) {
            LocalDate lastSignDate = lastSignIn.getSignDate();
            LocalDate yesterday = today.minusDays(1);

            // 如果最后一次签到是昨天，连续天数+1
            if (lastSignDate.equals(yesterday)) {
                consecutiveDays = lastSignIn.getConsecutiveDays() + 1;
            }
            // 如果最后一次签到不是昨天，重置连续天数
        }

        // 签到固定送10积分，有效期1年
        int pointsAwarded = SIGN_IN_INTEGRAL;

        // 发放积分（有效期1年）
        addIntegral(userId, pointsAwarded, Integral.CHANNEL_CHECK_IN, null,
                   "每日签到奖励（有效期1年）");

        // 保存签到记录
        SignInRecord signInRecord = new SignInRecord(userId, today, consecutiveDays, pointsAwarded);
        signInRecordMapper.insert(signInRecord);

        return pointsAwarded;
    }

    @Override
    public boolean isSignedInToday(Long userId) {
        LocalDate today = LocalDate.now();
        SignInRecord record = signInRecordMapper.findTodaySignIn(userId, today);
        return record != null;
    }

    @Override
    @Transactional
    public Integer handleBirthdayIntegral(Long userId) {
        LocalDate today = LocalDate.now();
        Integer totalEarned = 0;

        logger.info("开始处理用户 {} 的生日积分，当前日期：{}", userId, today);

        // 注意：这里需要从用户服务获取用户生日信息
        // 由于跨服务调用，这里暂时简化处理，假设用户生日已通过其他方式获取
        // 实际项目中应该通过 Feign 调用用户服务获取用户信息

        // TODO: 需要实现从用户服务获取用户生日的逻辑
        // User user = userFeignClient.getUserById(userId);
        // LocalDate birthday = user.getBirthday();

        // 暂时注释掉生日处理逻辑，等用户服务集成后再启用
        /*
        LocalDate birthday = getBirthdayFromUserService(userId);
        if (birthday == null) {
            logger.info("用户 {} 未设置生日，跳过积分发放", userId);
            return totalEarned;
        }

        logger.debug("用户 {} 的生日为：{}", userId, birthday);

        // 检查是否已经发放过今天的积分
        if (!birthdayIntegralRecordMapper.existsByUserIdAndRecordDate(userId, today)) {
            // 创建今天的积分记录
            BirthdayIntegralRecord record = new BirthdayIntegralRecord();
            record.setUserId(userId);
            record.setRecordDate(today);

            // 检查是否是生日月
            if (today.getMonth() == birthday.getMonth()) {
                logger.debug("用户 {} 当前月份 {} 是生日月", userId, today.getMonth());

                // 发放月度积分
                addIntegral(userId, BIRTHDAY_ACTIVITY_INTEGRAL_MONTH, Integral.CHANNEL_BIRTHDAY,
                           null, "生日当月每日获得" + BIRTHDAY_ACTIVITY_INTEGRAL_MONTH + "积分");

                record.setMonthlyEarned(true);
                record.setMonthlyPoints(BIRTHDAY_ACTIVITY_INTEGRAL_MONTH);
                totalEarned += BIRTHDAY_ACTIVITY_INTEGRAL_MONTH;

                // 检查是否是生日当天
                if (today.getDayOfMonth() == birthday.getDayOfMonth()) {
                    logger.debug("用户 {} 当前日期 {} 是生日当天", userId, today);

                    // 发放生日当天积分
                    addIntegral(userId, BIRTHDAY_ACTIVITY_INTEGRAL_DAY, Integral.CHANNEL_BIRTHDAY,
                               null, "生日当天额外获得" + BIRTHDAY_ACTIVITY_INTEGRAL_DAY + "积分");

                    record.setBirthdayEarned(true);
                    record.setBirthdayPoints(BIRTHDAY_ACTIVITY_INTEGRAL_DAY);
                    totalEarned += BIRTHDAY_ACTIVITY_INTEGRAL_DAY;
                }
            }

            // 保存积分记录
            birthdayIntegralRecordMapper.insert(record);
            logger.info("用户 {} 今日生日积分发放完成，共获得 {} 积分", userId, totalEarned);
        } else {
            logger.info("用户 {} 今日已发放生日积分，跳过重复发放", userId);
        }
        */

        return totalEarned;
    }

    @Override
    public boolean hasEarnedBirthdayMonthlyIntegralToday(Long userId) {
        LocalDate today = LocalDate.now();
        BirthdayIntegralRecord record = birthdayIntegralRecordMapper.findByUserIdAndRecordDate(userId, today);
        return record != null && record.getMonthlyEarned();
    }

    @Override
    public boolean hasEarnedBirthdayIntegralToday(Long userId) {
        LocalDate today = LocalDate.now();
        BirthdayIntegralRecord record = birthdayIntegralRecordMapper.findByUserIdAndRecordDate(userId, today);
        return record != null && record.getBirthdayEarned();
    }

    @Override
    public boolean canClaimBirthdayIntegral(Long userId) {
        // TODO: 需要实现从用户服务获取用户生日的逻辑
        // 这里暂时返回false，等用户服务集成后再实现
        return false;
    }

    @Override
    public Integer claimBirthdayIntegral(Long userId) {
        // TODO: 需要实现从用户服务获取用户生日的逻辑
        // 这里暂时返回0，等用户服务集成后再实现
        return 0;
    }

    @Override
    public IntegralStatistics getIntegralStatistics(Long userId) {
        // 查询所有积分记录
        List<Integral> allIntegrals = integralMapper.selectByUserIdOrderByCreateTimeDesc(userId);

        int totalEarned = 0;      // 累计获得的积分
        int totalUsed = 0;        // 累计使用的积分
        int availableIntegral = 0; // 当前可用积分
        int expiredIntegral = 0;   // 已过期积分

        LocalDateTime now = LocalDateTime.now();

        for (Integral integral : allIntegrals) {
            if (Integral.TYPE_INCREASE.equals(integral.getType())) {
                // 增加积分：累计获得
                totalEarned += integral.getAmount();

                if (Integral.STATUS_AVAILABLE.equals(integral.getStatus()) &&
                    integral.getExpireTime().isAfter(now)) {
                    // 可用积分：使用剩余金额（如果有）或原始金额
                    if (integral.getRemainingAmount() != null && integral.getRemainingAmount() > 0) {
                        availableIntegral += integral.getRemainingAmount();
                    } else {
                        availableIntegral += integral.getAmount();
                    }
                } else if (Integral.STATUS_EXPIRED.equals(integral.getStatus()) ||
                          integral.getExpireTime().isBefore(now)) {
                    // 已过期积分
                    expiredIntegral += integral.getAmount();
                }
            } else if (Integral.TYPE_DECREASE.equals(integral.getType())) {
                // 减少积分：累计使用
                totalUsed += integral.getAmount();
            }
        }

        // 总积分 = 累计获得 - 累计使用
        int totalIntegral = totalEarned - totalUsed;

        return new IntegralStatistics(
            totalIntegral, availableIntegral, expiredIntegral, totalUsed
        );
    }

    @Override
    public List<Integral> getAvailableIntegralDetails(Long userId) {
        // 获取所有积分记录
        List<Integral> allIntegrals = integralMapper.selectByUserIdOrderByCreateTimeDesc(userId);

        // 只返回INCREASE类型的记录（排除DECREASE消费记录）
        List<Integral> result = new ArrayList<>();
        for (Integral integral : allIntegrals) {
            if (Integral.TYPE_INCREASE.equals(integral.getType())) {
                result.add(integral);
            }
        }

        // 排序：未用完的 > 已用完的，然后按过期时间升序
        result.sort((a, b) -> {
            // 计算剩余金额
            Integer remainingA = (a.getRemainingAmount() != null) ? a.getRemainingAmount() : a.getAmount();
            Integer remainingB = (b.getRemainingAmount() != null) ? b.getRemainingAmount() : b.getAmount();

            // 判断是否用完
            boolean isUsedUpA = (remainingA == null || remainingA == 0) || Integral.STATUS_USED.equals(a.getStatus());
            boolean isUsedUpB = (remainingB == null || remainingB == 0) || Integral.STATUS_USED.equals(b.getStatus());

            // 判断是否过期
            boolean isExpiredA = Integral.STATUS_EXPIRED.equals(a.getStatus()) ||
                                (a.getExpireTime() != null && a.getExpireTime().isBefore(LocalDateTime.now()));
            boolean isExpiredB = Integral.STATUS_EXPIRED.equals(b.getStatus()) ||
                                (b.getExpireTime() != null && b.getExpireTime().isBefore(LocalDateTime.now()));

            // 第一优先级：未过期且未用完 > 未过期但已用完 > 已过期
            if (!isExpiredA && !isUsedUpA && (isExpiredB || isUsedUpB)) return -1;
            if ((isExpiredA || isUsedUpA) && !isExpiredB && !isUsedUpB) return 1;

            if (!isExpiredA && isUsedUpA && isExpiredB) return -1;
            if (isExpiredA && !isExpiredB && isUsedUpB) return 1;

            // 第二优先级：按过期时间升序排序
            if (a.getExpireTime() == null && b.getExpireTime() == null) return 0;
            if (a.getExpireTime() == null) return 1;
            if (b.getExpireTime() == null) return -1;
            return a.getExpireTime().compareTo(b.getExpireTime());
        });

        return result;
    }
}
