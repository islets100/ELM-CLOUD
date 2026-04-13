package team.tjusw.elm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import team.tjusw.elm.po.Integral;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 积分服务测试类
 * 测试积分系统的核心业务逻辑
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class IntegralServiceTest {

    @Autowired
    private IntegralService integralService;

    private static final Long TEST_USER_ID = 1001L;

    @Test
    @DisplayName("测试增加积分")
    void testAddIntegral() {
        // 执行：增加100积分
        Integral integral = integralService.addIntegral(
                TEST_USER_ID,
                100,
                Integral.CHANNEL_CONSUMPTION,
                1001L,
                "订单消费获得积分"
        );

        // 验证
        assertNotNull(integral);
        assertEquals(TEST_USER_ID, integral.getUserId());
        assertEquals(100, integral.getAmount());
        assertEquals(Integral.TYPE_INCREASE, integral.getType());
        assertEquals(Integral.STATUS_AVAILABLE, integral.getStatus());
        assertEquals(Integral.CHANNEL_CONSUMPTION, integral.getChannel());
        assertNotNull(integral.getExpireTime());
    }

    @Test
    @DisplayName("测试消费积分 - 成功场景")
    void testConsumeIntegral_Success() {
        // 准备：先添加200积分
        integralService.addIntegral(TEST_USER_ID, 200, Integral.CHANNEL_CONSUMPTION, null, "初始积分");

        // 执行：消费100积分
        List<Integral> consumedIntegrals = integralService.consumeIntegral(
                TEST_USER_ID,
                100,
                Integral.CHANNEL_EXCHANGE,
                2001L,
                "兑换商品"
        );

        // 验证
        assertNotNull(consumedIntegrals);
        assertFalse(consumedIntegrals.isEmpty());

        // 验证剩余积分
        Integer remaining = integralService.getAvailableIntegral(TEST_USER_ID);
        assertEquals(100, remaining);
    }

    @Test
    @DisplayName("测试消费积分 - 积分不足")
    void testConsumeIntegral_InsufficientPoints() {
        // 准备：只添加50积分
        integralService.addIntegral(TEST_USER_ID, 50, Integral.CHANNEL_CONSUMPTION, null, "初始积分");

        // 执行 & 验证：尝试消费100积分应该抛出异常
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            integralService.consumeIntegral(
                    TEST_USER_ID,
                    100,
                    Integral.CHANNEL_EXCHANGE,
                    2001L,
                    "兑换商品"
            );
        });

        assertTrue(exception.getMessage().contains("积分不足"));
    }

    @Test
    @DisplayName("测试获取可用积分")
    void testGetAvailableIntegral() {
        // 准备：添加300积分
        integralService.addIntegral(TEST_USER_ID, 300, Integral.CHANNEL_CONSUMPTION, null, "初始积分");

        // 执行
        Integer availableIntegral = integralService.getAvailableIntegral(TEST_USER_ID);

        // 验证
        assertEquals(300, availableIntegral);
    }

    @Test
    @DisplayName("测试用户签到 - 首次签到")
    void testSignIn_FirstTime() {
        // 使用不同的用户ID避免冲突
        Long newUser = 1002L;

        // 执行
        Integer pointsAwarded = integralService.signIn(newUser);

        // 验证
        assertEquals(10, pointsAwarded); // 签到固定10积分

        // 验证签到记录
        boolean isSignedIn = integralService.isSignedInToday(newUser);
        assertTrue(isSignedIn);

        // 验证积分增加
        Integer availableIntegral = integralService.getAvailableIntegral(newUser);
        assertEquals(10, availableIntegral);
    }

    @Test
    @DisplayName("测试根据订单金额计算积分")
    void testCalculateIntegralByOrderAmount() {
        // 执行 & 验证
        assertEquals(100, integralService.calculateIntegralByOrderAmount(100.0));
        assertEquals(158, integralService.calculateIntegralByOrderAmount(158.5));
    }

    @Test
    @DisplayName("测试根据评论计算积分 - 无图")
    void testCalculateIntegralByComment_WithoutPicture() {
        // 执行 & 验证
        assertEquals(0, integralService.calculateIntegralByComment("短评论", false)); // 少于15字
        assertEquals(5, integralService.calculateIntegralByComment("这是一条超过15个字的评论内容", false));
    }

    @Test
    @DisplayName("测试根据评论计算积分 - 有图")
    void testCalculateIntegralByComment_WithPicture() {
        // 执行 & 验证
        assertEquals(0, integralService.calculateIntegralByComment("短评论", true)); // 少于15字
        assertEquals(10, integralService.calculateIntegralByComment("这是一条超过15个字的评论内容", true));
    }

    @Test
    @DisplayName("测试计算积分抵扣金额")
    void testCalculateDeductedAmount() {
        // 执行 & 验证：100积分 = 1元
        Double deducted = integralService.calculateDeductedAmount(100.0, 100);
        assertEquals(99.0, deducted); // 100元订单 - 100积分(1元) = 99元

        // 测试积分数量超过订单金额
        Double deducted2 = integralService.calculateDeductedAmount(50.0, 10000);
        assertEquals(0.0, deducted2); // 最多抵扣50元
    }

    @Test
    @DisplayName("测试获取积分明细")
    void testGetIntegralDetails() {
        // 准备：添加几笔积分记录
        integralService.addIntegral(TEST_USER_ID, 100, Integral.CHANNEL_CONSUMPTION, 1001L, "订单1");
        integralService.addIntegral(TEST_USER_ID, 50, Integral.CHANNEL_COMMENT, 1002L, "评论奖励");

        // 执行
        List<Integral> integrals = integralService.getIntegralDetails(TEST_USER_ID);

        // 验证
        assertNotNull(integrals);
        assertTrue(integrals.size() >= 2);
    }

    @Test
    @DisplayName("测试获取积分统计信息")
    void testGetIntegralStatistics() {
        // 准备：添加积分
        integralService.addIntegral(TEST_USER_ID, 300, Integral.CHANNEL_CONSUMPTION, null, "初始积分");

        // 执行
        IntegralService.IntegralStatistics statistics = integralService.getIntegralStatistics(TEST_USER_ID);

        // 验证
        assertNotNull(statistics);
        assertEquals(300, statistics.getTotalIntegral());
        assertEquals(300, statistics.getAvailableIntegral());
        assertEquals(0, statistics.getUsedIntegral());
        assertEquals(0, statistics.getExpiredIntegral());
    }

    @Test
    @DisplayName("测试VIP积分计算")
    void testCalculateIntegralByVip() {
        // 执行 & 验证
        assertEquals(50, integralService.calculateIntegralByVip("BASIC", 1)); // 基础会员1个月
        assertEquals(70, integralService.calculateIntegralByVip("GOLD", 1)); // 黄金会员1个月
        assertEquals(100, integralService.calculateIntegralByVip("PLATINUM", 1)); // 白金会员1个月
        assertEquals(150, integralService.calculateIntegralByVip("DIAMOND", 1)); // 钻石会员1个月
    }

    @Test
    @DisplayName("测试活动积分计算")
    void testCalculateIntegralByActivity() {
        // 执行 & 验证
        assertEquals(100, integralService.calculateIntegralByActivity("NEW_USER")); // 新用户活动
        assertEquals(20, integralService.calculateIntegralByActivity("HOLIDAY")); // 节日活动
        assertEquals(0, integralService.calculateIntegralByActivity("UNKNOWN")); // 未知活动
    }

    @Test
    @DisplayName("测试生日积分计算 - 生日当天")
    void testCalculateIntegralByBirthday_Birthday() {
        // 准备
        LocalDate birthday = LocalDate.of(1990, 4, 13); // 4月13日生日
        LocalDate currentDate = LocalDate.of(2026, 4, 13); // 当前是4月13日

        // 执行
        Integer points = integralService.calculateIntegralByBirthday(birthday, currentDate);

        // 验证：生日当天100积分
        assertEquals(100, points);
    }

    @Test
    @DisplayName("测试生日积分计算 - 生日当月")
    void testCalculateIntegralByBirthday_BirthdayMonth() {
        // 准备
        LocalDate birthday = LocalDate.of(1990, 4, 13); // 4月13日生日
        LocalDate currentDate = LocalDate.of(2026, 4, 15); // 当前是4月15日

        // 执行
        Integer points = integralService.calculateIntegralByBirthday(birthday, currentDate);

        // 验证：生日当月但不是当天，10积分
        assertEquals(10, points);
    }

    @Test
    @DisplayName("测试生日积分计算 - 非生日月")
    void testCalculateIntegralByBirthday_NotBirthdayMonth() {
        // 准备
        LocalDate birthday = LocalDate.of(1990, 4, 13); // 4月13日生日
        LocalDate currentDate = LocalDate.of(2026, 5, 15); // 当前是5月15日

        // 执行
        Integer points = integralService.calculateIntegralByBirthday(birthday, currentDate);

        // 验证：不是生日月，0积分
        assertEquals(0, points);
    }

    @Test
    @DisplayName("测试连续签到积分计算")
    void testCalculateIntegralByCheckIn() {
        // 执行 & 验证
        assertEquals(1, integralService.calculateIntegralByCheckIn(1)); // 第1天
        assertEquals(1, integralService.calculateIntegralByCheckIn(6)); // 第6天
        assertEquals(10, integralService.calculateIntegralByCheckIn(7)); // 第7天
        assertEquals(1, integralService.calculateIntegralByCheckIn(8)); // 第8天
        assertEquals(50, integralService.calculateIntegralByCheckIn(30)); // 第30天
    }

    @Test
    @DisplayName("测试获取可用积分详细信息")
    void testGetAvailableIntegralDetails() {
        // 准备：添加积分
        integralService.addIntegral(TEST_USER_ID, 100, Integral.CHANNEL_CONSUMPTION, null, "订单积分");

        // 执行
        List<Integral> details = integralService.getAvailableIntegralDetails(TEST_USER_ID);

        // 验证
        assertNotNull(details);
        assertFalse(details.isEmpty());

        // 验证只返回INCREASE类型的记录
        for (Integral detail : details) {
            assertEquals(Integral.TYPE_INCREASE, detail.getType());
        }
    }
}
