package team.tjusw.elm.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import team.tjusw.elm.po.Integral;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 积分 Mapper 测试类
 * 测试数据访问层
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class IntegralMapperTest {

    @Autowired
    private IntegralMapper integralMapper;

    private static final Long TEST_USER_ID = 3001L;

    @Test
    @DisplayName("测试插入积分记录")
    void testInsert() {
        Integral integral = new Integral(
                TEST_USER_ID,
                200,
                Integral.TYPE_INCREASE,
                Integral.STATUS_AVAILABLE,
                Integral.CHANNEL_ACTIVITY,
                LocalDateTime.now().plusDays(30),
                null,
                "活动奖励"
        );

        int result = integralMapper.insert(integral);

        assertEquals(1, result);
        assertNotNull(integral.getId());
    }

    @Test
    @DisplayName("测试根据ID查询积分记录")
    void testSelectById() {
        // 先插入一条记录
        Integral integral = new Integral(
                TEST_USER_ID,
                150,
                Integral.TYPE_INCREASE,
                Integral.STATUS_AVAILABLE,
                Integral.CHANNEL_CHECK_IN,
                LocalDateTime.now().plusDays(30),
                null,
                "签到奖励"
        );
        integralMapper.insert(integral);

        // 查询
        Integral found = integralMapper.selectById(integral.getId());

        assertNotNull(found);
        assertEquals(integral.getUserId(), found.getUserId());
        assertEquals(integral.getAmount(), found.getAmount());
    }

    @Test
    @DisplayName("测试根据用户ID查询积分记录")
    void testSelectByUserIdOrderByCreateTimeDesc() {
        // 插入测试数据
        integralMapper.insert(new Integral(TEST_USER_ID, 100, Integral.TYPE_INCREASE,
                Integral.STATUS_AVAILABLE, Integral.CHANNEL_CONSUMPTION,
                LocalDateTime.now().plusDays(30), 1001L, "订单1"));

        integralMapper.insert(new Integral(TEST_USER_ID, 50, Integral.TYPE_INCREASE,
                Integral.STATUS_AVAILABLE, Integral.CHANNEL_COMMENT,
                LocalDateTime.now().plusDays(30), 1002L, "评论"));

        List<Integral> integrals = integralMapper.selectByUserIdOrderByCreateTimeDesc(TEST_USER_ID);

        assertNotNull(integrals);
        assertFalse(integrals.isEmpty());
        assertTrue(integrals.size() >= 2);
    }

    @Test
    @DisplayName("测试根据用户ID和状态查询积分记录")
    void testSelectByUserIdAndStatusOrderByExpireTimeAsc() {
        // 插入测试数据
        integralMapper.insert(new Integral(TEST_USER_ID, 100, Integral.TYPE_INCREASE,
                Integral.STATUS_AVAILABLE, Integral.CHANNEL_CONSUMPTION,
                LocalDateTime.now().plusDays(30), 1001L, "订单"));

        List<Integral> availableIntegrals = integralMapper.selectByUserIdAndStatusOrderByExpireTimeAsc(
                TEST_USER_ID, Integral.STATUS_AVAILABLE);

        assertNotNull(availableIntegrals);
        assertFalse(availableIntegrals.isEmpty());

        // 验证所有记录状态都是 AVAILABLE
        for (Integral integral : availableIntegrals) {
            assertEquals(Integral.STATUS_AVAILABLE, integral.getStatus());
        }
    }

    @Test
    @DisplayName("测试更新积分记录")
    void testUpdateById() {
        // 先插入一条记录
        Integral integral = new Integral(
                TEST_USER_ID,
                100,
                Integral.TYPE_INCREASE,
                Integral.STATUS_AVAILABLE,
                Integral.CHANNEL_CONSUMPTION,
                LocalDateTime.now().plusDays(30),
                null,
                "测试积分"
        );
        integralMapper.insert(integral);

        // 更新状态
        integral.setStatus(Integral.STATUS_USED);
        integral.setRemainingAmount(50);

        int result = integralMapper.updateById(integral);

        assertEquals(1, result);

        // 验证更新
        Integral updated = integralMapper.selectById(integral.getId());
        assertEquals(Integral.STATUS_USED, updated.getStatus());
        assertEquals(50, updated.getRemainingAmount());
    }

    @Test
    @DisplayName("测试查询用户可用积分总和")
    void testSumAmountByUserIdAndStatusAndExpireTimeAfter() {
        // 插入测试数据
        integralMapper.insert(new Integral(TEST_USER_ID, 100, Integral.TYPE_INCREASE,
                Integral.STATUS_AVAILABLE, Integral.CHANNEL_CONSUMPTION,
                LocalDateTime.now().plusDays(30), null, "积分1"));

        integralMapper.insert(new Integral(TEST_USER_ID, 50, Integral.TYPE_INCREASE,
                Integral.STATUS_AVAILABLE, Integral.CHANNEL_CONSUMPTION,
                LocalDateTime.now().plusDays(30), null, "积分2"));

        Integer total = integralMapper.sumAmountByUserIdAndStatusAndExpireTimeAfter(
                TEST_USER_ID, Integral.STATUS_AVAILABLE, LocalDateTime.now());

        assertNotNull(total);
        assertTrue(total > 0);
        assertEquals(150, total);
    }

    @Test
    @DisplayName("测试多用户数据隔离")
    void testMultiUserDataIsolation() {
        Long user1 = 3001L;
        Long user2 = 3002L;

        // 为两个用户分别插入积分
        integralMapper.insert(new Integral(user1, 100, Integral.TYPE_INCREASE,
                Integral.STATUS_AVAILABLE, Integral.CHANNEL_CONSUMPTION,
                LocalDateTime.now().plusDays(30), null, "用户1积分"));

        integralMapper.insert(new Integral(user2, 200, Integral.TYPE_INCREASE,
                Integral.STATUS_AVAILABLE, Integral.CHANNEL_CONSUMPTION,
                LocalDateTime.now().plusDays(30), null, "用户2积分"));

        // 验证两个用户的数据是隔离的
        List<Integral> user1Integrals = integralMapper.selectByUserIdOrderByCreateTimeDesc(user1);
        List<Integral> user2Integrals = integralMapper.selectByUserIdOrderByCreateTimeDesc(user2);

        assertEquals(1, user1Integrals.size());
        assertEquals(1, user2Integrals.size());
        assertEquals(100, user1Integrals.get(0).getAmount());
        assertEquals(200, user2Integrals.get(0).getAmount());
    }
}
