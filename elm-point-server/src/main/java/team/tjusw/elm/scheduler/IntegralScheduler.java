package team.tjusw.elm.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.tjusw.elm.service.IntegralService;

/**
 * 积分定时任务
 */
@Component
public class IntegralScheduler {

    private static final Logger logger = LoggerFactory.getLogger(IntegralScheduler.class);

    @Autowired
    private IntegralService integralService;

    /**
     * 处理积分过期任务
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void handleIntegralExpire() {
        logger.info("开始执行积分过期处理任务");
        try {
            integralService.handleIntegralExpire();
            logger.info("积分过期处理任务执行完成");
        } catch (Exception e) {
            logger.error("积分过期处理任务执行失败", e);
        }
    }

    /**
     * 处理生日积分发放任务
     * 每天凌晨2点执行
     * 注意：此任务需要获取所有用户列表，然后逐个处理生日积分
     * 由于需要调用用户服务，这里暂时只记录日志
     * 实际项目中应该通过 Feign 调用用户服务获取用户列表
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void handleBirthdayIntegral() {
        logger.info("开始执行生日积分发放任务");
        try {
            // TODO: 需要实现从用户服务获取所有用户列表的逻辑
            // List<User> users = userFeignClient.getAllUsers();
            // for (User user : users) {
            //     integralService.handleBirthdayIntegral(user.getId());
            // }

            logger.info("生日积分发放任务执行完成");
        } catch (Exception e) {
            logger.error("生日积分发放任务执行失败", e);
        }
    }
}
