package team.tjusw.elm.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.tjusw.elm.dto.IntegralDTO;
import team.tjusw.elm.dto.IntegralStatisticsDTO;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.Integral;
import team.tjusw.elm.service.IntegralService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分管理Controller
 */
@RestController
@RequestMapping("/integral")
public class IntegralController {

    @Autowired
    private IntegralService integralService;

    /**
     * 获取当前用户可用积分
     * 注意：这里暂时使用 userId 参数，实际应该从 token 中获取
     */
    @GetMapping("/available/{userId}")
    public CommonResult<Integer> getAvailableIntegral(@PathVariable("userId") Long userId) {
        try {
            Integer availableIntegral = integralService.getAvailableIntegral(userId);
            return new CommonResult<>(200, "成功获取可用积分", availableIntegral);
        } catch (Exception e) {
            return new CommonResult<>(500, "获取可用积分失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取积分明细
     */
    @GetMapping("/details/{userId}")
    public CommonResult<List<IntegralDTO>> getIntegralDetails(@PathVariable("userId") Long userId) {
        try {
            List<Integral> integrals = integralService.getIntegralDetails(userId);
            List<IntegralDTO> dtos = integrals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new CommonResult<>(200, "成功获取积分明细", dtos);
        } catch (Exception e) {
            return new CommonResult<>(500, "获取积分明细失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取可用积分的详细信息
     */
    @GetMapping("/available-details/{userId}")
    public CommonResult<List<IntegralDTO>> getAvailableIntegralDetails(@PathVariable("userId") Long userId) {
        try {
            List<Integral> integrals = integralService.getAvailableIntegralDetails(userId);
            List<IntegralDTO> dtos = integrals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new CommonResult<>(200, "成功获取可用积分详细信息", dtos);
        } catch (Exception e) {
            return new CommonResult<>(500, "获取可用积分详细信息失败: " + e.getMessage(), null);
        }
    }

    /**
     * 手动添加积分（管理员功能）
     */
    @PostMapping("/add")
    public CommonResult<IntegralDTO> addIntegral(@RequestParam Long userId,
                                                 @RequestParam Integer amount,
                                                 @RequestParam String channel,
                                                 @RequestParam(required = false) Long businessId,
                                                 @RequestParam(required = false) String description) {
        try {
            Integral integral = integralService.addIntegral(userId, amount, channel, businessId, description);
            IntegralDTO dto = convertToDTO(integral);
            return new CommonResult<>(200, "成功添加积分", dto);
        } catch (IllegalArgumentException e) {
            return new CommonResult<>(400, "无效的积分获得途径: " + e.getMessage(), null);
        } catch (Exception e) {
            return new CommonResult<>(500, "添加积分失败: " + e.getMessage(), null);
        }
    }

    /**
     * 消费积分
     */
    @PostMapping("/consume")
    public CommonResult<List<IntegralDTO>> consumeIntegral(@RequestParam Long userId,
                                                           @RequestParam Integer amount,
                                                           @RequestParam String channel,
                                                           @RequestParam(required = false) Long businessId,
                                                           @RequestParam(required = false) String description) {
        try {
            List<Integral> integrals = integralService.consumeIntegral(userId, amount, channel, businessId, description);
            List<IntegralDTO> dtos = integrals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new CommonResult<>(200, "成功消费积分", dtos);
        } catch (IllegalArgumentException e) {
            return new CommonResult<>(400, "无效的积分消费途径或参数: " + e.getMessage(), null);
        } catch (Exception e) {
            return new CommonResult<>(500, "积分消费失败: " + e.getMessage(), null);
        }
    }

    /**
     * 积分兑换商品
     */
    @PostMapping("/exchange")
    public CommonResult<List<IntegralDTO>> exchangeGoods(@RequestParam Long userId,
                                                        @RequestParam Long goodsId,
                                                        @RequestParam Integer goodsIntegral) {
        try {
            // 消费积分兑换商品
            List<Integral> integrals = integralService.consumeIntegral(userId, goodsIntegral,
                    Integral.CHANNEL_EXCHANGE, goodsId, "兑换商品");
            List<IntegralDTO> dtos = integrals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new CommonResult<>(200, "成功兑换商品", dtos);
        } catch (Exception e) {
            return new CommonResult<>(500, "积分兑换失败: " + e.getMessage(), null);
        }
    }

    /**
     * 计算积分抵扣后的订单金额
     */
    @GetMapping("/calculate-deduction")
    public CommonResult<Double> calculateDeduction(@RequestParam Double orderAmount,
                                                   @RequestParam Integer integralAmount) {
        try {
            Double deductedAmount = integralService.calculateDeductedAmount(orderAmount, integralAmount);
            return new CommonResult<>(200, "成功计算抵扣金额", deductedAmount);
        } catch (Exception e) {
            return new CommonResult<>(500, "计算积分抵扣金额失败: " + e.getMessage(), null);
        }
    }

    /**
     * 积分抵扣订单金额
     */
    @PostMapping("/deduct-order")
    public CommonResult<List<IntegralDTO>> deductOrder(@RequestParam Long userId,
                                                       @RequestParam Long orderId,
                                                       @RequestParam Double orderAmount,
                                                       @RequestParam Integer integralAmount) {
        try {
            // 计算实际可抵扣的积分数量（确保不超过订单金额）
            Integer actualIntegral = Math.min(integralAmount, (int) Math.floor(orderAmount * 100));
            // 消费积分抵扣订单金额
            List<Integral> integrals = integralService.consumeIntegral(userId, actualIntegral,
                    Integral.CHANNEL_ORDER_DEDUCTION, orderId, "订单抵扣");
            List<IntegralDTO> dtos = integrals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new CommonResult<>(200, "成功抵扣订单", dtos);
        } catch (Exception e) {
            return new CommonResult<>(500, "积分抵扣失败: " + e.getMessage(), null);
        }
    }

    /**
     * 用户签到
     */
    @PostMapping("/sign-in/{userId}")
    public CommonResult<Integer> signIn(@PathVariable("userId") Long userId) {
        try {
            Integer pointsAwarded = integralService.signIn(userId);
            if (pointsAwarded == 0) {
                return new CommonResult<>(400, "今天已签到，请明天再来", 0);
            }
            return new CommonResult<>(200, "签到成功", pointsAwarded);
        } catch (Exception e) {
            return new CommonResult<>(500, "签到失败: " + e.getMessage(), null);
        }
    }

    /**
     * 检查今天是否已签到
     */
    @GetMapping("/sign-in/check/{userId}")
    public CommonResult<Boolean> checkSignInToday(@PathVariable("userId") Long userId) {
        try {
            boolean isSignedIn = integralService.isSignedInToday(userId);
            return new CommonResult<>(200, "成功检查签到状态", isSignedIn);
        } catch (Exception e) {
            return new CommonResult<>(500, "检查签到状态失败: " + e.getMessage(), null);
        }
    }

    /**
     * 评论获取积分
     * 规则：
     * - 评论字数 < 15: 0 积分
     * - 评论字数 >= 15 且无图: 5 积分
     * - 评论字数 >= 15 且有图: 10 积分
     * - 积分有效期 1 年
     */
    @PostMapping("/comment")
    public CommonResult<Integer> commentEarnIntegral(@RequestParam Long userId,
                                                    @RequestParam Long orderId,
                                                    @RequestParam String content,
                                                    @RequestParam(defaultValue = "false") boolean hasPicture) {
        try {
            // 1. 计算应得积分
            Integer points = integralService.calculateIntegralByComment(content, hasPicture);
            if (points == null || points <= 0) {
                return new CommonResult<>(200, "评论成功但不满足积分条件", 0);
            }

            // 2. 发放积分，渠道区分是否带图
            String channel = hasPicture ? Integral.CHANNEL_PICTURE : Integral.CHANNEL_COMMENT;
            String desc = "订单 " + orderId + " 评论奖励（" + (hasPicture ? "图文" : "纯文字") + "）";
            integralService.addIntegral(userId, points, channel, orderId, desc);

            return new CommonResult<>(200, "评论成功，获得积分", points);
        } catch (Exception e) {
            return new CommonResult<>(500, "评论获取积分失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取积分统计信息
     */
    @GetMapping("/statistics/{userId}")
    public CommonResult<IntegralStatisticsDTO> getIntegralStatistics(@PathVariable("userId") Long userId) {
        try {
            IntegralService.IntegralStatistics statistics = integralService.getIntegralStatistics(userId);

            IntegralStatisticsDTO dto = new IntegralStatisticsDTO();
            dto.setTotalIntegral(statistics.getTotalIntegral());
            dto.setAvailableIntegral(statistics.getAvailableIntegral());
            dto.setExpiredIntegral(statistics.getExpiredIntegral());
            dto.setUsedIntegral(statistics.getUsedIntegral());

            return new CommonResult<>(200, "成功获取积分统计", dto);
        } catch (Exception e) {
            return new CommonResult<>(500, "获取积分统计失败: " + e.getMessage(), null);
        }
    }

    /**
     * 检查用户是否可以领取生日积分
     */
    @GetMapping("/birthday/check/{userId}")
    public CommonResult<Boolean> checkBirthdayIntegral(@PathVariable("userId") Long userId) {
        try {
            boolean canClaim = integralService.canClaimBirthdayIntegral(userId);
            return new CommonResult<>(200, "成功检查生日积分领取资格", canClaim);
        } catch (Exception e) {
            return new CommonResult<>(500, "检查生日积分领取资格失败: " + e.getMessage(), null);
        }
    }

    /**
     * 用户手动领取生日积分
     */
    @PostMapping("/birthday/claim/{userId}")
    public CommonResult<Integer> claimBirthdayIntegral(@PathVariable("userId") Long userId) {
        try {
            Integer claimedIntegral = integralService.claimBirthdayIntegral(userId);
            if (claimedIntegral == 0) {
                return new CommonResult<>(400, "您今天已经领取过生日积分，或者不满足领取条件", 0);
            }
            return new CommonResult<>(200, "成功领取生日积分", claimedIntegral);
        } catch (Exception e) {
            return new CommonResult<>(500, "领取生日积分失败: " + e.getMessage(), null);
        }
    }

    /**
     * 根据订单金额发放积分
     */
    @PostMapping("/earn-by-order")
    public CommonResult<Integer> earnIntegralByOrder(@RequestParam Long userId,
                                                     @RequestParam Long orderId,
                                                     @RequestParam Double orderAmount) {
        try {
            Integer points = integralService.calculateIntegralByOrderAmount(orderAmount);
            if (points > 0) {
                integralService.addIntegral(userId, points, Integral.CHANNEL_CONSUMPTION, orderId,
                                          "订单消费获得积分");
            }
            return new CommonResult<>(200, "成功发放订单积分", points);
        } catch (Exception e) {
            return new CommonResult<>(500, "发放订单积分失败: " + e.getMessage(), null);
        }
    }

    private IntegralDTO convertToDTO(Integral integral) {
        IntegralDTO dto = new IntegralDTO();
        BeanUtils.copyProperties(integral, dto);
        return dto;
    }
}
