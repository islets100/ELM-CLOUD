package team.tjusw.elm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.tjusw.elm.service.PromotionDayService;
import team.tjusw.elm.po.CommonResult;

import java.time.LocalDate;
import java.util.List;

/**
 * 促销日管理Controller
 */
@RestController
@RequestMapping("/promotion-day")
public class PromotionDayController {

    @Autowired
    private PromotionDayService promotionDayService;

    /**
     * 检查今天是否是促销日
     */
    @GetMapping("/today")
    public CommonResult<Boolean> isTodayPromotionDay() {
        try {
            boolean isPromotionDay = promotionDayService.isTodayPromotionDay();
            return new CommonResult<>(200, "成功检查今天是否是促销日", isPromotionDay);
        } catch (Exception e) {
            return new CommonResult<>(500, "检查促销日失败: " + e.getMessage(), false);
        }
    }

    /**
     * 检查是否可以领取红包
     */
    @GetMapping("/red-packet/check")
    public CommonResult<Boolean> checkCanClaimRedPacket(@RequestParam("userId") String userId) {
        try {
            boolean canClaim = promotionDayService.canClaimRedPacket(userId);
            return new CommonResult<>(200, "成功检查是否可以领取红包", canClaim);
        } catch (Exception e) {
            return new CommonResult<>(500, "检查红包领取资格失败: " + e.getMessage(), false);
        }
    }

    /**
     * 领取红包
     */
    @PostMapping("/red-packet/claim")
    public CommonResult<Integer> claimRedPacket(@RequestParam("userId") String userId) {
        try {
            Integer amount = promotionDayService.claimRedPacket(userId);
            if (amount == 0) {
                return new CommonResult<>(400, "今天已经领取过红包，或者今天不是促销日", 0);
            }
            return new CommonResult<>(200, "成功领取红包", amount);
        } catch (Exception e) {
            return new CommonResult<>(500, "领取红包失败: " + e.getMessage(), 0);
        }
    }

    /**
     * 获取红包领取记录
     */
    @GetMapping("/red-packet/records")
    public CommonResult<List<Object>> getRedPacketRecords(@RequestParam("userId") String userId) {
        try {
            List<Object> records = promotionDayService.getRedPacketRecords(userId);
            return new CommonResult<>(200, "成功获取红包领取记录", records);
        } catch (Exception e) {
            return new CommonResult<>(500, "获取红包领取记录失败: " + e.getMessage(), null);
        }
    }
}
