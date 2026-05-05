package team.tjusw.elm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.tjusw.elm.mapper.IntegralMapper;
import team.tjusw.elm.po.Integral;
import team.tjusw.elm.service.IntegralService;
import team.tjusw.elm.service.PromotionDayService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 促销日服务实现类
 */
@Service
@Transactional
public class PromotionDayServiceImpl implements PromotionDayService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionDayServiceImpl.class);

    @Autowired
    private IntegralService integralService;

    @Autowired
    private IntegralMapper integralMapper;

    // 促销日配置：每周六为促销日
    private static final DayOfWeek PROMOTION_DAY_OF_WEEK = DayOfWeek.SATURDAY;

    // 红包金额配置
    private static final int RED_PACKET_MIN_AMOUNT = 5;
    private static final int RED_PACKET_MAX_AMOUNT = 20;

    @Override
    public boolean isTodayPromotionDay() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        return dayOfWeek == PROMOTION_DAY_OF_WEEK;
    }

    @Override
    public boolean canClaimRedPacket(String userId) {
        // 检查今天是否是促销日
        if (!isTodayPromotionDay()) {
            logger.info("今天不是促销日，用户 {} 无法领取红包", userId);
            return false;
        }

        // 检查今天是否已经领���过红包
        LocalDate today = LocalDate.now();
        List<Integral> todaysRecords = integralMapper.selectByUserIdOrderByCreateTimeDesc(userId);

        for (Integral integral : todaysRecords) {
            if (Integral.CHANNEL_PACKET.equals(integral.getChannel()) &&
                integral.getCreateTime() != null &&
                integral.getCreateTime().toLocalDate().equals(today)) {
                logger.info("用户 {} 今天已经领取过红包", userId);
                return false;
            }
        }

        return true;
    }

    @Override
    public Integer claimRedPacket(String userId) {
        // 检查是否可以领取
        if (!canClaimRedPacket(userId)) {
            return 0;
        }

        // 生成随机红包金额
        int amount = generateRandomAmount();

        // 发放红包积分
        try {
            integralService.addIntegral(userId, amount, Integral.CHANNEL_PACKET,
                    null, "促销日红包奖励");

            logger.info("用户 {} 成功领取促销日红包，获得 {} 积分", userId, amount);
            return amount;
        } catch (Exception e) {
            logger.error("用户 {} 领取红包失败: {}", userId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public List<Object> getRedPacketRecords(String userId) {
        // 查询所有积分记录
        List<Integral> allIntegrals = integralMapper.selectByUserIdOrderByCreateTimeDesc(userId);

        // 筛选出红包相关的记录
        List<Object> records = new ArrayList<>();
        for (Integral integral : allIntegrals) {
            if (Integral.CHANNEL_PACKET.equals(integral.getChannel())) {
                records.add(new Object() {
                    public final Integer id = integral.getId();
                    public final Integer amount = integral.getAmount();
                    public final String channel = integral.getChannel();
                    public final String description = integral.getDescription();
                    public final java.time.LocalDateTime createTime = integral.getCreateTime();
                    public final String status = integral.getStatus();
                });
            }
        }

        return records;
    }

    /**
     * 生成随机红包金额
     * @return 红包金额（范围：RED_PACKET_MIN_AMOUNT 到 RED_PACKET_MAX_AMOUNT）
     */
    private int generateRandomAmount() {
        // 简单的随机算法，返回固定范围之间的随机金额
        return RED_PACKET_MIN_AMOUNT + (int)(Math.random() * (RED_PACKET_MAX_AMOUNT - RED_PACKET_MIN_AMOUNT + 1));
    }
}
