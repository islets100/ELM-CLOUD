package team.tjusw.elm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import team.tjusw.elm.service.IntegralService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 积分控制器测试类
 * 测试积分 REST API 接口（简化版）
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class IntegralControllerTest {

    @Autowired
    private IntegralService integralService;

    private static final String TEST_USER_ID = "user2001";

    @Test
    @DisplayName("测试获取用户可用积分 API")
    void testGetAvailableIntegral() {
        // 准备：添加初始积分
        integralService.addIntegral(TEST_USER_ID, 500, "CONSUMPTION", null, "初始积分");

        // 执行：通过 Service 模拟 API 调用
        Integer availableIntegral = integralService.getAvailableIntegral(TEST_USER_ID);

        // 验证
        assertNotNull(availableIntegral);
        assertEquals(500, availableIntegral);
    }

    @Test
    @DisplayName("测试手动添加积分 API")
    void testAddIntegral() {
        // 执行
        team.tjusw.elm.po.Integral integral = integralService.addIntegral(TEST_USER_ID, 100, "ACTIVITY", null, "活动奖励");

        // 验证
        assertNotNull(integral);
        assertEquals(TEST_USER_ID, integral.getUserId());
        assertEquals(100, integral.getAmount());
    }

    @Test
    @DisplayName("测试消费积分 API - 成功")
    void testConsumeIntegral_Success() {
        // 准备
        integralService.addIntegral(TEST_USER_ID, 500, "CONSUMPTION", null, "初始积分");

        // 执行
        java.util.List<team.tjusw.elm.po.Integral> consumedIntegrals = integralService.consumeIntegral(TEST_USER_ID, 50, "EXCHANGE", null, "兑换");

        // 验证
        assertNotNull(consumedIntegrals);
        assertFalse(consumedIntegrals.isEmpty());

        // 验证剩余积分
        Integer remaining = integralService.getAvailableIntegral(TEST_USER_ID);
        assertEquals(450, remaining);
    }

    @Test
    @DisplayName("测试用户签到 API - 首次签到")
    void testSignIn_FirstTime() {
        String newUser = "user3001";

        // 执行
        Integer pointsAwarded = integralService.signIn(newUser);

        // 验证
        assertEquals(10, pointsAwarded);
        assertTrue(integralService.isSignedInToday(newUser));
    }

    @Test
    @DisplayName("测试用户签到 API - 重复签到")
    void testSignIn_AlreadySignedIn() {
        // 第一次签到
        integralService.signIn(TEST_USER_ID);

        // 第二次签到
        Integer pointsAwarded = integralService.signIn(TEST_USER_ID);

        // 验证：应该返回0
        assertEquals(0, pointsAwarded);
    }

    @Test
    @DisplayName("测试评论获取积分 API - 不满足条件")
    void testCommentEarnIntegral_NotEligible() {
        // 执行
        Integer points = integralService.calculateIntegralByComment("短评论", false);

        // 验证
        assertEquals(0, points);
    }

    @Test
    @DisplayName("测试评论获取积分 API - 满足条件无图")
    void testCommentEarnIntegral_EligibleNoPicture() {
        // 执行
        Integer points = integralService.calculateIntegralByComment("这是一条超过十五个字的评论内容", false);

        // 验证
        assertEquals(5, points);
    }

    @Test
    @DisplayName("测试评论获取积分 API - 满足条件有图")
    void testCommentEarnIntegral_EligibleWithPicture() {
        // 执行
        Integer points = integralService.calculateIntegralByComment("这是一条超过十五个字的评论内容", true);

        // 验证
        assertEquals(10, points);
    }

    @Test
    @DisplayName("测试计算积分抵扣 API")
    void testCalculateDeduction() {
        // 执行
        Double deducted = integralService.calculateDeductedAmount(100.0, 100);

        // 验证
        assertEquals(99.0, deducted);
    }

    @Test
    @DisplayName("测试获取积分统计 API")
    void testGetIntegralStatistics() {
        // 准备
        integralService.addIntegral(TEST_USER_ID, 300, "CONSUMPTION", null, "初始积分");

        // 执行
        IntegralService.IntegralStatistics statistics = integralService.getIntegralStatistics(TEST_USER_ID);

        // 验证
        assertNotNull(statistics);
        assertEquals(300, statistics.getTotalIntegral());
        assertEquals(300, statistics.getAvailableIntegral());
    }

    @Test
    @DisplayName("测试根据订单发放积分 API")
    void testEarnIntegralByOrder() {
        // 执行
        Integer points = integralService.calculateIntegralByOrderAmount(158.5);

        // 验证
        assertEquals(158, points);
    }

    @Test
    @DisplayName("测试获取可用积分详细信息 API")
    void testGetAvailableIntegralDetails() {
        // 准备
        integralService.addIntegral(TEST_USER_ID, 100, "CONSUMPTION", null, "订单积分");

        // 执行
        java.util.List<team.tjusw.elm.po.Integral> details = integralService.getAvailableIntegralDetails(TEST_USER_ID);

        // 验证
        assertNotNull(details);
        assertFalse(details.isEmpty());

        // 验证只返回INCREASE类型的记录
        for (team.tjusw.elm.po.Integral detail : details) {
            assertEquals(team.tjusw.elm.po.Integral.TYPE_INCREASE, detail.getType());
        }
    }

    @Test
    @DisplayName("测试积分渠道验证")
    void testIntegralChannelValidation() {
        // 测试各种渠道
        assertDoesNotThrow(() -> {
            integralService.addIntegral(TEST_USER_ID, 10, team.tjusw.elm.po.Integral.CHANNEL_CHECK_IN, null, "签到");
            integralService.addIntegral(TEST_USER_ID, 20, team.tjusw.elm.po.Integral.CHANNEL_COMMENT, null, "评论");
            integralService.addIntegral(TEST_USER_ID, 30, team.tjusw.elm.po.Integral.CHANNEL_CONSUMPTION, null, "消费");
        });
    }

    @Test
    @DisplayName("测试积分过期处理 API")
    void testHandleIntegralExpire() {
        // 这个测试主要验证方法可以正常调用
        assertDoesNotThrow(() -> {
            integralService.handleIntegralExpire();
        });
    }
}
